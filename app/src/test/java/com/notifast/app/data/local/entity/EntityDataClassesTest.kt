package com.notifast.app.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class EntityDataClassesTest {

    @Test
    fun userEntityCreationAndSecondaryConstructor() {
        val user1 = UserEntity(
            uid = "u1",
            email = "user@test.com",
            firstName = "Carlos",
            lastName = "Perez",
            profilePictureUrl = null,
            createdAt = Date(1000L),
            updatedAt = Date(2000L)
        )
        assertEquals("u1", user1.uid)
        assertEquals("user@test.com", user1.email)
        assertEquals("Carlos", user1.firstName)
        assertEquals("Perez", user1.lastName)
        assertNull(user1.profilePictureUrl)
        assertEquals(1000L, user1.createdAt?.time)
        assertEquals(2000L, user1.updatedAt?.time)

        val user2 = UserEntity("u2", "user2@test.com", "Ana", "Gomez", "https://img.com/pfp.jpg", 3000L, 4000L)
        assertEquals("u2", user2.uid)
        assertEquals("https://img.com/pfp.jpg", user2.profilePictureUrl)
        assertEquals(3000L, user2.createdAt?.time)
        assertEquals(4000L, user2.updatedAt?.time)
    }

    @Test
    fun subjectEntityCreationAndCollation() {
        val subject1 = SubjectEntity(
            userId = "u1",
            name = "Matemáticas",
            icon = "📐",
            colorHex = "#BF092F",
            createdAt = Date(1000L)
        )
        assertEquals("u1", subject1.userId)
        assertEquals("Matemáticas", subject1.name)
        assertEquals("📐", subject1.icon)
        assertEquals("#BF092F", subject1.colorHex)
        assertEquals(1000L, subject1.createdAt?.time)

        val subject2 = SubjectEntity("u1", "Física", "⚡", "#3B9797", 5000L)
        assertEquals("Física", subject2.name)
        assertEquals(5000L, subject2.createdAt?.time)
    }

    @Test
    fun noteEntityCreationAndSecondaryConstructor() {
        val note1 = NoteEntity(
            id = "n1",
            userId = "u1",
            subject = "Matemáticas",
            title = "Álgebra Lineal",
            content = "Matrices y determinantes",
            imageUrl = "file:///img1.jpg",
            tags = listOf("Examen", "Fórmula"),
            timestamp = Date(1000L),
            updatedAt = Date(1500L)
        )
        assertEquals("n1", note1.id)
        assertEquals("Matemáticas", note1.subject)
        assertEquals(2, note1.tags.size)

        val note2 = NoteEntity(
            "n2",
            "u1",
            "Física",
            "Cinemática",
            "MRU y MRUA",
            null,
            listOf("Teoría"),
            2000L,
            2500L
        )
        assertEquals("n2", note2.id)
        assertEquals(2000L, note2.timestamp?.time)
        assertEquals(2500L, note2.updatedAt?.time)
    }

    @Test
    fun evaluationEntityCreationAndDefaults() {
        val eval1 = EvaluationEntity(
            id = "e1",
            userId = "u1",
            subjectName = "Matemáticas",
            title = "Parcial 1",
            description = "Capítulos 1 a 3",
            date = Date(5000L),
            noteIds = listOf("n1"),
            imageUrl = null,
            quickTips = "Repasar matrices",
            type = "EXAM",
            grade = 18.5,
            weight = 0.3,
            isPresented = true,
            createdAt = Date(1000L),
            updatedAt = Date(2000L)
        )
        assertEquals("e1", eval1.id)
        assertEquals("Matemáticas", eval1.subjectName)
        assertEquals(18.5, eval1.grade ?: 0.0, 0.001)
        assertEquals(0.3, eval1.weight ?: 0.0, 0.001)
        assertTrue(eval1.isPresented)

        val eval2 = EvaluationEntity(
            "e2",
            "u1",
            "Física",
            "Quiz",
            "Lab",
            6000L,
            emptyList(),
            null,
            "",
            "QUIZ",
            20.0,
            0.1,
            false,
            1000L,
            1500L
        )
        assertEquals("e2", eval2.id)
        assertEquals(6000L, eval2.date?.time)
    }

    @Test
    fun scheduleEntityCreationAndDefaults() {
        val sched1 = ScheduleEntity(
            id = "s1",
            userId = "u1",
            subjectName = "Matemáticas",
            modality = "presencial",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "10:00",
            classroom = "Aula 302",
            teacher = "Prof. Euler",
            meetingUrl = null,
            color = 0xFFBF092FL,
            createdAt = Date(1000L),
            updatedAt = Date(2000L)
        )
        assertEquals("s1", sched1.id)
        assertEquals("Aula 302", sched1.classroom)
        assertEquals("Prof. Euler", sched1.teacher)

        val sched2 = ScheduleEntity(
            "s2",
            "u1",
            "Física",
            "virtual",
            2,
            "10:00",
            "12:00",
            "",
            "Prof. Newton",
            "https://meet.google.com/abc-defg-hij",
            0xFF3B9797L,
            1000L,
            1000L
        )
        assertEquals("s2", sched2.id)
        assertEquals("virtual", sched2.modality)
        assertEquals("https://meet.google.com/abc-defg-hij", sched2.meetingUrl)
    }
}
