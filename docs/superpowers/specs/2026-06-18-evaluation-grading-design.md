# Evaluation Grading & Averages Redesign

**Date:** 2026-06-18
**Status:** Approved design

## Goal

Add functionality to mark evaluations as completed and enter grades (0–10 scale) directly from the SubjectDetailScreen, synced with the Grades section. Fix weighted average inconsistency in GradesViewModel.

## Architecture

### Data Flow

```
SubjectDetailScreen
  │  long-press on timeline card / eval list row
  ├─→ GradeDialog (ingresar nota 0-10, toggle isPresented)
  ├─→ DeleteDialog (confirmar eliminación)
  └─→ EditEvaluationScreen (navegación, para evaluaciones completadas)

SubjectDetailViewModel
  ├─→ updateEvaluation(userId, eval, null) → EvaluationRepository.updateEvaluation()
  └─→ deleteEvaluation(userId, eval)       → EvaluationRepository.deleteEvaluation()

GradesViewModel (fix)
  └─→ weighted average formula = sum(grade × weight) / sum(weight)
      (same as SubjectDetailViewModel, NotesViewModel)
```

### Event Flow

```
User long-press → DropdownMenu
  ├─ "Marcar completada"  → GradeDialog → SubjectDetailEvent.SaveGrade(evalId, grade)
  ├─ "Editar nota"        → GradeDialog → SubjectDetailEvent.SaveGrade(evalId, grade)
  ├─ "Eliminar"           → DeleteDialog → SubjectDetailEvent.DeleteEvaluation(evalId)
  └─ "Editar"             → navigate to EditEvaluationScreen
```

## Changes by File

### 1. SubjectDetailContract.kt — New events

Add to `SubjectDetailEvent`:
```kotlin
data class SaveGrade(val evaluationId: String, val grade: Double, val isPresented: Boolean) : SubjectDetailEvent
data class DeleteEvaluation(val evaluationId: String) : SubjectDetailEvent
```

### 2. SubjectDetailViewModel.kt — New methods

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

### 3. SubjectDetailTimeline.kt — Long-press context menu

Each timeline evaluation card gains:
- `onLongClick` → `DropdownMenu`
- Menu items: "Marcar completada" (only if `!eval.isPresented`), "Eliminar"
- Pass new callbacks: `onMarkComplete: (Evaluation) -> Unit`, `onDeleteEval: (Evaluation) -> Unit`
- DropdownMenu with red "Eliminar" item

### 4. SubjectDetailEvaluationList.kt — New component (NEW FILE)

**Location:** `components/SubjectDetailEvaluationList.kt`

**Behavior:**
- Section with "Todas las evaluaciones" header + divider
- Lists all `evaluations` from UiState (all, not just upcoming)
- Each row shows:
  - Colored circle (green fill if completed, gray outline if pending)
  - Evaluation title
  - Grade badge if completed, "Pendiente" chip if not
  - Weight badge if > 0
- **Long-press** → `DropdownMenu`:
  - If pending: "Marcar completada", "Eliminar"
  - If completed: "Editar nota", "Eliminar"
- Callbacks: `onMarkComplete: (Evaluation) -> Unit`, `onEditGrade: (Evaluation) -> Unit`, `onDeleteEval: (Evaluation) -> Unit`

### 5. SubjectDetailScreen.kt — Integrate everything

- Import new `SubjectDetailEvaluationList` component
- Add state for `gradeDialogEval: Evaluation?` to control the grade dialog
- Add state for `deleteEvalId: String?` for delete confirmation
- Insert `SubjectDetailEvaluationList` section between Calendar and Notes
- Add `GradeDialog` composable (AlertDialog with OutlinedTextField for grade 0.0–10.0, validated)
- Add delete confirmation AlertDialog
- Wire callbacks: `onMarkComplete` opens GradeDialog, `onDeleteEval` opens delete confirmation

### 6. GradesViewModel.kt — Fix weighted average consistency

**Current (broken):**
```kotlin
val graded = evals.filter { it.grade != null }
if (graded.isNotEmpty()) graded.map { it.grade!! }.average() else null
```

**Fixed (weighted):**
```kotlin
val graded = evals.filter { it.grade != null && it.isPresented }
val weightedSum = graded.sumOf { (it.grade ?: 0.0) * (it.weight ?: 1.0) }
val totalWeight = graded.sumOf { it.weight ?: 1.0 }
val avg = if (totalWeight > 0.0) weightedSum / totalWeight else null
```

### 7. GradesScreen.kt — Minor visual fix

`SubjectRevampCard` average display already shows `String.format("%.1f", avg)`. No changes needed. `LinearProgressIndicator` already scales to max 10 (`progress = { it.toFloat() / 10f }`).

## GradeDialog Composable (in SubjectDetailScreen.kt)

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
        title = { Text("Calificar evaluación") },
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
                        grade == null -> error = "Nota inválida"
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

## Components & Grading Consistency

| Screen | Average Formula | Status |
|---|---|---|
| NotesScreen (SubjectCard) | Weighted: sum(g×w)/sum(w) | ✅ Correct |
| SubjectDetailScreen (Stats) | Weighted: sum(g×w)/sum(w) | ✅ Correct |
| GradesScreen (SubjectRevampCard) | Simple: avg(grades) | ❌ Will be fixed to weighted |
| GradesScreen (MainAverageDashboard) | Simple avg of subject avgs | ❌ Will be fixed to weighted |

## Files to Modify

| File | Change |
|---|---|
| `SubjectDetailContract.kt` | Add SaveGrade, DeleteEvaluation events |
| `SubjectDetailViewModel.kt` | Add saveGrade(), deleteEvaluation() methods |
| `SubjectDetailTimeline.kt` | Add onLongClick, DropdownMenu, callbacks |
| `SubjectDetailScreen.kt` | Import EvaluationList, add GradeDialog, delete dialog, wire callbacks |
| `GradesViewModel.kt` | Fix to weighted average formula |

## Files to Create

| File | Content |
|---|---|
| `components/SubjectDetailEvaluationList.kt` | "Todas las evaluaciones" section with rows, long-press menus |

## Edge Cases

- **Grade 0.0**: Valid entry (student scored 0). Should be stored as 0.0, not treated as "no grade".
- **Deleting evaluation**: Confirmation dialog warns it will be permanently removed. No associated notes to delete (noteIds exist but are informational).
- **Empty grade field**: Dialog shows validation error "Ingresa una nota".
- **Grade out of range**: Shows error "La nota debe estar entre 0.0 y 10.0".
- **Firestore real-time sync**: EvaluationRepository.getEvaluations() uses `.snapshots()` Flow, so changes propagate automatically to all screens (GradesScreen, NotesScreen, SubjectDetailScreen).
- **Weighted average with no graded evals**: Shows "—" (already handled via `if (totalWeight > 0.0)... else null`).
