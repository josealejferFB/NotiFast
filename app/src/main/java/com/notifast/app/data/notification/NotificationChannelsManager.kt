package com.notifast.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.notifast.app.R

object NotificationChannelsManager {
    const val CHANNEL_EVALUATIONS = "notifast_evaluations"
    const val CHANNEL_CLASSES = "notifast_classes"
    const val CHANNEL_DAILY_SUMMARY = "notifast_daily_summary"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val evalChannel = NotificationChannel(
                CHANNEL_EVALUATIONS,
                context.getString(R.string.channel_evaluations_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_evaluations_desc)
                enableVibration(true)
            }

            val classChannel = NotificationChannel(
                CHANNEL_CLASSES,
                context.getString(R.string.channel_classes_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_classes_desc)
                enableVibration(true)
            }

            val summaryChannel = NotificationChannel(
                CHANNEL_DAILY_SUMMARY,
                context.getString(R.string.channel_daily_summary_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_daily_summary_desc)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannels(listOf(evalChannel, classChannel, summaryChannel))
        }
    }
}
