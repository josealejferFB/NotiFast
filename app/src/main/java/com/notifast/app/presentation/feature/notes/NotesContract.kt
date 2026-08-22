package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note

enum class NoteFilter { RECIENTES, ANTIGUAS, POR_MATERIA }

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
    val isLoading: Boolean = false,
    val error: String? = null,
    val notes: Map<String, List<Note>> = emptyMap(),
    val rawNotes: List<Note> = emptyList(),
    val subjects: List<String> = emptyList(),
    val subjectInfos: List<SubjectInfo> = emptyList(),
    val expandedSubject: String? = null,
    val currentFilter: NoteFilter = NoteFilter.RECIENTES,
    val searchQuery: String = "",
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
    data class DeleteNote(val note: Note) : NotesEvent
    data object ClearSyncMessage : NotesEvent
    data class ToggleSubject(val subjectName: String) : NotesEvent
    data class DeleteSubject(val subjectName: String) : NotesEvent
}
