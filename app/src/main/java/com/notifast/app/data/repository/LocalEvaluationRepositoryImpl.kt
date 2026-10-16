package com.notifast.app.data.repository

import android.net.Uri
import android.util.Log
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LocalEvaluationRepo"

@Singleton
class LocalEvaluationRepositoryImpl @Inject constructor(
    private val evaluationDao: EvaluationDao,
    private val imageRepository: ImageRepository,
    private val subjectDao: com.notifast.app.data.local.dao.SubjectDao? = null
) : EvaluationRepository {

    override fun getEvaluations(userId: String): Flow<List<Evaluation>> {
        return evaluationDao.getEvaluations(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun getEvaluation(userId: String, evaluationId: String): Flow<Evaluation?> {
        return evaluationDao.getEvaluationById(userId, evaluationId).map { it?.toDomain() }
    }

    override suspend fun addEvaluation(
        userId: String,
        evaluation: Evaluation,
        imageUri: Uri?
    ): Result<String> {
        return try {
            var imageUrl = evaluation.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isFailure) {
                    return Result.failure(uploadResult.exceptionOrNull() ?: Exception("No se pudo subir la imagen"))
                }
                imageUrl = uploadResult.getOrNull()
            }
            val evalId = if (evaluation.id.isBlank()) UUID.randomUUID().toString() else evaluation.id
            val now = Date()
            val resolvedSubjectId = if (evaluation.subjectId.isNotBlank()) {
                evaluation.subjectId
            } else {
                subjectDao?.getSubjectByName(userId, evaluation.subjectName)?.id.orEmpty()
            }
            evaluationDao.insertEvaluation(
                EvaluationEntity(
                    id = evalId,
                    userId = userId,
                    subjectId = resolvedSubjectId,
                    subjectName = evaluation.subjectName,
                    title = evaluation.title,
                    description = evaluation.description,
                    date = evaluation.date,
                    noteIds = evaluation.noteIds,
                    imageUrl = imageUrl,
                    quickTips = evaluation.quickTips,
                    type = evaluation.type,
                    grade = evaluation.grade,
                    weight = evaluation.weight,
                    isPresented = evaluation.isPresented,
                    gradeScaleId = evaluation.gradeScaleId,
                    createdAt = evaluation.createdAt ?: now,
                    updatedAt = now
                )
            )
            Result.success(evalId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add evaluation '${evaluation.title}' for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEvaluation(
        userId: String,
        evaluation: Evaluation,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            val existing = evaluationDao.getEvaluationByIdSync(userId, evaluation.id)
            var imageUrl = evaluation.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isFailure) {
                    return Result.failure(uploadResult.exceptionOrNull() ?: Exception("No se pudo subir la imagen"))
                }
                val newUrl = uploadResult.getOrNull()
                existing?.imageUrl?.takeIf { it.isNotBlank() && it != newUrl }?.let { oldUrl ->
                    try {
                        imageRepository.deleteImage(oldUrl)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete old evaluation image $oldUrl", e)
                    }
                }
                imageUrl = newUrl
            } else if (imageUrl.isNullOrEmpty() && !existing?.imageUrl.isNullOrEmpty()) {
                try {
                    imageRepository.deleteImage(existing!!.imageUrl!!)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete removed evaluation image ${existing.imageUrl}", e)
                }
            }

            val resolvedSubjectId = if (evaluation.subjectId.isNotBlank()) {
                evaluation.subjectId
            } else {
                existing?.subjectId?.takeIf { it.isNotBlank() }
                    ?: subjectDao?.getSubjectByName(userId, evaluation.subjectName)?.id.orEmpty()
            }

            evaluationDao.updateEvaluation(
                evaluation.toEntity(userId).copy(
                    subjectId = resolvedSubjectId,
                    imageUrl = imageUrl,
                    createdAt = evaluation.createdAt ?: existing?.createdAt ?: Date(),
                    updatedAt = Date()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update evaluation '${evaluation.id}'", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEvaluations(
        userId: String,
        evaluations: List<Evaluation>
    ): Result<Unit> {
        return try {
            evaluationDao.updateEvaluations(evaluations.map { it.toEntity(userId) })
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to batch update ${evaluations.size} evaluations", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteEvaluation(userId: String, evaluation: Evaluation): Result<Unit> {
        return try {
            if (!evaluation.imageUrl.isNullOrEmpty()) {
                try {
                    imageRepository.deleteImage(evaluation.imageUrl)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete evaluation image ${evaluation.imageUrl}", e)
                }
            }
            evaluationDao.deleteEvaluationById(userId, evaluation.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete evaluation '${evaluation.id}'", e)
            Result.failure(e)
        }
    }

    private fun EvaluationEntity.toDomain(): Evaluation = Evaluation(
        id = id,
        userId = userId,
        subjectId = subjectId,
        subjectName = subjectName,
        title = title,
        description = description,
        date = date,
        noteIds = noteIds,
        imageUrl = imageUrl,
        quickTips = quickTips,
        type = type,
        grade = grade,
        weight = weight,
        isPresented = isPresented,
        gradeScaleId = gradeScaleId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Evaluation.toEntity(userId: String): EvaluationEntity = EvaluationEntity(
        id = id,
        userId = userId,
        subjectId = subjectId,
        subjectName = subjectName,
        title = title,
        description = description,
        date = date,
        noteIds = noteIds,
        imageUrl = imageUrl,
        quickTips = quickTips,
        type = type,
        grade = grade,
        weight = weight,
        isPresented = isPresented,
        gradeScaleId = gradeScaleId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
