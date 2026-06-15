package com.example.notifast.presentation.feature.editnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.domain.repository.AuthRepository
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
class EditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])
    private val _uiState = MutableStateFlow(EditNoteUiState())
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    init { loadNote() }

    private fun loadNote() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val notes = noteRepository.getNotes(userId).first()
            val note = notes.find { it.id == noteId }
            if (note != null) _uiState.update { it.copy(note = note, subject = note.subject, title = note.title, content = note.content) }
        }
    }

    fun onEvent(event: EditNoteEvent) {
        when (event) {
            is EditNoteEvent.SubjectChanged -> _uiState.update { it.copy(subject = event.value) }
            is EditNoteEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is EditNoteEvent.ContentChanged -> _uiState.update { it.copy(content = event.value) }
            is EditNoteEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is EditNoteEvent.SaveClicked -> saveNote()
            is EditNoteEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveNote() {
        val state = _uiState.value; val note = state.note ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) { _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }; return@launch }
            noteRepository.updateNote(userId, note.copy(subject = state.subject, title = state.title, content = state.content), state.imageUri).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") } }
            )
        }
    }
}
