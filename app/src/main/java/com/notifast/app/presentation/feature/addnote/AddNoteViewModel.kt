package com.notifast.app.presentation.feature.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
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

    init { loadSubjects() }

    fun setInitialSubject(subject: String) {
        if (subject.isNotBlank()) _uiState.update { it.copy(subject = subject) }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            noteRepository.getSubjects(userId).collect { subjectDataList ->
                _uiState.update { it.copy(subjects = subjectDataList) }
            }
        }
    }

    fun onEvent(event: AddNoteEvent) {
        when (event) {
            is AddNoteEvent.SubjectChanged -> _uiState.update { it.copy(subject = event.value) }
            is AddNoteEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is AddNoteEvent.ContentChanged -> _uiState.update { it.copy(content = event.value) }
            is AddNoteEvent.AddTag -> {
                val clean = event.tag.trim()
                if (clean.isNotBlank() && !_uiState.value.tags.contains(clean)) {
                    _uiState.update { it.copy(tags = it.tags + clean) }
                }
            }
            is AddNoteEvent.RemoveTag -> {
                _uiState.update { it.copy(tags = it.tags.filterNot { tag -> tag == event.tag }) }
            }
            is AddNoteEvent.ToggleLivePreview -> _uiState.update { it.copy(isLivePreview = event.enabled) }
            is AddNoteEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is AddNoteEvent.SaveClicked -> saveNote()
            is AddNoteEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is AddNoteEvent.SetSubjects -> _uiState.update { it.copy(subjects = event.subjects) }
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state.subjects.isEmpty()) {
            _uiState.update { it.copy(error = "Crea una materia primero") }
            return
        }
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
            noteRepository.addNote(
                userId = userId,
                subject = state.subject,
                title = state.title,
                content = state.content,
                imageUri = state.imageUri,
                tags = state.tags
            ).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") } }
            )
        }
    }
}