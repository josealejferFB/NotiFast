package com.notifast.app.presentation.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    private val evaluationRepository: EvaluationRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(NoteFilter.RECIENTES)
    private val _searchQuery = MutableStateFlow("")

    init {
        loadNotes()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(uid).onSuccess { user ->
                _uiState.update { it.copy(
                    userFirstName = user.firstName,
                    userLastName = user.lastName,
                    userEmail = user.email,
                    userProfilePictureUrl = user.profilePictureUrl
                ) }
            }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            combine(
                noteRepository.getNotes(userId),
                noteRepository.getSubjects(userId),
                evaluationRepository.getEvaluations(userId),
                _currentFilter,
                _searchQuery
            ) { notes, subjects, evaluations, filter, query ->
                val filtered = filterNotes(notes, filter, query)
                val subjectInfos = computeSubjectInfos(notes, subjects, evaluations)
                val subjectNames = subjects.map { it.name }
                _uiState.value to NotesUiState(
                    notes = filtered,
                    rawNotes = notes,
                    subjects = subjectNames,
                    subjectInfos = subjectInfos,
                    currentFilter = filter,
                    searchQuery = query
                )
            }.catch { e ->
                val current = _uiState.value
                emit(current to current.copy(error = e.message ?: "Error al cargar notas", isLoading = false))
            }.collect { (_, newState) ->
                _uiState.update { it.copy(
                    notes = newState.notes,
                    rawNotes = newState.rawNotes,
                    subjects = newState.subjects,
                    subjectInfos = newState.subjectInfos,
                    currentFilter = newState.currentFilter,
                    searchQuery = newState.searchQuery,
                    isLoading = false,
                    error = newState.error
                ) }
            }
        }
    }

    private fun computeSubjectInfos(
        notes: List<Note>,
        subjects: List<SubjectData>,
        evaluations: List<Evaluation>
    ): List<SubjectInfo> {
        return subjects.map { subject ->
            val subjectNotes = notes.filter { it.subject == subject.name }
            val subjectEvals = evaluations.filter { it.subjectName == subject.name }
            val avg = calculateWeightedAverageUseCase(subjectEvals)
            val upcoming = subjectEvals
                .filter { !it.isPresented && it.date != null }
                .sortedBy { it.date }
                .take(3)

            SubjectInfo(
                name = subject.name,
                icon = subject.icon,
                colorHex = subject.colorHex,
                notes = subjectNotes,
                evaluations = subjectEvals,
                averageGrade = avg?.let { kotlin.math.round(it * 10) / 10.0 },
                completedEvalCount = subjectEvals.count { it.isPresented },
                pendingEvalCount = subjectEvals.count { !it.isPresented },
                upcomingEvaluations = upcoming
            )
        }
    }

    private fun filterNotes(notes: List<Note>, filter: NoteFilter, query: String): Map<String, List<Note>> {
        var filtered = notes
        val cleanQuery = query.trim()
        if (cleanQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(cleanQuery, ignoreCase = true) ||
                it.content.contains(cleanQuery, ignoreCase = true) ||
                it.subject.contains(cleanQuery, ignoreCase = true)
            }
        }
        return when (filter) {
            NoteFilter.RECIENTES -> filtered.sortedByDescending { it.timestamp }.groupBy { it.subject }
            NoteFilter.ANTIGUAS -> filtered.sortedBy { it.timestamp ?: java.util.Date() }.groupBy { it.subject }
            NoteFilter.POR_MATERIA -> filtered.groupBy { it.subject }
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.LoadNotes -> loadNotes()
            is NotesEvent.SetFilter -> _currentFilter.value = event.filter
            is NotesEvent.Search -> _searchQuery.value = event.query
            is NotesEvent.ClearSyncMessage -> _uiState.update { it.copy(syncMessage = null) }
            is NotesEvent.DeleteNote -> deleteNote(event.note)
            is NotesEvent.ToggleSubject -> {
                val current = _uiState.value.expandedSubject
                _uiState.update { it.copy(expandedSubject = if (current == event.subjectName) null else event.subjectName) }
            }
            is NotesEvent.DeleteSubject -> deleteSubject(event.subjectName)
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(syncMessage = "Eliminando...") }
            noteRepository.deleteNote(userId, note).fold(
                onSuccess = { _uiState.update { it.copy(syncMessage = null) } },
                onFailure = { _uiState.update { it.copy(syncMessage = "Error al eliminar") } }
            )
        }
    }

    private fun deleteSubject(subjectName: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(syncMessage = "Eliminando materia...") }
            noteRepository.deleteSubject(userId, subjectName).fold(
                onSuccess = { _uiState.update { it.copy(syncMessage = null) } },
                onFailure = { _uiState.update { it.copy(syncMessage = "Error al eliminar materia") } }
            )
        }
    }
}
