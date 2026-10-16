package com.notifast.app.data.repository

import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.notifast.app.data.local.dao.UserDao
import com.notifast.app.data.local.entity.UserEntity
import com.notifast.app.data.model.User
import com.notifast.app.di.IoDispatcher
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LocalAuthRepo"

@Singleton
class LocalAuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val dataStore: DataStore<Preferences>,
    private val imageRepository: ImageRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val keyActiveUserId = stringPreferencesKey("active_local_user_id")
    private val cachedUserId = AtomicReference<String?>(null)

    private val _authState = MutableStateFlow<Boolean?>(null)
    override val authState: StateFlow<Boolean?> = _authState.asStateFlow()

    init {
        CoroutineScope(ioDispatcher).launch {
            initializeAuthState()
        }
    }

    internal suspend fun initializeAuthState() {
        try {
            val uid = getCurrentUserId()
            if (uid != null && userDao.getUserById(uid) != null) {
                _authState.value = true
            } else {
                _authState.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing auth state", e)
            _authState.value = false
        }
    }

    override suspend fun getCurrentUserId(): String? {
        cachedUserId.get()?.let { return it }
        return try {
            val prefs = dataStore.data.first()
            val uid = prefs[keyActiveUserId]
            cachedUserId.set(uid)
            uid
        } catch (e: Exception) {
            Log.e(TAG, "Error reading active user id from DataStore", e)
            null
        }
    }

    override suspend fun createLocalProfile(
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String> {
        return try {
            // Logout must not orphan data: reuse the existing local user if present.
            val existing = userDao.getAllUsers().firstOrNull()
            if (existing != null) {
                return activateExistingUser(existing, firstName, lastName, profileImageUri)
            }
            insertNewUser(firstName, lastName, profileImageUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating local profile", e)
            Result.failure(e)
        }
    }

    override suspend fun createFreshLocalProfile(
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String> {
        return try {
            userDao.getAllUsers().forEach { userDao.deleteUser(it.uid) }
            insertNewUser(firstName, lastName, profileImageUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating fresh local profile", e)
            Result.failure(e)
        }
    }

    override suspend fun getLocalUsers(): List<com.notifast.app.data.model.User> {
        return userDao.getAllUsers().map {
            com.notifast.app.data.model.User(
                uid = it.uid,
                email = it.email,
                firstName = it.firstName,
                lastName = it.lastName,
                profilePictureUrl = it.profilePictureUrl
            )
        }
    }

    private suspend fun activateExistingUser(
        existing: UserEntity,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String> {
        var profilePictureUrl = existing.profilePictureUrl
        if (profileImageUri != null) {
            val uploadResult = imageRepository.uploadProfileImage(existing.uid, profileImageUri)
            if (uploadResult.isSuccess) {
                profilePictureUrl = uploadResult.getOrNull()
            }
        }
        val updated = existing.copy(
            firstName = firstName,
            lastName = lastName,
            profilePictureUrl = profilePictureUrl,
            updatedAt = Date()
        )
        userDao.updateUser(updated)
        dataStore.edit { prefs -> prefs[keyActiveUserId] = existing.uid }
        cachedUserId.set(existing.uid)
        _authState.value = true
        return Result.success(existing.uid)
    }

    private suspend fun insertNewUser(
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String> {
        val uid = UUID.randomUUID().toString()
        var profilePictureUrl: String? = null
        if (profileImageUri != null) {
            val uploadResult = imageRepository.uploadProfileImage(uid, profileImageUri)
            if (uploadResult.isSuccess) {
                profilePictureUrl = uploadResult.getOrNull()
            }
        }

        val now = System.currentTimeMillis()
        userDao.insertUser(
            UserEntity(
                uid = uid,
                email = "$uid@local.notifast",
                firstName = firstName,
                lastName = lastName,
                profilePictureUrl = profilePictureUrl,
                createdAtTimestamp = now,
                updatedAtTimestamp = now
            )
        )

        dataStore.edit { prefs -> prefs[keyActiveUserId] = uid }
        cachedUserId.set(uid)
        _authState.value = true
        return Result.success(uid)
    }

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val entity = userDao.getUserById(uid)
                ?: return Result.failure(Exception("User not found"))
            Result.success(
                User(
                    uid = entity.uid,
                    email = entity.email,
                    firstName = entity.firstName,
                    lastName = entity.lastName,
                    profilePictureUrl = entity.profilePictureUrl
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile for $uid", e)
            Result.failure(e)
        }
    }

    override fun observeUserProfile(uid: String): Flow<User?> {
        return userDao.observeUser(uid).map { entity ->
            entity?.let {
                User(
                    uid = it.uid,
                    email = it.email,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    profilePictureUrl = it.profilePictureUrl
                )
            }
        }
    }

    override suspend fun updateProfile(
        uid: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<Unit> {
        return try {
            val existing = userDao.getUserById(uid)
                ?: return Result.failure(Exception("User not found"))

            var profilePictureUrl = existing.profilePictureUrl
            if (profileImageUri != null) {
                val uploadResult = imageRepository.uploadProfileImage(uid, profileImageUri)
                if (uploadResult.isSuccess) {
                    val newUrl = uploadResult.getOrNull()
                    existing.profilePictureUrl?.takeIf { it.isNotBlank() && it != newUrl }?.let { oldUrl ->
                        try {
                            imageRepository.deleteImage(oldUrl)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to delete old profile image $oldUrl", e)
                        }
                    }
                    profilePictureUrl = newUrl
                }
            }

            val updatedEntity = existing.copy(
                firstName = firstName,
                lastName = lastName,
                profilePictureUrl = profilePictureUrl,
                updatedAt = Date()
            )
            userDao.updateUser(updatedEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile for $uid", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            dataStore.edit { prefs ->
                prefs.remove(keyActiveUserId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing active user during logout", e)
        } finally {
            cachedUserId.set(null)
            _authState.value = false
        }
    }
}
