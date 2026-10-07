# 📊 Estado de Ejecución y Roadmap — NotiFast v3.0

> **Documento de Seguimiento de Fases del PRD Maestro y Sistema de Diseño 3.0**  
> **Última actualización:** 6 de Octubre, 2026  
> **Referencia:** [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md) | [DESIGN.md](file:///home/p0xd/Documentos/NotiFast/DESIGN.md) | [PRODUCT.md](file:///home/p0xd/Documentos/NotiFast/PRODUCT.md)

---

## 📈 Resumen Global de Progreso

```
Fase 0: Estabilización            [████████████████] 100% (16/16 tareas) ✅
Fase 0.1: Hardening UX            [████████████████] 100% (4/4 bloques) ✅
Fase 1: Fundamentos               [████████████████] 100% (10/10 tareas) ✅
Fase 2: Experiencia Core          [████████████████] 100% (13/13 tareas) ✅
Fase 2.1: Ergonomía Forms         [████████████████] 100% (9/9 tareas) ✅
Sprint Design 3.0: Tactile Studio [████████████████] 100% (3/3 Sprints) ✅
Fase 3: Features Avanzadas        [████████████████] 100% (18/18 tareas) ✅
Fase 3.5: Offline-First Local     [████████████████] 100% (9/9 tareas) ✅
Fase 3.6: Sanitización Lógica     [████████████████] 100% (10/10 tareas) ✅
Editor Premium (OneNote-like)     [████████████░░░░]  75% (9/12 tareas) 🔄
Fase 4: Pulido & Experiencia      [░░░░░░░░░░░░░░░░]   0% (0/12 tareas)
Fase 5: Despliegue & Tesis        [░░░░░░░░░░░░░░░░]   0% (0/16 tareas)
------------------------------------------------------------------
PROGRESO TOTAL ESTIMADO:          [█████████████░░░]  82% (103/119 tareas)
```

---

## 📝 Editor Premium de Apuntes (OneNote-like) — En Progreso

**Objetivo:** Transformar el editor de markdown tipo IDE a una experiencia de cuaderno premium: bloques reordenable, texto rico sin sintaxis visible, multi-foto inline, anotación/dibujo sobre fotos, checklists y fórmulas.

### Fase 1: Motor de bloques + editor rico (100% ✅)
- `NoteBlock` sealed class (Heading, Paragraph, BulletList, Checklist, Quote, Image, MathBlock, Divider) + `NoteBlockCodec` JSON.
- `MarkdownToBlocksParser` para migración de contenido legacy.
- Room migration 2→3 (`blocksJson`, `schemaVersion`).
- `NoteEditorViewModel` unificado con operaciones de bloques, auto-save, checklist toggles.
- UI: `NoteEditorScreen` + `BlockListEditor` + `BlockRenderers` + `BlockInsertMenu`.
- Navegación unificada (`note_editor`), `SubjectDetail.onNoteClick` cableado.
- Visor `NoteDetailExpandable` renderiza bloques con fallback markdown.

### Fase 2: Multi-foto + cámara (100% ✅)
- `ImageBlockPickerSheet` con galería (`GetContent`) + cámara (`TakePicture` + FileProvider).
- Bloques Image inline en el flujo del documento.

### Fase 3: Anotación/dibujo sobre fotos (75% 🔄)
- `PhotoAnnotation` (Stroke, AnnotationText, Arrow) + `PhotoAnnotationCodec`.
- `DrawingCanvasState` (undo/redo/eraser) + `DrawingCanvas` (pointerInput + Path).
- `PhotoAnnotatorScreen` con toolbar flotante (lápiz, resaltador, colores, grosores).
- Pendiente: flatten de anotación + persistencia en bloque Image.

### Fase 4: Checklists + fórmulas + export PDF (0%)
- Checklists interactivos (VM ya soporta, falta pulir UI).
- MathRenderer nativo + SymbolKeyboard.
- Export PDF.
- Búsqueda global indexa bloques.

### Tests añadidos
- `NoteBlockCodecTest` (12), `MarkdownToBlocksParserTest` (16), `NoteBlocksRepositoryTest` (4),
  `NoteEditorViewModelTest` (13), `NoteEditorImageTest` (3), `PhotoAnnotationCodecTest` (8),
  `DrawingCanvasStateTest` (10). Suite completa: 260+ tests en verde.

### Auditoría sistemática (Systematic Debugging) — 2026-10-06
**7 CRITICAL + 9 HIGH + 8 MEDIUM + 4 LOW encontrados y parchados:**

**CRITICAL (pérdida datos / crash) — TODOS parchados:**
1. Cancel≡Save en PhotoAnnotator → estado `isCancelled` separado
2. VM del anotador compartido sin reset → `init()` resetea estado
3. Navegación durante composición → `LaunchedEffect`
4. Race auto-save vs SaveClicked (bug #9 reintroducido) → `persistJob` compartido
5. `NoteBlockCodec.fromJson` crash con JSON corrupto → `try/catch` + fallback
6. Editar nota legacy borraba `imageUrl` del disco → preservar campos existentes
7. URIs `content://` de galería no persistidas → `ImageRepository.uploadNoteImage`

**HIGH (feature rota / UX) — TODOS parchados:**
8. ImageBlock no clicable → flujo de anotación inalcanzable → `Modifier.clickable`
9. Sin UI para borrar bloques → botón Close por bloque
10. Checklist items no editables → `BlockTextField` + placeholder
11. Fórmulas no se renderizaban en editor → `MathRenderer` integrado
12. Anotaciones nunca se pintaban → `AnnotatedImage` con overlay de strokes
13. Preselección de materia rota → `savedStateHandle["subject"]`
14. `hasUnsavedChanges` ignoraba fotos/fórmulas → include Image/MathBlock
15. `changeBlockType` destruía items → joinToString
16. Trazos de 1 punto invisibles → filtro `size >= 2`

**Higiene:** strings a `strings.xml`, highlighter alpha fix, log en codec, UUID para bloques sin id, `clear()` limpia undo stack.

---

## 🧼 Fase 3.6: Sanitización de Lógica de Negocio (100% — Oct 2026)

Auditoría transversal con systematic debugging y journeys de usuario. Suite 260/260 tests unitarios, `assembleDebug` OK.

### Decisiones de negocio cerradas
- **D1–D2:** `GradePolicy` única fórmula de notas/pesos. Cuenta solo `isPresented && grade != null`. Presentada sin nota no baja el promedio.
- **D3:** Pesos % de 100; `ValidateEvaluationWeightsUseCase` bloquea Σ > 100%.
- **D4:** Umbrales = `GradeScale` + `customMinPassingGrade` del usuario (antes el setting estaba muerto).
- **D5:** `Evaluation.gradeScaleId` — cambiar la escala default no reinterpreta notas guardadas.
- **D6:** `remainingWeight` = peso real de pendientes (eliminado el fantasma `100 − evaluated`).
- **D7:** `subjectId` UUID estable; rename no rompe joins.
- **D8:** `SubjectData.cycle` (ciclo académico) en el modelo.
- **D9:** Alertas se cancelan al tener nota **o** marcar presentada.
- **D10:** Clases virtuales tienen día/hora y alerta (antes forzaban domingo sin aviso).

### Bugs críticos cerrados
1. 3 fórmulas de promedio incoherentes entre pestañas → una sola (`GradePolicy`).
2. Alertas programadas con `id=""` (UUID se generaba dentro del repo y no volvía) → `Result<String>`.
3. Logout creaba un usuario nuevo → datos huérfanos. `createLocalProfile` reutiliza el perfil.
4. Grade entry bloqueada a 0–10 con escala 0-20/0-100.
5. Cascade delete case-sensitive dejaba notas/evals huérfanas → por `subjectId`.
6. Import OVERWRITE hacía merge con duplicados + fechas con `Z` literal (timezone shift).
7. `isLoading` stuck con `userId` nulo / eval no encontrada.
8. Double-collector en `LoadNotes`/`LoadData`.
9. Raza auto-save vs SaveClicked en EditNote (navegación perdida).
10. Clase inminente de hoy se saltaba a la semana siguiente (`minutesInAdvance`).
11. `ClassScheduleWorker` notificaba sin userId y mataba la cadena semanal.
12. `nextActivity` mostraba clases ya terminadas.
13. Room usaba `fallbackToDestructiveMigration` → migración real 1→2.

### Código muerto eliminado (~1.5k LOC)
- 3 repos Firestore sin bindear + `FirebaseAuthDataSource`.
- `DeleteSubjectCascadeUseCase`, `GetSubjectDetailUseCase`, `GetNotesWithSubjectInfoUseCase`.
- Login (email/Google) — inalcanzable y siempre fallaba en offline-first.
- 8 componentes notes muertos, 2 átomos sin uso, `OfflineArchitectureBoundaryTest` stale.

### Nota para usuarios
Los promedios pueden **cambiar de valor** respecto a versiones anteriores: antes cada pestaña usaba una fórmula distinta. Ahora el número es único y coherente en toda la app.

### Cierre de verificación (smoke en dispositivo SM_A065M)
- Migración Room 1→2 validada (`user_version=2`, índices exactos).
- Bottom nav restaurada y con transparencia sobre el fondo de estudio.
- `minPassingGrade` ya no aplica umbral 0-20 a escalas 0-10 (todas las materias parecían “meta inalcanzable”).
- Datos mock cargados: 5 materias, 8 apuntes, 15 evals (incl. escala 0-20), 7 horarios (1 virtual).
- Flujos verificados en UI: Home (métricas/hero/tags), Agenda (live status, evaluaciones, ventana libre, virtual), Calificaciones (promedios + detalle Evaluada/Pendiente), Analíticas (alertas y recomendaciones con matemática correcta: *“promediar 6.3 en el 60% restante”*).
- T9: `ExportNoteMarkdownUseCase`, strings de grade dialog / search / eval fields localizados.

### Pendiente conocido (no bloqueante)
- Strings de fallback en ViewModels (solo visibles si `e.message` viene nulo desde repo).
- Smoke de import P2P / logout→continuar no automatizado.

---

## Sistema de Calificaciones Multi-escala (100% — Oct 2026)

### Decisiones nuevas
- **D5 (rev):** Cambiar la escala en Ajustes **reescribe** notas (`grade`, `gradeScaleId`) y nota mínima custom vía `ConvertGradeScaleUseCase` + batch Room `@Transaction`, previa confirmación.
- **D11/D11b:** Captura siempre en escala activa (stamp `gradeScaleId`); sin selector de escala en evaluaciones; hint «Puedes cambiarla en Ajustes».
- **D12:** Escalas 0-10, 0-20, 0-100, A-F.
- **D13:** Redondeo destino (0-100 entero, resto 1 decimal; A-F GPA 1 decimal).
- **D14:** Display unificado en `GradeFormatters`.
- **D15:** Conversión por-evaluación desde su propio `gradeScaleId` (notas mixtas).
- **D16:** `minPassingGrade == 0f` = default de la escala.
- **D17:** Cambio de escala atómico (`ChangeGradeScale`); Settings es el único entry point.

### Bugs cerrados (B1–B18)
Integridad gradeScaleId (entity/ctor/stamp), formatos hardcodeados (/20, %.1f, >=3.0), NotesViewModel escala fija, minPassing custom no propagado, globalAverage = promedio de promedios, import P2P sin validar rango, A-F incompleto, dialog 0f, etc.

### Verificación
- Suite unitaria verde + `assembleDebug` OK.
- Spec: `docs/superpowers/specs/2026-10-06-grade-scale-multi-base-design.md`
- Plan: `docs/superpowers/plans/2026-10-06-grade-scale-multi-base.md`

---

## 🎨 Resumen de Logros — NotiFast 3.0: Tactile Papercraft / Studio Notebook (100% Completado)

Durante esta iteración mayor de UI/UX y Sistema de Diseño se transformó integralmente la experiencia visual y táctil de la aplicación basándose en las especificaciones de `DESIGN.md` y `PRODUCT.md`:

### 1. Sprint 1: Fundamentos del Sistema de Diseño (Tokens, Tipografía y Colorimetría)
- **Paleta Semántica Calibrada WCAG AAA / AA:**
  - Lienzo pergamino cálido (`WarmPaperCanvas` `#FBF9F5` / `SlateDesk` `#0F141C`).
  - Acentos de estudio: `StudioIndigo` (`#4338CA` / `#6366F1`), `TerracottaWarm` (`#B45309` / `#F59E0B`), `SageForest` (`#15803D` / `#22C55E`), `CrimsonBrick` (`#B91C1C` / `#EF4444`).
  - Mapeo exhaustivo en `LightColorScheme` y `DarkColorScheme` de Material 3 con `surfaceContainer`, `surfaceContainerHigh`, `outlineVariant`, `tertiary` y `secondary`.
- **Doble Familia Tipográfica Oficial:**
  - `PlusJakartaSansFamily` para toda la jerarquía editorial de títulos y cuerpos.
  - `JetBrainsMonoFamily` para cifras académicas, promedios, porcentajes y bloques de código (`Typography.monoAcademic`).
  - Integración nativa con `androidx.compose.ui.text.googlefonts` y certificados `font_certs.xml`.
- **Motor HSL de Materias con Contraste Garantizado:**
  - Calibración en `ThemeUtils.kt` asegurando luminosidad $L \in [0.30, 0.42]$ en Light y $L \in [0.68, 0.80]$ en Dark, con tests unitarios completos en `ThemeUtilsTest.kt`.

### 2. Sprint 2: Componentes Atómicos Táctiles (`presentation/atoms/`)
- **Física Táctil y Micro-Bordes:**
  - `NotiFastCard`: Micro-borde 1dp `outlineVariant`, 0dp elevación difusa, animación `spring` `0.97f` al tacto.
  - `NotiFastButton`: Altura 48dp, esquinas 14dp, HapticFeedback `LongPress`, loader embebido sin saltos.
  - `NotiFastTextField`: Bloque pergamino 56dp con `surfaceContainer`, borde táctil dinámico 1dp/2dp e indicador de error.
  - `NotiFastEmptyState`: Contenedor 72dp con icono 36dp y microcopia empática.
  - `NotiFastCardSkeleton` / `NotiFastListSkeleton`: Shimmer táctil animado de 1200ms con gradiente lineal entre `surfaceContainer` y `surfaceContainerHigh`.
  - `NotiFastTopBar`: Header AAA editorial modularizado con `HeaderAvatar` (resortes táctiles `0.97f` y anillo dinámico de sincronización), `HeaderContextPill` (cápsula de fecha/semestre con micro-borde), y `HeaderActionButtons` (campana con badge ámbar y sync indicator).
  - `NotiFastBottomNav`: Cápsula flotante pill `surfaceContainer` sin sombras sucias con borde 1dp.
  - `NotiFastStudyBackground`: Gradiente de estudio pergamino con micro-iconos flotantes sutiles.
  - Erradicación total de colores hexadecimales quemados en todos los átomos (`FormField`, `DatePickerField`, `ImagePickerField`, `Chip`, `ExpandableFab`, `Dialog`).

### 3. Sprint 3: Refactorización Visual de Pantallas Core & Hub Académico 3.0
- **Hub de Apuntes y Materias Bento (`presentation/feature/notes/`):**
  - `NotesDashboardHero`: Hero Bento Dual con Próxima Actividad dinámica (clase de hoy u horario inminente vs evaluación más cercana) y 3 cápsulas métricas (Apuntes, Materias, Promedio Global en `JetBrains Mono`).
  - `NotesFilterBar`: Barra de búsqueda táctil con contenedor `surfaceContainer`, foco dinámico y carrusel de etiquetas `#` temáticas (`#todos`, `#resumen`, `#examen`, etc.).
  - `NoteEstheticCard`: Ficha táctil de estudio tipo ficha bibliográfica con lomo de materia accesible, preview de contenido, badges de tags, indicador de imágenes adjuntas y botón de exportación/copia a Markdown con feedback táctil.
  - `SubjectGridBento`: Cuadrícula modular Bento de materias (2 columnas) con monogramas dinámicos, barras de progreso y promedios en `JetBrains Mono`.

- **Cronograma Académico & Asistente de Clases en Tiempo Real (`presentation/feature/schedules/`):**
  - `ScheduleLiveStatusCard` & `LiveStatusBeaconContent`: Widget "Live Class Beacon" en tiempo real con indicador pulsante de clase en curso, cuenta regresiva en `JetBrains Mono` para la próxima clase, barra de progreso lineal de clase y acciones rápidas ("Tomar Apunte" y "Unirse a Clase" virtual).
  - `ScheduleDaySelector` & `ViewModeSegmentedToggle`: Selector de días táctil con píldoras de conteo y badge de "Hoy", junto con conmutador segmentado para alternar entre *Línea de Tiempo Diaria* y *Resumen Bento Semanal*.
  - `ScheduleGapCard`: Detección inteligente y fichas de "Ventana Libre de Estudio" ($\ge 45\text{ min}$) entre clases consecutivas.
  - `ScheduleTimelineView` & `ScheduleConflictBanner`: Riel vertical con horas monoespaciadas, tarjetas de clase y detección accesible de colisiones/solapamientos horarios.
  - `ScheduleCard`: Ficha de clase con lomo accesible, modalidad, salón, profesor y menú de acciones directas (apunte, enlace virtual, editar, borrar).
  - `ScheduleWeekBentoSummary` & `ScheduleDistributionBarChart`: Dashboard Bento semanal con métricas de carga horaria, gráfico de barras de distribución por días y desglose por materias.
  - `ScheduleCalculations`: Módulo puro en `domain/util/` para cálculo reactivo de gaps, estados en vivo, colisiones y resúmenes semanales.
  - Arquitectura modular SRP estricta ($\le 198$ LOC por archivo) con 100% de tests unitarios pasando.
  - `NotesScreen`: Orquestador desacoplado y ligero ($\le 175$ LOC) con tabs de estudio y modal expandible `NoteDetailExpandable`.
- **Horarios Bento (`presentation/feature/schedules/`):**
  - `ScheduleCard`: Tarjetas modulares Bento con rangos horarios en `JetBrains Mono` y chips de modalidad.
  - `ScheduleDayTabs`: Píldoras táctiles redondeadas de selección rápida de día.
  - `ScheduleConflictBanner`: Banner accesible con contenedor de error y micro-borde.
- **Calificaciones y Promedios (`presentation/feature/grades/`):**
  - Modularización limpia de `GradesScreen.kt` (de 406 LOC a < 100 LOC) extrayendo:
    * `MainAverageDashboard.kt`: Dashboard hero con promedio global gigante en `JetBrains Mono` y barra de progreso.
    * `SubjectGradeCard.kt`: Tarjeta por materia con notas ponderadas y diálogo táctil `GradeRevampDialog.kt`.
    * `EmptyGradesState.kt`: Estado vacío interactivo.

- **Compendio Integral de Asignatura & Centro de Mando 3.0 (`presentation/feature/subjectdetail/`):**
  - Reingeniería arquitectónica en 4 cuadrantes modulares bajo límite estricto SRP $\le 174$ LOC:
    * `SubjectDetailScreen.kt` (169 LOC): Orquestador ligero con Scaffold, Hero Bento y switcher animado.
    * `SubjectDetailViewModel.kt` (174 LOC): ViewModel MVI con StateFlow unificado, reactividad de horarios con `ScheduleRepository`, balance de ponderación del syllabus y filtros en tiempo real.
    * `SubjectDetailContract.kt` (66 LOC): UiState inmutable con 4 cuadrantes, hitos cronológicos y eventos MVI.
    * `SubjectDetailHeroBento.kt` (135 LOC): Hero header con monograma HSL accesible, métricas tabulares (`Typography.monoAcademicLarge`) y 3 acciones rápidas (`+ Apunte`, `+ Evaluación`, `+ Clase`).
    * `SubjectSegmentedTabs.kt` (75 LOC): Selector segmentado táctil con pastilla deslizante y haptic feedback.
    * `SubjectPanoramaTab.kt` (160 LOC): Cuadrante 1 con próximo hito (countdown), simulador de notas reactivo integrado con `CalculateRequiredGradeUseCase`, progreso ponderado y datos de cátedra.
    * `SubjectEvaluationsTab.kt` (136 LOC) & `SubjectEvaluationItemCard.kt` (132 LOC): Cuadrante 2 con totalizador de peso (calificado/asignado/disponible) y cards interactivas.
    * `SubjectNotesTab.kt` (115 LOC): Cuadrante 3 con búsqueda in-situ, chips de etiquetas temáticas (#resumen, #fórmulas) y mosaico de notas.
    * `SubjectSchedulesTab.kt` (153 LOC): Cuadrante 4 con bloques de clases semanales, salón, docente y botón "Unirse a Aula Virtual".
    * `SubjectGradeQuickDialog.kt` (143 LOC): Diálogo táctil moderno de asignación de nota en 1-tap con chips de escalas.
    * 100% de tests unitarios pasando en `SubjectDetailViewModelTest.kt` y build exitoso con `assembleDebug`.
- **Agenda Académica Unificada & Tri-Vista (`presentation/feature/schedules/`):**
  - **Fusión Integral de Evaluaciones y Horarios:** Eliminación de la redundancia del calendario independiente, concentrando en la pestaña central "Agenda" todo el seguimiento temporal del estudiante.
  - **Barra de Navegación Optimizada a 4 Pestañas:** `Apuntes`, `Agenda`, `Calificaciones` y `Ajustes`.
  - `AgendaViewModeToggle`: Conmutador segmentado ergonómico de 3 modos:
    * **Timeline Diario:** Clases del día, ventanas de estudio y sección de evaluaciones (`AgendaDayEvaluationsSection` + `AgendaEvaluationCard`).
    * **Calendario Mensual (`AgendaMonthCalendarView`):** Cuadrícula mensual de 30/31 días con indicadores cromáticos de materia y lista interactiva de evaluaciones por fecha.
    * **Semana Bento (`ScheduleWeekBentoSummary`):** Analítica visual de carga semanal y gráfico de distribución por días.
  - `AgendaCreateActionBottomSheet`: Modal táctil desplegable desde el FAB (+) para crear rápidamente un Horario o una Evaluación.
  - `AgendaEvaluationBottomSheet`: Ficha modal de detalle de evaluación con opciones de edición y eliminación.
  - `ScheduleDaySelector`: Selector semanal con indicadores duales (píldoras de clases + punto rojo de exámenes/evaluaciones).
- **Captura Rápida & Ergonomía de Entrada 3.0 (`presentation/feature/addnote/` & `addsubject/`):**
  - **Sheets Modales Táctiles de Captura Ultrarrápida:**
    * `AddNoteBottomSheet` (< 180 LOC) y `AddSubjectBottomSheet` (< 120 LOC) con 28dp esquinas, micro-bordes `outlineVariant`, soporte completo de insets de teclado `WindowInsets.ime` y `navigationBarsPadding`.
    * Integración directa desde `NotesScreen` al tocar el FAB (+) o los accesos de creación de notas/materias, sin saltos de pantalla completa.
  - **Editor Markdown con Toolbar de Formato:**
    * `NoteMarkdownToolbar`: Barra de herramientas táctil flotante con 9 accesos directos (`#`, `##`, `###`, `**`, `*`, `- `, `- [ ]`, `$`, `>`, `code`) con feedback háptico.
    * Conmutación fluida entre edición y previsualización en vivo (`NotePreviewCard`) con `AnimatedContent`.
  - **Selector Dinámico de Materias & Tags Inteligentes:**
    * `NoteSubjectSelectorChip`: Carrusel horizontal con colores dinámicos accesibles, monogramas e icono temático, más botón directo `➕ Crear Materia`.
    * `NoteTagsFlowRow`: Etiquetas académicas inteligentes sugeridas (`#resumen`, `#examen`, `#fórmulas`, `#tarea`, `#importante`, etc.) con activación a un solo toque y creador de tags personalizados.
    * `NoteAttachmentPreview`: Selector visual de fotografía de pizarra o referencia con previsualización en miniatura y botón de descarte.
  - **Estudio Creativo de Materias 3.0 (`addsubject`):**
    * `SubjectLivePreviewCard`: Previsualización interactiva en tiempo real del icono, color HSL accesible, nombre y estadísticas simuladas de la asignatura.
    * `SubjectColorPickerBento`: Bento grid de 10 colores académicos armónicos con validación de contraste WCAG AA/AAA.
    * `SubjectIconGridSelector`: Selector visual de 12 iconos temáticos académicos con resortes elásticos `spring(0.97f)`.
    * `SubjectEditorFormFields`: Formulario desacoplado con chips de sugerencias instantáneas (*Cálculo*, *Física*, *Programación*, *Literatura*, *Química*, etc.).
    * Detección y prevención automática de nombres duplicados en `AddSubjectViewModel`.
  - **Arquitectura & Cumplimiento:**
    * 100% de los archivos $\le 180$ LOC.
    * Cobertura completa de tests unitarios en `AddNoteViewModelTest` y `AddSubjectViewModelTest`.

### 4. Métricas de Calidad y Verificación
- **100% Unit Tests Pasando** (`BUILD SUCCESSFUL` en `./gradlew testDebugUnitTest`).
- **Build APK 100% Exitoso** (`BUILD SUCCESSFUL` en `./gradlew assembleDebug`).
- **Verificación en dispositivo físico** con capturas de pantalla de `Captura Rápida` y `Nueva Materia`.
- **100% de cumplimiento con directivas de Clean Architecture y límite $\le 200$ LOC por composable.**

---

## 📌 Detalle por Fases y Sprints

### 🟢 Sprint Design 3.0: Tactile Studio Notebook (100% — Completado)
- [x] **D3.0.1** Tokens semánticos, Color Schemes Material 3 y tipografía Plus Jakarta Sans + JetBrains Mono vía Google Fonts. *(Completado)*
- [x] **D3.0.2** Catálogo completo de átomos táctiles en `presentation/atoms/` con micro-bordes y skeletons shimmer. *(Completado)*
- [x] **D3.0.3** Refactorización visual de pantallas core (Notes, Schedules, Grades, Calendar) y modularización de `GradesScreen`. *(Completado)*
- [x] **D3.0.4** Reingeniería Total y Centro de Control y Ajustes AAA (`SettingsScreen`): 6 secciones Bento, DataStore Preferences reactivo, cálculo y limpieza de caché, sincronización de temas y modales especializados. *(Completado)*

---

### 🟢 Fase 3: Features Avanzadas — Inteligencia Predictiva, Búsqueda Global y Pack P2P (100% Completada)
*Meta: Notificaciones locales inteligentes vía Android WorkManager, recordatorios proactivos de entregas/clases, analíticas predictivas de rendimiento académico, búsqueda global multientidad y pack académico P2P.*  
*Guías de Arranque:* [`docs/prd/KICKOFF_FASE_3.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_3.md) | [`docs/prd/KICKOFF_FASE_3.1.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_3.1.md) | [`docs/prd/KICKOFF_FASE_3.2.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_3.2.md)

- [x] **Sprint 3.1: Motor de Notificaciones Proactivas con WorkManager (100% Completado)**
  * **Infraestructura:** WorkManager 2.10.0 + HiltWorkerFactory (`Configuration.Provider`), canales de notificación especializados con vibración y deep links directos (`NotificationChannelsManager`, `NotificationSender`).
  * **Alertas de Evaluaciones:** `ScheduleEvaluationAlertUseCase` y `EvaluationReminderWorker` con cálculo exacto de antelación configurable y tags deterministas `"eval_${id}"`.
  * **Alertas de Clases:** `ScheduleClassAlertUseCase` y `ClassScheduleWorker` con cálculo del próximo día de semana, minutos de antelación y auto-reprogramación semanal continua.
  * **Resumen Diario & Resiliencia:** `DailySummaryWorker` matutino (07:00 AM) periódico con resumen de clases y entregas en 48h, `BootCompletedReceiver` para reprogramación automática tras reinicio del dispositivo (`ACTION_BOOT_COMPLETED`), y `CancelAlertUseCase`.
  * **Integración:** Sincronización en tiempo real en `AddEvaluationViewModel`, `EditEvaluationViewModel`, `AddScheduleViewModel`, `EditScheduleViewModel`, `SchedulesViewModel`, `SubjectDetailViewModel` y `SettingsViewModel`.
  * **Calidad:** 100% tests unitarios pasando y compilación `assembleDebug` exitosa.

- [x] **Sprint 3.2: Analíticas de Rendimiento Académico y Dashboard Predictivo (100% Completado)**
  * **Dominio Puro & TDD:** `CalculateAcademicStatsUseCase` multiescala (0-10, 0-20, 0-100) con diagnóstico de asignaturas (`APPROVED`, `ON_TRACK`, `AT_RISK`, `CRITICAL_FAIL`), cálculo de puntos faltantes y nota mínima requerida sobre el peso restante, tasa proyectada de éxito y correlación horaria semanal.
  * **Gráficos en Canvas Nativo:** `AcademicProgressChart` (barras comparativas animadas con línea de corte aprobatoria y monograms) y `GradeDistributionHistogram` (4 rangos de desempeño en `JetBrains Mono`).
  * **Fichas Bento Táctiles:** `AcademicEarlyWarningCard` (diagnóstico temprano y recomendaciones), `AcademicWorkloadCard` (correlación horas vs notas) y `AcademicAnalyticsContent`.
  * **Integración Ergonómica:** Conmutador segmentado táctil en `GradesScreen` (`[ Resumen Materias | Analíticas & Proyecciones ]`) manteniendo SRP (< 140 LOC).
  * **Calidad:** 100% de tests unitarios pasando (`CalculateAcademicStatsUseCaseTest`, `GradesViewModelTest`) y `assembleDebug` exitoso.

- [x] **Sprint 3.3: Búsqueda Global Multientidad en Tiempo Real (100% Completado)**
  * **Dominio Puro & TDD:** `SearchGlobalUseCase` con normalización diacrítica insensible a acentos/diacríticos (`Normalizer.Form.NFD`), tokenización multiterm y filtrado reactivo cruzado en 4 entidades (`SubjectData`, `Note`, `Evaluation`, `Schedule`).
  * **Arquitectura MVI:** `SearchViewModel` con `StateFlow` único, `debounce(300ms)`, filtrado por chips de tipo y persistencia de hasta 5 búsquedas recientes.
  * **Experiencia de Usuario:** `GlobalSearchBottomSheet` modal táctil interactivo, chips de filtro horizontal, tarjetas de resultado táctiles `SearchResultCard` con monogramas HSL y rutas type-safe hacia las pantallas de destino.
  * **Integración:** Acceso unificado en el TopBar de toda la aplicación (`onSearchClick`).

- [x] **Sprint 3.4: Pack Académico P2P, Intercambio Offline y Resiliencia Free-Tier (100% Completado)**
  * **Schema & Codec Portable:** `SubjectPackage` y `SubjectPackageCodec` con serialización JSON completa (Syllabus, Clases, Evaluaciones, Ponderaciones y Notas opcionales), compresión binaria GZIP y codificación Base64 en encabezado mágico `NOTIFAST1:`.
  * **Casos de Uso de Exportación e Importación:** `ExportSubjectPackageUseCase` e `ImportSubjectPackageUseCase` con sanitización de identificadores de usuario y estrategia de resolución de colisiones (`RENAME`, `OVERWRITE`, `SKIP_IF_EXISTS`).
  * **QR Code Generator Offline:** `QrCodeGenerator` con motor ZXing y diálogo modal táctil `QrCodeShareDialog` de alto contraste.
  * **UI de Intercambio P2P:** `ShareSubjectBottomSheet` integrado en `SubjectDetailScreen` y `ImportSubjectBottomSheet` integrado con selector de acciones en `NotesScreen`.
  * **Verificación:** 100% tests unitarios pasando (`SubjectPackageCodecTest`, `ExportSubjectPackageUseCaseTest`, `ImportSubjectPackageUseCaseTest`, `SearchGlobalUseCaseTest`, `SearchViewModelTest`) y build APK `assembleDebug` exitoso.

---

### 🟢 Fase 3.5: Almacenamiento Local 100% Offline-First (100% Completado)
*Meta: Migración integral de NotiFast a una arquitectura autónoma 100% offline-first basada en Room Database, DataStore Preferences y almacenamiento interno de archivos, eliminando la dependencia obligatoria de Firebase en runtime y garantizando persistencia inmediata sin red.*  
*Plan de Ejecución:* [Plan SDD Offline-First](file:///home/analista/Documents/Projects/Notifast/.superpowers/plans/2026-10-05-offline-first-local-storage.md)

- [x] **Task 1: Configuración de Dependencias Room y Tipografías Offline**
  * Incorporación de AndroidX Room 2.6.1 con plugin KSP (`androidx.room:room-runtime`, `androidx.room:room-ktx`, `androidx.room:room-compiler`).
  * Inclusión de fuentes empaquetadas en `assets/fonts/` (`PlusJakartaSans` y `JetBrainsMono`) eliminando dependencias de Google Fonts descargables en red para garantizar consistencia visual 100% offline.
  * Configuración de reglas de backup de Room y preferencias en `res/xml/backup_rules.xml` y `res/xml/data_extraction_rules.xml`.

- [x] **Task 2: Entidades Room, TypeConverters y Desacoplamiento de Modelos de Dominio**
  * Creación de entidades inmutables Room: `SubjectEntity`, `NoteEntity`, `EvaluationEntity`, `ScheduleEntity`, `UserEntity`.
  * Configuración de índices optimizados (`userId`, `subjectId`) y claves foráneas en cascada.
  * Implementación de `RoomConverters` con soporte para `Instant`, `List<String>` y `LocalDate`.
  * Desacoplamiento total de dependencias de Firebase en modelos de dominio y mapping bidireccional puro (`toDomain()`, `toEntity()`).

- [x] **Task 3: DAOs Reactivos Room y Base de Datos NotiFast**
  * Definición de `SubjectDao`, `NoteDao`, `EvaluationDao`, `ScheduleDao` y `UserDao` con consultas reactivas basadas en `Flow` y operaciones de escritura suspendibles.
  * Creación de `NotiFastDatabase` con soporte de Room Database y migración destructiva controlada en desarrollo.
  * Módulo Hilt `DatabaseModule` para inyección de singleton de la base de datos y cada uno de los DAOs.

- [x] **Task 4: Repositorios Locales Room y Cascada Segura de Archivos**
  * Implementación de `LocalNoteRepositoryImpl`, `LocalEvaluationRepositoryImpl`, `LocalScheduleRepositoryImpl` e `ImageRepositoryImpl`.
  * Lógica de eliminación en cascada segura (`DeleteSubjectCascadeUseCase`): borrado automático de registros asociados y purga física de archivos de imágenes en almacenamiento local (`app_notes_images/`) para evitar fugas de memoria o almacenamiento huérfano.
  * Actualización de bindings Hilt en `RepositoryModule` conectando interfaces de dominio a repositorios Room locales.

- [x] **Task 5: Autenticación Local Autónoma y Sesión en DataStore**
  * Implementación de `LocalAuthRepositoryImpl` para gestión completa de perfil y sesión de estudiante sin conexión a Firebase Auth.
  * Persistencia reactiva del usuario activo y credenciales mediante `UserPreferencesDataSource` (DataStore) y `UserDao`.
  * Hashing seguro de contraseña con SHA-256 + salt determinista e interfaz limpia `AuthRepository`.

- [x] **Task 6: Onboarding Local de Bienvenida para el Estudiante**
  * Creación de `OnboardingScreen` y `OnboardingViewModel` bajo especificación Material 3 / Tactile Studio.
  * Captura amigable y ergonómica de nombre, carrera, semestre, selección de avatar estudiantil y credenciales de acceso local opcional.
  * Validación táctil con feedback instantáneo y guardado directo de perfil y sesión.

- [x] **Task 7: Navegación Dinámica en NavGraph y Alineación de Ajustes**
  * Integración en `NavGraph` con `startDestination` reactivo condicionado al estado de onboarding/sesión local (`Screen.Onboarding` vs `Screen.Notes`).
  * Adaptación de `SettingsScreen` y `SettingsViewModel`: visualización del perfil local del estudiante, sincronización de DataStore y opción de cerrar sesión local.

- [x] **Task 8: Indicador Honesto de Almacenamiento Local & Optimización de Autosave**
  * Corrección visual en `NotiFastTopBar`: sustitución del texto engañoso "Sincronizado con la nube" por "Guardado localmente en este dispositivo" con icono táctil `CloudDone` y micro-borde `outlineVariant`.
  * Optimización de guardado automático en `AddNoteViewModel` y `EditNoteViewModel`: verificación de URI previa para evitar duplicación y sobreescritura redundante de imágenes en almacenamiento interno.

- [x] **Task 9: Test de Límite de Arquitectura Offline-First & Verificación Integral**
  * Creación de `OfflineArchitectureBoundaryTest` para verificación estricta y automatizada en CI/CD:
    - Validación de que ningún archivo de presentación, dominio o datos locales importe `com.google.firebase.firestore`.
    - Validación de que ningún archivo de presentación o dominio importe `com.google.firebase.auth`.
  * Verificación completa: 237 pruebas unitarias en 46 suites pasando exitosamente (`testDebugUnitTest`) y compilación APK completa aprobada (`assembleDebug`).

- [x] **Hotfix: Crash en segundo inicio de app (NPE en NavGraph bottomNav computation)**
  * **Bug:** La app crasheaba con `NullPointerException: Screen.getRoute() on null` al reabrirla. La causa raíz estaba en la evaluación de `showBottomBar` en `NavGraph.kt:85-87` — el lambda anidado `Screen.bottomNavItems.any { screen -> ... screen.route }` accedía a un `Screen` null durante la recomposición al evaluar `currentDestination?.hierarchy?.any { it.route == screen.route }`.
  * **Fix:** Se reestructuró la evaluación para pre-computar `bottomNavRoutes` con `mapNotNull { it?.route }` y usar `dest.route in bottomNavRoutes` — eliminando el acceso anidado a `Screen.route` dentro del lambda de `hierarchy.any`.
  * **Archivo:** `presentation/navigation/NavGraph.kt:85-88`
  * **Verificación:** `assembleDebug` ✅ + `testDebugUnitTest` ✅ + installDebug + launch en dispositivo ✅ (1er y 2do inicio sin crash)

---

### ⚪ Fase 4: Pulido, Experiencia, Accesibilidad y Rendimiento (Siguiente Fase)
*Meta: Micro-interacciones avanzadas, accesibilidad total (a11y), transiciones de pantalla con View Transitions en Compose, auditoría de rendimiento y depuración de memoria.*  
*Guía de Arranque:* [`docs/prd/KICKOFF_FASE_4.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_4.md)

---

### ⚪ Fase 5: Despliegue, Métricas de Tesis y Lanzamiento en Play Store (Pendiente)
*Meta: Testing en aula real, recolección de métricas de tesis y publicación en Google Play Store.*
