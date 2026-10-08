# Live Rich-Text Canvas & Docked Accessory Bar Implementation Plan (v2.0)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide an ergonomic, high-performance "Word / Google Docs Mobile" note editing experience in NotiFast: real-time typography styling (`MarkdownVisualTransformation` cached with `remember`), zero keyboard flickering, cursor centering inside `**|**` with active toggle feedback on toolbar buttons, smart Enter list continuation with auto-cancellation, and inverted visual hierarchy placing Title and Content at the very top.

**Architecture:** Clean Architecture + MVI. `SmartMarkdownFormatter` provides pure functional algebra for `TextFieldValue` manipulation (wrapping, unwrapping, cursor placement, and Enter continuation). `BringIntoViewRequester` guarantees the cursor is never covered by the keyboard or docked toolbar. Persistence remains 100% valid, immutable Markdown.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose, Material 3, Hilt, JUnit4.

**Spec:** [`docs/superpowers/specs/2026-10-08-note-editor-live-rich-text-design.md`](file:///home/analista/Documents/Projects/Notifast/docs/superpowers/specs/2026-10-08-note-editor-live-rich-text-design.md)

---

## Global Constraints

- **Single Responsibility & Size Limits:** No Composable file may exceed ~250 lines of code.
- **Zero Hardcoded Strings:** Every user-visible text must reside in `res/values/strings.xml` in Spanish.
- **Zero Hardcoded Colors:** All colors must be sourced from `MaterialTheme.colorScheme` and corporate brand tokens (`Ruby` `#BF092F`, `Aqua` `#3B9797`).
- **Data Invariant:** Output text must remain 100% valid Markdown string for database persistence and PDF export.
- **Cursor Stability Invariant:** `MarkdownVisualTransformation` must never change the character length (`transformed.text.length == original.text.length`), using `OffsetMapping.Identity`.
- **Zero-Recomposition Stutter:** `MarkdownVisualTransformation` must be remembered across recompositions (`remember(colorScheme)`).

---

### Completed Baseline Tasks
- [x] **Task 1:** String Resources & Semantic Labels (ad679b0)
- [x] **Task 2:** TDD for `MarkdownVisualTransformation` (6f14a17)
- [x] **Task 3:** Create `DockedAccessoryToolbar` (a08d6ab)
- [x] **Task 4:** Reading Mode in `EditorTopBar` & `NoteEditorBottomSheet` (5e715ec)
- [x] **Task 5:** Refactor `NoteEditorContent.kt` & Canvas Sizing (063a54d)
- [x] **Task 6:** Dock Toolbar with `imePadding` in Screen and BottomSheet (d03165e)
- [x] **Task 7:** Full Test Suite & Lint Verification (393/393 unit tests green)

---

### Task 8: TDD for `SmartMarkdownFormatter` (Toggle, Cursor Placement & Smart Enter)

**Files:**
- Create: `app/src/test/java/com/notifast/app/presentation/feature/noteeditor/util/SmartMarkdownFormatterTest.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/util/SmartMarkdownFormatter.kt`

**Interfaces:**
- Produces:
  ```kotlin
  enum class FormatStyle { BOLD, ITALIC, QUOTE, H1, H2, CHECKBOX, BULLET, LATEX, DIVIDER }
  object SmartMarkdownFormatter {
      fun toggleFormat(value: TextFieldValue, style: FormatStyle): TextFieldValue
      fun detectActiveStyles(value: TextFieldValue): Set<FormatStyle>
      fun handleSmartEnter(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue
  }
  ```

- [ ] **Step 1: Write unit tests covering toggle, cursor placement and smart enter**

Create `SmartMarkdownFormatterTest.kt`:
- Test empty selection with [BOLD]: returns text with `****` and cursor at index 2 (`**|**`).
- Test toggle off on empty `**|**`: removes `****` and returns empty string with cursor at 0.
- Test wrapping selected text with [BOLD]: wraps `**selected**`.
- Test unwrapping selected text already in bold: unwraps to `selected`.
- Test smart enter on non-empty list item `- Item`: new line gets `- `.
- Test smart enter on empty list item `- `: removes `- ` from line and exits list.
- Test smart enter on non-empty task `- [ ] Task`: new line gets `- [ ] `.
- Test smart enter on empty task `- [ ] `: removes `- [ ] ` from line and exits list.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.noteeditor.util.SmartMarkdownFormatterTest`
Expected: FAIL (unresolved reference)

- [ ] **Step 3: Implement `SmartMarkdownFormatter.kt`**

Create `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/util/SmartMarkdownFormatter.kt` implementing the algorithms.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.noteeditor.util.SmartMarkdownFormatterTest`
Expected: PASS (all tests green)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/noteeditor/util/SmartMarkdownFormatter.kt app/src/test/java/com/notifast/app/presentation/feature/noteeditor/util/SmartMarkdownFormatterTest.kt
git commit -m "feat(editor): implement SmartMarkdownFormatter with cursor centering, toggle and smart enter"
```

---

### Task 9: Integrate Active Toggle Feedback, BringIntoView & Layout Reordering

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/DockedAccessoryToolbar.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorContent.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorBottomSheet.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorScreen.kt`

**Interfaces:**
- `DockedAccessoryToolbar` receives `activeStyles: Set<FormatStyle>`, `onToggleFormat: (FormatStyle) -> Unit`.
- Active buttons render with `MaterialTheme.colorScheme.primaryContainer` and `onPrimaryContainer`.
- `NoteEditorContent.kt`:
  - `remember(colorScheme) { MarkdownVisualTransformation(colorScheme) }` cached properly.
  - Reordered: Title and Content at top; Subject chip and Tags at bottom.
  - Uses `BringIntoViewRequester` with `Modifier.bringIntoViewRequester()` on the content text field.
- Smart Enter handled on text change when user presses Enter in list/tasks.

- [ ] **Step 1: Update `DockedAccessoryToolbar.kt` with active style indicators**

Update button visual state: if `style in activeStyles`, display container `primaryContainer` and tint `onPrimaryContainer`.

- [ ] **Step 2: Update `NoteEditorContent.kt` layout order and cached VisualTransformation**

Place Title, Content, and Photo at the top. Move Subject and Tags to the bottom. Cache `MarkdownVisualTransformation`.

- [ ] **Step 3: Wire Smart Enter and BringIntoView in `NoteEditorBottomSheet.kt` and `NoteEditorScreen.kt`**

Support `TextFieldValue` tracking for cursor and selection so formatting buttons toggle properly and center cursor inside tokens.

- [ ] **Step 4: Verify compilation & run unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL (all tests passing)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/DockedAccessoryToolbar.kt app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorContent.kt app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorBottomSheet.kt app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorScreen.kt
git commit -m "feat(editor): integrate active toggle feedback, bring-into-view and reorder content hierarchy"
```

---

### Task 10: Device Verification & Visual Certification on Samsung Galaxy A06

**Files:**
- Build output: `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 1: Build & install APK on Samsung Galaxy A06 (`R8YL40QFYYJ`)**

Run: `./gradlew assembleDebug`
Run: `adb -s R8YL40QFYYJ install -r app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: Launch app, test note editor on device, and capture screenshot**

Run:
```bash
adb -s R8YL40QFYYJ shell am start -n com.notifast.app/.presentation.MainActivity
# Capture screenshot to verify live rich-text canvas and docked toolbar
adb -s R8YL40QFYYJ exec-out screencap -p > /tmp/note_editor_certified_v2.png
```

- [ ] **Step 3: Verify UX acceptance criteria**

1. No keyboard flickering upon open/close.
2. Title and Content are immediately visible at the top.
3. Tapping [B] centers cursor inside `**|**` and highlights [B] button.
4. Pressing Enter inside `- item` creates new list item automatically.
5. Content field stays visible above keyboard.
