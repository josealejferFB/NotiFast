package com.example.notifast.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.model.Evaluation
import com.example.notifast.model.Note
import com.example.notifast.repository.NotesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

enum class NoteFilter {
    RECIENTES, ANTIGUAS, POR_MATERIA
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotesRepository(application.applicationContext)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val allNotes = _notes.asStateFlow()
    
    private val _evaluations = MutableStateFlow<List<Evaluation>>(emptyList())
    val evaluations = _evaluations.asStateFlow()

    private val _currentFilter = MutableStateFlow(NoteFilter.RECIENTES)
    val currentFilter = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    /**
     * LÓGICA DE FILTRADO PARA LA SECCIÓN "MIS NOTAS"
     */
    val filteredNotes: StateFlow<Map<String, List<Note>>> = combine(_notes, _currentFilter, _searchQuery) { notes, filter, query ->
        if (notes.isEmpty()) {
            emptyMap()
        } else {
            val searchedNotes = if (query.isBlank()) {
                notes
            } else {
                notes.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.subject.contains(query, ignoreCase = true) 
                }
            }

            if (searchedNotes.isEmpty() && query.isNotBlank()) {
                emptyMap()
            } else {
                when (filter) {
                    NoteFilter.RECIENTES -> mapOf("Recientes" to searchedNotes.sortedByDescending { it.timestamp })
                    NoteFilter.ANTIGUAS -> mapOf("Más Antiguas" to searchedNotes.sortedBy { it.timestamp })
                    NoteFilter.POR_MATERIA -> searchedNotes.groupBy { it.subject.ifBlank { "SIN MATERIA" } }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    /**
     * LÓGICA PARA LA SECCIÓN "MATERIAS" (Siempre agrupado)
     */
    val allNotesGroupedBySubject: StateFlow<Map<String, List<Note>>> = _notes
        .map { list -> list.groupBy { it.subject.ifBlank { "SIN MATERIA" } } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects = _subjects.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    private var notesJob: Job? = null
    private var subjectsJob: Job? = null
    private var evaluationsJob: Job? = null

    init {
        loadNotes()
        loadSubjects()
        loadEvaluations()
    }

    fun setFilter(filter: NoteFilter) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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

    fun loadEvaluations() {
        evaluationsJob?.cancel()
        evaluationsJob = viewModelScope.launch {
            repository.getEvaluations().collect { list ->
                _evaluations.value = list
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

    fun addEvaluation(
        subjectName: String,
        title: String,
        description: String,
        date: Date,
        noteIds: List<String>,
        quickTips: String,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _syncMessage.value = "Guardando evaluación..."
            var imageUrl: String? = null
            imageUri?.let {
                val uploadResult = repository.uploadImage(it)
                imageUrl = uploadResult.getOrNull()
            }
            repository.addEvaluation(subjectName, title, description, date, noteIds, quickTips, imageUrl)
            _syncMessage.value = "Evaluación guardada"
            delay(1000)
            _syncMessage.value = null
        }
    }

    fun updateEvaluation(
        evaluationId: String,
        title: String,
        description: String,
        date: Date,
        quickTips: String,
        imageUri: Uri? = null,
        existingImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _syncMessage.value = "Actualizando evaluación..."
            var imageUrl = existingImageUrl
            imageUri?.let {
                val uploadResult = repository.uploadImage(it)
                imageUrl = uploadResult.getOrNull()
            }
            val result = repository.updateEvaluation(evaluationId, title, description, date, quickTips, imageUrl)
            if (result.isSuccess) {
                _syncMessage.value = "Actualizado"
            } else {
                _syncMessage.value = "Error al actualizar"
            }
            delay(1000)
            _syncMessage.value = null
        }
    }

    fun deleteEvaluation(eval: Evaluation) {
        viewModelScope.launch {
            _syncMessage.value = "Borrando evaluación..."
            val result = repository.deleteEvaluation(eval.id, eval.imageUrl)
            if (result.isSuccess) {
                _syncMessage.value = "Eliminado"
            } else {
                _syncMessage.value = "Error al borrar"
            }
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
