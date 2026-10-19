package com.notifast.app.data.local.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.dao.TeacherDao
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.ScheduleEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.local.entity.TeacherEntity
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.repository.SubjectBackupStore
import com.notifast.app.domain.util.NoteBlockCodec
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSubjectBackupStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val db: NotiFastDatabase,
    private val subjectDao: SubjectDao,
    private val scheduleDao: ScheduleDao,
    private val evaluationDao: EvaluationDao,
    private val noteDao: NoteDao,
    private val subjectCloudStateDao: SubjectCloudStateDao,
    private val teacherDao: TeacherDao = db.teacherDao()
) : SubjectBackupStore {

    override suspend fun readSnapshot(userId: String, subjectId: String): Result<SubjectSnapshot> {
        return try {
            val subject = subjectDao.getSubjectById(userId, subjectId)
                ?: return Result.failure(NoSuchElementException("Subject $subjectId not found for user $userId"))

            val schedules = scheduleDao.getSchedules(userId).first()
                .filter { it.subjectId == subjectId || (it.subjectId.isBlank() && it.subjectName.equals(subject.name, ignoreCase = true)) }
                .map { it.toDomain() }
            val evaluations = evaluationDao.getEvaluations(userId).first()
                .filter { it.subjectId == subjectId || (it.subjectId.isBlank() && it.subjectName.equals(subject.name, ignoreCase = true)) }
                .map { it.toDomain() }
            val notes = noteDao.getNotes(userId).first()
                .filter { it.subjectId == subjectId || (it.subjectId.isBlank() && it.subject.equals(subject.name, ignoreCase = true)) }
                .map { it.toDomain() }

            val imageFiles = mutableMapOf<String, File>()

            fun collectImage(pathOrUri: String?) {
                if (pathOrUri.isNullOrBlank() || imageFiles.containsKey(pathOrUri)) return
                val rawPath = if (pathOrUri.startsWith("file://")) pathOrUri.removePrefix("file://") else pathOrUri
                val file = File(rawPath)
                if (file.exists() && file.isFile) {
                    imageFiles[pathOrUri] = file
                }
            }

            evaluations.forEach { collectImage(it.imageUrl) }
            notes.forEach { note ->
                collectImage(note.imageUrl)
                note.blocks.forEach { block ->
                    if (block is NoteBlock.Image) {
                        collectImage(block.path)
                    }
                }
            }

            val allTeachers = teacherDao.getTeachers(userId).first().map { it.toDomain() }
            val cleanSubjectName = subject.name.trim()
            val scheduleTeacherNames = schedules.mapNotNull { s -> s.teacher.trim().takeIf { it.isNotEmpty() } }.toSet()

            val relevantTeachers = allTeachers.filter { t ->
                t.subjectNames.any { it.equals(cleanSubjectName, ignoreCase = true) } ||
                    scheduleTeacherNames.any { it.equals(t.fullName, ignoreCase = true) }
            }

            Result.success(
                SubjectSnapshot(
                    subject = subject.toDomain(),
                    schedules = schedules,
                    evaluations = evaluations,
                    notes = notes,
                    teachers = relevantTeachers,
                    imageFiles = imageFiles
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun listSubjectIds(userId: String): List<Pair<String, String>> {
        return subjectDao.getSubjects(userId).first().map { it.id to it.name }
    }

    override suspend fun insertSnapshot(
        userId: String,
        snapshot: SubjectSnapshot,
        newSubjectId: String?,
        newName: String?
    ): Result<String> {
        val finalSubjectId = newSubjectId ?: snapshot.subject.id
        val finalSubjectName = newName ?: snapshot.subject.name
        val isRenamedOrNew = newSubjectId != null || newName != null

        val copiedFiles = mutableListOf<File>()
        val pathMapping = mutableMapOf<String, String>()
        val imagesDir = File(context.filesDir, "images").apply { mkdirs() }

        return try {
            for ((key, file) in snapshot.imageFiles) {
                if (file.exists() && file.isFile && file.length() > 0) {
                    val dest = File(imagesDir, "${UUID.randomUUID()}.jpg")
                    file.copyTo(dest, overwrite = true)
                    copiedFiles.add(dest)
                    pathMapping[key] = Uri.fromFile(dest).toString()
                }
            }

            val rewrittenSchedules = snapshot.schedules.map { sched ->
                val schedId = if (isRenamedOrNew) UUID.randomUUID().toString() else sched.id
                sched.copy(
                    id = schedId,
                    userId = userId,
                    subjectId = finalSubjectId,
                    subjectName = finalSubjectName
                )
            }

            val rewrittenEvals = snapshot.evaluations.map { eval ->
                val newUrl = eval.imageUrl?.let { pathMapping[it] }
                val evalId = if (isRenamedOrNew) UUID.randomUUID().toString() else eval.id
                eval.copy(
                    id = evalId,
                    userId = userId,
                    subjectId = finalSubjectId,
                    subjectName = finalSubjectName,
                    imageUrl = newUrl
                )
            }

            val rewrittenNotes = snapshot.notes.map { note ->
                val newUrl = note.imageUrl?.let { pathMapping[it] }
                val newBlocks = note.blocks.mapNotNull { block ->
                    if (block is NoteBlock.Image) {
                        val mapped = pathMapping[block.path]
                        if (mapped != null) {
                            block.copy(path = mapped)
                        } else {
                            null
                        }
                    } else {
                        block
                    }
                }
                val noteId = if (isRenamedOrNew) UUID.randomUUID().toString() else note.id
                note.copy(
                    id = noteId,
                    userId = userId,
                    subjectId = finalSubjectId,
                    subject = finalSubjectName,
                    imageUrl = newUrl,
                    blocks = newBlocks
                )
            }

            db.withTransaction {
                val existingByName = subjectDao.getSubjectByName(userId, finalSubjectName)
                if (existingByName != null && existingByName.id != finalSubjectId) {
                    throw IllegalStateException("Subject with name '$finalSubjectName' already exists for user $userId")
                }

                val subjectEntity = SubjectEntity(
                    userId = userId,
                    id = finalSubjectId,
                    name = finalSubjectName,
                    icon = snapshot.subject.icon,
                    colorHex = snapshot.subject.colorHex,
                    cycle = snapshot.subject.cycle.orEmpty(),
                    section = snapshot.subject.section.orEmpty(),
                    createdAt = Date()
                )
                subjectDao.insertSubject(subjectEntity)

                if (rewrittenSchedules.isNotEmpty()) {
                    scheduleDao.insertSchedules(rewrittenSchedules.map { it.toEntity(userId) })
                }
                if (rewrittenEvals.isNotEmpty()) {
                    evaluationDao.insertEvaluations(rewrittenEvals.map { it.toEntity(userId) })
                }
                if (rewrittenNotes.isNotEmpty()) {
                    noteDao.insertNotes(rewrittenNotes.map { it.toEntity(userId) })
                }
                if (snapshot.teachers.isNotEmpty()) {
                    mergeTeachers(userId, snapshot.teachers, finalSubjectName)
                }
            }

            Result.success(finalSubjectId)
        } catch (e: Exception) {
            copiedFiles.forEach { it.delete() }
            Result.failure(e)
        }
    }

    override suspend fun replaceSnapshot(
        userId: String,
        oldSubjectId: String,
        snapshot: SubjectSnapshot
    ): Result<String> {
        val finalSubjectId = snapshot.subject.id
        val finalSubjectName = snapshot.subject.name

        val oldImageUrls = mutableListOf<String>()
        try {
            val oldEvals = evaluationDao.getEvaluationsBySubjectId(userId, oldSubjectId).first()
            val oldNotes = noteDao.getNotesBySubjectId(userId, oldSubjectId).first()
            oldEvals.forEach { it.imageUrl?.let { url -> oldImageUrls.add(url) } }
            oldNotes.forEach { note ->
                note.imageUrl?.let { url -> oldImageUrls.add(url) }
                if (note.schemaVersion >= 2) {
                    NoteBlockCodec.fromJson(note.blocksJson).forEach { b ->
                        if (b is NoteBlock.Image) oldImageUrls.add(b.path)
                    }
                }
            }
        } catch (_: Exception) {}

        val copiedFiles = mutableListOf<File>()
        val pathMapping = mutableMapOf<String, String>()
        val imagesDir = File(context.filesDir, "images").apply { mkdirs() }

        return try {
            for ((key, file) in snapshot.imageFiles) {
                if (file.exists() && file.isFile && file.length() > 0) {
                    val dest = File(imagesDir, "${UUID.randomUUID()}.jpg")
                    file.copyTo(dest, overwrite = true)
                    copiedFiles.add(dest)
                    pathMapping[key] = Uri.fromFile(dest).toString()
                }
            }

            val rewrittenSchedules = snapshot.schedules.map { sched ->
                sched.copy(
                    id = sched.id,
                    userId = userId,
                    subjectId = finalSubjectId,
                    subjectName = finalSubjectName
                )
            }

            val rewrittenEvals = snapshot.evaluations.map { eval ->
                val newUrl = eval.imageUrl?.let { pathMapping[it] }
                eval.copy(
                    id = eval.id,
                    userId = userId,
                    subjectId = finalSubjectId,
                    subjectName = finalSubjectName,
                    imageUrl = newUrl
                )
            }

            val rewrittenNotes = snapshot.notes.map { note ->
                val newUrl = note.imageUrl?.let { pathMapping[it] }
                val newBlocks = note.blocks.mapNotNull { block ->
                    if (block is NoteBlock.Image) {
                        val mapped = pathMapping[block.path]
                        if (mapped != null) {
                            block.copy(path = mapped)
                        } else {
                            null
                        }
                    } else {
                        block
                    }
                }
                note.copy(
                    id = note.id,
                    userId = userId,
                    subjectId = finalSubjectId,
                    subject = finalSubjectName,
                    imageUrl = newUrl,
                    blocks = newBlocks
                )
            }

            db.withTransaction {
                // Delete old records
                val oldSubject = subjectDao.getSubjectById(userId, oldSubjectId)
                if (oldSubject != null) {
                    scheduleDao.deleteSchedulesBySubject(userId, oldSubject.name)
                    evaluationDao.deleteEvaluationsBySubject(userId, oldSubject.name)
                    noteDao.deleteNotesBySubject(userId, oldSubject.name)
                }
                noteDao.deleteNotesBySubjectId(userId, oldSubjectId)
                evaluationDao.deleteEvaluationsBySubjectId(userId, oldSubjectId)
                scheduleDao.deleteSchedulesBySubjectId(userId, oldSubjectId)
                subjectDao.deleteSubjectById(userId, oldSubjectId)
                subjectCloudStateDao.deleteBySubject(oldSubjectId)

                // Insert new records
                val subjectEntity = SubjectEntity(
                    userId = userId,
                    id = finalSubjectId,
                    name = finalSubjectName,
                    icon = snapshot.subject.icon,
                    colorHex = snapshot.subject.colorHex,
                    cycle = snapshot.subject.cycle.orEmpty(),
                    section = snapshot.subject.section.orEmpty(),
                    createdAt = Date()
                )
                subjectDao.insertSubject(subjectEntity)

                if (rewrittenSchedules.isNotEmpty()) {
                    scheduleDao.insertSchedules(rewrittenSchedules.map { it.toEntity(userId) })
                }
                if (rewrittenEvals.isNotEmpty()) {
                    evaluationDao.insertEvaluations(rewrittenEvals.map { it.toEntity(userId) })
                }
                if (rewrittenNotes.isNotEmpty()) {
                    noteDao.insertNotes(rewrittenNotes.map { it.toEntity(userId) })
                }
                if (snapshot.teachers.isNotEmpty()) {
                    mergeTeachers(userId, snapshot.teachers, finalSubjectName)
                }
            }

            // Post-commit: delete old images
            oldImageUrls.forEach { url ->
                val rawPath = if (url.startsWith("file://")) url.removePrefix("file://") else url
                try {
                    val f = File(rawPath)
                    if (f.exists()) f.delete()
                } catch (_: Exception) {}
            }

            Result.success(finalSubjectId)
        } catch (e: Exception) {
            copiedFiles.forEach { it.delete() }
            Result.failure(e)
        }
    }

    private fun SubjectEntity.toDomain() = SubjectData(
        id = id,
        name = name,
        icon = icon,
        colorHex = colorHex,
        cycle = cycle,
        section = section
    )

    private fun ScheduleEntity.toDomain() = Schedule(
        id = id,
        userId = userId,
        subjectId = subjectId,
        subjectName = subjectName,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        classroom = classroom,
        teacher = teacher,
        meetingUrl = meetingUrl,
        modality = modality,
        color = color
    )

    private fun Schedule.toEntity(userId: String) = ScheduleEntity(
        id = id,
        userId = userId,
        subjectId = subjectId,
        subjectName = subjectName,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        classroom = classroom,
        teacher = teacher,
        meetingUrl = meetingUrl,
        modality = modality,
        color = color,
        createdAt = Date(),
        updatedAt = Date()
    )

    private fun EvaluationEntity.toDomain() = Evaluation(
        id = id,
        userId = userId,
        subjectId = subjectId,
        subjectName = subjectName,
        title = title,
        description = description,
        date = date,
        noteIds = noteIds,
        imageUrl = imageUrl,
        quickTips = quickTips,
        type = type,
        grade = grade,
        weight = weight,
        isPresented = isPresented,
        gradeScaleId = gradeScaleId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Evaluation.toEntity(userId: String) = EvaluationEntity(
        id = id,
        userId = userId,
        subjectId = subjectId,
        subjectName = subjectName,
        title = title,
        description = description,
        date = date,
        noteIds = noteIds,
        imageUrl = imageUrl,
        quickTips = quickTips,
        type = type,
        grade = grade,
        weight = weight,
        isPresented = isPresented,
        gradeScaleId = gradeScaleId,
        createdAt = createdAt ?: Date(),
        updatedAt = Date()
    )

    private fun NoteEntity.toDomain(): Note {
        val blocks = if (schemaVersion >= 2) {
            NoteBlockCodec.fromJson(blocksJson)
        } else {
            com.notifast.app.domain.util.MarkdownToBlocksParser.parse(content, imageUrl)
        }
        return Note(
            id = id,
            userId = userId,
            subjectId = subjectId,
            subject = subject,
            title = title,
            content = content,
            imageUrl = imageUrl,
            tags = tags,
            timestamp = timestamp,
            updatedAt = updatedAt,
            blocks = blocks
        )
    }

    private fun Note.toEntity(userId: String): NoteEntity {
        return NoteEntity(
            id = id,
            userId = userId,
            subjectId = subjectId,
            subject = subject,
            title = title,
            content = content,
            imageUrl = imageUrl,
            tags = tags,
            timestamp = timestamp ?: Date(),
            updatedAt = Date(),
            blocksJson = NoteBlockCodec.toJson(blocks),
            schemaVersion = 2
        )
    }

    private suspend fun mergeTeachers(
        userId: String,
        remoteTeachers: List<Teacher>,
        subjectName: String
    ) {
        val localEntities = teacherDao.getTeachers(userId).first()
        val localByKey = localEntities.associateBy { teacherKey(it.toDomain()) }

        for (remote in remoteTeachers) {
            val key = teacherKey(remote)
            val existing = localByKey[key]
            if (existing == null) {
                val newSubjectNames = (remote.subjectNames + subjectName)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                val newEntity = TeacherEntity(
                    userId = userId,
                    id = if (remote.id.isNotBlank()) remote.id else UUID.randomUUID().toString(),
                    firstName = remote.firstName.trim(),
                    lastName = remote.lastName.trim(),
                    phoneNumber = remote.phoneNumber.trim(),
                    email = remote.email.trim(),
                    subjectNamesJson = subjectsToJson(newSubjectNames),
                    notes = remote.notes.trim(),
                    createdAt = remote.createdAt ?: Date(),
                    updatedAt = Date()
                )
                teacherDao.insertTeacher(newEntity)
            } else {
                val existingSubjects = jsonToSubjects(existing.subjectNamesJson)
                val mergedSubjects = mutableListOf<String>()
                val seen = mutableSetOf<String>()
                for (name in existingSubjects + remote.subjectNames + subjectName) {
                    val trimmed = name.trim()
                    val lower = trimmed.lowercase()
                    if (lower.isNotBlank() && seen.add(lower)) {
                        mergedSubjects.add(trimmed)
                    }
                }
                val updated = existing.copy(
                    subjectNamesJson = subjectsToJson(mergedSubjects),
                    updatedAt = Date()
                )
                teacherDao.updateTeacher(updated)
            }
        }
    }

    private fun teacherKey(t: Teacher): String {
        val first = t.firstName.trim().lowercase()
        val last = t.lastName.trim().lowercase()
        val phone = t.formattedPhoneNumber
        val email = t.email.trim().lowercase()
        return if (phone.isNotBlank()) "phone:$phone"
        else if (email.isNotBlank()) "email:$email"
        else "name:$first $last"
    }

    private fun TeacherEntity.toDomain(): Teacher {
        return Teacher(
            id = id,
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,
            subjectNames = jsonToSubjects(subjectNamesJson),
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun subjectsToJson(subjects: List<String>): String {
        val clean = subjects.map { it.trim() }.filter { it.isNotEmpty() }
        val escaped = clean.map { "\"${it.replace("\"", "\\\"")}\"" }
        return "[${escaped.joinToString(",")}]"
    }

    private fun jsonToSubjects(json: String): List<String> {
        if (json.isBlank() || json == "[]") return emptyList()
        val trimmed = json.trim().removePrefix("[").removeSuffix("]").trim()
        if (trimmed.isEmpty()) return emptyList()
        return trimmed.split(",").map {
            it.trim().removePrefix("\"").removeSuffix("\"").replace("\\\"", "\"")
        }
    }
}
