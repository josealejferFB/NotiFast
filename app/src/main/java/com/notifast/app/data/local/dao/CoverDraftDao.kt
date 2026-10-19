package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifast.app.data.local.entity.CoverDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoverDraftDao {

    @Query("SELECT * FROM cover_drafts WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeDrafts(userId: String): Flow<List<CoverDraftEntity>>

    @Query("SELECT * FROM cover_drafts WHERE userId = :userId AND id = :draftId LIMIT 1")
    suspend fun getDraftById(userId: String, draftId: String): CoverDraftEntity?

    @Query("SELECT * FROM cover_drafts WHERE userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestDraft(userId: String): CoverDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(entity: CoverDraftEntity)

    @Query("DELETE FROM cover_drafts WHERE userId = :userId AND id = :draftId")
    suspend fun deleteDraft(userId: String, draftId: String)
}
