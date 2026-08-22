# 📊 Estado de Ejecución y Roadmap — NotiFast v2.0

> **Documento de Seguimiento de Fases del PRD Maestro**  
> **Última actualización:** 21 de Agosto, 2026  
> **Referencia:** [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)

---

## 📈 Resumen Global de Progreso

```
Fase 0: Estabilización       [████████████████] 100% (16/16 tareas) ✅
Fase 0.1: Hardening UX       [████████████████] 100% (4/4 bloques) ✅
Fase 1: Fundamentos          [░░░░░░░░░░░░░░░░]   0% (0/10 tareas)
Fase 2: Features Core        [░░░░░░░░░░░░░░░░]   0% (0/17 tareas)
Fase 3: Features Avanzadas   [░░░░░░░░░░░░░░░░]   0% (0/18 tareas)
Fase 4: Pulido & Experiencia [░░░░░░░░░░░░░░░░]   0% (0/12 tareas)
Fase 5: Despliegue & Tesis   [░░░░░░░░░░░░░░░░]   0% (0/16 tareas)
------------------------------------------------------------------
PROGRESO TOTAL ESTIMADO:     [███░░░░░░░░░░░░░]  20.0% (20/89 tareas)
```

---

## 🔍 Resumen de Logros y Estabilización (Fase 0 & Fase 0.1 Completadas)

Durante la Fase 0 y Fase 0.1 se blindó la arquitectura y la experiencia de usuario con máxima rigurosidad:
- **Seguridad y Build:** `WEB_CLIENT_ID` desacoplado a `BuildConfig`, R8 y ProGuard rules optimizadas para Firebase, Crashlytics, Analytics, Hilt y Coroutines.
- **Navegación e Higiene UI:** Flujo atómico de navegación post-login en `NavGraph`, eliminación de strings quemados y uso de `stringResource`.
- **TopBar Limpia:** Reemplazo de flechas de retroceso huérfanas en pestañas principales (`Calendar`, `Schedules`, `Grades`, `Settings`) usando `TopBarType.Simple`.
- **Acciones Destructivas & Descarte:** Protección con diálogos `NotiFastDialog` al cerrar sesión (`SettingsScreen`, `EditProfileScreen`), eliminar horarios (`SchedulesScreen`), eliminar evaluaciones (`SubjectDetailScreen`), y descarte de cambios accidentales mediante `BackHandler` en todas las pantallas de edición/creación (`AddNote`, `EditNote`, `AddEvaluation`, `EditEvaluation`, `AddSubject`, `AddSchedule`, `EditSchedule`).
- **Validaciones & Resiliencia:** Trimming de whitespace, detección case-insensitive de materias duplicadas (`AddSubjectViewModel`), validación estricta de rangos de peso (`0..100%`) y notas (`0..10`), y filtrado resiliente de notas por título, contenido y materia.
- **Componentes y Diseño Atómico:** `NotesScreen` modularizada (≤ 200 LOC), `NotiFastDialog` adaptado dinámicamente a Modo Oscuro (`surface`).
- **Capa de Datos y Errores:** Manejo de excepciones estandarizado con logging en `NoteRepositoryImpl`, `EvaluationRepositoryImpl`, `ScheduleRepositoryImpl` retornando `Result<T>`.
- **Extensiones de Color y Dominio:** Parseo de color seguro `String?.toSafeComposeColor()` centralizado en `Color.kt` y `ThemeUtils.kt`.
- **Observabilidad:** Integración de Firebase Crashlytics y Firebase Analytics con abstracción `AnalyticsTracker`.
- **Verificación Continua:** 37 unit tests pasando y `assembleDebug` compilando con cero fallos.

---

## 📌 Detalle por Fases y Sprints

### 🟢 Fase 0.1: Hardening UX & Sanity Checks (100% — Completada)
*Meta: Blindar feedback destructivo, navegación fluida, prevención de pérdida de datos y validaciones de entrada.*

- [x] **0.1.A** Protección contra acciones destructivas y cierre de sesión con `NotiFastDialog` *(Completado)*
- [x] **0.1.B** Intercepción de retroceso con `BackHandler` y confirmación de descarte de cambios *(Completado)*
- [x] **0.1.C** Sanitización de inputs, trimming, validación de materias duplicadas y límites de notas/pesos *(Completado)*
- [x] **0.1.D** Suite de tests unitarios (37 tests) y verificación en dispositivo físico *(Completado)*

---

### 🟢 Fase 0: Estabilización (100% — Completada)
*Meta: Resolver deuda técnica crítica y bugs residuales para dejar base 100% estable.*

#### Sprint 0.1 — Bugs Críticos (Completado)
- [x] **0.1.1** Mover `WEB_CLIENT_ID` a `BuildConfig` *(Completado)*
- [x] **0.1.2** Habilitar R8 + minificación y ProGuard rules en build release *(Completado)*
- [x] **0.1.3** Fix: Cálculo de promedio global ponderado *(Completado via UseCase)*
- [x] **0.1.4** Fix: Side-effect `onLoginSuccess()` con `LaunchedEffect` *(Completado)*
- [x] **0.1.5** Fix: Prevenir posible doble navegación en flujo de Auth/NavGraph *(Completado)*
- [x] **0.1.6** Fix: `EditEvaluation` sobreescritura reactiva con `.first()` *(Completado)*
- [x] **0.1.7** Fix: Confirmación con diálogo `NotiFastDialog` antes de eliminar nota *(Completado)*
- [x] **0.1.8** Fix: Manejo y reporte visual de excepciones en repositorios y ViewModels *(Completado)*

#### Sprint 0.2 — Deuda Técnica Esencial (Completado)
- [x] **0.2.1** Eliminación atómica de materias con `WriteBatch` *(Completado)*
- [x] **0.2.2** Optimizar `getNotes()` para eliminar listeners N+1 en subcolecciones *(Completado)*
- [x] **0.2.3** Carga unitaria `getNoteById()` en edición de notas *(Completado)*
- [x] **0.2.4** Extracción de lógica de promedio a `CalculateWeightedAverageUseCase` *(Completado)*
- [x] **0.2.5** Unificar convención de `weight` (factor 0.0-1.0 vs porcentaje en UI) *(Completado)*
- [x] **0.2.6** Centralizar parseo seguro de colores Hex a `Color` en `ThemeUtils` y `Color.kt` *(Completado)*
- [x] **0.2.7** Integrar SDK y configuración de Firebase Crashlytics *(Completado)*
- [x] **0.2.8** Integrar SDK y eventos analíticos de Firebase Analytics *(Completado)*

---

### ⚪ Fase 1: Fundamentos de Arquitectura (Siguiente Fase)
*Meta: Consolidar Clean Architecture con Use Cases, corregir el sistema de temas y armar el arnés de pruebas.*  
*Guía de Arranque:* [`docs/prd/KICKOFF_FASE_1.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_1.md)

- [ ] **1.1.1** Capa de Use Cases en `domain/usecase/` para operaciones compuestas
- [ ] **1.1.2** Refactorizar Material 3 Theme con paleta corporativa `Ruby` & `Aqua` + Dark Mode real
- [ ] **1.1.3** Actualizar Compose BOM y version catalog a versiones estables
- [ ] **1.1.4** Modularizar y dividir pantallas monolíticas (>300 LOC)
- [ ] **1.1.5** Crear `UserPrefsRepository` para configuraciones del estudiante
- [ ] **1.1.6** Limpieza de firmas de métodos reactivos (`Flow` vs `suspend`)
- [ ] **1.2.1** Configurar infraestructura de tests (MockK, Turbine, Coroutines Test)
- [ ] **1.2.2** Tests unitarios para todos los Use Cases
- [ ] **1.2.3** Tests unitarios para ViewModels core (`Notes`, `Grades`, `Calendar`, `Auth`)
- [ ] **1.2.4** Tests de integración con emuladores de repositorios

---

### ⚪ Fase 2: Features Core Completas (Pendiente)
*Meta: CRUD completo de horarios, editor de notas enriquecido, almacenamiento cloud de imágenes y notas configurables.*

- [ ] **2.1.1 - 2.1.5** CRUD completo de Horarios (`AddScheduleScreen`, `EditScheduleScreen`, eliminación y validación de solapes)
- [ ] **2.2.1 - 2.2.6** Editor de notas con texto enriquecido (formato, listas, headings, renderizado seguro)
- [ ] **2.3.1 - 2.3.3** Sistema de escalas de calificación configurable por usuario
- [ ] **2.3.4 - 2.3.6** Migración de `ImageRepositoryImpl` a Firebase Storage (sincronización multi-dispositivo)

---

### ⚪ Fase 3: Features Avanzadas (Pendiente)
*Meta: Notificaciones inteligentes locales, buscador global, dashboard estadístico y compartir apuntes.*

- [ ] **3.1.1 - 3.1.6** Notificaciones inteligentes locales con `WorkManager` (recordatorios, resumen semanal)
- [ ] **3.2.1 - 3.2.4** Búsqueda global multi-módulo con debounce y resaltado
- [ ] **3.3.1 - 3.3.4** Dashboard de estadísticas de rendimiento con gráficos dinámicos
- [ ] **3.4.1 - 3.4.6** Compartir apuntes mediante enlaces protegidos de solo lectura (`shared_notes`)

---

### ⚪ Fase 4: Pulido y Experiencia (Pendiente)
*Meta: Onboarding para nuevos usuarios, exportación a PDF, backup/restauración de datos y accesibilidad total.*

- [ ] **4.1.1 - 4.1.2** Pantallas de Onboarding interactivo
- [ ] **4.1.3 - 4.1.4** Generación y exportación de Plan de Estudio en formato PDF
- [ ] **4.1.5** Extracción total de strings a `strings.xml`
- [ ] **4.2.1 - 4.2.3** Backup y restauración de datos del estudiante en JSON
- [ ] **4.2.4 - 4.2.5** Auditoría de accesibilidad (TalkBack, contrastes WCAG AA, target touch ≥ 48dp)
- [ ] **4.2.6 - 4.2.7** Previews completas para Android Studio y pulido UX Impeccable

---

### ⚪ Fase 5: Producción, Testing en Aula y Despliegue (Pendiente)
*Meta: Validación empírica para la tesis y publicación en Google Play Store.*

- [ ] **5.1.1 - 5.1.5** Configuración de Internal Testing Track y diseño de instrumentos (SUS / Likert)
- [ ] **5.2.1 - 5.2.6** Despliegue de beta cerrada en aula y recolección de métricas
- [ ] **5.3.1 - 5.3.4** Triage y resolución de incidencias post-testing
- [ ] **5.4.1 - 5.4.9** Preparación de assets de Play Store, políticas de privacidad, firmado AAB y release final
