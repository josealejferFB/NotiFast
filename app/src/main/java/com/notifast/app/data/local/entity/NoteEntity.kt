package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["subjectId"])
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subject: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val timestamp: Date? = null,
    val updatedAt: Date? = null
) {
    constructor(
        id: String,
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUrl: String?,
        tags: List<String>,
        timestampMillis: Long?,
        updatedAtMillis: Long?
    ) : this(
        id = id,
        userId = userId,
        subjectId = "",
        subject = subject,
        title = title,
        content = content,
        imageUrl = imageUrl,
        tags = tags,
        timestamp = timestampMillis?.let { Date(it) },
        updatedAt = updatedAtMillis?.let { Date(it) }
    )
}
