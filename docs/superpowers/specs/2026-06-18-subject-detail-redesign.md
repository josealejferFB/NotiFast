# Subject Detail Redesign — Design Spec

## Goal
Redesign SubjectDetailScreen as a stunning visual compendium of everything related to a subject, accessible via a "Más detalles" button in SubjectCard.

## UX
- "Más detalles" button in SubjectCard expanded view → navigates to SubjectDetailScreen
- Single scrollable screen with sections: Hero Header → Stats Dashboard → Timeline → Calendar → Notes Mosaic → Progress
- Floating action buttons for +Apunte and +Evaluación
- Color scheme: subject's colorHex + Ruby + Aqua + White

## Architecture

```
SubjectCard (nuevo botón "Más detalles")
  → NavGraph → SubjectDetailScreen (subjectName param)
    → SubjectDetailViewModel (loads notes + evaluations + subjects)
      → SubjectDetailUiState enriched with computed stats
```

### Data flow
1. ViewModel loads: `getNotesBySubject(userId, subjectName)` + `getEvaluations(userId)` + `getSubjects(userId)`
2. Computes: averageGrade, completedEvalCount, pendingEvalCount, upcomingEvaluations, progressPercent
3. Finds SubjectData for icon + colorHex

### New files needed
- None (modify existing SubjectDetailContract, ViewModel, Screen)

### Files to modify
- `SubjectCard.kt` — add "Más detalles" OutlinedButton in expanded quick actions
- `SubjectDetailContract.kt` — enrich UiState with SubjectData info + computed stats
- `SubjectDetailViewModel.kt` — load subjects, compute stats
- `SubjectDetailScreen.kt` — complete visual redesign
- `NavGraph.kt` — add SubjectData params if needed (not needed)

---

## Design Sections

### 0. SubjectCard — "Más detalles" button
- Add `onSubjectDetail: () -> Unit` parameter to SubjectCard
- Add a full-width `OutlinedButton` below the existing two-button row
- Text: "Más detalles", border uses the subject's color, text color subject color
- Icon: `Icons.Default.Info`

### 1. Hero Header (~280dp)
- Full-width gradient: subjectColor → subjectColor.copy(alpha=0.9f) → Color.White
- Back button (top-left, white, semi-transparent bg circle)
- Large icon circle (72dp) centered: SubjectIconMapper icon on white background
- Subject name below (28sp, Bold, white)
- Subtitle: "X apuntes · Y evaluaciones" (14sp, white.copy(alpha=0.7f))
- Gradient fades to white at bottom for seamless transition

### 2. Stats Dashboard (Grid 2×2)
Four square cards with white bg, rounded corners 16dp, subtle shadow, subject color accent (4dp left border):

| **Score Card** (large) | **Evaluaciones Card** |
|---|---|
| Circular score (72dp circle, subjectColor bg) | "Evaluaciones" label |
| Average grade in large text | Completed/Total count |
| "Promedio" label below | Mini progress ring |

| **Apuntes Card** | **Pendientes Card** |
|---|---|
| "Apuntes" label | "Pendientes" label |
| Total count in large text | Pending count |
| Notes icon tinted subjectColor | Countdown to next eval |

### 3. Timeline de Evaluaciones Próximas
- Section header: "Próximas evaluaciones" with calendar icon
- Vertical timeline: subjectColor line on left, dots per evaluation
- Each eval: title (bold), date, weight%, urgency chip
- Colors: red (≤7d), orange (≤14d), green (>14d), gray (vencida)
- Empty state: "¡Todo al día!" with green check

### 4. Calendario Cuadrado (LazyRow)
- Horizontal row of square cards (120×120dp)
- Top section: month abbreviation + day number (large, bold)
- Urgency color bar below day number
- Bottom: evaluation title (1 line)
- Tag with weight % if applicable
- Cards have white bg, subject color border, slight shadow

### 5. Mosaico de Apuntes (LazyVerticalGrid)
- `GridCells.Adaptive(160.dp)` within LazyColumn
- Each card: 160×120dp, white bg, rounded 12dp, shadow
- Top bar: 4dp subjectColor stripe
- Title (bold, 2 lines max)
- Content preview (1 line, gray)
- Date stamp (small, bottom-right)
- Image thumbnail if available (top-right corner, 40dp rounded)

### 6. Progreso General
- Section header: "Progreso general"
- Wide rounded progress bar: completed (subjectColor) vs pending (light gray)
- Percentage text (large, bold, subjectColor)
- Motivational text: "Has completado el X% de tus evaluaciones"
- Animated progress fill on appear

### 7. FABs
- Two separate FABs, stacked vertically in bottom-end corner with 12dp spacing
- Top FAB: +Apunte (Ruby, icon: Add)
- Bottom FAB: +Evaluación (Aqua, icon: CalendarMonth)
- Each navigates to the respective add screen with the subject pre-selected

---

## What's NOT Changed
- NoteRepository / EvaluationRepository interfaces
- Navigation routes
- SubjectCard expand/collapse behavior
- NotesScreen layout
- No new dependencies

## Edge Cases
- Subject with no notes → mosaic section shows friendly "Sin apuntes aún" card
- Subject with no evaluations → timeline + calendar show friendly empty states, progress shows 0%
- Subject with no upcoming evaluations → timeline shows "¡Todo al día!"
- Subject with very long name → truncate with ellipsis in header
- Subject with no colorHex → fallback to HSL hash (same as SubjectCard)
