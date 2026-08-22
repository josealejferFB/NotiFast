package com.notifast.app.presentation.feature.schedules

import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ScheduleRepository
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val sampleSchedules = listOf(
        Schedule(id = "1", subjectName = "Matemáticas", dayOfWeek = 1, startTime = "08:00", endTime = "10:00"),
        Schedule(id = "2", subjectName = "Física", dayOfWeek = 2, startTime = "10:00", endTime = "12:00")
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(sampleSchedules)
    }

    @Test
    fun `when initialized, loads schedules and groups by day`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, authRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.schedules.size)
        assertEquals(1, state.groupedSchedules[1]?.size)
        assertEquals(1, state.groupedSchedules[2]?.size)
    }

    @Test
    fun `when SelectDay event received, updates selectedDay`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.SelectDay(3))
        assertEquals(3, viewModel.uiState.value.selectedDay)
    }

    @Test
    fun `when DeleteSchedule event received, calls repository deleteSchedule`() = runTest {
        coEvery { scheduleRepository.deleteSchedule("user_123", sampleSchedules[0]) } returns Result.success(Unit)
        val viewModel = SchedulesViewModel(scheduleRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.DeleteSchedule(sampleSchedules[0]))
        advanceUntilIdle()

        coVerify { scheduleRepository.deleteSchedule("user_123", sampleSchedules[0]) }
    }
}
