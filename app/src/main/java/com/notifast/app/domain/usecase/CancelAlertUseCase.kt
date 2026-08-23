package com.notifast.app.domain.usecase

import androidx.work.WorkManager
import javax.inject.Inject

class CancelAlertUseCase @Inject constructor(
    private val workManager: WorkManager
) {
    fun cancelEvaluationAlert(evaluationId: String) {
        workManager.cancelUniqueWork("eval_$evaluationId")
    }

    fun cancelClassAlert(scheduleId: String) {
        workManager.cancelUniqueWork("class_$scheduleId")
    }

    fun cancelDailySummary() {
        workManager.cancelUniqueWork("daily_summary")
    }
}
