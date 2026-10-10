# Normalización de FAB y BottomSheets en NotiFast Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Normalizar la creación y edición de horarios y evaluaciones transformándolos en ModalBottomSheet unificados, y adoptar `NotiFastExpandableFab` con opciones verticales animadas en la Agenda.

**Architecture:** Se desacoplan los flujos de creación/edición de horarios y evaluaciones de las rutas de pantalla completa en Navigation Compose, migrándolos a `ModalBottomSheet` modulares (`ScheduleEditorBottomSheet` y `EvaluationEditorBottomSheet`) con hoisting de estado en `MainContent` (`NavGraph.kt`). Los ViewModels soportan inicialización por argumentos/eventos seguros. En `SchedulesScreen` se sustituye el FAB simple y el diálogo intermedio por el átomo existente `NotiFastExpandableFab`.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose, Material 3 ModalBottomSheet, Hilt, Room / StateFlow, Gradle.

**Spec:** `docs/superpowers/specs/2026-10-10-normalize-bottom-sheets-and-fab-design.md`

## Global Constraints
- Máximo ~250 líneas por archivo composable (extraer sub-composables a `components/` si se excede).
- Cero strings hardcodeados en UI (consumir siempre desde `res/values/strings.xml`).
- Cero colores arbitrarios hardcodeados (usar `MaterialTheme.colorScheme` o tokens semánticos corporativos Aqua/Ruby).
- Toda operación de guardado o actualización debe manejar errores y propagar feedback visual.
- Preservar el paso exitoso de toda la suite de pruebas con `./gradlew testDebugUnitTest`.

## Review Focus
1. **Instanciación segura de ViewModels de edición:** Evitar crashes por `checkNotNull` en `savedStateHandle` cuando el BottomSheet se abre fuera de una ruta con argumentos.
2. **Superposición de teclado (IME):** Asegurar que ningún campo de texto o botón de acción quede inaccesible cuando el teclado virtual está desplegado en el BottomSheet.
3. **Flujo de descarte con cambios sin guardar:** Confirmar con `NotiFastDialog` cuando el usuario intenta descartar cambios mediante drag down o botón atrás.
4. **Encadenamiento de materias:** Validar que al crear una materia desde el BottomSheet de horario/evaluación, la materia quede auto-seleccionada de inmediato sin perder datos previos.
5. **Animación y orden del FAB en Agenda:** Verificar que la opción superior sea "Nuevo Horario" y la inferior "Nueva Evaluación", con animación fluida de rotación y fade.

---

### Task 1: Normalización del FAB en Agenda (`SchedulesScreen`)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesScreen.kt:115-135`
- Delete: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/AgendaCreateActionBottomSheet.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/schedules/SchedulesFabNormalizationTest.kt`

**Interfaces:**
- Consumes: `NotiFastExpandableFab(actions: List<FabAction>)`, `FabAction(label, icon, onClick, contentDescription, color)`
- Produces: `SchedulesScreen` con FAB expandible vertical (Arriba: Nuevo Horario, Abajo: Nueva Evaluación) y eliminación del sheet intermedio de selección.

- [ ] **Step 1: Escribir prueba unitaria para validar configuración de acciones FAB**
Crear `SchedulesFabNormalizationTest.kt` validando que las opciones de FAB se configuren en el orden correcto (Horario arriba, Evaluación abajo) y colores apropiados.

- [ ] **Step 2: Ejecutar prueba unitaria para verificar que falle (o no compile aún)**
Comando: `JAVA_HOME=/home/p0xd/.jdks/temurin-21 ./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.schedules.SchedulesFabNormalizationTest"`

- [ ] **Step 3: Reemplazar el FAB y eliminar `AgendaCreateActionBottomSheet`**
En `SchedulesScreen.kt`:
1. Remover `showCreateSheet`, `createSheetState`, y el bloque `if (showCreateSheet) { ModalBottomSheet(...) }`.
2. En `floatingActionButton`, usar `NotiFastExpandableFab`:
   - Acción 1: `label = stringResource(R.string.agenda_create_schedule_option)`, `icon = Icons.Default.Schedule`, `color = MaterialTheme.colorScheme.primary`, `onClick = onAddScheduleClick`.
   - Acción 2: `label = stringResource(R.string.agenda_create_evaluation_option)`, `icon = Icons.AutoMirrored.Filled.Assignment`, `color = MaterialTheme.colorScheme.error`, `onClick = onAddEvaluationClick`.
3. Eliminar archivo obsoleto `AgendaCreateActionBottomSheet.kt`.

- [ ] **Step 4: Ejecutar pruebas y verificar compilación limpia**
Comando: `JAVA_HOME=/home/p0xd/.jdks/temurin-21 ./gradlew testDebugUnitTest`

- [ ] **Step 5: Commit de Task 1**
```bash
git add app/src/main/java/com/notifast/app/presentation/feature/schedules/
git commit -m "refactor(agenda): normalize fab with NotiFastExpandableFab and remove redundant selection sheet"
```

---

### Task 2: Adaptación de ViewModels para Inicialización Segura en BottomSheet

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editevaluation/EditEvaluationViewModel.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/editschedule/EditScheduleViewModelTest.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/editevaluation/EditEvaluationViewModelTest.kt`

**Interfaces:**
- Consumes: `savedStateHandle.get<String>("scheduleId")`, `savedStateHandle.get<String>("evalId")` opcionales.
- Produces: `EditScheduleEvent.Init(scheduleId: String)`, `EditEvaluationEvent.Init(evalId: String)` para carga diferida segura sin crasheos de `checkNotNull`.

- [ ] **Step 1: Escribir tests unitarios que verifiquen instanciación sin argumentos en SavedStateHandle**
En `EditScheduleViewModelTest` y `EditEvaluationViewModelTest`, agregar tests donde `SavedStateHandle` esté vacío y se invoque `Init(id)` manualmente.

- [ ] **Step 2: Ejecutar tests y verificar fallo**
Comando: `JAVA_HOME=/home/p0xd/.jdks/temurin-21 ./gradlew testDebugUnitTest --tests "*EditScheduleViewModelTest*"`

- [ ] **Step 3: Implementar inicialización segura en `EditScheduleViewModel` y `EditEvaluationViewModel`**
1. En `EditScheduleViewModel`:
   - Cambiar `private val scheduleId: String = checkNotNull(savedStateHandle["scheduleId"])` por `private var scheduleId: String? = savedStateHandle["scheduleId"]`.
   - Agregar evento `Init(scheduleId: String)`. Si ya existe `scheduleId`, cargar normalmente en `init`.
2. En `EditEvaluationViewModel`:
   - Cambiar `private val evalId: String = checkNotNull(savedStateHandle["evalId"])` por `private var evalId: String? = savedStateHandle["evalId"]`.
   - Agregar evento `Init(evalId: String)`. Si ya existe `evalId`, cargar normalmente en `init`.

- [ ] **Step 4: Ejecutar tests para validar paso en verde**
Comando: `JAVA_HOME=/home/p0xd/.jdks/temurin-21 ./gradlew testDebugUnitTest --tests "*EditScheduleViewModelTest*" --tests "*EditEvaluationViewModelTest*"`

- [ ] **Step 5: Commit de Task 2**
```bash
git add app/src/main/java/com/notifast/app/presentation/feature/editschedule/ app/src/main/java/com/notifast/app/presentation/feature/editevaluation/ app/src/test/
git commit -m "fix(viewmodel): allow safe initialization of edit view models for bottom sheet usage"
```

---

### Task 3: Creación de `ScheduleEditorBottomSheet` (Alta y Edición de Horarios)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/ScheduleEditorBottomSheet.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/components/ScheduleEditorSheetHeader.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleScreen.kt` (marcar deprecado o delegar al sheet)
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleScreen.kt` (marcar deprecado o delegar al sheet)

**Interfaces:**
- Consumes: `scheduleId: String?` (null = nuevo, no-null = edición), `onDismiss: () -> Unit`, `onSaved: () -> Unit`, `onCreateSubject: () -> Unit`.
- Produces: `ScheduleEditorBottomSheet` autónomo con esquinas redondeadas 28.dp, drag handle, diálogo de descarte con `NotiFastDialog`, y soporte de teclado/insets.

- [ ] **Step 1: Crear `ScheduleEditorSheetHeader.kt`**
Componente desacoplado con título dinámico ("Nuevo Horario" vs "Editar Horario") y botón cerrar (X) para mantener el archivo principal ≤ 250 LOC.

- [ ] **Step 2: Crear `ScheduleEditorBottomSheet.kt`**
Implementar `ModalBottomSheet`:
- Estados de edición vs creación controlados por `scheduleId`.
- Integración de sub-componentes existentes (`ScheduleSubjectChips`, `ScheduleDaySegmentedBar`, `ScheduleTimeRangePicker`, etc.).
- BackHandler inteligente (si el teclado está visible lo oculta; si hay cambios sin guardar abre diálogo de descarte; si no, cierra).
- Soporte para invocar `onCreateSubject()` (abrir `AddSubjectBottomSheet`).

- [ ] **Step 3: Verificar compilación y tamaño de líneas (≤ 250 LOC)**
Comando: `wc -l app/src/main/java/com/notifast/app/presentation/feature/addschedule/ScheduleEditorBottomSheet.kt`
Comando: `JAVA_HOME=/home/p0xd/.jdks/temurin-21 ./gradlew compileDebugKotlin`

- [ ] **Step 4: Commit de Task 3**
```bash
git add app/src/main/java/com/notifast/app/presentation/feature/addschedule/ app/src/main/java/com/notifast/app/presentation/feature/editschedule/
git commit -m "feat(schedules): create ScheduleEditorBottomSheet for unified creation and editing"
```

---

### Task 4: Creación de `EvaluationEditorBottomSheet` (Alta y Edición de Evaluaciones)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/EvaluationEditorBottomSheet.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/components/EvaluationEditorSheetHeader.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/AddEvaluationScreen.kt` (marcar deprecado o delegar)
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editevaluation/EditEvaluationScreen.kt` (marcar deprecado o delegar)

**Interfaces:**
- Consumes: `evalId: String?`, `initialSubject: String?`, `onDismiss: () -> Unit`, `onSaved: () -> Unit`, `onCreateSubject: () -> Unit`.
- Produces: `EvaluationEditorBottomSheet` autónomo con soporte de fecha, ponderación, tipo de evaluación, calificación obtenida (en edición) y diálogo de descarte.

- [ ] **Step 1: Crear `EvaluationEditorSheetHeader.kt`**
Encabezado con título dinámico ("Nueva Evaluación" vs "Editar Evaluación") y botón cerrar (X).

- [ ] **Step 2: Crear `EvaluationEditorBottomSheet.kt`**
Implementar `ModalBottomSheet`:
- Selector de materias, tipos de evaluación, título, fecha y slider de ponderación.
- Si `evalId != null`, mostrar campos adicionales de calificación y estado de entrega.
- BackHandler y diálogo de descarte ante cambios sin guardar.
- Manejo de `imePadding()` y `verticalScroll`.

- [ ] **Step 3: Verificar compilación y tamaño de líneas (≤ 250 LOC)**
Comando: `wc -l app/src/main/java/com/notifast/app/presentation/feature/addevaluation/EvaluationEditorBottomSheet.kt`
Comando: `JAVA_HOME=/home/p0xd/.jdks/temurin-21 ./gradlew compileDebugKotlin`

- [ ] **Step 4: Commit de Task 4**
```bash
git add app/src/main/java/com/notifast/app/presentation/feature/addevaluation/ app/src/main/java/com/notifast/app/presentation/feature/editevaluation/
git commit -m "feat(evaluations): create EvaluationEditorBottomSheet for unified creation and editing"
```

---

### Task 5: Integración y Hoisting en `NavGraph.kt` y Callers de la App

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesScreen.kt`

**Interfaces:**
- Consumes: `activeScheduleEditorId: String?`, `activeEvaluationEditorArgs: Pair<String?, String?>?`, `showAddSubjectSheetFromEditor: Boolean`.
- Produces: Apertura de bottom sheets contextuales desde cualquier pantalla sin alterar la navegación ni la barra inferior.

- [ ] **Step 1: Agregar variables de estado en `MainContent` de `NavGraph.kt`**
1. `var activeScheduleEditorId by remember { mutableStateOf<String?>(null) }`
2. `var activeEvaluationEditorArgs by remember { mutableStateOf<Pair<String?, String?>?>(null) }`
3. `var pendingSubjectTarget by remember { mutableStateOf<String?>(null) }` (para auto-seleccionar materia creada).

- [ ] **Step 2: Conectar los callbacks de todas las pantallas**
- `NotesScreen`: `onAddEvaluationClick = { subject -> activeEvaluationEditorArgs = Pair(null, subject) }`.
- `SubjectDetailScreen`: `onAddEvaluationClick = { activeEvaluationEditorArgs = Pair(null, subjectName) }`.
- `SchedulesScreen`:
  - `onAddScheduleClick = { activeScheduleEditorId = "NEW" }`
  - `onAddEvaluationClick = { activeEvaluationEditorArgs = Pair(null, null) }`
  - `onEditScheduleClick = { id -> activeScheduleEditorId = id }`
  - `onEditEvaluationClick = { eval -> activeEvaluationEditorArgs = Pair(eval.id, null) }`

- [ ] **Step 3: Instanciar los BottomSheets al final de `MainContent`**
Al igual que `NoteEditorBottomSheet`, instanciar `ScheduleEditorBottomSheet` y `EvaluationEditorBottomSheet` controlados por sus variables de estado activas, con soporte para desplegar `AddSubjectBottomSheet` encima cuando el usuario lo solicite.

- [ ] **Step 4: Ejecutar suite de pruebas completa y verificar cero regresiones**
Comando: `JAVA_HOME=/home/p0xd/.jdks/temurin-21 ./gradlew testDebugUnitTest assembleDebug`

- [ ] **Step 5: Commit de Task 5**
```bash
git add app/src/main/java/com/notifast/app/presentation/navigation/ app/src/main/java/com/notifast/app/presentation/feature/
git commit -m "feat(navigation): hoist schedule and evaluation editor bottom sheets into MainContent"
```
