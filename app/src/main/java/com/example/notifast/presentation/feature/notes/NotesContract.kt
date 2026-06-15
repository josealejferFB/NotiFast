package com.example.notifast.presentation.feature.notes

import com.example.notifast.data.model.Note

enum class NoteFilter { RECIENTES, ANTIGUAS, POR_MATERIA }

data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: Map<String, List<Note>> = emptyMap(),
    val subjects: List<String> = emptyList(),
    val currentFilter: NoteFilter = NoteFilter.RECIENTES,
    val searchQuery: String = "",
    val syncMessage: String? = null
)

sealed interface NotesEvent {
    data object LoadNotes : NotesEvent
    data class SetFilter(val filter: NoteFilter) : NotesEvent
    data class Search(val query: String) : NotesEvent
    data class DeleteNote(val note: Note) : NotesEvent
    data object ClearSyncMessage : NotesEvent
}
