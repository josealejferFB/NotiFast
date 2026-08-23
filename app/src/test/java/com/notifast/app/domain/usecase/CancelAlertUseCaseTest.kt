package com.notifast.app.domain.usecase

import androidx.work.WorkManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class CancelAlertUseCaseTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private lateinit var useCase: CancelAlertUseCase

    @Before
    fun setUp() {
        useCase = CancelAlertUseCase(workManager)
    }

    @Test
    fun `cancelEvaluationAlert calls cancelUniqueWork with eval prefix`() {
        useCase.cancelEvaluationAlert("123")
        verify { workManager.cancelUniqueWork("eval_123") }
    }

    @Test
    fun `cancelClassAlert calls cancelUniqueWork with class prefix`() {
        useCase.cancelClassAlert("456")
        verify { workManager.cancelUniqueWork("class_456") }
    }

    @Test
    fun `cancelDailySummary calls cancelUniqueWork with daily_summary`() {
        useCase.cancelDailySummary()
        verify { workManager.cancelUniqueWork("daily_summary") }
    }
}
