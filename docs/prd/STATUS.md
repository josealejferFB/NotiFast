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
Fase 2: Features Core        [░░░░░░░░░░░░░░░░]   0% (0/17 tareas)
Fase 3: Features Avanzadas   [░░░░░░░░░░░░░░░░]   0% (0/18 tareas)
Fase 4: Pulido & Experiencia [░░░░░░░░░░░░░░░░]   0% (0/12 tareas)
Fase 5: Despliegue & Tesis   [░░░░░░░░░░░░░░░░]   0% (0/16 tareas)
------------------------------------------------------------------
PROGRESO TOTAL ESTIMADO:     [██████░░░░░░░░░░]  33.7% (30/89 tareas)
```

---

## 🔍 Resumen de Logros — Fase 1: Fundamentos de Arquitectura (100% Completada)

Durante la Fase 1 se consolidaron los pilares arquitectónicos, de diseño y testing con máxima disciplina de ingeniería:
- **Capa de Dominio Compuesta:** Creación de modelos de datos compuestos (`SubjectDetailData`, `NoteWithSubject`) y Use Cases puros (`DeleteSubjectCascadeUseCase`, `GetSubjectDetailUseCase`, `GetNotesWithSubjectInfoUseCase`).
- **Sistema de Diseño Impeccable & Dark Mode Real:** Rediseño integral de tokens semánticos de color (`Ruby` `#BF092F`, `Aqua` `#3B9797`, superficies oscuras de alto contraste `#10161A` / `#1B242A`), tipografía escalada y utilidades puras de contraste WCAG y conversión HSL (`ThemeUtils.getAccessibleColor`).
- **Persistencia de Preferencias del Estudiante:** Integración de `androidx.datastore:datastore-preferences`, creación de `UserPrefsRepository` (DataStore Flow con Modo Oscuro, Modo Offline y Autenticación Biométrica) con inyección Hilt.
- **Modularización Estricta de Pantallas (≤ 250 LOC):** Descomposición de pantallas monolíticas (`SubjectCard`, `AddEvaluationScreen`, `EditEvaluationScreen`, `CalendarScreen`) en 11 subcomponentes desacoplados en `components/`.
- **Estandarización de Firmas Reactivas:** Normalización estricta de repositorios (`Flow<T>` reactivo sin `suspend`, operaciones de escritura `suspend fun ...: Result<T>`).
- **Infraestructura y Arnés de Testing:** Creación de `MainDispatcherRule` y `TestDataFactory` para testing asíncrono con corrutinas y StateFlows.
- **Suite de Pruebas Unitarias Exhaustiva:** Cobertura de tests unitarios para Use Cases (`CalculateWeightedAverageUseCaseTest`, `DeleteSubjectCascadeUseCaseTest`, `GetSubjectDetailUseCaseTest`, `GetNotesWithSubjectInfoUseCaseTest`), Repositorio de Preferencias (`UserPrefsRepositoryTest`), Utilitarios de Tema (`ThemeUtilsTest`), y ViewModels core (`LoginViewModelTest`, `NotesViewModelTest`, `CalendarViewModelTest`, `SubjectDetailViewModelTest`, `GradesViewModelTest`).
- **Métricas de Calidad:** **68 unit tests pasando** (0 fallos, 0 errores), build `assembleDebug` 100% exitoso.

---

## 📌 Detalle por Fases y Sprints

### 🟢 Fase 1: Fundamentos de Arquitectura (100% — Completada)
*Meta: Consolidar Clean Architecture con Use Cases, corregir el sistema de temas y armar el arnés de pruebas.*  
*Guía de Ejecución:* [`docs/prd/KICKOFF_FASE_1.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_1.md)

#### Sprint 1.1 — Arquitectura Base y Refactors Estructurales (Completado)
- [x] **1.1.1** Capa de Use Cases en `domain/usecase/` para operaciones compuestas (`DeleteSubjectCascadeUseCase`, `GetSubjectDetailUseCase`, `GetNotesWithSubjectInfoUseCase`) *(Completado)*
- [x] **1.1.2** Refactorizar Material 3 Theme con paleta corporativa `Ruby` & `Aqua` + Dark Mode real y contraste WCAG *(Completado)*
- [x] **1.1.3** Actualizar Version Catalog con `androidx.datastore.preferences` *(Completado)*
- [x] **1.1.4** Modularizar y dividir pantallas monolíticas (`SubjectCard`, `AddEvaluation`, `EditEvaluation`, `CalendarScreen` a ≤ 250 LOC) *(Completado)*
- [x] **1.1.5** Crear `UserPrefsRepository` con DataStore Preferences para preferencias del estudiante *(Completado)*
- [x] **1.1.6** Limpieza y estandarización de firmas reactivas (`Flow` vs `suspend fun ...: Result<T>`) *(Completado)*

#### Sprint 1.2 — Infraestructura y Suite de Tests Unitarios (Completado)
- [x] **1.2.1** Configurar infraestructura de tests (`MainDispatcherRule`, `TestDataFactory`, MockK, Turbine) *(Completado)*
- [x] **1.2.2** Tests unitarios exhaustivos para todos los Use Cases de dominio *(Completado)*
- [x] **1.2.3** Tests unitarios para ViewModels core (`LoginViewModel`, `NotesViewModel`, `CalendarViewModel`, `SubjectDetailViewModel`, `GradesViewModel`) *(Completado)*
- [x] **1.2.4** Verificación integral de build (`assembleDebug`) y cierre de Fase 1 *(Completado)*

---

### ⚪ Fase 2: Experiencia Core — Cuaderno y Hub Académico (Siguiente Fase)
*Meta: CRUD completo de horarios con detección de solapes, editor de notas enriquecido (Markdown/formato), almacenamiento cloud de imágenes y personalización académica.*  
*Guía de Arranque:* [`docs/prd/KICKOFF_FASE_2.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_2.md)

#### Sprint 2.1 — Gestión Completa de Horarios Académicos
- [ ] **2.1.1** Caso de uso para detección y validación de solapamientos horarios (`ValidateScheduleOverlapUseCase`)
- [ ] **2.1.2** Pantalla `AddScheduleScreen` con selector de días de la semana, picker de horario de inicio/fin y asignación de aula
- [ ] **2.1.3** Pantalla `EditScheduleScreen` con carga reactiva y edición segura
- [ ] **2.1.4** Integración de visualización por bloques temporales en `SchedulesScreen`
- [ ] **2.1.5** Unit tests para validación de horarios y ViewModel de horarios

#### Sprint 2.2 — Cuaderno Inteligente y Editor de Apuntes
- [ ] **2.2.1** Soporte de Markdown / texto enriquecido (negritas, listas, encabezados) en notas
- [ ] **2.2.2** Previsualización en tiempo real del apunte formateado
- [ ] **2.2.3** Selector dinámico de etiquetas / tags temáticos por apunte
- [ ] **2.2.4** Búsqueda y filtrado avanzado de apuntes por contenido y tags
- [ ] **2.2.5** Exportación básica de apuntes en formato texto/Markdown

#### Sprint 2.3 — Hub de Calificaciones y Personalización
- [ ] **2.3.1** Configuración de escala de calificaciones (0-10, 0-20, 0-100, Letras A-F)
- [ ] **2.3.2** Indicador de notas mínimas aprobatorias configurables en `GradesScreen`
- [ ] **2.3.3** Simulación de nota requerida en evaluaciones pendientes para aprobar la materia (`CalculateRequiredGradeUseCase`)

---

### ⚪ Fase 3: Features Avanzadas (Pendiente)
*Meta: Notificaciones inteligentes vía WorkManager, sincronización offline local y analíticas de estudio.*

---

### ⚪ Fase 4: Pulido & Experiencia (Pendiente)
*Meta: Micro-interacciones con Impeccable, accesibilidad completa (a11y), y optimización de rendimiento Compose.*

---

### ⚪ Fase 5: Despliegue & Tesis (Pendiente)
*Meta: Testing en aula real, recolección de métricas de tesis y publicación en Google Play Store.*
