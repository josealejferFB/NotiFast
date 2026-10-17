package com.notifast.app.domain.usecase

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class RescheduleAllAlertsUseCaseTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val scheduleEvaluationAlertUseCase: ScheduleEvaluationAlertUseCase = mockk(relaxed = true)
    private val scheduleClassAlertUseCase: ScheduleClassAlertUseCase = mockk(relaxed = true)

    private lateinit var useCase: RescheduleAllAlertsUseCase

    @Before
    fun setUp() {
        useCase = RescheduleAllAlertsUseCase(
            workManager = workManager,
            authRepository = authRepository,
            evaluationRepository = evaluationRepository,
            scheduleRepository = scheduleRepository,
            userPrefsRepository = userPrefsRepository,
            scheduleEvaluationAlertUseCase = scheduleEvaluationAlertUseCase,
            scheduleClassAlertUseCase = scheduleClassAlertUseCase
        )
    }

    @Test
    fun `when user is authenticated, reschedules active evaluations and schedules`() = runTest {
        val userId = "user_42"
        coEvery { authRepository.getCurrentUserId() } returns userId

        val activeEval = Evaluation(id = "e1", title = "Quiz", date = Date(), isPresented = false, grade = null)
        val presentedEval = Evaluation(id = "e2", title = "Midterm", date = Date(), isPresented = true, grade = 18.0)
        coEvery { evaluationRepository.getEvaluations(userId) } returns flowOf(listOf(activeEval, presentedEval))

        val schedule = Schedule(id = "s1", subjectName = "Calculus", dayOfWeek = 2, startTime = "10:00", endTime = "12:00")
        coEvery { scheduleRepository.getSchedules(userId) } returns flowOf(listOf(schedule))
        every { userPrefsRepository.dailySummaryEnabledFlow } returns flowOf(true)

        useCase()

        coVerify(exactly = 1) { scheduleEvaluationAlertUseCase(activeEval) }
        coVerify(exactly = 0) { scheduleEvaluationAlertUseCase(presentedEval) }
        coVerify(exactly = 1) { scheduleClassAlertUseCase(schedule) }
        verify {
            workManager.enqueueUniquePeriodicWork(
                "daily_summary",
                ExistingPeriodicWorkPolicy.UPDATE,
                any<PeriodicWorkRequest>()
            )
        }
    }

    @Test
    fun `when user is not authenticated, does nothing`() = runTest {
        coEvery { authRepository.getCurrentUserId() } returns null

        useCase()

        coVerify(exactly = 0) { scheduleEvaluationAlertUseCase(any()) }
        coVerify(exactly = 0) { scheduleClassAlertUseCase(any()) }
    }

    @Test
    fun `when daily summary disabled, cancels daily summary work`() = runTest {
        val userId = "user_42"
        coEvery { authRepository.getCurrentUserId() } returns userId
        coEvery { evaluationRepository.getEvaluations(userId) } returns flowOf(emptyList())
        coEvery { scheduleRepository.getSchedules(userId) } returns flowOf(emptyList())
        every { userPrefsRepository.dailySummaryEnabledFlow } returns flowOf(false)

        useCase()

        verify { workManager.cancelUniqueWork("daily_summary") }
    }
}
