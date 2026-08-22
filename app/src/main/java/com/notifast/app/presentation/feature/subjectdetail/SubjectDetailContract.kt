package com.notifast.app.presentation.feature.subjectdetail

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.usecase.RequiredGradeResult

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
    val progressPercent: Float = 0f,
    val gradeScale: GradeScale = GradeScale.SCALE_0_10,
    val targetGrade: Double = 6.0,
    val simulationResult: RequiredGradeResult? = null,
    val error: String? = null
)

sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
    data class SaveGrade(val evaluationId: String, val grade: Double?, val isPresented: Boolean) : SubjectDetailEvent
    data class DeleteEvaluation(val evaluationId: String) : SubjectDetailEvent
    data class UpdateTargetGrade(val targetGrade: Double) : SubjectDetailEvent
    data class SetGradeScale(val scale: GradeScale) : SubjectDetailEvent
    data object ClearError : SubjectDetailEvent
}
