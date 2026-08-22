package com.notifast.app.presentation.feature.addevaluation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEvaluationViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEvaluationUiState())
    val uiState: StateFlow<AddEvaluationUiState> = _uiState.asStateFlow()

    init { loadSubjects() }

    fun setInitialSubject(subject: String) {
        if (subject.isNotBlank()) _uiState.update { it.copy(selectedSubject = subject) }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val subjects = noteRepository.getSubjects(userId).first()
            _uiState.update { it.copy(subjects = subjects, isLoading = false) }
        }
    }

    fun onEvent(event: AddEvaluationEvent) {
        when (event) {
            is AddEvaluationEvent.LoadSubjects -> loadSubjects()
            is AddEvaluationEvent.SelectSubject -> _uiState.update { it.copy(selectedSubject = event.subject) }
            is AddEvaluationEvent.UpdateTitle -> _uiState.update { it.copy(title = event.value) }
            is AddEvaluationEvent.UpdateDescription -> _uiState.update { it.copy(description = event.value) }
            is AddEvaluationEvent.UpdateQuickTips -> _uiState.update { it.copy(quickTips = event.value) }
            is AddEvaluationEvent.UpdateWeight -> {
                val v = event.value
                val parsed = v.toDoubleOrNull()
                if (v.isEmpty() || (parsed != null && parsed in 0.0..100.0) || v.matches(Regex("^\\d{1,2}\\.$|^100\\.$"))) {
                    _uiState.update { it.copy(weight = v) }
                }
            }
            is AddEvaluationEvent.SelectType -> _uiState.update { it.copy(selectedType = event.type) }
            is AddEvaluationEvent.Save -> save(event.date, event.imageUri)
            is AddEvaluationEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun save(date: java.util.Date, imageUri: android.net.Uri?) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }
            val state = _uiState.value
            val cleanTitle = state.title.trim()
            if (cleanTitle.isBlank() || state.selectedSubject.isBlank()) {
                _uiState.update { it.copy(isSaving = false, error = "Materia y título son requeridos") }
                return@launch
            }
            val evaluation = Evaluation(
                userId = userId,
                subjectName = state.selectedSubject.trim(),
                title = cleanTitle,
                description = state.description.trim(),
                date = date,
                quickTips = state.quickTips.trim(),
                type = state.selectedType.name,
                weight = if (state.selectedType.showWeight) state.weight.toDoubleOrNull()?.coerceIn(0.0, 100.0) else null
            )
            evaluationRepository.addEvaluation(userId, evaluation, imageUri).onSuccess {
                _uiState.update { it.copy(isSaved = true, isSaving = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error al guardar la evaluación") }
            }
        }
    }
}
