# 📊 Estado de Ejecución y Roadmap — NotiFast v2.0

> **Documento de Seguimiento de Fases del PRD Maestro**  
> **Última actualización:** 22 de Agosto, 2026  
> **Referencia:** [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)

---

## 📈 Resumen Global de Progreso

```
Fase 0: Estabilización       [████████████████] 100% (16/16 tareas) ✅
Fase 0.1: Hardening UX       [████████████████] 100% (4/4 bloques) ✅
Fase 1: Fundamentos          [████████████████] 100% (10/10 tareas) ✅
Fase 2: Experiencia Core     [████████████████] 100% (13/13 tareas) ✅
Fase 3: Features Avanzadas   [░░░░░░░░░░░░░░░░]   0% (0/18 tareas)
Fase 4: Pulido & Experiencia [░░░░░░░░░░░░░░░░]   0% (0/12 tareas)
Fase 5: Despliegue & Tesis   [░░░░░░░░░░░░░░░░]   0% (0/16 tareas)
------------------------------------------------------------------
PROGRESO TOTAL ESTIMADO:     [████████░░░░░░░░]  48.3% (43/89 tareas)
```

---

## 🔍 Resumen de Logros — Fase 2: Experiencia Core (100% Completada)

Durante la Fase 2 se implementaron las funcionalidades centrales del cuaderno y hub académico bajo el estándar de 5 fases de Impeccable UI/UX:
- **Gestión Avanzada de Horarios y Detección de Solapes:**
  - `ValidateScheduleOverlapUseCase`: algoritmo matemático de validación de intervalos horarios con soporte para exclusión de ID en edición y detección en vivo.
  - Rediseño modular de `SchedulesScreen` (< 150 LOC) con `ScheduleDayTabs`, `ScheduleCard`, `ScheduleTimeBlockList` y banner dinámico de advertencia `ScheduleConflictBanner`.
  - Formularios `AddScheduleScreen` y `EditScheduleScreen` con detección de conflictos en tiempo real, pickers y diálogo seguro de descarte de cambios.
- **Cuaderno Inteligente y Motor Markdown:**
  - `MarkdownParser` y `NotiFastMarkdown`: motor puro en Kotlin con renderizado de encabezados (H1-H3), negritas, cursivas, listas con viñetas, bloques de código monospace, citas y checklists interactivos de tareas con estado.
  - Editor enriquecido en `AddNoteScreen` y `EditNoteScreen` con barra de formato rápido (`NoteFormattingToolbar`), selector dinámico de etiquetas temáticas (`NoteTagSelector`), vista previa en vivo ("Editar" / "Vista Previa") y auto-guardado resiliente.
  - Búsqueda multivariable en `NotesScreen` por materia, etiquetas (`NoteTagFilterRow`) y texto libre, junto con exportación directa de notas en formato Markdown mediante Android Sharesheet.
- **Hub de Calificaciones y Simulador Predictivo:**
  - Modelo formal de escalas de evaluación `GradeScale` (0-10, 0-20, 0-100, Letras A-F) integrado reactivamente con `UserPrefsRepository`.
  - `CalculateRequiredGradeUseCase`: cálculo determinista de puntaje acumulado, porcentaje de ponderación restante y nota requerida en evaluaciones pendientes para aprobar la materia con categorización de estado (`ALREADY_PASSED`, `ACHIEVABLE`, `AT_RISK`, `IMPOSSIBLE`, `NO_PENDING_EVALUATIONS`).
  - Componente interactivo `GradeSimulatorCard` en `SubjectDetailScreen` con slider de nota meta y banner visual con código de color.
- **Métricas de Calidad:**
  - **79 unit tests pasando** (0 fallos, 0 errores).
  - Build `assembleDebug` 100% exitoso.

---

## 📌 Detalle por Fases y Sprints

### 🟢 Fase 2: Experiencia Core — Cuaderno y Hub Académico (100% — Completada)
*Meta: CRUD completo de horarios con detección de solapes, editor de notas enriquecido (Markdown/formato), almacenamiento cloud de imágenes y personalización académica.*  
*Guía de Arranque:* [`docs/prd/KICKOFF_FASE_2.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_2.md)

#### Sprint 2.1 — Gestión Completa de Horarios Académicos (Completado)
- [x] **2.1.1** Caso de uso para detección y validación de solapamientos horarios (`ValidateScheduleOverlapUseCase`) *(Completado)*
- [x] **2.1.2** Pantalla `AddScheduleScreen` con selector de días de la semana, picker de horario de inicio/fin y banner de solapes en vivo *(Completado)*
- [x] **2.1.3** Pantalla `EditScheduleScreen` con carga reactiva, validación de solapes y edición segura *(Completado)*
- [x] **2.1.4** Integración de visualización por bloques temporales en `SchedulesScreen` *(Completado)*
- [x] **2.1.5** Unit tests para validación de horarios y ViewModels de horarios *(Completado)*

#### Sprint 2.2 — Cuaderno Inteligente y Editor de Apuntes (Completado)
- [x] **2.2.1** Soporte de Markdown / texto enriquecido (negritas, listas, encabezados, citas, código) vía `NotiFastMarkdown` *(Completado)*
- [x] **2.2.2** Previsualización en tiempo real del apunte formateado con pestañas Editar / Vista Previa *(Completado)*
- [x] **2.2.3** Selector dinámico de etiquetas / tags temáticos por apunte (`NoteTagSelector`) *(Completado)*
- [x] **2.2.4** Búsqueda y filtrado avanzado de apuntes por contenido y tags (`NoteTagFilterRow`) *(Completado)*
- [x] **2.2.5** Exportación básica de apuntes en formato texto/Markdown vía Sharesheet *(Completado)*

#### Sprint 2.3 — Hub de Calificaciones y Personalización (Completado)
- [x] **2.3.1** Configuración de escala de calificaciones (0-10, 0-20, 0-100, Letras A-F) vía `GradeScale` y `UserPrefsRepository` *(Completado)*
- [x] **2.3.2** Indicador de notas mínimas aprobatorias configurables en `GradesScreen` *(Completado)*
- [x] **2.3.3** Simulación de nota requerida en evaluaciones pendientes para aprobar la materia (`CalculateRequiredGradeUseCase` + `GradeSimulatorCard`) *(Completado)*

---

### ⚪ Fase 3: Features Avanzadas — Inteligencia Predictiva y Notificaciones Proactivas (Siguiente Fase)
*Meta: Notificaciones locales inteligentes vía Android WorkManager, recordatorios proactivos de entregas/clases, sincronización offline local y analíticas de estudio.*  
*Guía de Arranque:* [`docs/prd/KICKOFF_FASE_3.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_3.md)

#### Sprint 3.1 — Motor de Notificaciones Locales y Recordatorios Proactivos con WorkManager
- [ ] **3.1.1** `NotificationSchedulerUseCase` y Worker de Android `NotificationWorker` para recordatorios programados (24h antes, 2h antes de evaluaciones y clases)
- [ ] **3.1.2** Canales de notificación en Android (`NotificationChannel`) para Evaluaciones, Clases y Alertas Académicas con permisos Android 13+ (`POST_NOTIFICATIONS`)
- [ ] **3.1.3** Pantalla / Diálogo de Configuración de Notificaciones (activar/desactivar recordatorios, anticipación en horas/días)
- [ ] **3.1.4** Unit tests para el scheduler y serialización de parámetros de background

#### Sprint 3.2 — Analíticas de Rendimiento Académico y Dashboard Predictivo
- [ ] **3.2.1** `CalculateAcademicStatsUseCase` (índice de aprobación por materia, distribución de calificaciones, carga horaria semanal)
- [ ] **3.2.2** Dashboard analítico en `StatsScreen` con gráficos de rendimiento (Compose Canvas / barras de progreso segmentadas)
- [ ] **3.2.3** Recomendaciones inteligentes de estudio basadas en proximidad de evaluaciones y dificultad

#### Sprint 3.3 — Modo Offline y Sincronización Resiliente
- [ ] **3.3.1** Caché de datos Firestore persistente y manejo de conectividad en tiempo real (`ConnectivityObserver`)
- [ ] **3.3.2** Indicador visual de estado de sincronización (Offline / Sincronizando / Sincronizado) en UI

---

### ⚪ Fase 4: Pulido & Experiencia (Pendiente)
*Meta: Micro-interacciones con Impeccable, accesibilidad completa (a11y), y optimización de rendimiento Compose.*

---

### ⚪ Fase 5: Despliegue & Tesis (Pendiente)
*Meta: Testing en aula real, recolección de métricas de tesis y publicación en Google Play Store.*
