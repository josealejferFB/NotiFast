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

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init { loadSubjects() }

    fun setInitialSubject(subject: String) {
        if (subject.isNotBlank()) _uiState.update { it.copy(selectedSubject = subject) }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            noteRepository.getSubjects(userId).collect { subjects ->
                _uiState.update { it.copy(subjects = subjects, isLoading = false) }
            }
        }
    }

    fun onEvent(event: AddEvaluationEvent) {
        when (event) {
            is AddEvaluationEvent.LoadSubjects -> loadSubjects()
            is AddEvaluationEvent.SelectSubject -> _uiState.update { it.copy(selectedSubject = event.subject) }
            is AddEvaluationEvent.UpdateTitle -> _uiState.update { it.copy(title = event.value) }
            is AddEvaluationEvent.UpdateDescription -> _uiState.update { it.copy(description = event.value) }
            is AddEvaluationEvent.UpdateQuickTips -> _uiState.update { it.copy(quickTips = event.value) }
            is AddEvaluationEvent.Save -> save(event.date, event.imageUri)
        }
    }

    private fun save(date: java.util.Date, imageUri: android.net.Uri?) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true) }
            val evaluation = Evaluation(
                userId = userId,
                subjectName = _uiState.value.selectedSubject,
                title = _uiState.value.title,
                description = _uiState.value.description,
                date = date,
                quickTips = _uiState.value.quickTips
            )
            evaluationRepository.addEvaluation(userId, evaluation, imageUri).onSuccess {
                _saved.value = true
            }.onFailure {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
