package com.example.notifast.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.model.User
import com.example.notifast.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Inicializamos el repositorio usando el contexto de la aplicación
    private val repository: AuthRepository = AuthRepository(application.applicationContext)

    private val _user = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val user = _user.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        _user.value?.let { fetchUserProfile(it.uid) }
    }

    private fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val document = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                if (document.exists()) {
                    _userProfile.value = document.toObject<User>()
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar perfil: ${e.message}"
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.login(email, pass)
                .onSuccess { 
                    _user.value = it 
                    fetchUserProfile(it.uid)
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun signUp(email: String, pass: String, firstName: String, lastName: String, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.signUp(email, pass, firstName, lastName, imageUri)
                .onSuccess { 
                    _user.value = it 
                    fetchUserProfile(it.uid)
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _user.value = null
        _userProfile.value = null
    }
}
