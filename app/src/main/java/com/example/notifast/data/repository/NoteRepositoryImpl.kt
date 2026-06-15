package com.example.notifast.data.repository

import android.net.Uri
import com.example.notifast.data.model.Note
import com.example.notifast.domain.repository.ImageRepository
import com.example.notifast.domain.repository.NoteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : NoteRepository {

    override fun getNotes(userId: String): Flow<List<Note>> {
        return db.collectionGroup("notes")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.copy(id = doc.id)
                }
            }
    }

    override fun getSubjects(userId: String): Flow<List<String>> {
        return db.collection("users").document(userId)
            .collection("subjects")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("name") }
            }
    }

    override suspend fun addNote(
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val noteMap = mapOf(
                "userId" to userId,
                "subject" to subject,
                "title" to title,
                "content" to content,
                "imageUrl" to (imageUrl ?: "")
            )
            db.collection("users").document(userId)
                .collection("subjects").document(subject)
                .collection("notes").add(noteMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addSubject(userId: String, subjectName: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("subjects").document(subjectName)
                .set(mapOf("name" to subjectName, "createdAt" to com.google.firebase.Timestamp.now()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(userId: String, note: Note): Result<Unit> {
        return try {
            if (!note.imageUrl.isNullOrEmpty()) {
                imageRepository.deleteImage(note.imageUrl)
            }
            db.collection("users").document(userId)
                .collection("subjects").document(note.subject)
                .collection("notes").document(note.id)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(
        userId: String,
        note: Note,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl = note.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val updates = mutableMapOf<String, Any>(
                "title" to note.title,
                "content" to note.content,
                "subject" to note.subject
            )
            if (imageUrl != null) updates["imageUrl"] = imageUrl
            db.collection("users").document(userId)
                .collection("subjects").document(note.subject)
                .collection("notes").document(note.id)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>> {
        return db.collection("users").document(userId)
            .collection("subjects").document(subject)
            .collection("notes")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.copy(id = doc.id)
                }
            }
    }
}
