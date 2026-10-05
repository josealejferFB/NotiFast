package com.notifast.app.data.repository

import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LocalNoteRepository"

@Singleton
class LocalNoteRepositoryImpl @Inject constructor(
    private val db: NotiFastDatabase,
    private val noteDao: NoteDao,
    private val subjectDao: SubjectDao,
    private val imageRepository: ImageRepository,
    private val evaluationDao: EvaluationDao = db.evaluationDao(),
    private val scheduleDao: ScheduleDao = db.scheduleDao()
) : NoteRepository {

    override fun getNotes(userId: String): Flow<List<Note>> {
        return noteDao.getNotes(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSubjects(userId: String): Flow<List<SubjectData>> {
        return subjectDao.getSubjects(userId).map { list ->
            list.map { SubjectData(name = it.name, icon = it.icon, colorHex = it.colorHex) }
        }
    }

    override suspend fun addNote(
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUri: Uri?,
        tags: List<String>
    ): Result<Unit> {
        return try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) {
                    imageUrl = uploadResult.getOrNull()
                }
            }
            val noteId = UUID.randomUUID().toString()
            val now = Date()
            val entity = NoteEntity(
                id = noteId,
                userId = userId,
                subject = subject,
                title = title,
                content = content,
                imageUrl = imageUrl,
                tags = tags,
                timestamp = now,
                updatedAt = now
            )
            noteDao.insertNote(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add note '$title' for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun addSubject(
        userId: String,
        subjectName: String,
        icon: String,
        colorHex: String
    ): Result<Unit> {
        return try {
            val existing = subjectDao.getSubjectByName(userId, subjectName)
            if (existing != null) {
                return Result.failure(Exception("La materia ya existe"))
            }
            val entity = SubjectEntity(
                userId = userId,
                name = subjectName,
                icon = icon,
                colorHex = colorHex,
                createdAt = Date()
            )
            subjectDao.insertSubject(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add subject '$subjectName' for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(userId: String, note: Note): Result<Unit> {
        return try {
            if (!note.imageUrl.isNullOrEmpty()) {
                try {
                    imageRepository.deleteImage(note.imageUrl)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete image ${note.imageUrl}", e)
                }
            }
            noteDao.deleteNoteById(userId, note.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete note '${note.id}'", e)
            Result.failure(e)
        }
    }

    override suspend fun updateNote(
        userId: String,
        note: Note,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            val existing = noteDao.getNoteById(userId, note.id)
            var imageUrl = note.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) {
                    val newUrl = uploadResult.getOrNull()
                    existing?.imageUrl?.takeIf { it.isNotBlank() && it != newUrl }?.let { oldUrl ->
                        try {
                            imageRepository.deleteImage(oldUrl)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to delete old image $oldUrl", e)
                        }
                    }
                    imageUrl = newUrl
                }
            } else if (imageUrl.isNullOrEmpty() && !existing?.imageUrl.isNullOrEmpty()) {
                try {
                    imageRepository.deleteImage(existing!!.imageUrl!!)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete removed image ${existing.imageUrl}", e)
                }
            }

            val entity = NoteEntity(
                id = note.id,
                userId = userId,
                subject = note.subject,
                title = note.title,
                content = note.content,
                imageUrl = imageUrl,
                tags = note.tags,
                timestamp = note.timestamp ?: existing?.timestamp ?: Date(),
                updatedAt = Date()
            )
            noteDao.updateNote(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update note '${note.id}'", e)
            Result.failure(e)
        }
    }

    override fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>> {
        return noteDao.getNotesBySubject(userId, subject).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteSubject(userId: String, subjectName: String): Result<Unit> {
        return try {
            // Step 1: Collect orphan image URLs before deleting records
            val notes = noteDao.getNotesBySubject(userId, subjectName).first()
            val evals = evaluationDao.getEvaluationsBySubject(userId, subjectName).first()

            val orphanImages = mutableListOf<String>()
            notes.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }.forEach { orphanImages.add(it) }
            evals.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }.forEach { orphanImages.add(it) }

            // Step 2: Atomic cascading deletion in Room transaction
            db.withTransaction {
                noteDao.deleteNotesBySubject(userId, subjectName)
                evaluationDao.deleteEvaluationsBySubject(userId, subjectName)
                scheduleDao.deleteSchedulesBySubject(userId, subjectName)
                subjectDao.deleteSubject(userId, subjectName)
            }

            // Step 3: Delete orphan images from disk after successful commit
            for (imageUrl in orphanImages) {
                try {
                    imageRepository.deleteImage(imageUrl)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete orphan image: $imageUrl", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete subject '$subjectName'", e)
            Result.failure(e)
        }
    }

    override suspend fun getNoteById(userId: String, subject: String, noteId: String): Result<Note> {
        return try {
            val entity = noteDao.getNoteById(userId, noteId)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get note '$noteId'", e)
            Result.failure(e)
        }
    }

    private fun NoteEntity.toDomain(): Note = Note(
        id = id,
        userId = userId,
        subject = subject,
        title = title,
        content = content,
        imageUrl = imageUrl,
        tags = tags,
        timestamp = timestamp,
        updatedAt = updatedAt
    )
}
