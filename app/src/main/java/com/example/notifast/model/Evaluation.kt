package com.example.notifast.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Evaluation(
    val id: String = "",
    val userId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date? = null, // Fecha de la evaluación
    val noteIds: List<String> = emptyList(),
    val imageUrl: String? = null,
    val quickTips: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)
