# Delete Subject Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add long-press context menu to delete a subject and all its notes from SubjectCard

**Architecture:** Follows existing `deleteNote` pattern — new event in Contract, method in ViewModel, UI in SubjectCard + NotesScreen. Repository layer already implemented.

**Tech Stack:** Kotlin, Jetpack Compose, Firebase Firestore, Hilt

---

### Task 1: Add DeleteSubject event

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesContract.kt`

- [ ] **Step 1: Add event and update NotesUiState**

In `NotesContract.kt`, add `DeleteSubject` to `NotesEvent`:

```kotlin
sealed interface NotesEvent {
    data object LoadNotes : NotesEvent
    data class SetFilter(val filter: NoteFilter) : NotesEvent
    data class Search(val query: String) : NotesEvent
    data class DeleteNote(val note: Note) : NotesEvent
    data class UpdateNote(val note: Note, val title: String, val content: String) : NotesEvent
    data object ClearSyncMessage : NotesEvent
    data class ToggleSubject(val subjectName: String) : NotesEvent
    data class DeleteSubject(val subjectName: String) : NotesEvent
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/NotesContract.kt
git commit -m "feat: add DeleteSubject event to NotesContract"
```

---

### Task 2: Add deleteSubject to NotesViewModel

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesViewModel.kt`

- [ ] **Step 1: Add handler in onEvent and deleteSubject method**

Add to `onEvent`:
```kotlin
is NotesEvent.DeleteSubject -> deleteSubject(event.subjectName)
```

Add method (follows same pattern as existing `deleteNote`):
```kotlin
private fun deleteSubject(subjectName: String) {
    viewModelScope.launch {
        val userId = authRepository.getCurrentUserId() ?: return@launch
        _uiState.update { it.copy(syncMessage = "Eliminando materia...") }
        noteRepository.deleteSubject(userId, subjectName).fold(
            onSuccess = { _uiState.update { it.copy(syncMessage = null) } },
            onFailure = { e -> _uiState.update { it.copy(syncMessage = "Error al eliminar materia") } }
        )
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/NotesViewModel.kt
git commit -m "feat: add deleteSubject to NotesViewModel"
```

---

### Task 3: Add context menu to SubjectCard

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectCard.kt`

- [ ] **Step 1: Add imports**

Add these imports to `SubjectCard.kt`:
```kotlin
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
```

- [ ] **Step 2: Add parameters and long-press logic**

Change SubjectCard signature to add `onDeleteSubject`:
```kotlin
@Composable
fun SubjectCard(
    info: SubjectInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddNote: (String) -> Unit,
    onAddEvaluation: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onDeleteSubject: () -> Unit
) {
```

Add local state for menu visibility and wrap the header Row with `combinedClickable`:

After `val initials = getInitials(info.name)` add:
```kotlin
var showMenu by remember { mutableStateOf(false) }
```

Change the header Row's modifier from:
```kotlin
modifier = Modifier
    .fillMaxWidth()
    .clickable { onToggle() }
    .padding(16.dp),
```

To:
```kotlin
modifier = Modifier
    .fillMaxWidth()
    .combinedClickable(
        onClick = { onToggle() },
        onLongClick = { showMenu = true }
    )
    .padding(16.dp),
```

Add `@OptIn(ExperimentalFoundationApi::class)` to the SubjectCard function.

After the header Row and before `AnimatedVisibility`, add the DropdownMenu:
```kotlin
DropdownMenu(
    expanded = showMenu,
    onDismissRequest = { showMenu = false }
) {
    DropdownMenuItem(
        text = { Text("Eliminar materia", color = Color(0xFFE53935)) },
        onClick = {
            showMenu = false
            onDeleteSubject()
        },
        leadingIcon = {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935))
        }
    )
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectCard.kt
git commit -m "feat: add long-press context menu with delete option to SubjectCard"
```

---

### Task 4: Wire delete subject in NotesScreen

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt`

- [ ] **Step 1: Add dialog state and AlertDialog**

Add imports:
```kotlin
import androidx.compose.material3.AlertDialog
```

Add state variable alongside `expandedNote`:
```kotlin
var subjectToDelete by remember { mutableStateOf<String?>(null) }
```

Add AlertDialog before the closing `}` of the outer Box (before the last `}`), after the Scaffold:
```kotlin
subjectToDelete?.let { name ->
    AlertDialog(
        onDismissRequest = { subjectToDelete = null },
        title = { Text("Eliminar materia") },
        text = { Text("¿Estás seguro de eliminar $name? También se borrarán todos los apuntes asociados.") },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.onEvent(NotesEvent.DeleteSubject(name))
                    subjectToDelete = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) { Text("Eliminar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = { subjectToDelete = null }) { Text("Cancelar") }
        }
    )
}
```

- [ ] **Step 2: Wire onDeleteSubject in SubjectCard items**

In the SubjectCard items block, add `onDeleteSubject`:
```kotlin
items(uiState.subjectInfos, key = { it.name }) { info ->
    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
        SubjectCard(
            info = info,
            isExpanded = uiState.expandedSubject == info.name,
            onToggle = { viewModel.onEvent(NotesEvent.ToggleSubject(info.name)) },
            onAddNote = { onAddNoteClick() },
            onAddEvaluation = { subject -> onAddEvaluationClick(subject) },
            onNoteClick = { note -> expandedNote = note },
            onDeleteSubject = { subjectToDelete = info.name }
        )
    }
}
```

Also make sure the `Button` import is already present (it is, from the existing code).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt
git commit -m "feat: add delete subject confirmation dialog in NotesScreen"
```

---

### Task 5: Verify build

- [ ] **Step 1: Run assembleDebug**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Fix any compilation errors** (iterate until clean)
