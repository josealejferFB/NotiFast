package com.notifast.app.presentation.feature.addschedule

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
class AddScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val validateScheduleOverlapUseCase = ValidateScheduleOverlapUseCase()
    private val scheduleClassAlertUseCase: com.notifast.app.domain.usecase.ScheduleClassAlertUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )
    private val sampleSchedules = listOf(
        Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(sampleSchedules)
    }

    @Test
    fun `when times set and overlap with existing, conflictingSchedules is populated`() = runTest {
        val viewModel = AddScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase)
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("09:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("11:00"))

        assertEquals(1, viewModel.uiState.value.conflictingSchedules.size)
        assertEquals("1", viewModel.uiState.value.conflictingSchedules.first().id)
    }

    @Test
    fun `when times set and no overlap, conflictingSchedules is empty`() = runTest {
        val viewModel = AddScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase)
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("12:00"))

        assertTrue(viewModel.uiState.value.conflictingSchedules.isEmpty())
    }

    @Test
    fun `when saving with valid data, calls repository addSchedule and sets isSaved`() = runTest {
        coEvery { scheduleRepository.addSchedule(any(), any()) } returns Result.success("sch-1")

        val viewModel = AddScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase)
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("12:00"))
        viewModel.onEvent(AddScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify { scheduleRepository.addSchedule("user_123", any()) }
        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `when endTime is less than or equal to startTime, autocorrects endTime`() = runTest {
        val viewModel = AddScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase)
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("09:00"))

        assertEquals("10:00", viewModel.uiState.value.startTime)
        assertEquals("12:00", viewModel.uiState.value.endTime)
    }

    @Test
    fun `when updateTimeRange is called with invalid range, autocorrects endTime`() = runTest {
        val viewModel = AddScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase)
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateTimeRange("14:00", "14:00"))

        assertEquals("14:00", viewModel.uiState.value.startTime)
        assertEquals("16:00", viewModel.uiState.value.endTime)
    }

    @Test
    fun `when dayOfWeek is updated, dayOfWeek in state reflects change`() = runTest {
        val viewModel = AddScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase)
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(4)) // Viernes
        assertEquals(4, viewModel.uiState.value.dayOfWeek)
    }

    @Test
    fun `when ResetState is dispatched, resets form fields but preserves loaded subjects and schedules`() = runTest {
        val viewModel = AddScheduleViewModel(scheduleRepository, authRepository, noteRepository, validateScheduleOverlapUseCase, scheduleClassAlertUseCase)
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(3))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("08:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateClassroom("Aula 101"))
        viewModel.onEvent(AddScheduleEvent.UpdateTeacher("Prof. Gómez"))

        viewModel.onEvent(AddScheduleEvent.ResetState)

        val state = viewModel.uiState.value
        assertEquals("", state.subjectName)
        assertEquals(1, state.dayOfWeek)
        assertEquals("", state.startTime)
        assertEquals("", state.endTime)
        assertEquals("", state.classroom)
        assertEquals("", state.teacher)
        assertFalse(state.isSaved)
        assertEquals(1, state.subjects.size)
        assertEquals(1, state.existingSchedules.size)
    }
}
