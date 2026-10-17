package com.notifast.app.domain.notification

interface ExactAlarmScheduler {
    fun scheduleClassAlarm(
        scheduleId: String,
        subjectName: String,
        classroom: String,
        startTime: String,
        triggerAtMillis: Long
    )

    fun scheduleEvaluationAlarm(
        evaluationId: String,
        subjectName: String,
        title: String,
        dateText: String,
        weight: Double,
        triggerAtMillis: Long
    )

    fun cancelClassAlarm(scheduleId: String)

    fun cancelEvaluationAlarm(evaluationId: String)

    fun canScheduleExactAlarms(): Boolean
}
