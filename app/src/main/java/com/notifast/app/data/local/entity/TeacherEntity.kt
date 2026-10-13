package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "teachers",
    primaryKeys = ["userId", "id"],
    indices = [
        Index(value = ["userId", "firstName", "lastName"])
    ]
)
data class TeacherEntity(
    val userId: String = "",
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val subjectNamesJson: String = "[]",
    val notes: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
