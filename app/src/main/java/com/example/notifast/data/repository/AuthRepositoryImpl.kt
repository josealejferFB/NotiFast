package com.example.notifast.data.repository

import android.net.Uri
import com.example.notifast.data.model.User
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : AuthRepository {

    private val _authState = MutableStateFlow<Boolean?>(null)
    override val authState: StateFlow<Boolean?> = _authState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
        }
    }

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed: no user"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("SignUp failed: no user"))

            var profilePictureUrl: String? = null
            if (profileImageUri != null) {
                val uploadResult = imageRepository.uploadProfileImage(uid, profileImageUri)
                if (uploadResult.isSuccess) profilePictureUrl = uploadResult.getOrNull()
            }

            val userMap = mapOf(
                "uid" to uid,
                "email" to email,
                "firstName" to firstName,
                "lastName" to lastName,
                "profilePictureUrl" to (profilePictureUrl ?: "")
            )
            db.collection("users").document(uid).set(userMap).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Google sign-in failed"))

            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                val nameParts = (result.user?.displayName ?: "").split(" ")
                val firstName = nameParts.firstOrNull { it.isNotBlank() } ?: ""
                val lastName = nameParts.drop(1).joinToString(" ")
                val userMap = mapOf(
                    "uid" to uid,
                    "email" to (result.user?.email ?: ""),
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "profilePictureUrl" to (result.user?.photoUrl?.toString() ?: "")
                )
                db.collection("users").document(uid).set(userMap).await()
            }
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = db.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
                ?: return Result.failure(Exception("User not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        uid: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<Unit> {
        return try {
            var profilePictureUrl: String? = null
            if (profileImageUri != null) {
                val uploadResult = imageRepository.uploadProfileImage(uid, profileImageUri)
                if (uploadResult.isSuccess) profilePictureUrl = uploadResult.getOrNull()
            }
            val updates = mutableMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName
            )
            if (profilePictureUrl != null) {
                updates["profilePictureUrl"] = profilePictureUrl
            }
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
