package com.notifast.app.presentation.feature.editevaluation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.util.ErrorSanitizer

@HiltViewModel
class EditEvaluationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val scheduleEvaluationAlertUseCase: com.notifast.app.domain.usecase.ScheduleEvaluationAlertUseCase,
    private val cancelAlertUseCase: com.notifast.app.domain.usecase.CancelAlertUseCase
) : ViewModel() {

    private var evalId: String? = savedStateHandle["evalId"]
    private val _uiState = MutableStateFlow(EditEvaluationUiState())
    val uiState: StateFlow<EditEvaluationUiState> = _uiState.asStateFlow()

    init {
        if (!evalId.isNullOrBlank()) {
            loadEvaluation()
        }
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSubjects = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoadingSubjects = false) }
                return@launch
            }
            val subjects = noteRepository.getSubjects(userId).first()
            _uiState.update { it.copy(subjects = subjects, isLoadingSubjects = false) }
        }
    }

    private fun loadEvaluation() {
        val currentEvalId = evalId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = ErrorSanitizer.MSG_AUTH_REQUIRED) }
                return@launch
            }
            val eval = evaluationRepository.getEvaluation(userId, currentEvalId).first()
            if (eval == null) {
                _uiState.update { it.copy(isLoading = false, error = ErrorSanitizer.MSG_EVALUATION_NOT_FOUND) }
                return@launch
            }
            val activeScaleId = userPrefsRepository.defaultGradeScaleFlow.first()
            val activeScale = com.notifast.app.domain.model.GradeScale.fromId(activeScaleId)
            val srcScale = com.notifast.app.domain.model.GradeScale.fromId(eval.gradeScaleId)
            val displayGrade = eval.grade?.let { com.notifast.app.domain.model.GradePolicy.convertGrade(it, srcScale, activeScale) }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    evalId = eval.id,
                    type = eval.type,
                    subjectName = eval.subjectName,
                    title = eval.title,
                    description = eval.description,
                    quickTips = eval.quickTips,
                    grade = displayGrade?.toString() ?: "",
                    weight = eval.weight?.toString() ?: "",
                    isPresented = eval.isPresented,
                    gradeScaleId = activeScaleId,
                    existingImageUrl = eval.imageUrl,
                    date = eval.date
                )
            }
        }
    }

    fun onEvent(event: EditEvaluationEvent) {
        when (event) {
            is EditEvaluationEvent.Init -> {
                this.evalId = event.evalId
                _uiState.update {
                    it.copy(
                        isSaved = false,
                        error = null,
                        title = "",
                        description = "",
                        quickTips = "",
                        grade = "",
                        weight = "",
                        isPresented = false,
                        existingImageUrl = null,
                        date = null,
                        subjectName = ""
                    )
                }
                loadEvaluation()
            }
            is EditEvaluationEvent.LoadEvaluation -> loadEvaluation()
            is EditEvaluationEvent.SelectSubject -> _uiState.update { it.copy(subjectName = event.subject) }
            is EditEvaluationEvent.UpdateTitle -> _uiState.update { it.copy(title = event.value) }
            is EditEvaluationEvent.UpdateDescription -> _uiState.update { it.copy(description = event.value) }
            is EditEvaluationEvent.UpdateQuickTips -> _uiState.update { it.copy(quickTips = event.value) }
            is EditEvaluationEvent.UpdateGrade -> {
                val v = event.value
                val scale = com.notifast.app.domain.model.GradeScale.fromId(_uiState.value.gradeScaleId)
                val parsed = v.toDoubleOrNull()
                if (v.isEmpty() || (parsed != null && com.notifast.app.domain.model.GradePolicy.validateGrade(parsed, scale)) || v.matches(Regex("^[0-9]{1,3}\\.$"))) {
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
            is EditEvaluationEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun save(date: java.util.Date, imageUri: android.net.Uri?) {
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.isSaved) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }
            val state = _uiState.value
            val cleanTitle = state.title.trim()
            val currentEvalId = evalId ?: state.evalId
            if (cleanTitle.isBlank() || state.subjectName.isBlank() || currentEvalId.isBlank()) {
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }
            val scale = com.notifast.app.domain.model.GradeScale.fromId(state.gradeScaleId)
            val matchedSubject = state.subjects.find { it.name.equals(state.subjectName.trim(), ignoreCase = true) }
            val updatedEval = com.notifast.app.data.model.Evaluation(
                id = currentEvalId,
                userId = userId,
                subjectId = matchedSubject?.id.orEmpty(),
                subjectName = state.subjectName.trim(),
                title = cleanTitle,
                description = state.description.trim(),
                date = date,
                imageUrl = if (imageUri != null) null else state.existingImageUrl,
                type = state.type,
                quickTips = state.quickTips.trim(),
                grade = state.grade.toDoubleOrNull()?.let { com.notifast.app.domain.model.GradePolicy.clampGrade(it, scale) },
                weight = state.weight.toDoubleOrNull()?.coerceIn(0.0, 100.0),
                isPresented = state.isPresented,
                gradeScaleId = state.gradeScaleId
            )
            evaluationRepository.updateEvaluation(userId, updatedEval, imageUri).onSuccess {
                if (updatedEval.grade != null || updatedEval.isPresented) {
                    cancelAlertUseCase.cancelEvaluationAlert(updatedEval.id)
                } else {
                    scheduleEvaluationAlertUseCase(updatedEval)
                }
                _uiState.update { it.copy(isSaved = true, isSaving = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = ErrorSanitizer.sanitize(e, "Error al guardar la evaluación")) }
            }
        }
    }
}
