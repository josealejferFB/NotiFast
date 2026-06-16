package com.example.notifast.presentation.feature.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(userId).onSuccess { user ->
                _uiState.update { it.copy(
                    userProfile = user,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    isLoading = false
                )}
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.LoadProfile -> loadProfile()
            is EditProfileEvent.UpdateFirstName -> _uiState.update { it.copy(firstName = event.value) }
            is EditProfileEvent.UpdateLastName -> _uiState.update { it.copy(lastName = event.value) }
            is EditProfileEvent.SaveProfile -> saveProfile(event.imageUri)
        }
    }

    private fun saveProfile(imageUri: android.net.Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            authRepository.updateProfile(
                uid = userId,
                firstName = _uiState.value.firstName,
                lastName = _uiState.value.lastName,
                profileImageUri = imageUri
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _saved.value = true
            }.onFailure {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()
}
