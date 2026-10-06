package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notifast.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>): List<Long>

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Query("DELETE FROM notes WHERE userId = :userId AND id = :noteId")
    suspend fun deleteNoteById(userId: String, noteId: String): Int

    @Query("DELETE FROM notes WHERE userId = :userId AND subject = :subject")
    suspend fun deleteNotesBySubject(userId: String, subject: String): Int

    @Query("DELETE FROM notes WHERE userId = :userId AND subjectId = :subjectId")
    suspend fun deleteNotesBySubjectId(userId: String, subjectId: String): Int

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteNotesByUserId(userId: String): Int

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotes(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND subject = :subject ORDER BY timestamp DESC")
    fun getNotesBySubject(userId: String, subject: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND subjectId = :subjectId ORDER BY timestamp DESC")
    fun getNotesBySubjectId(userId: String, subjectId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND id = :noteId LIMIT 1")
    suspend fun getNoteById(userId: String, noteId: String): NoteEntity?
}
