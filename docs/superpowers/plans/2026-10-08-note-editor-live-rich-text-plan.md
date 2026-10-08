# Live Rich-Text Canvas & Docked Accessory Bar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the NotiFast note editor into a fluid "Word / Google Docs Mobile" experience with real-time typography styling (Live Markdown Canvas via `MarkdownVisualTransformation`), eliminate the debug "2 COLUMNAS" header, remove the forced preview TabRow, and dock a semantically grouped accessory toolbar directly above the virtual keyboard (`Modifier.imePadding()`).

**Architecture:** MVI architecture with Clean Architecture persistence guarantees. The Presentation layer renders Markdown visually in real time using `MarkdownVisualTransformation` with `OffsetMapping.Identity` (zero cursor jumping). The accessory bar is anchored above the keyboard, dispatching formatting insertion events. Firestore, Room, and PDF exporters continue receiving clean, immutable standard Markdown.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose, Material 3, Hilt, Android WorkManager, JUnit4.

**Spec:** [`docs/superpowers/specs/2026-10-08-note-editor-live-rich-text-design.md`](file:///home/analista/Documents/Projects/Notifast/docs/superpowers/specs/2026-10-08-note-editor-live-rich-text-design.md)

---

## Global Constraints

- **Single Responsibility & Size Limits:** No Composable file may exceed ~250 lines of code.
- **Zero Hardcoded Strings:** Every user-visible text must reside in `res/values/strings.xml` in Spanish.
- **Zero Hardcoded Colors:** All colors must be sourced from `MaterialTheme.colorScheme` and corporate brand tokens (`Ruby` `#BF092F`, `Aqua` `#3B9797`).
- **Data Invariant:** Output text must remain 100% valid Markdown string for database persistence and PDF export.
- **Cursor Stability Invariant:** `MarkdownVisualTransformation` must never change the character length (`transformed.text.length == original.text.length`), using `OffsetMapping.Identity`.
- **Systematic Debugging:** Zero blind patches; verify failure before writing implementation and verify passing after.

---

### Task 1: String Resources & Semantic Labels

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Produces: String resource IDs `R.string.editor_format_bold`, `R.string.editor_format_italic`, `R.string.editor_format_quote`, `R.string.editor_format_h1`, `R.string.editor_format_h2`, `R.string.editor_format_checkbox`, `R.string.editor_format_bullet`, `R.string.editor_format_latex`, `R.string.editor_format_divider`, `R.string.editor_format_photo`, `R.string.editor_toggle_reading_mode`.

- [ ] **Step 1: Check existing strings in `strings.xml`**

Check if any of the target string keys already exist to prevent duplicate resource compilation errors:
Run: `grep -rn "editor_format_" app/src/main/res/values/strings.xml || true`

- [ ] **Step 2: Add missing string resources to `strings.xml`**

Add the semantic toolbar tooltips, content descriptions, and reading mode toggle strings:
```xml
    <!-- Note Editor Accessory Toolbar & Reading Mode -->
    <string name="editor_format_bold">Negrita</string>
    <string name="editor_format_italic">Cursiva</string>
    <string name="editor_format_quote">Cita en bloque</string>
    <string name="editor_format_h1">Título principal</string>
    <string name="editor_format_h2">Subtítulo</string>
    <string name="editor_format_checkbox">Tarea con casilla</string>
    <string name="editor_format_bullet">Lista de viñetas</string>
    <string name="editor_format_latex">Fórmula LaTeX</string>
    <string name="editor_format_divider">Separador horizontal</string>
    <string name="editor_format_photo">Adjuntar foto de pizarra</string>
    <string name="editor_toggle_reading_mode">Modo lectura</string>
    <string name="editor_toggle_edit_mode">Modo edición</string>
```

- [ ] **Step 3: Verify resources parse cleanly**

Run: `./gradlew processDebugResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat(editor): add string resources for docked accessory toolbar and reading mode"
```

---

### Task 2: TDD for `MarkdownVisualTransformation`

**Files:**
- Create: `app/src/test/java/com/notifast/app/presentation/atoms/MarkdownVisualTransformationTest.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/atoms/MarkdownVisualTransformation.kt`

**Interfaces:**
- Produces: `class MarkdownVisualTransformation(private val colorScheme: ColorScheme) : VisualTransformation`
- Consumes: Compose `AnnotatedString`, `SpanStyle`, `TransformedText`, `OffsetMapping.Identity`.

- [ ] **Step 1: Write the failing unit test**

Create `app/src/test/java/com/notifast/app/presentation/atoms/MarkdownVisualTransformationTest.kt`:
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarkdownVisualTransformationTest {

    private lateinit var transformation: MarkdownVisualTransformation
    private val colorScheme = lightColorScheme()

    @Before
    fun setup() {
        transformation = MarkdownVisualTransformation(colorScheme)
    }

    @Test
    fun `filter preserves exact character count invariant`() {
        val input = AnnotatedString("# Título\n**Negrita** y *cursiva*\n- [ ] Tarea\n> Cita\n`código`")
        val result = transformation.filter(input)
        assertEquals(input.text.length, result.text.length)
        assertEquals(input.text, result.text.text)
    }

    @Test
    fun `filter applies bold style to double asterisks`() {
        val input = AnnotatedString("Hola **mundo** cruel")
        val result = transformation.filter(input)
        assertEquals(input.text.length, result.text.length)
        val styles = result.text.spanStyles
        assertTrue("Debe existir al menos un estilo aplicado", styles.isNotEmpty())
        val boldSpan = styles.find { it.item.fontWeight == FontWeight.Bold }
        assertTrue("Debe existir Span con FontWeight.Bold", boldSpan != null)
    }

    @Test
    fun `filter applies italic style to single asterisks and underscores`() {
        val input = AnnotatedString("Palabra *cursiva* y _otra_")
        val result = transformation.filter(input)
        val italicSpans = result.text.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
        assertEquals(2, italicSpans.size)
    }

    @Test
    fun `filter handles unclosed tokens without throwing exception`() {
        val input = AnnotatedString("**sin cerrar *otra # y nada mas")
        val result = transformation.filter(input)
        assertEquals(input.text.length, result.text.length)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.atoms.MarkdownVisualTransformationTest`
Expected: FAIL with "Unresolved reference: MarkdownVisualTransformation"

- [ ] **Step 3: Implement `MarkdownVisualTransformation`**

Create `app/src/main/java/com/notifast/app/presentation/atoms/MarkdownVisualTransformation.kt`:
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

class MarkdownVisualTransformation(
    private val colorScheme: ColorScheme
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val annotated = buildAnnotatedString {
            append(raw)
            val tokenDimStyle = SpanStyle(
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                fontSize = 13.sp
            )

            // Line-based formatting (H1, H2, H3, quotes, lists)
            var lineStart = 0
            val lines = raw.split("\n")
            for (line in lines) {
                val lineEnd = lineStart + line.length
                val trimmed = line.trimStart()
                val indent = line.length - trimmed.length

                when {
                    trimmed.startsWith("### ") -> {
                        addStyle(tokenDimStyle, lineStart + indent, lineStart + indent + 4)
                        addStyle(
                            SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary),
                            lineStart + indent + 4,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("## ") -> {
                        addStyle(tokenDimStyle, lineStart + indent, lineStart + indent + 3)
                        addStyle(
                            SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ruby),
                            lineStart + indent + 3,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("# ") -> {
                        addStyle(tokenDimStyle, lineStart + indent, lineStart + indent + 2)
                        addStyle(
                            SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = colorScheme.onSurface),
                            lineStart + indent + 2,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("> ") -> {
                        addStyle(SpanStyle(color = Aqua, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + 2)
                        addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic, color = colorScheme.onSurfaceVariant),
                            lineStart + indent + 2,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ", ignoreCase = true) -> {
                        addStyle(SpanStyle(color = colorScheme.primary, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + 6)
                    }
                    trimmed.startsWith("- ") -> {
                        addStyle(SpanStyle(color = colorScheme.primary, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + 2)
                    }
                }
                lineStart = lineEnd + 1
            }

            // Inline formatting: Bold (**text**)
            var i = 0
            while (i < raw.length) {
                if (raw.startsWith("**", i)) {
                    val end = raw.indexOf("**", i + 2)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 2)
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), i + 2, end)
                        addStyle(tokenDimStyle, end, end + 2)
                        i = end + 2
                        continue
                    }
                } else if (raw.startsWith("`", i)) {
                    val end = raw.indexOf("`", i + 1)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 1)
                        addStyle(
                            SpanStyle(fontFamily = FontFamily.Monospace, background = colorScheme.surfaceContainerHighest),
                            i + 1,
                            end
                        )
                        addStyle(tokenDimStyle, end, end + 1)
                        i = end + 1
                        continue
                    }
                } else if (raw[i] == '*' && i + 1 < raw.length && raw[i + 1] != ' ' && raw[i + 1] != '*') {
                    val end = raw.indexOf('*', i + 1)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 1)
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), i + 1, end)
                        addStyle(tokenDimStyle, end, end + 1)
                        i = end + 1
                        continue
                    }
                } else if (raw[i] == '_' && i + 1 < raw.length && raw[i + 1] != ' ') {
                    val end = raw.indexOf('_', i + 1)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 1)
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), i + 1, end)
                        addStyle(tokenDimStyle, end, end + 1)
                        i = end + 1
                        continue
                    }
                }
                i++
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.atoms.MarkdownVisualTransformationTest`
Expected: BUILD SUCCESSFUL (all 4 tests passing)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/atoms/MarkdownVisualTransformation.kt app/src/test/java/com/notifast/app/presentation/atoms/MarkdownVisualTransformationTest.kt
git commit -m "feat(editor): implement MarkdownVisualTransformation with live token styling"
```

---

### Task 3: Create `DockedAccessoryToolbar`

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/DockedAccessoryToolbar.kt`

**Interfaces:**
- Produces: `@Composable fun DockedAccessoryToolbar(onInsertFormatting: (prefix: String, suffix: String) -> Unit, onAttachPhotoClick: () -> Unit, modifier: Modifier = Modifier)`
- Consumes: Material 3 `Surface`, `Row`, `IconButton`, `VerticalDivider`, `LocalHapticFeedback`, string resources.

- [ ] **Step 1: Write `DockedAccessoryToolbar.kt`**

Create `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/DockedAccessoryToolbar.kt` with:
- Categories:
  - Text: [B], [I], [“]
  - Structure: [H1], [H2]
  - Lists: [☑], [•]
  - Insert: [Σ], [—], [📷]
- Touch targets: 42dp x 42dp, tactile rounded corners (10.dp), `HapticFeedbackType.TextHandleMove`.
- Micro-separators between categories.
- Total lines $\le 200$ LOC.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/DockedAccessoryToolbar.kt
git commit -m "feat(editor): create ergonomic DockedAccessoryToolbar with categorized tools"
```

---

### Task 4: Integrate Reading Mode in `EditorTopBar` & `NoteEditorBottomSheet`

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/EditorTopBar.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorBottomSheet.kt`

**Interfaces:**
- Consumes: `isPreviewMode: Boolean`, `onTogglePreview: () -> Unit`.
- Produces: TopBar with toggle action button (Eye icon when in edit mode, Edit icon when in preview mode).

- [ ] **Step 1: Update `EditorTopBar.kt`**

Add `isPreviewMode: Boolean = false`, `onTogglePreview: () -> Unit = {}` to `EditorTopBar` parameters and insert the toggle IconButton before the Save button.

- [ ] **Step 2: Update header in `NoteEditorBottomSheet.kt`**

Add the preview toggle button in the header of `NoteEditorBottomSheet.kt` next to the Save button.

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/EditorTopBar.kt app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorBottomSheet.kt
git commit -m "feat(editor): add reading mode toggle action in EditorTopBar and BottomSheet header"
```

---

### Task 5: Refactor `NoteEditorContent.kt` & Canvas Sizing

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorContent.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/atoms/NotiFastFormField.kt` (support `visualTransformation`)

**Interfaces:**
- Consumes: `MarkdownVisualTransformation(MaterialTheme.colorScheme)`, `NotiFastFormField(visualTransformation = ...)`.
- Produces: Unified editing canvas without redundant TabRow. If `uiState.isPreviewMode` is active (triggered via TopBar), renders `NoteLivePreviewCard`; otherwise renders the Live Canvas with `MarkdownVisualTransformation`.

- [ ] **Step 1: Add `visualTransformation` parameter to `NotiFastFormField`**

Enable `NotiFastFormField` to receive `visualTransformation: VisualTransformation = VisualTransformation.None` and forward it to `OutlinedTextField`.

- [ ] **Step 2: Refactor `NoteEditorContent.kt`**

- Remove the segmented TabRow `[Editar | Vista Previa]` from `NoteEditorContent.kt`.
- Remove the old `NoteMarkdownToolbar`.
- Apply `MarkdownVisualTransformation(MaterialTheme.colorScheme)` to the content `NotiFastFormField`.
- Set `minLines = 8` and remove rigid 200dp limit.
- Keep file size $\le 200$ LOC.

- [ ] **Step 3: Delete old `NoteMarkdownToolbar.kt`**

Delete `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/NoteMarkdownToolbar.kt` to eradicate `"2 COLUMNAS"` and legacy layout.

- [ ] **Step 4: Verify compilation & run tests**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/atoms/NotiFastFormField.kt app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorContent.kt
git rm app/src/main/java/com/notifast/app/presentation/feature/noteeditor/components/NoteMarkdownToolbar.kt
git commit -m "refactor(editor): unify canvas with MarkdownVisualTransformation and remove legacy 2 COLUMNAS toolbar"
```

---

### Task 6: Dock Toolbar with `imePadding` in Screen and BottomSheet

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorBottomSheet.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorScreen.kt`

**Interfaces:**
- Consumes: `DockedAccessoryToolbar`.
- Layout structure:
  ```kotlin
  Column(modifier = Modifier.fillMaxWidth().imePadding()) {
      Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
          NoteEditorContent(...)
      }
      if (!uiState.isPreviewMode) {
          DockedAccessoryToolbar(...)
      }
  }
  ```

- [ ] **Step 1: Update `NoteEditorBottomSheet.kt`**

Ensure `DockedAccessoryToolbar` is placed outside the scrolling container so it floats above the keyboard when focused.

- [ ] **Step 2: Update `NoteEditorScreen.kt`**

Ensure `DockedAccessoryToolbar` is placed above the bottom navigation/keyboard in full screen mode.

- [ ] **Step 3: Verify compilation & tests**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorBottomSheet.kt app/src/main/java/com/notifast/app/presentation/feature/noteeditor/NoteEditorScreen.kt
git commit -m "feat(editor): dock accessory toolbar above virtual keyboard with imePadding"
```

---

### Task 7: Full Test Suite & Lint Verification

**Files:**
- Test all: `app/src/test/java/...`

- [ ] **Step 1: Execute all unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL (All unit tests green)

- [ ] **Step 2: Assemble Debug APK**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 8: Device Installation & Visual Certification (Galaxy A06)

**Files:**
- Build output: `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 1: Install APK on Samsung Galaxy A06 (`R8YL40QFYYJ`)**

Run: `adb -s R8YL40QFYYJ install -r app/build/outputs/apk/debug/app-debug.apk`
Expected: Success

- [ ] **Step 2: Launch Note Editor on device and capture screenshot**

Run:
```bash
adb -s R8YL40QFYYJ shell am start -n com.notifast.app/.presentation.MainActivity
# Wait 2 seconds, capture screenshot to verify UI
adb -s R8YL40QFYYJ exec-out screencap -p > /tmp/note_editor_certification.png
```

- [ ] **Step 3: Verify visual aesthetics & touch ergonomics**

Check captured screenshot to ensure:
1. No "2 COLUMNAS" or debug residue.
2. Docked accessory toolbar is visible, correctly sized, and aligned.
3. Live Markdown canvas typography is legible, balanced, and student-friendly.
