package com.example.notifast.domain.repository

import android.net.Uri
import com.example.notifast.data.model.Evaluation
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    fun getEvaluations(userId: String): Flow<List<Evaluation>>
    suspend fun addEvaluation(userId: String, evaluation: Evaluation, imageUri: Uri?): Result<Unit>
    suspend fun updateEvaluation(userId: String, evaluation: Evaluation, imageUri: Uri?): Result<Unit>
    suspend fun deleteEvaluation(userId: String, evaluation: Evaluation): Result<Unit>
}
