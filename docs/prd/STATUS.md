# 📊 Estado de Ejecución y Roadmap — NotiFast v2.0

> **Documento de Seguimiento de Fases del PRD Maestro**  
> **Última actualización:** 21 de Agosto, 2026  
> **Referencia:** [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)

---

## 📈 Resumen Global de Progreso

```
Fase 0: Estabilización       [██████░░░░░░░░░░] 38% (6/16 tareas)
Fase 1: Fundamentos          [░░░░░░░░░░░░░░░░]  0% (0/10 tareas)
Fase 2: Features Core        [░░░░░░░░░░░░░░░░]  0% (0/17 tareas)
Fase 3: Features Avanzadas   [░░░░░░░░░░░░░░░░]  0% (0/18 tareas)
Fase 4: Pulido & Experiencia [░░░░░░░░░░░░░░░░]  0% (0/12 tareas)
Fase 5: Despliegue & Tesis   [░░░░░░░░░░░░░░░░]  0% (0/16 tareas)
------------------------------------------------------------------
PROGRESO TOTAL ESTIMADO:     [█░░░░░░░░░░░░░░░]  6.7% (6/89 tareas)
```

---

## 🔍 Hallazgos Clave de la Verificación Inicial (CodeGraph)

Durante la inspección de código fuente y git history (`fix/audit-phase1`), se determinó que **6 de las 16 tareas críticas de la Fase 0 ya habían sido subsanadas**:
- `WEB_CLIENT_ID` ya no está hardcodeado (usa `BuildConfig.WEB_CLIENT_ID`).
- Side-effect en `LoginScreen` ya está controlado con `LaunchedEffect(uiState.isLoggedIn)`.
- `EditEvaluationViewModel` ya no escucha infinitamente (`.first()` implementado).
- Borrado de materias ya implementa `WriteBatch` atómico.
- Existe `getNoteById()` en repositorio evitando carga global de notas en edición.
- Cálculo de promedio ponderado unificado mediante `CalculateWeightedAverageUseCase` (corrigiendo el bug del promedio global).

---

## 📌 Detalle por Fases y Sprints

### 🟢 Fase 0: Estabilización (En Curso)
*Meta: Resolver deuda técnica crítica y bugs residuales para dejar base 100% estable.*

#### Sprint 0.1 — Bugs Críticos
- [x] **0.1.1** Mover `WEB_CLIENT_ID` a `BuildConfig` *(Completado)*
- [ ] **0.1.2** Habilitar R8 + minificación y ProGuard rules en build release *(Pendiente)*
- [x] **0.1.3** Fix: Cálculo de promedio global ponderado *(Completado via UseCase)*
- [x] **0.1.4** Fix: Side-effect `onLoginSuccess()` con `LaunchedEffect` *(Completado)*
- [ ] **0.1.5** Fix: Prevenir posible doble navegación en flujo de Auth/NavGraph *(Pendiente de verificación)*
- [x] **0.1.6** Fix: `EditEvaluation` sobreescritura reactiva con `.first()` *(Completado)*
- [ ] **0.1.7** Fix: Confirmación con diálogo antes de eliminar nota en `NoteEstheticCard` *(Pendiente)*
- [ ] **0.1.8** Fix: Manejo y reporte visual de excepciones en repositorios y ViewModels *(Pendiente)*

#### Sprint 0.2 — Deuda Técnica Esencial
- [x] **0.2.1** Eliminación atómica de materias con `WriteBatch` *(Completado)*
- [ ] **0.2.2** Optimizar `getNotes()` para eliminar listeners N+1 en subcolecciones *(Pendiente)*
- [x] **0.2.3** Carga unitaria `getNoteById()` en edición de notas *(Completado)*
- [x] **0.2.4** Extracción de lógica de promedio a `CalculateWeightedAverageUseCase` *(Completado)*
- [ ] **0.2.5** Unificar convención de `weight` (factor 0.0-1.0 vs porcentaje en UI) *(Pendiente)*
- [ ] **0.2.6** Centralizar parseo seguro de colores Hex a `Color` en `ThemeUtils` *(Pendiente)*
- [ ] **0.2.7** Integrar SDK y configuración de Firebase Crashlytics *(Pendiente)*
- [ ] **0.2.8** Integrar SDK y eventos analíticos de Firebase Analytics *(Pendiente)*

---

### ⚪ Fase 1: Fundamentos de Arquitectura (Pendiente)
*Meta: Consolidar Clean Architecture con Use Cases, corregir el sistema de temas y armar el arnés de pruebas.*

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
