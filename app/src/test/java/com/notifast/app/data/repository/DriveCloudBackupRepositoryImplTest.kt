package com.notifast.app.data.repository

import com.notifast.app.data.remote.drive.DriveException
import com.notifast.app.data.remote.drive.DriveFileDto
import com.notifast.app.data.remote.drive.DriveRestClient
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.GoogleAccountRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DriveCloudBackupRepositoryImplTest {

    private val client: DriveRestClient = mockk()
    private val accountRepository: GoogleAccountRepository = mockk()
    private lateinit var repository: DriveCloudBackupRepositoryImpl

    @Before
    fun setUp() {
        repository = DriveCloudBackupRepositoryImpl(client, accountRepository)
    }

    @Test
    fun listFiles_mapsSubjectAndGlobalProps_andSkipsUnknownKinds() = runTest {
        coEvery { accountRepository.accessToken(forceRefresh = false) } returns Result.success("token123")

        val files = listOf(
            DriveFileDto(
                id = "sub1",
                name = "subject_1.nfbak",
                size = 1000L,
                modifiedTime = "2026-10-15T10:00:00Z",
                appProperties = mapOf(
                    "kind" to "subject",
                    "subjectId" to "id1",
                    "subjectName" to "Matemáticas",
                    "icon" to "calculator",
                    "colorHex" to "#BF092F",
                    "contentHash" to "hash1",
                    "uploadedAt" to "1729000000000",
                    "deviceModel" to "Pixel 8",
                    "noteCount" to "5",
                    "evaluationCount" to "2",
                    "schemaVersion" to "1"
                )
            ),
            DriveFileDto(
                id = "glob1",
                name = "global.nfbak",
                size = 2000L,
                modifiedTime = "2026-10-15T11:00:00Z",
                appProperties = mapOf(
                    "kind" to "global",
                    "contentHash" to "hashGlobal",
                    "uploadedAt" to "1729000001000",
                    "deviceModel" to "Pixel 8",
                    "schemaVersion" to "1"
                )
            ),
            DriveFileDto(
                id = "unknown1",
                name = "random.bin",
                size = 500L,
                modifiedTime = "2026-10-15T12:00:00Z",
                appProperties = mapOf(
                    "kind" to "unknown"
                )
            ),
            DriveFileDto(
                id = "corrupt1",
                name = "subject_corrupt.nfbak",
                size = 500L,
                appProperties = mapOf(
                    "kind" to "subject",
                    "schemaVersion" to "not-a-number"
                )
            )
        )

        coEvery { client.list("token123") } returns files

        val result = repository.listFiles()

        assertTrue(result.isSuccess)
        val cloudFiles = result.getOrThrow()
        assertEquals(2, cloudFiles.size)

        val subjectFile = cloudFiles.first { it.kind == "subject" }
        assertEquals("sub1", subjectFile.driveFileId)
        assertEquals("id1", subjectFile.subjectId)
        assertEquals("Matemáticas", subjectFile.subjectName)
        assertEquals("calculator", subjectFile.icon)
        assertEquals("#BF092F", subjectFile.colorHex)
        assertEquals("hash1", subjectFile.contentHash)
        assertEquals(1729000000000L, subjectFile.uploadedAt)
        assertEquals("Pixel 8", subjectFile.deviceModel)
        assertEquals(5, subjectFile.noteCount)
        assertEquals(2, subjectFile.evaluationCount)
        assertEquals(1000L, subjectFile.sizeBytes)
        assertEquals(1, subjectFile.schemaVersion)

        val globalFile = cloudFiles.first { it.kind == "global" }
        assertEquals("glob1", globalFile.driveFileId)
        assertEquals(null, globalFile.subjectId)
        assertEquals("hashGlobal", globalFile.contentHash)
        assertEquals(2000L, globalFile.sizeBytes)
        assertEquals(1, globalFile.schemaVersion)
    }

    @Test
    fun upload_whenExistingFileIdProvidedAndReturns404_fallsBackToCreate() = runTest {
        coEvery { accountRepository.accessToken(forceRefresh = false) } returns Result.success("token123")
        val dummyFile = File.createTempFile("test", "bak")

        coEvery {
            client.update("token123", "oldFileId", any(), dummyFile)
        } throws DriveException(404, "Not Found")

        coEvery {
            client.create("token123", "subject_1.nfbak", any(), dummyFile)
        } returns DriveFileDto(id = "newFileId", name = "subject_1.nfbak")

        val result = repository.upload(
            existingFileId = "oldFileId",
            fileName = "subject_1.nfbak",
            props = mapOf("kind" to "subject"),
            file = dummyFile
        )

        assertTrue(result.isSuccess)
        assertEquals("newFileId", result.getOrThrow())
        coVerify(exactly = 1) { client.create("token123", "subject_1.nfbak", any(), dummyFile) }
    }

    @Test
    fun upload_whenClientThrows401_retriesWithForceRefreshAndSucceeds() = runTest {
        coEvery { accountRepository.accessToken(forceRefresh = false) } returns Result.success("expiredToken")
        coEvery { accountRepository.accessToken(forceRefresh = true) } returns Result.success("freshToken")

        val dummyFile = File.createTempFile("test", "bak")

        coEvery {
            client.create("expiredToken", "subject_1.nfbak", any(), dummyFile)
        } throws DriveException(401, "Invalid Credentials")

        coEvery {
            client.create("freshToken", "subject_1.nfbak", any(), dummyFile)
        } returns DriveFileDto(id = "fileCreatedAfterRefresh", name = "subject_1.nfbak")

        val result = repository.upload(
            existingFileId = null,
            fileName = "subject_1.nfbak",
            props = mapOf("kind" to "subject"),
            file = dummyFile
        )

        assertTrue(result.isSuccess)
        assertEquals("fileCreatedAfterRefresh", result.getOrThrow())
        coVerify(exactly = 1) { accountRepository.accessToken(forceRefresh = true) }
    }

    @Test
    fun operation_whenDouble401_failsWithReauthRequired() = runTest {
        coEvery { accountRepository.accessToken(forceRefresh = false) } returns Result.success("token1")
        coEvery { accountRepository.accessToken(forceRefresh = true) } returns Result.success("token2")

        coEvery { client.list("token1") } throws DriveException(401, "Invalid Credentials")
        coEvery { client.list("token2") } throws DriveException(401, "Invalid Credentials")

        val result = repository.listFiles()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CloudAuthException)
        assertEquals(CloudAuthException.Reason.REAUTH_REQUIRED, (exception as CloudAuthException).reason)
    }

    @Test
    fun delete_callsClientDelete_andHandlesErrors() = runTest {
        coEvery { accountRepository.accessToken(forceRefresh = false) } returns Result.success("token123")
        coEvery { client.delete("token123", "fileId123") } returns Unit

        val result = repository.delete("fileId123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { client.delete("token123", "fileId123") }
    }

    @Test
    fun download_callsClientDownload_andHandlesErrors() = runTest {
        coEvery { accountRepository.accessToken(forceRefresh = false) } returns Result.success("token123")
        val target = File.createTempFile("target", "bak")
        coEvery { client.download("token123", "fileId123", target) } returns Unit

        val result = repository.download("fileId123", target)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { client.download("token123", "fileId123", target) }
    }
}
