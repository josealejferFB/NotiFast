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

import com.notifast.app.domain.usecase.ResolveSubjectNameUseCase
import com.notifast.app.domain.util.ErrorSanitizer
import com.notifast.app.domain.util.NameValidation

@HiltViewModel
class AddSubjectViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    private val resolveSubjectNameUseCase: ResolveSubjectNameUseCase = ResolveSubjectNameUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSubjectUiState())
    val uiState: StateFlow<AddSubjectUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddSubjectEvent) {
        when (event) {
            is AddSubjectEvent.NameChanged -> _uiState.update { it.copy(subjectName = event.name, error = null) }
            is AddSubjectEvent.SectionChanged -> _uiState.update { it.copy(section = event.section) }
            is AddSubjectEvent.SuggestionSelected -> _uiState.update { it.copy(subjectName = event.name, error = null) }
            is AddSubjectEvent.IconSelected -> _uiState.update { it.copy(selectedIcon = event.icon) }
            is AddSubjectEvent.ColorSelected -> _uiState.update { it.copy(selectedColor = event.colorHex) }
            is AddSubjectEvent.SaveClicked -> saveSubject()
            is AddSubjectEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is AddSubjectEvent.ResetState -> _uiState.update { AddSubjectUiState() }
        }
    }

    private fun saveSubject() {
        val state = _uiState.value
        if (state.isLoading || state.isSaved) return
        val cleanName = state.subjectName.trim()
        val cleanSection = state.section.trim()
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }

            // Validar materia (longitud, vacía, duplicados case-insensitive)
            val existingSubjects = try {
                noteRepository.getSubjects(userId).first()
            } catch (e: Exception) {
                emptyList()
            }

            when (resolveSubjectNameUseCase.validate(cleanName, existingSubjects.map { it.name })) {
                is NameValidation.Blank -> {
                    _uiState.update { it.copy(isLoading = false, error = "El nombre de la materia no puede estar vacío") }
                    return@launch
                }
                is NameValidation.TooLong -> {
                    _uiState.update { it.copy(isLoading = false, error = "El nombre no puede superar los 50 caracteres") }
                    return@launch
                }
                is NameValidation.Duplicate -> {
                    _uiState.update { it.copy(isLoading = false, error = "Ya tienes una materia registrada con este nombre") }
                    return@launch
                }
                is NameValidation.Valid -> Unit
            }

            noteRepository.addSubject(
                userId = userId,
                subjectName = cleanName,
                icon = state.selectedIcon,
                colorHex = state.selectedColor,
                section = cleanSection
            ).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true, createdSubjectName = cleanName, subjectName = cleanName) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = ErrorSanitizer.sanitize(e, "Error al guardar la materia")) } }
            )
        }
    }
}
