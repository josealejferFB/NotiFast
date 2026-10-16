package com.notifast.app.data.repository

import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.dao.TeacherDao
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.util.MarkdownToBlocksParser
import com.notifast.app.domain.util.NoteBlockCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LocalNoteRepository"
private val CHECKLIST_REGEX = Regex("""^[-*]\s*\[([ xX]?)]""", RegexOption.MULTILINE)

@Singleton
class LocalNoteRepositoryImpl @Inject constructor(
    private val db: NotiFastDatabase,
    private val noteDao: NoteDao,
    private val subjectDao: SubjectDao,
    private val imageRepository: ImageRepository,
    private val evaluationDao: EvaluationDao = db.evaluationDao(),
    private val scheduleDao: ScheduleDao = db.scheduleDao(),
    private val teacherDao: TeacherDao = db.teacherDao(),
    private val subjectCloudStateDao: com.notifast.app.data.local.dao.SubjectCloudStateDao = db.subjectCloudStateDao()
) : NoteRepository {

    override fun getNotes(userId: String): Flow<List<Note>> {
        return noteDao.getNotes(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun getSubjects(userId: String): Flow<List<SubjectData>> {
        return subjectDao.getSubjects(userId).map { list ->
            list.map {
                SubjectData(
                    id = it.id,
                    name = it.name,
                    icon = it.icon,
                    colorHex = it.colorHex,
                    cycle = it.cycle
                )
            }
        }
    }

    override suspend fun addNote(
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUri: Uri?,
        tags: List<String>
    ): Result<String> {
        return try {
            val subjectEntity = subjectDao.getSubjectByName(userId, subject)
                ?: return Result.failure(Exception("La materia no existe"))
            var imageUrl: String? = null
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isFailure) {
                    return Result.failure(uploadResult.exceptionOrNull() ?: Exception("No se pudo subir la imagen"))
                }
                imageUrl = uploadResult.getOrNull()
            }
            val parsedBlocks = MarkdownToBlocksParser.parse(content, imageUrl)
            val blocksJson = NoteBlockCodec.toJson(parsedBlocks)
            val noteId = UUID.randomUUID().toString()
            val now = Date()
            noteDao.insertNote(
                NoteEntity(
                    id = noteId,
                    userId = userId,
                    subjectId = subjectEntity.id,
                    subject = subject,
                    title = title,
                    content = content,
                    imageUrl = imageUrl,
                    tags = tags,
                    timestamp = now,
                    updatedAt = now,
                    blocksJson = blocksJson,
                    schemaVersion = 2
                )
            )
            Result.success(noteId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add note '$title' for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun addNoteWithBlocks(
        userId: String,
        subject: String,
        title: String,
        blocks: List<NoteBlock>,
        tags: List<String>
    ): Result<String> {
        return try {
            val subjectEntity = subjectDao.getSubjectByName(userId, subject)
                ?: return Result.failure(Exception("La materia no existe"))
            val noteId = UUID.randomUUID().toString()
            val now = Date()
            noteDao.insertNote(
                NoteEntity(
                    id = noteId,
                    userId = userId,
                    subjectId = subjectEntity.id,
                    subject = subject,
                    title = title,
                    content = NoteBlockCodec.blocksToMarkdown(blocks),
                    imageUrl = null,
                    tags = tags,
                    timestamp = now,
                    updatedAt = now,
                    blocksJson = NoteBlockCodec.toJson(blocks),
                    schemaVersion = 2
                )
            )
            Result.success(noteId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add note with blocks '$title' for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun addSubject(
        userId: String,
        subjectName: String,
        icon: String,
        colorHex: String,
        cycle: String
    ): Result<String> {
        return try {
            val existing = subjectDao.getSubjectByName(userId, subjectName)
            if (existing != null) {
                return Result.failure(Exception("La materia ya existe"))
            }
            val subjectId = UUID.randomUUID().toString()
            subjectDao.insertSubject(
                SubjectEntity(
                    userId = userId,
                    id = subjectId,
                    name = subjectName,
                    icon = icon,
                    colorHex = colorHex,
                    cycle = cycle,
                    createdAt = Date()
                )
            )
            Result.success(subjectId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add subject '$subjectName' for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateSubject(userId: String, subject: SubjectData): Result<Unit> {
        val oldName = if (subject.name.isNotBlank()) subject.name else {
            val existing = subjectDao.getSubjectById(userId, subject.id)
            existing?.name.orEmpty()
        }
        return updateSubject(userId, oldName, subject)
    }

    override suspend fun updateSubject(
        userId: String,
        oldName: String,
        newSubject: SubjectData
    ): Result<Unit> {
        return try {
            val existing = subjectDao.getSubjectByName(userId, oldName)
                ?: (if (newSubject.id.isNotBlank()) subjectDao.getSubjectById(userId, newSubject.id) else null)
                ?: return Result.failure(Exception("La materia no existe"))

            val isRenaming = !oldName.equals(newSubject.name, ignoreCase = false)
            if (isRenaming) {
                val clash = subjectDao.getSubjectByName(userId, newSubject.name)
                if (clash != null && clash.id != existing.id) {
                    return Result.failure(Exception("Ya existe una materia con ese nombre"))
                }
            }

            db.withTransaction {
                subjectDao.updateSubject(
                    existing.copy(
                        name = newSubject.name,
                        icon = newSubject.icon,
                        colorHex = newSubject.colorHex,
                        cycle = newSubject.cycle
                    )
                )

                if (isRenaming) {
                    noteDao.updateSubjectName(userId, oldName, newSubject.name)
                    evaluationDao.updateSubjectName(userId, oldName, newSubject.name)
                    scheduleDao.updateSubjectName(userId, oldName, newSubject.name)

                    val teachers = teacherDao.getTeachers(userId).first()
                    teachers.forEach { teacherEntity ->
                        if (teacherEntity.subjectNamesJson.contains("\"$oldName\"")) {
                            val updatedJson = teacherEntity.subjectNamesJson.replace("\"$oldName\"", "\"${newSubject.name}\"")
                            teacherDao.updateTeacher(teacherEntity.copy(subjectNamesJson = updatedJson))
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update subject '$oldName'", e)
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
                if (uploadResult.isFailure) {
                    return Result.failure(uploadResult.exceptionOrNull() ?: Exception("No se pudo subir la imagen"))
                }
                val newUrl = uploadResult.getOrNull()
                existing?.imageUrl?.takeIf { it.isNotBlank() && it != newUrl }?.let { oldUrl ->
                    try {
                        imageRepository.deleteImage(oldUrl)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete old image $oldUrl", e)
                    }
                }
                imageUrl = newUrl
            } else if (imageUrl.isNullOrEmpty() && !existing?.imageUrl.isNullOrEmpty()) {
                try {
                    imageRepository.deleteImage(existing!!.imageUrl!!)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete removed image ${existing.imageUrl}", e)
                }
            }

            val subjectId = if (note.subjectId.isNotBlank()) {
                note.subjectId
            } else {
                subjectDao.getSubjectByName(userId, note.subject)?.id ?: ""
            }

            val hasBlocks = note.blocks.isNotEmpty()
            val contentToSave = if (note.content.isNotBlank()) {
                note.content
            } else if (hasBlocks) {
                NoteBlockCodec.blocksToMarkdown(note.blocks)
            } else {
                ""
            }
            val blocksToSave = if (hasBlocks) {
                note.blocks
            } else if (contentToSave.isNotBlank()) {
                MarkdownToBlocksParser.parse(contentToSave, imageUrl)
            } else {
                emptyList()
            }
            val blocksJsonToSave = if (blocksToSave.isNotEmpty()) {
                NoteBlockCodec.toJson(blocksToSave)
            } else {
                existing?.blocksJson ?: "[]"
            }
            val schemaVersionToSave = if (blocksToSave.isNotEmpty()) 2 else (existing?.schemaVersion ?: 1)

            noteDao.updateNote(
                NoteEntity(
                    id = note.id,
                    userId = userId,
                    subjectId = subjectId,
                    subject = note.subject,
                    title = note.title,
                    content = contentToSave,
                    imageUrl = imageUrl,
                    tags = note.tags,
                    timestamp = note.timestamp ?: existing?.timestamp ?: Date(),
                    updatedAt = Date(),
                    blocksJson = blocksJsonToSave,
                    schemaVersion = schemaVersionToSave
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update note '${note.id}'", e)
            Result.failure(e)
        }
    }

    override fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>> {
        return noteDao.getNotesBySubject(userId, subject).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteSubject(userId: String, subjectName: String): Result<Unit> {
        return try {
            val subject = subjectDao.getSubjectByName(userId, subjectName)
            val subjectId = subject?.id ?: ""

            val notes = if (subjectId.isNotBlank()) {
                noteDao.getNotesBySubjectId(userId, subjectId).first()
            } else {
                noteDao.getNotesBySubject(userId, subjectName).first()
            }
            val evals = if (subjectId.isNotBlank()) {
                evaluationDao.getEvaluationsBySubjectId(userId, subjectId).first()
            } else {
                evaluationDao.getEvaluationsBySubject(userId, subjectName).first()
            }

            val orphanImages = mutableListOf<String>()
            notes.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }.forEach { orphanImages.add(it) }
            evals.mapNotNull { it.imageUrl }.filter { it.isNotBlank() }.forEach { orphanImages.add(it) }

            db.withTransaction {
                if (subjectId.isNotBlank()) {
                    noteDao.deleteNotesBySubjectId(userId, subjectId)
                    evaluationDao.deleteEvaluationsBySubjectId(userId, subjectId)
                    scheduleDao.deleteSchedulesBySubjectId(userId, subjectId)
                    subjectDao.deleteSubjectById(userId, subjectId)
                    subjectCloudStateDao.deleteBySubject(subjectId)
                } else {
                    // Legacy rows without subjectId still cascade by name (case-safe)
                    noteDao.deleteNotesBySubject(userId, subjectName)
                    evaluationDao.deleteEvaluationsBySubject(userId, subjectName)
                    scheduleDao.deleteSchedulesBySubject(userId, subjectName)
                    subjectDao.deleteSubject(userId, subjectName)
                }
            }

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

    private fun NoteEntity.toDomain(): Note {
        val blocks = if (schemaVersion >= 2) {
            NoteBlockCodec.fromJson(blocksJson)
        } else {
            MarkdownToBlocksParser.parse(content, imageUrl)
        }
        val resolvedContent = if (schemaVersion >= 2 && blocks.isNotEmpty() && contentNeedsMarkdownRestore(content, blocks)) {
            NoteBlockCodec.blocksToMarkdown(blocks)
        } else {
            content
        }
        return Note(
            id = id,
            userId = userId,
            subjectId = subjectId,
            subject = subject,
            title = title,
            content = resolvedContent,
            imageUrl = imageUrl,
            tags = tags,
            timestamp = timestamp,
            updatedAt = updatedAt,
            blocks = blocks
        )
    }

    private fun contentNeedsMarkdownRestore(content: String, blocks: List<NoteBlock>): Boolean {
        if (content.isBlank()) return true
        val hasChecklistInBlocks = blocks.any { it is NoteBlock.Checklist }
        if (hasChecklistInBlocks && !CHECKLIST_REGEX.containsMatchIn(content)) {
            return true
        }
        return false
    }
}
