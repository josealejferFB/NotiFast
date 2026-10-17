package com.notifast.app.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.notifast.app.domain.notification.ExactAlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExactAlarmSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ExactAlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun scheduleClassAlarm(
        scheduleId: String,
        subjectName: String,
        classroom: String,
        startTime: String,
        triggerAtMillis: Long
    ) {
        if (alarmManager == null) return

        val intent = Intent(context, AlarmTriggerReceiver::class.java).apply {
            action = AlarmTriggerReceiver.ACTION_CLASS_ALERT
            putExtra(AlarmTriggerReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(AlarmTriggerReceiver.EXTRA_SUBJECT_NAME, subjectName)
            putExtra(AlarmTriggerReceiver.EXTRA_CLASSROOM, classroom)
            putExtra(AlarmTriggerReceiver.EXTRA_START_TIME, startTime)
        }

        val requestCode = ("class_" + scheduleId).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactInternal(triggerAtMillis, pendingIntent)
    }

    override fun scheduleEvaluationAlarm(
        evaluationId: String,
        subjectName: String,
        title: String,
        dateText: String,
        weight: Double,
        triggerAtMillis: Long
    ) {
        if (alarmManager == null) return

        val intent = Intent(context, AlarmTriggerReceiver::class.java).apply {
            action = AlarmTriggerReceiver.ACTION_EVALUATION_ALERT
            putExtra(AlarmTriggerReceiver.EXTRA_EVALUATION_ID, evaluationId)
            putExtra(AlarmTriggerReceiver.EXTRA_SUBJECT_NAME, subjectName)
            putExtra(AlarmTriggerReceiver.EXTRA_TITLE, title)
            putExtra(AlarmTriggerReceiver.EXTRA_DATE, dateText)
            putExtra(AlarmTriggerReceiver.EXTRA_WEIGHT, weight)
        }

        val requestCode = ("eval_" + evaluationId).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactInternal(triggerAtMillis, pendingIntent)
    }

    override fun cancelClassAlarm(scheduleId: String) {
        if (alarmManager == null) return
        val intent = Intent(context, AlarmTriggerReceiver::class.java).apply {
            action = AlarmTriggerReceiver.ACTION_CLASS_ALERT
        }
        val requestCode = ("class_" + scheduleId).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override fun cancelEvaluationAlarm(evaluationId: String) {
        if (alarmManager == null) return
        val intent = Intent(context, AlarmTriggerReceiver::class.java).apply {
            action = AlarmTriggerReceiver.ACTION_EVALUATION_ALERT
        }
        val requestCode = ("eval_" + evaluationId).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override fun canScheduleExactAlarms(): Boolean {
        if (alarmManager == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun scheduleExactInternal(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val manager = alarmManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (manager.canScheduleExactAlarms()) {
                        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                    } else {
                        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                    }
                } else {
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while scheduling exact alarm, falling back to setAndAllowWhileIdle", e)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
    }

    companion object {
        private const val TAG = "ExactAlarmScheduler"
    }
}
