package com.notifast.app.data.model

import java.util.Date

data class Evaluation(
    val id: String = "",
    val userId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date? = null,
    val noteIds: List<String> = emptyList(),
    val imageUrl: String? = null,
    val quickTips: String = "",
    val type: String = EvaluationType.EXAM.name,
    val grade: Double? = null,
    val weight: Double? = null,
    val isPresented: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
