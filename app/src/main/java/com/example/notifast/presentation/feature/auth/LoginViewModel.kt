package com.example.notifast.presentation.feature.auth

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _uiState.update { it.copy(email = event.value) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value) }
            is LoginEvent.LoginClicked -> login()
            is LoginEvent.SignUpClicked -> signUp()
            is LoginEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(
                _uiState.value.email,
                _uiState.value.password
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signUp(
                _uiState.value.email,
                _uiState.value.password,
                "", "", null
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
