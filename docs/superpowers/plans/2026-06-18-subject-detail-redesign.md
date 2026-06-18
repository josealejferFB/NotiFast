# Subject Detail Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign SubjectDetailScreen as a stunning visual compendium with Hero Header, Stats Dashboard, Timeline, Calendar Cards, Notes Mosaic, and Progress section

**Architecture:** SubjectDetailViewModel loads notesBySubject + evaluations filtered + subjects list to compute stats and find icon/colorHex. New component files in `components/` subdirectory for each visual section. Screen assembles them in a LazyColumn with gradient hero header and floating action buttons.

**Tech Stack:** Kotlin, Jetpack Compose, Firebase Firestore, Hilt

**Files to modify:**
- `SubjectCard.kt` — add "Más detalles" button
- `SubjectDetailContract.kt` — enrich UiState
- `SubjectDetailViewModel.kt` — compute stats, load SubjectData
- `SubjectDetailScreen.kt` — complete redesign
- `NavGraph.kt` — wire FAB navigation (already partially done)

**Files to create:**
- `components/SubjectDetailHero.kt` — gradient header with icon + name
- `components/SubjectDetailStats.kt` — 2×2 stats dashboard
- `components/SubjectDetailTimeline.kt` — upcoming evals timeline
- `components/SubjectDetailCalendarCard.kt` — calendar square card
- `components/SubjectDetailNoteTile.kt` — mosaic note tile
- `components/SubjectDetailProgress.kt` — progress bar section

---

### Task 1: Add "Más detalles" button to SubjectCard

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectCard.kt`

- [ ] **Step 1: Add onSubjectDetail parameter**

Add to SubjectCard signature:
```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectCard(
    info: SubjectInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddNote: (String) -> Unit,
    onAddEvaluation: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onDeleteSubject: () -> Unit,
    onSubjectDetail: () -> Unit
) {
```

- [ ] **Step 2: Add import and button**

Add import:
```kotlin
import androidx.compose.material.icons.filled.Info
```

In the quick actions Row, after the existing two buttons, add a full-width OutlinedButton below the Row:

After the closing `}` of the quick actions Row and before its enclosing Column's closing:
```kotlin
Spacer(Modifier.height(8.dp))
OutlinedButton(
    onClick = onSubjectDetail,
    modifier = Modifier.fillMaxWidth().height(44.dp),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, color),
    colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
) {
    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(6.dp))
    Text("Más detalles", fontWeight = FontWeight.Bold)
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/components/SubjectCard.kt
git commit -m "feat: add 'Más detalles' button to SubjectCard expanded view"
```

---

### Task 2: Wire SubjectCard button in NotesScreen + NavGraph

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/navigation/NavGraph.kt`

- [ ] **Step 1: Add onSubjectDetailClick to NotesScreen**

In NotesScreen signature, add:
```kotlin
onSubjectDetailClick: (String) -> Unit,
```

- [ ] **Step 2: Wire onSubjectDetail in SubjectCard call**

In the SubjectCard items block, add:
```kotlin
onSubjectDetail = { onSubjectDetailClick(info.name) }
```

- [ ] **Step 3: Wire in NavGraph**

In `NavGraph.kt`, add to NotesScreen call:
```kotlin
onSubjectDetailClick = { subject -> navController.navigate(Screen.SubjectDetail.createRoute(subject)) },
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt app/src/main/java/com/example/notifast/presentation/navigation/NavGraph.kt
git commit -m "feat: wire SubjectDetail navigation from NotesScreen"
```

---

### Task 3: Enrich SubjectDetailContract

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailContract.kt`

- [ ] **Step 1: Replace UiState with enriched version**

Replace entire file content:
```kotlin
package com.example.notifast.presentation.feature.subjectdetail

import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.Note

data class SubjectDetailUiState(
    val isLoading: Boolean = true,
    val subjectName: String = "",
    val subjectIcon: String = "📚",
    val subjectColorHex: String = "",
    val notes: List<Note> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val averageGrade: Double? = null,
    val completedEvalCount: Int = 0,
    val pendingEvalCount: Int = 0,
    val upcomingEvaluations: List<Evaluation> = emptyList(),
    val progressPercent: Float = 0f
)

sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailContract.kt
git commit -m "feat: enrich SubjectDetailUiState with SubjectData and computed stats"
```

---

### Task 4: Enrich SubjectDetailViewModel

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`

- [ ] **Step 1: Replace ViewModel with enriched version**

Replace entire file content:
```kotlin
package com.example.notifast.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.data.model.SubjectData
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.EvaluationRepository
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
class SubjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val subjectName: String = checkNotNull(savedStateHandle["subjectName"])
    private val _uiState = MutableStateFlow(SubjectDetailUiState(subjectName = subjectName))
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            combine(
                noteRepository.getNotesBySubject(userId, subjectName),
                evaluationRepository.getEvaluations(userId),
                noteRepository.getSubjects(userId)
            ) { notes, evaluations, subjects ->
                val subjectEvals = evaluations.filter { it.subjectName == subjectName }
                val subjectData = subjects.find { it.name == subjectName } ?: SubjectData(name = subjectName)

                val graded = subjectEvals.filter { it.grade != null && it.isPresented }
                val weightedSum = graded.sumOf { (it.grade ?: 0.0) * (it.weight ?: 1.0) }
                val totalWeight = graded.sumOf { it.weight ?: 1.0 }
                val avg = if (totalWeight > 0.0) weightedSum / totalWeight else null

                val upcoming = subjectEvals
                    .filter { !it.isPresented && it.date != null }
                    .sortedBy { it.date }
                    .take(5)

                val completed = subjectEvals.count { it.isPresented }
                val total = subjectEvals.size
                val progress = if (total > 0) completed.toFloat() / total else 0f

                SubjectDetailUiState(
                    isLoading = false,
                    subjectName = subjectName,
                    subjectIcon = subjectData.icon,
                    subjectColorHex = subjectData.colorHex,
                    notes = notes,
                    evaluations = subjectEvals,
                    averageGrade = avg?.let { kotlin.math.round(it * 10) / 10.0 },
                    completedEvalCount = completed,
                    pendingEvalCount = subjectEvals.count { !it.isPresented },
                    upcomingEvaluations = upcoming,
                    progressPercent = progress
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: SubjectDetailEvent) { if (event is SubjectDetailEvent.LoadData) loadData() }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailViewModel.kt
git commit -m "feat: enrich SubjectDetailViewModel with stats computation and SubjectData"
```

---

### Task 5: Create SubjectDetailHero component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailHero.kt`

- [ ] **Step 1: Create hero header composable**

```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifast.presentation.atoms.SubjectIconMapper
import com.example.notifast.presentation.theme.color.Aqua

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

@Composable
fun SubjectDetailHero(
    subjectName: String,
    subjectIcon: String,
    subjectColorHex: String,
    noteCount: Int,
    evalCount: Int,
    onBackClick: () -> Unit
) {
    val color = parseSubjectColor(subjectColorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.85f),
                        color.copy(alpha = 0.3f),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SubjectIconMapper.getIcon(subjectIcon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = subjectName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$noteCount apuntes · $evalCount evaluaciones",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailHero.kt
git commit -m "feat: create SubjectDetailHero with gradient background and subject branding"
```

---

### Task 6: Create SubjectDetailStats component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailStats.kt`

- [ ] **Step 1: Create stats dashboard composable**

```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

@Composable
fun SubjectDetailStats(
    averageGrade: Double?,
    completedEvalCount: Int,
    pendingEvalCount: Int,
    totalEvalCount: Int,
    noteCount: Int,
    subjectColorHex: String
) {
    val color = parseSubjectColor(subjectColorHex)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Score card (tall, left column)
            StatCard(
                modifier = Modifier.weight(1f).height(140.dp),
                content = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = averageGrade?.let { String.format("%.1f", it) } ?: "—",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = color
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Promedio", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                accentColor = color
            )

            // Right column: two stacked cards
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.height(64.dp),
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Evaluaciones", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Spacer(Modifier.height(4.dp))
                                Text("$completedEvalCount/$totalEvalCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Aqua.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Aqua)
                            }
                        }
                    },
                    accentColor = Aqua
                )
                StatCard(
                    modifier = Modifier.height(64.dp),
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pendientes", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Spacer(Modifier.height(4.dp))
                                Text("$pendingEvalCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            if (pendingEvalCount > 0) {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Ruby.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                    Text("!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ruby)
                                }
                            }
                        }
                    },
                    accentColor = Ruby
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).width(0.dp).background(accentColor))
            Box(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}
```

Wait, this needs `progressPercent`. I forgot to pass it. Let me fix.

Actually, `progressPercent` is in the UiState. I should add it as parameter or compute it from the counts in the composable.

Let me simplify: compute `progressPercent` inside the composable:
```kotlin
private val progressPercent: Float
    get() = if (totalEvalCount > 0) completedEvalCount.toFloat() / totalEvalCount else 0f
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailStats.kt
git commit -m "feat: create SubjectDetailStats with score circle and stat cards"
```

---

### Task 7: Create SubjectDetailTimeline component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailTimeline.kt`

- [ ] **Step 1: Create timeline composable**

```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

private fun daysUntil(date: Date): Long {
    val diff = date.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}

private fun urgencyColor(days: Long): Color = when {
    days < 0 -> Color.Gray
    days <= 7 -> Color(0xFFE53935)
    days <= 14 -> Color(0xFFFB8C00)
    else -> Color(0xFF43A047)
}

private fun formatDate(date: Date): String {
    val cal = java.util.Calendar.getInstance().apply { time = date }
    val months = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    return "${cal.get(java.util.Calendar.DAY_OF_MONTH)} ${months[cal.get(java.util.Calendar.MONTH)]}"
}

@Composable
fun SubjectDetailTimeline(
    upcomingEvaluations: List<Evaluation>,
    subjectColorHex: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Próximas evaluaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (upcomingEvaluations.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF43A047)), contentAlignment = Alignment.Center) {
                        Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("¡Todo al día!", fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                }
            }
        } else {
            val color = parseSubjectColor(subjectColorHex)
            Column {
                upcomingEvaluations.forEachIndexed { index, eval ->
                    val days = eval.date?.let { daysUntil(it) } ?: 999
                    val chipColor = urgencyColor(days)
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
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(eval.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(eval.date?.let { formatDate(it) } ?: "Sin fecha", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        val weight = eval.weight ?: 0.0
                                        if (weight > 0.0) {
                                            Text(" · ${(weight * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(6.dp)).background(chipColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            days < 0 -> "Vencida"
                                            days == 0L -> "Hoy"
                                            days == 1L -> "Mañana"
                                            else -> "${days}d"
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = chipColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailTimeline.kt
git commit -m "feat: create SubjectDetailTimeline with urgency dots and gradable cards"
```

---

### Task 8: Create SubjectDetailCalendarCard component

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailCalendarCard.kt`

- [ ] **Step 1: Create calendar card composable**

```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import java.util.Calendar
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

private fun daysUntil(date: Date): Long {
    val diff = date.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}

private fun urgencyColor(days: Long): Color = when {
    days < 0 -> Color.Gray
    days <= 7 -> Color(0xFFE53935)
    days <= 14 -> Color(0xFFFB8C00)
    else -> Color(0xFF43A047)
}

private val months = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun dayOfMonth(date: Date): Int = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH)
private fun monthOf(date: Date): String = months[Calendar.getInstance().apply { time = date }.get(Calendar.MONTH)]

@Composable
fun SubjectDetailCalendarRow(
    evaluations: List<Evaluation>,
    subjectColorHex: String
) {
    if (evaluations.isEmpty()) return

    val color = parseSubjectColor(subjectColorHex)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Calendario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            items(evaluations.take(6)) { eval ->
                val date = eval.date
                if (date != null) {
                    CalendarSquare(
                        day = dayOfMonth(date),
                        month = monthOf(date),
                        title = eval.title,
                        color = color,
                        urgencyColor = urgencyColor(daysUntil(date)),
                        weight = eval.weight
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarSquare(
    day: Int,
    month: String,
    title: String,
    color: Color,
    urgencyColor: Color,
    weight: Double?
) {
    Card(
        modifier = Modifier.size(width = 120.dp, height = 130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Calendar header
            Box(
                modifier = Modifier.fillMaxWidth().background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(month, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
            }
            // Day number
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("$day", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            // Urgency bar
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(urgencyColor))
            // Title
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            // Weight tag
            if (weight != null && weight > 0.0) {
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 6.dp, vertical = 2.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                ) {
                    Text("${(weight * 100).toInt()}%", fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailCalendarCard.kt
git commit -m "feat: create SubjectDetailCalendarCard with calendar-square design"
```

---

### Task 9: Create SubjectDetailNoteTile + SubjectDetailProgress

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailNoteTile.kt`
- Create: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailProgress.kt`

- [ ] **Step 1: Create note tile composable**

```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.theme.color.Aqua
import java.util.Calendar
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

private fun formatShortDate(date: Date?): String {
    if (date == null) return ""
    val cal = Calendar.getInstance().apply { time = date }
    return "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
}

@Composable
fun SubjectDetailNotesMosaic(
    notes: List<Note>,
    subjectColorHex: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Apuntes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (notes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Sin apuntes aún", color = Color.Gray)
                }
            }
        } else {
            val color = parseSubjectColor(subjectColorHex)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
            ) {
                items(notes) { note ->
                    NoteTile(note = note, color = color)
                }
            }
        }
    }
}

@Composable
private fun NoteTile(note: Note, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(color))
            Box(modifier = Modifier.padding(12.dp)) {
                if (note.imageUrl != null) {
                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(4.dp))
                            note.content.take(80).let { preview ->
                                Text(preview, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray.copy(alpha = 0.2f))) {
                            SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.Crop)
                        }
                    }
                } else {
                    Text(note.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(note.content, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                note.timestamp?.let {
                    Text(formatShortDate(it), style = MaterialTheme.typography.labelSmall, color = Color.LightGray, modifier = Modifier.align(Alignment.BottomEnd).padding(top = 4.dp))
                }
            }
        }
    }
}
```

This uses `heightIn`, add import:
```kotlin
import androidx.compose.foundation.layout.heightIn
```

- [ ] **Step 2: Create progress composable**

```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.theme.color.Aqua

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

@Composable
fun SubjectDetailProgress(
    progressPercent: Float,
    completedCount: Int,
    totalCount: Int,
    subjectColorHex: String
) {
    val color = parseSubjectColor(subjectColorHex)
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )
    val pendingCount = totalCount - completedCount

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Progreso general", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        // Progress bar
        Box(
            modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)).background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(color)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color)
                Text(if (progressPercent >= 1f) "¡Completaste todo!" else "Has completado el ${(progressPercent * 100).toInt()}% de tus evaluaciones", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    Spacer(Modifier.width(6.dp))
                    Text("$completedCount completadas", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color.LightGray))
                    Spacer(Modifier.width(6.dp))
                    Text("$pendingCount pendientes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailNoteTile.kt app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailProgress.kt
git commit -m "feat: create SubjectDetailNoteTile and SubjectDetailProgress components"
```

---

### Task 10: Assemble SubjectDetailScreen

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt`
- Make sure directory `components/` exists

- [ ] **Step 1: Check/create components directory**

```bash
mkdir -p app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components
```

- [ ] **Step 2: Replace SubjectDetailScreen with full redesign**

Replace the entire file:
```kotlin
package com.example.notifast.presentation.feature.subjectdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailCalendarRow
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailHero
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailNotesMosaic
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailProgress
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailStats
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailTimeline
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onAddNoteClick: (String) -> Unit = {},
    onAddEvaluationClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        if (uiState.isLoading) { NotiFastCircularLoading(); return@Box }
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBackClick, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                }
            },
            floatingActionButton = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(
                        onClick = { onAddNoteClick(uiState.subjectName) },
                        containerColor = Ruby,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar apunte", modifier = Modifier.size(24.dp))
                    }
                    FloatingActionButton(
                        onClick = { onAddEvaluationClick(uiState.subjectName) },
                        containerColor = Aqua,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Agregar evaluación", modifier = Modifier.size(24.dp))
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Hero
                item(key = "hero") {
                    SubjectDetailHero(
                        subjectName = uiState.subjectName,
                        subjectIcon = uiState.subjectIcon,
                        subjectColorHex = uiState.subjectColorHex,
                        noteCount = uiState.notes.size,
                        evalCount = uiState.evaluations.size,
                        onBackClick = onBackClick
                    )
                }

                // Stats
                item(key = "stats") {
                    SubjectDetailStats(
                        averageGrade = uiState.averageGrade,
                        completedEvalCount = uiState.completedEvalCount,
                        pendingEvalCount = uiState.pendingEvalCount,
                        totalEvalCount = uiState.evaluations.size,
                        noteCount = uiState.notes.size,
                        subjectColorHex = uiState.subjectColorHex
                    )
                }

                // Timeline
                item(key = "timeline") {
                    SubjectDetailTimeline(
                        upcomingEvaluations = uiState.upcomingEvaluations,
                        subjectColorHex = uiState.subjectColorHex
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // Calendar
                item(key = "calendar") {
                    SubjectDetailCalendarRow(
                        evaluations = uiState.evaluations.filter { it.date != null },
                        subjectColorHex = uiState.subjectColorHex
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // Notes mosaic
                item(key = "notes") {
                    SubjectDetailNotesMosaic(
                        notes = uiState.notes,
                        subjectColorHex = uiState.subjectColorHex
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // Progress
                item(key = "progress") {
                    SubjectDetailProgress(
                        progressPercent = uiState.progressPercent,
                        completedCount = uiState.completedEvalCount,
                        totalCount = uiState.evaluations.size,
                        subjectColorHex = uiState.subjectColorHex
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/
git commit -m "feat: redesign SubjectDetailScreen with all visual sections"
```

---

### Task 11: Verify build

- [ ] **Step 1: Run assembleDebug**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Fix any compilation errors** (iterate until clean)
