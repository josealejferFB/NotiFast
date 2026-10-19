package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.CoverBackupStore
import com.notifast.app.domain.repository.GoogleAccountRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UploadAndRestoreCoversUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val coverStore: CoverBackupStore = mockk()
    private val hasher: ContentHasher = mockk()
    private val archiveCodec: BackupArchiveCodec = mockk()
    private val cloud: CloudBackupRepository = mockk()
    private val accountRepository: GoogleAccountRepository = mockk()

    private lateinit var uploadCoversUseCase: UploadCoversUseCase
    private lateinit var restoreCoversUseCase: RestoreCoversUseCase

    private val userId = "user-123"
    private val account = CloudAccount("acc1", "user@test.com", "Test User", null)

    @Before
    fun setUp() {
        uploadCoversUseCase = UploadCoversUseCase(
            coverStore = coverStore,
            hasher = hasher,
            archiveCodec = archiveCodec,
            cloud = cloud,
            accountRepository = accountRepository
        )
        restoreCoversUseCase = RestoreCoversUseCase(
            coverStore = coverStore,
            archiveCodec = archiveCodec,
            cloud = cloud
        )
    }

    @Test
    fun uploadCovers_whenContentUnchanged_isNoOp() = runTest {
        val cacheDir = tempFolder.newFolder("cache")
        every { accountRepository.account } returns flowOf(account)

        val drafts = listOf(
            CoverDraft(id = "c1", userId = userId, templateType = CoverTemplateType.APA_7, workTitle = "Tesis")
        )
        coEvery { coverStore.read(userId) } returns Result.success(drafts)
        every { hasher.hashCovers(drafts) } returns "hash-123"

        val existingCloudFile = CloudFile(
            driveFileId = "drive-file-1",
            kind = "covers",
            subjectId = null,
            subjectName = "Portadas de Estudio",
            icon = "📄",
            colorHex = null,
            contentHash = "hash-123",
            uploadedAt = 1000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            coverCount = 1,
            sizeBytes = 500L,
            schemaVersion = 1
        )

        val result = uploadCoversUseCase(userId, existingCloudFile, cacheDir)
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { cloud.upload(any(), any(), any(), any()) }
    }

    @Test
    fun uploadCovers_whenContentChanged_uploadsCoversArchive() = runTest {
        val cacheDir = tempFolder.newFolder("cache")
        every { accountRepository.account } returns flowOf(account)

        val drafts = listOf(
            CoverDraft(id = "c1", userId = userId, templateType = CoverTemplateType.APA_7, workTitle = "Tesis"),
            CoverDraft(id = "c2", userId = userId, templateType = CoverTemplateType.IEEE, workTitle = "Paper")
        )
        coEvery { coverStore.read(userId) } returns Result.success(drafts)
        every { hasher.hashCovers(drafts) } returns "new-hash-456"
        every { archiveCodec.writeCovers(drafts, any(), any()) } returns Result.success(Unit)

        val propsSlot = slot<Map<String, String>>()
        coEvery {
            cloud.upload(
                existingFileId = "drive-file-1",
                fileName = "covers.nfbak",
                props = capture(propsSlot),
                file = any()
            )
        } returns Result.success("drive-file-1")

        val existingCloudFile = CloudFile(
            driveFileId = "drive-file-1",
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
            sizeBytes = 500L,
            schemaVersion = 1
        )

        val result = uploadCoversUseCase(userId, existingCloudFile, cacheDir)
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { cloud.upload("drive-file-1", "covers.nfbak", any(), any()) }
        assertEquals("covers", propsSlot.captured["kind"])
        assertEquals("new-hash-456", propsSlot.captured["contentHash"])
        assertEquals("2", propsSlot.captured["coverCount"])
    }

    @Test
    fun restoreCovers_downloadsAndAppliesDrafts() = runTest {
        val cacheDir = tempFolder.newFolder("cache")
        val cloudFile = CloudFile(
            driveFileId = "drive-file-covers",
            kind = "covers",
            subjectId = null,
            subjectName = "Portadas de Estudio",
            icon = "📄",
            colorHex = null,
            contentHash = "hash-covers",
            uploadedAt = 2000L,
            deviceModel = "Pixel",
            noteCount = 0,
            evaluationCount = 0,
            coverCount = 1,
            sizeBytes = 1200L,
            schemaVersion = 1
        )

        val restoredDrafts = listOf(
            CoverDraft(id = "cd-cloud", userId = "remote", templateType = CoverTemplateType.APA_7, workTitle = "Restaurada")
        )

        coEvery { cloud.download("drive-file-covers", any()) } returns Result.success(Unit)
        every { archiveCodec.readCovers(any(), any()) } returns Result.success(restoredDrafts)
        coEvery { coverStore.apply(userId, restoredDrafts) } returns Result.success(Unit)

        val result = restoreCoversUseCase(userId, cloudFile, cacheDir)
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { cloud.download("drive-file-covers", any()) }
        coVerify(exactly = 1) { coverStore.apply(userId, restoredDrafts) }
    }
}
