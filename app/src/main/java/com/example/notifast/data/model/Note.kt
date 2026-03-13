package com.example.notifast.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Note(
    val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)
