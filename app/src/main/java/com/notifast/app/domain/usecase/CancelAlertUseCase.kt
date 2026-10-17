package com.notifast.app.domain.usecase

import androidx.work.WorkManager
import com.notifast.app.domain.notification.ExactAlarmScheduler
import javax.inject.Inject

class CancelAlertUseCase @Inject constructor(
    private val exactAlarmScheduler: ExactAlarmScheduler,
    private val workManager: WorkManager
) {
    fun cancelEvaluationAlert(evaluationId: String) {
        exactAlarmScheduler.cancelEvaluationAlarm(evaluationId)
        workManager.cancelUniqueWork("eval_$evaluationId")
    }

    fun cancelClassAlert(scheduleId: String) {
        exactAlarmScheduler.cancelClassAlarm(scheduleId)
        workManager.cancelUniqueWork("class_$scheduleId")
    }

    fun cancelDailySummary() {
        workManager.cancelUniqueWork("daily_summary")
    }
}
