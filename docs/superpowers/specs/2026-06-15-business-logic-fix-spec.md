# NotiFast Business Logic Fix — Spec

## Problemas identificados

1. **AddNoteScreen** no tiene dropdown de materias. Usuario debe escribir nombre exacto.
2. **AddNoteScreen** permite crear notas sin que existan materias.
3. **AddNoteViewModel** no carga subjects desde Firestore.
4. **SubjectDetailScreen** no tiene botones para crear apuntes/evaluaciones.
5. **NavGraph** rutas `add_note` y `add_evaluation` no aceptan subject pre-seleccionado.

## Solución

### 1. AddNoteContract
```kotlin
data class AddNoteUiState(
    val subject: String = "",
    val subjects: List<String> = emptyList(),
    val title: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface AddNoteEvent {
    data class SubjectChanged(val value: String) : AddNoteEvent
    data class TitleChanged(val value: String) : AddNoteEvent
    data class ContentChanged(val value: String) : AddNoteEvent
    data class ImageSelected(val uri: Uri?) : AddNoteEvent
    data object SaveClicked : AddNoteEvent
    data object ClearError : AddNoteEvent
    data class SetSubjects(val subjects: List<String>) : AddNoteEvent
}
```

### 2. AddNoteViewModel
- En `init`, recolectar `noteRepository.getSubjects(userId)` y emitir `SetSubjects`
- Si hay un subject inicial (pasado desde navegación), presetearlo

### 3. AddNoteScreen
- `ExposedDropdownMenuBox` con subjects cargados
- Si `subjects.isEmpty()`, mostrar "No hay materias. Crea una primero." y deshabilitar guardar
- Soportar subject inicial desde navegación

### 4. SubjectDetailScreen
- Agregar botones: "Nuevo Apunte", "Nueva Evaluación", "Editar Perfil" (si aplica)
- Callbacks `onAddNoteClick: (String) -> Unit`, `onAddEvaluationClick: (String) -> Unit`
- Las notas se muestran con `NoteEstheticCard` (reutilizar)
- Las evaluaciones se muestran con cards minimalistas

### 5. NavGraph
- `Screen.AddNote` → `"add_note?subject={subject}"` con navArgument opcional
- `Screen.AddEvaluation` → `"add_evaluation?subject={subject}"`
- Pasar callbacks desde SubjectDetailScreen a NavGraph

### 6. Screen.kt
- Actualizar rutas de AddNote y AddEvaluation

## Archivos a modificar

| Archivo | Cambio |
|---|---|
| `Screen.kt` | Rutas AddNote y AddEvaluation con parámetro opcional |
| `NavGraph.kt` | Nuevas rutas + callbacks SubjectDetailScreen |
| `AddNoteContract.kt` | +subjects field, +SetSubjects event |
| `AddNoteViewModel.kt` | Cargar subjects en init, preset inicial |
| `AddNoteScreen.kt` | Dropdown + validación sin materias |
| `SubjectDetailScreen.kt` | Botones de acción + NoteEstheticCard |
| `SubjectDetailViewModel.kt` | (sin cambios mayores) |
| `SubjectDetailContract.kt` | (sin cambios mayores) |
