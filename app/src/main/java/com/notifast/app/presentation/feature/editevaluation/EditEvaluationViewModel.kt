package com.notifast.app.presentation.feature.editevaluation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.notifast.app.domain.util.GradeConstants

import com.notifast.app.domain.repository.NoteRepository

@HiltViewModel
class EditEvaluationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val evalId: String = checkNotNull(savedStateHandle["evalId"])
    private val _uiState = MutableStateFlow(EditEvaluationUiState())
    val uiState: StateFlow<EditEvaluationUiState> = _uiState.asStateFlow()

    init {
        loadEvaluation()
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSubjects = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val subjects = noteRepository.getSubjects(userId).first()
            _uiState.update { it.copy(subjects = subjects, isLoadingSubjects = false) }
        }
    }

    private fun loadEvaluation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val eval = evaluationRepository.getEvaluation(userId, evalId).first() ?: return@launch
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
            is EditEvaluationEvent.SelectSubject -> _uiState.update { it.copy(subjectName = event.subject) }
            is EditEvaluationEvent.UpdateTitle -> _uiState.update { it.copy(title = event.value) }
            is EditEvaluationEvent.UpdateDescription -> _uiState.update { it.copy(description = event.value) }
            is EditEvaluationEvent.UpdateQuickTips -> _uiState.update { it.copy(quickTips = event.value) }
            is EditEvaluationEvent.UpdateGrade -> {
                val v = event.value
                val parsed = v.toDoubleOrNull()
                if (v.isEmpty() || (parsed != null && parsed in GradeConstants.MIN_GRADE..GradeConstants.MAX_GRADE) || v.matches(Regex("^[0-9]{1,2}\\.$"))) {
                    _uiState.update { it.copy(grade = v) }
                }
            }
            is EditEvaluationEvent.UpdateWeight -> {
                val v = event.value
                val parsed = v.toDoubleOrNull()
                if (v.isEmpty() || (parsed != null && parsed in 0.0..100.0) || v.matches(Regex("^\\d{1,2}\\.$|^100\\.$"))) {
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
            val cleanTitle = state.title.trim()
            if (cleanTitle.isBlank() || state.subjectName.isBlank()) {
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }
            val updatedEval = com.notifast.app.data.model.Evaluation(
                id = state.evalId,
                userId = userId,
                subjectName = state.subjectName.trim(),
                title = cleanTitle,
                description = state.description.trim(),
                date = date,
                imageUrl = if (imageUri != null) null else state.existingImageUrl,
                type = state.type,
                quickTips = state.quickTips.trim(),
                grade = state.grade.toDoubleOrNull()?.coerceIn(GradeConstants.MIN_GRADE, GradeConstants.MAX_GRADE),
                weight = state.weight.toDoubleOrNull()?.coerceIn(0.0, 100.0),
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
