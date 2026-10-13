package com.notifast.app.data.repository

import android.util.Log
import com.notifast.app.data.local.dao.TeacherDao
import com.notifast.app.data.local.entity.TeacherEntity
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LocalTeacherRepository"

@Singleton
class LocalTeacherRepositoryImpl @Inject constructor(
    private val teacherDao: TeacherDao
) : TeacherRepository {

    override fun getTeachers(userId: String): Flow<List<Teacher>> {
        return teacherDao.getTeachers(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTeacherById(userId: String, id: String): Flow<Teacher?> {
        return teacherDao.getTeacherById(userId, id).map { it?.toDomain() }
    }

    override fun getTeachersBySubject(userId: String, subjectName: String): Flow<List<Teacher>> {
        val cleanSubject = subjectName.trim()
        return getTeachers(userId).map { list ->
            list.filter { teacher ->
                teacher.subjectNames.any { it.equals(cleanSubject, ignoreCase = true) }
            }
        }
    }

    override suspend fun saveTeacher(teacher: Teacher): Result<Unit> {
        return try {
            val teacherId = if (teacher.id.isNotBlank()) teacher.id else UUID.randomUUID().toString()
            val now = Date()
            val entity = TeacherEntity(
                userId = teacher.userId,
                id = teacherId,
                firstName = teacher.firstName.trim(),
                lastName = teacher.lastName.trim(),
                phoneNumber = teacher.phoneNumber.trim(),
                email = teacher.email.trim(),
                subjectNamesJson = subjectsToJson(teacher.subjectNames),
                notes = teacher.notes.trim(),
                createdAt = teacher.createdAt ?: now,
                updatedAt = now
            )
            teacherDao.insertTeacher(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save teacher '${teacher.fullName}'", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTeacher(userId: String, teacherId: String): Result<Unit> {
        return try {
            teacherDao.deleteTeacher(userId, teacherId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete teacher '$teacherId'", e)
            Result.failure(e)
        }
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
            it.trim().removeSurrounding("\"").replace("\\\"", "\"")
        }.filter { it.isNotBlank() }
    }
}
