package com.example.notifast.domain.repository

import android.net.Uri
import com.example.notifast.data.model.Note
import com.example.notifast.data.model.SubjectData
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
    suspend fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>>
    suspend fun deleteSubject(userId: String, subjectName: String): Result<Unit>
}
