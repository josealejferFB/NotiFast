package com.notifast.app.domain.usecase

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.worker.ClassScheduleWorker
import com.notifast.app.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleClassAlertUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val userPrefsRepository: UserPrefsRepository
) {
    suspend operator fun invoke(schedule: Schedule, customMinutesInAdvance: Int? = null) {
        val enabled = userPrefsRepository.classAlertsEnabledFlow.first()
        if (!enabled) return

        val minutesInAdvance = customMinutesInAdvance ?: userPrefsRepository.classAlertMinutesFlow.first()
        val parts = schedule.startTime.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val targetCal = Calendar.getInstance().apply {
            val currentDay = (get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
            var daysUntilTarget = (schedule.dayOfWeek - currentDay + 7) % 7

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -minutesInAdvance)

            if (daysUntilTarget == 0 && timeInMillis <= System.currentTimeMillis()) {
                daysUntilTarget = 7
            }
            if (daysUntilTarget > 0) {
                add(Calendar.DAY_OF_YEAR, daysUntilTarget)
            }
        }

        val delayMillis = targetCal.timeInMillis - System.currentTimeMillis()
        if (delayMillis < 0) return

        val inputData = Data.Builder()
            .putString(ClassScheduleWorker.KEY_SCHEDULE_ID, schedule.id)
            .putString(ClassScheduleWorker.KEY_SUBJECT_NAME, schedule.subjectName)
            .putString(ClassScheduleWorker.KEY_START_TIME, schedule.startTime)
            .putString(ClassScheduleWorker.KEY_CLASSROOM, schedule.classroom)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ClassScheduleWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("class_${schedule.id}")
            .build()

        workManager.enqueueUniqueWork(
            "class_${schedule.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
