package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notifast.app.data.local.entity.EvaluationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(eval: EvaluationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluations(evals: List<EvaluationEntity>): List<Long>

    @Update
    suspend fun updateEvaluation(eval: EvaluationEntity): Int

    @Query("DELETE FROM evaluations WHERE userId = :userId AND id = :evalId")
    suspend fun deleteEvaluationById(userId: String, evalId: String): Int

    @Query("DELETE FROM evaluations WHERE userId = :userId AND subjectName = :subjectName")
    suspend fun deleteEvaluationsBySubject(userId: String, subjectName: String): Int

    @Query("DELETE FROM evaluations WHERE userId = :userId AND subjectId = :subjectId")
    suspend fun deleteEvaluationsBySubjectId(userId: String, subjectId: String): Int

    @Query("DELETE FROM evaluations WHERE userId = :userId")
    suspend fun deleteEvaluationsByUserId(userId: String): Int

    @Query("SELECT * FROM evaluations WHERE userId = :userId ORDER BY date ASC")
    fun getEvaluations(userId: String): Flow<List<EvaluationEntity>>

    @Query("SELECT * FROM evaluations WHERE userId = :userId AND subjectName = :subjectName ORDER BY date ASC")
    fun getEvaluationsBySubject(userId: String, subjectName: String): Flow<List<EvaluationEntity>>

    @Query("SELECT * FROM evaluations WHERE userId = :userId AND subjectId = :subjectId ORDER BY date ASC")
    fun getEvaluationsBySubjectId(userId: String, subjectId: String): Flow<List<EvaluationEntity>>

    @Query("SELECT * FROM evaluations WHERE userId = :userId AND id = :evalId LIMIT 1")
    fun getEvaluationById(userId: String, evalId: String): Flow<EvaluationEntity?>

    @Query("SELECT * FROM evaluations WHERE userId = :userId AND id = :evalId LIMIT 1")
    suspend fun getEvaluationByIdSync(userId: String, evalId: String): EvaluationEntity?
}
