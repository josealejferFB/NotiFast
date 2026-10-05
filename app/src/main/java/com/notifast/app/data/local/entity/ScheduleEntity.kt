package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "schedules",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["dayOfWeek"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val subjectName: String = "",
    val modality: String = "presencial",
    val dayOfWeek: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val classroom: String = "",
    val teacher: String = "",
    val meetingUrl: String? = null,
    val color: Long = 0L,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    @Ignore
    constructor(
        id: String,
        userId: String,
        subjectName: String,
        modality: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        classroom: String,
        teacher: String,
        meetingUrl: String?,
        color: Long,
        createdAtMillis: Long?,
        updatedAtMillis: Long?
    ) : this(
        id = id,
        userId = userId,
        subjectName = subjectName,
        modality = modality,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        classroom = classroom,
        teacher = teacher,
        meetingUrl = meetingUrl,
        color = color,
        createdAt = createdAtMillis?.let { Date(it) },
        updatedAt = updatedAtMillis?.let { Date(it) }
    )
}
