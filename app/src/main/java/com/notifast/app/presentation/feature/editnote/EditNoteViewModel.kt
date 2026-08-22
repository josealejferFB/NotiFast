package com.notifast.app.presentation.feature.editnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var saveJob: Job? = null

    init { loadNote() }

    private fun loadNote() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val notes = noteRepository.getNotes(userId).first()
            val note = notes.find { it.id == noteId }
            if (note != null) {
                val subjects = noteRepository.getSubjects(userId).first()
                val colorHex = subjects.find { it.name == note.subject }?.colorHex ?: ""
                _uiState.update { 
                    it.copy(
                        note = note,
                        subjectColorHex = colorHex,
                        title = note.title,
                        content = note.content,
                        tags = note.tags
                    ) 
                }
            }
        }
    }

    fun onEvent(event: EditNoteEvent) {
        when (event) {
            is EditNoteEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.value, syncStatus = "Guardando...") }
                scheduleAutoSave()
            }
            is EditNoteEvent.ContentChanged -> {
                _uiState.update { it.copy(content = event.value, syncStatus = "Guardando...") }
                scheduleAutoSave()
            }
            is EditNoteEvent.AddTag -> {
                val clean = event.tag.trim()
                if (clean.isNotBlank() && !_uiState.value.tags.contains(clean)) {
                    _uiState.update { it.copy(tags = it.tags + clean, syncStatus = "Guardando...") }
                    scheduleAutoSave()
                }
            }
            is EditNoteEvent.RemoveTag -> {
                _uiState.update { it.copy(tags = it.tags.filterNot { tag -> tag == event.tag }, syncStatus = "Guardando...") }
                scheduleAutoSave()
            }
            is EditNoteEvent.ToggleLivePreview -> _uiState.update { it.copy(isLivePreview = event.enabled) }
            is EditNoteEvent.ImageSelected -> {
                _uiState.update { it.copy(imageUri = event.uri, syncStatus = "Guardando...") }
                scheduleAutoSave()
            }
            is EditNoteEvent.SaveClicked -> {
                saveJob?.cancel()
                saveNote(exitAfter = true)
            }
            is EditNoteEvent.ClearError -> _uiState.update { it.copy(error = null) }
            else -> {}
        }
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000)
            saveNote(exitAfter = false)
        }
    }

    private fun saveNote(exitAfter: Boolean = false) {
        val state = _uiState.value; val note = state.note ?: return
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) { _uiState.update { it.copy(error = "Usuario no autenticado", syncStatus = "Error") }; return@launch }
            
            noteRepository.updateNote(
                userId = userId,
                note = note.copy(title = state.title, content = state.content, tags = state.tags),
                imageUri = state.imageUri
            ).fold(
                onSuccess = { _uiState.update { it.copy(syncStatus = "Guardado", isSaved = exitAfter) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Error al guardar", syncStatus = "Error") } }
            )
        }
    }
}
