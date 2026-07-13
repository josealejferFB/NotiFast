package com.example.notifast.presentation.feature.calendar

import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.Note

import java.util.Calendar

data class CalendarUiState(
    val isLoading: Boolean = false,
    val evaluationsByDate: Map<String, List<Evaluation>> = emptyMap(),
    val dailyEvaluations: List<Evaluation> = emptyList(),
    val error: String? = null,
    val currentMonth: Calendar = Calendar.getInstance(),
    val selectedDate: Calendar = Calendar.getInstance()
)

sealed interface CalendarEvent {
    data object LoadData : CalendarEvent
    data class DeleteEvaluation(val evaluation: Evaluation) : CalendarEvent
    data object ClearError : CalendarEvent
    data class SelectDate(val date: Calendar) : CalendarEvent
    data class ChangeMonth(val amount: Int) : CalendarEvent
    data object GoToToday : CalendarEvent
}
