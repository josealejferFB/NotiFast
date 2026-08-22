# Top Padding + Bottom Nav Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce top padding on all screens and replace the bottom nav with a floating Ruby bar with icons + tooltip animations.

**Architecture:** (1) edit `NotiFastCenterAlignedTopBar` for compact windowInsets, (2) create `NotiFastBottomNav` atom, (3) update `NavGraph.kt`, (4) remove vertical content padding from 7 screens.

**Tech Stack:** Jetpack Compose, Material3, Navigation Compose

---

### Task 1: Compact `NotiFastCenterAlignedTopBar`

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/atoms/NotiFastCenterAlignedTopBar.kt`

- [ ] **Step 1: Replace file content**

```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotiFastCenterAlignedTopBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}
```

- [ ] **Step 2: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 2: Create `NotiFastBottomNav` atom

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/atoms/NotiFastBottomNav.kt`

- [ ] **Step 1: Write the atom**

```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.navigation.Screen
import com.notifast.app.presentation.theme.color.Ruby

@Composable
fun NotiFastBottomNav(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    Surface(
        color = Ruby,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route ||
                    currentRoute?.startsWith(screen.route) == true

                val animatedScale by animateFloatAsState(
                    targetValue = if (selected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
                    label = "scale"
                )
                val animatedOffset by animateDpAsState(
                    targetValue = if (selected) (-4).dp else 0.dp,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
                    label = "offset"
                )

                Box(
                    modifier = Modifier.height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                        ) {
                            Surface(
                                color = Ruby,
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = screen.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        IconButton(
                            onClick = { onItemClick(screen) },
                            modifier = Modifier
                                .size(48.dp)
                                .offset(y = if (selected) animatedOffset else 0.dp)
                        ) {
                            screen.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = screen.title,
                                    tint = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .graphicsLayer {
                                            scaleX = animatedScale
                                            scaleY = animatedScale
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 3: Update NavGraph.kt — use `NotiFastBottomNav`

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/navigation/NavGraph.kt`

- [ ] **Step 1: Replace imports**

Remove these imports (no longer used):
```kotlin
import androidx.compose.material3.Icon          // was only in NavigationBarItem
import androidx.compose.material3.NavigationBar  // replaced by NotiFastBottomNav
import androidx.compose.material3.NavigationBarItem // replaced by NotiFastBottomNav
import androidx.compose.material3.Text          // was only in NavigationBarItem
import androidx.navigation.NavDestination.Companion.hierarchy // was only in NavigationBarItem
```

Add this import:
```kotlin
import com.notifast.app.presentation.atoms.NotiFastBottomNav
```

- [ ] **Step 2: Replace NavigationBar block**

Replace this block (lines 47-68):
```kotlin
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
```

With:
```kotlin
                NotiFastBottomNav(
                    items = Screen.bottomNavItems,
                    currentRoute = currentDestination?.route,
                    onItemClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
```

Also remove this unused import (was used by the old nav):
```kotlin
import androidx.compose.foundation.layout.padding
```
(Check if still used — it's used by `Modifier.padding(innerPadding)` on line 75, so **keep it**.)

- [ ] **Step 3: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 4: Remove vertical content padding from 7 screens

These screens have `.padding(16.dp)` or `.padding(24.dp)` that adds top/bottom padding to content. Change to horizontal-only padding.

**Screens that DO NOT need changes** (already correct):
- `NotesScreen.kt` — uses `.padding(padding)` with no extra padding
- `CalendarScreen.kt` — uses `.padding(horizontal = 16.dp)`
- `GradesScreen.kt` — uses `.padding(horizontal = 20.dp)`
- `AddEvaluationScreen.kt` — uses `.padding(horizontal = 24.dp)`
- `EditEvaluationScreen.kt` — uses `.padding(horizontal = 24.dp)`

- [ ] **Step 1: Fix AddNoteScreen**

Edit `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteScreen.kt:62`:
```kotlin
Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
```

- [ ] **Step 2: Fix EditNoteScreen**

Edit `app/src/main/java/com/example/notifast/presentation/feature/editnote/EditNoteScreen.kt:48`:
```kotlin
Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
```

- [ ] **Step 3: Fix SchedulesScreen**

Edit `app/src/main/java/com/example/notifast/presentation/feature/schedules/SchedulesScreen.kt:72`:
```kotlin
modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
```

- [ ] **Step 4: Fix SettingsScreen**

Edit `app/src/main/java/com/example/notifast/presentation/feature/settings/SettingsScreen.kt:64`:
```kotlin
.padding(padding)
.padding(horizontal = 24.dp)
```

- [ ] **Step 5: Fix SubjectDetailScreen**

Edit `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt:55`:
```kotlin
LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
```

- [ ] **Step 6: Fix EditProfileScreen**

Edit `app/src/main/java/com/example/notifast/presentation/feature/editprofile/EditProfileScreen.kt:86`:
```kotlin
.padding(padding)
.padding(horizontal = 24.dp),
```

- [ ] **Step 7: Fix AddSubjectScreen**

Edit `app/src/main/java/com/example/notifast/presentation/feature/addsubject/AddSubjectScreen.kt:50`:
```kotlin
.padding(padding)
.padding(horizontal = 16.dp)
```

- [ ] **Step 8: Build check**

Run: `cd /home/analista/AndroidStudioProjects/NotiFast && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL
