package com.example.notifast.presentation.feature.editevaluation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.EvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.notifast.domain.util.GradeConstants

@HiltViewModel
class EditEvaluationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val evalId: String = checkNotNull(savedStateHandle["evalId"])
    private val _uiState = MutableStateFlow(EditEvaluationUiState())
    val uiState: StateFlow<EditEvaluationUiState> = _uiState.asStateFlow()

    init { loadEvaluation() }

    private fun loadEvaluation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val evaluations = evaluationRepository.getEvaluations(userId).first()
            val eval = evaluations.find { it.id == evalId } ?: return@launch
            _uiState.update {
                it.copy(
                    isLoading = false,
                    evalId = eval.id,
                    type = eval.type,
                    subjectName = eval.subjectName,
                    title = eval.title,
                    description = eval.description,
                    quickTips = eval.quickTips,
                    grade = eval.grade?.toString() ?: "",
                    weight = eval.weight?.toString() ?: "",
                    isPresented = eval.isPresented,
                    existingImageUrl = eval.imageUrl,
                    date = eval.date
                )
            }
        }
    }

    fun onEvent(event: EditEvaluationEvent) {
        when (event) {
            is EditEvaluationEvent.LoadEvaluation -> loadEvaluation()
            is EditEvaluationEvent.UpdateTitle -> _uiState.update { it.copy(title = event.value) }
            is EditEvaluationEvent.UpdateDescription -> _uiState.update { it.copy(description = event.value) }
            is EditEvaluationEvent.UpdateQuickTips -> _uiState.update { it.copy(quickTips = event.value) }
            is EditEvaluationEvent.UpdateGrade -> {
                val parsed = event.value.toDoubleOrNull()
                if (event.value.isEmpty() || (parsed != null && parsed in GradeConstants.MIN_GRADE..GradeConstants.MAX_GRADE)) {
                    _uiState.update { it.copy(grade = event.value) }
                }
            }
            is EditEvaluationEvent.UpdateWeight -> {
                val v = event.value
                if (v.isEmpty() || (v.toDoubleOrNull() != null && v.toDouble() >= 0.0)) {
                    _uiState.update { it.copy(weight = v) }
                }
            }
            is EditEvaluationEvent.TogglePresented -> _uiState.update { it.copy(isPresented = !it.isPresented) }
            is EditEvaluationEvent.Save -> save(event.date, event.imageUri)
        }
    }

    private fun save(date: java.util.Date, imageUri: android.net.Uri?) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val updatedEval = com.example.notifast.data.model.Evaluation(
                id = state.evalId,
                userId = userId,
                subjectName = state.subjectName,
                title = state.title,
                description = state.description,
                date = date,
                imageUrl = if (imageUri != null) null else state.existingImageUrl,
                type = state.type,
                quickTips = state.quickTips,
                grade = state.grade.toDoubleOrNull(),
                weight = state.weight.toDoubleOrNull(),
                isPresented = state.isPresented
            )
            evaluationRepository.updateEvaluation(userId, updatedEval, imageUri).onSuccess {
                _uiState.update { it.copy(isSaved = true, isSaving = false) }
            }.onFailure {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
