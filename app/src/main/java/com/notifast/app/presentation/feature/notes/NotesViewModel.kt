package com.notifast.app.presentation.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
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
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.repository.TeacherRepository
import com.notifast.app.domain.usecase.ImportSubjectPackageUseCase
import com.notifast.app.domain.util.MarkdownToBlocksParser
import com.notifast.app.presentation.atoms.MarkdownParser

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase,
    val importSubjectPackageUseCase: ImportSubjectPackageUseCase,
    private val teacherRepository: TeacherRepository,
    private val observeLocalSyncSummaryUseCase: com.notifast.app.domain.usecase.cloud.ObserveLocalSyncSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _teacherSearchQuery = MutableStateFlow("")

    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadNotes()
        loadUserProfile()
        observeSyncSummary()
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

            val gradePrefs = combine(
                userPrefsRepository.defaultGradeScaleFlow,
                userPrefsRepository.minPassingGradeFlow
            ) { scaleId, minPassing -> ScalePrefs(scaleId, minPassing) }

            val dataFlow = combine(
                noteRepository.getNotes(userId),
                noteRepository.getSubjects(userId),
                evaluationRepository.getEvaluations(userId),
                scheduleRepository.getSchedules(userId),
                gradePrefs
            ) { notes: List<Note>, subjects: List<SubjectData>, evaluations: List<Evaluation>, schedules: List<Schedule>, prefs: ScalePrefs ->
                DomainData(notes, subjects, evaluations, schedules, prefs.scaleId, prefs.minPassing)
            }

            val teachersFlow = combine(
                teacherRepository.getTeachers(userId),
                _teacherSearchQuery
            ) { teachers, query ->
                val cleanQ = query.trim()
                val filtered = if (cleanQ.isBlank()) {
                    teachers
                } else {
                    teachers.filter {
                        it.fullName.contains(cleanQ, ignoreCase = true) ||
                        it.subjectNames.any { sub -> sub.contains(cleanQ, ignoreCase = true) } ||
                        it.email.contains(cleanQ, ignoreCase = true)
                    }
                }
                teachers to filtered
            }

            combine(
                dataFlow,
                combine(_searchQuery, _selectedTag) { query, tag -> query to tag },
                teachersFlow
            ) { domain, params, teacherData ->
                val (query, tag) = params
                val (allTeachers, filteredTeachers) = teacherData
                val scale = GradeScale.fromId(domain.scaleId)
                val customMinPassing = domain.minPassing.toDouble().takeIf {
                    it > scale.minGrade && it <= scale.maxGrade
                }
                val minPassingGrade = GradePolicy.resolveMinPassing(scale, customMinPassing)
                val notes = domain.notes
                val subjects = domain.subjects
                val evaluations = domain.evaluations
                val availableTags = notes.flatMap { it.tags }.distinct().sorted()
                val filtered = filterNotes(notes, query, tag)
                val subjectInfos = computeSubjectInfos(notes, subjects, evaluations, scale)
                val subjectNames = subjects.map { it.name }
                val nextActivity = computeNextActivity(domain.schedules, evaluations, subjects)
                val globalAverage = calculateWeightedAverageUseCase(evaluations, scale)?.let {
                    scale.roundGrade(it)
                }

                _uiState.update { prev ->
                    prev.copy(
                        notes = filtered,
                        rawNotes = notes,
                        subjects = subjectNames,
                        subjectInfos = subjectInfos,
                        teachers = allTeachers,
                        filteredTeachers = filteredTeachers,
                        teacherSearchQuery = _teacherSearchQuery.value,
                        nextActivity = nextActivity,
                        globalAverage = globalAverage,
                        gradeScale = scale,
                        minPassingGrade = minPassingGrade,
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
        evaluations: List<Evaluation>,
        scale: GradeScale
    ): List<SubjectInfo> {
        return subjects.map { subject ->
            val subjectNotes = notes.filter { it.subject == subject.name }
            val subjectEvals = evaluations.filter { it.subjectName == subject.name }
            val avg = calculateWeightedAverageUseCase(subjectEvals, scale)
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
                averageGrade = avg?.let { scale.roundGrade(it) },
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
            is NotesEvent.UpdateSubject -> updateSubject(event.oldName, event.newName, event.newColorHex, event.newIcon)
            is NotesEvent.ToggleTaskCheck -> toggleTaskCheck(event.note, event.taskIndex, event.isChecked)
            is NotesEvent.SelectSubTab -> _uiState.update { it.copy(selectedSubTab = event.subTab) }
            is NotesEvent.SearchTeachers -> _teacherSearchQuery.value = event.query
            is NotesEvent.SelectTeacherForDetail -> _uiState.update { it.copy(selectedTeacherForDetail = event.teacher) }
            is NotesEvent.OpenAddEditTeacher -> _uiState.update { it.copy(teacherBeingEdited = event.teacher) }
            is NotesEvent.SaveTeacher -> saveTeacher(event.teacher)
            is NotesEvent.DeleteTeacher -> deleteTeacher(event.teacherId)
            is NotesEvent.CloudClick -> _uiState.update { it.copy(showCloudHub = true) }
            is NotesEvent.DismissCloudHub -> _uiState.update { it.copy(showCloudHub = false) }
        }
    }

    private fun observeSyncSummary() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            observeLocalSyncSummaryUseCase(uid).collect { summary ->
                _uiState.update { it.copy(syncSummary = summary) }
            }
        }
    }

    private fun saveTeacher(teacher: Teacher) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val toSave = if (teacher.userId.isBlank()) teacher.copy(userId = userId) else teacher
            teacherRepository.saveTeacher(toSave).fold(
                onSuccess = {
                    _uiState.update { it.copy(teacherBeingEdited = null, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error al guardar profesor") }
                }
            )
        }
    }

    private fun deleteTeacher(teacherId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            teacherRepository.deleteTeacher(userId, teacherId).fold(
                onSuccess = {
                    _uiState.update { it.copy(selectedTeacherForDetail = null, error = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error al eliminar profesor") }
                }
            )
        }
    }

    private fun updateSubject(oldName: String, newName: String, newColorHex: String, newIcon: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val updatedSubject = SubjectData(
                name = newName,
                colorHex = newColorHex,
                icon = newIcon
            )
            noteRepository.updateSubject(userId, oldName, updatedSubject).fold(
                onSuccess = { _uiState.update { it.copy(error = null) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Error al actualizar materia") } }
            )
        }
    }

    private fun toggleTaskCheck(note: Note, taskIndex: Int, isChecked: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val currentNote = _uiState.value.rawNotes.find { it.id == note.id } ?: note
            val updatedContent = MarkdownParser.toggleTaskCheckbox(currentNote.content, taskIndex, isChecked)
            val updatedNote = currentNote.copy(
                content = updatedContent,
                updatedAt = Date(),
                blocks = MarkdownToBlocksParser.parse(updatedContent, currentNote.imageUrl)
            )
            _uiState.update { prev ->
                val newRaw = prev.rawNotes.map { if (it.id == updatedNote.id) updatedNote else it }
                val newFiltered = prev.notes.mapValues { (_, list) ->
                    list.map { if (it.id == updatedNote.id) updatedNote else it }
                }
                prev.copy(rawNotes = newRaw, notes = newFiltered)
            }
            noteRepository.updateNote(userId, updatedNote, null).fold(
                onSuccess = { _uiState.update { it.copy(error = null) } },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error al actualizar nota") }
                    loadNotes()
                }
            )
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

    private data class DomainData(
        val notes: List<Note>,
        val subjects: List<SubjectData>,
        val evaluations: List<Evaluation>,
        val schedules: List<Schedule>,
        val scaleId: String,
        val minPassing: Float
    )

    private data class ScalePrefs(val scaleId: String, val minPassing: Float)
}
