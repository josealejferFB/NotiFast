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
        val startMinutes = com.notifast.app.domain.util.TimeParser.toMinutes(schedule.startTime) ?: return

        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1
        var daysUntilTarget = (schedule.dayOfWeek - currentDay + 7) % 7

        val classCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startMinutes / 60)
            set(Calendar.MINUTE, startMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If today's class already started/ended, move to next week
        if (daysUntilTarget == 0 && classCal.timeInMillis <= now.timeInMillis) {
            daysUntilTarget = 7
        }
        if (daysUntilTarget > 0) {
            classCal.add(Calendar.DAY_OF_YEAR, daysUntilTarget)
        }

        val targetCal = (classCal.clone() as Calendar).apply {
            add(Calendar.MINUTE, -minutesInAdvance)
        }

        val delayMillis = targetCal.timeInMillis - now.timeInMillis
        // Alert time already passed but class has not: fire shortly instead of skipping a week
        val effectiveDelay = if (delayMillis < 0 && classCal.timeInMillis > now.timeInMillis) {
            TimeUnit.MINUTES.toMillis(1)
        } else {
            delayMillis
        }
        if (effectiveDelay < 0) return

        val inputData = Data.Builder()
            .putString(ClassScheduleWorker.KEY_SCHEDULE_ID, schedule.id)
            .putString(ClassScheduleWorker.KEY_SUBJECT_NAME, schedule.subjectName)
            .putString(ClassScheduleWorker.KEY_START_TIME, schedule.startTime)
            .putString(ClassScheduleWorker.KEY_CLASSROOM, schedule.classroom)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ClassScheduleWorker>()
            .setInitialDelay(effectiveDelay, TimeUnit.MILLISECONDS)
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
