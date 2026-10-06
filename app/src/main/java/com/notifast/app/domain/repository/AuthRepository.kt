package com.notifast.app.domain.repository

import android.net.Uri
import com.notifast.app.data.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<Boolean?>

    suspend fun logout()
    suspend fun getCurrentUserId(): String?
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun updateProfile(
        uid: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<Unit>
    /** Reuses the existing local user if present (logout must not orphan data). */
    suspend fun createLocalProfile(
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String>
    /** Creates a brand-new profile after wiping all local data. */
    suspend fun createFreshLocalProfile(
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String>
    suspend fun getLocalUsers(): List<User>
}
