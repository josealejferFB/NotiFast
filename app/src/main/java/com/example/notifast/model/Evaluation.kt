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
    val grade: Double? = null, // Calificación obtenida (0-100)
    val weight: Double? = null, // Peso o porcentaje de la evaluación (0-100)
    val isPresented: Boolean = false, // Si ya se realizó/presentó
    @ServerTimestamp
    val createdAt: Date? = null
)
