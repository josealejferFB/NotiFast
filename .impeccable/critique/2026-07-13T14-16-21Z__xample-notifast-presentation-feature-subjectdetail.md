---
target: subjectdetail
total_score: 25
p0_count: 0
p1_count: 2
timestamp: 2026-07-13T14-16-21Z
slug: xample-notifast-presentation-feature-subjectdetail
---
Method: dual-agent (A: manual fallback · B: skipped due to native platform)

### Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Good loading states and speed dial feedback. |
| 2 | Match System / Real World | 4 | Clear domain language (schedules, notes, grades). |
| 3 | User Control and Freedom | 3 | Back buttons and cancel buttons available. |
| 4 | Consistency and Standards | 2 | Speed dial code is duplicated instead of reused; layout feels like a dense dashboard rather than a focused detail view. |
| 5 | Error Prevention | 3 | Solid grade validation with min/max constraints. |
| 6 | Recognition Rather Than Recall | 3 | Too many sections visible at once, causing clutter. |
| 7 | Flexibility and Efficiency | 2 | Speed dial groups actions, but no fast paths. |
| 8 | Aesthetic and Minimalist Design | 1 | Extreme cognitive load: 7 different complex sections stacked vertically on a single screen. |
| 9 | Error Recovery | 3 | Grade dialog validates and prevents bad input gracefully. |
| 10 | Help and Documentation | 1 | No contextual help available. |
| **Total** | | **25/40** | **Acceptable** |

### Anti-Patterns Verdict

**LLM assessment**: The screen suffers from "dashboard syndrome"—throwing every possible piece of data (Stats, Timeline, Calendar, Evaluations list, Notes mosaic, Progress bar) into a single vertical scrolling list. This creates massive cognitive load. Additionally, the custom Speed Dial is a copy-paste implementation with severe accessibility flaws, indicating a lack of component reuse.

**Deterministic scan**: Unavailable. Automated HTML/CSS detector and browser overlays do not apply to native Android Compose code.

### Overall Impression
The screen functions well as an all-in-one hub for a subject, but the sheer volume of information is overwhelming. The single biggest opportunity is to introduce **Tabs** (e.g., "Overview", "Evaluations", "Notes") to chunk the information and drastically reduce cognitive load.

### What's Working
1. **Grade Validation**: The `GradeDialog` has excellent error prevention, enforcing min/max grades with clear error messages and smart defaults (`isPresented` auto-checks).
2. **Delete Confirmation**: Critical destructive actions correctly use an `AlertDialog` to prevent accidental data loss.

### Priority Issues

- **[P1] Speed Dial Accessibility Gap**:
  - **Why it matters**: The `FloatingActionButton` items in the Speed Dial have `contentDescription = null`. Screen readers will announce "unlabeled button", completely blocking visually impaired users from creating new items.
  - **Fix**: Add descriptive `contentDescription`s to the icons or group the Text and FAB into a single focusable semantic row.
  - **Suggested command**: `/impeccable harden`

- **[P1] Cognitive Overload (The Wall of Information)**:
  - **Why it matters**: Stacking 7 dense sections vertically overwhelms the user and hides important information deep down the scroll view.
  - **Fix**: Implement a `TabRow` to separate "Overview" (Stats, Progress), "Evaluations" (Timeline, List), and "Notes" (Mosaic).
  - **Suggested command**: `/impeccable layout`

- **[P2] Duplicated Component Logic**:
  - **Why it matters**: The Speed Dial logic is manually copy-pasted and modified from the main screen's `NotiFastExpandableFab`, leading to inconsistencies and harder maintenance.
  - **Fix**: Extract this into a reusable, configurable component.
  - **Suggested command**: `/impeccable extract`

### Persona Red Flags

**Jordan (First-Timer)**: The sheer amount of sections (Hero, Stats, Timeline, Calendar, Evaluations, Notes, Progress) all stacked vertically on a single page causes immediate cognitive overload. Jordan will not know where to start or what the primary action of the page is, likely feeling lost.

**Sam (Accessibility-Dependent)**: The Speed Dial options are constructed with a floating text `Surface` and a `FloatingActionButton` side-by-side, but the FAB itself has `contentDescription = null`. When tabbing, the screen reader will announce "unlabeled button" for the most critical actions on the screen (Add Note, Add Evaluation), effectively locking Sam out.

### Minor Observations
- The `GradeDialog` input type is `KeyboardType.Decimal`, which is great, but could benefit from an `imeAction = ImeAction.Done` to close the keyboard easily.
- The `SubjectDetailNoteDetail` has a manual "Cerrar" button at the bottom and a close icon at the top. The top icon is very small (`size(18.dp)`) on images, which is below the 48dp minimum touch target.

### Questions to Consider
- Does the user really need to see the `SubjectDetailTimeline` and the `SubjectDetailCalendarRow` at the same time, or do they serve the exact same purpose?
- What if we used a Tabbed layout to separate "Evaluations" from "Notes"?
