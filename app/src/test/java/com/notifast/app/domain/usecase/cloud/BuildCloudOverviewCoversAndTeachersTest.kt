package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.CoverBackupStore
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuildCloudOverviewCoversAndTeachersTest {

    private val store: SubjectBackupStore = mockk()
    private val hasher: ContentHasher = mockk()
    private val cloud: CloudBackupRepository = mockk()
    private val stateDao: SubjectCloudStateDao = mockk()
    private val accountRepository: GoogleAccountRepository = mockk()
    private val coverStore: CoverBackupStore = mockk()

    private lateinit var useCase: BuildCloudOverviewUseCase

    private val userId = "u1"
    private val account = CloudAccount("acc1", "user@test.com", "User", null)

    @Before
    fun setUp() {
        useCase = BuildCloudOverviewUseCase(
            store = store,
            hasher = hasher,
            cloud = cloud,
            stateDao = stateDao,
            accountRepository = accountRepository,
            coverStore = coverStore
        )
        every { accountRepository.account } returns flowOf(account)
        coEvery { stateDao.getAll("acc1") } returns emptyList()
    }

    @Test
    fun overview_populatesTeacherCountFromLocalSnapshotAndCloudFile() = runTest {
        val teacher1 = Teacher(id = "t1", firstName = "Alan", lastName = "Turing", email = "turing@cam.ac.uk")
        val teacher2 = Teacher(id = "t2", firstName = "Ada", lastName = "Lovelace", email = "ada@analytical.org")

        val localSnapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub1", name = "Computación", colorHex = "#1E88E5", icon = "computer"),
            teachers = listOf(teacher1, teacher2),
            imageFiles = emptyMap()
        )

        coEvery { store.listSubjectIds(userId) } returns listOf("sub1" to "Computación")
        coEvery { store.readSnapshot(userId, "sub1") } returns Result.success(localSnapshot)
        every { hasher.hash(localSnapshot) } returns "hash1"

        val remoteSubject = CloudFile(
            driveFileId = "drive-sub2",
            kind = "subject",
            subjectId = "sub2",
            subjectName = "Algoritmos",
            icon = "code",
            colorHex = "#43A047",
            contentHash = "hash2",
            uploadedAt = 2000L,
            deviceModel = "Pixel",
            noteCount = 2,
            evaluationCount = 1,
            teacherCount = 3,
            sizeBytes = 1500L,
            schemaVersion = 1
        )

        coEvery { coverStore.read(userId) } returns Result.success(emptyList())
        coEvery { cloud.listFiles() } returns Result.success(listOf(remoteSubject))

        val result = useCase(userId)
        assertTrue(result.isSuccess)
        val overview = result.getOrThrow()

        val localItem = overview.subjects.first { it.subjectId == "sub1" }
        assertEquals(2, localItem.teacherCount)

        val remoteItem = overview.subjects.first { it.subjectId == "sub2" }
        assertEquals(3, remoteItem.teacherCount)
    }

    @Test
    fun overview_coversStatus_whenSynced() = runTest {
        coEvery { store.listSubjectIds(userId) } returns emptyList()

        val drafts = listOf(
            CoverDraft(id = "c1", userId = userId, templateType = CoverTemplateType.APA_7, workTitle = "Tesis 1")
        )
        coEvery { coverStore.read(userId) } returns Result.success(drafts)
        every { hasher.hashCovers(drafts) } returns "covers-hash-123"

        val cloudCovers = CloudFile(
            driveFileId = "drive-covers-1",
            kind = "covers",
            subjectId = null,
            subjectName = "Portadas de Estudio",
            icon = "📄",
            colorHex = null,
            contentHash = "covers-hash-123",
            uploadedAt = 1000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            coverCount = 1,
            sizeBytes = 800L,
            schemaVersion = 1
        )
        coEvery { cloud.listFiles() } returns Result.success(listOf(cloudCovers))

        val result = useCase(userId)
        assertTrue(result.isSuccess)
        val overview = result.getOrThrow()

        assertNotNull(overview.covers)
        assertEquals("drive-covers-1", overview.covers?.driveFileId)
        assertEquals(1, overview.localCoverCount)
        assertEquals(CloudSubjectStatus.SYNCED, overview.coversStatus)
    }

    @Test
    fun overview_coversStatus_whenLocalChanges() = runTest {
        coEvery { store.listSubjectIds(userId) } returns emptyList()

        val drafts = listOf(
            CoverDraft(id = "c1", userId = userId, templateType = CoverTemplateType.APA_7, workTitle = "Tesis 1 Modificada")
        )
        coEvery { coverStore.read(userId) } returns Result.success(drafts)
        every { hasher.hashCovers(drafts) } returns "new-hash"

        val cloudCovers = CloudFile(
            driveFileId = "drive-covers-1",
            kind = "covers",
            subjectId = null,
            subjectName = "Portadas de Estudio",
            icon = "📄",
            colorHex = null,
            contentHash = "old-hash",
            uploadedAt = 1000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            coverCount = 1,
            sizeBytes = 800L,
            schemaVersion = 1
        )
        coEvery { cloud.listFiles() } returns Result.success(listOf(cloudCovers))

        val result = useCase(userId)
        assertTrue(result.isSuccess)
        val overview = result.getOrThrow()

        assertEquals(1, overview.localCoverCount)
        assertEquals(CloudSubjectStatus.LOCAL_CHANGES, overview.coversStatus)
    }

    @Test
    fun overview_coversStatus_whenLocalOnly() = runTest {
        coEvery { store.listSubjectIds(userId) } returns emptyList()

        val drafts = listOf(
            CoverDraft(id = "c1", userId = userId, templateType = CoverTemplateType.APA_7, workTitle = "Tesis Local")
        )
        coEvery { coverStore.read(userId) } returns Result.success(drafts)
        every { hasher.hashCovers(drafts) } returns "local-hash"
        coEvery { cloud.listFiles() } returns Result.success(emptyList())

        val result = useCase(userId)
        assertTrue(result.isSuccess)
        val overview = result.getOrThrow()

        assertNull(overview.covers)
        assertEquals(1, overview.localCoverCount)
        assertEquals(CloudSubjectStatus.LOCAL_ONLY, overview.coversStatus)
    }

    @Test
    fun overview_coversStatus_whenCloudOnly() = runTest {
        coEvery { store.listSubjectIds(userId) } returns emptyList()

        coEvery { coverStore.read(userId) } returns Result.success(emptyList())

        val cloudCovers = CloudFile(
            driveFileId = "drive-covers-1",
            kind = "covers",
            subjectId = null,
            subjectName = "Portadas de Estudio",
            icon = "📄",
            colorHex = null,
            contentHash = "cloud-hash",
            uploadedAt = 1000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            coverCount = 2,
            sizeBytes = 800L,
            schemaVersion = 1
        )
        coEvery { cloud.listFiles() } returns Result.success(listOf(cloudCovers))

        val result = useCase(userId)
        assertTrue(result.isSuccess)
        val overview = result.getOrThrow()

        assertNotNull(overview.covers)
        assertEquals(0, overview.localCoverCount)
        assertEquals(CloudSubjectStatus.CLOUD_ONLY, overview.coversStatus)
    }
}
