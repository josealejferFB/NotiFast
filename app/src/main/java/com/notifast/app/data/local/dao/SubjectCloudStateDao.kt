package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectCloudStateDao {
    @Query("SELECT * FROM subject_cloud_state WHERE accountId = :accountId")
    fun observe(accountId: String): Flow<List<SubjectCloudStateEntity>>

    @Query("SELECT * FROM subject_cloud_state WHERE accountId = :accountId")
    suspend fun getAll(accountId: String): List<SubjectCloudStateEntity>

    @Upsert
    suspend fun upsert(state: SubjectCloudStateEntity)

    @Query("DELETE FROM subject_cloud_state WHERE subjectId = :subjectId")
    suspend fun deleteBySubject(subjectId: String)

    @Query("DELETE FROM subject_cloud_state WHERE accountId = :accountId")
    suspend fun deleteByAccount(accountId: String)
}
