package com.notifast.app.domain.usecase

import androidx.work.WorkManager
import com.notifast.app.domain.notification.ExactAlarmScheduler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class CancelAlertUseCaseTest {

    private val exactAlarmScheduler: ExactAlarmScheduler = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private lateinit var useCase: CancelAlertUseCase

    @Before
    fun setUp() {
        useCase = CancelAlertUseCase(exactAlarmScheduler, workManager)
    }

    @Test
    fun `cancelEvaluationAlert cancels exact alarm and legacy work`() {
        useCase.cancelEvaluationAlert("123")
        verify {
            exactAlarmScheduler.cancelEvaluationAlarm("123")
            workManager.cancelUniqueWork("eval_123")
        }
    }

    @Test
    fun `cancelClassAlert cancels exact alarm and legacy work`() {
        useCase.cancelClassAlert("456")
        verify {
            exactAlarmScheduler.cancelClassAlarm("456")
            workManager.cancelUniqueWork("class_456")
        }
    }

    @Test
    fun `cancelDailySummary calls cancelUniqueWork with daily_summary`() {
        useCase.cancelDailySummary()
        verify { workManager.cancelUniqueWork("daily_summary") }
    }
}
