package com.notifast.app.presentation.feature.noteeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]
    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = noteId))
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId()
            val subjects = if (userId != null) noteRepository.getSubjects(userId).first() else emptyList()
            if (noteId != null && userId != null) {
                val notes = noteRepository.getNotes(userId).first()
                val note = notes.find { it.id == noteId }
                if (note != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            subject = note.subject,
                            subjects = subjects,
                            title = note.title,
                            blocks = note.blocks.ifEmpty { defaultBlocks() },
                            tags = note.tags
                        )
                    }
                    return@launch
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    subjects = subjects,
                    blocks = defaultBlocks()
                )
            }
        }
    }

    private fun defaultBlocks(): List<NoteBlock> = listOf(
        NoteBlock.Paragraph(id = UUID.randomUUID().toString(), text = "")
    )

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.title) }
                scheduleAutoSave()
            }
            is NoteEditorEvent.SubjectChanged -> {
                _uiState.update { it.copy(subject = event.subject) }
                scheduleAutoSave()
            }
            is NoteEditorEvent.TagToggled -> toggleTag(event.tag)
            is NoteEditorEvent.CustomTagAdded -> addTag(event.tag)
            is NoteEditorEvent.CustomTagRemoved -> removeTag(event.tag)

            is NoteEditorEvent.BlockTextChanged -> updateBlock(event.blockId) { block ->
                when (block) {
                    is NoteBlock.Heading -> block.copy(text = event.text)
                    is NoteBlock.Paragraph -> block.copy(text = event.text)
                    is NoteBlock.Quote -> block.copy(text = event.text)
                    else -> block
                }
            }
            is NoteEditorEvent.BlockSpansChanged -> updateBlock(event.blockId) { block ->
                when (block) {
                    is NoteBlock.Heading -> block.copy(spans = event.spans)
                    is NoteBlock.Paragraph -> block.copy(spans = event.spans)
                    is NoteBlock.Quote -> block.copy(spans = event.spans)
                    else -> block
                }
            }
            is NoteEditorEvent.BlockFocused -> _uiState.update { it.copy(focusedBlockId = event.blockId) }
            is NoteEditorEvent.BlockMoved -> moveBlock(event.fromIndex, event.toIndex)
            is NoteEditorEvent.BlockDeleted -> deleteBlock(event.blockId)
            is NoteEditorEvent.BlockInserted -> insertBlock(event.afterBlockId, event.type)
            is NoteEditorEvent.BlockTypeChanged -> changeBlockType(event.blockId, event.newType)

            is NoteEditorEvent.CheckItemToggled -> updateChecklistItem(event.blockId, event.itemIndex) { item ->
                item.copy(isChecked = !item.isChecked)
            }
            is NoteEditorEvent.CheckItemTextChanged -> updateChecklistItem(event.blockId, event.itemIndex) { item ->
                item.copy(text = event.text)
            }
            is NoteEditorEvent.CheckItemAdded -> updateBlock(event.blockId) { block ->
                if (block is NoteBlock.Checklist) {
                    block.copy(items = block.items + CheckItem(text = ""))
                } else block
            }
            is NoteEditorEvent.CheckItemRemoved -> updateBlock(event.blockId) { block ->
                if (block is NoteBlock.Checklist) {
                    block.copy(items = block.items.filterIndexed { i, _ -> i != event.itemIndex })
                } else block
            }

            is NoteEditorEvent.ImageBlockAdded -> insertImageBlock(event.afterBlockId, event.path)
            is NoteEditorEvent.ImageBlockRemoved -> deleteBlock(event.blockId)

            is NoteEditorEvent.MathSourceChanged -> updateBlock(event.blockId) { block ->
                if (block is NoteBlock.MathBlock) block.copy(source = event.source) else block
            }

            is NoteEditorEvent.SaveClicked -> saveNote()
            is NoteEditorEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is NoteEditorEvent.ResetState -> _uiState.update { NoteEditorUiState(noteId = noteId) }
        }
    }

    private fun toggleTag(tag: String) {
        val clean = tag.trim()
        if (clean.isBlank()) return
        _uiState.update { state ->
            val newTags = if (state.tags.contains(clean)) {
                state.tags.filterNot { it == clean }
            } else {
                state.tags + clean
            }
            state.copy(tags = newTags)
        }
        scheduleAutoSave()
    }

    private fun addTag(tag: String) {
        val clean = tag.trim()
        if (clean.isBlank()) return
        _uiState.update { state ->
            if (state.tags.contains(clean)) state
            else state.copy(tags = state.tags + clean)
        }
        scheduleAutoSave()
    }

    private fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags.filterNot { t -> t == tag }) }
        scheduleAutoSave()
    }

    private fun updateBlock(blockId: String, transform: (NoteBlock) -> NoteBlock) {
        _uiState.update { state ->
            state.copy(blocks = state.blocks.map { if (it.id == blockId) transform(it) else it })
        }
        scheduleAutoSave()
    }

    private fun updateChecklistItem(blockId: String, itemIndex: Int, transform: (CheckItem) -> CheckItem) {
        updateBlock(blockId) { block ->
            if (block is NoteBlock.Checklist) {
                block.copy(items = block.items.mapIndexed { i, item -> if (i == itemIndex) transform(item) else item })
            } else block
        }
    }

    private fun moveBlock(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutable = state.blocks.toMutableList()
            if (fromIndex in mutable.indices && toIndex in mutable.indices) {
                val item = mutable.removeAt(fromIndex)
                mutable.add(toIndex, item)
            }
            state.copy(blocks = mutable)
        }
        scheduleAutoSave()
    }

    private fun deleteBlock(blockId: String) {
        _uiState.update { state ->
            val remaining = state.blocks.filterNot { it.id == blockId }
            state.copy(
                blocks = remaining.ifEmpty { defaultBlocks() },
                focusedBlockId = if (state.focusedBlockId == blockId) null else state.focusedBlockId
            )
        }
        scheduleAutoSave()
    }

    private fun insertBlock(afterBlockId: String?, type: BlockType) {
        val newBlock = createBlock(type)
        _uiState.update { state ->
            val mutable = state.blocks.toMutableList()
            val index = if (afterBlockId == null) {
                mutable.size
            } else {
                mutable.indexOfFirst { it.id == afterBlockId }.let { if (it == -1) mutable.size else it + 1 }
            }
            mutable.add(index, newBlock)
            state.copy(blocks = mutable, focusedBlockId = newBlock.id)
        }
        scheduleAutoSave()
    }

    private fun insertImageBlock(afterBlockId: String?, path: String) {
        val newBlock = NoteBlock.Image(id = UUID.randomUUID().toString(), path = path)
        _uiState.update { state ->
            val mutable = state.blocks.toMutableList()
            val index = if (afterBlockId == null) {
                mutable.size
            } else {
                mutable.indexOfFirst { it.id == afterBlockId }.let { if (it == -1) mutable.size else it + 1 }
            }
            mutable.add(index, newBlock)
            state.copy(blocks = mutable)
        }
        scheduleAutoSave()
    }

    private fun changeBlockType(blockId: String, newType: BlockType) {
        updateBlock(blockId) { block ->
            val text = when (block) {
                is NoteBlock.Heading -> block.text
                is NoteBlock.Paragraph -> block.text
                is NoteBlock.Quote -> block.text
                is NoteBlock.BulletList -> block.items.firstOrNull()?.text ?: ""
                is NoteBlock.Checklist -> block.items.firstOrNull()?.text ?: ""
                else -> ""
            }
            createBlock(newType, id = blockId, text = text)
        }
    }

    private fun createBlock(type: BlockType, id: String = UUID.randomUUID().toString(), text: String = ""): NoteBlock {
        return when (type) {
            BlockType.HEADING -> NoteBlock.Heading(id = id, text = text, level = 1)
            BlockType.PARAGRAPH -> NoteBlock.Paragraph(id = id, text = text)
            BlockType.BULLET_LIST -> NoteBlock.BulletList(id = id, items = listOf(ListItem(text = text)))
            BlockType.CHECKLIST -> NoteBlock.Checklist(id = id, items = listOf(CheckItem(text = text)))
            BlockType.QUOTE -> NoteBlock.Quote(id = id, text = text)
            BlockType.IMAGE -> NoteBlock.Image(id = id, path = text)
            BlockType.MATH -> NoteBlock.MathBlock(id = id, source = text)
            BlockType.DIVIDER -> NoteBlock.Divider(id = id)
        }
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1000)
            persistNote()
        }
    }

    private fun saveNote() {
        saveJob?.cancel()
        viewModelScope.launch { persistNote(exitAfter = true) }
    }

    private suspend fun persistNote(exitAfter: Boolean = false) {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }
        if (state.subject.isBlank()) {
            if (exitAfter) _uiState.update { it.copy(error = "Selecciona una materia") }
            return
        }
        if (state.title.isBlank()) {
            if (exitAfter) _uiState.update { it.copy(error = "El título es obligatorio") }
            return
        }

        _uiState.update { it.copy(syncStatus = "Guardando...", error = null) }

        if (state.noteId == null) {
            noteRepository.addNoteWithBlocks(
                userId = userId,
                subject = state.subject,
                title = state.title,
                blocks = state.blocks,
                tags = state.tags
            ).fold(
                onSuccess = { id ->
                    _uiState.update {
                        it.copy(
                            noteId = id,
                            isSaved = exitAfter,
                            syncStatus = if (exitAfter) null else "Guardado"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(syncStatus = null, error = e.message ?: "Error al guardar")
                    }
                }
            )
        } else {
            val note = Note(
                id = state.noteId,
                userId = userId,
                subject = state.subject,
                title = state.title,
                content = "",
                tags = state.tags,
                blocks = state.blocks
            )
            noteRepository.updateNote(userId, note, imageUri = null).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaved = exitAfter,
                            syncStatus = if (exitAfter) null else "Guardado"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(syncStatus = null, error = e.message ?: "Error al guardar")
                    }
                }
            )
        }
    }
}
