package com.notifast.app.domain.repository

import com.notifast.app.data.model.Teacher
import kotlinx.coroutines.flow.Flow

interface TeacherRepository {
    fun getTeachers(userId: String): Flow<List<Teacher>>
    fun getTeacherById(userId: String, id: String): Flow<Teacher?>
    fun getTeachersBySubject(userId: String, subjectName: String): Flow<List<Teacher>>
    suspend fun saveTeacher(teacher: Teacher): Result<Unit>
    suspend fun deleteTeacher(userId: String, teacherId: String): Result<Unit>
}
