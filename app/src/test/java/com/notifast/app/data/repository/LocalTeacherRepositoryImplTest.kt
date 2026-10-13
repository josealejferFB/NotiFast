package com.notifast.app.data.repository

import com.notifast.app.data.local.dao.TeacherDao
import com.notifast.app.data.local.entity.TeacherEntity
import com.notifast.app.data.model.Teacher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class LocalTeacherRepositoryImplTest {

    private val teacherDao: TeacherDao = mockk(relaxed = true)
    private lateinit var repository: LocalTeacherRepositoryImpl

    @Before
    fun setUp() {
        repository = LocalTeacherRepositoryImpl(teacherDao)
    }

    @Test
    fun getTeachers_maps_entity_to_domain() = runTest {
        val entity = TeacherEntity(
            userId = "u1",
            id = "t1",
            firstName = "Carlos",
            lastName = "Mendoza",
            phoneNumber = "+584121234567",
            email = "carlos@universidad.edu",
            subjectNamesJson = "[\"Cálculo I\", \"Física II\"]",
            notes = "Cubículo 4B"
        )
        coEvery { teacherDao.getTeachers("u1") } returns flowOf(listOf(entity))

        val result = repository.getTeachers("u1").first()
        assertEquals(1, result.size)
        val teacher = result.first()
        assertEquals("Carlos", teacher.firstName)
        assertEquals("Mendoza", teacher.lastName)
        assertEquals("Carlos Mendoza", teacher.fullName)
        assertEquals(listOf("Cálculo I", "Física II"), teacher.subjectNames)
        assertEquals("+584121234567", teacher.phoneNumber)
        assertTrue(teacher.hasPhoneNumber)
    }

    @Test
    fun getTeachersBySubject_filters_correctly() = runTest {
        val t1 = TeacherEntity(
            userId = "u1",
            id = "t1",
            firstName = "Carlos",
            subjectNamesJson = "[\"Cálculo I\"]"
        )
        val t2 = TeacherEntity(
            userId = "u1",
            id = "t2",
            firstName = "Elena",
            subjectNamesJson = "[\"Química\", \"Física\"]"
        )
        coEvery { teacherDao.getTeachers("u1") } returns flowOf(listOf(t1, t2))

        val result = repository.getTeachersBySubject("u1", "Cálculo I").first()
        assertEquals(1, result.size)
        assertEquals("Carlos", result.first().firstName)

        val emptyResult = repository.getTeachersBySubject("u1", "Historia").first()
        assertTrue(emptyResult.isEmpty())
    }

    @Test
    fun saveTeacher_generates_id_if_blank_and_serializes_subjects() = runTest {
        val teacher = Teacher(
            id = "",
            userId = "u1",
            firstName = "María",
            lastName = "Gómez",
            phoneNumber = "+584149876543",
            subjectNames = listOf("Álgebra", "Geometría")
        )
        coEvery { teacherDao.insertTeacher(any()) } returns 1L

        val result = repository.saveTeacher(teacher)
        assertTrue(result.isSuccess)

        val slot = slot<TeacherEntity>()
        coVerify { teacherDao.insertTeacher(capture(slot)) }
        assertEquals("u1", slot.captured.userId)
        assertTrue(slot.captured.id.isNotBlank())
        assertEquals("María", slot.captured.firstName)
        assertTrue(slot.captured.subjectNamesJson.contains("Álgebra"))
        assertTrue(slot.captured.subjectNamesJson.contains("Geometría"))
    }

    @Test
    fun deleteTeacher_calls_dao() = runTest {
        coEvery { teacherDao.deleteTeacher("u1", "t1") } returns 1
        val result = repository.deleteTeacher("u1", "t1")
        assertTrue(result.isSuccess)
        coVerify { teacherDao.deleteTeacher("u1", "t1") }
    }
}
