package com.notifast.app.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.repository.UserPrefsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class ScheduleEvaluationAlertUseCaseTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)

    private lateinit var useCase: ScheduleEvaluationAlertUseCase

    @Before
    fun setUp() {
        every { userPrefsRepository.evalAlertsEnabledFlow } returns flowOf(true)
        every { userPrefsRepository.evalAlertDaysFlow } returns flowOf(1)
        useCase = ScheduleEvaluationAlertUseCase(workManager, userPrefsRepository)
    }

    @Test
    fun `when evaluation date is in future, enqueues unique work with eval tag`() = runTest {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
        val evaluation = Evaluation(
            id = "eval_123",
            subjectName = "Cálculo",
            title = "Examen Parcial",
            date = cal.time,
            weight = 20.0
        )

        useCase(evaluation)

        verify {
            workManager.enqueueUniqueWork(
                "eval_eval_123",
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `when eval alerts disabled in preferences, does not enqueue work`() = runTest {
        every { userPrefsRepository.evalAlertsEnabledFlow } returns flowOf(false)
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
        val evaluation = Evaluation(
            id = "eval_456",
            subjectName = "Física",
            title = "Laboratorio 1",
            date = cal.time,
            weight = 10.0
        )

        useCase(evaluation)

        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }
}
