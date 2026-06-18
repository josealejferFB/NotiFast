package com.example.notifast.presentation.feature.calendar

import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.Note

data class CalendarUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val error: String? = null
)

sealed interface CalendarEvent {
    data object LoadData : CalendarEvent
    data class DeleteEvaluation(val evaluation: Evaluation) : CalendarEvent
    data object ClearError : CalendarEvent
}
