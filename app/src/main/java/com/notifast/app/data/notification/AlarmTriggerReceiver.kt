package com.notifast.app.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.ScheduleClassAlertUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AlarmTriggerReceiver"

@AndroidEntryPoint
class AlarmTriggerReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationSender: NotificationSender

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var evaluationRepository: EvaluationRepository

    @Inject
    lateinit var userPrefsRepository: UserPrefsRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var scheduleClassAlertUseCase: ScheduleClassAlertUseCase

    @Inject
    @com.notifast.app.di.IoDispatcher
    lateinit var ioDispatcher: kotlinx.coroutines.CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(ioDispatcher).launch {
            try {
                when (action) {
                    ACTION_CLASS_ALERT -> handleClassAlert(intent)
                    ACTION_EVALUATION_ALERT -> handleEvaluationAlert(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle alarm trigger for action: $action", e)
            } finally {
                try {
                    pendingResult.finish()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to finish broadcast pending result", e)
                }
            }
        }
    }

    private suspend fun handleClassAlert(intent: Intent) {
        val enabled = userPrefsRepository.classAlertsEnabledFlow.first()
        if (!enabled) return

        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID) ?: return
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: ""
        val classroom = intent.getStringExtra(EXTRA_CLASSROOM) ?: ""
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""

        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            val schedules = scheduleRepository.getSchedules(userId).first()
            val schedule = schedules.find { it.id == scheduleId }
            if (schedule != null) {
                // Re-arm for next week before notifying
                scheduleClassAlertUseCase(schedule)
            }
        }

        notificationSender.sendClassNotification(
            notificationId = scheduleId.hashCode(),
            subjectName = subjectName,
            classroom = classroom,
            startTime = startTime
        )
    }

    private suspend fun handleEvaluationAlert(intent: Intent) {
        val enabled = userPrefsRepository.evalAlertsEnabledFlow.first()
        if (!enabled) return

        val evaluationId = intent.getStringExtra(EXTRA_EVALUATION_ID) ?: return
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val dateText = intent.getStringExtra(EXTRA_DATE) ?: ""
        val weight = intent.getDoubleExtra(EXTRA_WEIGHT, 0.0)

        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            val eval = evaluationRepository.getEvaluation(userId, evaluationId).first()
            if (eval == null || eval.grade != null || eval.isPresented) {
                // Evaluation was already presented, graded, or deleted: suppress notification
                return
            }
        }

        notificationSender.sendEvaluationNotification(
            notificationId = evaluationId.hashCode(),
            subjectName = subjectName,
            evalTitle = title,
            dateText = dateText,
            weight = weight,
            evaluationId = evaluationId
        )
    }

    companion object {
        const val ACTION_CLASS_ALERT = "com.notifast.app.ACTION_CLASS_ALERT"
        const val ACTION_EVALUATION_ALERT = "com.notifast.app.ACTION_EVALUATION_ALERT"

        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_CLASSROOM = "extra_classroom"
        const val EXTRA_START_TIME = "extra_start_time"

        const val EXTRA_EVALUATION_ID = "extra_evaluation_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_WEIGHT = "extra_weight"
    }
}
