package com.notifast.app.presentation.feature.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
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
    private val authRepository: AuthRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase
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
                val evalsBySubject = evaluations.groupBy { it.subjectName }
                val globalAverage = calculateWeightedAverageUseCase(evaluations) ?: 0.0
                val subjectGrades = evalsBySubject.map { (subject, evals) ->
                    SubjectGradeInfo(
                        subject = subject,
                        evaluations = evals,
                        average = calculateWeightedAverageUseCase(evals) ?: 0.0
                    )
                }.sortedBy { it.subject }
                
                GradesUiState(
                    evaluationsBySubject = evalsBySubject,
                    subjects = subjects,
                    globalAverage = globalAverage,
                    subjectGrades = subjectGrades
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: GradesEvent) {
        when (event) {
            is GradesEvent.LoadData -> loadData()
            is GradesEvent.UpdateGrade -> updateGrade(event.evaluationId, event.grade, event.isPresented)
            is GradesEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun updateGrade(evaluationId: String, grade: Double?, isPresented: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val current = _uiState.value.evaluationsBySubject.values.flatten().find { it.id == evaluationId } ?: return@launch
            val updated = current.copy(grade = grade, isPresented = isPresented)
            evaluationRepository.updateEvaluation(userId, updated, null).onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al actualizar nota") }
            }
        }
    }
}
