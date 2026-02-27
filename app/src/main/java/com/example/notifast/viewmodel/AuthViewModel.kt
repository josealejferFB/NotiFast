package com.example.notifast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val user = _user.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.login(email, pass)
                .onSuccess { _user.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.signUp(email, pass)
                .onSuccess { _user.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _user.value = null
    }
}
