package com.notifast.app.presentation.feature.noteeditor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.usecase.ExportNoteMarkdownUseCase
import com.notifast.app.domain.util.MarkdownToBlocksParser
import com.notifast.app.presentation.atoms.MarkdownParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    private val imageRepository: ImageRepository,
    private val exportNoteMarkdownUseCase: ExportNoteMarkdownUseCase = ExportNoteMarkdownUseCase(),
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val noteId: String? = savedStateHandle.get<String>("noteId")?.let { if (it.isBlank()) null else it }
    private val initialSubject: String = savedStateHandle.get<String>("subject") ?: ""

    private val _uiState = MutableStateFlow(
        NoteEditorUiState(
            noteId = noteId,
            subject = initialSubject
        )
    )
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        loadInitial(noteId, initialSubject)
    }

    private fun loadInitial(targetNoteId: String? = noteId, targetSubject: String = initialSubject) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSaved = false,
                    error = null,
                    syncStatus = null,
                    noteId = targetNoteId,
                    subject = targetSubject
                )
            }
            val userId = authRepository.getCurrentUserId()
            val subjects = if (userId != null) noteRepository.getSubjects(userId).first() else emptyList()

            if (targetNoteId != null && userId != null) {
                val notes = noteRepository.getNotes(userId).first()
                val existing = notes.find { it.id == targetNoteId }
                if (existing != null) {
                    val content = when {
                        existing.content.isNotBlank() -> existing.content
                        existing.blocks.isNotEmpty() -> {
                            val fullExport = exportNoteMarkdownUseCase(existing)
                            fullExport.substringAfter("\n\n", existing.content).trim()
                        }
                        else -> ""
                    }
                    val words = calculateWordCount(content)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            noteId = existing.id,
                            subject = existing.subject,
                            subjects = subjects,
                            title = existing.title,
                            content = content,
                            tags = existing.tags,
                            imageUrl = existing.imageUrl,
                            imageUri = null,
                            isPreviewMode = false,
                            wordCount = words,
                            readingTimeMinutes = calculateReadingTime(words),
                            isSaved = false,
                            error = null
                        )
                    }
                    return@launch
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    noteId = null,
                    subjects = subjects,
                    subject = if (it.subject.isBlank()) targetSubject else it.subject,
                    title = "",
                    content = "",
                    tags = emptyList(),
                    imageUrl = null,
                    imageUri = null,
                    isPreviewMode = false,
                    wordCount = 0,
                    readingTimeMinutes = 0,
                    isSaved = false,
                    error = null
                )
            }
        }
    }

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.Init -> {
                loadInitial(event.noteId, event.subject ?: "")
            }
            is NoteEditorEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.title) }
            }
            is NoteEditorEvent.ContentChanged -> {
                val words = calculateWordCount(event.content)
                _uiState.update {
                    it.copy(
                        content = event.content,
                        wordCount = words,
                        readingTimeMinutes = calculateReadingTime(words)
                    )
                }
            }
            is NoteEditorEvent.SubjectChanged -> {
                _uiState.update { it.copy(subject = event.subject) }
            }
            is NoteEditorEvent.TagToggled -> toggleTag(event.tag)
            is NoteEditorEvent.CustomTagAdded -> addTag(event.tag)
            is NoteEditorEvent.CustomTagRemoved -> removeTag(event.tag)
            is NoteEditorEvent.ImageSelected -> {
                _uiState.update { it.copy(imageUri = event.uri) }
            }
            is NoteEditorEvent.InsertFormatting -> insertFormatting(event.prefix, event.suffix)
            is NoteEditorEvent.TogglePreview -> {
                _uiState.update { it.copy(isPreviewMode = event.isPreview) }
            }
            is NoteEditorEvent.TaskCheckToggled -> {
                val updatedContent = MarkdownParser.toggleTaskCheckbox(
                    rawText = _uiState.value.content,
                    targetIndex = event.taskIndex,
                    isChecked = event.isChecked
                )
                val words = calculateWordCount(updatedContent)
                _uiState.update {
                    it.copy(
                        content = updatedContent,
                        wordCount = words,
                        readingTimeMinutes = calculateReadingTime(words)
                    )
                }
            }
            is NoteEditorEvent.SaveClicked -> saveNote()
            is NoteEditorEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
            is NoteEditorEvent.ResetState -> {
                _uiState.update { NoteEditorUiState() }
            }
        }
    }

    private fun insertFormatting(prefix: String, suffix: String) {
        _uiState.update { state ->
            val current = state.content
            val newContent = if (suffix.isEmpty()) {
                if (current.isEmpty() || current.endsWith("\n")) {
                    current + prefix
                } else {
                    current + "\n" + prefix
                }
            } else {
                current + prefix + suffix
            }
            val words = calculateWordCount(newContent)
            state.copy(
                content = newContent,
                wordCount = words,
                readingTimeMinutes = calculateReadingTime(words)
            )
        }
    }

    private fun toggleTag(tag: String) {
        val clean = tag.removePrefix("#").trim()
        if (clean.isBlank()) return
        _uiState.update { state ->
            val exists = state.tags.any { it.equals(clean, ignoreCase = true) }
            val updated = if (exists) {
                state.tags.filterNot { it.equals(clean, ignoreCase = true) }
            } else {
                state.tags + clean
            }
            state.copy(tags = updated)
        }
    }

    private fun addTag(tag: String) {
        val clean = tag.removePrefix("#").trim()
        if (clean.isBlank()) return
        _uiState.update { state ->
            if (state.tags.none { it.equals(clean, ignoreCase = true) }) {
                state.copy(tags = state.tags + clean)
            } else state
        }
    }

    private fun removeTag(tag: String) {
        val clean = tag.removePrefix("#").trim()
        _uiState.update { state ->
            state.copy(tags = state.tags.filterNot { it.equals(clean, ignoreCase = true) })
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state.subject.isBlank()) {
            _uiState.update { it.copy(error = context.getString(R.string.note_error_select_subject)) }
            return
        }
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = context.getString(R.string.note_error_title_required)) }
            return
        }

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(error = context.getString(R.string.note_error_unauthenticated)) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, syncStatus = context.getString(R.string.sync_saving), error = null) }

            if (state.noteId == null) {
                val result = noteRepository.addNote(
                    userId = userId,
                    subject = state.subject,
                    title = state.title,
                    content = state.content,
                    imageUri = state.imageUri,
                    tags = state.tags
                )
                result.fold(
                    onSuccess = { id ->
                        _uiState.update {
                            it.copy(
                                noteId = id,
                                isLoading = false,
                                isSaved = true,
                                syncStatus = context.getString(R.string.sync_saved)
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                syncStatus = null,
                                error = e.message ?: context.getString(R.string.note_error_save_failed)
                            )
                        }
                    }
                )
            } else {
                val existingNotes = noteRepository.getNotes(userId).first()
                val existing = existingNotes.find { it.id == state.noteId }
                val updatedNote = Note(
                    id = state.noteId,
                    userId = userId,
                    subjectId = existing?.subjectId ?: "",
                    subject = state.subject,
                    title = state.title,
                    content = state.content,
                    imageUrl = existing?.imageUrl,
                    tags = state.tags,
                    timestamp = existing?.timestamp ?: Date(),
                    updatedAt = Date(),
                    blocks = MarkdownToBlocksParser.parse(state.content, existing?.imageUrl)
                )

                val result = noteRepository.updateNote(
                    userId = userId,
                    note = updatedNote,
                    imageUri = state.imageUri
                )
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSaved = true,
                                syncStatus = context.getString(R.string.sync_saved)
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                syncStatus = null,
                                error = e.message ?: context.getString(R.string.note_error_save_failed)
                            )
                        }
                    }
                )
            }
        }
    }

    private fun calculateWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    private fun calculateReadingTime(wordCount: Int): Int {
        if (wordCount <= 0) return 0
        return maxOf(1, (wordCount / 200))
    }
}
