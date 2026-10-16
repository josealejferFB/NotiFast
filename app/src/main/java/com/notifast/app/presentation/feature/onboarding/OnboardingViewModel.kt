package com.notifast.app.presentation.feature.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.AuthStep
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.util.ErrorSanitizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "OnboardingViewModel"

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleAccountRepository: GoogleAccountRepository
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
            is OnboardingEvent.SignInWithGoogle -> {
                signInWithGoogle(event.context)
            }
            is OnboardingEvent.GoogleAuthResult -> {
                handleGoogleAuthResult(event.data)
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
                            autoOpenCloudHub = false,
                            error = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCompleted = false,
                            error = ErrorSanitizer.sanitize(throwable, "Error al crear perfil")
                        )
                    }
                }
            )
        }
    }

    private fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, error = null) }
            googleAccountRepository.signIn(context).fold(
                onSuccess = { step ->
                    when (step) {
                        is AuthStep.NeedsResolution -> {
                            _uiState.update {
                                it.copy(
                                    pendingAuthIntent = step.pendingIntent,
                                    isGoogleLoading = false
                                )
                            }
                        }
                        is AuthStep.Linked -> {
                            handleSuccessfulGoogleLink(step.account)
                        }
                    }
                },
                onFailure = { throwable ->
                    Log.e(TAG, "signInWithGoogle failed: ${throwable.message}", throwable)
                    handleGoogleError(throwable)
                }
            )
        }
    }

    private fun handleGoogleAuthResult(data: Intent?) {
        if (data == null) {
            _uiState.update {
                it.copy(
                    pendingAuthIntent = null,
                    isGoogleLoading = false,
                    error = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(pendingAuthIntent = null, isGoogleLoading = true, error = null) }
            googleAccountRepository.completeAuthorization(data).fold(
                onSuccess = { account ->
                    handleSuccessfulGoogleLink(account)
                },
                onFailure = { throwable ->
                    Log.e(TAG, "completeAuthorization failed: ${throwable.message}", throwable)
                    handleGoogleError(throwable)
                }
            )
        }
    }

    private fun handleSuccessfulGoogleLink(account: CloudAccount) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val (googleFirst, googleLast) = parseGoogleName(account.displayName, account.email)
            val firstName = currentState.firstName.trim().ifBlank { googleFirst }
            val lastName = currentState.lastName.trim().ifBlank { googleLast }
            val photoUri = currentState.profileImageUri
                ?: account.photoUrl?.let { runCatching { Uri.parse(it) }.getOrNull() }

            val localResult = authRepository.createLocalProfile(
                firstName = firstName,
                lastName = lastName,
                profileImageUri = photoUri
            )

            localResult.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            isCompleted = true,
                            autoOpenCloudHub = true,
                            error = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            isCompleted = false,
                            error = throwable.message ?: "Error al configurar perfil"
                        )
                    }
                }
            )
        }
    }

    private fun handleGoogleError(throwable: Throwable) {
        if (ErrorSanitizer.isCancellation(throwable)) {
            _uiState.update { it.copy(isGoogleLoading = false, error = null) }
        } else {
            _uiState.update {
                it.copy(
                    isGoogleLoading = false,
                    error = ErrorSanitizer.sanitize(throwable, "Error al conectar con Google")
                )
            }
        }
    }

    fun parseGoogleName(displayName: String?, email: String): Pair<String, String> {
        val trimmed = displayName?.trim().orEmpty()
        if (trimmed.isNotBlank()) {
            val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }
            return when {
                parts.size == 1 -> Pair(parts[0], "")
                parts.size == 2 -> Pair(parts[0], parts[1])
                parts.size >= 3 -> Pair(parts.take(parts.size - 1).joinToString(" "), parts.last())
                else -> Pair(trimmed, "")
            }
        }
        val fallback = email.substringBefore("@").ifBlank { "Estudiante" }
        return Pair(fallback, "")
    }
}
