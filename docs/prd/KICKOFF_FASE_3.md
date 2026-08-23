# 🚀 Kickoff Oficial — Fase 3: Inteligencia Predictiva, Notificaciones Proactivas y Features Avanzadas

> **Propósito:** Guía maestra de arranque y prompt de sesión para ejecutar la **Fase 3** del roadmap de NotiFast v3.0, adaptada rigurosamente a la arquitectura actual del proyecto (Sistema de Diseño Tactile Studio 3.0, navegación en 4 pestañas, Agenda unificada, DataStore Preferences, centro de Ajustes Bento, compartición P2P de materias y optimización para el plan gratuito de Firebase).  
> **Referencias Obligatorias:** [`PRD-MAESTRO-NOTIFAST.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md), [`STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md), [`DESIGN.md`](file:///home/p0xd/Documentos/NotiFast/DESIGN.md) y [`AGENTS.md`](file:///home/p0xd/Documentos/NotiFast/AGENTS.md).

---

## 📋 1. Prompt Copiable para Iniciar la Sesión de Fase 3

Copia y pega el siguiente bloque para iniciar de inmediato el desarrollo de la Fase 3 en una nueva sesión:

```text
Actúa como Ingeniero de Software IA de Élite y Tech Lead de NotiFast. Vamos a ejecutar al 100% la Fase 3 del roadmap: "Inteligencia Predictiva, Notificaciones Proactivas y Features Avanzadas", siguiendo estrictamente las especificaciones de docs/prd/KICKOFF_FASE_3.md, docs/prd/PRD-MAESTRO-NOTIFAST.md, docs/prd/STATUS.md, DESIGN.md y AGENTS.md.

Contexto de la arquitectura actual (NotiFast v3.0):
- Paquete base: `com.notifast.app`
- Sistema de Diseño 3.0 Tactile Studio: Paleta editorial (`WarmPaperCanvas`, `SlateDesk`, `StudioIndigo`, `TerracottaWarm`, `SageForest`, `CrimsonBrick`), tipografía dual (`PlusJakartaSansFamily` para UI y `JetBrainsMonoFamily` / `Typography.monoAcademic` para números, métricas y horas), y componentes atómicos táctiles en `presentation/atoms/` con resortes `0.97f` y haptic feedback.
- Navegación consolidada en 4 pestañas principales: `Apuntes` (`Screen.Notes`), `Agenda` (`Screen.Schedules`), `Calificaciones` (`Screen.Grades`) y `Ajustes` (`Screen.Settings`).
- Las preferencias de alertas ya están modeladas en `UserPrefsRepository` (DataStore) y los diálogos de configuración (`SettingsAlertTimingDialog`) ya residen en `SettingsScreen`.
- La Agenda unifica cronograma, Live Status beacon con cuenta regresiva, detección de ventanas libres de estudio (gaps >= 45m), calendario mensual y resumen bento semanal.
- El detalle de materia opera en 4 cuadrantes modulares: Panorama (con simulador reactivo), Evaluaciones, Apuntes y Horarios.
- Restricción de Infraestructura: Operamos bajo el plan gratuito Spark de Firebase (cuotas limitadas de Cloud Storage y lecturas/escrituras en Firestore). Se prioriza el intercambio local P2P (Android Nearby / Quick Share, QR Code, archivos de respaldo `.notifast`) y optimización estricta de cuotas en la nube.

Skills obligatorias requeridas:
- `systematic-debugging`: Hipótesis y aislamiento de causa raíz ante cualquier bug o fallo de tests.
- `test-driven-development`: Red ➔ Green ➔ Refactor para todos los Use Cases, Workers de WorkManager, serializadores de materias y lógica predictiva.
- `verification-before-completion`: Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` con evidencia antes de dar por terminada cualquier tarea.
- `codegraph`: Exploración estructural de callers/callees y dependencias mediante `codegraph_explore`.
- `impeccable`: Diseño UI/UX premium alineado con DESIGN.md para gráficos en Canvas, modales de compartición/Nearby, banners de conectividad y diálogos de permisos.

Estándares de higiene de código no negociables:
1. Límite estricto de ~250 líneas por archivo composable (modularizar subcomponentes en `components/`).
2. Un solo StateFlow inmutable `uiState` por ViewModel y flujo MVI unidireccional `onEvent()`.
3. Cero strings quemados en UI (100% en `res/values/strings.xml`).
4. Cero colores quemados (utilizar `MaterialTheme.colorScheme.*`, tokens semánticos y `ThemeUtils`).
5. Uso exclusivo de Android WorkManager con tags deterministas basados en ID para cancelación y actualización limpia de alertas.
6. Manejo transparente y accesible de permisos en tiempo de ejecución para Android 13+ (`POST_NOTIFICATIONS`) y permisos de Nearby/Bluetooth con diálogo explicativo previo (`NotiFastDialog`).
7. Todas las operaciones de repositorio deben retornar `Result<T>` con manejo estricto de excepciones y cero bloques try/catch vacíos.

Plan de Trabajo de Fase 3 a ejecutar:
- Sprint 3.1: Motor de Notificaciones Proactivas con WorkManager (Channels, Workers para Evaluaciones, Clases y Resumen Diario matutino 07:00 AM, BroadcastReceiver de reinicio `BootCompletedReceiver`, Use Cases de programación/cancelación, y conexión con las preferencias existentes de `UserPrefsRepository` y `SettingsScreen`).
- Sprint 3.2: Analíticas de Rendimiento Académico y Dashboard Predictivo (`CalculateAcademicStatsUseCase`, integración de vista conmutada o sección Bento en la pestaña `Calificaciones`, gráficos declarativos en Canvas con `JetBrains Mono` y detección de materias en riesgo crítico).
- Sprint 3.3: Búsqueda Global Multientidad (`SearchGlobalUseCase` con debounce de 300ms, modal táctil/bottom sheet de búsqueda global con resultados categorizados por Materias, Apuntes, Evaluaciones y Horarios, con navegación directa).
- Sprint 3.4: Sistema de Compartir Materia ("Pack Académico"), Intercambio P2P y Resiliencia Offline (Exportación/Importación completa de materias con horarios y syllabus de evaluaciones, soporte multicanal: Android Nearby Connections / Quick Share P2P sin internet, código QR comprimido, archivo `.notifast` vía Android Sharesheet, enlace Firestore con TTL de 7 días, y `ConnectivityObserver` reactivo con `OfflineIndicatorBanner`).
- Protocolo de Cierre: Ejecutar suite de pruebas completa (`./gradlew testDebugUnitTest`), compilar APK (`./gradlew assembleDebug`), actualizar `docs/prd/STATUS.md` y generar `docs/prd/KICKOFF_FASE_4.md`.

Comienza analizando el codebase con codegraph y ejecutando el Sprint 3.1 con TDD.
```

---

## 🎯 2. Objetivos y Alcance de la Fase 3 Adaptada

La **Fase 3** eleva a NotiFast de un hub organizador a un **asistente académico colaborativo, proactivo e inteligente**, anticipándose a las necesidades del estudiante y facilitando la colaboración en el aula sin depender de costosa infraestructura en la nube:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                          MAPA DE CAPACIDADES DE LA FASE 3                              │
├─────────────────────────┬──────────────────────────────────┬───────────────────────────┤
│ Módulo                  │ Estado Previo                    │ Transformación Fase 3     │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 1. Notificaciones       │ Preferencias en DataStore sin    │ Motor WorkManager activo, │
│    Proactivas           │ disparador real en segundo plano │ canales, workers y boot   │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 2. Analíticas           │ Promedio ponderado básico y      │ Dashboard predictivo con  │
│    Predictivas          │ simulador en detalle de materia  │ gráficos Canvas y riesgo  │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 3. Búsqueda             │ Filtros locales separados por    │ Búsqueda global unificada │
│    Global               │ pantalla (solo notas o materias) │ multientidad con debounce │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 4. Compartir Materia    │ Asignaturas aisladas sin         │ "Pack Académico" P2P      │
│    y Social P2P         │ mecanismo de respaldo o envío    │ (Nearby, QR, .notifast)   │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 5. Resiliencia & Cuotas │ Dependencia potencial de cloud   │ Zero-cloud sharing, caché │
│    Free Tier            │ storage sin control de cuotas    │ offline y ahorro de datos │
└─────────────────────────┴──────────────────────────────────┴───────────────────────────┘
```

---

## 🛠️ 3. Desglose Detallado de Sprints y Criterios de Aceptación

### 🔹 Sprint 3.1 — Motor de Notificaciones Proactivas con WorkManager

> **Nota de Contexto:** La interfaz de usuario y las preferencias en DataStore (`classAlertsEnabled`, `classAlertMinutes`, `evalAlertsEnabled`, `evalAlertDays`, `dailySummaryEnabled`) **ya existen** en `UserPrefsRepository` y `SettingsScreen`. El objetivo de este sprint es construir el motor de fondo, los Workers, los Use Cases de programación y el flujo de permisos de Android 13+.

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.1.1** | Dependencias WorkManager | Añadir `androidx.work:work-runtime-ktx` y `androidx.hilt:hilt-work` en `libs.versions.toml` y `app/build.gradle.kts`, configurando `HiltWorkerFactory` en `NotiFastApplication`. |
| **3.1.2** | `NotificationChannelsManager.kt` | Inicialización centralizada de canales (`EVALUATIONS_CHANNEL`, `CLASSES_CHANNEL`, `DAILY_SUMMARY_CHANNEL`) con prioridades adecuadas, vibración háptica y nombres en español. |
| **3.1.3** | `EvaluationReminderWorker.kt` | `@HiltWorker` que consulta la evaluación, valida si sigue pendiente/activa y emite una notificación táctil con deep link a la materia o evaluación. |
| **3.1.4** | `ClassScheduleWorker.kt` | `@HiltWorker` que alerta con $N$ minutos de anticipación (10, 15, 30 min) sobre la clase inminente, indicando salón, profesor o enlace virtual de reunión. |
| **3.1.5** | `DailySummaryWorker.kt` | `@HiltWorker` periódico (07:00 AM) que recopila las clases del día y evaluaciones de las próximas 48h en un resumen condensado. |
| **3.1.6** | `BootCompletedReceiver.kt` | `BroadcastReceiver` con permiso `RECEIVE_BOOT_COMPLETED` que reprograma las alertas activas tras un reinicio del dispositivo. |
| **3.1.7** | Use Cases de Alertas | `ScheduleEvaluationAlertUseCase.kt`, `ScheduleClassAlertUseCase.kt`, `CancelAlertUseCase.kt` y `RescheduleAllAlertsUseCase.kt` con tags deterministas `eval_{id}` / `schedule_{id}`. |
| **3.1.8** | Permisos Android 13+ & UI Hook | Integración en `SettingsScreen` y al crear evaluaciones/horarios de un diálogo explicativo (`NotiFastDialog`) antes de lanzar el request de `POST_NOTIFICATIONS`. |
| **3.1.9** | Tests de Notificaciones | Pruebas unitarias completas para el cálculo de delays, cancelación y lógica de los Use Cases con coroutines test. |

---

### 🔹 Sprint 3.2 — Analíticas de Rendimiento Académico y Dashboard Predictivo

> **Nota de Contexto:** Para preservar la barra de navegación de 4 pestañas (`Apuntes`, `Agenda`, `Calificaciones`, `Ajustes`), el dashboard analítico se integra dentro de la pestaña `Calificaciones` mediante un conmutador segmentado táctil (`[ Resumen Materias | Analíticas & Proyecciones ]`) o como panel expansible Bento.

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.2.1** | `CalculateAcademicStatsUseCase.kt` | Use Case puro que computa: total de créditos/materias aprobadas vs en curso, tasa de aprobación proyectada (basada en notas actuales y peso restante), materia con mayor/menor rendimiento, correlación de carga horaria semanal vs calificaciones, y lista de materias en riesgo crítico ($< 11.0$ o nota mínima configurada). |
| **3.2.2** | `AcademicProgressChart.kt` | Componente gráfico declarativo en Canvas puro / Jetpack Compose con barras comparativas estilizadas con la paleta de materias y fuentes `JetBrains Mono`. |
| **3.2.3** | `GradeDistributionChart.kt` | Histograma o gráfico de distribución de notas agrupadas por rangos (ej. Excelente, Bueno, Regular, En Riesgo) con tokens de diseño Tactile Studio. |
| **3.2.4** | `AcademicAnalyticsContent.kt` | Contenedor Bento modular (< 200 LOC) con tarjetas de diagnóstico temprano, insights de rendimiento, barra de avance de ciclo y recomendaciones de estudio. |
| **3.2.5** | Conmutador en `GradesScreen.kt` | Integración ergonómica en `GradesScreen` manteniendo SRP ($\le 200$ LOC) mediante tabs segmentados táctiles. |
| **3.2.6** | Tests de Analíticas | Pruebas unitarias exhaustivas en `CalculateAcademicStatsUseCaseTest` para casos de borde (sin materias, materias sin notas, 100% aprobadas, escala personalizada 0-10 y 0-100). |

---

### 🔹 Sprint 3.3 — Búsqueda Global Multientidad

> **Nota de Contexto:** NotiFast cuenta con buscador local en la pestaña de Apuntes. Este sprint introduce una **Búsqueda Global Unificada** accesible desde la TopBar (`NotiFastTopBar`) o mediante atajo táctil para localizar en milisegundos cualquier elemento académico.

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.3.1** | `SearchGlobalUseCase.kt` | Use Case puro que ejecuta búsquedas concurrentes en memoria y repositorios con debounce de 300ms sobre Materias, Apuntes (título, contenido y tags), Evaluaciones (nombre y tipo) y Horarios (salón, profesor, día). |
| **3.3.2** | `SearchViewModel.kt` & `SearchContract.kt` | ViewModel MVI con StateFlow unificado que gestiona query, histórico de búsquedas recientes, filtros rápidos por entidad y estados vacío/cargando. |
| **3.3.3** | `GlobalSearchBottomSheet.kt` | Sheet táctil modal de búsqueda rápida con campo de texto pergamino, chips de filtro (`Todos`, `Apuntes`, `Materias`, `Evaluaciones`, `Horarios`) y resultados categorizados con monogramas y fechas. |
| **3.3.4** | Integración de Navegación | Al pulsar un resultado, navega instantáneamente al destino correspondiente (`SubjectDetailScreen`, `EditNote`, `EditEvaluation`, `Agenda`) cerrando el buscador. |
| **3.3.5** | Tests de Búsqueda | Pruebas unitarias para `SearchGlobalUseCaseTest` y `SearchViewModelTest` validando debounce, normalización de acentos/mayúsculas y ordenamiento por relevancia. |

---

### 🔹 Sprint 3.4 — Compartir Materia ("Pack Académico"), Intercambio P2P y Resiliencia Free-Tier

> **Nota de Contexto:** Para respetar las cuotas del plan gratuito de Firebase y permitir el intercambio instantáneo entre compañeros en el aula (incluso sin señal o internet), implementamos un sistema integral de **"Compartir Materia Completa"** que empaqueta metadatos de la asignatura, bloques de horarios semanales y syllabus de evaluaciones con fechas y ponderaciones.

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.4.1** | `ExportSubjectPackageUseCase.kt` & `ImportSubjectPackageUseCase.kt` | Empaqueta una materia completa (datos básicos, horarios semanales, syllabus de evaluaciones con ponderaciones y apuntes clave en texto/markdown) en un schema JSON compacto `.notifast` comprimido en Base64/GZIP. Al importar, valida nombres duplicados y ofrece fusionar o crear nueva materia con 0 esfuerzo. |
| **3.4.2** | `NearbyShareManager.kt` (P2P Android Nearby) | Integración con Google Play Services Nearby Connections API para descubrimiento y transferencia directa dispositivo a dispositivo en el aula sin internet y con 0 consumo de Firebase. |
| **3.4.3** | `QrCodeShareDialog.kt` & `QrScannerBottomSheet.kt` | Generador de código QR comprimido para materias/horarios y escáner de cámara integrado para transferencia óptica instantánea en persona (100% offline). |
| **3.4.4** | `ShareSubjectBottomSheet.kt` | Modal táctil en `SubjectDetailScreen` con 4 canales de distribución: **1)** *Quick Share / Nearby P2P*, **2)** *Código QR en Pantalla*, **3)** *Exportar Archivo `.notifast` vía Sharesheet*, **4)** *Enlace en la Nube (con TTL de expiración de 7 días en Firestore para no saturar cuota gratuita)*. |
| **3.4.5** | `ShareNoteUseCase.kt` | Permite exportar apuntes individuales a Markdown limpio, texto para portapapeles o archivo adjunto sin saturar Cloud Storage con imágenes pesadas. |
| **3.4.6** | `ConnectivityObserver.kt` & `OfflineIndicatorBanner.kt` | Monitoreo de conectividad (`Flow<NetworkStatus>`) y micro-banner táctil no intrusivo con soporte y verificación de caché local de Firestore (`AppModule`). |
| **3.4.7** | Tests de Importación / Exportación y P2P | Pruebas unitarias de serialización/deserialización, integridad de datos, manejo de colisiones y casos de payload corrupto en `SubjectPackageTest`. |

---

## 🚫 4. Buenas Prácticas, Restricciones Free Tier y Anti-Patrones Críticos

| # | Anti-Patrón Prohibido | Práctica Correcta Obligatoria |
|---|---|---|
| 1 | Subir imágenes pesadas a Firebase Cloud Storage sin control | Almacenar imágenes localmente en el almacenamiento privado del dispositivo (`cacheDir`/`filesDir`) y usar Cloud Storage exclusivamente cuando el usuario lo solicite explícitamente o sincronizar solo metadatos y texto enriquecido para preservar la cuota gratuita de 5 GB. |
| 2 | Guardar documentos de compartición en Firestore indefinidamente | Establecer un campo de expiración `expiresAt` (TTL de 7 días) y limpiar documentos obsoletos para mantener la base de datos dentro de los 1 GB / 50k lecturas diarias gratuitas de Firestore. |
| 3 | Requerir internet obligatorio para que dos compañeros compartan una materia | Priorizar **Android Nearby Connections P2P** o **Códigos QR** que transfieren el syllabus y horarios directamente entre teléfonos sin requerir conexión a internet ni servidores. |
| 4 | Usar `AlarmManager` para tareas que no requieren precisión al milisegundo | Utilizar **Android WorkManager** para garantizar ejecución resiliente a reinicios del sistema y optimización Doze de batería. |
| 5 | Hardcodear Notification IDs o no poder cancelarlas al editar | Usar tags deterministas (`eval_{id}`, `class_{id}`) y IDs basados en `id.hashCode()` para permitir actualización y cancelación inmediata. |
| 6 | Solicitar permiso `POST_NOTIFICATIONS` o Bluetooth en frío | Mostrar diálogo `NotiFastDialog` con microcopia empática explicando el beneficio educativo antes de solicitar el permiso del sistema. |
| 7 | Crear una 5ta pestaña en la barra inferior para estadísticas | Integrar el dashboard analítico dentro de la pestaña `Calificaciones` o accesible desde el TopBar para mantener la navegación de 4 pestañas limpia. |
| 8 | Cálculos pesados o búsquedas bloqueantes en el hilo principal | Ejecutar toda la analítica y búsqueda en `Dispatchers.Default` con debounce de 300ms en `SearchViewModel`. |

---

## 🔄 5. Protocolo Obligatorio de Transición y Cierre de Fase 3

Al completar el 100% de las tareas de la Fase 3:
1. Ejecutar `./gradlew testDebugUnitTest` y asegurar 100% de tests unitarios pasando.
2. Ejecutar `./gradlew assembleDebug` y verificar que el build compila sin advertencias críticas.
3. Actualizar [`docs/prd/STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md) marcando la Fase 3 al 100% con la fecha y métricas de calidad.
4. Generar automáticamente [`docs/prd/KICKOFF_FASE_4.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_4.md) para la fase de Pulido, Onboarding, Exportación PDF, Backup y Accesibilidad (a11y).

