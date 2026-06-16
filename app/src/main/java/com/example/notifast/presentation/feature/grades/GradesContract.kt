package com.example.notifast.presentation.feature.grades

import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.SubjectData

data class GradesUiState(
    val isLoading: Boolean = false,
    val evaluationsBySubject: Map<String, List<Evaluation>> = emptyMap(),
    val subjects: List<SubjectData> = emptyList()
)

sealed interface GradesEvent {
    data object LoadData : GradesEvent
    data class UpdateGrade(val evaluationId: String, val grade: Double, val isPresented: Boolean) : GradesEvent
}
