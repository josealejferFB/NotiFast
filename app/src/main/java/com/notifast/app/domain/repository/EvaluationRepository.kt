package com.notifast.app.domain.repository

import android.net.Uri
import com.notifast.app.data.model.Evaluation
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    fun getEvaluations(userId: String): Flow<List<Evaluation>>
    fun getEvaluation(userId: String, evaluationId: String): Flow<Evaluation?>
    suspend fun addEvaluation(userId: String, evaluation: Evaluation, imageUri: Uri?): Result<Unit>
    suspend fun updateEvaluation(userId: String, evaluation: Evaluation, imageUri: Uri?): Result<Unit>
    suspend fun deleteEvaluation(userId: String, evaluation: Evaluation): Result<Unit>
}
