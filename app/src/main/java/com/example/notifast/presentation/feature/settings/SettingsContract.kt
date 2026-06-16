package com.example.notifast.presentation.feature.settings

import com.example.notifast.data.model.User

data class SettingsUiState(
    val isLoading: Boolean = false,
    val userProfile: User? = null
)

sealed interface SettingsEvent {
    data object LoadProfile : SettingsEvent
    data object Logout : SettingsEvent
}
