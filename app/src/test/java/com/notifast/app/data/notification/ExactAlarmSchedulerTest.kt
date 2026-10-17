package com.notifast.app.data.notification

import android.app.AlarmManager
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExactAlarmSchedulerTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var shadowAlarmManager: ShadowAlarmManager
    private lateinit var scheduler: ExactAlarmSchedulerImpl

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        shadowAlarmManager = shadowOf(alarmManager)
        scheduler = ExactAlarmSchedulerImpl(context)
    }

    @Test
    fun `scheduleClassAlarm sets exact alarm with correct trigger time and extras`() {
        val triggerTime = System.currentTimeMillis() + 900_000L

        scheduler.scheduleClassAlarm(
            scheduleId = "sch_101",
            subjectName = "Cálculo IV",
            classroom = "Aula 302",
            startTime = "09:00",
            triggerAtMillis = triggerTime
        )

        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull("An alarm should be scheduled", scheduledAlarm)
        assertEquals(triggerTime, scheduledAlarm.triggerAtTime)

        val intent = shadowOf(scheduledAlarm.operation).savedIntent
        assertEquals(AlarmTriggerReceiver.ACTION_CLASS_ALERT, intent.action)
        assertEquals("sch_101", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_SCHEDULE_ID))
        assertEquals("Cálculo IV", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_SUBJECT_NAME))
        assertEquals("Aula 302", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_CLASSROOM))
        assertEquals("09:00", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_START_TIME))
    }

    @Test
    fun `scheduleEvaluationAlarm sets exact alarm with correct trigger time and extras`() {
        val triggerTime = System.currentTimeMillis() + 86_400_000L

        scheduler.scheduleEvaluationAlarm(
            evaluationId = "eval_202",
            subjectName = "Física II",
            title = "Examen Parcial",
            dateText = "20/10/2026",
            weight = 25.0,
            triggerAtMillis = triggerTime
        )

        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull("An alarm should be scheduled", scheduledAlarm)
        assertEquals(triggerTime, scheduledAlarm.triggerAtTime)

        val intent = shadowOf(scheduledAlarm.operation).savedIntent
        assertEquals(AlarmTriggerReceiver.ACTION_EVALUATION_ALERT, intent.action)
        assertEquals("eval_202", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_EVALUATION_ID))
        assertEquals("Física II", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_SUBJECT_NAME))
        assertEquals("Examen Parcial", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_TITLE))
        assertEquals("20/10/2026", intent.getStringExtra(AlarmTriggerReceiver.EXTRA_DATE))
        assertEquals(25.0, intent.getDoubleExtra(AlarmTriggerReceiver.EXTRA_WEIGHT, 0.0), 0.001)
    }

    @Test
    fun `cancelClassAlarm removes scheduled alarm`() {
        val triggerTime = System.currentTimeMillis() + 600_000L
        scheduler.scheduleClassAlarm(
            scheduleId = "sch_cancel",
            subjectName = "Química",
            classroom = "Lab 1",
            startTime = "11:00",
            triggerAtMillis = triggerTime
        )

        scheduler.cancelClassAlarm("sch_cancel")

        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNull("Alarm should be cancelled", scheduledAlarm)
    }

    @Test
    fun `cancelEvaluationAlarm removes scheduled alarm`() {
        val triggerTime = System.currentTimeMillis() + 600_000L
        scheduler.scheduleEvaluationAlarm(
            evaluationId = "eval_cancel",
            subjectName = "Álgebra",
            title = "Quiz 1",
            dateText = "22/10/2026",
            weight = 10.0,
            triggerAtMillis = triggerTime
        )

        scheduler.cancelEvaluationAlarm("eval_cancel")

        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNull("Alarm should be cancelled", scheduledAlarm)
    }

    @Test
    fun `canScheduleExactAlarms returns true when permission granted in shadow`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        assertTrue(scheduler.canScheduleExactAlarms())
    }

    @Test
    fun `canScheduleExactAlarms returns false when permission revoked`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        org.junit.Assert.assertFalse(scheduler.canScheduleExactAlarms())
    }
}
