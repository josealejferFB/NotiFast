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
                _uiState.value = CalendarUiState(evaluations = evaluations)
            }
        }
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.LoadData -> loadData()
            is CalendarEvent.DeleteEvaluation -> deleteEvaluation(event.evaluation)
            is CalendarEvent.ClearError -> _uiState.update { it.copy(error = null) }
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
