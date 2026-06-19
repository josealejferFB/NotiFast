# Educational Hub & Contrast Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix severe contrast issues in dark mode and elevate the NotiFast UI to a modern "Educational Hub" aesthetic using glassmorphism, organic shapes, and a unified speed dial FAB.

**Architecture:** 
1. Introduce a `ThemeUtils` object to dynamically calculate accessible colors based on the current theme.
2. Update atomic background components for a softer dark mode.
3. Refactor `SubjectDetailScreen` and its sub-components to use the new accessible colors, glassmorphism hero, pill-shaped headers, and a unified FAB.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Firebase.

## Global Constraints
- Language: English for code/variables, Spanish for UI text.
- No wildcard imports.
- Clean Architecture boundaries.
- Follow MVVM + MVI Contract pattern if creating new features.
- UI elements must use predefined styles and atoms where possible.

---

### Task 1: Theme & Color Utilities

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/theme/ThemeUtils.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/theme/color/Color.kt:15-20`
- Test: `app/src/test/java/com/example/notifast/presentation/theme/ThemeUtilsTest.kt`

**Interfaces:**
- Produces: `ThemeUtils.getAccessibleColor(hex: String, isDark: Boolean): Color`

- [ ] **Step 1: Write test for ThemeUtils**
```kotlin
package com.example.notifast.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ThemeUtilsTest {
    @Test
    fun testAccessibleColor() {
        // Bright yellow
        val yellowHex = "#FFFF00"
        
        // In light mode, yellow should be darkened
        val lightModeColor = ThemeUtils.getAccessibleColor(yellowHex, false)
        assertNotEquals(Color(android.graphics.Color.parseColor(yellowHex)), lightModeColor)
        
        // In dark mode, it should be desaturated/darkened
        val darkModeColor = ThemeUtils.getAccessibleColor(yellowHex, true)
        assertNotEquals(Color(android.graphics.Color.parseColor(yellowHex)), darkModeColor)
    }
}
```

- [ ] **Step 2: Run test (will fail)**
Run: `./gradlew testDebugUnitTest --tests "com.example.notifast.presentation.theme.ThemeUtilsTest"`
Expected: FAIL (Unresolved reference: ThemeUtils)

- [ ] **Step 3: Implement ThemeUtils**
```kotlin
package com.example.notifast.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

object ThemeUtils {
    fun getAccessibleColor(hex: String, isDark: Boolean): Color {
        val baseColorInt = try {
            if (hex.isNotBlank()) android.graphics.Color.parseColor(hex)
            else android.graphics.Color.parseColor("#3B9797") // Aqua fallback
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#3B9797")
        }

        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(baseColorInt, hsl)

        if (isDark) {
            // Desaturate and reduce lightness for dark mode
            hsl[1] = hsl[1].coerceAtMost(0.6f) // Max saturation 60%
            hsl[2] = hsl[2].coerceAtMost(0.5f) // Max lightness 50%
        } else {
            // Darken extremely light colors for light mode
            if (hsl[2] > 0.8f) {
                hsl[2] = 0.6f 
            }
        }

        return Color(ColorUtils.HSLToColor(hsl))
    }
}
```

- [ ] **Step 4: Update base dark colors in Color.kt**
Modify `Color.kt` to soften the dark mode theme:
```kotlin
// Dark
val RubyDark = Color(0xFFCC5A68) // Muted Ruby
val AquaDark = Color(0xFF4A9696) // Muted Aqua
val SurfaceDark = Color(0xFF1B262C) // Slate
val OnSurfaceDark = Color(0xFFE6E1E5)
```

- [ ] **Step 5: Run test to verify**
Run: `./gradlew testDebugUnitTest --tests "com.example.notifast.presentation.theme.ThemeUtilsTest"`
Expected: PASS

- [ ] **Step 6: Commit**
```bash
git add app/src/main/java/com/example/notifast/presentation/theme/ThemeUtils.kt app/src/test/java/com/example/notifast/presentation/theme/ThemeUtilsTest.kt app/src/main/java/com/example/notifast/presentation/theme/color/Color.kt
git commit -m "feat: add ThemeUtils for intelligent contrast and soften dark mode colors"
```

---

### Task 2: Backgrounds & Hero Redesign

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/atoms/NotiFastStudyBackground.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/atoms/NotiFastBackground.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailHero.kt`

**Interfaces:**
- Consumes: `ThemeUtils.getAccessibleColor`

- [ ] **Step 1: Soften Dark Backgrounds**
In `NotiFastStudyBackground.kt`, update `bgColor1` and `bgColor2` for dark mode:
```kotlin
val bgColor1 = if (isDark) Color(0xFF1B262C) else Color.White
val bgColor2 = if (isDark) Color(0xFF223038) else Color(0xFFF8F9FA)
```
Do the same principle if applicable in `NotiFastBackground.kt`.

- [ ] **Step 2: Redesign SubjectDetailHero**
Replace the harsh linear gradient with a solid background and radial glow, plus a glassmorphism icon container.
```kotlin
package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.notifast.presentation.feature.notes.components.SubjectIconMapper
import com.example.notifast.presentation.theme.ThemeUtils

@Composable
fun SubjectDetailHero(
    subjectName: String,
    subjectIcon: String,
    subjectColorHex: String,
    noteCount: Int,
    evalCount: Int,
    onBackClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val color = ThemeUtils.getAccessibleColor(subjectColorHex, isDark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Radial Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.2f), Color.Transparent),
                        radius = 400f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glassmorphism Icon Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
                    .border(1.dp, color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SubjectIconMapper.getIcon(subjectIcon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = subjectName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$noteCount apuntes · $evalCount evaluaciones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/example/notifast/presentation/atoms/NotiFastStudyBackground.kt app/src/main/java/com/example/notifast/presentation/atoms/NotiFastBackground.kt app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailHero.kt
git commit -m "feat: redesign SubjectDetailHero with glassmorphism and soften atomic backgrounds"
```

---

### Task 3: Section Hierarchy (Pills) & Component Updates

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailEvaluationList.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailNoteTile.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/components/SubjectDetailCalendarCard.kt`

**Interfaces:**
- Consumes: `ThemeUtils.getAccessibleColor`

- [ ] **Step 1: Replace simple headers with Pill Headers**
In all 3 components, replace the plain `Text("Sección", ...)` with a Pill header:
```kotlin
Row(
    modifier = Modifier
        .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
        .padding(horizontal = 16.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text("Evaluaciones", color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
}
```
*Note: Adjust text appropriately ("Apuntes", "Calendario", etc).*

- [ ] **Step 2: Use ThemeUtils for component colors**
In `SubjectDetailEvaluationList.kt`, `SubjectDetailNoteTile.kt`, and `SubjectDetailCalendarCard.kt`, replace `parseSubjectColor` with `ThemeUtils.getAccessibleColor(subjectColorHex, isSystemInDarkTheme())`.

- [ ] **Step 3: Fix 0.dp bug in SubjectDetailStats.kt**
In `SubjectDetailStats.kt`, find `Modifier.fillMaxWidth().height(4.dp).width(0.dp).background(accentColor)` and remove `.width(0.dp)`. Also use `ThemeUtils` here.

- [ ] **Step 4: Commit**
```bash
git commit -am "feat: implement pill section headers and accessible colors for detail components"
```

---

### Task 4: Unified Speed Dial FAB

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt`

**Interfaces:**
- UI modification only.

- [ ] **Step 1: Create Speed Dial UI**
In `SubjectDetailScreen.kt`, replace the two stacked FABs with a stateful single FAB that expands.
```kotlin
var isFabExpanded by remember { mutableStateOf(false) }

// Inside Scaffold floatingActionButton
Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    AnimatedVisibility(visible = isFabExpanded) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Apunte", modifier = Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                FloatingActionButton(
                    onClick = { isFabExpanded = false; onAddNoteClick(uiState.subjectName) },
                    containerColor = Ruby, contentColor = Color.White, modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Default.Add, contentDescription = "Agregar apunte") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Evaluación", modifier = Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                FloatingActionButton(
                    onClick = { isFabExpanded = false; onAddEvaluationClick(uiState.subjectName) },
                    containerColor = Aqua, contentColor = Color.White, modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Default.CalendarMonth, contentDescription = "Agregar evaluación") }
            }
        }
    }
    FloatingActionButton(
        onClick = { isFabExpanded = !isFabExpanded },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, contentDescription = "Opciones")
    }
}
```

- [ ] **Step 2: Commit**
```bash
git commit -am "feat: implement unified speed dial FAB in SubjectDetailScreen"
```
