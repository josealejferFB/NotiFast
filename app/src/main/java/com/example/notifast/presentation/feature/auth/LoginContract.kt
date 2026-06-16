package com.example.notifast.presentation.feature.auth

import android.net.Uri

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

sealed interface LoginEvent {
    data class LoginWithEmail(val email: String, val password: String) : LoginEvent
    data class SignUpWithDetails(
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val profileImageUri: Uri?
    ) : LoginEvent
    data class GoogleSignIn(val idToken: String) : LoginEvent
    data class GoogleError(val message: String) : LoginEvent
}
