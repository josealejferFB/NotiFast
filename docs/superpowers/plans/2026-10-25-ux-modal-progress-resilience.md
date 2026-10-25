# UX Modal & Progress Resilience (NotiFast UI/UX Pro Max) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate all keyboard/IME dismiss collisions, prevent accidental swipe/scrim loss of progress in modals, enforce three-tier BackHandler navigation, eliminate double-submits, and preserve active drafts across process recreation.

**Architecture:** Compose MVI Presentation Layer hardening with Material 3 `ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false)`, `confirmValueChange` gesture interceptors, `Modifier.imePadding()` ergonomics, atomic `NotiFastButton` debouncing (600ms), and ViewModel `SavedStateHandle` draft persistence with double-submit mutual exclusion.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3, Hilt 2.59, SavedStateHandle, JUnit 4, MockK, Coroutines Test.

**Spec:** USER_REQUEST specification: 4 implementation pillars + Clean Architecture / AGENTS.md hygiene standards.

## Global Constraints

- Max ~250 lines per composable file.
- Zero hardcoded strings (all user-visible copy in `res/values/strings.xml`, Spanish UI).
- WCAG AAA theme compliance: use `MaterialTheme.colorScheme.isDark`, never `isSystemInDarkTheme()`.
- Use atoms in `presentation/atoms/` (`NotiFastButton`, `NotiFastCard`, `NotiFastDialog`, etc.).
- Zero business logic in Composables; all validation and state management in ViewModels.
- All existing 684 unit tests must remain green; new tests must pass before completion.

## Review Focus

- Pressing Back while keyboard is open in a dirty BottomSheet must hide the keyboard ONLY; it must not show the discard dialog or dismiss the sheet.
- Swiping down on a dirty BottomSheet must be physically blocked and trigger the discard confirmation dialog.
- Tapping outside the sheet (scrim) with unsaved changes must trigger the discard confirmation dialog without losing progress.
- Rapid double-tapping on "Guardar" buttons must register exactly one save operation.
- Recreating `NoteEditorViewModel` with an existing `SavedStateHandle` containing draft title/content must restore the draft without data loss.

---

### Task 1: Add Localized Strings for Discard Confirmations

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Consumes: None
- Produces: `R.string.edit_profile_discard_title`, `R.string.edit_profile_discard_message`

- [ ] **Step 1: Add edit profile discard strings to `strings.xml`**
- [ ] **Step 2: Verify strings format with `./gradlew assembleDebug` check**

---

### Task 2: Atomic Debounce & Double-Tap Protection in `NotiFastButton`

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/atoms/NotiFastButton.kt`

**Interfaces:**
- Consumes: `android.os.SystemClock.uptimeMillis()`
- Produces: Debounced `onClick` (600ms threshold) in `NotiFastButton`

- [ ] **Step 1: Implement 600ms uptimeMillis debounce in `NotiFastButton`**
- [ ] **Step 2: Verify existing button tests and preview rendering**

---

### Task 3: Shielding `NotiFastCreationBottomSheet` (Three-Tier Back, Swipe Intercept, Scrim Shield)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/atoms/NotiFastCreationBottomSheet.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/components/ScheduleEditorSheetLayout.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/components/EvaluationEditorSheetLayout.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/AddEditTeacherBottomSheet.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/studyhub/components/CoverTemplateEditorSheet.kt`

**Interfaces:**
- Consumes: `ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false)`, `WindowInsets.isImeVisible`
- Produces: Hardened `NotiFastCreationBottomSheet` protecting all nested sheets from accidental dismiss

- [ ] **Step 1: Update `NotiFastCreationBottomSheet` to configure `shouldDismissOnBackPress = false`, `confirmValueChange` swipe interceptor, and three-tier BackHandler**
- [ ] **Step 2: Update `ScheduleEditorSheetLayout` and `EvaluationEditorSheetLayout` to add `.imePadding()` and use `NotiFastCreationBottomSheet`'s protected sheet state**
- [ ] **Step 3: Update `AddEditTeacherBottomSheet` to add `.imePadding()` and use `NotiFastCreationBottomSheet`'s protected sheet state**
- [ ] **Step 4: Verify SRP (<250 LOC per file) and compile**

---

### Task 4: Hardening `NoteEditorBottomSheet` (IME Priority, Swipe Shield, Docked Toolbar IME Padding)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorBottomSheet.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorContent.kt`

**Interfaces:**
- Consumes: `ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false)`
- Produces: Keyboard-resilient, swipe-proof Note Editor with smoothly docked toolbar

- [ ] **Step 1: Update `NoteEditorBottomSheet` sheetState with `confirmValueChange`, `shouldDismissOnBackPress = false`, and three-tier BackHandler**
- [ ] **Step 2: Update docked accessory toolbar with `.navigationBarsPadding().imePadding()`**
- [ ] **Step 3: Add `KeyboardOptions(imeAction = ImeAction.Next)` on title field in `NoteEditorContent`**
- [ ] **Step 4: Verify SRP (<250 LOC per file) and compile**

---

### Task 5: Three-Tier Back Handling in Full Screens (`AddSubjectScreen` & `EditProfileScreen`)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editprofile/EditProfileScreen.kt`

**Interfaces:**
- Consumes: `WindowInsets.isImeVisible`, `LocalSoftwareKeyboardController`, `LocalFocusManager`
- Produces: Keyboard-safe navigation in full screen forms with discard confirmation

- [ ] **Step 1: Fix `AddSubjectScreen` BackHandler to prioritize hiding keyboard before triggering discard dialog**
- [ ] **Step 2: Implement three-tier BackHandler, unsaved changes detection, and discard dialog in `EditProfileScreen`**
- [ ] **Step 3: Replace standard `Button` in `EditProfileScreen` with `NotiFastButton`**
- [ ] **Step 4: Verify SRP (<250 LOC per file) and compile**

---

### Task 6: ViewModel Anti-Double Submit & Draft State Preservation (`NoteEditorViewModel`, etc.)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/AddEvaluationViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editevaluation/EditEvaluationViewModel.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorViewModelTest.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/addsubject/AddSubjectViewModelTest.kt`

**Interfaces:**
- Consumes: `SavedStateHandle`
- Produces: Survives process death for unsaved drafts; prevents concurrent duplicate writes

- [ ] **Step 1: Write unit tests in `NoteEditorViewModelTest` for anti-double submit, draft persistence in `SavedStateHandle`, and draft cleanup**
- [ ] **Step 2: Write unit test in `AddSubjectViewModelTest` for anti-double submit**
- [ ] **Step 3: Run tests to verify failure (Red)**
- [ ] **Step 4: Implement `SavedStateHandle` draft persistence and anti-double submit in `NoteEditorViewModel`**
- [ ] **Step 5: Implement anti-double submit guards in `AddSubjectViewModel`, `AddScheduleViewModel`, `EditScheduleViewModel`, `AddEvaluationViewModel`, `EditEvaluationViewModel`**
- [ ] **Step 6: Run tests to verify all pass (Green)**

---

### Task 7: Full Verification, ADB Physical Device Testing & Roadmap Status Update

**Files:**
- Modify: `docs/prd/STATUS.md`

- [ ] **Step 1: Run full unit test suite `./gradlew testDebugUnitTest` to verify all tests pass**
- [ ] **Step 2: Assemble debug build `./gradlew assembleDebug`**
- [ ] **Step 3: Deploy and verify on physical test device via ADB (`192.168.1.6:34573`)**
- [ ] **Step 4: Update `docs/prd/STATUS.md` with UX resilience achievements and test counts**
