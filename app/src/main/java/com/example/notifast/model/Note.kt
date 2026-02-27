package com.example.notifast.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Note(
    val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null, // <--- Nueva propiedad opcional
    @ServerTimestamp
    val timestamp: Date? = null
)
