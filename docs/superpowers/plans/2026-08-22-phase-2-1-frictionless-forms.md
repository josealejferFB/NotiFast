# Phase 2.1 — Frictionless Forms & Ergonomics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform all creation and editing forms in NotiFast (Subjects, Schedules, Notes, Evaluations) from cumbersome multi-step accordions and raw text inputs into continuous, one-page frictionless ergonomic forms with single-tap selectors, native Material 3 pickers (TimePicker, DatePicker, Sliders), and live animated previews.

**Architecture:** Clean Architecture + MVI/MVVM with Jetpack Compose. Eradication of monolithic composables by extracting sub-components into dedicated `components/` modules (strictly adhering to SRP and <= 250 LOC per file). Single immutable StateFlow `uiState` per ViewModel with `onEvent()` intent handling.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3 (BOM 2024.09.00), Compose Animation (`animateColorAsState`, `AnimatedVisibility`), Hilt DI, JUnit 4, Kotlinx Coroutines Test.

**Spec:** [`docs/prd/KICKOFF_FASE_2.1.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_2.1.md)

## Global Constraints & Grill-Me Decisions
- **TimePicker Format:** Adaptive native system format (12h AM/PM or 24h) based on `android.text.format.DateFormat.is24HourFormat(context)`, persisted as standard `"HH:mm"`.
- **Time Autocorrection:** If user selects `endTime <= startTime`, automatically correct `endTime` to `startTime + 1h` / `2h` and notify through smooth UI update.
- **Weighting Slider:** Discrete 5% steps (0%, 5%, 10%, 15%, ..., 100%) with live available percentage badge (e.g., *"Disponible en la materia: 30%"*).
- **Empty Subjects Flow:** Interactive chip *"➕ Crear Materia"* leading directly to `AddSubjectScreen`.
- **Quick Tag Suggestions (Toggleable):** `#resumen`, `#examen`, `#fórmulas`, `#tarea`, `#laboratorio`, `#proyecto`, `#glosario`, `#bibliografía`.
- **LOC Limit:** Maximum ~250 lines per composable file. All reusable sub-components in `components/`.
- **Design Tokens:** 100% adherence to NotiFast design system (`Ruby` `#BF092F`, `Aqua` `#3B9797`, `MaterialTheme.colorScheme.*`, `NotiFast*` atoms).
- **Language:** 100% Spanish strings in `res/values/strings.xml`, English code & tests.

---

## File Structure & Decomposition Plan

```
app/src/main/java/com/notifast/app/presentation/feature/
├── addsubject/
│   ├── AddSubjectScreen.kt                     # Continuous one-page screen (<= 200 LOC)
│   ├── AddSubjectViewModel.kt                  # State management & validation
│   ├── AddSubjectContract.kt                   # State & Events
│   └── components/
│       ├── SubjectPreviewBadge.kt              # [NEW] Live animated preview badge with icon + color
│       ├── SubjectColorGrid.kt                 # [NEW] 12-color compact selector grid
│       └── SubjectIconGrid.kt                  # [NEW] Theme icon picker grid
│
├── addschedule/
│   ├── AddScheduleScreen.kt                    # Continuous schedule screen (<= 220 LOC)
│   ├── AddScheduleViewModel.kt                 # Conflict check & time autocorrection logic
│   ├── AddScheduleContract.kt                  # State & Events
│   └── components/                             # [NEW DIRECTORY]
│       ├── ScheduleDaySegmentedBar.kt          # [NEW] 7-day single-tap segmented bar (L-D)
│       ├── ScheduleTimeRangePicker.kt          # [NEW] Adaptive M3 TimePicker dialog + quick presets
│       └── ScheduleSubjectChips.kt             # [NEW] Horizontal scrollable chips + "Crear Materia" action
│
├── editschedule/
│   └── EditScheduleScreen.kt                   # Refactored using shared schedule components (<= 200 LOC)
│
├── addnote/
│   ├── AddNoteScreen.kt                        # Frictionless note screen (<= 200 LOC)
│   ├── AddNoteViewModel.kt                     # State & tags toggle logic
│   ├── AddNoteContract.kt                      # State & Events
│   └── components/
│       ├── NoteSubjectChips.kt                 # [NEW] Horizontal subject chip carousel + empty action
│       ├── NoteQuickTagRow.kt                  # [NEW] 8 preset toggle tag chips (#resumen, #glosario, etc.)
│       ├── NoteFormattingToolbar.kt            # Existing markdown toolbar
│       └── NoteTagSelector.kt                  # Existing custom tag chip list
│
├── editnote/
│   └── EditNoteScreen.kt                       # Refactored using NoteSubjectChips & NoteQuickTagRow
│
├── addevaluation/
│   ├── AddEvaluationScreen.kt                  # One-page evaluation screen (<= 220 LOC)
│   ├── AddEvaluationViewModel.kt               # 5% stepped weighting & date state
│   ├── AddEvaluationContract.kt                # State & Events
│   └── components/
│       ├── EvaluationWeightSlider.kt           # [NEW] 5% discrete slider (0-100%) with available % badge
│       ├── EvaluationDatePickerField.kt        # [NEW] Native M3 DatePicker modal trigger
│       ├── EvaluationSubjectSelector.kt        # Refactored to horizontal chips + empty action
│       └── EvaluationTypeSelector.kt           # Horizontal card/pill type selector
│
└── editevaluation/
    └── EditEvaluationScreen.kt                 # Refactored to one-page continuous form (<= 220 LOC)
```

---

## Task Decomposition

### Task 1: String Resources & Design Tokens for Phase 2.1

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Consumes: Existing strings
- Produces: Localized strings for segmented day names, time presets ("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00", "Personalizado"), quick tags (`#resumen`, `#examen`, `#fórmulas`, `#tarea`, `#laboratorio`, `#proyecto`, `#glosario`, `#bibliografía`), empty subject action ("➕ Crear Materia"), and weight badge strings.

- [ ] **Step 1: Add new localized string resources in `strings.xml`**
- [ ] **Step 2: Verify build with `./gradlew assembleDebug`**
- [ ] **Step 3: Commit string tokens**

---

### Task 2: Sprint 2.1.1 — Subject Components (`SubjectPreviewBadge`, `SubjectColorGrid`, `SubjectIconGrid`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/components/SubjectPreviewBadge.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/components/SubjectColorGrid.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/components/SubjectIconGrid.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectScreen.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/addsubject/AddSubjectValidationTest.kt`

**Interfaces:**
- Consumes: `AddSubjectContract.UiState`, `SubjectIconMapper`, `subjectColorPalette`
- Produces: `SubjectPreviewBadge(iconName, hexColor)`, `SubjectColorGrid(selectedColor, onColorSelected)`, `SubjectIconGrid(selectedIcon, previewColor, onIconSelected)`

- [ ] **Step 1: Write unit tests in `AddSubjectValidationTest.kt` for instant color/icon updates and duplicate name rejection**
- [ ] **Step 2: Run test to verify behavior**
- [ ] **Step 3: Create `SubjectPreviewBadge.kt` with `animateColorAsState` and 90dp rounded card**
- [ ] **Step 4: Create `SubjectColorGrid.kt` with 12-color compact selector circles**
- [ ] **Step 5: Create `SubjectIconGrid.kt` with theme icon grid**
- [ ] **Step 6: Refactor `AddSubjectScreen.kt` into a single continuous One-Page form (< 200 LOC)**
- [ ] **Step 7: Verify with `./gradlew testDebugUnitTest` and commit**

---

### Task 3: Sprint 2.1.2 — Schedule Ergonomic Components (`ScheduleDaySegmentedBar`, `ScheduleTimeRangePicker`, `ScheduleSubjectChips`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/components/ScheduleDaySegmentedBar.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/components/ScheduleTimeRangePicker.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/components/ScheduleSubjectChips.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModel.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModelTest.kt`

**Interfaces:**
- Consumes: `Schedule.DAY_NAMES`, `SubjectData`, `android.text.format.DateFormat`
- Produces:
  * `ScheduleDaySegmentedBar(selectedDayIndex: Int, onDaySelected: (Int) -> Unit)`
  * `ScheduleTimeRangePicker(startTime: String, endTime: String, onTimeRangeChanged: (String, String) -> Unit)`
  * `ScheduleSubjectChips(subjects: List<SubjectData>, selectedSubject: String, onSubjectSelected: (String) -> Unit, onCreateSubjectClick: () -> Unit)`

- [ ] **Step 1: Add unit tests for time autocorrection (when `endTime <= startTime`) and day selection in `AddScheduleViewModelTest.kt`**
- [ ] **Step 2: Implement time autocorrection logic in `AddScheduleViewModel.kt`**
- [ ] **Step 3: Build `ScheduleDaySegmentedBar.kt` (7 single-tap pills with animated selection)**
- [ ] **Step 4: Build `ScheduleTimeRangePicker.kt` with adaptive 12h/24h Material 3 `TimePickerDialog` + fast presets**
- [ ] **Step 5: Build `ScheduleSubjectChips.kt` horizontal chips with "➕ Crear Materia" action**
- [ ] **Step 6: Refactor `AddScheduleScreen.kt` and `EditScheduleScreen.kt` into continuous ergonomic forms (< 220 LOC each)**
- [ ] **Step 7: Run `./gradlew testDebugUnitTest` and commit**

---

### Task 4: Sprint 2.1.3 (Part 1) — Notes Frictionless Ergonomics (`NoteSubjectChips` & `NoteQuickTagRow`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addnote/components/NoteSubjectChips.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addnote/components/NoteQuickTagRow.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addnote/AddNoteScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editnote/EditNoteScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addnote/AddNoteViewModel.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/addnote/AddNoteViewModelTest.kt`

**Interfaces:**
- Consumes: `SubjectData`, `uiState.tags`
- Produces:
  * `NoteSubjectChips(subjects: List<SubjectData>, selectedSubject: String, onSubjectSelected: (String) -> Unit, onCreateSubjectClick: () -> Unit)`
  * `NoteQuickTagRow(selectedTags: List<String>, onTagToggled: (String) -> Unit)`

- [ ] **Step 1: Add quick tag toggle unit tests in `AddNoteViewModelTest.kt`**
- [ ] **Step 2: Implement `ToggleTag` event handling in `AddNoteViewModel.kt`**
- [ ] **Step 3: Build `NoteSubjectChips.kt` with instant chip selection and "➕ Crear Materia" chip**
- [ ] **Step 4: Build `NoteQuickTagRow.kt` with 8 preset toggleable tags**
- [ ] **Step 5: Refactor `AddNoteScreen.kt` and `EditNoteScreen.kt` to use the new ergonomic components (< 200 LOC each)**
- [ ] **Step 6: Run `./gradlew testDebugUnitTest` and commit**

---

### Task 5: Sprint 2.1.3 (Part 2) — Evaluation Continuous Form (`EvaluationWeightSlider` & `EvaluationDatePickerField`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/components/EvaluationWeightSlider.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/components/EvaluationDatePickerField.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/components/EvaluationSubjectSelector.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/AddEvaluationScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editevaluation/EditEvaluationScreen.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/addevaluation/EvaluationInputValidationTest.kt`

**Interfaces:**
- Consumes: `EvaluationType`, `uiState.weight`, `selectedDate: Date`
- Produces:
  * `EvaluationWeightSlider(weight: String, onWeightChanged: (String) -> Unit, availableSubjectWeight: Int)` (5% discrete steps with available % badge)
  * `EvaluationDatePickerField(selectedDate: Date, onDateSelected: (Date) -> Unit)`

- [ ] **Step 1: Write unit tests in `EvaluationInputValidationTest.kt` for 5% discrete slider steps and available weight formatting**
- [ ] **Step 2: Build `EvaluationWeightSlider.kt` with discrete steps of 5% and available percentage badge**
- [ ] **Step 3: Build `EvaluationDatePickerField.kt` with native M3 `DatePickerDialog`**
- [ ] **Step 4: Refactor `EvaluationSubjectSelector.kt` to horizontal chips + "➕ Crear Materia"**
- [ ] **Step 5: Eradicate `StepCard` accordions from `AddEvaluationScreen.kt` and `EditEvaluationScreen.kt` creating seamless continuous forms (< 220 LOC each)**
- [ ] **Step 6: Run `./gradlew testDebugUnitTest` and commit**

---

### Task 6: Full Regression Verification & Phase 2.1 Closeout

**Files:**
- Modify: `docs/prd/STATUS.md`

- [ ] **Step 1: Run full unit test suite `./gradlew testDebugUnitTest`**
- [ ] **Step 2: Build full debug APK `./gradlew assembleDebug`**
- [ ] **Step 3: Update `docs/prd/STATUS.md` to reflect Phase 2.1 at 100%**
- [ ] **Step 4: Generate `docs/prd/KICKOFF_FASE_3.md` (Predictive Intelligence & Notifications)**
- [ ] **Step 5: Final commit & merge readiness**
