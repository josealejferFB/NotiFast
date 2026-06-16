# NotiFast UI Fix: Top Padding + Bottom Nav Redesign

## 1. Top Padding: Compact Layout

### Problem

Current screens have ~128dp from top of screen before content starts:
- Outer Scaffold adds status bar padding to NavHost (~38dp)
- Each screen's Scaffold places TopAppBar at the top of its content, default height ~64dp
- Content adds `.padding(16.dp)` on all sides

### Solution: Two changes

**A. Compact `NotiFastCenterAlignedTopBar` atom** (1 file)

Set `WindowInsets(0, 0, 0, 0)` on the `CenterAlignedTopAppBar`:

```kotlin
CenterAlignedTopAppBar(
    title = { Text(title, fontWeight = FontWeight.ExtraBold) },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
    windowInsets = WindowInsets(0, 0, 0, 0)
)
```

This tells the TopAppBar it does NOT extend into the status bar area. The bar's height is just its content (~48dp). The Scaffold places the bar below the status bar. The title sits right below the status bar background.

**B. Remove content vertical padding** (12 screen files)

Current: `.padding(innerPadding).padding(16.dp)` adds 16dp on all 4 sides.

Change to: `.padding(innerPadding).padding(horizontal = 16.dp)` — only horizontal padding, no top/bottom.

Special cases:
- `SettingsScreen`: `.padding(24.dp)` → `.padding(horizontal = 24.dp)`
- `GradesScreen`: already uses `.padding(horizontal = 20.dp)` → no change needed

**Result**: Content starts ~48dp below the status bar, saving ~32dp per screen.

---

## 2. Bottom Nav: Floting Ruby Bar + Icons Only + Tooltip

### Files
- **Create**: `presentation/atoms/NotiFastBottomNav.kt` — new atom
- **Modify**: `presentation/navigation/NavGraph.kt` — replace `NavigationBar` with new atom
- **No change**: `Screen.kt` (routes stay the same)

### Visual Design

```
                   ┌──────────────┐
                   │ Calificaciones│  ← tooltip chip: Ruby bg, white text, rounded(12)
                   └──────────────┘
                         ↑
    ┌──────────────────────────────────────────────┐
    │  📖     📅     ⏰     📊     ⚙️            │  ← white icons, selected lifts
    │                                              │  ← bg: Ruby, 24dp rounded top corners
    └──────────────────────────────────────────────┘
                   ↑ 8dp from bottom edge
```

### Component: `NotiFastBottomNav`

```kotlin
@Composable
fun NotiFastBottomNav(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
)
```

**Container**:
- `Surface(color = Ruby, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))`
- `Modifier.shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))`
- Outer `Modifier.padding(bottom = 8.dp)` so it floats above bottom edge

**Items** (Row of 5 icon buttons):
- `IconButton` with `Icon(screen.icon, tint = if (selected) Color.White else Color.White.copy(alpha = 0.5f))`
- Selected icon: `graphicsLayer { scaleX = 1.15f; scaleY = 1.15f }` + `offset(y = -4.dp)` → `animateFloatAsState` + `animateDpAsState` with `spring(dampingRatio = 0.5f, stiffness = 300f)`
- Inactive icon: no lift, lower alpha
- No label text visible by default

**Tooltip chip** (above selected item):
- Only visible for the current selected tab
- `AnimatedVisibility(visible = selected, enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom), exit = fadeOut() + shrinkVertically())`
- Content: `Surface(color = Ruby, shape = RoundedCornerShape(12.dp)) { Text(screen.title, color = White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) }`
- Positioned above the icon (offset(y = -8.dp - chipHeight))

### "Calificaciones" Overflow

Resolved by design: text appears ONLY in the floating tooltip chip, which has no width constraint. The chip expands horizontally to fit the full word.

---

## 3. Files Affected

| File | Change |
|---|---|
| `presentation/atoms/NotiFastCenterAlignedTopBar.kt` | Add `windowInsets = WindowInsets(0,0,0,0)` |
| `presentation/atoms/NotiFastBottomNav.kt` | **New** — floating Ruby bar, icons + tooltip, animations |
| `presentation/navigation/NavGraph.kt` | Replace `NavigationBar`/`NavigationBarItem` with `NotiFastBottomNav`; remove unused imports |
| `presentation/feature/notes/NotesScreen.kt` | `.padding(16.dp)` → `.padding(horizontal=16.dp)` |
| `presentation/feature/addnote/AddNoteScreen.kt` | Same |
| `presentation/feature/editnote/EditNoteScreen.kt` | Same |
| `presentation/feature/calendar/CalendarScreen.kt` | Same |
| `presentation/feature/schedules/SchedulesScreen.kt` | Same |
| `presentation/feature/settings/SettingsScreen.kt` | `.padding(24.dp)` → `.padding(horizontal=24.dp)` |
| `presentation/feature/addevaluation/AddEvaluationScreen.kt` | `.padding(16.dp)` → `.padding(horizontal=16.dp)` |
| `presentation/feature/editevaluation/EditEvaluationScreen.kt` | Same |
| `presentation/feature/subjectdetail/SubjectDetailScreen.kt` | Same |
| `presentation/feature/editprofile/EditProfileScreen.kt` | Same |
| `presentation/feature/addsubject/AddSubjectScreen.kt` | Same |
| `presentation/feature/grades/GradesScreen.kt` | Already correct — no change |
| `presentation/feature/auth/LoginScreen.kt` | No Scaffold+TopAppBar — no change |

---

## 4. Implementation Order

1. Edit `NotiFastCenterAlignedTopBar.kt` — add windowInsets
2. Create `NotiFastBottomNav.kt` — new atom
3. Edit `NavGraph.kt` — use new bottom nav
4. Edit all 12 screen files — remove vertical content padding
5. Build & verify

---

## 5. Risks & Mitigation

| Risk | Mitigation |
|---|---|
| Status bar overlaps TopAppBar title with `WindowInsets(0,0,0,0)` | If so, adjust to `WindowInsets(statusBarsPadding, 0, 0, 0)` via `WindowInsets.statusBars.only(Bottom)` or custom approach |
| Tooltip chip clips at top edge | Use `Modifier.offset(y = -chipHeight)` and ensure `clipToPadding = false` on parent |
| Icons-only nav not discoverable | Tooltip on selected item gives name; users learn 5 icons quickly |
| Bottom nav floats but overlaps content | Ensure `innerPadding` from outer Scaffold still accommodates the bar height |
