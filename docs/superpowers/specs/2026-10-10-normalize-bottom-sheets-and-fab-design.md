# Normalización de Experiencia: FAB Expandible en Agenda y BottomSheets de Creación/Edición

**Fecha:** 2026-10-10  
**Estado:** Aprobado  
**Área:** Presentation / Navigation / Design System  

---

## 1. Contexto y Objetivos

Actualmente en NotiFast coexisten dos paradigmas distintos para la creación de entidades:
1. **Enfoque BottomSheet:** Utilizado en "Nuevo Apunte" (`NoteEditorBottomSheet`) y "Nueva Materia" (`AddSubjectBottomSheet`). Ofrece una interacción contextual, sin salir de la pantalla activa ni contaminar el backstack de navegación.
2. **Enfoque Pantalla Completa:** Utilizado en "Nuevo Horario" (`AddScheduleScreen`), "Editar Horario" (`EditScheduleScreen`), "Nueva Evaluación" (`AddEvaluationScreen`) y "Editar Evaluación" (`EditEvaluationScreen`).
3. **Inconsistencia de FAB:** En la pantalla de Apuntes (`NotesScreen`), el botón `+` usa `NotiFastExpandableFab` con opciones verticales animadas, mientras que en Agenda (`SchedulesScreen`) se usa un FAB solitario que abre un sheet intermedio (`AgendaCreateActionBottomSheet`) para seleccionar la acción.

### Metas
- Normalizar el botón `+` de Agenda para usar directamente `NotiFastExpandableFab` con animación vertical:
  - Arriba: **Nuevo Horario** (ícono reloj, color primario).
  - Abajo: **Nueva Evaluación** (ícono evaluación, color de alerta/acento).
- Eliminar `AgendaCreateActionBottomSheet` y cualquier paso intermedio innecesario.
- Transformar la creación y edición de Horarios y Evaluaciones en `ModalBottomSheet`:
  - `ScheduleEditorBottomSheet` (soporta creación y edición de horario).
  - `EvaluationEditorBottomSheet` (soporta creación y edición de evaluación, incluyendo calificaciones obtenidas).
- Soportar encadenamiento fluido: si el usuario pulsa "+ Nueva materia" dentro del sheet de horario o evaluación, se abre `AddSubjectBottomSheet` y al completarse se auto-selecciona en el formulario activo sin pérdida de datos.
- Respetar las reglas de diseño y arquitectura de `AGENTS.md`: componentes atómicos, ≤ 250 LOC por archivo, insets para teclado (`imePadding()`), confirmación de descarte con `NotiFastDialog`, y cero strings/colores hardcodeados.

---

## 2. Hallazgos de Systematic Debugging

1. **SavedStateHandle en ViewModels de Edición:**
   - `EditScheduleViewModel` y `EditEvaluationViewModel` requerían obligatoriamente argumentos no nulos en `savedStateHandle` (`checkNotNull(savedStateHandle["scheduleId"])`). Al instanciarse como BottomSheets fuera de una ruta de navegación dedicada, `savedStateHandle` no recibe esos argumentos de forma nativa.
   - *Fix arquitectónico:* Permitir inicialización segura mediante un evento `Init(id)` o parámetro inicial opcional sin romper los tests unitarios existentes.
2. **Manejo de Teclado (IME) y Scroll en Formularios Complejos:**
   - Los formularios de Horario y Evaluación tienen múltiples campos (chips, segmented bar, textfields, slider). Al ser BottomSheet, requieren `Modifier.imePadding()`, `verticalScroll(rememberScrollState())` y cierre de teclado al interactuar con el fondo.
3. **Manejo de Back / Descarte:**
   - Si el usuario tiene cambios no guardados y presiona Atrás o pulsa fuera del sheet, debe mostrarse el diálogo de confirmación `NotiFastDialog`.
4. **Redundancia:**
   - `AgendaCreateActionBottomSheet.kt` queda 100% obsoleto al integrarse las opciones en `NotiFastExpandableFab`.

---

## 3. Especificación Técnica

### 3.1 `SchedulesScreen` FAB
- Reemplazar `FloatingActionButton` y `AgendaCreateActionBottomSheet` por `NotiFastExpandableFab`.
- `actions`:
  1. `FabAction(label = "Nuevo Horario", icon = Icons.Default.Schedule, color = primary, onClick = onAddScheduleClick)`
  2. `FabAction(label = "Nueva Evaluación", icon = Icons.AutoMirrored.Filled.Assignment, color = error, onClick = onAddEvaluationClick)`

### 3.2 `ScheduleEditorBottomSheet`
- Parámetros: `scheduleId: String? = null`, `onDismiss: () -> Unit`, `onSaved: () -> Unit`, `onCreateSubject: () -> Unit`.
- Contiene los campos existentes:
  - Selector de materias con chips y acción "+ Nueva materia"
  - Barra segmentada de día de la semana (1-7)
  - TimeRangePicker (hora inicio, hora fin)
  - TextField de Aula y Docente
  - Banner de conflictos
  - Recordatorios previos
  - Link de reunión virtual
- Encabezado con título "Nuevo Horario" o "Editar Horario" y botón de cerrar (X).
- Diálogo de descarte si hay cambios pendientes.

### 3.3 `EvaluationEditorBottomSheet`
- Parámetros: `evalId: String? = null`, `initialSubject: String = ""`, `onDismiss: () -> Unit`, `onSaved: () -> Unit`, `onCreateSubject: () -> Unit`.
- Contiene los campos existentes:
  - Selector de materias y acción "+ Nueva materia"
  - Selector de tipo de evaluación (Examen, Tarea, Quiz, etc.)
  - Título, Fecha (DatePicker) y Ponderación (Slider)
  - Descripción y Tips de estudio opcionales
  - Campos de Calificación obtenida y estado de entrega (en modo edición)
- Encabezado con título dinámico y botón de cerrar.
- Diálogo de descarte si hay cambios pendientes.

### 3.4 Hoisting en `MainContent` (`NavGraph.kt`)
- `activeScheduleEditorId: String?` (null = cerrado, "NEW" = creación, `<id>` = edición).
- `activeEvaluationEditorArgs: Pair<String?, String?>?` (null = cerrado, `Pair(evalId, initialSubject)`).
- `showAddSubjectSheetFromEditor: Boolean` para abrir `AddSubjectBottomSheet` encima si se requiere.
- Conexión de callbacks desde `NotesScreen`, `SchedulesScreen`, `SubjectDetailScreen`.
