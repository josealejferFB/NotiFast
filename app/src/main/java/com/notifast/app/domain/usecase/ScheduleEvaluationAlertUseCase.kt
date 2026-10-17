package com.notifast.app.domain.usecase

import androidx.work.WorkManager
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.notification.ExactAlarmScheduler
import com.notifast.app.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleEvaluationAlertUseCase @Inject constructor(
    private val exactAlarmScheduler: ExactAlarmScheduler,
    private val workManager: WorkManager,
    private val userPrefsRepository: UserPrefsRepository
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    suspend operator fun invoke(evaluation: Evaluation, customDaysInAdvance: Int? = null) {
        val enabled = userPrefsRepository.evalAlertsEnabledFlow.first()
        if (!enabled) return

        val evalDate = evaluation.date ?: return
        val daysInAdvance = customDaysInAdvance ?: userPrefsRepository.evalAlertDaysFlow.first()

        val targetCal = Calendar.getInstance().apply {
            time = evalDate
            add(Calendar.DAY_OF_YEAR, -daysInAdvance)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()
        var delayMillis = targetCal.timeInMillis - now

        if (delayMillis < 0) {
            val examEndCal = Calendar.getInstance().apply {
                time = evalDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }
            if (examEndCal.timeInMillis > now) {
                delayMillis = TimeUnit.MINUTES.toMillis(5)
            } else {
                return
            }
        }

        val formattedDate = dateFormat.format(evalDate)
        val triggerAtMillis = now + delayMillis

        exactAlarmScheduler.scheduleEvaluationAlarm(
            evaluationId = evaluation.id,
            subjectName = evaluation.subjectName,
            title = evaluation.title,
            dateText = formattedDate,
            weight = evaluation.weight ?: 0.0,
            triggerAtMillis = triggerAtMillis
        )
        workManager.cancelUniqueWork("eval_${evaluation.id}")
    }
}
