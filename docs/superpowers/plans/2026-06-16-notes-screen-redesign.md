# Notes Screen Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Compress note cards and replace subject section with inline expandable rich cards

**Architecture:** New `SubjectData` model enriches subjects with icons; `NotesViewModel` loads evaluations alongside notes and computes per-subject stats; new `SubjectCard` composable renders compact/expanded states inline in the LazyColumn.

**Tech Stack:** Kotlin, Jetpack Compose, Firebase Firestore, Hilt

---

### Task 1: Create SubjectData model

**Files:**
- Create: `app/src/main/java/com/example/notifast/data/model/Subject.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.notifast.app.data.model

data class SubjectData(
    val name: String,
    val icon: String = "📚"
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/data/model/Subject.kt
git commit -m "feat: add SubjectData model with name and icon"
```

---

### Task 2: Update NoteRepository interface

**Files:**
- Modify: `app/src/main/java/com/example/notifast/domain/repository/NoteRepository.kt`

- [ ] **Step 1: Change getSubjects return type and addSubject signature**

```kotlin
package com.notifast.app.domain.repository

import android.net.Uri
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(userId: String): Flow<List<Note>>
    fun getSubjects(userId: String): Flow<List<SubjectData>>
    suspend fun addNote(
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUri: Uri?
    ): Result<Unit>
    suspend fun addSubject(
        userId: String,
        subjectName: String,
        icon: String = "📚"
    ): Result<Unit>
    suspend fun deleteNote(userId: String, note: Note): Result<Unit>
    suspend fun updateNote(userId: String, note: Note, imageUri: Uri?): Result<Unit>
    suspend fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>>
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/domain/repository/NoteRepository.kt
git commit -m "feat: update NoteRepository to use SubjectData and add icon param"
```

---

### Task 3: Update NoteRepositoryImpl

**Files:**
- Modify: `app/src/main/java/com/example/notifast/data/repository/NoteRepositoryImpl.kt`

- [ ] **Step 1: Update getSubjects to return SubjectData**

Change:
```kotlin
override fun getSubjects(userId: String): Flow<List<String>> {
```
to:
```kotlin
override fun getSubjects(userId: String): Flow<List<SubjectData>> {
```

Change the map lambda:
```kotlin
.map { snapshot ->
    snapshot.documents.map { it.getString("name") ?: it.id }
}
```
to:
```kotlin
.map { snapshot ->
    snapshot.documents.map { doc ->
        SubjectData(
            name = doc.getString("name") ?: doc.id,
            icon = doc.getString("icon") ?: "📚"
        )
    }
}
```

Add import:
```kotlin
import com.notifast.app.data.model.SubjectData
```

- [ ] **Step 2: Update addSubject to accept icon**

Change:
```kotlin
override suspend fun addSubject(userId: String, subjectName: String): Result<Unit> {
```
to:
```kotlin
override suspend fun addSubject(userId: String, subjectName: String, icon: String): Result<Unit> {
```

Change the map in addSubject:
```kotlin
.set(mapOf("name" to subjectName, "createdAt" to com.google.firebase.Timestamp.now()))
```
to:
```kotlin
.set(mapOf("name" to subjectName, "icon" to icon, "createdAt" to com.google.firebase.Timestamp.now()))
```

- [ ] **Step 3: Also update the getNotes internal query that reads subjects**

In `getNotes()`, the `flatMapLatest` lambda reads subjects from the snapshot:
```kotlin
val subjects = snapshot.documents.map { it.getString("name") ?: it.id }
```
→ No changes needed here, we still just need names for the per-subject queries.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/notifast/data/repository/NoteRepositoryImpl.kt
git commit -m "feat: update NoteRepositoryImpl to use SubjectData and accept icon"
```

---

### Task 4: Update AddSubjectViewModel

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectContract.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectViewModel.kt`

- [ ] **Step 1: Add icon to AddSubjectUiState**

In `AddSubjectContract.kt`:
```kotlin
data class AddSubjectUiState(
    val subjectName: String = "",
    val selectedIcon: String = "📚",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

- [ ] **Step 2: Update AddSubjectViewModel**

```kotlin
package com.notifast.app.presentation.feature.addsubject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSubjectViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSubjectUiState())
    val uiState: StateFlow<AddSubjectUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddSubjectEvent) {
        when (event) {
            is AddSubjectEvent.NameChanged -> _uiState.update { it.copy(subjectName = event.value) }
            is AddSubjectEvent.IconSelected -> _uiState.update { it.copy(selectedIcon = event.icon) }
            is AddSubjectEvent.SaveClicked -> saveSubject()
        }
    }

    private fun saveSubject() {
        val state = _uiState.value
        if (state.subjectName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }
            noteRepository.addSubject(userId, state.subjectName, state.selectedIcon).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") } }
            )
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectContract.kt app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectViewModel.kt
git commit -m "feat: add icon support to AddSubjectViewModel"
```

---

### Task 5: Add emoji picker to AddSubjectScreen

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectScreen.kt`

- [ ] **Step 1: Add emoji picker grid**

Read the full current file, then add an emoji grid after the name field.

```kotlin
// Add after the subject name OutlinedTextField
Text("Icono", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
Spacer(Modifier.height(8.dp))

val emojis = listOf("📚", "🔬", "📐", "🎨", "⚽", "🌍", "📝", "🧮", "💻", "🧪", "📊", "🎵", "🎭", "🏛️", "🌿", "⚡", "💎", "🎯", "🧩", "🎪")
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 48.dp),
    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
) {
    items(emojis) { emoji ->
        val selected = uiState.selectedIcon == emoji
        Surface(
            onClick = { viewModel.onEvent(AddSubjectEvent.IconSelected(emoji)) },
            shape = CircleShape,
            color = if (selected) Ruby else Color.White.copy(alpha = 0.9f),
            border = if (selected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Text(emoji, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        }
    }
}
Spacer(Modifier.height(16.dp))
```

Add imports:
```kotlin
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import com.notifast.app.presentation.theme.color.Ruby
```

- [ ] **Step 2: Update the AddSubjectEvent to include IconSelected**

In `AddSubjectContract.kt`:
```kotlin
sealed interface AddSubjectEvent {
    data class NameChanged(val value: String) : AddSubjectEvent
    data class IconSelected(val icon: String) : AddSubjectEvent
    data object SaveClicked : AddSubjectEvent
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectScreen.kt app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectContract.kt
git commit -m "feat: add emoji picker to AddSubjectScreen"
```

---

### Task 6: Update AddNote for SubjectData

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteContract.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteViewModel.kt`

- [ ] **Step 1: Change subjects type in AddNoteUiState**

```kotlin
import com.notifast.app.data.model.SubjectData

data class AddNoteUiState(
    val subject: String = "",
    val subjects: List<SubjectData> = emptyList(),
    val title: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
```

- [ ] **Step 2: Update AddNoteViewModel.loadSubjects**

Change from:
```kotlin
noteRepository.getSubjects(userId).collect { subjects ->
    _uiState.update { it.copy(subjects = subjects) }
}
```
to:
```kotlin
noteRepository.getSubjects(userId).collect { subjectDataList ->
    _uiState.update { it.copy(subjects = subjectDataList) }
}
```

- [ ] **Step 3: Update AddNoteScreen subject dropdown to show icons**

In `AddNoteScreen.kt`, update the dropdown items to show icon + name:
```kotlin
uiState.subjects.forEach { subjectData ->
    DropdownMenuItem(
        text = { Text("${subjectData.icon}  ${subjectData.name}") },
        onClick = {
            viewModel.onEvent(AddNoteEvent.SubjectChanged(subjectData.name))
            expanded = false
        }
    )
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteContract.kt app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteViewModel.kt app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteScreen.kt
git commit -m "feat: update AddNote to use SubjectData with icons"
```

---

### Task 7: Add evaluation loading to NotesViewModel

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesContract.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesViewModel.kt`

- [ ] **Step 1: Add new state fields to NotesUiState**

In `NotesContract.kt`, add:
```kotlin
data class SubjectInfo(
    val name: String,
    val icon: String,
    val notes: List<Note>,
    val evaluations: List<Evaluation>,
    val averageGrade: Double?,
    val completedEvalCount: Int,
    val pendingEvalCount: Int,
    val upcomingEvaluations: List<Evaluation>
)
```

Add to `NotesUiState`:
```kotlin
val subjectInfos: List<SubjectInfo> = emptyList(),
val expandedSubject: String? = null,
```

Add import:
```kotlin
import com.notifast.app.data.model.Evaluation
```

Add event:
```kotlin
data class ToggleSubject(val subjectName: String) : NotesEvent
```

- [ ] **Step 2: Inject EvaluationRepository and compute SubjectInfo**

In `NotesViewModel.kt`, add to constructor:
```kotlin
private val evaluationRepository: EvaluationRepository
```

Add import:
```kotlin
import com.notifast.app.domain.repository.EvaluationRepository
```

Update the combine to include evaluations:
```kotlin
combine(
    noteRepository.getNotes(userId),
    noteRepository.getSubjects(userId),
    evaluationRepository.getEvaluations(userId),
    _currentFilter,
    _searchQuery
) { notes, subjects, evaluations, filter, query ->
```

After computing `filtered`, compute `subjectInfos`:
```kotlin
val subjectInfos = subjects.map { subject ->
    val subjectNotes = notes.filter { it.subject == subject.name }
    val subjectEvals = evaluations.filter { it.subjectName == subject.name }
    val graded = subjectEvals.filter { it.grade != null && it.isPresented }
    val weightedSum = graded.sumOf { (it.grade ?: 0.0) * (it.weight ?: 1.0) }
    val totalWeight = graded.sumOf { it.weight ?: 1.0 }
    val avg = if (totalWeight > 0.0) weightedSum / totalWeight else null
    val upcoming = subjectEvals
        .filter { !it.isPresented && it.date != null }
        .sortedBy { it.date }
        .take(3)

    SubjectInfo(
        name = subject.name,
        icon = subject.icon,
        notes = subjectNotes,
        evaluations = subjectEvals,
        averageGrade = avg?.let { kotlin.math.round(it * 10) / 10.0 },
        completedEvalCount = subjectEvals.count { it.isPresented },
        pendingEvalCount = subjectEvals.count { !it.isPresented },
        upcomingEvaluations = upcoming
    )
}
```

Update the Pair creation:
```kotlin
_uiState.value to NotesUiState(
    notes = filtered,
    rawNotes = notes,
    subjects = subjects.map { it.name },
    subjectInfos = subjectInfos,
    currentFilter = filter,
    searchQuery = query
)
```

Update `onEvent`:
```kotlin
is NotesEvent.ToggleSubject -> {
    val current = _uiState.value.expandedSubject
    _uiState.update { it.copy(expandedSubject = if (current == event.subjectName) null else event.subjectName) }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/NotesContract.kt app/src/main/java/com/example/notifast/presentation/feature/notes/NotesViewModel.kt
git commit -m "feat: add evaluation loading and SubjectInfo computation to NotesViewModel"
```

---

### Task 8: Compress NoteEstheticCard

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/NoteEstheticCard.kt`

- [ ] **Step 1: Reduce dimensions**

Change:
```kotlin
Box(modifier = Modifier.width(373.dp).height(208.dp).padding(vertical = 8.dp)...
```
to:
```kotlin
Box(modifier = Modifier.fillMaxWidth().height(148.dp).padding(vertical = 4.dp)...
```

Change main Card:
```kotlin
Card(modifier = Modifier.width(363.dp).height(171.dp)...
```
to:
```kotlin
Card(modifier = Modifier.fillMaxWidth().height(120.dp)...
```

Change overlapping Card:
```kotlin
Card(modifier = Modifier.width(174.dp).height(202.dp)...
```
to:
```kotlin
Card(modifier = Modifier.width(140.dp).height(140.dp)...
```

Change content text to max 1 line:
```kotlin
Text(text = note.content, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/components/NoteEstheticCard.kt
git commit -m "refactor: compress NoteEstheticCard height from 208dp to 148dp"
```

---

### Task 9: Create SubjectCard component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectCard.kt`

- [ ] **Step 1: Create the SubjectCard composable**

```kotlin
package com.notifast.app.presentation.feature.notes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.feature.notes.SubjectInfo
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import java.util.Date

private fun subjectColor(name: String): Color {
    val hash = name.hashCode()
    val hue = ((hash and 0xFF).toFloat() / 255f * 360f)
    return Color.hsl(hue, 0.7f, 0.55f)
}

private fun daysUntil(date: Date): Long {
    val diff = date.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}

private fun urgencyColor(days: Long): Color {
    return when {
        days < 0 -> Color.Gray
        days <= 7 -> Color(0xFFE53935) // red
        days <= 14 -> Color(0xFFFB8C00) // orange
        else -> Color(0xFF43A047) // green
    }
}

private fun starRating(avg: Double, maxStars: Int = 5): String {
    val filled = (avg / 2.0).toInt().coerceIn(0, maxStars)
    val empty = maxStars - filled
    return "★".repeat(filled) + "☆".repeat(empty)
}

@Composable
fun SubjectCard(
    info: SubjectInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddNote: (String) -> Unit,
    onAddEvaluation: (String) -> Unit
) {
    val color = subjectColor(info.name)
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "chevron"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            // Compact header (always visible)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(info.icon, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📝 ${info.notes.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(" · ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("📅 ${info.evaluations.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        if (info.pendingEvalCount > 0) {
                            Text(" · ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("⏳ ${info.pendingEvalCount}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
                if (info.averageGrade != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(starRating(info.averageGrade), color = Color(0xFFFFB300), fontSize = 14.sp)
                        Text(String.format("%.1f", info.averageGrade), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = rotation },
                    tint = Color.Gray
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)) + fadeIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)),
                exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)) + fadeOut(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    // Upcoming evaluations
                    if (info.upcomingEvaluations.isNotEmpty()) {
                        Text("📅 Próximas evaluaciones", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        info.upcomingEvaluations.forEach { eval ->
                            val days = eval.date?.let { daysUntil(it) } ?: 999
                            UrgencyCard(eval = eval, days = days, color = color)
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    // Dashboard: average + stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular score
                        Box(
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = info.averageGrade?.let { String.format("%.1f", it) } ?: "—",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = color
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Promedio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("📝 ${info.evaluations.size} evaluaciones", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("✅ ${info.completedEvalCount} completadas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("⏳ ${info.pendingEvalCount} pendientes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    // Recent notes mini cards
                    if (info.notes.isNotEmpty()) {
                        Text("📝 Últimos apuntes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(info.notes.take(3)) { note ->
                                MiniNoteCard(note = note, color = color)
                            }
                            if (info.notes.size > 3) {
                                item {
                                    Surface(
                                        modifier = Modifier.size(width = 100.dp, height = 80.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = color.copy(alpha = 0.05f),
                                        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("+${info.notes.size - 3}", fontWeight = FontWeight.Bold, color = color)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Quick actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onAddNote(info.name) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apunte", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onAddEvaluation(info.name) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Aqua)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Evaluación", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UrgencyCard(eval: Evaluation, days: Long, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(eval.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (eval.weight != null && eval.weight!! > 0.0) {
                        Text("🏷️ ${(eval.weight!! * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(" · ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text("📆 ${eval.date?.let { formatDate(it) } ?: "Sin fecha"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            val chipColor = urgencyColor(days)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = chipColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = when {
                        days < 0 -> "Vencida"
                        days == 0L -> "Hoy"
                        days == 1L -> "Mañana"
                        else -> "en ${days}días"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = chipColor
                )
            }
        }
    }
}

@Composable
private fun MiniNoteCard(note: Note, color: Color) {
    Surface(
        modifier = Modifier.size(width = 100.dp, height = 80.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(painter = rememberAsyncImagePainter(model = note.imageUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(Modifier.height(4.dp))
            }
            Text(note.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatDate(date: Date): String {
    val cal = java.util.Calendar.getInstance().apply { time = date }
    return "${cal.get(java.util.Calendar.DAY_OF_MONTH)}/${cal.get(java.util.Calendar.MONTH) + 1}"
}
```

Add needed imports:
```kotlin
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material3.HorizontalDivider
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectCard.kt
git commit -m "feat: create SubjectCard with compact/expanded states"
```

---

### Task 10: Update NotesScreen — replace Materias section

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt`
- Remove: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectFeaturedCard.kt`

- [ ] **Step 1: Replace the LazyRow SubjectFeaturedCard section with new SubjectCards**

In the notes list after the notes loop, replace:
```kotlin
if (uiState.searchQuery.isEmpty() && uiState.rawNotes.isNotEmpty()) {
    item {
        Text("Materias", ...)
    }
    item {
        LazyRow(...) {
            val subjectsMap = uiState.rawNotes.groupBy { it.subject }
            subjectsMap.forEach { (subjectName, notes) ->
                item(key = "subject_feat_$subjectName") {
                    SubjectFeaturedCard(...)
                }
            }
        }
    }
}
```

With:
```kotlin
if (uiState.searchQuery.isEmpty() && uiState.subjectInfos.isNotEmpty()) {
    item {
        Text(
            text = "Materias",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 8.dp)
        )
    }
    items(uiState.subjectInfos, key = { it.name }) { info ->
        SubjectCard(
            info = info,
            isExpanded = uiState.expandedSubject == info.name,
            onToggle = { viewModel.onEvent(NotesEvent.ToggleSubject(info.name)) },
            onAddNote = onAddNoteClick,
            onAddEvaluation = { subject -> navController.navigate(Screen.AddEvaluation.createRoute(subject)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
```

Add missing `navController` reference by passing `onAddEvaluationClick` to NotesScreen:
In NavGraph, add `onAddEvaluationClick` parameter to NotesScreen call:
```kotlin
NotesScreen(
    onAddNoteClick = { navController.navigate(Screen.AddNote.createRoute()) },
    onAddSubjectClick = { navController.navigate(Screen.AddSubject.route) },
    onSubjectClick = { subject -> navController.navigate(Screen.SubjectDetail.createRoute(subject)) },
    onAddEvaluationClick = { subject -> navController.navigate(Screen.AddEvaluation.createRoute(subject)) },
    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
    onLogout = { ... }
)
```

Add `onAddEvaluationClick` parameter to NotesScreen composable:
```kotlin
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onAddNoteClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
)
```

- [ ] **Step 2: Delete SubjectFeaturedCard.kt**

```bash
rm app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectFeaturedCard.kt
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt
git rm app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectFeaturedCard.kt
git commit -m "feat: replace SubjectFeaturedCard with new inline expandable SubjectCard"
```

---

### Task 11: Verify build

- [ ] **Step 1: Run assembleDebug**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Fix any compilation errors** (iterate until clean)
