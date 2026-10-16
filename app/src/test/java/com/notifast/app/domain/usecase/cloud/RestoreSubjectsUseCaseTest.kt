package com.notifast.app.domain.usecase.cloud

import app.cash.turbine.test
import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.backup.GlobalSnapshot
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.model.cloud.RestoreDecision
import com.notifast.app.domain.model.cloud.UploadProgress
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GlobalBackupStore
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase
import com.notifast.app.domain.usecase.ResolveSubjectNameUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class RestoreSubjectsUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val store: SubjectBackupStore = mockk()
    private val hasher: ContentHasher = mockk()
    private val archiveCodec: BackupArchiveCodec = mockk()
    private val cloud: CloudBackupRepository = mockk()
    private val stateDao: SubjectCloudStateDao = mockk(relaxed = true)
    private val accountRepository: GoogleAccountRepository = mockk()
    private val resolveSubjectNameUseCase: ResolveSubjectNameUseCase = ResolveSubjectNameUseCase()
    private val rescheduleAllAlertsUseCase: RescheduleAllAlertsUseCase = mockk(relaxed = true)
    private val globalStore: GlobalBackupStore = mockk()

    private lateinit var restoreSubjectsUseCase: RestoreSubjectsUseCase
    private lateinit var restoreGlobalUseCase: RestoreGlobalDataUseCase

    @Before
    fun setUp() {
        restoreSubjectsUseCase = RestoreSubjectsUseCase(
            store, hasher, archiveCodec, cloud, stateDao, accountRepository,
            resolveSubjectNameUseCase, rescheduleAllAlertsUseCase
        )
        restoreGlobalUseCase = RestoreGlobalDataUseCase(
            globalStore, archiveCodec, cloud
        )
    }

    private fun mockSubjectItem(id: String, name: String, driveFileId: String = "drive_$id") = CloudSubjectItem(
        subjectId = id,
        name = name,
        icon = "📚",
        colorHex = "#BF092F",
        status = CloudSubjectStatus.CLOUD_ONLY,
        driveFileId = driveFileId,
        localHash = null,
        cloudFile = CloudFile(
            driveFileId = driveFileId,
            kind = "subject",
            subjectId = id,
            subjectName = name,
            icon = "📚",
            colorHex = "#BF092F",
            contentHash = "hash_$id",
            uploadedAt = 1000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            sizeBytes = 100L,
            schemaVersion = 1
        )
    )

    private fun mockSnapshot(id: String, name: String) = SubjectSnapshot(
        subject = SubjectData(id = id, name = name, colorHex = "#BF092F", icon = "📚"),
        imageFiles = emptyMap()
    )

    @Test
    fun findConflicts_detectsBatchInternalCollision() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)
        coEvery { stateDao.getAll("acc1") } returns emptyList()
        coEvery { store.listSubjectIds("u1") } returns emptyList()

        val item1 = mockSubjectItem("sub1", "Física")
        val item2 = mockSubjectItem("sub2", "física ")

        val conflicts = restoreSubjectsUseCase.findConflicts("u1", listOf(item1, item2))

        assertEquals(1, conflicts.size)
        assertEquals("sub2", conflicts[0].item.subjectId)
        assertEquals("Física (1)", conflicts[0].suggestedName)
    }

    @Test
    fun restore_noConflict_insertsWithCloudIdAndUpsertsStateWithRecomputedHash() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val item = mockSubjectItem("cloudSub1", "Biología")
        val snap = mockSnapshot("cloudSub1", "Biología")

        coEvery { cloud.download("drive_cloudSub1", any()) } returns Result.success(Unit)
        every { archiveCodec.readSubject(any(), any()) } returns Result.success(snap)
        coEvery { store.insertSnapshot("u1", snap) } returns Result.success("cloudSub1")
        coEvery { store.readSnapshot("u1", "cloudSub1") } returns Result.success(snap)
        every { hasher.hash(snap) } returns "recomputedHash123"

        val cacheDir = tempFolder.newFolder("restoreCache")

        restoreSubjectsUseCase("u1", listOf(item), emptyMap(), cacheDir).test {
            val p = awaitItem() as UploadProgress.Progress
            assertEquals("Biología", p.current)

            val done = awaitItem() as UploadProgress.Done
            assertEquals(1, done.result.outcomes.size)
            assertNull(done.result.outcomes[0].error)

            awaitComplete()
        }

        coVerify(exactly = 1) { store.insertSnapshot("u1", snap) }
        coVerify(exactly = 1) {
            stateDao.upsert(match {
                it.subjectId == "cloudSub1" &&
                    it.driveFileId == "drive_cloudSub1" &&
                    it.lastUploadedHash == "recomputedHash123"
            })
        }
        coVerify(exactly = 1) { rescheduleAllAlertsUseCase() }
    }

    @Test
    fun restore_withOverwriteDecision_replacesSnapshotAndUpsertsState() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val item = mockSubjectItem("cloudSub1", "Historia")
        val snap = mockSnapshot("cloudSub1", "Historia")

        coEvery { store.listSubjectIds("u1") } returns listOf("localHistId" to "Historia")
        coEvery { cloud.download("drive_cloudSub1", any()) } returns Result.success(Unit)
        every { archiveCodec.readSubject(any(), any()) } returns Result.success(snap)
        coEvery { store.replaceSnapshot("u1", "localHistId", snap) } returns Result.success("cloudSub1")
        coEvery { store.readSnapshot("u1", "cloudSub1") } returns Result.success(snap)
        every { hasher.hash(snap) } returns "histHashRecomputed"

        val cacheDir = tempFolder.newFolder("restoreOverwriteCache")

        restoreSubjectsUseCase(
            "u1",
            listOf(item),
            mapOf("cloudSub1" to RestoreDecision.Overwrite),
            cacheDir
        ).test {
            awaitItem() // progress
            val done = awaitItem() as UploadProgress.Done
            assertNull(done.result.outcomes[0].error)
            awaitComplete()
        }

        coVerify(exactly = 1) { store.replaceSnapshot("u1", "localHistId", snap) }
        coVerify(exactly = 1) {
            stateDao.upsert(match {
                it.subjectId == "cloudSub1" &&
                    it.lastUploadedHash == "histHashRecomputed"
            })
        }
    }

    @Test
    fun restore_withAddAsDecision_insertsWithNewIdAndNoState() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val item = mockSubjectItem("cloudSub1", "Arte")
        val snap = mockSnapshot("cloudSub1", "Arte")

        coEvery { store.listSubjectIds("u1") } returns listOf("oldArteId" to "Arte")
        coEvery { cloud.download("drive_cloudSub1", any()) } returns Result.success(Unit)
        every { archiveCodec.readSubject(any(), any()) } returns Result.success(snap)
        coEvery {
            store.insertSnapshot("u1", snap, newSubjectId = any(), newName = "Arte (1)")
        } returns Result.success("newGeneratedId")

        val cacheDir = tempFolder.newFolder("restoreAddAsCache")

        restoreSubjectsUseCase(
            "u1",
            listOf(item),
            mapOf("cloudSub1" to RestoreDecision.AddAs("Arte (1)")),
            cacheDir
        ).test {
            awaitItem() // progress
            val done = awaitItem() as UploadProgress.Done
            assertNull(done.result.outcomes[0].error)
            awaitComplete()
        }

        coVerify(exactly = 1) {
            store.insertSnapshot("u1", snap, newSubjectId = any(), newName = "Arte (1)")
        }
        // NO state upserted for AddAs!
        coVerify(exactly = 0) { stateDao.upsert(any()) }
    }

    @Test
    fun restore_whenValidationFailsWithWeight150_recordsOutcomeError() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val item = mockSubjectItem("badSub", "Materia Mala")
        val snapWithBadEval = mockSnapshot("badSub", "Materia Mala").copy(
            evaluations = listOf(
                Evaluation(id = "e1", title = "Parcial", weight = 150.0)
            )
        )

        coEvery { cloud.download("drive_badSub", any()) } returns Result.success(Unit)
        every { archiveCodec.readSubject(any(), any()) } returns Result.success(snapWithBadEval)

        val cacheDir = tempFolder.newFolder("restoreBadCache")

        restoreSubjectsUseCase("u1", listOf(item), emptyMap(), cacheDir).test {
            awaitItem() // progress
            val done = awaitItem() as UploadProgress.Done
            assertNotNull(done.result.outcomes[0].error)
            awaitComplete()
        }

        coVerify(exactly = 0) { store.insertSnapshot(any(), any(), any(), any()) }
    }

    @Test
    fun restoreGlobal_appliesDownloadedGlobalSnapshot() = runTest {
        val cloudFile = CloudFile(
            driveFileId = "globalDriveId",
            kind = "global",
            subjectId = null,
            subjectName = null,
            icon = null,
            colorHex = null,
            contentHash = "hashGlobal",
            uploadedAt = 1000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            sizeBytes = 200L,
            schemaVersion = 1
        )
        val globalSnap = GlobalSnapshot(firstName = "Jane", lastName = "Doe")

        coEvery { cloud.download("globalDriveId", any()) } returns Result.success(Unit)
        every { archiveCodec.readGlobal(any(), any()) } returns Result.success(globalSnap)
        coEvery { globalStore.apply("u1", globalSnap) } returns Result.success(Unit)

        val cacheDir = tempFolder.newFolder("globalRestoreCache")

        val result = restoreGlobalUseCase("u1", cloudFile, cacheDir)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { globalStore.apply("u1", globalSnap) }
    }

    @Test
    fun findConflicts_whenLocalContentIdenticalToCloudContent_doesNotTriggerConflict() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)
        coEvery { stateDao.getAll("acc1") } returns emptyList()

        val item = mockSubjectItem("sub1", "Química").copy(
            cloudFile = mockSubjectItem("sub1", "Química").cloudFile?.copy(contentHash = "identicalHash123")
        )
        val snap = mockSnapshot("sub1", "Química")

        coEvery { store.listSubjectIds("u1") } returns listOf("sub1" to "Química")
        coEvery { store.readSnapshot("u1", "sub1") } returns Result.success(snap)
        every { hasher.hash(snap) } returns "identicalHash123"

        val conflicts = restoreSubjectsUseCase.findConflicts("u1", listOf(item))

        assertTrue("Should not trigger conflict when local content is identical to cloud content", conflicts.isEmpty())
    }

    @Test
    fun restore_whenLocalContentIdenticalToCloudContent_isNoOpWithoutDbOverwrite() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val item = mockSubjectItem("sub1", "Química").copy(
            status = CloudSubjectStatus.SYNCED,
            localHash = "identicalHash123",
            cloudFile = mockSubjectItem("sub1", "Química").cloudFile?.copy(contentHash = "identicalHash123")
        )
        val snap = mockSnapshot("sub1", "Química")

        coEvery { store.listSubjectIds("u1") } returns listOf("sub1" to "Química")
        coEvery { store.readSnapshot("u1", "sub1") } returns Result.success(snap)
        every { hasher.hash(snap) } returns "identicalHash123"

        val cacheDir = tempFolder.newFolder("restoreNoOpCache")

        restoreSubjectsUseCase("u1", listOf(item), emptyMap(), cacheDir).test {
            awaitItem() // Progress
            val done = awaitItem() as UploadProgress.Done
            assertEquals(1, done.result.outcomes.size)
            val outcome = done.result.outcomes[0]
            assertNull(outcome.error)
            assertTrue("Outcome should be marked as isNoOp", outcome.isNoOp)
            awaitComplete()
        }

        // Verify neither download nor db replacement/insertion was performed
        coVerify(exactly = 0) { cloud.download(any(), any()) }
        coVerify(exactly = 0) { store.insertSnapshot(any(), any(), any(), any()) }
        coVerify(exactly = 0) { store.replaceSnapshot(any(), any(), any()) }

        // State DAO should be updated to SYNCED
        coVerify(exactly = 1) {
            stateDao.upsert(match {
                it.subjectId == "sub1" &&
                    it.driveFileId == "drive_sub1" &&
                    it.lastUploadedHash == "identicalHash123"
            })
        }
    }

    @Test
    fun restore_whenCancelled_rethrowsCancellationException() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val item = mockSubjectItem("sub1", "Química").copy(
            cloudFile = mockSubjectItem("sub1", "Química").cloudFile?.copy(contentHash = "hash1")
        )

        coEvery { store.listSubjectIds("u1") } returns emptyList()
        coEvery { cloud.download(any(), any()) } throws kotlinx.coroutines.CancellationException("Job cancelled")

        val cacheDir = tempFolder.newFolder("restoreCancelCache")

        var caughtCancellation = false
        try {
            restoreSubjectsUseCase("u1", listOf(item), emptyMap(), cacheDir).collect { }
        } catch (e: kotlinx.coroutines.CancellationException) {
            caughtCancellation = true
        }

        assertTrue("CancellationException must be rethrown and not swallowed as ItemOutcome error", caughtCancellation)
    }
}
