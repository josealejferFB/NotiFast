package com.example.notifast.data.repository

import android.net.Uri
import com.example.notifast.data.model.Evaluation
import com.example.notifast.domain.repository.EvaluationRepository
import com.example.notifast.domain.repository.ImageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : EvaluationRepository {

    override fun getEvaluations(userId: String): Flow<List<Evaluation>> {
        return db.collection("users").document(userId)
            .collection("evaluations")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Evaluation::class.java)?.copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun addEvaluation(
        userId: String,
        evaluation: Evaluation,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl = evaluation.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val evalMap = mutableMapOf<String, Any>(
                "userId" to evaluation.userId,
                "subjectName" to evaluation.subjectName,
                "title" to evaluation.title,
                "description" to evaluation.description,
                "date" to (evaluation.date ?: ""),
                "noteIds" to evaluation.noteIds,
                "imageUrl" to (imageUrl ?: ""),
                "quickTips" to evaluation.quickTips,
                "isPresented" to evaluation.isPresented,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            if (evaluation.grade != null) evalMap["grade"] = evaluation.grade
            if (evaluation.weight != null) evalMap["weight"] = evaluation.weight
            db.collection("users").document(userId)
                .collection("evaluations").add(evalMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEvaluation(
        userId: String,
        evaluation: Evaluation,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl = evaluation.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val updates = mutableMapOf<String, Any>(
                "title" to evaluation.title,
                "description" to evaluation.description,
                "subjectName" to evaluation.subjectName,
                "date" to (evaluation.date ?: ""),
                "quickTips" to evaluation.quickTips,
                "isPresented" to evaluation.isPresented
            )
            if (imageUrl != null) updates["imageUrl"] = imageUrl
            if (evaluation.grade != null) updates["grade"] = evaluation.grade
            if (evaluation.weight != null) updates["weight"] = evaluation.weight
            db.collection("users").document(userId)
                .collection("evaluations").document(evaluation.id)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvaluation(userId: String, evaluation: Evaluation): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("evaluations").document(evaluation.id)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
