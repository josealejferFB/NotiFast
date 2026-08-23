# Agenda Académica Unificada (NotiFast 3.0) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Unificar y fusionar las capacidades de seguimiento de evaluaciones dentro de la pantalla de Horarios (`SchedulesScreen`), transformándola en el hub central de Agenda Académica, y consolidar la navegación global en 4 pestañas (`Apuntes`, `Agenda`, `Calificaciones`, `Ajustes`).

**Architecture:** MVI unificado en `SchedulesViewModel` que combina flujos reactivos de `ScheduleRepository` y `EvaluationRepository`. Vista tri-modo (`DAILY_TIMELINE`, `MONTHLY_CALENDAR`, `WEEKLY_BENTO`) construida con composables modulares ($\le 200$ LOC por archivo) bajo el sistema de diseño NotiFast 3.0 (Muted Crimson `#9E1B32` y Aqua Green `#2A7B7B`).

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose (Material 3 BOM 2024.09.00), Hilt 2.59, Navigation Compose 2.8.5, MockK, Kotlinx Coroutines Test.

**Spec:** [`docs/superpowers/specs/2026-08-23-unified-academic-agenda-design.md`](file:///home/p0xd/Documentos/NotiFast/docs/superpowers/specs/2026-08-23-unified-academic-agenda-design.md)

## Global Constraints
- Máximo ~200 líneas de código por archivo composable.
- Cero strings hardcodeados en UI (consumir exclusivamente desde `res/values/strings.xml`).
- Cero colores arbitrarios quemados (usar `MaterialTheme.colorScheme` y parseo centralizado).
- Micro-bordes de 1dp (`outlineVariant`), elevación 0dp, física táctil `0.97f`.
- Manejo riguroso de errores (`Result<T>`, propagación a `uiState.error`).

---

### Task 1: Capa de Dominio & ViewModel Unificado (TDD)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesViewModel.kt`
- Modify: `app/src/test/java/com/notifast/app/presentation/feature/schedules/SchedulesViewModelTest.kt`

**Interfaces:**
- Consumes: `ScheduleRepository.getSchedules(userId)`, `EvaluationRepository.getEvaluations(userId)`, `AuthRepository.getCurrentUserId()`
- Produces: `SchedulesUiState` extendido con `evaluations`, `evaluationsByDate`, `dailyEvaluations`, `selectedDate`, `currentMonth`, `MONTHLY_CALENDAR` en `ScheduleViewMode`, y eventos `SelectDate`, `ChangeMonth`, `GoToToday`, `DeleteEvaluation`.

- [ ] **Step 1: Escribir pruebas unitarias que fallen para las nuevas capacidades en `SchedulesViewModelTest.kt`**
- [ ] **Step 2: Ejecutar `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.schedules.SchedulesViewModelTest"` para verificar que falla**
- [ ] **Step 3: Actualizar `SchedulesContract.kt` con el nuevo `ScheduleViewMode`, campos de evaluaciones en `SchedulesUiState` y eventos en `SchedulesEvent`**
- [ ] **Step 4: Implementar la inyección de `EvaluationRepository` y la lógica reactiva combinada en `SchedulesViewModel.kt`**
- [ ] **Step 5: Ejecutar la suite de tests y verificar que pasa al 100%**

---

### Task 2: Actualización de Strings del Sistema

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Produces: Nuevas cadenas `agenda_*` para el selector tri-vista, títulos de evaluaciones, diálogo de borrado y modal de creación rápida.

- [ ] **Step 1: Añadir las nuevas claves string en `res/values/strings.xml`**
- [ ] **Step 2: Verificar sintaxis XML limpia y sin duplicados**

---

### Task 3: Subcomponentes Modulares de la Agenda ($\le 200$ LOC)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/AgendaViewModeToggle.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleDaySelector.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/AgendaDayEvaluationsSection.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/AgendaEvaluationCard.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/AgendaMonthCalendarView.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/AgendaEvaluationBottomSheet.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/AgendaCreateActionBottomSheet.kt`

**Interfaces:**
- Consumes: Tokens de `MaterialTheme.colorScheme`, componentes atómicos de `atoms/` (`NotiFastCard`, `NotiFastButton`, `NotiFastDialog`), modelos `Schedule` y `Evaluation`.
- Produces: Componentes reutilizables con soporte táctil, bordes de 1dp y animaciones fluidas.

- [ ] **Step 1: Crear `AgendaViewModeToggle.kt` con animación tipo pill para los 3 modos**
- [ ] **Step 2: Adaptar `ScheduleDaySelector.kt` para mostrar indicadores de clases y exámenes por día**
- [ ] **Step 3: Crear `AgendaEvaluationCard.kt` y `AgendaDayEvaluationsSection.kt` para el timeline diario**
- [ ] **Step 4: Crear `AgendaMonthCalendarView.kt` para la navegación mensual con cuadrícula de días**
- [ ] **Step 5: Crear `AgendaEvaluationBottomSheet.kt` y `AgendaCreateActionBottomSheet.kt`**

---

### Task 4: Ensamblado de `SchedulesScreen.kt` y Actualización de Navegación

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/navigation/Screen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/navigation/NavGraph.kt`

**Interfaces:**
- Consumes: Subcomponentes de `schedules/components/`, `SchedulesViewModel`.
- Produces: Agenda unificada y barra de navegación de 4 pestañas.

- [ ] **Step 1: Refactorizar `SchedulesScreen.kt` integrando el alternador de 3 vistas, FAB inteligente y BottomSheets modal ($\le 200$ LOC)**
- [ ] **Step 2: Actualizar `Screen.kt` ajustando `bottomNavItems = listOf(Notes, Schedules, Grades, Settings)` y título "Agenda"**
- [ ] **Step 3: Actualizar `NavGraph.kt` enrutando callbacks de Horarios y Evaluaciones hacia `SchedulesScreen` y removiendo la ruta obsoleta de Calendar**

---

### Task 5: Desacoplamiento y Limpieza de Código Obsoleto

**Files:**
- Delete: `app/src/main/java/com/notifast/app/presentation/feature/calendar/` (todos sus archivos)
- Delete: `app/src/test/java/com/notifast/app/presentation/feature/calendar/CalendarViewModelTest.kt`

- [ ] **Step 1: Eliminar archivos de la carpeta `calendar/` y su test obsoleto**
- [ ] **Step 2: Verificar que no queden referencias ni imports huérfanos en todo el proyecto**

---

### Task 6: Verificación Integral & Compilación

**Files:**
- Test: Todas las suites de pruebas unitarias

- [ ] **Step 1: Ejecutar `./gradlew testDebugUnitTest` y asegurar 100% pruebas pasando**
- [ ] **Step 2: Ejecutar `./gradlew assembleDebug` y verificar build limpio**
- [ ] **Step 3: Actualizar `docs/prd/STATUS.md` reflejando la culminación exitosa de la unificación**
