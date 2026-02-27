package com.example.notifast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.model.Note
import com.example.notifast.repository.NotesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NotesRepository = NotesRepository()) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    private var notesJob: Job? = null

    init {
        loadNotes()
    }

    fun loadNotes() {
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.getNotes().collect { notes ->
                _notes.value = notes
            }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            _syncMessage.value = "Guardando nota..."
            repository.addNote(title, content)
            _syncMessage.value = "Sincronizado"
            kotlinx.coroutines.delay(1000)
            _syncMessage.value = null
        }
    }

    fun updateNote(noteId: String, title: String, content: String) {
        // ACTUALIZACIÓN OPTIMISTA: 
        // 1. Actualizamos la nota en la lista local inmediatamente.
        val currentList = _notes.value
        _notes.value = currentList.map { 
            if (it.id == noteId) it.copy(title = title, content = content) else it 
        }

        _syncMessage.value = "Actualizando nota..."

        // 2. Sincronizamos con Firebase
        viewModelScope.launch {
            val result = repository.updateNote(noteId, title, content)
            if (result.isSuccess) {
                _syncMessage.value = "Actualizado"
            } else {
                _syncMessage.value = "Error al sincronizar"
                loadNotes() // Restauramos si falla
            }
            kotlinx.coroutines.delay(1000)
            _syncMessage.value = null
        }
    }

    fun deleteNote(noteId: String) {
        val currentList = _notes.value
        _notes.value = currentList.filter { it.id != noteId }
        
        _syncMessage.value = "Borrando nota..."

        viewModelScope.launch {
            val result = repository.deleteNote(noteId)
            if (result.isSuccess) {
                _syncMessage.value = "Eliminado"
            } else {
                _syncMessage.value = "Error al borrar"
                loadNotes() 
            }
            kotlinx.coroutines.delay(1000)
            _syncMessage.value = null
        }
    }
}
