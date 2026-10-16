package com.notifast.app.presentation.feature.onboarding

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri

data class OnboardingUiState(
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val pendingAuthIntent: PendingIntent? = null,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val autoOpenCloudHub: Boolean = false
)

sealed interface OnboardingEvent {
    data class FirstNameChanged(val value: String) : OnboardingEvent
    data class LastNameChanged(val value: String) : OnboardingEvent
    data class ImageSelected(val uri: Uri?) : OnboardingEvent
    data object SubmitProfile : OnboardingEvent
    data class SignInWithGoogle(val context: Context) : OnboardingEvent
    data class GoogleAuthResult(val data: Intent?) : OnboardingEvent
    data object ClearError : OnboardingEvent
}
