package com.example.notifast.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.notifast.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AuthRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        email: String, 
        pass: String, 
        firstName: String, 
        lastName: String, 
        imageUri: Uri?
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user!!.uid
            
            var localProfilePath: String? = null

            imageUri?.let { uri ->
                val fileName = "profile_$uid.jpg"
                val file = File(context.filesDir, fileName)
                
                context.contentResolver.openInputStream(uri).use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    FileOutputStream(file).use { out ->
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                }
                localProfilePath = file.absolutePath
            }

            val user = User(
                uid = uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                profilePictureUrl = localProfilePath
            )
            
            db.collection("users").document(uid).set(user).await()
            
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ACTUALIZACIÓN DE PERFIL:
     * Gestiona el cambio de nombre, apellido y foto local.
     */
    suspend fun updateProfile(
        firstName: String, 
        lastName: String, 
        imageUri: Uri?
    ): Result<Unit> {
        val uid = currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
        
        return try {
            val updates = mutableMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName
            )

            imageUri?.let { uri ->
                val fileName = "profile_$uid.jpg"
                val file = File(context.filesDir, fileName)
                
                context.contentResolver.openInputStream(uri).use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    FileOutputStream(file).use { out ->
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                }
                updates["profilePictureUrl"] = file.absolutePath
            }

            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
