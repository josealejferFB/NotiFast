package com.example.notifast.presentation.feature.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddNoteEvent) {
        when (event) {
            is AddNoteEvent.SubjectChanged -> _uiState.update { it.copy(subject = event.value) }
            is AddNoteEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is AddNoteEvent.ContentChanged -> _uiState.update { it.copy(content = event.value) }
            is AddNoteEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is AddNoteEvent.SaveClicked -> saveNote()
            is AddNoteEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state.subject.isBlank() || state.title.isBlank()) {
            _uiState.update { it.copy(error = "Materia y título son obligatorios") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }
            noteRepository.addNote(userId, state.subject, state.title, state.content, state.imageUri).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") } }
            )
        }
    }
}
