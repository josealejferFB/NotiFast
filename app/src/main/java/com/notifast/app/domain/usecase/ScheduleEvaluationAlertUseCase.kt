package com.notifast.app.domain.usecase

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.worker.EvaluationReminderWorker
import com.notifast.app.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleEvaluationAlertUseCase @Inject constructor(
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

        val inputData = Data.Builder()
            .putString(EvaluationReminderWorker.KEY_EVALUATION_ID, evaluation.id)
            .putString(EvaluationReminderWorker.KEY_SUBJECT_NAME, evaluation.subjectName)
            .putString(EvaluationReminderWorker.KEY_TITLE, evaluation.title)
            .putString(EvaluationReminderWorker.KEY_DATE, formattedDate)
            .putDouble(EvaluationReminderWorker.KEY_WEIGHT, evaluation.weight ?: 0.0)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EvaluationReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("eval_${evaluation.id}")
            .build()

        workManager.enqueueUniqueWork(
            "eval_${evaluation.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
