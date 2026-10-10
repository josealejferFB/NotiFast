package com.notifast.app.presentation.feature.editschedule

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.usecase.ValidateScheduleOverlapUseCase
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val validateScheduleOverlapUseCase = ValidateScheduleOverlapUseCase()
    private val scheduleClassAlertUseCase: com.notifast.app.domain.usecase.ScheduleClassAlertUseCase = mockk(relaxed = true)
    private val cancelAlertUseCase: com.notifast.app.domain.usecase.CancelAlertUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )
    private val sampleSchedules = listOf(
        Schedule(id = "1", subjectName = "Matemáticas", dayOfWeek = 1, startTime = "08:00", endTime = "10:00"),
        Schedule(id = "2", subjectName = "Física", dayOfWeek = 1, startTime = "10:00", endTime = "12:00")
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(sampleSchedules)
    }

    @Test
    fun `when initialized with scheduleId, loads existing schedule fields`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("scheduleId" to "1"))
        val viewModel = EditScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase, cancelAlertUseCase, savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Matemáticas", state.subjectName)
        assertEquals("08:00", state.startTime)
        assertEquals("10:00", state.endTime)
    }

    @Test
    fun `when time changed to overlap with different schedule, conflictingSchedules populated`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("scheduleId" to "1"))
        val viewModel = EditScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase, cancelAlertUseCase, savedStateHandle)
        advanceUntilIdle()

        viewModel.onEvent(EditScheduleEvent.UpdateStartTime("10:30"))
        viewModel.onEvent(EditScheduleEvent.UpdateEndTime("11:30"))

        assertEquals(1, viewModel.uiState.value.conflictingSchedules.size)
        assertEquals("2", viewModel.uiState.value.conflictingSchedules.first().id)
    }

    @Test
    fun `when saving updated schedule, calls updateSchedule`() = runTest {
        coEvery { scheduleRepository.updateSchedule(any(), any()) } returns Result.success(Unit)
        val savedStateHandle = SavedStateHandle(mapOf("scheduleId" to "1"))
        val viewModel = EditScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase, cancelAlertUseCase, savedStateHandle)
        advanceUntilIdle()

        viewModel.onEvent(EditScheduleEvent.UpdateClassroom("Aula 302"))
        viewModel.onEvent(EditScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify { scheduleRepository.updateSchedule("user_123", any()) }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `when initialized with empty savedStateHandle, does not crash and Init event loads data`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = EditScheduleViewModel(
            scheduleRepository,
            authRepository,
            noteRepository,
            validateScheduleOverlapUseCase,
            scheduleClassAlertUseCase,
            cancelAlertUseCase,
            savedStateHandle
        )
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.subjectName)

        viewModel.onEvent(EditScheduleEvent.Init("1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Matemáticas", state.subjectName)
        assertEquals("08:00", state.startTime)
        assertEquals("10:00", state.endTime)
    }
}
