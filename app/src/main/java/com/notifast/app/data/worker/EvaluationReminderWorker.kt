package com.notifast.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifast.app.data.notification.NotificationSender
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class EvaluationReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationSender: NotificationSender,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val evalAlertsEnabled = userPrefsRepository.evalAlertsEnabledFlow.first()
        if (!evalAlertsEnabled) return Result.success()

        val evaluationId = inputData.getString(KEY_EVALUATION_ID) ?: return Result.failure()
        val subjectName = inputData.getString(KEY_SUBJECT_NAME) ?: ""
        val title = inputData.getString(KEY_TITLE) ?: ""
        val dateText = inputData.getString(KEY_DATE) ?: ""
        val weight = inputData.getDouble(KEY_WEIGHT, 0.0)

        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            val eval = evaluationRepository.getEvaluation(userId, evaluationId).first()
            if (eval == null || eval.grade != null || eval.isPresented) {
                return Result.success()
            }
        }

        notificationSender.sendEvaluationNotification(
            notificationId = evaluationId.hashCode(),
            subjectName = subjectName,
            evalTitle = title,
            dateText = dateText,
            weight = weight
        )

        return Result.success()
    }

    companion object {
        const val KEY_EVALUATION_ID = "eval_id"
        const val KEY_SUBJECT_NAME = "subject_name"
        const val KEY_TITLE = "title"
        const val KEY_DATE = "date"
        const val KEY_WEIGHT = "weight"
    }
}
