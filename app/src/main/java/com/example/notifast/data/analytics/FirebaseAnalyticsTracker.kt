package com.example.notifast.data.analytics

import android.os.Bundle
import com.example.notifast.domain.util.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        val bundle = Bundle()
        for ((key, value) in params) {
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    override fun logLogin(method: String) {
        logEvent(FirebaseAnalytics.Event.LOGIN, mapOf(FirebaseAnalytics.Param.METHOD to method))
    }

    override fun logSignUp(method: String) {
        logEvent(FirebaseAnalytics.Event.SIGN_UP, mapOf(FirebaseAnalytics.Param.METHOD to method))
    }

    override fun logAddNote(subject: String) {
        logEvent("add_note", mapOf("subject" to subject))
    }

    override fun logAddEvaluation(subject: String, type: String) {
        logEvent("add_evaluation", mapOf("subject" to subject, "type" to type))
    }

    override fun logAddSchedule(subject: String) {
        logEvent("add_schedule", mapOf("subject" to subject))
    }
}
