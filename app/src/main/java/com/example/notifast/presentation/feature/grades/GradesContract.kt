package com.example.notifast.presentation.feature.grades

import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.SubjectData

data class SubjectGradeInfo(
    val subject: String,
    val evaluations: List<Evaluation>,
    val average: Double
)

data class GradesUiState(
    val isLoading: Boolean = false,
    val evaluationsBySubject: Map<String, List<Evaluation>> = emptyMap(),
    val subjects: List<SubjectData> = emptyList(),
    val globalAverage: Double = 0.0,
    val subjectGrades: List<SubjectGradeInfo> = emptyList()
)

sealed interface GradesEvent {
    data object LoadData : GradesEvent
    data class UpdateGrade(val evaluationId: String, val grade: Double, val isPresented: Boolean) : GradesEvent
}
