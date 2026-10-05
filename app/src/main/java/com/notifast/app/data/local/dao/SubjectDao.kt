package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifast.app.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>): List<Long>

    @Query("SELECT * FROM subjects WHERE userId = :userId ORDER BY name ASC")
    fun getSubjects(userId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE userId = :userId AND name = :name LIMIT 1")
    suspend fun getSubjectByName(userId: String, name: String): SubjectEntity?

    @Query("DELETE FROM subjects WHERE userId = :userId AND name = :name")
    suspend fun deleteSubject(userId: String, name: String): Int

    @Query("DELETE FROM subjects WHERE userId = :userId")
    suspend fun deleteSubjectsByUserId(userId: String): Int
}
