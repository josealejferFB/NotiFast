package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule

enum class NoteFilter { RECIENTES, ANTIGUAS, POR_MATERIA }

sealed interface NextActivity {
    data class ClassActivity(
        val schedule: Schedule,
        val colorHex: String = ""
    ) : NextActivity

    data class EvaluationActivity(
        val evaluation: Evaluation,
        val colorHex: String = ""
    ) : NextActivity

    data object FreeDay : NextActivity
}

data class SubjectInfo(
    val name: String,
    val icon: String,
    val colorHex: String,
    val notes: List<Note>,
    val evaluations: List<Evaluation>,
    val averageGrade: Double?,
    val completedEvalCount: Int,
    val pendingEvalCount: Int,
    val upcomingEvaluations: List<Evaluation>
)

data class NotesUiState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val notes: Map<String, List<Note>> = emptyMap(),
    val rawNotes: List<Note> = emptyList(),
    val subjects: List<String> = emptyList(),
    val subjectInfos: List<SubjectInfo> = emptyList(),
    val nextActivity: NextActivity = NextActivity.FreeDay,
    val globalAverage: Double? = null,
    val totalNotesCount: Int = 0,
    val totalSubjectsCount: Int = 0,
    val expandedSubject: String? = null,
    val currentFilter: NoteFilter = NoteFilter.RECIENTES,
    val searchQuery: String = "",
    val availableTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val syncMessage: String? = null,
    val userFirstName: String = "",
    val userLastName: String = "",
    val userEmail: String = "",
    val userProfilePictureUrl: String? = null
)

sealed interface NotesEvent {
    data object LoadNotes : NotesEvent
    data class SetFilter(val filter: NoteFilter) : NotesEvent
    data class Search(val query: String) : NotesEvent
    data class SelectTag(val tag: String?) : NotesEvent
    data class DeleteNote(val note: Note) : NotesEvent
    data object ClearSyncMessage : NotesEvent
    data class ToggleSubject(val subjectName: String) : NotesEvent
    data class DeleteSubject(val subjectName: String) : NotesEvent
}
