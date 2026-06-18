package com.example.notifast.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SubjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val subjectName: String = checkNotNull(savedStateHandle["subjectName"])
    private val _uiState = MutableStateFlow(SubjectDetailUiState(subjectName = subjectName))
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            combine(
                noteRepository.getNotesBySubject(userId, subjectName),
                evaluationRepository.getEvaluations(userId),
                noteRepository.getSubjects(userId)
            ) { notes, evaluations, subjects ->
                val subjectEvals = evaluations.filter { it.subjectName == subjectName }
                val subjectData = subjects.find { it.name == subjectName } ?: SubjectData(name = subjectName)

                val graded = subjectEvals.filter { it.grade != null && it.isPresented }
                val weightedSum = graded.sumOf { (it.grade ?: 0.0) * (it.weight ?: 1.0) }
                val totalWeight = graded.sumOf { it.weight ?: 1.0 }
                val avg = if (totalWeight > 0.0) weightedSum / totalWeight else null

                val upcoming = subjectEvals
                    .filter { !it.isPresented && it.date != null }
                    .sortedBy { it.date }
                    .take(5)

                val completed = subjectEvals.count { it.isPresented }
                val total = subjectEvals.size
                val progress = if (total > 0) completed.toFloat() / total else 0f

                SubjectDetailUiState(
                    isLoading = false,
                    subjectName = subjectName,
                    subjectIcon = subjectData.icon,
                    subjectColorHex = subjectData.colorHex,
                    notes = notes,
                    evaluations = subjectEvals,
                    averageGrade = avg?.let { kotlin.math.round(it * 10) / 10.0 },
                    completedEvalCount = completed,
                    pendingEvalCount = subjectEvals.count { !it.isPresented },
                    upcomingEvaluations = upcoming,
                    progressPercent = progress
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: SubjectDetailEvent) {
        when (event) {
            is SubjectDetailEvent.LoadData -> loadData()
            is SubjectDetailEvent.SaveGrade -> saveGrade(event.evaluationId, event.grade, event.isPresented)
            is SubjectDetailEvent.DeleteEvaluation -> deleteEvaluation(event.evaluationId)
        }
    }

    private fun saveGrade(evalId: String, grade: Double, isPresented: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val eval = _uiState.value.evaluations.find { it.id == evalId } ?: return@launch
            val updated = eval.copy(grade = grade, isPresented = isPresented)
            evaluationRepository.updateEvaluation(userId, updated, null)
        }
    }

    private fun deleteEvaluation(evalId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val eval = _uiState.value.evaluations.find { it.id == evalId } ?: return@launch
            evaluationRepository.deleteEvaluation(userId, eval)
        }
    }
}
