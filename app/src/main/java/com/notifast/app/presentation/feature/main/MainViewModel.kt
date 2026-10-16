package com.notifast.app.presentation.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    userPrefsRepository: UserPrefsRepository
) : ViewModel() {
    val authState: StateFlow<Boolean?> = authRepository.authState

    val isDarkMode: StateFlow<Boolean?> = userPrefsRepository.isDarkModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val hapticsEnabled: StateFlow<Boolean> = userPrefsRepository.hapticsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val themePresetId: StateFlow<String> = userPrefsRepository.themePresetIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "classic"
    )

    val backgroundPresetId: StateFlow<String> = userPrefsRepository.backgroundPresetIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "classic"
    )

    val customPrimaryHex: StateFlow<String?> = userPrefsRepository.customPrimaryHexFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}
