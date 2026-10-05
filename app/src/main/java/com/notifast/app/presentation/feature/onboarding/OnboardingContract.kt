package com.notifast.app.presentation.feature.onboarding

import android.net.Uri

data class OnboardingUiState(
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false
)

sealed interface OnboardingEvent {
    data class FirstNameChanged(val value: String) : OnboardingEvent
    data class LastNameChanged(val value: String) : OnboardingEvent
    data class ImageSelected(val uri: Uri?) : OnboardingEvent
    data object SubmitProfile : OnboardingEvent
    data object ClearError : OnboardingEvent
}
