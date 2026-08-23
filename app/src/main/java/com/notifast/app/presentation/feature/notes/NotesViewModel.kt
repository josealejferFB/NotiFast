package com.notifast.app.presentation.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(NoteFilter.RECIENTES)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<String?>(null)

    init {
        loadNotes()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(uid).onSuccess { user ->
                _uiState.update { it.copy(
                    userFirstName = user.firstName,
                    userLastName = user.lastName,
                    userEmail = user.email,
                    userProfilePictureUrl = user.profilePictureUrl
                ) }
            }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }

            val filterParamsFlow = combine(_currentFilter, _searchQuery, _selectedTag) { filter, query, tag ->
                Triple(filter, query, tag)
            }

            combine(
                noteRepository.getNotes(userId),
                noteRepository.getSubjects(userId),
                evaluationRepository.getEvaluations(userId),
                scheduleRepository.getSchedules(userId),
                filterParamsFlow
            ) { notes: List<Note>, subjects: List<SubjectData>, evaluations: List<Evaluation>, schedules: List<Schedule>, params: Triple<NoteFilter, String, String?> ->
                val (filter, query, tag) = params
                val availableTags = notes.flatMap { it.tags }.distinct().sorted()
                val filtered = filterNotes(notes, filter, query, tag)
                val subjectInfos = computeSubjectInfos(notes, subjects, evaluations)
                val subjectNames = subjects.map { it.name }
                val nextActivity = computeNextActivity(schedules, evaluations, subjects)
                val globalAverage = calculateWeightedAverageUseCase(evaluations)?.let {
                    kotlin.math.round(it * 10) / 10.0
                }

                NotesUiState(
                    notes = filtered,
                    rawNotes = notes,
                    subjects = subjectNames,
                    subjectInfos = subjectInfos,
                    nextActivity = nextActivity,
                    globalAverage = globalAverage,
                    totalNotesCount = notes.size,
                    totalSubjectsCount = subjects.size,
                    currentFilter = filter,
                    searchQuery = query,
                    availableTags = availableTags,
                    selectedTag = tag
                )
            }.catch { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al cargar notas", isLoading = false) }
            }.collect { newState ->
                _uiState.update { it.copy(
                    notes = newState.notes,
                    rawNotes = newState.rawNotes,
                    subjects = newState.subjects,
                    subjectInfos = newState.subjectInfos,
                    nextActivity = newState.nextActivity,
                    globalAverage = newState.globalAverage,
                    totalNotesCount = newState.totalNotesCount,
                    totalSubjectsCount = newState.totalSubjectsCount,
                    currentFilter = newState.currentFilter,
                    searchQuery = newState.searchQuery,
                    availableTags = newState.availableTags,
                    selectedTag = newState.selectedTag,
                    isLoading = false,
                    error = newState.error
                ) }
            }
        }
    }

    private fun computeNextActivity(
        schedules: List<Schedule>,
        evaluations: List<Evaluation>,
        subjects: List<SubjectData>
    ): NextActivity {
        val subjectColorMap = subjects.associate { it.name to it.colorHex }
        val todayDayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        val todaySchedules = schedules
            .filter { it.dayOfWeek == todayDayOfWeek }
            .sortedBy { it.startTime }

        if (todaySchedules.isNotEmpty()) {
            val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val upcomingClass = todaySchedules.firstOrNull { it.endTime >= now } ?: todaySchedules.first()
            return NextActivity.ClassActivity(
                schedule = upcomingClass,
                colorHex = subjectColorMap[upcomingClass.subjectName].orEmpty()
            )
        }

        val upcomingEval = evaluations
            .filter { !it.isPresented && it.date != null }
            .sortedBy { it.date }
            .firstOrNull()

        if (upcomingEval != null) {
            return NextActivity.EvaluationActivity(
                evaluation = upcomingEval,
                colorHex = subjectColorMap[upcomingEval.subjectName].orEmpty()
            )
        }

        return NextActivity.FreeDay
    }

    private fun computeSubjectInfos(
        notes: List<Note>,
        subjects: List<SubjectData>,
        evaluations: List<Evaluation>
    ): List<SubjectInfo> {
        return subjects.map { subject ->
            val subjectNotes = notes.filter { it.subject == subject.name }
            val subjectEvals = evaluations.filter { it.subjectName == subject.name }
            val avg = calculateWeightedAverageUseCase(subjectEvals)
            val upcoming = subjectEvals
                .filter { !it.isPresented && it.date != null }
                .sortedBy { it.date }
                .take(3)

            SubjectInfo(
                name = subject.name,
                icon = subject.icon,
                colorHex = subject.colorHex,
                notes = subjectNotes,
                evaluations = subjectEvals,
                averageGrade = avg?.let { kotlin.math.round(it * 10) / 10.0 },
                completedEvalCount = subjectEvals.count { it.isPresented },
                pendingEvalCount = subjectEvals.count { !it.isPresented },
                upcomingEvaluations = upcoming
            )
        }
    }

    private fun filterNotes(notes: List<Note>, filter: NoteFilter, query: String, selectedTag: String?): Map<String, List<Note>> {
        var filtered = notes
        if (!selectedTag.isNullOrBlank()) {
            filtered = filtered.filter { it.tags.any { tag -> tag.equals(selectedTag, ignoreCase = true) } }
        }
        val cleanQuery = query.trim()
        if (cleanQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(cleanQuery, ignoreCase = true) ||
                it.content.contains(cleanQuery, ignoreCase = true) ||
                it.subject.contains(cleanQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(cleanQuery, ignoreCase = true) }
            }
        }
        return when (filter) {
            NoteFilter.RECIENTES -> filtered.sortedByDescending { it.timestamp }.groupBy { it.subject }
            NoteFilter.ANTIGUAS -> filtered.sortedBy { it.timestamp ?: Date() }.groupBy { it.subject }
            NoteFilter.POR_MATERIA -> filtered.groupBy { it.subject }
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.LoadNotes -> loadNotes()
            is NotesEvent.SetFilter -> _currentFilter.value = event.filter
            is NotesEvent.Search -> _searchQuery.value = event.query
            is NotesEvent.SelectTag -> _selectedTag.value = event.tag
            is NotesEvent.ClearSyncMessage -> _uiState.update { it.copy(syncMessage = null) }
            is NotesEvent.DeleteNote -> deleteNote(event.note)
            is NotesEvent.ToggleSubject -> {
                val current = _uiState.value.expandedSubject
                _uiState.update { it.copy(expandedSubject = if (current == event.subjectName) null else event.subjectName) }
            }
            is NotesEvent.DeleteSubject -> deleteSubject(event.subjectName)
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

    private fun deleteSubject(subjectName: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(syncMessage = "Eliminando materia...") }
            noteRepository.deleteSubject(userId, subjectName).fold(
                onSuccess = { _uiState.update { it.copy(syncMessage = null) } },
                onFailure = { _uiState.update { it.copy(syncMessage = "Error al eliminar materia") } }
            )
        }
    }
}
