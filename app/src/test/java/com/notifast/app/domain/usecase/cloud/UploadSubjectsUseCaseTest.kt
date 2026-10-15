package com.notifast.app.domain.usecase.cloud

import app.cash.turbine.test
import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.backup.GlobalSnapshot
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.model.cloud.UploadProgress
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GlobalBackupStore
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
class UploadSubjectsUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val store: SubjectBackupStore = mockk()
    private val hasher: ContentHasher = mockk()
    private val archiveCodec: BackupArchiveCodec = mockk()
    private val cloud: CloudBackupRepository = mockk()
    private val stateDao: SubjectCloudStateDao = mockk(relaxed = true)
    private val accountRepository: GoogleAccountRepository = mockk()
    private val globalStore: GlobalBackupStore = mockk()

    private lateinit var uploadSubjectsUseCase: UploadSubjectsUseCase
    private lateinit var uploadGlobalUseCase: UploadGlobalDataUseCase

    @Before
    fun setUp() {
        uploadSubjectsUseCase = UploadSubjectsUseCase(
            store, hasher, archiveCodec, cloud, stateDao, accountRepository
        )
        uploadGlobalUseCase = UploadGlobalDataUseCase(
            globalStore, hasher, archiveCodec, cloud, accountRepository
        )
    }

    private fun mockSubjectItem(id: String, name: String, driveFileId: String? = null) = CloudSubjectItem(
        subjectId = id,
        name = name,
        icon = "📚",
        colorHex = "#BF092F",
        status = CloudSubjectStatus.LOCAL_CHANGES,
        driveFileId = driveFileId,
        localHash = "localHash_$id",
        cloudFile = null
    )

    private fun mockSnapshot(id: String, name: String) = SubjectSnapshot(
        subject = SubjectData(id = id, name = name, colorHex = "#BF092F", icon = "📚"),
        imageFiles = emptyMap()
    )

    @Test
    fun invoke_withThreeItemsWhereSecondFails_producesPartialSuccessAndCleansTempDir() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val item1 = mockSubjectItem("sub1", "Matemáticas")
        val item2 = mockSubjectItem("sub2", "Física")
        val item3 = mockSubjectItem("sub3", "Química")
        val items = listOf(item1, item2, item3)

        val snap1 = mockSnapshot("sub1", "Matemáticas")
        val snap2 = mockSnapshot("sub2", "Física")
        val snap3 = mockSnapshot("sub3", "Química")

        coEvery { store.readSnapshot("u1", "sub1") } returns Result.success(snap1)
        coEvery { store.readSnapshot("u1", "sub2") } returns Result.success(snap2)
        coEvery { store.readSnapshot("u1", "sub3") } returns Result.success(snap3)

        every { hasher.hash(snap1) } returns "hash1"
        every { hasher.hash(snap2) } returns "hash2"
        every { hasher.hash(snap3) } returns "hash3"

        every { archiveCodec.writeSubject(snap1, any(), any()) } returns Result.success(Unit)
        every { archiveCodec.writeSubject(snap2, any(), any()) } returns Result.failure(RuntimeException("Codec error"))
        every { archiveCodec.writeSubject(snap3, any(), any()) } returns Result.success(Unit)

        val propsSlot = slot<Map<String, String>>()
        coEvery {
            cloud.upload(null, "subject_sub1.nfbak", capture(propsSlot), any())
        } returns Result.success("driveFile1")

        coEvery {
            cloud.upload(null, "subject_sub3.nfbak", any(), any())
        } returns Result.success("driveFile3")

        val cacheDir = tempFolder.newFolder("cache")

        uploadSubjectsUseCase("u1", items, cacheDir).test {
            // Emits progress updates
            val p1 = awaitItem() as UploadProgress.Progress
            assertEquals("Matemáticas", p1.current)
            assertEquals(1, p1.done)

            val p2 = awaitItem() as UploadProgress.Progress
            assertEquals("Física", p2.current)
            assertEquals(2, p2.done)

            val p3 = awaitItem() as UploadProgress.Progress
            assertEquals("Química", p3.current)
            assertEquals(3, p3.done)

            // Final done event
            val done = awaitItem() as UploadProgress.Done
            val result = done.result
            assertEquals(3, result.outcomes.size)

            assertEquals("sub1", result.outcomes[0].id)
            assertNull(result.outcomes[0].error)

            assertEquals("sub2", result.outcomes[1].id)
            assertNotNull(result.outcomes[1].error)

            assertEquals("sub3", result.outcomes[2].id)
            assertNull(result.outcomes[2].error)

            awaitComplete()
        }

        // Verify state upserted only for sub1 and sub3
        coVerify(exactly = 1) {
            stateDao.upsert(match { it.subjectId == "sub1" && it.driveFileId == "driveFile1" && it.lastUploadedHash == "hash1" })
        }
        coVerify(exactly = 0) {
            stateDao.upsert(match { it.subjectId == "sub2" })
        }
        coVerify(exactly = 1) {
            stateDao.upsert(match { it.subjectId == "sub3" && it.driveFileId == "driveFile3" && it.lastUploadedHash == "hash3" })
        }

        // Verify props
        val capturedProps = propsSlot.captured
        assertEquals("subject", capturedProps["kind"])
        assertEquals("sub1", capturedProps["subjectId"])
        assertEquals("hash1", capturedProps["contentHash"])

        // Verify temp dir is clean
        val remainingFiles = cacheDir.listFiles()?.filter { it.name.startsWith("upload_") } ?: emptyList()
        assertTrue(remainingFiles.isEmpty())
    }

    @Test
    fun uploadGlobal_uploadsGlobalDataAndProps() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val globalSnap = GlobalSnapshot(
            firstName = "John",
            lastName = "Doe",
            prefs = mapOf("theme" to "dark"),
            teachers = emptyList(),
            avatar = null
        )
        coEvery { globalStore.read("u1") } returns Result.success(globalSnap)
        every { hasher.hashGlobal(globalSnap) } returns "hashGlobal"
        every { archiveCodec.writeGlobal(globalSnap, any(), any()) } returns Result.success(Unit)

        val propsSlot = slot<Map<String, String>>()
        coEvery {
            cloud.upload(null, "global.nfbak", capture(propsSlot), any())
        } returns Result.success("globalDriveId")

        val cacheDir = tempFolder.newFolder("cacheGlobal")

        val result = uploadGlobalUseCase("u1", existing = null, cacheDir = cacheDir)

        assertTrue(result.isSuccess)
        val capturedProps = propsSlot.captured
        assertEquals("global", capturedProps["kind"])
        assertEquals("hashGlobal", capturedProps["contentHash"])

        val remainingFiles = cacheDir.listFiles()?.filter { it.name.startsWith("upload_global_") } ?: emptyList()
        assertTrue(remainingFiles.isEmpty())
    }
}
