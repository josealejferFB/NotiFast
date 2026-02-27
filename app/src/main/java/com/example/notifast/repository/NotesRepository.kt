package com.example.notifast.repository

import com.example.notifast.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NotesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val userId: String
        get() = auth.currentUser?.uid.orEmpty()

    fun getNotes(): Flow<List<Note>> {
        return db.collection("notes")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Note>()?.copy(id = doc.id)
                }
            }
    }

    suspend fun addNote(title: String, content: String): Result<Unit> {
        return try {
            val note = hashMapOf(
                "userId" to userId,
                "title" to title,
                "content" to content,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("notes").add(note).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(noteId: String, title: String, content: String): Result<Unit> {
        return try {
            val updates = hashMapOf(
                "title" to title,
                "content" to content,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Actualizamos la fecha al editar
            )
            db.collection("notes").document(noteId).update(updates as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            db.collection("notes").document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
