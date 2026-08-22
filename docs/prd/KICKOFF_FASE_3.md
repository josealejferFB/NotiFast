# 🚀 Kickoff Oficial — Fase 3: Inteligencia Predictiva y Notificaciones Proactivas

> **Propósito:** Guía maestra de arranque y prompt de sesión para ejecutar la **Fase 3** del roadmap de NotiFast v2.0.  
> **Referencias Obligatorias:** [`PRD-MAESTRO-NOTIFAST.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md), [`STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md) y [`AGENTS.md`](file:///home/p0xd/Documentos/NotiFast/AGENTS.md).

---

## 📋 1. Prompt Copiable para Iniciar la Sesión de Fase 3

Copia y pega el siguiente bloque para iniciar de inmediato el desarrollo de la Fase 3:

```text
Actúa como Ingeniero de Software IA de Élite y Tech Lead de NotiFast. Vamos a ejecutar al 100% la Fase 3 del roadmap: "Inteligencia Predictiva y Notificaciones Proactivas", siguiendo estrictamente las especificaciones de docs/prd/KICKOFF_FASE_3.md, docs/prd/PRD-MAESTRO-NOTIFAST.md, docs/prd/STATUS.md y AGENTS.md.

Skills obligatorias requeridas:
- `systematic-debugging`: Hipótesis y aislamiento de causa raíz ante cualquier bug o fallo de tests.
- `test-driven-development`: Red ➔ Green ➔ Refactor para todos los Use Cases y lógica de programación de alarmas/analíticas.
- `verification-before-completion`: Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` con evidencia antes de dar por terminada cualquier tarea.
- `codegraph`: Exploración estructural de callers/callees y símbolos mediante `codegraph_explore`.
- `impeccable`: Diseño UI/UX premium de 5 fases para cada pantalla (StatsScreen, canales de notificación, switches y dashboards analíticos).

Estándares de higiene de código no negociables:
1. Máximo ~250 líneas por archivo composable; extraer subcomponentes a `components/`.
2. Un solo StateFlow inmutable `uiState` por ViewModel y flujo MVI `onEvent()`.
3. Cero strings quemados en UI (100% en `res/values/strings.xml`).
4. Cero colores quemados (utilizar `MaterialTheme.colorScheme.*` y tokens `Ruby` / `Aqua`).
5. Uso exclusivo de WorkManager y NotificationCompat con canales de notificación obligatorios en Android 8.0+ y permisos en tiempo de ejecución para Android 13+ (TIRAMISU).
6. Todas las operaciones de repositorio deben retornar `Result<T>` con manejo de excepciones y cero bloques try/catch vacíos.

Plan de Trabajo de Fase 3 a ejecutar:
- Sprint 3.1: Motor de Notificaciones Programadas con WorkManager (`NotificationSchedulerUseCase`, `EvaluationReminderWorker`, `ClassScheduleWorker`, `NotificationChannelsManager`, Diálogo/Pantalla de Ajustes de Notificaciones).
- Sprint 3.2: Analíticas de Rendimiento Académico y Dashboard Predictivo (`CalculateAcademicStatsUseCase`, pantalla `StatsScreen` con métricas de carga académica y proyecciones de aprobación).
- Sprint 3.3: Conectividad y Resiliencia Offline (`ConnectivityObserver`, estado visual de red y persistencia offline de Firestore).
- Protocolo de Cierre: Ejecutar suite de pruebas, compilar APK debug, actualizar `docs/prd/STATUS.md` y generar `docs/prd/KICKOFF_FASE_4.md`.

Comienza analizando el codebase con codegraph y ejecutando el Sprint 3.1 con TDD.
```

---

## 🎯 2. Objetivos y Alcance de la Fase 3

La **Fase 3** transforma a NotiFast de un organizador reactivo a un **asistente académico proactivo e inteligente**, reduciendo la carga cognitiva y el estrés estudiantil mediante:
1. **Recordatorios Programados en Segundo Plano:** Notificaciones locales automáticas 24 horas y 2 horas antes de cada examen, entrega de tarea o clase programada.
2. **Canales de Notificación y Permisos:** Configuración granular de canales (`EVALUATIONS_CHANNEL`, `CLASSES_CHANNEL`) y solicitud accesible de permisos en Android 13+.
3. **Analíticas de Rendimiento:** Dashboard visual con métricas de carga horaria, tasa de aprobación proyectada y distribución de notas por materia.
4. **Resiliencia Offline:** Detección de conectividad en tiempo real e indicador visual sutil cuando se trabaja sin conexión a Internet.

---

## 🛠️ 3. Desglose Detallado de Tareas y Criterios de Aceptación

### 🔹 Sprint 3.1 — Motor de Notificaciones Locales con WorkManager

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.1.1** | `NotificationChannelsManager.kt` | Inicializa canales de notificación en `Application.onCreate()` con prioridades correctas, vibración y descripciones claras. |
| **3.1.2** | `EvaluationReminderWorker.kt` | `CoroutineWorker` que construye y emite notificaciones ricas con deep link a la evaluación o materia correspondiente. |
| **3.1.3** | `ClassScheduleWorker.kt` | `CoroutineWorker` que alerta 15-30 minutos antes del inicio de una clase presencial/virtual con aula o enlace de reunión. |
| **3.1.4** | `ScheduleNotificationUseCase.kt` | Use Case puro que calcula el `delay` temporal exacto y programa `OneTimeWorkRequest` o `PeriodicWorkRequest` con tags únicos por ID de evaluación/horario para cancelación segura. |
| **3.1.5** | `NotificationSettingsScreen.kt` | Interfaz de configuración con switches y sliders para anticipación de recordatorios (1h, 2h, 24h) y permisos dinámicos en Android 13+. |
| **3.1.6** | Tests de Notificaciones | Pruebas unitarias para `ScheduleNotificationUseCaseTest` y validación de parámetros de WorkManager. |

---

### 🔹 Sprint 3.2 — Analíticas de Rendimiento y Dashboard Predictivo

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.2.1** | `CalculateAcademicStatsUseCase.kt` | Calcula créditos totales, porcentaje de materias aprobadas, materia de mayor/menor rendimiento y carga horaria semanal acumulada. |
| **3.2.2** | `StatsScreen.kt` & `components/` | Pantalla analítica (< 250 LOC) con tarjetas de resumen visual, barras de progreso de carga y recomendaciones de estudio. |
| **3.2.3** | `AcademicProgressChart.kt` | Componente gráfico declarativo en Canvas / Jetpack Compose para comparar promedios por materia. |
| **3.2.4** | Tests de Analíticas | Pruebas unitarias de `CalculateAcademicStatsUseCaseTest` con casos de borde (0 materias, notas perfectas, materias reprobadas). |

---

### 🔹 Sprint 3.3 — Conectividad y Modo Offline

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.3.1** | `ConnectivityObserver.kt` | `NetworkCallback` encapsulado en un `Flow<NetworkStatus>` (Available, Lost, Losing, Unavailable). |
| **3.3.2** | `OfflineBanner.kt` | Barra superior animada y discreta que notifica cambios en la conectividad sin bloquear la navegación del estudiante. |
| **3.3.3** | Tests de Conectividad | Pruebas unitarias para el mapper de conectividad y estados reactivos. |

---

## 🚫 4. Buenas Prácticas y Anti-Patrones Críticos para Fase 3

| # | Anti-Patrón Prohibido | Práctica Correcta Obligatoria |
|---|---|---|
| 1 | Usar `AlarmManager` para tareas no exactas | Usar **Android WorkManager** para garantizar ejecución resiliente a reinicios del sistema y optimización Doze. |
| 2 | Hardcodear Notification IDs o no permitir cancelación | Asignar IDs basados en el `hashCode()` o hash determinista de la entidad (`evalId`, `scheduleId`) para poder cancelarlas al eliminar/editar. |
| 3 | Solicitar permisos `POST_NOTIFICATIONS` sin contexto | Mostrar un diálogo explicativo amigable (Justificación de valor) antes de invocar el launcher de permisos de Android 13. |
| 4 | Cálculos estadísticos pesados en la UI | Centralizar todos los cálculos en `CalculateAcademicStatsUseCase` dentro del hilo IO/Default. |
| 5 | Composables de estadísticas > 250 líneas | Descomponer gráficos, tarjetas y métricas en `presentation/feature/stats/components/`. |

---

## 🔄 5. Protocolo de Transición y Cierre de Fase 3

Al completar el 100% de las tareas de la Fase 3:
1. Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.
2. Actualizar [`docs/prd/STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md) marcando Fase 3 al 100%.
3. Generar automáticamente [`docs/prd/KICKOFF_FASE_4.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_4.md) para la fase de Pulido, Micro-interacciones y Accesibilidad (a11y).
