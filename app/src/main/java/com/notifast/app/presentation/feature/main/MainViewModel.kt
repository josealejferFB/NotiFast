package com.notifast.app.presentation.feature.main

import androidx.lifecycle.ViewModel
import com.notifast.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val authState: StateFlow<Boolean?> = authRepository.authState
}
