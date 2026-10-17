package com.notifast.app.data.notification

import android.app.NotificationManager
import android.content.Context
import com.notifast.app.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationSenderTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager
    private lateinit var sender: NotificationSender

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        val app = context as android.app.Application
        shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = shadowOf(notificationManager)
        NotificationChannelsManager.createChannels(context)
        sender = NotificationSender(context)
    }

    @Test
    fun `areNotificationsEnabled returns true when permission granted`() {
        assertTrue(sender.areNotificationsEnabled())
    }

    @Test
    fun `areNotificationsEnabled returns false when permission denied`() {
        val app = context as android.app.Application
        shadowOf(app).denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        org.junit.Assert.assertFalse(sender.areNotificationsEnabled())
    }

    @Test
    fun `sendTestNotification posts notification to system on CHANNEL_CLASSES`() {
        val result = sender.sendTestNotification()

        assertTrue(result)
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(1, notifications.size)

        val notification = notifications[0]
        assertEquals(NotificationChannelsManager.CHANNEL_CLASSES, notification.channelId)
        assertNotNull(notification.contentIntent)

        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(MainActivity.DESTINATION_SETTINGS, intent.getStringExtra(MainActivity.EXTRA_DESTINATION))
    }

    @Test
    fun `sendClassNotification posts heads-up alert with quick action`() {
        sender.sendClassNotification(
            notificationId = 101,
            subjectName = "Estructuras Discretas",
            classroom = "Lab 2",
            startTime = "08:00"
        )

        val notification = shadowNotificationManager.getNotification(101)
        assertNotNull(notification)
        assertEquals(NotificationChannelsManager.CHANNEL_CLASSES, notification.channelId)
        assertTrue(notification.actions != null && notification.actions.isNotEmpty())

        val action = notification.actions[0]
        assertNotNull(action.actionIntent)
        val actionIntent = shadowOf(action.actionIntent).savedIntent
        assertEquals(MainActivity.DESTINATION_SCHEDULES, actionIntent.getStringExtra(MainActivity.EXTRA_DESTINATION))
    }

    @Test
    fun `sendEvaluationNotification posts alert with deep link to subject detail`() {
        sender.sendEvaluationNotification(
            notificationId = 202,
            subjectName = "Algoritmos II",
            evalTitle = "Proyecto Final",
            dateText = "30/11/2026",
            weight = 40.0,
            evaluationId = "eval_404"
        )

        val notification = shadowNotificationManager.getNotification(202)
        assertNotNull(notification)
        assertEquals(NotificationChannelsManager.CHANNEL_EVALUATIONS, notification.channelId)

        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals(MainActivity.DESTINATION_SUBJECT_DETAIL, intent.getStringExtra(MainActivity.EXTRA_DESTINATION))
        assertEquals("Algoritmos II", intent.getStringExtra(MainActivity.EXTRA_SUBJECT_NAME))
        assertEquals("eval_404", intent.getStringExtra(MainActivity.EXTRA_EVALUATION_ID))
    }
}
