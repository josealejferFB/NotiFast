package com.notifast.app.presentation.feature.settings

import com.notifast.app.data.model.User

data class SettingsUiState(
    val isLoading: Boolean = false,
    val userProfile: User? = null
)

sealed interface SettingsEvent {
    data object LoadProfile : SettingsEvent
    data object Logout : SettingsEvent
}
