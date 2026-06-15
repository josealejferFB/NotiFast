package com.example.notifast.presentation.feature.subjectdetail

import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.Note

data class SubjectDetailUiState(
    val subjectName: String = "",
    val notes: List<Note> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
}
