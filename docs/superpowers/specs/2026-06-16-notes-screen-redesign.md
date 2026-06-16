# Notes Screen Redesign — Spec

## Scope

Two changes to the Notes screen (`presentation/feature/notes/`):

1. **Compress `NoteEstheticCard`** — reduce vertical footprint
2. **Replace `SubjectFeaturedCard` with inline expandable SubjectCard** — rich per-subject dashboard

---

## 1. NoteEstheticCard Compression

### Current
- Outer Box: `373x208.dp` with `padding(vertical = 8.dp)`
- Main Card: `363x171.dp`
- Overlapping Card: `174x202.dp`

### Target
- Outer Box: `373x148.dp` with `padding(vertical = 4.dp)`
- Main Card: height reduced proportionally (~120dp)
- Overlapping Card: `140x140.dp`
- Content preview: 1 line instead of 3
- Edit/Delete icons: kept at bottom-right

### Visual
```
┌─────────────────────────┐
│  ┌──────┐               │
│  │ icono│  Título        │
│  │ mat. │  Preview...    │
│  │      │           ✏️ 🗑️│
│  └──────┘               │
└─────────────────────────┘
```

No behavior change — tap still opens the expandable detail view.

---

## 2. Inline Expandable SubjectCard

### Data Model Changes

New data class in `data/model/Subject.kt`:
```kotlin
data class SubjectData(
    val name: String,
    val icon: String = "📚"
)
```

Add `icon` field to Firestore subject documents (defaults to `"📚"`).

### Repository Changes

**`NoteRepository.kt` interface:**
```kotlin
// getSubjects now returns SubjectData instead of just names
fun getSubjects(userId: String): Flow<List<SubjectData>>
```

**`NoteRepositoryImpl.kt`:**
- `getSubjects()` : read `icon` field from Firestore doc, fallback to `"📚"`
- `addSubject()` : accept and persist `icon` parameter

**`EvaluationRepository`**: no changes (already has `getEvaluations(userId)`)

### NotesViewModel Changes

New per-subject state type:
```kotlin
data class SubjectDisplayInfo(
    val name: String,
    val icon: String,
    val notes: List<Note>,
    val evaluations: List<Evaluation>,
    val averageGrade: Double?,
    val completedEvalCount: Int,
    val pendingEvalCount: Int,
    val upcomingEvaluations: List<Evaluation>  // sorted by date, upcoming only
)
```

Additions to `NotesViewModel`:
- Inject `EvaluationRepository`
- Add `evaluationRepository.getEvaluations(userId)` to the combine flow
- Compute `SubjectDisplayInfo` from raw notes + evaluations + subjects
- Add `expandedSubject: String?` state (name of expanded subject, null = collapsed)

New events:
- `ToggleSubject(name: String)` — expand/collapse a subject card

### NotesScreen Changes — Notes Section

The notes list (filter-based) continues as-is.

The **Materias section** (currently a `LazyRow` of `SubjectFeaturedCard`) becomes:
- Foreach subject in `uiState.subjectInfos`:
  - **Compact state** (~76dp):
    - Circle with gradient (generated from subject name hash)
    - Subject icon + name
    - Star rating visual based on avg grade
    - Quick stats row (📝 count · 📅 eval count · ⏳ pending count)
    - Expand chevron (˅ / ˄)
  - On tap: animate expansion inline using `AnimatedVisibility` with spring

  - **Expanded state** (added below the compact card):
    - **Próximas evaluaciones**: up to 2-3 upcoming evals
      - Each shows: title, weight %, date, urgency chip (🔴 < 7d / 🟡 < 14d / 🟢 > 14d)
      - Card style: white surface with subtle border, rounded
    - **Dashboard card**:
      - Circular average grade visualization (large number, circular progress)
      - Stats row: total evals, completed, pending
    - **Últimos apuntes**: horizontal scrollable row of mini note cards (3 max + "+N más")
      - Each mini card: gradient border from subject color, title (1 line), image thumbnail or icon
    - **Quick actions**: "➕ Apunte" and "📅 Evaluación" buttons with subject color

### AddSubjectScreen Changes

Add an **emoji picker grid**:
- Show a grid of ~20 emojis (📐🔬📖🎨⚽🌍📝🧮💻🧪📊🎵🎭🏛️🌿⚡💎🎯🧩🎪)
- Selected emoji highlighted with subject's generated color
- Save icon alongside subject name to Firestore

### Subject Icon Color Generation

Algorithm to generate a consistent vibrant color per subject:
- Hash the subject name string → produce HSL hue (0-360)
- Set saturation = 75%, lightness = 60%
- Produces a unique pastel-vibrant color per subject

---

## 3. Files Changed

| File | Change |
|------|--------|
| `data/model/Subject.kt` | **New** — `SubjectData` data class |
| `domain/repository/NoteRepository.kt` | Change `getSubjects()` return type to `Flow<List<SubjectData>>` |
| `data/repository/NoteRepositoryImpl.kt` | Update `getSubjects()`, `addSubject()` to handle icon |
| `presentation/feature/addsubject/AddSubjectContract.kt` | Add `icon` field to UI state |
| `presentation/feature/addsubject/AddSubjectScreen.kt` | Add emoji picker grid |
| `presentation/feature/addsubject/AddSubjectViewModel.kt` | Pass icon to repository |
| `presentation/feature/addnote/AddNoteContract.kt` | Update `subjects` type |
| `presentation/feature/addnote/AddNoteScreen.kt` | Update for new `subjects` type |
| `presentation/feature/addnote/AddNoteViewModel.kt` | Update for new `subjects` type |
| `presentation/feature/notes/NotesContract.kt` | Add `subjectInfos`, `expandedSubject` |
| `presentation/feature/notes/NotesViewModel.kt` | Add evaluations, compute subject infos |
| `presentation/feature/notes/NotesScreen.kt` | Replace SubjectFeaturedCard, add expandable cards |
| `presentation/feature/notes/components/NoteEstheticCard.kt` | Compress height |
| `presentation/feature/notes/components/SubjectFeaturedCard.kt` | Remove or replace with new inline card |
| `presentation/feature/notes/components/SubjectCard.kt` | **New** — the expandable subject card |

---

## 4. Constraints & Notes

- **No bottom padding** on content (floating nav bar will overlay)
- Subject icon defaults to `"📚"` for existing subjects without icon
- Evaluation urgency computed client-side from `evaluation.date`
- Average grade = weighted average of all graded evaluations in that subject
- The expand/collapse animation should use same spring spec as bottom nav tooltip for consistency
