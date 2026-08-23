package com.notifast.app.presentation.feature.schedules

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.util.MainDispatcherRule
import com.notifast.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val sampleSchedules = listOf(
        Schedule(
            id = "1",
            subjectName = "Matemáticas",
            dayOfWeek = 1, // Lunes
            startTime = "08:00",
            endTime = "10:00",
            color = 0xFFBF092FL
        ),
        Schedule(
            id = "2",
            subjectName = "Física",
            dayOfWeek = 1, // Lunes
            startTime = "12:00",
            endTime = "14:00",
            color = 0xFF3B9797L
        ),
        Schedule(
            id = "3",
            subjectName = "Química",
            dayOfWeek = 2, // Martes
            startTime = "10:00",
            endTime = "11:30",
            color = 0xFF4A90E2L
        )
    )

    private val sampleEvaluations = listOf(
        Evaluation(
            id = "e1",
            userId = "user_123",
            subjectName = "Matemáticas",
            title = "Examen Parcial 1",
            date = Date()
        ),
        Evaluation(
            id = "e2",
            userId = "user_123",
            subjectName = "Física",
            title = "Laboratorio 1",
            date = Date()
        )
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { authRepository.getUserProfile(any()) } returns Result.success(TestDataFactory.createUser())
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(sampleSchedules)
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(sampleEvaluations)
    }

    @Test
    fun `when initialized, loads schedules and evaluations concurrently`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.schedules.size)
        assertEquals(2, state.evaluations.size)
        assertEquals(2, state.groupedSchedules[1]?.size)
        assertEquals(1, state.groupedSchedules[2]?.size)

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = format.format(Date())
        assertNotNull(state.evaluationsByDate[todayKey])
        assertEquals(2, state.evaluationsByDate[todayKey]?.size)
        assertEquals(2, state.dailyEvaluations.size)

        assertEquals(330, state.weeklySummary.totalWeeklyMinutes)
        assertEquals(3, state.weeklySummary.totalClasses)
        assertEquals("Lunes", state.weeklySummary.busiestDayName)
    }

    @Test
    fun `when date is selected, updates selectedDate and dailyEvaluations`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        val futureCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 5) }
        viewModel.onEvent(SchedulesEvent.SelectDate(futureCal))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(futureCal.get(Calendar.DAY_OF_YEAR), state.selectedDate.get(Calendar.DAY_OF_YEAR))
        assertEquals(0, state.dailyEvaluations.size)
    }

    @Test
    fun `when month changes, updates currentMonth correctly`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        val initialMonth = viewModel.uiState.value.currentMonth.get(Calendar.MONTH)
        viewModel.onEvent(SchedulesEvent.ChangeMonth(1))
        assertEquals((initialMonth + 1) % 12, viewModel.uiState.value.currentMonth.get(Calendar.MONTH))

        viewModel.onEvent(SchedulesEvent.ChangeMonth(-1))
        assertEquals(initialMonth, viewModel.uiState.value.currentMonth.get(Calendar.MONTH))
    }

    @Test
    fun `when GoToToday event is triggered, restores current date and month`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.ChangeMonth(2))
        viewModel.onEvent(SchedulesEvent.GoToToday)
        advanceUntilIdle()

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        assertEquals(currentMonth, viewModel.uiState.value.currentMonth.get(Calendar.MONTH))
    }

    @Test
    fun `when current time is during a class on today, detects Active LiveClassStatus`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository) { 1 to "09:00" }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.liveClassStatus is LiveClassStatus.Active)
        val active = state.liveClassStatus as LiveClassStatus.Active
        assertEquals("Matemáticas", active.schedule.subjectName)
        assertEquals(60, active.remainingMinutes)
        assertEquals(0.5f, active.progressFraction, 0.05f)
    }

    @Test
    fun `when current time is before a class on today, detects Upcoming LiveClassStatus`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository) { 1 to "07:35" }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.liveClassStatus is LiveClassStatus.Upcoming)
        val upcoming = state.liveClassStatus as LiveClassStatus.Upcoming
        assertEquals("Matemáticas", upcoming.schedule.subjectName)
        assertEquals(25, upcoming.startsInMinutes)
    }

    @Test
    fun `when all classes of today are finished, detects DayCompleted LiveClassStatus`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository) { 1 to "15:00" }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.liveClassStatus is LiveClassStatus.DayCompleted)
        val completed = state.liveClassStatus as LiveClassStatus.DayCompleted
        assertEquals(2, completed.totalClassesToday)
    }

    @Test
    fun `when today has no classes, detects NoClassesToday LiveClassStatus`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository) { 0 to "10:00" }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.liveClassStatus is LiveClassStatus.NoClassesToday)
    }

    @Test
    fun `when day has gap of 45min or more, interleaves GapItem in timeline`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository) { 1 to "07:00" }
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.SelectDay(1))
        val items = viewModel.uiState.value.timelineItems

        assertEquals(3, items.size)
        assertTrue(items[0] is ScheduleTimelineItem.ClassItem)
        assertEquals("Matemáticas", (items[0] as ScheduleTimelineItem.ClassItem).schedule.subjectName)

        assertTrue(items[1] is ScheduleTimelineItem.GapItem)
        val gap = items[1] as ScheduleTimelineItem.GapItem
        assertEquals("10:00", gap.startTime)
        assertEquals("12:00", gap.endTime)
        assertEquals(120, gap.durationMinutes)

        assertTrue(items[2] is ScheduleTimelineItem.ClassItem)
        assertEquals("Física", (items[2] as ScheduleTimelineItem.ClassItem).schedule.subjectName)
    }

    @Test
    fun `when day has overlapping schedules, detects conflicts`() = runTest {
        val conflictingSchedules = listOf(
            Schedule(id = "1", subjectName = "Matemáticas", dayOfWeek = 3, startTime = "08:00", endTime = "10:00"),
            Schedule(id = "2", subjectName = "Álgebra", dayOfWeek = 3, startTime = "09:30", endTime = "11:30")
        )
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(conflictingSchedules)

        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.SelectDay(3))
        val conflicts = viewModel.uiState.value.conflicts

        assertEquals(2, conflicts.size)
        assertTrue(conflicts.any { it.id == "1" })
        assertTrue(conflicts.any { it.id == "2" })
    }

    @Test
    fun `when SetViewMode event received, updates viewMode for all 3 modes`() = runTest {
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.SetViewMode(ScheduleViewMode.WEEKLY_BENTO))
        assertEquals(ScheduleViewMode.WEEKLY_BENTO, viewModel.uiState.value.viewMode)

        viewModel.onEvent(SchedulesEvent.SetViewMode(ScheduleViewMode.MONTHLY_CALENDAR))
        assertEquals(ScheduleViewMode.MONTHLY_CALENDAR, viewModel.uiState.value.viewMode)

        viewModel.onEvent(SchedulesEvent.SetViewMode(ScheduleViewMode.DAILY_TIMELINE))
        assertEquals(ScheduleViewMode.DAILY_TIMELINE, viewModel.uiState.value.viewMode)
    }

    @Test
    fun `when DeleteSchedule event received, calls repository deleteSchedule`() = runTest {
        coEvery { scheduleRepository.deleteSchedule("user_123", sampleSchedules[0]) } returns Result.success(Unit)
        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.DeleteSchedule(sampleSchedules[0]))
        advanceUntilIdle()

        coVerify { scheduleRepository.deleteSchedule("user_123", sampleSchedules[0]) }
    }

    @Test
    fun `when DeleteEvaluation event received, calls repository deleteEvaluation and handles errors`() = runTest {
        val eval = sampleEvaluations[0]
        coEvery { evaluationRepository.deleteEvaluation("user_123", eval) } returns Result.failure(Exception("Error eliminando evaluación"))

        val viewModel = SchedulesViewModel(scheduleRepository, evaluationRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(SchedulesEvent.DeleteEvaluation(eval))
        advanceUntilIdle()

        coVerify { evaluationRepository.deleteEvaluation("user_123", eval) }
        assertEquals("Error eliminando evaluación", viewModel.uiState.value.error)

        viewModel.onEvent(SchedulesEvent.ClearError)
        assertNull(viewModel.uiState.value.error)
    }
}
