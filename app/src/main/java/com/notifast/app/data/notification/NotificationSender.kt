package com.notifast.app.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.notifast.app.MainActivity
import com.notifast.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSender @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun areNotificationsEnabled(): Boolean {
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun sendEvaluationNotification(
        notificationId: Int,
        subjectName: String,
        evalTitle: String,
        dateText: String,
        weight: Double,
        evaluationId: String? = null
    ) {
        if (!areNotificationsEnabled()) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_SUBJECT_DETAIL)
            putExtra(MainActivity.EXTRA_SUBJECT_NAME, subjectName)
            if (evaluationId != null) {
                putExtra(MainActivity.EXTRA_EVALUATION_ID, evaluationId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_SUBJECT_DETAIL)
            putExtra(MainActivity.EXTRA_SUBJECT_NAME, subjectName)
            if (evaluationId != null) {
                putExtra(MainActivity.EXTRA_EVALUATION_ID, evaluationId)
            }
        }
        val actionPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + ACTION_REQUEST_CODE_OFFSET,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickAction = NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.notification_action_view_subject),
            actionPendingIntent
        ).build()

        val body = context.getString(
            R.string.notification_eval_body,
            subjectName,
            dateText,
            String.format(Locale.US, "%.0f", weight)
        )

        val notification = NotificationCompat.Builder(context, NotificationChannelsManager.CHANNEL_EVALUATIONS)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(context.getString(R.string.notification_eval_title, evalTitle))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(pendingIntent)
            .addAction(quickAction)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun sendClassNotification(
        notificationId: Int,
        subjectName: String,
        classroom: String,
        startTime: String
    ) {
        if (!areNotificationsEnabled()) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_SCHEDULES)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_SCHEDULES)
        }
        val actionPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + ACTION_REQUEST_CODE_OFFSET,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickAction = NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.notification_action_view_schedule),
            actionPendingIntent
        ).build()

        val body = context.getString(R.string.notification_class_body, startTime, classroom.ifBlank { "N/A" })

        val notification = NotificationCompat.Builder(context, NotificationChannelsManager.CHANNEL_CLASSES)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(context.getString(R.string.notification_class_title, subjectName))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 200, 200, 200))
            .setContentIntent(pendingIntent)
            .addAction(quickAction)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun sendTestNotification(): Boolean {
        if (!areNotificationsEnabled()) return false

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_SETTINGS)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            TEST_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notification_test_title)
        val body = context.getString(R.string.notification_test_body)

        val notification = NotificationCompat.Builder(context, NotificationChannelsManager.CHANNEL_CLASSES)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID, notification)
        return true
    }

    fun sendDailySummaryNotification(
        notificationId: Int,
        summaryBody: String
    ) {
        if (!areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_SCHEDULES)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannelsManager.CHANNEL_DAILY_SUMMARY)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(context.getString(R.string.notification_daily_summary_title))
            .setContentText(summaryBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryBody))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val TEST_NOTIFICATION_ID = 9999
        private const val ACTION_REQUEST_CODE_OFFSET = 100_000
    }
}
