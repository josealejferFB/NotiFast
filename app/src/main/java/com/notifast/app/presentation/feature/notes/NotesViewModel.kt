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
import com.notifast.app.domain.usecase.ImportSubjectPackageUseCase

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase,
    val importSubjectPackageUseCase: ImportSubjectPackageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<String?>(null)

    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadNotes()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(uid).onSuccess { user ->
                _uiState.update { it.copy(
                    userId = uid,
                    userFirstName = user.firstName,
                    userLastName = user.lastName,
                    userEmail = user.email,
                    userProfilePictureUrl = user.profilePictureUrl
                ) }
            }
        }
    }

    private fun loadNotes() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(userId = userId, isLoading = true, error = null) }

            val filterParamsFlow = combine(_searchQuery, _selectedTag) { query, tag ->
                query to tag
            }

            combine(
                noteRepository.getNotes(userId),
                noteRepository.getSubjects(userId),
                evaluationRepository.getEvaluations(userId),
                scheduleRepository.getSchedules(userId),
                filterParamsFlow
            ) { notes: List<Note>, subjects: List<SubjectData>, evaluations: List<Evaluation>, schedules: List<Schedule>, params: Pair<String, String?> ->
                val (query, tag) = params
                val availableTags = notes.flatMap { it.tags }.distinct().sorted()
                val filtered = filterNotes(notes, query, tag)
                val subjectInfos = computeSubjectInfos(notes, subjects, evaluations)
                val subjectNames = subjects.map { it.name }
                val nextActivity = computeNextActivity(schedules, evaluations, subjects)
                val globalAverage = calculateWeightedAverageUseCase(evaluations)?.let {
                    kotlin.math.round(it * 10) / 10.0
                }

                _uiState.update { prev ->
                    prev.copy(
                        notes = filtered,
                        rawNotes = notes,
                        subjects = subjectNames,
                        subjectInfos = subjectInfos,
                        nextActivity = nextActivity,
                        globalAverage = globalAverage,
                        totalNotesCount = notes.size,
                        totalSubjectsCount = subjects.size,
                        searchQuery = query,
                        availableTags = availableTags,
                        selectedTag = tag,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al cargar notas", isLoading = false) }
            }.collect { }
        }
    }

    private fun computeNextActivity(
        schedules: List<Schedule>,
        evaluations: List<Evaluation>,
        subjects: List<SubjectData>
    ): NextActivity {
        val subjectColorMap = subjects.associate { it.name to it.colorHex }
        val nowCal = java.util.Calendar.getInstance()
        val todayDayOfWeek = nowCal.get(java.util.Calendar.DAY_OF_WEEK) - 1
        val nowMinutes = nowCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + nowCal.get(java.util.Calendar.MINUTE)

        val todaySchedules = schedules
            .filter { it.dayOfWeek == todayDayOfWeek }
            .sortedBy { com.notifast.app.domain.util.TimeParser.toMinutes(it.startTime) ?: 0 }

        val ongoingOrNextToday = todaySchedules.firstOrNull { sched ->
            val end = com.notifast.app.domain.util.TimeParser.toMinutes(sched.endTime) ?: 0
            end > nowMinutes
        }
        if (ongoingOrNextToday != null) {
            return NextActivity.ClassActivity(
                schedule = ongoingOrNextToday,
                colorHex = subjectColorMap[ongoingOrNextToday.subjectName].orEmpty()
            )
        }

        // No more classes today → look ahead 7 days for the next class
        val nextClass = (1..7).firstNotNullOfOrNull { offset ->
            val day = (todayDayOfWeek + offset) % 7
            schedules.filter { it.dayOfWeek == day }
                .minByOrNull { com.notifast.app.domain.util.TimeParser.toMinutes(it.startTime) ?: Int.MAX_VALUE }
        }
        val upcomingEval = evaluations
            .filter { !it.isPresented && it.date != null }
            .sortedBy { it.date }
            .firstOrNull()

        val nextClassTime = nextClass?.let {
            val day = it.dayOfWeek
            val daysAhead = (day - todayDayOfWeek + 7) % 7
            daysAhead * 24 * 60 + (com.notifast.app.domain.util.TimeParser.toMinutes(it.startTime) ?: 0)
        } ?: Int.MAX_VALUE
        val evalTime = upcomingEval?.date?.time ?: Long.MAX_VALUE

        return when {
            upcomingEval != null && (nextClass == null || evalTime <= System.currentTimeMillis() + nextClassTime * 60_000L) ->
                NextActivity.EvaluationActivity(
                    evaluation = upcomingEval,
                    colorHex = subjectColorMap[upcomingEval.subjectName].orEmpty()
                )
            nextClass != null ->
                NextActivity.ClassActivity(
                    schedule = nextClass,
                    colorHex = subjectColorMap[nextClass.subjectName].orEmpty()
                )
            else -> NextActivity.FreeDay
        }
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

    private fun filterNotes(notes: List<Note>, query: String, selectedTag: String?): Map<String, List<Note>> {
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
        // Fixed order: most recent first; notes without timestamp last
        return filtered.sortedByDescending { it.timestamp?.time ?: 0L }.groupBy { it.subject }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.LoadNotes -> loadNotes()
            is NotesEvent.Search -> _searchQuery.value = event.query
            is NotesEvent.SelectTag -> _selectedTag.value = event.tag
            is NotesEvent.DeleteNote -> deleteNote(event.note)
            is NotesEvent.DeleteSubject -> deleteSubject(event.subjectName)
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            noteRepository.deleteNote(userId, note).fold(
                onSuccess = { _uiState.update { it.copy(error = null) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Error al eliminar") } }
            )
        }
    }

    private fun deleteSubject(subjectName: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            noteRepository.deleteSubject(userId, subjectName).fold(
                onSuccess = { _uiState.update { it.copy(error = null) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Error al eliminar materia") } }
            )
        }
    }
}
