package com.example.notifast.presentation.feature.addschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.data.model.Schedule
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.NoteRepository
import com.example.notifast.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddScheduleUiState())
    val uiState: StateFlow<AddScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val subjects = noteRepository.getSubjects(userId).first()
            _uiState.update { it.copy(subjects = subjects) }
        }
    }

    fun onEvent(event: AddScheduleEvent) {
        when (event) {
            is AddScheduleEvent.UpdateSubjectName -> _uiState.update { it.copy(subjectName = event.subjectName) }
            is AddScheduleEvent.UpdateModality -> _uiState.update {
                // When switching to virtual, clear time/day/classroom fields
                if (event.modality == Schedule.MODALITY_VIRTUAL) {
                    it.copy(modality = event.modality, dayOfWeek = 0, startTime = "", endTime = "", classroom = "")
                } else {
                    it.copy(modality = event.modality, meetingUrl = "")
                }
            }
            is AddScheduleEvent.UpdateDayOfWeek -> _uiState.update { it.copy(dayOfWeek = event.day) }
            is AddScheduleEvent.UpdateStartTime -> _uiState.update { it.copy(startTime = event.time) }
            is AddScheduleEvent.UpdateEndTime -> _uiState.update { it.copy(endTime = event.time) }
            is AddScheduleEvent.UpdateClassroom -> _uiState.update { it.copy(classroom = event.classroom) }
            is AddScheduleEvent.UpdateTeacher -> _uiState.update { it.copy(teacher = event.teacher) }
            is AddScheduleEvent.UpdateMeetingUrl -> _uiState.update { it.copy(meetingUrl = event.url) }
            is AddScheduleEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is AddScheduleEvent.SaveSchedule -> saveSchedule()
        }
    }

    private fun saveSchedule() {
        val state = _uiState.value
        if (state.subjectName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre de la materia es requerido") }
            return
        }
        // Presencial requires time and day; virtual does not
        if (state.modality == Schedule.MODALITY_PRESENCIAL) {
            if (state.startTime.isBlank() || state.endTime.isBlank()) {
                _uiState.update { it.copy(error = "Las horas de inicio y fin son requeridas para clases presenciales") }
                return
            }
        }

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }

            // Find subject color
            val subjectColorHex = state.subjects.find { it.name == state.subjectName }?.colorHex
            val subjectColor = if (!subjectColorHex.isNullOrBlank()) {
                try {
                    android.graphics.Color.parseColor(subjectColorHex).toLong()
                } catch (e: Exception) { 0L }
            } else 0L

            val schedule = Schedule(
                userId = userId,
                subjectName = state.subjectName,
                modality = state.modality,
                dayOfWeek = state.dayOfWeek,
                startTime = state.startTime,
                endTime = state.endTime,
                classroom = state.classroom,
                teacher = state.teacher,
                meetingUrl = state.meetingUrl.takeIf { it.isNotBlank() },
                color = subjectColor,
                createdAt = Date()
            )

            scheduleRepository.addSchedule(userId, schedule).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaved = true, isSaving = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error al guardar horario", isSaving = false) }
                }
            )
        }
    }
}
