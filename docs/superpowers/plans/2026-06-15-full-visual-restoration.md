# Full Visual Restoration + NotesScreen Enhancement

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply `NotiFastStudyBackground` to all 11 screens and restore the original NotesScreen experience (NoteEstheticCard, NoteDetailExpandable, SubjectFeaturedCard, custom filters, animated transitions) adapted to the current MVVM architecture.

**Architecture:** All changes are in `presentation/` layer. NotesViewModel gets `rawNotes` + `updateNote`. NotesScreen gets full rewrite with expandable detail view. All other screens get a `Box { NotiFastStudyBackground(); Scaffold(...) }` wrapper. No changes to data layer, DI, NavGraph, or domain models.

**Tech Stack:** Jetpack Compose, Material3, Coil, Firestore

---

### Task 1: Add `rawNotes` and `UpdateNote` to NotesContract

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesContract.kt`

- [ ] **Step 1: Add `rawNotes` field and `UpdateNote` event**

Edit `NotesContract.kt` — add `rawNotes` field and `UpdateNote` event:

```kotlin
package com.example.notifast.presentation.feature.notes

import com.example.notifast.data.model.Note

enum class NoteFilter { RECIENTES, ANTIGUAS, POR_MATERIA }

data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: Map<String, List<Note>> = emptyMap(),
    val rawNotes: List<Note> = emptyList(),
    val subjects: List<String> = emptyList(),
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
    data class UpdateNote(val note: Note, val title: String, val content: String) : NotesEvent
    data object ClearSyncMessage : NotesEvent
}
```

- [ ] **Step 2: Build check**

Run: `./gradlew assembleDebug`

---

### Task 2: Update NotesViewModel — rawNotes + updateNote

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesViewModel.kt`

- [ ] **Step 1: Add `rawNotes` to combine and `updateNote` method**

Edit `NotesViewModel.kt`:
- Pass `rawNotes = notes` in the combine's state constructor
- Add `UpdateNote` case in `onEvent`
- Add `updateNote` private method

Full file (replace existing):

```kotlin
package com.example.notifast.presentation.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.data.model.Note
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(NoteFilter.RECIENTES)
    private val _searchQuery = MutableStateFlow("")

    init {
        loadNotes()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(uid).onSuccess { user ->
                _uiState.update { it.copy(
                    userFirstName = user.firstName,
                    userLastName = user.lastName,
                    userEmail = user.email,
                    userProfilePictureUrl = user.profilePictureUrl
                ) }
            }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            combine(
                noteRepository.getNotes(userId),
                noteRepository.getSubjects(userId),
                _currentFilter,
                _searchQuery
            ) { notes, subjects, filter, query ->
                val filtered = filterNotes(notes, filter, query)
                NotesUiState(notes = filtered, rawNotes = notes, subjects = subjects, currentFilter = filter, searchQuery = query)
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun filterNotes(notes: List<Note>, filter: NoteFilter, query: String): Map<String, List<Note>> {
        var filtered = notes
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }
        return when (filter) {
            NoteFilter.RECIENTES -> filtered.sortedByDescending { it.timestamp }.groupBy { it.subject }
            NoteFilter.ANTIGUAS -> filtered.sortedBy { it.timestamp ?: java.util.Date() }.groupBy { it.subject }
            NoteFilter.POR_MATERIA -> filtered.groupBy { it.subject }
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.LoadNotes -> loadNotes()
            is NotesEvent.SetFilter -> _currentFilter.value = event.filter
            is NotesEvent.Search -> _searchQuery.value = event.query
            is NotesEvent.ClearSyncMessage -> _uiState.update { it.copy(syncMessage = null) }
            is NotesEvent.DeleteNote -> deleteNote(event.note)
            is NotesEvent.UpdateNote -> updateNote(event.note, event.title, event.content)
        }
    }

    private fun updateNote(note: Note, title: String, content: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            noteRepository.updateNote(userId, note.copy(title = title, content = content), null)
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(syncMessage = "Eliminando...") }
            noteRepository.deleteNote(userId, note).fold(
                onSuccess = { _uiState.update { it.copy(syncMessage = null) } },
                onFailure = { _uiState.update { it.copy(syncMessage = "Error al eliminar") } }
            )
        }
    }
}
```

- [ ] **Step 2: Build check**

Run: `./gradlew assembleDebug`

---

### Task 3: Create NoteEstheticCard component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/NoteEstheticCard.kt`
- Delete: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/NoteCard.kt`

- [ ] **Step 1: Create `NoteEstheticCard.kt`**

```kotlin
package com.example.notifast.presentation.feature.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@Composable
fun NoteEstheticCard(note: Note, isEven: Boolean, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val baseColor = if (isEven) Ruby else Aqua
    val subjectColor = if (isEven) Aqua else Ruby
    Box(modifier = Modifier.width(373.dp).height(208.dp).padding(vertical = 8.dp).clickable(interactionSource = interactionSource, indication = null) { onClick() }) {
        Card(modifier = Modifier.width(363.dp).height(171.dp).align(if (isEven) Alignment.BottomEnd else Alignment.BottomStart), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = baseColor)) {
            Column(modifier = Modifier.fillMaxSize().padding(start = if (isEven) 185.dp else 24.dp, end = if (isEven) 24.dp else 185.dp, top = 20.dp, bottom = 12.dp)) {
                Text(text = note.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = note.content, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                }
            }
        }
        Card(modifier = Modifier.width(174.dp).height(202.dp).align(if (isEven) Alignment.TopStart else Alignment.TopEnd), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = subjectColor), elevation = CardDefaults.cardElevation(10.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp).clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.15f))) {
                    if (note.imageUrl != null) SubcomposeAsyncImage(model = note.imageUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(44.dp), tint = Color.White.copy(alpha = 0.3f))
                }
                Text(text = note.subject, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
```

- [ ] **Step 2: Delete `NoteCard.kt`**

Run: `rm app/src/main/java/com/example/notifast/presentation/feature/notes/components/NoteCard.kt`

- [ ] **Step 3: Build check**

Run: `./gradlew assembleDebug`

---

### Task 4: Create SubjectFeaturedCard component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectFeaturedCard.kt`

- [ ] **Step 1: Create `SubjectFeaturedCard.kt`**

```kotlin
package com.example.notifast.presentation.feature.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.theme.color.Ruby

@Composable
fun SubjectFeaturedCard(subjectName: String, notes: List<Note>, onNoteClick: (Note) -> Unit, onSubjectClick: () -> Unit) {
    Card(modifier = Modifier.width(280.dp).height(220.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().height(120.dp).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        val note = notes.getOrNull(index)
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = 0.2f)).clickable { note?.let { onNoteClick(it) } }) {
                            if (note?.imageUrl != null) SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(20.dp), tint = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = subjectName, modifier = Modifier.fillMaxWidth().clickable { onSubjectClick() }, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ruby)
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${notes.size} notas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = "ver todas", modifier = Modifier.clickable { onSubjectClick() }, style = MaterialTheme.typography.bodySmall, color = Ruby, fontWeight = FontWeight.Bold)
                }
            }
            Surface(modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp).size(56.dp).clickable { onSubjectClick() }, shape = CircleShape, color = Ruby, shadowElevation = 6.dp) {
                Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) }
            }
        }
    }
}
```

- [ ] **Step 2: Build check**

Run: `./gradlew assembleDebug`

---

### Task 5: Rewrite NotesScreen

**Files:**
- Rewrite: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt`

- [ ] **Step 1: Rewrite `NotesScreen.kt`**

Full replacement with:
- `NotiFastStudyBackground` behind Scaffold
- `AnimatedVisibility` for header ↔ detail transition (fade 400/200 for header; fade + spring scale for detail)
- `expandedNote` state + `BackHandler`
- `NoteEstheticCard` with alternating even/odd
- `NoteDetailExpandable` (inline — defined as private composable at bottom of file)
- `SubjectFeaturedCard` + "Materias" section
- Custom filter chips (Surface with Ruby selected)
- Improved empty state with "Crear mi primera Materia" button

```kotlin
package com.example.notifast.presentation.feature.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.feature.notes.components.NoteEstheticCard
import com.example.notifast.presentation.feature.notes.components.SubjectFeaturedCard
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onAddNoteClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var expandedNote by remember { mutableStateOf<Note?>(null) }
    val isHeaderVisible = expandedNote == null

    BackHandler(enabled = !isHeaderVisible) { expandedNote = null }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(200))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            HeaderProfile(
                                firstName = uiState.userFirstName,
                                lastName = uiState.userLastName,
                                email = uiState.userEmail,
                                profilePictureUrl = uiState.userProfilePictureUrl,
                                onEditProfile = onEditProfile,
                                onAddSubject = { onSubjectClick("") },
                                onAddNote = onAddNoteClick,
                                onLogout = onLogout
                            )
                        }

                        item {
                            Text(
                                text = "Mis Notas",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = Color.White.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Aqua, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (uiState.searchQuery.isEmpty()) {
                                            Text("Buscar apuntes o materias...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.6f))
                                        }
                                        BasicTextField(
                                            value = uiState.searchQuery,
                                            onValueChange = { viewModel.onEvent(NotesEvent.Search(it)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            cursorBrush = SolidColor(Aqua),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onEvent(NotesEvent.Search("")) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NoteFilter.entries.forEach { filter ->
                                    val label = when (filter) { NoteFilter.RECIENTES -> "Recientes"; NoteFilter.ANTIGUAS -> "Más Antiguas"; NoteFilter.POR_MATERIA -> "Por Materia" }
                                    val selected = uiState.currentFilter == filter
                                    item {
                                        Surface(
                                            modifier = Modifier.clickable { viewModel.onEvent(NotesEvent.SetFilter(filter)) },
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (selected) Ruby else Color.Transparent,
                                            border = if (selected) null else BorderStroke(1.dp, Color.LightGray)
                                        ) {
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selected) Color.White else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.isLoading) {
                            item { NotiFastCircularLoading() }
                        } else if (uiState.notes.isEmpty() && uiState.searchQuery.isEmpty() && uiState.rawNotes.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Aqua.copy(alpha = 0.15f)) {
                                            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.padding(24.dp).fillMaxSize(), tint = Ruby)
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text("Comienza a organizar tus apuntes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Button(onClick = { onSubjectClick("") }, colors = ButtonDefaults.buttonColors(containerColor = Aqua)) {
                                            Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Crear mi primera Materia")
                                        }
                                    }
                                }
                            }
                        } else if (uiState.notes.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No hay coincidencias", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            var notesOffset = 0
                            uiState.notes.forEach { (header, notesList) ->
                                val listToShow = if (uiState.currentFilter == NoteFilter.RECIENTES) notesList.take(3) else notesList

                                if (uiState.currentFilter == NoteFilter.POR_MATERIA && listToShow.isNotEmpty()) {
                                    item(key = "header_$header") {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).clickable { onSubjectClick(header) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = Aqua.copy(alpha = 0.1f)
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Aqua, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = header.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = Aqua, letterSpacing = 1.sp)
                                            }
                                        }
                                    }
                                }

                                val currentOffset = notesOffset
                                itemsIndexed(listToShow, key = { _, note -> note.id }) { index, note ->
                                    NoteEstheticCard(
                                        note = note,
                                        isEven = (currentOffset + index) % 2 == 0,
                                        onClick = { expandedNote = note },
                                        onEdit = { expandedNote = note },
                                        onDelete = { viewModel.onEvent(NotesEvent.DeleteNote(note)) }
                                    )
                                }
                                notesOffset += listToShow.size
                            }

                            if (uiState.searchQuery.isEmpty() && uiState.rawNotes.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Materias",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 16.dp)
                                    )
                                }
                                item {
                                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                        val subjectsMap = uiState.rawNotes.groupBy { it.subject }
                                        subjectsMap.forEach { (subjectName, notes) ->
                                            item(key = "subject_feat_$subjectName") {
                                                SubjectFeaturedCard(
                                                    subjectName = subjectName,
                                                    notes = notes,
                                                    onNoteClick = { expandedNote = it },
                                                    onSubjectClick = { onSubjectClick(subjectName) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !isHeaderVisible,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)),
                    exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = 300f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp), contentAlignment = Alignment.Center) {
                        expandedNote?.let { note ->
                            NoteDetailExpandable(
                                note = note,
                                onSave = { title, content -> viewModel.onEvent(NotesEvent.UpdateNote(note, title, content)) },
                                onClose = { expandedNote = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteDetailExpandable(note: Note, onSave: (String, String) -> Unit, onClose: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                        onClick = { if (isEditing) { onSave(title, content) }; isEditing = !isEditing },
                        shape = CircleShape, color = if (isEditing) Aqua else Ruby
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Aqua.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                        onClick = { if (isEditing) { onSave(title, content) }; isEditing = !isEditing },
                        shape = CircleShape, color = if (isEditing) Aqua else Ruby
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (isEditing) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Contenido") }, modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby))
                } else {
                    Text(text = note.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = note.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ruby.copy(alpha = 0.08f), contentColor = Ruby)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar lectura", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun HeaderProfile(firstName: String, lastName: String, email: String, profilePictureUrl: String?, onEditProfile: () -> Unit, onAddSubject: () -> Unit, onAddNote: () -> Unit, onLogout: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onEditProfile() }, contentAlignment = Alignment.Center) {
                if (profilePictureUrl != null) SubcomposeAsyncImage(model = profilePictureUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val displayName = if (firstName.isNotBlank()) "$firstName $lastName" else email.split("@").getOrElse(0) { "Usuario" }
                Text(text = "Hola, $displayName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp).clickable { onLogout() }, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onAddSubject, shape = RoundedCornerShape(14.dp), color = Aqua, modifier = Modifier.size(width = 46.dp, height = 46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) } }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(onClick = onAddNote, shape = RoundedCornerShape(14.dp), color = Ruby, modifier = Modifier.size(width = 46.dp, height = 46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp)) } }
        }
    }
}
```

- [ ] **Step 2: Build check**

Run: `./gradlew assembleDebug`

---

### Task 6: Add StudyBackground to all other screens (batch)

**Files to modify (10 files):**
Each file gets `Box(Modifier.fillMaxSize()) { NotiFastStudyBackground(); [existing Scaffold] }` wrapper.

Screens with `Scaffold(containerColor = Color.Transparent, ...)` → wrap in `Box { NotiFastStudyBackground(); Scaffold(...) }`:
1. `app/src/main/java/com/example/notifast/presentation/feature/settings/SettingsScreen.kt`
2. `app/src/main/java/com/example/notifast/presentation/feature/grades/GradesScreen.kt`
3. `app/src/main/java/com/example/notifast/presentation/feature/schedules/SchedulesScreen.kt`
4. `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteScreen.kt`
5. `app/src/main/java/com/example/notifast/presentation/feature/editnote/EditNoteScreen.kt`
6. `app/src/main/java/com/example/notifast/presentation/feature/addevaluation/AddEvaluationScreen.kt`
7. `app/src/main/java/com/example/notifast/presentation/feature/editevaluation/EditEvaluationScreen.kt`
8. `app/src/main/java/com/example/notifast/presentation/feature/editprofile/EditProfileScreen.kt`
9. `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt`

Screen with direct `Box(Modifier.fillMaxSize().safeDrawingPadding())` (no Scaffold):
10. `app/src/main/java/com/example/notifast/presentation/feature/calendar/CalendarScreen.kt`

- [ ] **Step 1: For Scaffold-based screens (1-9)**

Pattern for each file:
Add import: `import com.example.notifast.presentation.atoms.NotiFastStudyBackground`
Then wrap: `Scaffold(...)` → `Box(modifier = Modifier.fillMaxSize()) { NotiFastStudyBackground(); Scaffold(...) }`

Example for SettingsScreen (line 53):
```kotlin
// Before:
    Scaffold(
        containerColor = Color.Transparent,
        topBar = { NotiFastCenterAlignedTopBar(title = "Ajustes") }
    ) { padding ->
        Column(...)

// After:
    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { NotiFastCenterAlignedTopBar(title = "Ajustes") }
        ) { padding ->
            Column(...)
```

Apply the exact same pattern to all 9 screens.

- [ ] **Step 2: For CalendarScreen (10)**

Add import: `import com.example.notifast.presentation.atoms.NotiFastStudyBackground`

```kotlin
// Before:
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(...)

// After:
    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(...)
```

- [ ] **Step 3: Build check**

Run: `./gradlew assembleDebug`

---

### Task 7: Final build verification

- [ ] **Step 1: Run full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Summary**

Run: `git diff --stat` to confirm all changed files
