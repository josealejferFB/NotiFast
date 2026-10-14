package com.notifast.app.presentation.feature.editschedule

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.TeacherRepository
import com.notifast.app.domain.usecase.ScheduleOverlapResult
import com.notifast.app.domain.usecase.ValidateScheduleOverlapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

private const val TAG = "EditScheduleViewModel"

@HiltViewModel
class EditScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val teacherRepository: TeacherRepository,
    private val validateScheduleOverlapUseCase: ValidateScheduleOverlapUseCase,
    private val scheduleClassAlertUseCase: com.notifast.app.domain.usecase.ScheduleClassAlertUseCase,
    private val cancelAlertUseCase: com.notifast.app.domain.usecase.CancelAlertUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var scheduleId: String? = savedStateHandle["scheduleId"]
    private var originalSchedule: Schedule? = null
    private var teachersJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState: StateFlow<EditScheduleUiState> = _uiState.asStateFlow()

    init {
        if (!scheduleId.isNullOrBlank()) {
            loadData()
        }
    }

    private fun loadData() {
        val currentId = scheduleId ?: return
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val subjects = noteRepository.getSubjects(userId).first()
            val schedules = scheduleRepository.getSchedules(userId).first()
            val schedule = schedules.find { it.id == currentId }
            if (schedule != null) {
                originalSchedule = schedule
                _uiState.update { 
                    it.copy(
                        subjects = subjects,
                        existingSchedules = schedules,
                        subjectName = schedule.subjectName,
                        modality = schedule.modality,
                        dayOfWeek = schedule.dayOfWeek,
                        startTime = schedule.startTime,
                        endTime = schedule.endTime,
                        classroom = schedule.classroom,
                        teacher = schedule.teacher,
                        meetingUrl = schedule.meetingUrl ?: "",
                        isLoading = false
                    ) 
                }
                loadTeachersForSubject(schedule.subjectName)
            } else {
                _uiState.update { it.copy(subjects = subjects, existingSchedules = schedules, isLoading = false, error = "Horario no encontrado") }
            }
        }
    }

    private fun loadTeachersForSubject(subjectName: String) {
        teachersJob?.cancel()
        if (subjectName.isBlank()) {
            _uiState.update { it.copy(availableTeachers = emptyList(), hasConfiguredTeacher = true) }
            return
        }
        teachersJob = viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            teacherRepository.getTeachersBySubject(userId, subjectName).collect { teachers ->
                _uiState.update {
                    it.copy(
                        availableTeachers = teachers,
                        hasConfiguredTeacher = teachers.isNotEmpty()
                    )
                }
            }
        }
    }

    fun onEvent(event: EditScheduleEvent) {
        when (event) {
            is EditScheduleEvent.Init -> {
                this.scheduleId = event.scheduleId
                _uiState.update {
                    it.copy(
                        subjectName = "",
                        modality = Schedule.MODALITY_PRESENCIAL,
                        dayOfWeek = 1,
                        startTime = "",
                        endTime = "",
                        classroom = "",
                        teacher = "",
                        meetingUrl = "",
                        conflictingSchedules = emptyList(),
                        isLoading = true,
                        isSaved = false,
                        error = null
                    )
                }
                loadData()
            }
            is EditScheduleEvent.UpdateSubjectName -> {
                _uiState.update { it.copy(subjectName = event.subjectName) }
                loadTeachersForSubject(event.subjectName)
            }
            is EditScheduleEvent.UpdateModality -> _uiState.update {
                if (event.modality == Schedule.MODALITY_VIRTUAL) {
                    it.copy(modality = event.modality, classroom = "", conflictingSchedules = emptyList())
                } else {
                    it.copy(modality = event.modality, meetingUrl = "")
                }
            }
            is EditScheduleEvent.UpdateDayOfWeek -> {
                _uiState.update { it.copy(dayOfWeek = event.day) }
                checkConflicts()
            }
            is EditScheduleEvent.UpdateStartTime -> {
                val correctedEnd = if (_uiState.value.endTime.isNotBlank()) {
                    sanitizeEndTime(event.time, _uiState.value.endTime)
                } else _uiState.value.endTime
                _uiState.update { it.copy(startTime = event.time, endTime = correctedEnd) }
                checkConflicts()
            }
            is EditScheduleEvent.UpdateEndTime -> {
                val correctedEnd = if (_uiState.value.startTime.isNotBlank()) {
                    sanitizeEndTime(_uiState.value.startTime, event.time)
                } else event.time
                _uiState.update { it.copy(endTime = correctedEnd) }
                checkConflicts()
            }
            is EditScheduleEvent.UpdateTimeRange -> {
                val correctedEnd = sanitizeEndTime(event.startTime, event.endTime)
                _uiState.update { it.copy(startTime = event.startTime, endTime = correctedEnd) }
                checkConflicts()
            }
            is EditScheduleEvent.UpdateClassroom -> _uiState.update { it.copy(classroom = event.classroom) }
            is EditScheduleEvent.UpdateTeacher -> _uiState.update { it.copy(teacher = event.teacher) }
            is EditScheduleEvent.UpdateMeetingUrl -> _uiState.update { it.copy(meetingUrl = event.url) }
            is EditScheduleEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is EditScheduleEvent.SaveSchedule -> saveSchedule()
        }
    }

    private fun sanitizeEndTime(start: String, end: String): String {
        if (start.isBlank() || end.isBlank()) return end
        val startParts = start.split(":").mapNotNull { it.toIntOrNull() }
        val endParts = end.split(":").mapNotNull { it.toIntOrNull() }
        if (startParts.size != 2 || endParts.size != 2) return end
        val startMins = startParts[0] * 60 + startParts[1]
        val endMins = endParts[0] * 60 + endParts[1]
        return if (endMins <= startMins) {
            val correctedMins = minOf(startMins + 120, 23 * 60 + 59)
            String.format(java.util.Locale.US, "%02d:%02d", correctedMins / 60, correctedMins % 60)
        } else {
            end
        }
    }

    private fun checkConflicts() {
        val state = _uiState.value
        if (state.modality != Schedule.MODALITY_PRESENCIAL || state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.update { it.copy(conflictingSchedules = emptyList()) }
            return
        }
        val currentId = scheduleId ?: ""
        val tempSchedule = Schedule(
            id = currentId,
            subjectName = state.subjectName,
            dayOfWeek = state.dayOfWeek,
            startTime = state.startTime,
            endTime = state.endTime
        )
        val result = validateScheduleOverlapUseCase(
            existingSchedules = state.existingSchedules,
            newSchedule = tempSchedule,
            excludeScheduleId = currentId
        )
        val conflicts = when (result) {
            is ScheduleOverlapResult.Conflict -> result.conflictingSchedules
            is ScheduleOverlapResult.NoOverlap -> emptyList()
        }
        _uiState.update { it.copy(conflictingSchedules = conflicts) }
    }

    private fun saveSchedule() {
        val currentId = scheduleId
        if (currentId == null) {
            _uiState.update { it.copy(error = "Horario no encontrado") }
            return
        }
        val state = _uiState.value
        if (state.subjectName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre de la materia es requerido") }
            return
        }
        if (state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.update { it.copy(error = "Las horas de inicio y fin son requeridas") }
            return
        }

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }

            val subjectColorHex = state.subjects.find { it.name == state.subjectName }?.colorHex
            val subjectColor = if (!subjectColorHex.isNullOrBlank()) {
                try {
                    android.graphics.Color.parseColor(subjectColorHex).toLong()
                } catch (e: Exception) { 0L }
            } else 0L

            val schedule = Schedule(
                id = currentId,
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
                createdAt = originalSchedule?.createdAt ?: Date()
            )

            val teacherName = state.teacher.trim()
            if (teacherName.isNotBlank() && state.subjectName.isNotBlank()) {
                try {
                    val allTeachers = teacherRepository.getTeachers(userId).first()
                    val existing = allTeachers.find { it.fullName.equals(teacherName, ignoreCase = true) }
                    if (existing != null) {
                        if (!existing.subjectNames.any { it.equals(state.subjectName, ignoreCase = true) }) {
                            teacherRepository.saveTeacher(
                                existing.copy(subjectNames = (existing.subjectNames + state.subjectName).distinct())
                            )
                        }
                    } else {
                        val parts = teacherName.split(" ", limit = 2)
                        val newTeacher = Teacher(
                            userId = userId,
                            firstName = parts.getOrNull(0).orEmpty(),
                            lastName = parts.getOrNull(1).orEmpty(),
                            subjectNames = listOf(state.subjectName)
                        )
                        teacherRepository.saveTeacher(newTeacher)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Best-effort teacher sync failed for teacher '$teacherName'", e)
                }
            }

            scheduleRepository.updateSchedule(userId, schedule).fold(
                onSuccess = {
                    // Virtual classes are scheduled slots too (D10)
                    scheduleClassAlertUseCase(schedule)
                    _uiState.update { it.copy(isSaved = true, isSaving = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error al actualizar horario", isSaving = false) }
                }
            )
        }
    }
}
