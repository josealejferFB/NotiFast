package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BuildCloudOverviewUseCaseTest {

    private val store: SubjectBackupStore = mockk()
    private val hasher: ContentHasher = mockk()
    private val cloud: CloudBackupRepository = mockk()
    private val stateDao: SubjectCloudStateDao = mockk()
    private val accountRepository: GoogleAccountRepository = mockk()

    private lateinit var useCase: BuildCloudOverviewUseCase

    @Before
    fun setUp() {
        useCase = BuildCloudOverviewUseCase(store, hasher, cloud, stateDao, accountRepository)
    }

    private fun createCloudFile(
        id: String = "file1",
        subjectId: String = "sub1",
        hash: String = "hashA"
    ) = CloudFile(
        driveFileId = id,
        kind = "subject",
        subjectId = subjectId,
        subjectName = "Test Subject",
        icon = "book",
        colorHex = "#BF092F",
        contentHash = hash,
        uploadedAt = 1000L,
        deviceModel = "Pixel",
        noteCount = 1,
        evaluationCount = 1,
        sizeBytes = 100L,
        schemaVersion = 1
    )

    private fun createState(
        subjectId: String = "sub1",
        fileId: String = "file1",
        hash: String = "hashA"
    ) = SubjectCloudStateEntity(
        accountId = "acc1",
        subjectId = subjectId,
        driveFileId = fileId,
        lastUploadedHash = hash,
        lastSyncedAt = 1000L
    )

    @Test
    fun classify_whenLocalMatchesSaved_andCloudMatchesSaved_returnsSynced() {
        val cloud = createCloudFile(hash = "hashA")
        val state = createState(hash = "hashA")

        val status = useCase.classify(localHash = "hashA", cloud = cloud, state = state)
        assertEquals(CloudSubjectStatus.SYNCED, status)
    }

    @Test
    fun classify_whenLocalDiffersFromSaved_andCloudMatchesSaved_returnsLocalChanges() {
        val cloud = createCloudFile(hash = "hashA")
        val state = createState(hash = "hashA")

        val status = useCase.classify(localHash = "hashB", cloud = cloud, state = state)
        assertEquals(CloudSubjectStatus.LOCAL_CHANGES, status)
    }

    @Test
    fun classify_whenLocalMatchesSaved_andCloudDiffersFromSaved_returnsCloudNewer() {
        val cloud = createCloudFile(hash = "hashCloud")
        val state = createState(hash = "hashA")

        val status = useCase.classify(localHash = "hashA", cloud = cloud, state = state)
        assertEquals(CloudSubjectStatus.CLOUD_NEWER, status)
    }

    @Test
    fun classify_whenBothDifferFromSaved_andFromEachOther_returnsBothUnlinked() {
        val cloud = createCloudFile(hash = "hashCloud")
        val state = createState(hash = "hashSaved")

        val status = useCase.classify(localHash = "hashLocal", cloud = cloud, state = state)
        assertEquals(CloudSubjectStatus.BOTH_UNLINKED, status)
    }

    @Test
    fun classify_whenBothPresentWithoutSavedState_andHashesDiffer_returnsBothUnlinked() {
        val cloud = createCloudFile(hash = "hashCloud")

        val status = useCase.classify(localHash = "hashLocal", cloud = cloud, state = null)
        assertEquals(CloudSubjectStatus.BOTH_UNLINKED, status)
    }

    @Test
    fun classify_whenBothPresentWithoutSavedState_andHashesMatch_returnsSynced() {
        val cloud = createCloudFile(hash = "hashMatch")

        val status = useCase.classify(localHash = "hashMatch", cloud = cloud, state = null)
        assertEquals(CloudSubjectStatus.SYNCED, status)
    }

    @Test
    fun classify_whenLocalPresentAndCloudAbsent_returnsLocalOnly() {
        val status = useCase.classify(localHash = "hashLocal", cloud = null, state = null)
        assertEquals(CloudSubjectStatus.LOCAL_ONLY, status)
    }

    @Test
    fun classify_whenLocalAbsentAndCloudPresent_returnsCloudOnly() {
        val cloud = createCloudFile(hash = "hashCloud")
        val status = useCase.classify(localHash = null, cloud = cloud, state = null)
        assertEquals(CloudSubjectStatus.CLOUD_ONLY, status)
    }

    @Test
    fun invoke_mergesLocalAndCloudAndOrdersByName() = runTest {
        val account = CloudAccount("acc1", "user@test.com", "User", null)
        every { accountRepository.account } returns flowOf(account)

        val localSnapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub1", name = "Matemáticas", colorHex = "#BF092F", icon = "calculator"),
            imageFiles = emptyMap()
        )
        coEvery { store.listSubjectIds("u1") } returns listOf("sub1" to "Matemáticas")
        coEvery { store.readSnapshot("u1", "sub1") } returns Result.success(localSnapshot)
        every { hasher.hash(localSnapshot) } returns "hash1"

        val cloudSubject = createCloudFile(id = "drive1", subjectId = "sub2", hash = "hash2").copy(
            subjectName = "Álgebra"
        )
        val cloudGlobal = CloudFile(
            driveFileId = "globalDrive",
            kind = "global",
            subjectId = null,
            subjectName = null,
            icon = null,
            colorHex = null,
            contentHash = "hashG",
            uploadedAt = 1000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            sizeBytes = 50L,
            schemaVersion = 1
        )
        coEvery { cloud.listFiles() } returns Result.success(listOf(cloudSubject, cloudGlobal))
        coEvery { stateDao.getAll("acc1") } returns emptyList()

        val result = useCase("u1")

        assertTrue(result.isSuccess)
        val overview = result.getOrThrow()
        assertEquals(2, overview.subjects.size)
        // Ordered alphabetically: Álgebra before Matemáticas
        assertEquals("Álgebra", overview.subjects[0].name)
        assertEquals(CloudSubjectStatus.CLOUD_ONLY, overview.subjects[0].status)

        assertEquals("Matemáticas", overview.subjects[1].name)
        assertEquals(CloudSubjectStatus.LOCAL_ONLY, overview.subjects[1].status)

        assertEquals("globalDrive", overview.global?.driveFileId)
    }
}
