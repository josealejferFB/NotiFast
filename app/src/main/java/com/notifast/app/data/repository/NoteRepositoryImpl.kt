package com.notifast.app.data.repository

import android.net.Uri
import android.util.Log
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NoteRepository"

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : NoteRepository {

    override fun getNotes(userId: String): Flow<List<Note>> {
        return db.collection("users").document(userId)
            .collection("subjects")
            .snapshots()
            .flatMapLatest { snapshot ->
                val subjects = snapshot.documents.mapNotNull { it.getString("name") ?: it.id }
                if (subjects.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val noteFlows: List<Flow<List<Note>>> = subjects.map { subject ->
                        db.collection("users").document(userId)
                            .collection("subjects").document(subject)
                            .collection("notes")
                            .snapshots()
                            .map { noteSnapshot ->
                                noteSnapshot.documents.mapNotNull { doc ->
                                    doc.toObject(Note::class.java)?.copy(id = doc.id)
                                }
                            }
                    }
                    combine(noteFlows) { arrays: Array<List<Note>> -> arrays.flatMap { it } }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error fetching notes for user $userId", e)
                emit(emptyList())
            }
    }

    override fun getSubjects(userId: String): Flow<List<SubjectData>> {
        return db.collection("users").document(userId)
            .collection("subjects")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    SubjectData(
                        name = doc.getString("name") ?: doc.id,
                        icon = doc.getString("icon") ?: "📚",
                        colorHex = doc.getString("colorHex") ?: ""
                    )
                }
            }
            .catch { e ->
                Log.e(TAG, "Error fetching subjects for user $userId", e)
                emit(emptyList())
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
            val noteMap = mutableMapOf<String, Any>(
                "userId" to userId,
                "subject" to subject,
                "title" to title,
                "content" to content,
                "timestamp" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!imageUrl.isNullOrEmpty()) {
                noteMap["imageUrl"] = imageUrl
            }
            db.collection("users").document(userId)
                .collection("subjects").document(subject)
                .collection("notes").add(noteMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add note '$title' in subject '$subject'", e)
            Result.failure(e)
        }
    }

    override suspend fun addSubject(userId: String, subjectName: String, icon: String, colorHex: String): Result<Unit> {
        return try {
            val docRef = db.collection("users").document(userId)
                .collection("subjects").document(subjectName)
            val existing = docRef.get().await()
            if (existing.exists()) {
                return Result.failure(Exception("La materia ya existe"))
            }

            val data = mutableMapOf<String, Any>(
                "name" to subjectName,
                "icon" to icon,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            if (colorHex.isNotBlank()) data["colorHex"] = colorHex
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add subject '$subjectName'", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(userId: String, note: Note): Result<Unit> {
        return try {
            if (!note.imageUrl.isNullOrEmpty()) {
                try {
                    imageRepository.deleteImage(note.imageUrl)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete note image: ${note.imageUrl}", e)
                }
            }
            db.collection("users").document(userId)
                .collection("subjects").document(note.subject)
                .collection("notes").document(note.id)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete note '${note.id}'", e)
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
                "subject" to note.subject,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!imageUrl.isNullOrEmpty()) {
                updates["imageUrl"] = imageUrl
            } else {
                updates["imageUrl"] = FieldValue.delete()
            }
            db.collection("users").document(userId)
                .collection("subjects").document(note.subject)
                .collection("notes").document(note.id)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update note '${note.id}'", e)
            Result.failure(e)
        }
    }

    override fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>> {
        return db.collection("users").document(userId)
            .collection("subjects").document(subject)
            .collection("notes")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.copy(id = doc.id)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error fetching notes for subject '$subject'", e)
                emit(emptyList())
            }
    }

    override suspend fun deleteSubject(userId: String, subjectName: String): Result<Unit> {
        return try {
            val batch = db.batch()

            // 1. Delete all notes under this subject and their local images
            val notesSnapshot = db.collection("users").document(userId)
                .collection("subjects").document(subjectName)
                .collection("notes")
                .get()
                .await()
            for (doc in notesSnapshot.documents) {
                val imageUrl = doc.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    try {
                        imageRepository.deleteImage(imageUrl)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete note image: $imageUrl", e)
                    }
                }
                batch.delete(doc.reference)
            }

            // 2. Delete all evaluations linked to this subject
            val evaluationsSnapshot = db.collection("users").document(userId)
                .collection("evaluations")
                .whereEqualTo("subjectName", subjectName)
                .get()
                .await()
            for (doc in evaluationsSnapshot.documents) {
                val imageUrl = doc.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    try {
                        imageRepository.deleteImage(imageUrl)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete evaluation image: $imageUrl", e)
                    }
                }
                batch.delete(doc.reference)
            }

            // 3. Delete all schedules linked to this subject
            val schedulesSnapshot = db.collection("users").document(userId)
                .collection("schedules")
                .whereEqualTo("subjectName", subjectName)
                .get()
                .await()
            for (doc in schedulesSnapshot.documents) {
                batch.delete(doc.reference)
            }

            // 4. Delete the subject document itself
            batch.delete(
                db.collection("users").document(userId)
                    .collection("subjects").document(subjectName)
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete subject '$subjectName' and cascading children", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getNoteById(userId: String, subject: String, noteId: String): Result<Note> {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("subjects").document(subject)
                .collection("notes").document(noteId)
                .get()
                .await()
            val note = doc.toObject(Note::class.java)?.copy(id = doc.id)
            if (note != null) {
                Result.success(note)
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch note with id '$noteId' in subject '$subject'", e)
            Result.failure(e)
        }
    }
}
