package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteCloudSubjectUseCaseTest {

    private val cloud: CloudBackupRepository = mockk()
    private val stateDao: SubjectCloudStateDao = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk()

    private lateinit var useCase: DeleteCloudSubjectUseCase

    @Before
    fun setUp() {
        useCase = DeleteCloudSubjectUseCase(cloud, stateDao, noteRepository)
    }

    @Test
    fun cloudOnly_whenCloudSucceeds_returnsSuccess() = runTest {
        coEvery { cloud.delete("driveFile123") } returns Result.success(Unit)

        val result = useCase.cloudOnly("driveFile123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { cloud.delete("driveFile123") }
    }

    @Test
    fun cloudOnly_whenCloudFails_returnsFailure() = runTest {
        val error = RuntimeException("Network error")
        coEvery { cloud.delete("driveFile123") } returns Result.failure(error)

        val result = useCase.cloudOnly("driveFile123")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun localAndCloud_whenCloudFails_doesNotCallLocalDelete_andReturnsCloudDeleteFailed() = runTest {
        coEvery { cloud.delete("driveFile123") } returns Result.failure(RuntimeException("Drive error"))

        val result = useCase.localAndCloud("user1", "Matemáticas", "driveFile123")

        assertTrue(result.isFailure)
        assertEquals(CloudDeleteFailed, result.exceptionOrNull())
        coVerify(exactly = 0) { noteRepository.deleteSubject(any(), any()) }
    }

    @Test
    fun localAndCloud_whenCloudSucceeds_callsLocalDelete_andReturnsSuccess() = runTest {
        coEvery { cloud.delete("driveFile123") } returns Result.success(Unit)
        coEvery { noteRepository.deleteSubject("user1", "Matemáticas") } returns Result.success(Unit)

        val result = useCase.localAndCloud("user1", "Matemáticas", "driveFile123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { cloud.delete("driveFile123") }
        coVerify(exactly = 1) { noteRepository.deleteSubject("user1", "Matemáticas") }
    }
}
