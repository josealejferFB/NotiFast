package com.example.notifast.presentation.feature.subjectdetail

import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.Note

data class SubjectDetailUiState(
    val isLoading: Boolean = true,
    val subjectName: String = "",
    val subjectIcon: String = "📚",
    val subjectColorHex: String = "",
    val notes: List<Note> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val averageGrade: Double? = null,
    val completedEvalCount: Int = 0,
    val pendingEvalCount: Int = 0,
    val upcomingEvaluations: List<Evaluation> = emptyList(),
    val progressPercent: Float = 0f
)

sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
    data class SaveGrade(val evaluationId: String, val grade: Double, val isPresented: Boolean) : SubjectDetailEvent
    data class DeleteEvaluation(val evaluationId: String) : SubjectDetailEvent
}
