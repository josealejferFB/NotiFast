package com.example.notifast.presentation.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.data.model.Note
import com.example.notifast.domain.repository.AuthRepository
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
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(NoteFilter.RECIENTES)
    private val _searchQuery = MutableStateFlow("")

    init { loadNotes() }

    private fun loadNotes() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            combine(
                noteRepository.getNotes(userId),
                noteRepository.getSubjects(userId),
                _currentFilter,
                _searchQuery
            ) { notes, subjects, filter, query ->
                val filtered = filterNotes(notes, filter, query)
                NotesUiState(notes = filtered, subjects = subjects, currentFilter = filter, searchQuery = query)
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun filterNotes(notes: List<Note>, filter: NoteFilter, query: String): Map<String, List<Note>> {
        var filtered = notes
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
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
}
