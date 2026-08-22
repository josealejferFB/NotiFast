package com.notifast.app.domain.repository

import android.net.Uri
import com.notifast.app.data.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<Boolean?>

    suspend fun login(email: String, password: String): Result<String>
    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String>
    suspend fun signInWithGoogle(idToken: String): Result<String>
    suspend fun logout()
    suspend fun getCurrentUserId(): String?
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun updateProfile(
        uid: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<Unit>
}
