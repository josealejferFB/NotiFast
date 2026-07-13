# Calendar V2.0 — Tipos de Actividad Académica — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 4 evaluation types (Exam, Presentation, Project, Homework) with color-coded calendar dots, adaptive form fields, and backward-compatible Firestore storage.

**Architecture:** New `EvaluationType` enum encapsulates type metadata (label, color, icon, field visibility rules). The `Evaluation` data class gains a `type: String` field defaulting to `"EXAM"` for backward compatibility. The repository writes the new field; the UI adapts forms and calendar indicators based on the enum.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose + Material 3, Firebase Firestore, Hilt DI

## Global Constraints

- Language: Code in English, UI strings in Spanish
- No wildcard imports
- Max ~250 lines per composable file
- All repository write operations return `Result<T>`
- Use `MaterialTheme.colorScheme` where possible; direct color references (`Ruby`, `Aqua`) acceptable per current codebase pattern
- Follow MVVM + MVI Contract pattern per AGENTS.md

---

### Task 1: Add `EvaluationType` Enum and Update Data Model

**Files:**
- Create: `app/src/main/java/com/example/notifast/data/model/EvaluationType.kt`
- Modify: `app/src/main/java/com/example/notifast/data/model/Evaluation.kt`

**Interfaces:**
- Produces: `EvaluationType` enum with properties `label`, `color`, `icon`, `showWeight`, `showGrade`; `Evaluation.type` field

- [ ] **Step 1: Create `EvaluationType.kt`**

```kotlin
package com.example.notifast.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

enum class EvaluationType(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val showWeight: Boolean,
    val showGrade: Boolean
) {
    EXAM(
        label = "Examen",
        color = Ruby,
        icon = Icons.Default.Quiz,
        showWeight = true,
        showGrade = true
    ),
    PRESENTATION(
        label = "Exposición",
        color = Color(0xFFFF9800),
        icon = Icons.Default.PresentToAll,
        showWeight = true,
        showGrade = true
    ),
    PROJECT(
        label = "Entrega de Trabajo",
        color = Aqua,
        icon = Icons.Default.Assignment,
        showWeight = true,
        showGrade = true
    ),
    HOMEWORK(
        label = "Tarea",
        color = Color(0xFF7E57C2),
        icon = Icons.Default.Task,
        showWeight = false,
        showGrade = false
    );

    companion object {
        fun fromName(name: String): EvaluationType {
            return entries.find { it.name == name } ?: EXAM
        }
    }
}
```

- [ ] **Step 2: Add `type` field to `Evaluation.kt`**

Add the field after `quickTips`:

```kotlin
val type: String = EvaluationType.EXAM.name,
```

The full updated data class becomes:

```kotlin
data class Evaluation(
    val id: String = "",
    val userId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date? = null,
    val noteIds: List<String> = emptyList(),
    val imageUrl: String? = null,
    val quickTips: String = "",
    val type: String = EvaluationType.EXAM.name,
    val grade: Double? = null,
    val weight: Double? = null,
    val isPresented: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
```

- [ ] **Step 3: Build to verify compilation**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/notifast/data/model/EvaluationType.kt \
       app/src/main/java/com/example/notifast/data/model/Evaluation.kt
git commit -m "feat(model): add EvaluationType enum and type field to Evaluation"
```

---

### Task 2: Update Repository to Persist `type` Field

**Files:**
- Modify: `app/src/main/java/com/example/notifast/data/repository/EvaluationRepositoryImpl.kt`

**Interfaces:**
- Consumes: `Evaluation.type` (String field from Task 1)
- Produces: Firestore documents now include `type` field on write

- [ ] **Step 1: Add `type` to the write map in `addEvaluation()`**

In the `evalMap` builder inside `addEvaluation()`, add this line after `"isPresented" to evaluation.isPresented`:

```kotlin
"type" to evaluation.type,
```

- [ ] **Step 2: Add `type` to the write map in `updateEvaluation()`**

In the `updates` builder inside `updateEvaluation()`, add this line after `"isPresented" to evaluation.isPresented`:

```kotlin
"type" to evaluation.type,
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/notifast/data/repository/EvaluationRepositoryImpl.kt
git commit -m "feat(repo): persist evaluation type field to Firestore"
```

---

### Task 3: Update Add Evaluation Feature (Contract + ViewModel + Screen)

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addevaluation/AddEvaluationContract.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addevaluation/AddEvaluationViewModel.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/addevaluation/AddEvaluationScreen.kt`

**Interfaces:**
- Consumes: `EvaluationType` enum (Task 1), `Evaluation.type` (Task 1)
- Produces: `AddEvaluationUiState.selectedType`, `AddEvaluationEvent.SelectType`, adaptive form UI

- [ ] **Step 1: Update `AddEvaluationContract.kt`**

Add to `AddEvaluationUiState`:
```kotlin
val selectedType: EvaluationType = EvaluationType.EXAM,
```

Add new import:
```kotlin
import com.example.notifast.data.model.EvaluationType
```

Add new event to `AddEvaluationEvent`:
```kotlin
data class SelectType(val type: EvaluationType) : AddEvaluationEvent
```

- [ ] **Step 2: Update `AddEvaluationViewModel.kt`**

In `onEvent()`, add handler for the new event:
```kotlin
is AddEvaluationEvent.SelectType -> _uiState.update { it.copy(selectedType = event.type) }
```

In `save()`, update the `Evaluation` constructor to include the type:
```kotlin
val evaluation = Evaluation(
    userId = userId,
    subjectName = state.selectedSubject,
    title = state.title,
    description = state.description,
    date = date,
    quickTips = state.quickTips,
    type = state.selectedType.name,
    weight = if (state.selectedType.showWeight) state.weight.toDoubleOrNull() else null
)
```

Add import:
```kotlin
import com.example.notifast.data.model.EvaluationType
```

- [ ] **Step 3: Update `AddEvaluationScreen.kt`**

Add a row of `FilterChip` components after the subject dropdown and before the title field. Each chip represents one `EvaluationType`. The selected chip uses the type's color as tint.

```kotlin
// Type selector chips — after subject dropdown, before title field
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    EvaluationType.entries.forEach { type ->
        FilterChip(
            selected = uiState.selectedType == type,
            onClick = { viewModel.onEvent(AddEvaluationEvent.SelectType(type)) },
            label = { Text(type.label, fontSize = 12.sp) },
            leadingIcon = {
                Icon(type.icon, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = type.color.copy(alpha = 0.15f),
                selectedLabelColor = type.color,
                selectedLeadingIconColor = type.color
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = uiState.selectedType == type,
                selectedBorderColor = type.color
            )
        )
    }
}
```

Update the TopAppBar title to be dynamic:
```kotlin
title = "Añadir ${uiState.selectedType.label}",
```

Wrap the weight field with a conditional:
```kotlin
if (uiState.selectedType.showWeight) {
    Spacer(modifier = Modifier.height(16.dp))
    NotiFastFormField(
        value = uiState.weight,
        onValueChange = { viewModel.onEvent(AddEvaluationEvent.UpdateWeight(it)) },
        label = "Peso / Porcentaje (opcional)",
        leadingIcon = Icons.Default.Scale,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorColor = Aqua
    )
}
```

Update the save button text to be dynamic:
```kotlin
Text("GUARDAR ${uiState.selectedType.label.uppercase()}", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
```

Add required imports:
```kotlin
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.notifast.data.model.EvaluationType
```

- [ ] **Step 4: Build to verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/addevaluation/
git commit -m "feat(add-eval): type selector chips, adaptive form fields"
```

---

### Task 4: Update Edit Evaluation Feature (Contract + ViewModel + Screen)

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/editevaluation/EditEvaluationContract.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/editevaluation/EditEvaluationViewModel.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/editevaluation/EditEvaluationScreen.kt`

**Interfaces:**
- Consumes: `EvaluationType.fromName()` (Task 1), `Evaluation.type` (Task 1)
- Produces: Read-only type chip in edit screen, type-aware field visibility

- [ ] **Step 1: Update `EditEvaluationContract.kt`**

Add to `EditEvaluationUiState`:
```kotlin
val type: String = EvaluationType.EXAM.name,
```

Add import:
```kotlin
import com.example.notifast.data.model.EvaluationType
```

- [ ] **Step 2: Update `EditEvaluationViewModel.kt`**

In `loadEvaluation()`, add to the `_uiState.update` block:
```kotlin
type = eval.type,
```

In `save()`, add to the `Evaluation` constructor:
```kotlin
type = state.type,
```

- [ ] **Step 3: Update `EditEvaluationScreen.kt`**

After the subject TextField and before the title field, add a read-only chip showing the type:

```kotlin
val evaluationType = EvaluationType.fromName(uiState.type)

Spacer(modifier = Modifier.height(12.dp))

// Read-only type indicator
AssistChip(
    onClick = { },
    label = { Text(evaluationType.label) },
    leadingIcon = { Icon(evaluationType.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
    colors = AssistChipDefaults.assistChipColors(
        containerColor = evaluationType.color.copy(alpha = 0.1f),
        labelColor = evaluationType.color,
        leadingIconContentColor = evaluationType.color
    ),
    border = AssistChipDefaults.assistChipBorder(borderColor = evaluationType.color.copy(alpha = 0.3f))
)

Spacer(modifier = Modifier.height(12.dp))
```

Wrap the weight field with a conditional:
```kotlin
if (evaluationType.showWeight) {
    // existing weight field
}
```

Wrap the grade row with a conditional:
```kotlin
if (evaluationType.showGrade) {
    // existing grade + isPresented row
}
```

Update TopAppBar title:
```kotlin
title = "Editar ${evaluationType.label}",
```

Update save button text:
```kotlin
Text("ACTUALIZAR ${evaluationType.label.uppercase()}", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
```

Add required imports:
```kotlin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import com.example.notifast.data.model.EvaluationType
```

- [ ] **Step 4: Build to verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/editevaluation/
git commit -m "feat(edit-eval): read-only type chip, adaptive field visibility"
```

---

### Task 5: Update Calendar UI with Multi-Color Dots and Type-Aware Cards

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/calendar/CalendarScreen.kt`

**Interfaces:**
- Consumes: `EvaluationType.fromName()` (Task 1), `Evaluation.type` (Task 1)

- [ ] **Step 1: Update `CalendarGrid` to show multi-color dots**

Replace the single-dot logic in the day cell with a multi-dot row. Inside the `Box` for each day cell, replace the existing dot rendering:

```kotlin
// Multi-color dots for event types
val dayEvalTypes = evaluations
    .filter { isSameDay(it.date, date.time) }
    .map { EvaluationType.fromName(it.type) }
    .distinct()
    .sortedBy { it.ordinal }
    .take(3)

if (dayEvalTypes.isNotEmpty()) {
    Spacer(modifier = Modifier.height(2.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        dayEvalTypes.forEach { evalType ->
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) evalType.color else evalType.color.copy(alpha = 0.7f))
            )
        }
    }
} else {
    Spacer(modifier = Modifier.height(6.dp))
}
```

- [ ] **Step 2: Update `EvaluationListItem` to use type color and icon**

Replace the hardcoded `Ruby` and `Icons.Default.Event`:

```kotlin
@Composable
fun EvaluationListItem(eval: Evaluation, onClick: () -> Unit) {
    val evalType = EvaluationType.fromName(eval.type)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(evalType.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(evalType.icon, contentDescription = null, tint = evalType.color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(eval.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(eval.subjectName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
```

- [ ] **Step 3: Update `EvaluationDetailContent` to use type color and icon**

Replace the hardcoded `Ruby` and `Icons.Default.Event` in the bottom sheet:

```kotlin
@Composable
fun EvaluationDetailContent(eval: Evaluation, onEdit: () -> Unit, onDelete: () -> Unit) {
    val evalType = EvaluationType.fromName(eval.type)
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).background(evalType.color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(evalType.icon, contentDescription = null, tint = evalType.color, modifier = Modifier.size(28.dp))
            }
            // ... rest stays the same
        }
        // ... rest of the function stays the same, but replace DetailRow Ruby default with evalType.color:
    }
}
```

- [ ] **Step 4: Add missing import**

```kotlin
import com.example.notifast.data.model.EvaluationType
```

- [ ] **Step 5: Build to verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/calendar/CalendarScreen.kt
git commit -m "feat(calendar): multi-color dots and type-aware list items"
```
