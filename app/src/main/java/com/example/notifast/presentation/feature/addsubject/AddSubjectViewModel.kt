package com.example.notifast.presentation.feature.addsubject

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
class AddSubjectViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSubjectUiState())
    val uiState: StateFlow<AddSubjectUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddSubjectEvent) {
        when (event) {
            is AddSubjectEvent.NameChanged -> _uiState.update { it.copy(subjectName = event.name) }
            is AddSubjectEvent.IconSelected -> _uiState.update { it.copy(selectedIcon = event.icon) }
            is AddSubjectEvent.ColorSelected -> _uiState.update { it.copy(selectedColor = event.colorHex) }
            is AddSubjectEvent.SaveClicked -> saveSubject()
        }
    }

    private fun saveSubject() {
        val state = _uiState.value
        if (state.subjectName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }
            noteRepository.addSubject(userId, state.subjectName, state.selectedIcon, state.selectedColor).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") } }
            )
        }
    }
}
