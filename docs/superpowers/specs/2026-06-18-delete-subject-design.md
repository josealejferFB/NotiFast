# Delete Subject — Design Spec

## Goal
Allow users to delete a subject (and all its notes) from the Notes screen via a long-press context menu on SubjectCard.

## UX
- Long-press on SubjectCard header → DropdownMenu with "Eliminar materia" (icon: Delete, color: Ruby/red)
- Tap → confirmation AlertDialog: "¿Eliminar [nombre]? Todos los apuntes asociados se borrarán."
- Confirm → delete subject + all notes under it → UI refreshes automatically via the existing combine flow

## Architecture
Follows the existing `deleteNote` pattern exactly.

```
SubjectCard (long-press → onDeleteSubject callback)
  → NotesScreen (shows AlertDialog, dispatches DeleteSubject event)
    → NotesViewModel.deleteSubject()
      → NoteRepository.deleteSubject(userId, subjectName) [already implemented]
```

## Changes

### 1. NotesContract.kt
- Add `DeleteSubject(val subjectName: String)` to `NotesEvent`

### 2. NotesViewModel.kt
- Add `deleteSubject()` method (same pattern as existing `deleteNote()`)
- Wire in `onEvent`: `is NotesEvent.DeleteSubject -> deleteSubject(event.subjectName)`

### 3. SubjectCard.kt
- Add `onDeleteSubject: () -> Unit` parameter
- Wrap header Row with `Modifier.combinedClickable(onLongClick = { showMenu = true })`
- Add `DropdownMenu` with "Eliminar materia" item (icon + text, red tint)
- Show/hide menu via local `var showMenu by remember { mutableStateOf(false) }`

### 4. NotesScreen.kt
- Add local state: `var subjectToDelete by remember { mutableStateOf<String?>(null) }`
- When `subjectToDelete != null`, show `AlertDialog`:
  - Title: "Eliminar materia"
  - Text: "¿Estás seguro de eliminar [nombre]? También se borrarán todos los apuntes asociados."
  - Confirm button (Ruby): "Eliminar" → dispatches `DeleteSubject`, clears state
  - Cancel button: cancela, clears state
- Wire `onDeleteSubject = { subjectToDelete = info.name }` in SubjectCard items

## What's NOT Changed
- `NoteRepository` interface (already has `deleteSubject()`)
- `NoteRepositoryImpl` (already has working implementation with image cleanup + note deletion)
- No new files needed
- No new dependencies

## Edge Cases
- Deleting while subject is expanded → card remains but will disappear on next flow emission
- Deleting the last subject → empty state shows "Comienza a organizar tus apuntes"
- Repository already handles errors (image deletion failures are caught silently)
