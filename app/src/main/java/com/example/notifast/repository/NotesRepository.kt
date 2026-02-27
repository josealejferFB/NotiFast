package com.example.notifast.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.notifast.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NotesRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val userId: String
        get() = auth.currentUser?.uid.orEmpty()

    fun getNotes(): Flow<List<Note>> {
        return db.collectionGroup("notes")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Note>()?.copy(id = doc.id)
                }
            }
    }

    fun getSubjects(): Flow<List<String>> {
        return db.collection("users")
            .document(userId)
            .collection("subjects")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.id }
            }
    }

    suspend fun addSubject(subjectName: String): Result<Unit> {
        return try {
            val data = mapOf(
                "name" to subjectName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(userId).collection("subjects").document(subjectName).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ESTRATEGIA LOCAL: Guarda la imagen en la memoria interna del teléfono (GRATIS)
     */
    suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            val fileName = "note_img_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            
            // 1. Cargar y optimizar
            val options = BitmapFactory.Options().apply { inSampleSize = 2 }
            context.contentResolver.openInputStream(uri).use { input ->
                val bitmap = BitmapFactory.decodeStream(input, null, options)
                FileOutputStream(file).use { out ->
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, out)
                }
            }
            
            // Retornamos la ruta del archivo local
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addNote(subject: String, title: String, content: String, imageUrl: String? = null): Result<Unit> {
        return try {
            val noteMap = hashMapOf(
                "userId" to userId,
                "subject" to subject,
                "title" to title,
                "content" to content,
                "imageUrl" to imageUrl, // Guardamos la ruta local en la DB
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(subject)
                .collection("notes")
                .add(noteMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(noteId: String, subject: String, title: String, content: String, imageUrl: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "title" to title,
                "content" to content,
                "timestamp" to FieldValue.serverTimestamp()
            )
            imageUrl?.let { updates["imageUrl"] = it }

            db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(subject)
                .collection("notes")
                .document(noteId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(note: Note): Result<Unit> {
        return try {
            val subjectRef = db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(note.subject)
            
            val notesCollectionRef = subjectRef.collection("notes")
            notesCollectionRef.document(note.id).delete().await()

            // LIMPIEZA LOCAL: Borramos el archivo del teléfono
            note.imageUrl?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }

            val remainingNotes = notesCollectionRef.limit(1).get().await()
            if (remainingNotes.isEmpty) {
                subjectRef.delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
