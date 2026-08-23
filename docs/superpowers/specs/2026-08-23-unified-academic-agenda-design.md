# Especificación de Diseño: Unificación de Agenda Académica (NotiFast 3.0)

## 📌 1. Resumen Ejecutivo
Esta especificación define la arquitectura técnica y el diseño UX para fusionar la pantalla independiente de Calendario de Evaluaciones dentro de la pantalla de Horarios (`SchedulesScreen`), transformándola en el hub central de **Agenda Académica**.

Se consolida la navegación global reduciendo la barra inferior de 5 a **4 pestañas esenciales**:
1. 📚 **Apuntes** (`Notes`)
2. 🗓️ **Agenda** (`Schedules` / `Agenda`)
3. 🎓 **Calificaciones** (`Grades`)
4. ⚙️ **Ajustes** (`Settings`)

---

## 🎯 2. Objetivos y Criterios de Aceptación
1. **Reducción de Fricción Cognitiva:** El estudiante visualiza en una sola vista diaria tanto sus bloques de clase como las evaluaciones y entregas de la jornada.
2. **Selector Tri-Vista Ergonómico:** Alternancia fluida entre 3 modos:
   - **Timeline Diario:** Clases del día + Hitos de evaluación con insignias de urgencia y cálculo de ventanas de estudio.
   - **Calendario Mensual:** Cuadrícula de 30/31 días con indicadores cromáticos de materia para planificar el mes.
   - **Semana Bento:** Métricas analíticas de carga horaria, días más activos y distribución por materias.
3. **MVI Unificado y Clean Architecture:** `SchedulesViewModel` combina reactivamente `ScheduleRepository` y `EvaluationRepository` en un único `SchedulesUiState`.
4. **Higiene NotiFast 3.0:**
   - Todos los composables $\le 200$ líneas de código.
   - Design tokens semánticos (Muted Crimson `#9E1B32` y Aqua Green `#2A7B7B`).
   - Micro-bordes de 1dp (`outlineVariant`), elevación 0dp, física táctil `0.97f`.
   - Cero cadenas o colores quemados (100% en `strings.xml` y `MaterialTheme.colorScheme`).
5. **Cero Código Muerto / Rutas Huérfanas:** Desacoplamiento y eliminación limpia de la carpeta obsoleta `presentation/feature/calendar/` sin dejar imports rotos.

---

## 🏗️ 3. Arquitectura de Estado (MVI)

### `ScheduleViewMode` (Enum)
```kotlin
enum class ScheduleViewMode {
    DAILY_TIMELINE,
    MONTHLY_CALENDAR,
    WEEKLY_BENTO
}
```

### `SchedulesUiState` (Data Class Inmutable)
```kotlin
data class SchedulesUiState(
    val isLoading: Boolean = false,
    val schedules: List<Schedule> = emptyList(),
    val groupedSchedules: Map<Int, List<Schedule>> = emptyMap(),
    val evaluations: List<Evaluation> = emptyList(),
    val evaluationsByDate: Map<String, List<Evaluation>> = emptyMap(),
    val dailyEvaluations: List<Evaluation> = emptyList(),
    val selectedDay: Int = currentDayOfWeek(),
    val selectedDate: Calendar = Calendar.getInstance(),
    val currentMonth: Calendar = Calendar.getInstance(),
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAILY_TIMELINE,
    val liveClassStatus: LiveClassStatus = LiveClassStatus.NoClassesToday,
    val timelineItems: List<ScheduleTimelineItem> = emptyList(),
    val conflicts: List<Schedule> = emptyList(),
    val weeklySummary: WeeklyScheduleSummary = WeeklyScheduleSummary(),
    val error: String? = null,
    val userFirstName: String = "",
    val userEmail: String = "",
    val userProfilePictureUrl: String? = null
)
```

### `SchedulesEvent` (Sealed Interface)
```kotlin
sealed interface SchedulesEvent {
    data object LoadData : SchedulesEvent
    data object RefreshLiveStatus : SchedulesEvent
    data class SelectDay(val dayOfWeek: Int) : SchedulesEvent
    data class SelectDate(val date: Calendar) : SchedulesEvent
    data class ChangeMonth(val amount: Int) : SchedulesEvent
    data object GoToToday : SchedulesEvent
    data class SetViewMode(val mode: ScheduleViewMode) : SchedulesEvent
    data class DeleteSchedule(val schedule: Schedule) : SchedulesEvent
    data class DeleteEvaluation(val evaluation: Evaluation) : SchedulesEvent
    data object ClearError : SchedulesEvent
}
```

---

## 🎨 4. Desglose Modular de Componentes UI

```
presentation/feature/schedules/
├── SchedulesScreen.kt                         # Screen principal orquestadora (<= 200 LOC)
├── SchedulesViewModel.kt                      # ViewModel Hilt con StateFlow unificado
├── SchedulesContract.kt                       # UiState + Events + Modos + Sealed Interfaces
└── components/
    ├── AgendaViewModeToggle.kt                # Selector de 3 modos (Timeline | Calendario | Semana)
    ├── ScheduleDaySelector.kt                 # Barra semanal L-D con badges de clase + dot de examen
    ├── AgendaDayEvaluationsSection.kt         # Sección destacada de evaluaciones del día seleccionado
    ├── AgendaEvaluationCard.kt                # Tarjeta de examen/entrega con badge de urgencia y peso %
    ├── AgendaMonthCalendarView.kt             # Cuadrícula mensual interactiva y lista de fecha
    ├── AgendaEvaluationBottomSheet.kt         # Detalle modal de evaluación con acciones
    ├── AgendaCreateActionBottomSheet.kt       # Modal de creación rápida (Horario vs Evaluación)
    ├── ScheduleLiveStatusCard.kt              # Beacon dinámico (En curso, próxima clase, examen hoy)
    ├── LiveStatusBeaconContent.kt             # Contenido interno del beacon
    ├── ScheduleTimelineView.kt                # Timeline de clases y ventanas libres
    ├── ScheduleCard.kt                        # Tarjeta de clase con física táctil y tags
    ├── ScheduleGapCard.kt                     # Tarjeta de ventana libre de estudio
    ├── ScheduleConflictBanner.kt              # Alerta de solapamiento
    ├── ScheduleWeekBentoSummary.kt            # Cuadrícula Bento de estadísticas semanales
    └── ScheduleDistributionBarChart.kt        # Gráfico de barras de carga semanal
```

---

## 🧭 5. Actualización de Rutas y Navegación

1. **`Screen.kt`**:
   - `Screen.Calendar` removido.
   - `Screen.Schedules`: Título actualizado a `"Agenda"`.
   - `Screen.bottomNavItems = listOf(Notes, Schedules, Grades, Settings)` (4 ítems).
2. **`NavGraph.kt`**:
   - Eliminada la entrada `composable(Screen.Calendar.route)`.
   - `SchedulesScreen` recibe callbacks para:
     * `onAddScheduleClick: () -> Unit`
     * `onAddEvaluationClick: () -> Unit`
     * `onEditScheduleClick: (String) -> Unit`
     * `onEditEvaluationClick: (Evaluation) -> Unit`
     * `onTakeNoteClick: (String) -> Unit`
     * `onEditProfile: () -> Unit`
3. **Deprecación de Archivos:**
   - Eliminar `app/src/main/java/com/notifast/app/presentation/feature/calendar/` y su archivo de pruebas unitarias antiguo (reemplazado y ampliado en `SchedulesViewModelTest.kt`).

---

## 🧪 6. Estrategia de Pruebas Unitarias (TDD)
En `SchedulesViewModelTest.kt`:
1. `when initialized, loads schedules and evaluations concurrently and groups properly`
2. `when date is selected in timeline or calendar, updates dailyEvaluations and selectedDate`
3. `when day of week is selected, filters timeline items and calculates gaps`
4. `when month changes, updates currentMonth and recalibrates month grid`
5. `when deleteEvaluation is triggered, calls evaluationRepository and handles failure gracefully`
6. `when deleteSchedule is triggered, calls scheduleRepository and updates state`
7. `when switching view mode between DAILY_TIMELINE, MONTHLY_CALENDAR and WEEKLY_BENTO, updates state cleanly`

---

## 📱 7. Verificación de Compilación y Calidad
- `./gradlew testDebugUnitTest` (100% pasando).
- `./gradlew assembleDebug` (compilación limpia sin warnings ni imports rotos).
- Verificación en dispositivo físico Android.
