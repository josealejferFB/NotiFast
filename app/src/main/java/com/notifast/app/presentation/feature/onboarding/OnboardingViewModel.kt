package com.notifast.app.presentation.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.FirstNameChanged -> {
                _uiState.update { it.copy(firstName = event.value) }
            }
            is OnboardingEvent.LastNameChanged -> {
                _uiState.update { it.copy(lastName = event.value) }
            }
            is OnboardingEvent.ImageSelected -> {
                _uiState.update { it.copy(profileImageUri = event.uri) }
            }
            is OnboardingEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
            is OnboardingEvent.SubmitProfile -> {
                submitProfile()
            }
        }
    }

    private fun submitProfile() {
        val currentState = _uiState.value
        val trimmedFirstName = currentState.firstName.trim()
        val trimmedLastName = currentState.lastName.trim()

        if (trimmedFirstName.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Por favor escribe tu nombre para continuar",
                    isLoading = false
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.createLocalProfile(
                firstName = trimmedFirstName,
                lastName = trimmedLastName,
                profileImageUri = currentState.profileImageUri
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCompleted = true,
                            error = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCompleted = false,
                            error = throwable.message ?: "Error al crear perfil"
                        )
                    }
                }
            )
        }
    }
}
