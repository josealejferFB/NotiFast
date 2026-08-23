package com.notifast.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifast.app.data.notification.NotificationSender
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.ScheduleClassAlertUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ClassScheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationSender: NotificationSender,
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val scheduleClassAlertUseCase: ScheduleClassAlertUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val classAlertsEnabled = userPrefsRepository.classAlertsEnabledFlow.first()
        if (!classAlertsEnabled) return Result.success()

        val scheduleId = inputData.getString(KEY_SCHEDULE_ID) ?: return Result.failure()
        val subjectName = inputData.getString(KEY_SUBJECT_NAME) ?: ""
        val startTime = inputData.getString(KEY_START_TIME) ?: ""
        val classroom = inputData.getString(KEY_CLASSROOM) ?: ""

        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            val schedules = scheduleRepository.getSchedules(userId).first()
            val schedule = schedules.find { it.id == scheduleId }
            if (schedule == null) return Result.success()

            // Reprogramar para la semana siguiente
            scheduleClassAlertUseCase(schedule)
        }

        notificationSender.sendClassNotification(
            notificationId = scheduleId.hashCode(),
            subjectName = subjectName,
            classroom = classroom,
            startTime = startTime
        )

        return Result.success()
    }

    companion object {
        const val KEY_SCHEDULE_ID = "schedule_id"
        const val KEY_SUBJECT_NAME = "subject_name"
        const val KEY_START_TIME = "start_time"
        const val KEY_CLASSROOM = "classroom"
    }
}
