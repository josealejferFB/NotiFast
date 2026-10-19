package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifast.app.data.local.entity.StudyDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDocumentDao {

    @Query("SELECT * FROM study_documents WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeDocuments(userId: String): Flow<List<StudyDocumentEntity>>

    @Query("SELECT * FROM study_documents WHERE userId = :userId AND subjectName = :subjectName COLLATE NOCASE ORDER BY createdAt DESC")
    fun observeDocumentsBySubject(userId: String, subjectName: String): Flow<List<StudyDocumentEntity>>

    @Query("SELECT * FROM study_documents WHERE userId = :userId AND id = :docId LIMIT 1")
    suspend fun getDocumentById(userId: String, docId: String): StudyDocumentEntity?

    @Query("SELECT * FROM study_documents WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllDocuments(userId: String): List<StudyDocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(entity: StudyDocumentEntity)

    @Query("DELETE FROM study_documents WHERE userId = :userId AND id = :docId")
    suspend fun deleteDocument(userId: String, docId: String)
}
