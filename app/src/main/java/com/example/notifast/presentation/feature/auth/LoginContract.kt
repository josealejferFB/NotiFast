package com.example.notifast.presentation.feature.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object LoginClicked : LoginEvent
    data object SignUpClicked : LoginEvent
    data object ClearError : LoginEvent
}
