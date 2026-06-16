# Full Visual Restoration + NotesScreen Enhancement

## Goal

Apply `NotiFastStudyBackground` to all 11 screens and restore the original NotesScreen experience (NoteEstheticCard, NoteDetailExpandable, SubjectFeaturedCard, custom filters, animated transitions) adapted to the current MVVM + SOLID atómico architecture.

---

## Global: StudyBackground en todas las screens (11)

**Patrón:**
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    NotiFastStudyBackground()
    Scaffold(containerColor = Color.Transparent, ...) { padding ->
        // contenido existente
    }
}
```

**Screens afectadas:**
- SettingsScreen
- CalendarScreen
- GradesScreen
- SchedulesScreen
- AddNoteScreen
- EditNoteScreen
- AddEvaluationScreen
- EditEvaluationScreen
- EditProfileScreen
- SubjectDetailScreen
- NotesScreen (ya tiene Box, agregar StudyBackground)

**Nota:** CalendarScreen ya no tiene Scaffold, usa Box+Column. Se mantiene así pero con StudyBackground atrás.

---

## NotesViewModel: cambios

### `NotesUiState` — agregar `rawNotes`
```kotlin
data class NotesUiState(
    ...
    val rawNotes: List<Note> = emptyList()  // unfiltered, for subject cards
)
```

### `combine` — exponer raw notes
```kotlin
combine(notes, subjects, _currentFilter, _searchQuery) { notes, subjects, filter, query ->
    val filtered = filterNotes(notes, filter, query)
    NotesUiState(notes = filtered, rawNotes = notes, ...)
}
```

### Nuevo evento `UpdateNote`
```kotlin
sealed interface NotesEvent {
    ...
    data class UpdateNote(val note: Note, val title: String, val content: String) : NotesEvent
}
```

### Nuevo método `updateNote`
```kotlin
NotesViewModel.updateNote(note, title, content) {
    noteRepository.updateNote(userId, note.copy(title = title, content = content), null)
}
```

---

## NotesScreen: reestructuración completa

### 1. BackHandler + expandedNote
```kotlin
var expandedNote by remember { mutableStateOf<Note?>(null) }
val isHeaderVisible = expandedNote == null

BackHandler(enabled = !isHeaderVisible) { expandedNote = null }
```

### 2. StudyBackground + AnimatedVisibility
```kotlin
Box(Modifier.fillMaxSize().safeDrawingPadding()) {
    NotiFastStudyBackground()
    Scaffold(containerColor = Color.Transparent) {
        Box(Modifier.fillMaxSize().padding(it)) {
            AnimatedVisibility(visible = isHeaderVisible, enter = fadeIn(400), exit = fadeOut(200)) {
                // HeaderProfile + "Mis Notas" + SearchBar + FilterBar + content
            }
            AnimatedVisibility(!isHeaderVisible, enter = fadeIn + scaleIn spring, exit = fadeOut + scaleOut) {
                // NoteDetailExpandable
            }
        }
    }
}
```

### 3. NoteEstheticCard (reemplaza NoteCard)
- Diseño superpuesto (overlapping cards)
- Alterna colores Ruby/Aqua según índice par/impar
- Card grande (363x171) con título + contenido + edit/delete
- Card chica (174x202) overlap con imagen + subject name
- Callbacks: onClick, onEdit, onDelete

### 4. NoteDetailExpandable
- Card flotante con animación spring bounce
- Muestra imagen si existe
- Toggle edit mode (icono Edit/Check en círculo Ruby/Aqua)
- Campos editables: OutlinedTextField para título y contenido
- Botón "Finalizar lectura" para cerrar

### 5. SubjectFeaturedCard
- Card horizontal (280x220) con preview de 3 notas
- Nombre de materia + contador de notas + "ver todas"
- Icono circular Ruby superpuesto

### 6. Sección "Materias" al final de notas
```kotlin
if (searchQuery.isEmpty() && rawNotes.isNotEmpty()) {
    Text("Materias", headlineMedium)
    LazyRow { subjectCards }
}
```

### 7. Empty state mejorado
- Icono NoteAdd en círculo Aqua
- "Comienza a organizar tus apuntes"
- Botón "Crear mi primera Materia" (si no hay searchQuery)

### 8. Filtros con Surface (original style)
- Ruby seleccionado, blanco bold; border gris si no seleccionado

---

## Archivos a modificar/crear

| Archivo | Acción |
|---|---|
| `NotesContract.kt` | + `rawNotes` field, + `UpdateNote` event |
| `NotesViewModel.kt` | + `rawNotes` in combine, + `updateNote` method |
| `NotesScreen.kt` | Reescritura completa con expandable y background |
| `presentation/feature/notes/components/NoteEstheticCard.kt` | Nuevo (restaurado) |
| `presentation/feature/notes/components/SubjectFeaturedCard.kt` | Nuevo |
| `presentation/feature/notes/components/NoteCard.kt` | Eliminar (reemplazado por NoteEstheticCard) |
| `SettingsScreen.kt` | + StudyBackground |
| `CalendarScreen.kt` | + StudyBackground |
| `GradesScreen.kt` | + StudyBackground |
| `SchedulesScreen.kt` | + StudyBackground |
| `AddNoteScreen.kt` | + StudyBackground |
| `EditNoteScreen.kt` | + StudyBackground |
| `AddEvaluationScreen.kt` | + StudyBackground |
| `EditEvaluationScreen.kt` | + StudyBackground |
| `EditProfileScreen.kt` | + StudyBackground |
| `SubjectDetailScreen.kt` | + StudyBackground |

---

## Lo que NO cambia
- NavGraph, DI, repositorios, modelos, otros ViewModels
- LoginScreen (ya completo)
- Google Sign-In (ya funciona)
- .catch en repositorios (ya agregado)

## Verificación
`./gradlew assembleDebug` → BUILD SUCCESSFUL
