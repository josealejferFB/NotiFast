package com.example.notifast.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.model.Note
import com.example.notifast.repository.NotesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NoteFilter {
    RECIENTES, ANTIGUAS, POR_MATERIA
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotesRepository(application.applicationContext)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    
    private val _currentFilter = MutableStateFlow(NoteFilter.RECIENTES)
    val currentFilter = _currentFilter.asStateFlow()

    val filteredNotes: StateFlow<Map<String, List<Note>>> = combine(_notes, _currentFilter) { notes, filter ->
        if (notes.isEmpty()) {
            emptyMap<String, List<Note>>() // Aseguramos que devuelva un mapa vacío
        } else {
            when (filter) {
                NoteFilter.RECIENTES -> mapOf("Recientes" to notes.sortedByDescending { it.timestamp })
                NoteFilter.ANTIGUAS -> mapOf("Más Antiguas" to notes.sortedBy { it.timestamp })
                NoteFilter.POR_MATERIA -> notes.groupBy { it.subject.ifBlank { "SIN MATERIA" } }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects = _subjects.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    private var notesJob: Job? = null
    private var subjectsJob: Job? = null

    init {
        loadNotes()
        loadSubjects()
    }

    fun setFilter(filter: NoteFilter) {
        _currentFilter.value = filter
    }

    fun loadNotes() {
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.getNotes().collect { notes ->
                _notes.value = notes
            }
        }
    }

    fun loadSubjects() {
        subjectsJob?.cancel()
        subjectsJob = viewModelScope.launch {
            repository.getSubjects().collect { list ->
                _subjects.value = list
            }
        }
    }

    fun addSubject(name: String) {
        viewModelScope.launch {
            _syncMessage.value = "Creando materia..."
            repository.addSubject(name)
            _syncMessage.value = "Materia creada"
            delay(1000)
            _syncMessage.value = null
        }
    }

    fun addNote(subject: String, title: String, content: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            _syncMessage.value = "Subiendo nota..."
            var imageUrl: String? = null
            imageUri?.let {
                val uploadResult = repository.uploadImage(it)
                imageUrl = uploadResult.getOrNull()
            }
            repository.addNote(subject, title, content, imageUrl)
            _syncMessage.value = "Nota guardada"
            delay(1000)
            _syncMessage.value = null
        }
    }

    fun updateNote(noteId: String, subject: String, title: String, content: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            _syncMessage.value = "Actualizando..."
            var imageUrl: String? = null
            imageUri?.let {
                val uploadResult = repository.uploadImage(it)
                imageUrl = uploadResult.getOrNull()
            }
            val result = repository.updateNote(noteId, subject, title, content, imageUrl)
            if (result.isSuccess) {
                _syncMessage.value = "Actualizado"
            } else {
                _syncMessage.value = "Error"
                loadNotes() 
            }
            delay(1000)
            _syncMessage.value = null
        }
    }

    fun deleteNote(note: Note) {
        val currentList = _notes.value
        _notes.value = currentList.filter { it.id != note.id }
        _syncMessage.value = "Borrando..."
        viewModelScope.launch {
            val result = repository.deleteNote(note)
            if (result.isSuccess) {
                _syncMessage.value = "Eliminado"
            } else {
                _syncMessage.value = "Error"
                loadNotes() 
            }
            delay(1000)
            _syncMessage.value = null
        }
    }
}
