package com.notifast.app.data.model

import java.util.Date

data class Note(
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
)
