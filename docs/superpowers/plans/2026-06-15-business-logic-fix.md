# NotiFast Business Logic Fix

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix note creation flow (dropdown subjects, pre-fill from SubjectDetail, no-subject validation) and add action buttons to SubjectDetailScreen.

**Architecture:** Changes to AddNote feature (contract, VM, screen), SubjectDetailScreen (buttons), NavGraph (routes with params), Screen.kt (route definitions).

**Tech Stack:** Jetpack Compose, Navigation Compose, Hilt, Firestore

---

### Task 1: Update Screen.kt routes with optional subject params

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/navigation/Screen.kt`

- [ ] **Step 1: Update routes**

Replace `AddNote` and `AddEvaluation` routes to accept optional `subject` argument:

```kotlin
data object AddNote : Screen("add_note?subject={subject}") {
    fun createRoute(subject: String? = null) =
        if (subject != null) "add_note?subject=$subject" else "add_note"
}
data object AddEvaluation : Screen("add_evaluation?subject={subject}") {
    fun createRoute(subject: String? = null) =
        if (subject != null) "add_evaluation?subject=$subject" else "add_evaluation"
}
```

Remove the old `data object AddNote : Screen("add_note")` line entirely.

- [ ] **Step 2: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`

---

### Task 2: Update AddNoteContract — add subjects + SetSubjects

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteContract.kt`

- [ ] **Step 1: Replace file content**

```kotlin
package com.notifast.app.presentation.feature.addnote

import android.net.Uri

data class AddNoteUiState(
    val subject: String = "",
    val subjects: List<String> = emptyList(),
    val title: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface AddNoteEvent {
    data class SubjectChanged(val value: String) : AddNoteEvent
    data class TitleChanged(val value: String) : AddNoteEvent
    data class ContentChanged(val value: String) : AddNoteEvent
    data class ImageSelected(val uri: Uri?) : AddNoteEvent
    data object SaveClicked : AddNoteEvent
    data object ClearError : AddNoteEvent
    data class SetSubjects(val subjects: List<String>) : AddNoteEvent
}
```

- [ ] **Step 2: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`

---

### Task 3: Update AddNoteViewModel — load subjects, accept initial

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteViewModel.kt`

- [ ] **Step 1: Replace file content**

```kotlin
package com.notifast.app.presentation.feature.addnote

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
class AddNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    init { loadSubjects() }

    fun setInitialSubject(subject: String) {
        if (subject.isNotBlank()) _uiState.update { it.copy(subject = subject) }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            noteRepository.getSubjects(userId).collect { subjects ->
                _uiState.update { it.copy(subjects = subjects) }
            }
        }
    }

    fun onEvent(event: AddNoteEvent) {
        when (event) {
            is AddNoteEvent.SubjectChanged -> _uiState.update { it.copy(subject = event.value) }
            is AddNoteEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is AddNoteEvent.ContentChanged -> _uiState.update { it.copy(content = event.value) }
            is AddNoteEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is AddNoteEvent.SaveClicked -> saveNote()
            is AddNoteEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is AddNoteEvent.SetSubjects -> _uiState.update { it.copy(subjects = event.subjects) }
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state.subjects.isEmpty()) {
            _uiState.update { it.copy(error = "Crea una materia primero") }
            return
        }
        if (state.subject.isBlank() || state.title.isBlank()) {
            _uiState.update { it.copy(error = "Materia y título son obligatorios") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }
            noteRepository.addNote(userId, state.subject, state.title, state.content, state.imageUri).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") } }
            )
        }
    }
}
```

- [ ] **Step 2: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`

---

### Task 4: Update AddNoteScreen — dropdown + validation

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteScreen.kt`

- [ ] **Step 1: Replace file content**

```kotlin
package com.notifast.app.presentation.feature.addnote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCenterAlignedTopBar
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel = hiltViewModel(),
    subject: String = "",
    onBackClick: () -> Unit,
    onNoteAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isSaved) { onNoteAdded(); return }

    LaunchedEffect(subject) { viewModel.setInitialSubject(subject) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.onEvent(AddNoteEvent.ImageSelected(uri))
    }

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = { NotiFastCenterAlignedTopBar(title = "Nuevo Apunte") }) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                if (uiState.subjects.isEmpty()) {
                    Text(
                        "No hay materias. Crea una primero.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = uiState.subject,
                            onValueChange = { viewModel.onEvent(AddNoteEvent.SubjectChanged(it)) },
                            label = { Text("Materia") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            uiState.subjects.forEach { subjectName ->
                                DropdownMenuItem(
                                    text = { Text(subjectName) },
                                    onClick = {
                                        viewModel.onEvent(AddNoteEvent.SubjectChanged(subjectName))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) },
                    label = { Text("Contenido") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby),
                    singleLine = false
                )
                Spacer(Modifier.height(12.dp))
                NotiFastButton(text = if (uiState.imageUri == null) "Agregar Imagen" else "Imagen seleccionada", onClick = { imagePickerLauncher.launch("image/*") })
                Spacer(Modifier.height(16.dp))
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (uiState.error != null) { Spacer(Modifier.height(8.dp)) }
                NotiFastButton(
                    text = "GUARDAR APUNTE",
                    onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) },
                    isLoading = uiState.isLoading,
                    enabled = uiState.subjects.isNotEmpty()
                )
            }
        }
    }
}
```

- [ ] **Step 2: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`

---

### Task 5: Update NavGraph — new routes + SubjectDetail callbacks

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/navigation/NavGraph.kt`

- [ ] **Step 1: Update composable routes and SubjectDetailScreen callbacks**

Changes needed:

**a)** Add navArgument imports at top if not present:
```kotlin
import androidx.navigation.NavType
import androidx.navigation.navArgument
```

**b)** Update AddNote composable:
```kotlin
composable(
    route = Screen.AddNote.route,
    arguments = listOf(navArgument("subject") { type = NavType.StringType; defaultValue = "" })
) { backStackEntry ->
    val subject = backStackEntry.arguments?.getString("subject") ?: ""
    AddNoteScreen(
        subject = subject,
        onBackClick = { navController.popBackStack() },
        onNoteAdded = { navController.popBackStack() }
    )
}
```

**c)** Update AddEvaluation composable:
```kotlin
composable(
    route = Screen.AddEvaluation.route,
    arguments = listOf(navArgument("subject") { type = NavType.StringType; defaultValue = "" })
) { backStackEntry ->
    val subject = backStackEntry.arguments?.getString("subject") ?: ""
    AddEvaluationScreen(
        subject = subject,
        onEvaluationAdded = { navController.popBackStack() },
        onBack = { navController.popBackStack() }
    )
}
```

**d)** Update NotesScreen's `onAddNoteClick`:
```kotlin
onAddNoteClick = { navController.navigate(Screen.AddNote.createRoute()) },
```

**e)** Update SubjectDetailScreen composable to pass callbacks:
```kotlin
composable(
    route = Screen.SubjectDetail.route,
    arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
) { backStackEntry ->
    val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
    SubjectDetailScreen(
        subjectName = subjectName,
        onAddNoteClick = { navController.navigate(Screen.AddNote.createRoute(subjectName)) },
        onAddEvaluationClick = { navController.navigate(Screen.AddEvaluation.createRoute(subjectName)) },
        onBackClick = { navController.popBackStack() }
    )
}
```

**f)** Update NotesScreen's `onAddNoteClick` (from the "+" button):
```kotlin
onAddNoteClick = { navController.navigate(Screen.AddNote.createRoute()) },
```

- [ ] **Step 2: Read current NavGraph.kt** and apply the above edits precisely. Make sure to:
  1. Remove the old `composable(Screen.AddNote.route)` and `composable(Screen.AddEvaluation.route)` blocks
  2. Replace with the new parameterized versions
  3. Update the SubjectDetailScreen call to include `onAddNoteClick` and `onAddEvaluationClick`
  4. Update the NotesScreen's `onAddNoteClick` to use `Screen.AddNote.createRoute()`
  5. Remove the old `composable(Screen.AddSubject.route)` block if it still exists

- [ ] **Step 3: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`

---

### Task 6: Update SubjectDetailScreen — action buttons + cards

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailContract.kt`

- [ ] **Step 1: Update SubjectDetailContract.kt**

Replace with:
```kotlin
package com.notifast.app.presentation.feature.subjectdetail

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note

data class SubjectDetailUiState(
    val subjectName: String = "",
    val notes: List<Note> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
}
```

- [ ] **Step 2: Update SubjectDetailViewModel.kt**

Replace with:
```kotlin
package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val subjectName: String = checkNotNull(savedStateHandle["subjectName"])
    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true, subjectName = subjectName) }
            combine(
                noteRepository.getNotesBySubject(userId, subjectName),
                evaluationRepository.getEvaluations(userId)
            ) { notes, evaluations ->
                SubjectDetailUiState(
                    subjectName = subjectName,
                    notes = notes,
                    evaluations = evaluations.filter { it.subjectName == subjectName }
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: SubjectDetailEvent) { if (event is SubjectDetailEvent.LoadData) loadData() }
}
```

- [ ] **Step 3: Update SubjectDetailScreen.kt**

Replace with:
```kotlin
package com.notifast.app.presentation.feature.subjectdetail

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastCenterAlignedTopBar
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onAddNoteClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = { NotiFastCenterAlignedTopBar(title = uiState.subjectName) }) { innerPadding ->
            if (uiState.isLoading) { NotiFastCircularLoading(); return@Scaffold }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onAddNoteClick(uiState.subjectName) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apunte", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onAddEvaluationClick(uiState.subjectName) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Aqua)
                        ) {
                            Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Evaluación", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)); Text("Apuntes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                if (uiState.notes.isEmpty()) { item { Text("Sin apuntes", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                else {
                    items(uiState.notes) { note ->
                        Text(note.title, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                    }
                }

                item { Spacer(Modifier.height(24.dp)); Text("Evaluaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                if (uiState.evaluations.isEmpty()) { item { Text("Sin evaluaciones", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                else {
                    items(uiState.evaluations) { eval ->
                        Text(eval.title, style = MaterialTheme.typography.bodyLarge, color = if (eval.isPresented) Aqua else MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`

---

### Task 7: Final build verification

- [ ] **Step 1: Full build**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL
