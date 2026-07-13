package com.example.notifast.presentation.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.data.model.Evaluation
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.EvaluationRepository
import com.example.notifast.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            evaluationRepository.getEvaluations(userId).collect { evaluations ->
                val grouped = groupEvaluationsByDate(evaluations)
                _uiState.update { state -> 
                    state.copy(
                        evaluationsByDate = grouped,
                        dailyEvaluations = filterEvaluationsByDate(grouped, state.selectedDate.time),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun groupEvaluationsByDate(evaluations: List<Evaluation>): Map<String, List<Evaluation>> {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return evaluations.filter { it.date != null }.groupBy { format.format(it.date!!) }
    }

    private fun filterEvaluationsByDate(evaluationsByDate: Map<String, List<Evaluation>>, date: java.util.Date): List<Evaluation> {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return evaluationsByDate[format.format(date)] ?: emptyList()
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.LoadData -> loadData()
            is CalendarEvent.DeleteEvaluation -> deleteEvaluation(event.evaluation)
            is CalendarEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is CalendarEvent.SelectDate -> {
                _uiState.update { state ->
                    state.copy(
                        selectedDate = event.date,
                        dailyEvaluations = filterEvaluationsByDate(state.evaluationsByDate, event.date.time)
                    )
                }
            }
            is CalendarEvent.ChangeMonth -> {
                _uiState.update { state ->
                    val newMonth = state.currentMonth.clone() as java.util.Calendar
                    newMonth.add(java.util.Calendar.MONTH, event.amount)
                    state.copy(currentMonth = newMonth)
                }
            }
            is CalendarEvent.GoToToday -> {
                val today = java.util.Calendar.getInstance()
                _uiState.update { state ->
                    state.copy(
                        currentMonth = today.clone() as java.util.Calendar,
                        selectedDate = today.clone() as java.util.Calendar,
                        dailyEvaluations = filterEvaluationsByDate(state.evaluationsByDate, today.time)
                    )
                }
            }
        }
    }

    private fun deleteEvaluation(evaluation: Evaluation) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            evaluationRepository.deleteEvaluation(userId, evaluation).onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al eliminar") }
            }
        }
    }
}
