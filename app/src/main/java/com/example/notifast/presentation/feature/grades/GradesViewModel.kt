package com.example.notifast.presentation.feature.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.SubjectData
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
class GradesViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            combine(
                evaluationRepository.getEvaluations(userId),
                noteRepository.getSubjects(userId)
            ) { evaluations, subjects ->
                GradesUiState(
                    evaluationsBySubject = evaluations.groupBy { it.subjectName },
                    subjects = subjects
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: GradesEvent) {
        when (event) {
            is GradesEvent.LoadData -> loadData()
            is GradesEvent.UpdateGrade -> updateGrade(event.evaluationId, event.grade, event.isPresented)
        }
    }

    private fun updateGrade(evaluationId: String, grade: Double, isPresented: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val current = _uiState.value.evaluationsBySubject.values.flatten().find { it.id == evaluationId } ?: return@launch
            val updated = current.copy(grade = grade, isPresented = isPresented)
            evaluationRepository.updateEvaluation(userId, updated, null)
        }
    }
}
