package com.notifast.app.presentation.feature.addsubject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.first

@HiltViewModel
class AddSubjectViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSubjectUiState())
    val uiState: StateFlow<AddSubjectUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddSubjectEvent) {
        when (event) {
            is AddSubjectEvent.NameChanged -> _uiState.update { it.copy(subjectName = event.name, error = null) }
            is AddSubjectEvent.IconSelected -> _uiState.update { it.copy(selectedIcon = event.icon) }
            is AddSubjectEvent.ColorSelected -> _uiState.update { it.copy(selectedColor = event.colorHex) }
            is AddSubjectEvent.SaveClicked -> saveSubject()
        }
    }

    private fun saveSubject() {
        val state = _uiState.value
        val cleanName = state.subjectName.trim()
        if (cleanName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre de la materia no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }

            // Validar materias duplicadas (case-insensitive)
            val existingSubjects = try {
                noteRepository.getSubjects(userId).first()
            } catch (e: Exception) {
                emptyList()
            }

            if (existingSubjects.any { it.name.equals(cleanName, ignoreCase = true) }) {
                _uiState.update { it.copy(isLoading = false, error = "Ya tienes una materia registrada con este nombre") }
                return@launch
            }

            noteRepository.addSubject(userId, cleanName, state.selectedIcon, state.selectedColor).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true, subjectName = cleanName) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar la materia") } }
            )
        }
    }
}
