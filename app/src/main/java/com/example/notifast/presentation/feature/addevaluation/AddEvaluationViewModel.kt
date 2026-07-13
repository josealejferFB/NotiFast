package com.example.notifast.presentation.feature.addevaluation

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
                // Allow empty string or valid non-negative number
                val v = event.value
                if (v.isEmpty() || (v.toDoubleOrNull() != null && v.toDouble() >= 0.0)) {
                    _uiState.update { it.copy(weight = v) }
                }
            }
            is AddEvaluationEvent.SelectType -> _uiState.update { it.copy(selectedType = event.type) }
            is AddEvaluationEvent.Save -> save(event.date, event.imageUri)
        }
    }

    private fun save(date: java.util.Date, imageUri: android.net.Uri?) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val evaluation = Evaluation(
                userId = userId,
                subjectName = state.selectedSubject,
                title = state.title,
                description = state.description,
                date = date,
                quickTips = state.quickTips,
                type = state.selectedType.name,
                weight = if (state.selectedType.showWeight) state.weight.toDoubleOrNull() else null
            )
            evaluationRepository.addEvaluation(userId, evaluation, imageUri).onSuccess {
                _uiState.update { it.copy(isSaved = true, isSaving = false) }
            }.onFailure {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
