package com.notifast.app.util

import com.google.firebase.Timestamp
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.User
import java.util.Date

/**
 * Factory for test fixtures and mock data models.
 */
object TestDataFactory {

    fun createNote(
        id: String = "note-1",
        userId: String = "user-1",
        subject: String = "Matemáticas",
        title: String = "Álgebra Lineal",
        content: String = "Matrices y determinantes",
        imageUrl: String? = null,
        timestamp: Timestamp = Timestamp(Date()),
        updatedAt: Timestamp = Timestamp(Date())
    ) = Note(
        id = id,
        userId = userId,
        subject = subject,
        title = title,
        content = content,
        imageUrl = imageUrl,
        timestamp = timestamp,
        updatedAt = updatedAt
    )

    fun createSubject(
        name: String = "Matemáticas",
        icon: String = "📐",
        colorHex: String = "#BF092F"
    ) = SubjectData(
        name = name,
        icon = icon,
        colorHex = colorHex
    )

    fun createEvaluation(
        id: String = "eval-1",
        userId: String = "user-1",
        subjectName: String = "Matemáticas",
        title: String = "Examen Parcial",
        description: String = "Temas 1 al 4",
        quickTips: String = "Repasar fórmulas",
        type: String = "EXAM",
        date: Timestamp = Timestamp(Date()),
        grade: Double? = 9.5,
        weight: Double? = 30.0,
        isPresented: Boolean = true,
        imageUrl: String? = null
    ) = Evaluation(
        id = id,
        userId = userId,
        subjectName = subjectName,
        title = title,
        description = description,
        quickTips = quickTips,
        type = type,
        date = date,
        grade = grade,
        weight = weight,
        isPresented = isPresented,
        imageUrl = imageUrl
    )

    fun createSchedule(
        id: String = "sched-1",
        userId: String = "user-1",
        subjectName: String = "Matemáticas",
        dayOfWeek: Int = 2, // Lunes
        startTime: String = "08:00",
        endTime: String = "10:00",
        classroom: String = "Aula 101"
    ) = Schedule(
        id = id,
        userId = userId,
        subjectName = subjectName,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        classroom = classroom
    )

    fun createUser(
        uid: String = "user-1",
        email: String = "test@example.com",
        firstName: String = "Juan",
        lastName: String = "Pérez",
        profileImageUrl: String? = null
    ) = User(
        uid = uid,
        email = email,
        firstName = firstName,
        lastName = lastName,
        profileImageUrl = profileImageUrl
    )
}
