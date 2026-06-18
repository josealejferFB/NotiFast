# Evaluation Grading & Averages Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add evaluation grading (mark complete + enter grade 0-10) to SubjectDetailScreen, sync with Grades, fix weighted average inconsistency.

**Architecture:** SubjectDetailViewModel gets `saveGrade()` and `deleteEvaluation()` methods that call EvaluationRepository. Timeline cards and new EvaluationList component use long-press DropdownMenus. GradeDialog collects grade input with 0-10 validation. GradesViewModel/Screeen switch from simple to weighted average. Real-time Firestore sync via `.snapshots()` propagates changes to all screens.

**Tech Stack:** Kotlin, Jetpack Compose, Firebase Firestore, Hilt

**Files to modify:**
- `SubjectDetailContract.kt` — new events
- `SubjectDetailViewModel.kt` — new CRUD methods
- `SubjectDetailTimeline.kt` — long-press context menu
- `SubjectDetailScreen.kt` — GradeDialog, delete dialog, EvaluationList integration
- `GradesViewModel.kt` — weighted average formula
- `GradesScreen.kt` — minor label fix (1-10 → 0-10)

**Files to create:**
- `components/SubjectDetailEvaluationList.kt` — full eval list with controls

---

### Task 1: Add SaveGrade and DeleteEvaluation events to SubjectDetailContract

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailContract.kt`

- [ ] **Step 1: Add new event types**

Read current file, then add two new events to `SubjectDetailEvent`:

```kotlin
sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
    data class SaveGrade(val evaluationId: String, val grade: Double, val isPresented: Boolean) : SubjectDetailEvent
    data class DeleteEvaluation(val evaluationId: String) : SubjectDetailEvent
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailContract.kt
git commit -m "feat: add SaveGrade and DeleteEvaluation events to SubjectDetailContract"
```

---

### Task 2: Add saveGrade and deleteEvaluation methods to SubjectDetailViewModel

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`

- [ ] **Step 1: Update onEvent and add methods**

Replace the `onEvent` method and add two new private methods after `loadData()`:

```kotlin
fun onEvent(event: SubjectDetailEvent) {
    when (event) {
        is SubjectDetailEvent.LoadData -> loadData()
        is SubjectDetailEvent.SaveGrade -> saveGrade(event.evaluationId, event.grade, event.isPresented)
        is SubjectDetailEvent.DeleteEvaluation -> deleteEvaluation(event.evaluationId)
    }
}

private fun saveGrade(evalId: String, grade: Double, isPresented: Boolean) {
    viewModelScope.launch {
        val userId = authRepository.getCurrentUserId() ?: return@launch
        val eval = _uiState.value.evaluations.find { it.id == evalId } ?: return@launch
        val updated = eval.copy(grade = grade, isPresented = isPresented)
        evaluationRepository.updateEvaluation(userId, updated, null)
    }
}

private fun deleteEvaluation(evalId: String) {
    viewModelScope.launch {
        val userId = authRepository.getCurrentUserId() ?: return@launch
        val eval = _uiState.value.evaluations.find { it.id == evalId } ?: return@launch
        evaluationRepository.deleteEvaluation(userId, eval)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailViewModel.kt
git commit -m "feat: add saveGrade and deleteEvaluation to SubjectDetailViewModel"
```

---

### Task 3: Add long-press context menu to SubjectDetailTimeline

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailTimeline.kt`

- [ ] **Step 1: Read current file**

Read the file to see the exact current state.

- [ ] **Step 2: Add new parameters and imports**

Add to the composable signature:
```kotlin
@Composable
fun SubjectDetailTimeline(
    upcomingEvaluations: List<Evaluation>,
    subjectColorHex: String,
    onMarkComplete: (Evaluation) -> Unit = {},
    onDeleteEval: (Evaluation) -> Unit = {}
) {
```

Add imports:
```kotlin
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
```

- [ ] **Step 3: Add context menu to each timeline card**

Replace the `Surface` wrapping each evaluation card with one that has `combinedClickable` and a `DropdownMenu`. Inside the `upcomingEvaluations.forEachIndexed` block, wrap the inner `Row(modifier = Modifier.fillMaxWidth())` content with a `Box` that holds both the `DropdownMenu` and the card surface:

Replace:
```kotlin
Row(modifier = Modifier.fillMaxWidth()) {
    // Timeline line
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(chipColor))
        if (index < upcomingEvaluations.lastIndex) {
            Box(modifier = Modifier.width(2.dp).height(50.dp).background(color.copy(alpha = 0.3f)))
        }
    }
    Spacer(Modifier.width(12.dp))
    Surface(
        modifier = Modifier.weight(1f).padding(bottom = 8.dp),
        ...
    ) { ... }
}
```

With:
```kotlin
Row(modifier = Modifier.fillMaxWidth()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(chipColor))
        if (index < upcomingEvaluations.lastIndex) {
            Box(modifier = Modifier.width(2.dp).height(50.dp).background(color.copy(alpha = 0.3f)))
        }
    }
    Spacer(Modifier.width(12.dp))
    Box(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
        var showMenu by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxWidth().combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            ),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) { /* existing Row with title, date, weight, urgency chip */ }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Marcar completada") },
                onClick = {
                    showMenu = false
                    onMarkComplete(eval)
                },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Aqua) }
            )
            DropdownMenuItem(
                text = { Text("Eliminar", color = Ruby) },
                onClick = {
                    showMenu = false
                    onDeleteEval(eval)
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Ruby) }
            )
        }
    }
}
```

Add the required state import:
```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailTimeline.kt
git commit -m "feat: add long-press context menu to SubjectDetailTimeline"
```

---

### Task 4: Create SubjectDetailEvaluationList component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailEvaluationList.kt`

- [ ] **Step 1: Create the evaluation list component**

```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectDetailEvaluationList(
    evaluations: List<Evaluation>,
    subjectColorHex: String,
    onMarkComplete: (Evaluation) -> Unit = {},
    onEditGrade: (Evaluation) -> Unit = {},
    onDeleteEval: (Evaluation) -> Unit = {}
) {
    if (evaluations.isEmpty()) return

    val color = parseSubjectColor(subjectColorHex)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Todas las evaluaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        evaluations.forEach { eval ->
            var showMenu by remember { mutableStateOf(false) }
            val isCompleted = eval.isPresented

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true }
                    ).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape).background(
                            if (isCompleted) Color(0xFF43A047) else color.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(eval.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompleted) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Aqua.copy(alpha = 0.1f)) {
                                    Text(
                                        text = eval.grade?.let { String.format("%.1f", it) } ?: "—",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Aqua
                                    )
                                }
                            } else {
                                Text("Pendiente", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            val weight = eval.weight ?: 0.0
                            if (weight > 0.0) {
                                Spacer(Modifier.width(6.dp))
                                Text("${(weight * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!isCompleted) {
                        DropdownMenuItem(
                            text = { Text("Marcar completada") },
                            onClick = { showMenu = false; onMarkComplete(eval) },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Aqua) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Editar nota") },
                            onClick = { showMenu = false; onEditGrade(eval) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Aqua) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Ruby) },
                        onClick = { showMenu = false; onDeleteEval(eval) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Ruby) }
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailEvaluationList.kt
git commit -m "feat: create SubjectDetailEvaluationList with long-press controls"
```

---

### Task 5: Integrate everything in SubjectDetailScreen

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt`

- [ ] **Step 1: Read current file**

Read the full file to know exact content to modify.

- [ ] **Step 2: Add imports**

Add after existing imports:
```kotlin
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailEvaluationList
```

- [ ] **Step 3: Add state and dialog logic inside SubjectDetailScreen composable**

After `var expandedNote by remember { mutableStateOf<Note?>(null) }`, add:
```kotlin
var gradeDialogEval by remember { mutableStateOf<Evaluation?>(null) }
var deleteEvalId by remember { mutableStateOf<String?>(null) }
```

- [ ] **Step 4: Insert SubjectDetailEvaluationList between calendar and notes**

After the `item(key = "calendar")` block and before `item(key = "notes")`, add:
```kotlin
item(key = "all-evaluations") {
    SubjectDetailEvaluationList(
        evaluations = uiState.evaluations,
        subjectColorHex = uiState.subjectColorHex,
        onMarkComplete = { gradeDialogEval = it },
        onEditGrade = { gradeDialogEval = it },
        onDeleteEval = { deleteEvalId = it.id }
    )
    Spacer(Modifier.height(24.dp))
}
```

- [ ] **Step 5: Wire onMarkComplete and onDeleteEval in SubjectDetailTimeline call**

In the `item(key = "timeline")` section, update `SubjectDetailTimeline` call to pass:
```kotlin
SubjectDetailTimeline(
    upcomingEvaluations = uiState.upcomingEvaluations,
    subjectColorHex = uiState.subjectColorHex,
    onMarkComplete = { gradeDialogEval = it },
    onDeleteEval = { deleteEvalId = it.id }
)
```

- [ ] **Step 6: Add GradeDialog composable**

After the `SubjectDetailNoteDetail` composable at the bottom of the file, add:

```kotlin
@Composable
private fun GradeDialog(
    evaluation: Evaluation,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var gradeText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar evaluaci\u00F3n") },
        text = {
            Column {
                Text(evaluation.title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = {
                        gradeText = it
                        error = null
                    },
                    label = { Text("Nota (0.0 - 10.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = Ruby) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val grade = gradeText.toDoubleOrNull()
                    when {
                        gradeText.isBlank() -> error = "Ingresa una nota"
                        grade == null -> error = "Nota inv\u00E1lida"
                        grade < 0.0 || grade > 10.0 -> error = "La nota debe estar entre 0.0 y 10.0"
                        else -> onSave(grade)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) { Text("Guardar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
```

- [ ] **Step 7: Add GradeDialog and delete dialog invocations inside the Box**

After `AnimatedVisibility` blocks and before the closing `}` of the outer `Box(modifier = Modifier.fillMaxSize())`, add:
```kotlin
gradeDialogEval?.let { eval ->
    GradeDialog(
        evaluation = eval,
        onSave = { grade ->
            viewModel.onEvent(SubjectDetailEvent.SaveGrade(eval.id, grade, isPresented = true))
            gradeDialogEval = null
        },
        onDismiss = { gradeDialogEval = null }
    )
}

deleteEvalId?.let { id ->
    val eval = uiState.evaluations.find { it.id == id }
    AlertDialog(
        onDismissRequest = { deleteEvalId = null },
        title = { Text("Eliminar evaluaci\u00F3n") },
        text = { Text("\u00BFEst\u00E1s seguro de eliminar ${eval?.title ?: ""}? Esta acci\u00F3n no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.onEvent(SubjectDetailEvent.DeleteEvaluation(id))
                    deleteEvalId = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) { Text("Eliminar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = { deleteEvalId = null }) { Text("Cancelar") }
        }
    )
}
```

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt
git commit -m "feat: integrate GradeDialog and EvaluationList in SubjectDetailScreen"
```

---

### Task 6: Fix weighted average inconsistency in GradesViewModel + GradesScreen

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/grades/GradesViewModel.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/grades/GradesScreen.kt`

- [ ] **Step 1: Update GradesViewModel — nothing to change (averages computed in UI)**

GradesViewModel does not compute averages — they are computed in GradesScreen's `MainAverageDashboard` and `SubjectRevampCard`. The ViewModel is already correct.

- [ ] **Step 2: Fix MainAverageDashboard in GradesScreen**

Replace the simple average calculation:
```kotlin
val subjectAverages = evaluationsBySubject.values.mapNotNull { evaluations ->
    val graded = evaluations.filter { it.grade != null }
    if (graded.isNotEmpty()) {
        graded.map { it.grade!! }.average()
    } else null
}
```

With weighted average:
```kotlin
val subjectAverages = evaluationsBySubject.values.mapNotNull { evaluations ->
    val graded = evaluations.filter { it.grade != null && it.isPresented }
    if (graded.isNotEmpty()) {
        val weightedSum = graded.sumOf { (it.grade ?: 0.0) * (it.weight ?: 1.0) }
        val totalWeight = graded.sumOf { it.weight ?: 1.0 }
        if (totalWeight > 0.0) weightedSum / totalWeight else null
    } else null
}
```

- [ ] **Step 3: Fix SubjectRevampCard in GradesScreen**

Replace:
```kotlin
val graded = evaluations.filter { it.grade != null }
val subjectAverage = if (graded.isNotEmpty()) graded.map { it.grade!! }.average() else 0.0
```

With:
```kotlin
val graded = evaluations.filter { it.grade != null && it.isPresented }
val subjectAverage = if (graded.isNotEmpty()) {
    val weightedSum = graded.sumOf { (it.grade ?: 0.0) * (it.weight ?: 1.0) }
    val totalWeight = graded.sumOf { it.weight ?: 1.0 }
    if (totalWeight > 0.0) weightedSum / totalWeight else 0.0
} else 0.0
```

- [ ] **Step 4: Fix label in GradeRevampDialog**

Change line:
```kotlin
label = { Text("Nota (1-10)") },
```
To:
```kotlin
label = { Text("Nota (0-10)") },
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/grades/GradesScreen.kt
git commit -m "fix: use weighted average in GradesScreen, fix grade label to 0-10"
```

---

### Task 7: Verify build

- [ ] **Step 1: Run assembleDebug**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Fix any compilation errors** (iterate until clean)

- [ ] **Step 3: Final commit with all remaining changes**

```bash
git add -A
git commit -m "feat: complete evaluation grading with weighted averages sync"
```
