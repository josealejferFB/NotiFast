# 🚀 Implementación Maestro — Fase 2: Experiencia Core (Cuaderno y Hub Académico)

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir la experiencia central de NotiFast v2.0: gestión integral de horarios con detección matemática de conflictos, cuaderno inteligente con parser de Markdown y etiquetas temáticas, y el hub de calificaciones con escalas personalizables y simulador proactivo de notas requeridas.

**Architecture:** Clean Architecture estricta (Domain UseCases puros sin dependencias de Android/Compose, Data Repositories reactivos con Flow y Result<T>, Presentation con MVVM + MVI Unidirectional Data Flow). Cada pantalla se rige por el protocolo de 5 fases Impeccable (Alineación previa `/grill-me`, Refinamiento `distill/clarify`, Construcción modular `≤ 250 LOC`, Resiliencia `harden/audit`, Pulido heurístico `critique/polish`).

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3 (BOM 2024.09.00), Hilt 2.59, DataStore Preferences, Firebase Firestore & Storage, Kotlinx Coroutines & Flow, Turbine, MockK, JUnit 4.

**Spec:** [`docs/prd/KICKOFF_FASE_2.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_2.md), [`AGENTS.md`](file:///home/p0xd/Documentos/NotiFast/AGENTS.md), [`DESIGN.md`](file:///home/p0xd/Documentos/NotiFast/DESIGN.md), [`PRODUCT.md`](file:///home/p0xd/Documentos/NotiFast/PRODUCT.md).

## Global Constraints

- **Límite de tamaño:** Máximo ~250 líneas por archivo composable; modularizar en `components/`.
- **Cero Strings Hardcodeados:** Todo texto de UI DEBE residir en `res/values/strings.xml` (Español).
- **Design Tokens:** Consumir colores corporativos `Ruby` (`#BF092F`) y `Aqua` (`#3B9797`) y contrastes dinámicos vía `ThemeUtils.getAccessibleColor()`.
- **MVI Contract:** Un solo `UiState` inmutable por ViewModel y entrada única de eventos `onEvent(event: FeatureEvent)`.
- **TDD:** Ciclo Red ➔ Green ➔ Refactor antes de implementar Use Cases y lógica de negocio.
- **Evidencia antes de afirmaciones:** Compilación reproducible con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.

---

## 🎨 El Protocolo Impeccable de 5 Fases por Pantalla

Cada una de las pantallas de la Fase 2 debe transitar obligatoriamente por estas 5 fases:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. ALINEACIÓN PREVIA (/grill-me & shape)                                    │
│    Cuestionar supuestos de ergonomía, jerarquía y accesibilidad             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2. REFINAMIENTO Y ESENCIA (distill & clarify)                               │
│    Eliminar ruido visual, cards redundantes y textos ambiguos               │
├─────────────────────────────────────────────────────────────────────────────┤
│ 3. CONSTRUCCIÓN MODULAR (≤ 250 LOC & Tokens)                                │
│    Subcomponentes en components/, MaterialTheme.colorScheme, ThemeUtils     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4. RESILIENCIA Y ACCESIBILIDAD (harden & audit)                             │
│    Loading/Error/Empty states, strings.xml, BackHandler con NotiFastDialog  │
├─────────────────────────────────────────────────────────────────────────────┤
│ 5. AUDITORÍA HEURÍSTICA FINAL (critique & polish)                           │
│    Scanabilidad, micro-animaciones fluidas (AnimatedContent/Spring), haptic │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📅 SPRINT 2.1: GESTIÓN INTEGRAL DE HORARIOS ACADÉMICOS

### Task 1: TDD & Algoritmo de Detección de Conflictos Horarios (`ValidateScheduleOverlapUseCase`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/usecase/ValidateScheduleOverlapUseCase.kt`
- Create: `app/src/test/java/com/notifast/app/domain/usecase/ValidateScheduleOverlapUseCaseTest.kt`
- Modify: `app/src/main/java/com/notifast/app/di/UseCaseModule.kt`

**Interfaces:**
- Consumes: `Schedule` (`dayOfWeek: Int`, `startTime: String`, `endTime: String`, `id: String`)
- Produces: `ValidateScheduleOverlapUseCase.invoke(schedules: List<Schedule>, newSchedule: Schedule, excludeScheduleId: String? = null): ScheduleOverlapResult`
  where `sealed interface ScheduleOverlapResult { data object NoOverlap : ScheduleOverlapResult; data class Conflict(val conflictingSchedules: List<Schedule>) : ScheduleOverlapResult }`

- [ ] **Step 1: Escribir la suite de pruebas unitarias que falle (Red)**

```kotlin
// app/src/test/java/com/notifast/app/domain/usecase/ValidateScheduleOverlapUseCaseTest.kt
package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateScheduleOverlapUseCaseTest {

    private lateinit var useCase: ValidateScheduleOverlapUseCase

    @Before
    fun setUp() {
        useCase = ValidateScheduleOverlapUseCase()
    }

    @Test
    fun `when no existing schedules, returns NoOverlap`() {
        val newSchedule = Schedule(id = "1", subjectName = "Matemáticas", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        val result = useCase(emptyList(), newSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when times overlap on same day, returns Conflict`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val newSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 1, startTime = "09:00", endTime = "11:00")
        val result = useCase(existing, newSchedule)
        assertTrue(result is ScheduleOverlapResult.Conflict)
        assertEquals(1, (result as ScheduleOverlapResult.Conflict).conflictingSchedules.size)
        assertEquals("1", result.conflictingSchedules.first().id)
    }

    @Test
    fun `when times are adjacent on same day, returns NoOverlap`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val newSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 1, startTime = "10:00", endTime = "12:00")
        val result = useCase(existing, newSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when times overlap but on different days, returns NoOverlap`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val newSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 2, startTime = "08:00", endTime = "10:00")
        val result = useCase(existing, newSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when editing existing schedule, excludeScheduleId prevents self-conflict`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val editedSchedule = Schedule(id = "1", subjectName = "Física Avanzada", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        val result = useCase(existing, editedSchedule, excludeScheduleId = "1")
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }
}
```

- [ ] **Step 2: Ejecutar prueba y verificar que falla por falta de clases**

Ejecutar: `export JAVA_HOME=/home/p0xd/.jdks/temurin-17 && export PATH=$JAVA_HOME/bin:$PATH && ./gradlew testDebugUnitTest --tests com.notifast.app.domain.usecase.ValidateScheduleOverlapUseCaseTest`
Esperado: FALLA (Unresolved reference: ValidateScheduleOverlapUseCase).

- [ ] **Step 3: Implementar `ValidateScheduleOverlapUseCase` (Green)**

```kotlin
// app/src/main/java/com/notifast/app/domain/usecase/ValidateScheduleOverlapUseCase.kt
package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Schedule
import javax.inject.Inject

sealed interface ScheduleOverlapResult {
    data object NoOverlap : ScheduleOverlapResult
    data class Conflict(val conflictingSchedules: List<Schedule>) : ScheduleOverlapResult
}

class ValidateScheduleOverlapUseCase @Inject constructor() {

    operator fun invoke(
        existingSchedules: List<Schedule>,
        newSchedule: Schedule,
        excludeScheduleId: String? = null
    ): ScheduleOverlapResult {
        val newStartMin = parseTimeToMinutes(newSchedule.startTime) ?: return ScheduleOverlapResult.NoOverlap
        val newEndMin = parseTimeToMinutes(newSchedule.endTime) ?: return ScheduleOverlapResult.NoOverlap

        if (newEndMin <= newStartMin) return ScheduleOverlapResult.NoOverlap

        val conflicts = existingSchedules.filter { existing ->
            if (existing.id == excludeScheduleId || existing.id == newSchedule.id) return@filter false
            if (existing.dayOfWeek != newSchedule.dayOfWeek) return@filter false

            val existingStart = parseTimeToMinutes(existing.startTime) ?: return@filter false
            val existingEnd = parseTimeToMinutes(existing.endTime) ?: return@filter false

            // Interval overlap condition: max(start1, start2) < min(end1, end2)
            maxOf(newStartMin, existingStart) < minOf(newEndMin, existingEnd)
        }

        return if (conflicts.isEmpty()) {
            ScheduleOverlapResult.NoOverlap
        } else {
            ScheduleOverlapResult.Conflict(conflicts)
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int? {
        val parts = timeStr.trim().split(":")
        if (parts.size != 2) return null
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        if (hours !in 0..23 || minutes !in 0..59) return null
        return hours * 60 + minutes
    }
}
```

- [ ] **Step 4: Registrar en `UseCaseModule.kt` y ejecutar tests**

Ejecutar: `export JAVA_HOME=/home/p0xd/.jdks/temurin-17 && export PATH=$JAVA_HOME/bin:$PATH && ./gradlew testDebugUnitTest --tests com.notifast.app.domain.usecase.ValidateScheduleOverlapUseCaseTest`
Esperado: PASS (100% verde).

---

### Task 2: UI/UX Impeccable — Vista Semanal y Bloques Temporales (`SchedulesScreen`)

Aplicación de las **5 Fases Impeccable**:
1. **Alineación previa (`/grill-me` & `shape`):** Pestañas horizontales por día (Lunes a Domingo) con badges numéricos de clases vs lista corrida; navegación intuitiva y clara.
2. **Refinamiento (`distill` & `clarify`):** Retirar bordes pesados, mostrar hora de inicio y fin como tag de tiempo (`08:00 - 10:00`), indicador visual si la clase está en curso actualmente.
3. **Construcción Modular (`≤ 250 LOC`):** Extraer `ScheduleDayTabs.kt`, `ScheduleTimeBlockList.kt`, `ScheduleConflictBanner.kt` y `ScheduleCard.kt`.
4. **Resiliencia (`harden` & `audit`):** `NotiFastEmptyState` cuando el día seleccionado no tiene clases; confirmación de eliminación con `NotiFastDialog`.
5. **Pulido Heurístico (`critique` & `polish`):** Micro-animación en cambio de día con `AnimatedContent`, colores semánticos con `ThemeUtils.getAccessibleColor()`.

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleDayTabs.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleTimeBlockList.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleConflictBanner.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleCard.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesViewModel.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/schedules/SchedulesViewModelTest.kt`

- [ ] **Step 1: Actualizar `SchedulesContract.kt` con soporte de día seleccionado**
- [ ] **Step 2: Crear `ScheduleDayTabs.kt` con selector de días y badges**
- [ ] **Step 3: Crear `ScheduleCard.kt` y `ScheduleTimeBlockList.kt` con tokens de diseño**
- [ ] **Step 4: Refactorizar `SchedulesScreen.kt` a `< 200 LOC`**
- [ ] **Step 5: Escribir y ejecutar `SchedulesViewModelTest.kt`**

---

### Task 3: UI/UX Impeccable — Formularios `AddScheduleScreen` y `EditScheduleScreen` con Detección en Vivo

Aplicación de las **5 Fases Impeccable**:
1. **Alineación previa (`/grill-me` & `shape`):** Selector de hora accesible (Dialog TimePicker con formato 24h/12h); advertencia proactiva de colisión sin bloquear si el estudiante conscientemente la acepta.
2. **Refinamiento (`distill` & `clarify`):** Eliminación de pasos innecesarios; campos directos con autocompletado de materias existentes.
3. **Construcción Modular (`≤ 250 LOC`):** TimePickers desacoplados, banner de advertencia reutilizable `ScheduleConflictBanner`.
4. **Resiliencia (`harden` & `audit`):** `BackHandler` con diálogo `NotiFastDialog` ("¿Descartar cambios?"), manejo de errores en `strings.xml`.
5. **Pulido Heurístico (`critique` & `polish`):** Transición suave al alertar solapamiento, estados interactivos de guardado con `NotiFastButton(isLoading = ...)`.

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleContract.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModelTest.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/editschedule/EditScheduleViewModelTest.kt`

- [ ] **Step 1: Integrar `ValidateScheduleOverlapUseCase` en `AddScheduleViewModel` y `EditScheduleViewModel`**
- [ ] **Step 2: Conectar detección de colisiones reactiva en `AddScheduleContract` y `EditScheduleContract`**
- [ ] **Step 3: Implementar TimePicker accesible y `ScheduleConflictBanner` en `AddScheduleScreen` y `EditScheduleScreen`**
- [ ] **Step 4: Integrar protección de descarte seguro `NotiFastDialog` y `BackHandler`**
- [ ] **Step 5: Escribir y validar pruebas unitarias `AddScheduleViewModelTest` y `EditScheduleViewModelTest`**

---

## 📝 SPRINT 2.2: CUADERNO INTELIGENTE Y EDITOR DE APUNTES

### Task 4: Motor de Renderizado y Parser de Markdown para Compose (`NotiFastMarkdown`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/atoms/MarkdownRenderer.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/atoms/MarkdownParserTest.kt`

**Interfaces:**
- Produces: `MarkdownParser.parse(rawText: String): List<MarkdownBlock>`
- Produces: `@Composable fun NotiFastMarkdown(content: String, modifier: Modifier = Modifier, onTaskCheckedChange: ((index: Int, isChecked: Boolean) -> Unit)? = null)`
- Soporta: Encabezados (`#`, `##`, `###`), Negrita (`**texto**`), Cursiva (`*texto*`, `_texto_`), Listas (`* `, `- `), Checklist interactiva (`- [ ] `, `- [x] `), Citas (`> `), Bloques de código (`` `inline` `` y ````code````).

- [ ] **Step 1: Escribir pruebas unitarias TDD para el parser de Markdown (`MarkdownParserTest.kt`)**
- [ ] **Step 2: Implementar el parser puro y el renderizador composable con tipografía Impeccable**
- [ ] **Step 3: Ejecutar y validar que todas las pruebas pasen en verde**

---

### Task 5: UI/UX Impeccable — Editor de Apuntes con Live Preview y Tags (`AddNoteScreen` & `EditNoteScreen`)

Aplicación de las **5 Fases Impeccable**:
1. **Alineación previa (`/grill-me` & `shape`):** Toolbar de formato anclada/contextual con botones de acceso rápido (`H1`, `H2`, `B`, `I`, `• Lista`, `☑ Tarea`, `“ Cita`); Toggle fluido entre modo "Edición" y "Vista Previa".
2. **Refinamiento (`distill` & `clarify`):** Reemplazar el formulario antiguo escalonado por una experiencia de editor moderno de una sola vista con pestañas Editar/Previsualizar.
3. **Construcción Modular (`≤ 250 LOC`):** Subcomponentes `NoteFormattingToolbar.kt`, `NoteTagSelector.kt`, `NoteImagePickerField.kt`.
4. **Resiliencia (`harden` & `audit`):** Descarte seguro `NotiFastDialog`, selección de fotos de referencia, soporte de campo de etiquetas (`tags: List<String>`).
5. **Pulido Heurístico (`critique` & `polish`):** Transición `AnimatedContent` entre edición y preview, animaciones de chips de tags temáticos.

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/model/Note.kt` (añadir `tags: List<String> = emptyList()`)
- Modify: `app/src/main/java/com/notifast/app/domain/repository/NoteRepository.kt` & `NoteRepositoryImpl.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addnote/components/NoteFormattingToolbar.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addnote/components/NoteTagSelector.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addnote/AddNoteScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addnote/AddNoteViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addnote/AddNoteContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editnote/EditNoteScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editnote/EditNoteViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editnote/EditNoteContract.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/addnote/AddNoteViewModelTest.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/editnote/EditNoteViewModelTest.kt`

- [ ] **Step 1: Actualizar modelo `Note.kt` y repositorios con soporte de `tags`**
- [ ] **Step 2: Construir `NoteFormattingToolbar.kt` e inserción contextual en cursor**
- [ ] **Step 3: Construir `NoteTagSelector.kt` con tags sugeridos y creador interactivo**
- [ ] **Step 4: Refactorizar `AddNoteScreen.kt` y `EditNoteScreen.kt` con Live Preview y `AnimatedContent`**
- [ ] **Step 5: Crear y ejecutar tests unitarios de ViewModels**

---

### Task 6: UI/UX Impeccable — Búsqueda Multivariable y Exportación en `NotesScreen`

Aplicación de las **5 Fases Impeccable**:
1. **Alineación previa (`/grill-me` & `shape`):** Filtro compuesto: selección de materias + tags temáticos en scroll horizontal + barra de búsqueda en tiempo real + ordenamiento.
2. **Refinamiento (`distill` & `clarify`):** Búsqueda reactiva instantánea que filtra por título, contenido, materia y etiquetas simultáneamente.
3. **Construcción Modular (`≤ 250 LOC`):** Subcomponentes `NoteFilterBar.kt`, `NoteTagFilterRow.kt`, `NoteSearchBar.kt`.
4. **Resiliencia (`harden` & `audit`):** Estado vacío específico de búsqueda sin coincidencias, textos centralizados en `strings.xml`.
5. **Pulido Heurístico (`critique` & `polish`):** Exportación básica mediante `Intent.ACTION_SEND` (compartir apunte formateado como Markdown a otras apps).

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/NoteFilterBar.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/NoteTagFilterRow.kt`
- Modify: `app/src/test/java/com/notifast/app/presentation/feature/notes/NotesViewModelTest.kt`

- [ ] **Step 1: Actualizar `NotesContract.kt` y `NotesViewModel.kt` con filtrado por tags**
- [ ] **Step 2: Implementar `NoteTagFilterRow.kt` con selección múltiple / deselección**
- [ ] **Step 3: Agregar acción de compartir / exportar apunte en formato Markdown**
- [ ] **Step 4: Actualizar `NotesScreen.kt` modularizado**
- [ ] **Step 5: Ejecutar y validar tests unitarios de `NotesViewModelTest`**

---

## 🎓 SPRINT 2.3: HUB DE CALIFICACIONES Y SIMULADOR ACADÉMICO

### Task 7: Capa de Dominio & Preferencias de Escalas de Calificación (`GradeScale` & `UserPrefsRepository`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/model/GradeScale.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/repository/UserPrefsRepository.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/UserPrefsRepositoryImpl.kt`
- Modify: `app/src/test/java/com/notifast/app/data/repository/UserPrefsRepositoryTest.kt`

**Interfaces:**
- Produces: `enum class GradeScale(val minGrade: Double, val maxGrade: Double, val defaultPassingGrade: Double, val label: String)`
- Produces: `UserPrefsRepository.gradeScaleFlow: Flow<GradeScale>`, `UserPrefsRepository.minPassingGradeFlow: Flow<Double>`, `setGradeScale(scale: GradeScale)`, `setMinPassingGrade(grade: Double)`

- [ ] **Step 1: Definir `GradeScale.kt` con escalas 0-10, 0-20, 0-100 y Letras A-F**
- [ ] **Step 2: Extender `UserPrefsRepository` y `UserPrefsRepositoryImpl` con persistencia DataStore**
- [ ] **Step 3: Actualizar y verificar `UserPrefsRepositoryTest.kt`**

---

### Task 8: TDD & Algoritmo del Simulador de Calificaciones (`CalculateRequiredGradeUseCase`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/usecase/CalculateRequiredGradeUseCase.kt`
- Create: `app/src/test/java/com/notifast/app/domain/usecase/CalculateRequiredGradeUseCaseTest.kt`
- Modify: `app/src/main/java/com/notifast/app/di/UseCaseModule.kt`

**Interfaces:**
- Produces: `CalculateRequiredGradeUseCase.invoke(evaluations: List<Evaluation>, targetGrade: Double, maxScaleGrade: Double = 10.0): RequiredGradeResult`
  where `sealed interface RequiredGradeResult {`
  `  data class Completed(val finalGrade: Double, val isPassed: Boolean) : RequiredGradeResult`
  `  data class AlreadyPassed(val currentAccumulated: Double, val projectedMinGrade: Double) : RequiredGradeResult`
  `  data class Possible(val requiredGrade: Double, val remainingWeight: Double) : RequiredGradeResult`
  `  data class Impossible(val requiredGrade: Double, val maxPossibleGrade: Double) : RequiredGradeResult`
  `  data object NoRemainingWeight : RequiredGradeResult`
  `  data object InsufficientData : RequiredGradeResult`
  `}`

- [ ] **Step 1: Escribir suite de pruebas unitarias exhaustivas con todos los casos de borde (Red)**
- [ ] **Step 2: Implementar la fórmula matemática y manejo de pesos en `CalculateRequiredGradeUseCase.kt` (Green)**
- [ ] **Step 3: Registrar en `UseCaseModule.kt` y verificar tests unitarios**

---

### Task 9: UI/UX Impeccable — `GradeSimulatorCard` en `SubjectDetailScreen` & `GradesScreen`

Aplicación de las **5 Fases Impeccable**:
1. **Alineación previa (`/grill-me` & `shape`):** Presentación clara e inspiradora: "¿Cuánto necesito en los exámenes restantes para aprobar?". Slider interactivo de nota objetivo.
2. **Refinamiento (`distill` & `clarify`):** Semáforo visual instantáneo: `Aqua` (`#3B9797`) cuando la nota es alcanzable / ya aprobó vs `Ruby` (`#BF092F`) cuando se requiere más de la nota máxima o está en riesgo crítico.
3. **Construcción Modular (`≤ 250 LOC`):** Crear `GradeSimulatorCard.kt`, integrar en pestaña Resumen de `SubjectDetailScreen.kt`.
4. **Resiliencia (`harden` & `audit`):** Adaptación a la `GradeScale` del usuario, manejo de materias sin ponderaciones definidas, textos en `strings.xml`.
5. **Pulido Heurístico (`critique` & `polish`):** Sliders táctiles con feedback numérico en tiempo real, micro-animación de cambio de estado de viabilidad.

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/components/GradeSimulatorCard.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/grades/GradesScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/grades/GradesViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/grades/GradesContract.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModelTest.kt`
- Modify: `app/src/test/java/com/notifast/app/presentation/feature/grades/GradesViewModelTest.kt`

- [ ] **Step 1: Crear `GradeSimulatorCard.kt` con slider reactivo y semáforo visual Aqua/Ruby**
- [ ] **Step 2: Integrar simulación en `SubjectDetailViewModel` y `SubjectDetailScreen`**
- [ ] **Step 3: Adaptar `GradesScreen` al formato de `GradeScale` y nota mínima**
- [ ] **Step 4: Escribir y validar pruebas unitarias `SubjectDetailViewModelTest` y `GradesViewModelTest`**

---

## 🔍 AUDITORÍA DE CIERRE Y PROTOCOLO DE TRANSICIÓN

### Task 10: Verificación Integral, Cierre al 100% de Fase 2 y Kickoff Fase 3

**Files:**
- Modify: `app/src/main/res/values/strings.xml` (auditoría de cero strings quemados en UI)
- Modify: `docs/prd/STATUS.md` (marcar Fase 2 al 100%, 17/17 tareas)
- Create: `docs/prd/KICKOFF_FASE_3.md` (generación automática con prompt maestro y guía técnica de Fase 3)
- Modify: `AGENTS.md` (enlazar `KICKOFF_FASE_3.md` en la matriz de documentación)

- [ ] **Step 1: Auditoría de strings en `res/values/strings.xml` y limpieza de imports**
- [ ] **Step 2: Ejecutar suite de pruebas unitarias completa (`./gradlew testDebugUnitTest`)**
- [ ] **Step 3: Compilar APK completo (`./gradlew assembleDebug`)**
- [ ] **Step 4: Actualizar `docs/prd/STATUS.md` registrando la Fase 2 al 100%**
- [ ] **Step 5: Generar `docs/prd/KICKOFF_FASE_3.md` y actualizar enlaces en `AGENTS.md` y `README.md`**

---

## 🎯 Plan Complete and Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-22-phase-2-core-experience.md` and `implementation_plan.md`.
