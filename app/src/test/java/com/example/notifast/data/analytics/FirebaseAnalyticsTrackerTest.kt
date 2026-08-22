package com.example.notifast.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FirebaseAnalyticsTrackerTest {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var tracker: FirebaseAnalyticsTracker

    @Before
    fun setUp() {
        firebaseAnalytics = mockk(relaxed = true)
        tracker = FirebaseAnalyticsTracker(firebaseAnalytics)
    }

    @Test
    fun `logLogin logs correct event to FirebaseAnalytics`() {
        tracker.logLogin("email")
        verify { firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, any<Bundle>()) }
    }

    @Test
    fun `logSignUp logs correct event to FirebaseAnalytics`() {
        tracker.logSignUp("google")
        verify { firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, any<Bundle>()) }
    }

    @Test
    fun `logAddNote logs add_note event to FirebaseAnalytics`() {
        tracker.logAddNote("Matemáticas")
        verify { firebaseAnalytics.logEvent("add_note", any<Bundle>()) }
    }

    @Test
    fun `logAddEvaluation logs add_evaluation event to FirebaseAnalytics`() {
        tracker.logAddEvaluation("Física", "EXAM")
        verify { firebaseAnalytics.logEvent("add_evaluation", any<Bundle>()) }
    }

    @Test
    fun `logAddSchedule logs add_schedule event to FirebaseAnalytics`() {
        tracker.logAddSchedule("Química")
        verify { firebaseAnalytics.logEvent("add_schedule", any<Bundle>()) }
    }
}
