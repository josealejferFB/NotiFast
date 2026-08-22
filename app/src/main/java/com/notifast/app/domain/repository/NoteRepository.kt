package com.notifast.app.domain.repository

import android.net.Uri
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(userId: String): Flow<List<Note>>
    fun getSubjects(userId: String): Flow<List<SubjectData>>
    suspend fun addNote(
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUri: Uri?
    ): Result<Unit>
    suspend fun addSubject(
        userId: String,
        subjectName: String,
        icon: String = "📚",
        colorHex: String = ""
    ): Result<Unit>
    suspend fun deleteNote(userId: String, note: Note): Result<Unit>
    suspend fun updateNote(userId: String, note: Note, imageUri: Uri?): Result<Unit>
    fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>>
    suspend fun deleteSubject(userId: String, subjectName: String): Result<Unit>
    suspend fun getNoteById(userId: String, subject: String, noteId: String): Result<Note>
}
