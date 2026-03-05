package com.example.notifast.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.notifast.model.Evaluation
import com.example.notifast.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID

class NotesRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val userId: String
        get() = auth.currentUser?.uid.orEmpty()

    // --- SECCIÓN NOTAS ---

    fun getNotes(): Flow<List<Note>> {
        val uid = userId
        if (uid.isEmpty()) return emptyFlow()

        return db.collectionGroup("notes")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Note>()?.copy(id = doc.id)
                }
            }
    }

    fun getSubjects(): Flow<List<String>> {
        val uid = userId
        if (uid.isEmpty()) return emptyFlow()

        return db.collection("users")
            .document(uid)
            .collection("subjects")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.id }
            }
    }

    suspend fun addSubject(subjectName: String): Result<Unit> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val data = mapOf(
                "name" to subjectName,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("subjects").document(subjectName).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addNote(subject: String, title: String, content: String, imageUrl: String? = null): Result<Unit> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val noteMap = hashMapOf(
                "userId" to uid,
                "subject" to subject,
                "title" to title,
                "content" to content,
                "imageUrl" to imageUrl,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("users")
                .document(uid)
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

    suspend fun deleteNote(note: Note): Result<Unit> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val subjectRef = db.collection("users")
                .document(uid)
                .collection("subjects")
                .document(note.subject)
            
            val notesCollectionRef = subjectRef.collection("notes")
            notesCollectionRef.document(note.id).delete().await()

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

    // --- SECCIÓN EVALUACIONES ---

    fun getEvaluations(): Flow<List<Evaluation>> {
        val uid = userId
        if (uid.isEmpty()) return emptyFlow()

        return db.collection("users")
            .document(uid)
            .collection("evaluations")
            .orderBy("date", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Evaluation>()?.copy(id = doc.id)
                }
            }
    }

    suspend fun addEvaluation(
        subjectName: String,
        title: String,
        description: String,
        date: Date,
        noteIds: List<String>,
        quickTips: String,
        imageUrl: String? = null
    ): Result<Unit> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val evaluation = Evaluation(
                userId = uid,
                subjectName = subjectName,
                title = title,
                description = description,
                date = date,
                noteIds = noteIds,
                quickTips = quickTips,
                imageUrl = imageUrl
            )
            
            db.collection("users")
                .document(uid)
                .collection("evaluations")
                .add(evaluation)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvaluation(
        evaluationId: String,
        title: String,
        description: String,
        date: Date,
        quickTips: String,
        imageUrl: String? = null
    ): Result<Unit> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val updates = mutableMapOf<String, Any>(
                "title" to title,
                "description" to description,
                "date" to date,
                "quickTips" to quickTips
            )
            imageUrl?.let { updates["imageUrl"] = it }

            db.collection("users")
                .document(uid)
                .collection("evaluations")
                .document(evaluationId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvaluation(evaluationId: String, imageUrl: String? = null): Result<Unit> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            db.collection("users")
                .document(uid)
                .collection("evaluations")
                .document(evaluationId)
                .delete()
                .await()

            imageUrl?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- UTILIDADES DE IMAGEN ---

    suspend fun uploadImage(uri: Uri): Result<String> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri).use { 
                BitmapFactory.decodeStream(it, null, options) 
            }

            var inSampleSize = 1
            if (options.outHeight > 1024 || options.outWidth > 1024) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= 1024 && halfWidth / inSampleSize >= 1024) {
                    inSampleSize *= 2
                }
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            
            val bitmap = context.contentResolver.openInputStream(uri).use { 
                BitmapFactory.decodeStream(it, null, options) 
            }

            FileOutputStream(file).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, out)
            }
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(noteId: String, subject: String, title: String, content: String, imageUrl: String? = null): Result<Unit> {
        val uid = userId
        if (uid.isEmpty()) return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val updates = mutableMapOf<String, Any>(
                "title" to title,
                "content" to content,
                "timestamp" to FieldValue.serverTimestamp()
            )
            imageUrl?.let { updates["imageUrl"] = it }

            db.collection("users")
                .document(uid)
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
}
