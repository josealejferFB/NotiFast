package com.notifast.app.domain.util

interface AnalyticsTracker {
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun logLogin(method: String)
    fun logSignUp(method: String)
    fun logAddNote(subject: String)
    fun logAddEvaluation(subject: String, type: String)
    fun logAddSchedule(subject: String)
}
