package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "evaluations",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["subjectId"]),
        Index(value = ["subjectName"])
    ]
)
data class EvaluationEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date? = null,
    val noteIds: List<String> = emptyList(),
    val imageUrl: String? = null,
    val quickTips: String = "",
    val type: String = "EXAM",
    val grade: Double? = null,
    val weight: Double? = null,
    val isPresented: Boolean = false,
    val gradeScaleId: String = "0-10",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    constructor(
        id: String,
        userId: String,
        subjectName: String,
        title: String,
        description: String,
        dateMillis: Long?,
        noteIds: List<String>,
        imageUrl: String?,
        quickTips: String,
        type: String,
        grade: Double?,
        weight: Double?,
        isPresented: Boolean,
        createdAtMillis: Long?,
        updatedAtMillis: Long?
    ) : this(
        id = id,
        userId = userId,
        subjectId = "",
        subjectName = subjectName,
        title = title,
        description = description,
        date = dateMillis?.let { Date(it) },
        noteIds = noteIds,
        imageUrl = imageUrl,
        quickTips = quickTips,
        type = type,
        grade = grade,
        weight = weight,
        isPresented = isPresented,
        gradeScaleId = "0-10",
        createdAt = createdAtMillis?.let { Date(it) },
        updatedAt = updatedAtMillis?.let { Date(it) }
    )
}
