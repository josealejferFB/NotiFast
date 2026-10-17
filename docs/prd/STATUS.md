# 📊 Estado de Ejecución y Roadmap — NotiFast v3.0

> **Documento de Seguimiento de Fases del PRD Maestro y Sistema de Diseño 3.0**  
> **Última actualización:** 17 de Octubre, 2026  
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
Fase 3.7: Normalización Sheets/FAB[████████████████] 100% (5/5 tareas) ✅
Editor Premium (OneNote-like)     [████████████████] 100% (12/12 tareas) ✅
Sanitización Materias & Profesores[████████████████] 100% (9/9 tareas) ✅
Auditoría Global & Pipelines CI/CD[████████████████] 100% (6/6 pilares) ✅
Copia en la Nube (Google Drive)   [████████████████] 100% (21/21 tareas) ✅
Cloud Hub AAA & Rediseño Táctil   [████████████████] 100% (4/4 objetivos) ✅
Onboarding & Acceso Cloud Sync    [████████████████] 100% (4/4 pilares) ✅
Grades Hub & Analíticas AAA       [████████████████] 100% (5/5 pilares) ✅
Sistema de Temas Dinámicos AAA    [████████████████] 100% (15/15 tareas) ✅
Fase 4: Pulido & Experiencia      [░░░░░░░░░░░░░░░░]   0% (0/12 tareas)
Fase 5: Despliegue & Tesis        [░░░░░░░░░░░░░░░░]   0% (16 tareas)
------------------------------------------------------------------
PROGRESO TOTAL ESTIMADO:          [████████████████]  98% (177/183 tareas)
SUITE DE PRUEBAS AUTOMATIZADA:    645 tests pasando (0 fallos, 0 ignorados) ✅
```

---

## 📝 Editor Premium de Apuntes (OneNote-like) — 100% Completado

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

### Fase 3: Anotación/dibujo sobre fotos (100% ✅)
- `PhotoAnnotation` (Stroke, AnnotationText, Arrow) + `PhotoAnnotationCodec`.
- `DrawingCanvasState` (undo/redo/eraser) + `DrawingCanvas` (pointerInput + Path).
- `PhotoAnnotatorScreen` con toolbar flotante (lápiz, resaltador, colores, grosores).
- Flatten de anotación + persistencia en bloque Image (`annotationJson`).

### Fase 4: Checklists + fórmulas + export PDF (100% ✅)
- Checklists interactivos (toggle, editar, añadir/quitar items).
- MathRenderer nativo + SymbolKeyboard.
- Export PDF con listas ordenadas/numeradas.
- Búsqueda global indexa bloques.

### Fase 5: Bottom Sheet Unificado + Rich Text + Tags (100% ✅ — Oct 2026)
- **`NoteEditorBottomSheet`** como destino `dialog()` de Navigation Compose — crear y editar llaman al mismo flujo modal expandible (70% ↔ full screen).
- **`RichBlockTextField`** + `applyNoteSpans` — formato de texto (negrita, cursiva, subrayado, tachado, resaltado) con `TextSpan` sobre selección.
- **`TextSpanOps`** — álgebra pura de spans (toggle/merge/split/adjustForEdit/stylesAt) con tests.
- **`TextFormatToolbar`** contextual sobre selección de texto.
- **`NoteTagsEditor`** — chips de etiquetas temáticas sugeridas + input custom + remove (FlowRow wrap).
- **`EditableListBlock`** — listas ordenadas (`isOrdered`) y desordenadas editables con add/remove.
- **Checklists** con añadir/quitar tareas.
- **Fix bugs:** ruta de búsqueda global (`note_editor?noteId=`), match de tags case-insensitive, export markdown con bloques+spans, `NoteWithSubject` dead code eliminado.
- **Riesgos mitigados:** R1 span index shifting (`adjustForEdit`), R2 cursor reset (`TextFieldValue` un-keyed), R3 codec manual `isOrdered`, R4 shadowing `applyNoteSpans`, R5 sin refactor VM (SavedStateHandle en dialog dest).

### Fase 6: Simplificación Nativa AAA & Acoplamiento Teclado-Toolbar (100% ✅ — 9 Oct 2026)
- **Eliminación Total de Código Huérfano/Full-Screen:** Se removieron limpiamente 10 archivos legados de pantalla completa y editores de bloques pesados (`NoteEditorScreen`, `EditorTopBar`, `BlockTextField`, `BlockListEditor`, etc.), recuperando la simplicidad nativa y reduciendo la complejidad ciclomática.
- **Bottom Sheet Nativo Material 3:** `NoteEditorBottomSheet` ahora utiliza `rememberModalBottomSheetState(skipPartiallyExpanded = true)`, garantizando que el editor abra siempre de forma expandida y sin pasos intermedios incómodos.
- **Resolución Definitiva de Conflicto IME / Toolbar:** Se agregó `android:windowSoftInputMode="adjustResize"` en `AndroidManifest.xml` y se configuró `contentWindowInsets = { WindowInsets.statusBars }` en `ModalBottomSheet`. La barra de herramientas `DockedAccessoryToolbar` utiliza `Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))` y el área de contenido utiliza `weight(1f)`, asegurando que al enfocar el campo Markdown, la barra flote **directamente encima del teclado**, sin quedar oculta ni detrás del mismo.
- **Ergonomía de Entrada & Cierre Táctil de Foco:** Se implementó `onTapOutside` en el scope del `ModalBottomSheet`, permitiendo que al tocar cualquier área exterior a los campos de texto activos (cabecera, espaciado, fondo, etiquetas), se remueva el foco (`clearFocus(force = true)`) y se minimice/cierre el teclado virtual de manera nativa y fluida.
- **Interoperabilidad de Casillas en Vista Previa:** Integración de `onTaskCheckedChange` en `NoteDetailExpandable` y `NotesViewModel.ToggleTaskCheck`, permitiendo alternar casillas de tareas (`- [ ]` / `- [x]`) interactivamente desde el modal de vista previa del dashboard con persistencia inmediata y actualización optimista.
- **Ergonomía de Creación de Materia y Etiquetas:** Botón de "Crear materia" cierra fluidamente el bottom sheet para abrir el creador de materias; creador de etiquetas enriquecido con cápsula de input animada (`TagInputCapsule`).
- **Verificación en Hardware Real:** Instalado y probado con captura de pantalla y validación de toques en dispositivo Samsung (`SM-A065M`). Suite de pruebas unitarias 100% en verde.

### Tests añadidos
- `NoteBlockCodecTest` (14), `MarkdownToBlocksParserTest` (16), `NoteBlocksRepositoryTest` (4),
  `NoteEditorViewModelTest` (22), `NoteEditorImageTest` (3), `PhotoAnnotationCodecTest` (8),
  `DrawingCanvasStateTest` (10), `TextSpanOpsTest` (11), `RichTextSpanTest` (6),
  `ExportNoteMarkdownUseCaseTest` (4). Suite completa: 402 tests en verde.

---

## 👨‍🏫 Sanitización de Materias y Directorio de Profesores — 100% Completado (13 Oct 2026)

**Objetivo:** Sanitizar la gestión de asignaturas garantizando que cada materia muestre su color corporativo e icono temático seleccionados, permitir su edición con actualización atómica en cascada multi-tabla en Room, e introducir el Directorio de Profesores con sub-pestaña `[ Materias | Profesores ]`, fichas de contacto directo (WhatsApp con saludo contextual prellenado, SMS, Llamada), selector contextual en el creador de horarios, y sección de cuerpo docente en el detalle de la asignatura.

### Entregables Completados:
1. **Modelo de Dominio & Utilidades de Comunicación:**
   - Modelo inmutable `Teacher` (`id`, `userId`, `firstName`, `lastName`, `fullName`, `phoneNumber`, `email`, `subjectNames`, `notes`).
   - `TeacherFormatters`: formateo E.164, codificación URI segura y generación de mensajes de saludo académico prellenados para WhatsApp. Tests unitarios en verde.
2. **Persistencia Local Room v4 & Migración No Destructiva:**
   - `TeacherEntity` y `TeacherDao` con operaciones reactivas Flow y CRUD completo.
   - `MIGRATION_3_4` de base de datos Room v3 a v4 preservando la integridad de materias, notas, evaluaciones y horarios existentes. Tests en memoria Room pasando.
3. **Transacción Multi-Tabla de Renombrado en Cascada:**
   - `updateSubject` con `database.withTransaction`: sincroniza atómicamente el cambio de nombre en `subjects`, `notes`, `evaluations`, `schedules` y `teachers`.
   - Detección y prevención estricta de colisiones de nombres de asignaturas duplicadas.
4. **Repositorio Docente:**
   - `TeacherRepository` y `LocalTeacherRepositoryImpl` inyectados vía Hilt con suite de tests unitarios completos.
5. **Edición Visual de Materias:**
   - `EditSubjectDialog`: diálogo para personalizar nombre, paleta cromática e icono temático.
   - Integración directa en tarjetas de la cuadrícula Bento (`SubjectGridBento`) y hero del detalle (`SubjectDetailHeroBento`).
6. **Selector Contextual Docente en Horarios:**
   - `ScheduleTeacherSelector`: dropdown si la materia ya tiene profesores registrados; tarjeta de advertencia y campo rápido para ingresar un nuevo docente si no tiene.
   - Vinculación automática en `AddScheduleViewModel` y `EditScheduleViewModel`: al guardar un horario con un profesor nuevo, este se crea y asocia inmediatamente a la materia.
7. **Sub-pestaña "Profesores" y Directorio Docente:**
   - Segmented pill bar `[ Materias | Profesores ]` en la pestaña Materias de `NotesScreen`.
   - `ProfessorDirectoryContent`: buscador en tiempo real, estados vacíos pulidos, listado reactivo con `ProfessorCard`.
   - `TeacherDetailBottomSheet`: ficha de contacto completa con acciones directas para WhatsApp, SMS, Llamar, Correo, Editar y Eliminar.
   - `AddEditTeacherBottomSheet`: modal para añadir y editar profesores con selector de materias por FilterChips.
8. **Cuerpo Docente en `SubjectDetailScreen`:**
   - Inyección de `TeacherRepository` en `SubjectDetailViewModel` y reactividad combinada.
   - `SubjectFacultyCard` integrado en el cuadrante Panorama (`SubjectPanoramaTab`), mostrando los profesores asignados con sus avatares, contacto y acceso rápido a WhatsApp y la ficha detallada.

### Tests Unitarios Añadidos (100% pasando):
- `TeacherFormattersTest` (limpieza de teléfonos, URL e intents de WhatsApp).
- `RoomDaoInMemoryTest` (operaciones CRUD en `TeacherDao`, consultas por materia).
- `LocalNoteRepositoryImplTest` (renombrado en cascada multi-tabla y manejo de colisiones).
- `LocalTeacherRepositoryImplTest` (contrato de repositorio docente y aislamiento multi-usuario).
- `AddScheduleViewModelTest` & `EditScheduleViewModelTest` (selector contextual y auto-guardado).
- `NotesViewModelTest` (búsqueda de docentes, selección y borrado).
- `SubjectDetailViewModelTest` (reactividad de docentes por asignatura).

#### Integración de Selección y Creación Oficial de Docentes en Horarios (Octubre 2026):
- **Eliminación de input de texto libre:** Se removió el campo de texto manual de profesor en `ScheduleTeacherSelector`, reemplazándolo por un selector conectado al sistema de profesores guardados (`TeacherRepository`).
- **Integración con `AddEditTeacherBottomSheet`:** La opción "+ Nuevo Profesor" o el estado vacío abren directamente la hoja oficial de creación de docentes, preseleccionando la materia actual.
- **Persistencia y Compatibilidad Cloud:** Los docentes creados se guardan en Room (`TeacherEntity`) y se sincronizan automáticamente con Google Drive dentro de `GlobalSnapshot.teachers`.
- **Modularización y SRP:** `ScheduleTeacherSelector` subdividido en `EmptyTeacherCard` y `TeacherDropdownMenuItem` para garantizar archivos $\le 250$ LOC.

---

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

## 🎨 Fase 3.7: Normalización de BottomSheets & FAB Expandible (100% ✅ — 10 Oct 2026)

**Objetivo:** Eliminar la inconsistencia de navegación modal vs pantalla completa unificando la creación y edición de horarios y evaluaciones en `ModalBottomSheet` (alineado con Notas y Materias), sustituir el bottom sheet selector de Agenda por un FAB animado vertical (`NotiFastExpandableFab`), y garantizar persistencia de borradores al encadenar la creación de nuevas materias.

### Entregables implementados
1. **FAB Expandible en Agenda (`SchedulesScreen`):**
   - Sustituido el FAB simple y eliminado el modal intermedio redundante (`AgendaCreateActionBottomSheet.kt`).
   - Integrado `NotiFastExpandableFab` con acciones animadas: arriba *Nuevo Horario* (icono calendario/horario, color primario) y abajo *Nueva Evaluación* (icono tarea/evaluación, color acento/error).
   - Test unitario de orden y atributos: `SchedulesFabNormalizationTest`.
2. **`ScheduleEditorBottomSheet`:**
   - Unificación de creación y edición en modal `ModalBottomSheet` con bordes redondeados (28.dp), drag handle, `imePadding`, detección de cambios y confirmación de descarte con `NotiFastDialog`.
   - Soporte para preselección de materia (`initialSubject`) y recarga reactiva con auto-selección (`RefreshSubjectsAndSelect`).
3. **`EvaluationEditorBottomSheet`:**
   - Unificación de creación y edición de evaluaciones con selector de materia, tipo, fecha, slider de ponderación e ingreso de nota (en modo edición).
   - Confirmación de descarte con `NotiFastDialog`, soporte para `initialSubject` y `RefreshSubjectsAndSelect`.
4. **Inicialización Segura & Desacoplamiento de ViewModels:**
   - `EditScheduleViewModel` y `EditEvaluationViewModel` soportan inicialización sin argumentos en `SavedStateHandle` y evento `Init(id)` con recarga limpia.
   - `AddScheduleViewModel` y `AddEvaluationViewModel` implementan `ResetState` previniendo cierres prematuros en aperturas consecutivas.
5. **Hoisting no destructivo en `NavGraph.kt`:**
   - Estados de editores hoisted en `MainContent` (`activeScheduleEditorArgs`, `activeEvaluationEditorArgs`).
   - Superposición de `AddSubjectBottomSheet` sobre los editores activos sin descartar campos ni perder texto en progreso.
   - Preservación de borrador en `NoteEditorViewModel` al seleccionar materia nueva.
   - Respeto estricto del límite ≤ 250 LOC por archivo composable (`NavGraph.kt` en 244 LOC).

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

## 🛡️ Auditoría Global de Calidad, Pipelines y Remediación (100% Completado)

> **Documento de Auditoría:** [`audit-global-notifast.md`](file:///home/p0xd/.gemini/antigravity/brain/320a28ab-17a7-4cef-ba4d-84e12f05929f/audit-global-notifast.md)

- [x] **Pilar 1: Erradicación de Estados Bloqueantes y Ciclo de Vida Crítico**
  * `BootCompletedReceiver.kt`: Refactorizado con `goAsync()` y ejecución en coroutine `CoroutineScope(Dispatchers.IO).launch` con `pendingResult.finish()` en bloque `finally`, previniendo ANR y terminación prematura del proceso por parte del sistema operativo al reprogramar alarmas tras reinicio del dispositivo.
  * Eliminación de bloques `catch` vacíos o silenciados en `AddScheduleViewModel.kt`, `EditScheduleViewModel.kt`, `SettingsSectionsContent.kt`, `ImportSubjectBottomSheet.kt`, `SchedulesScreen.kt` y `SubjectSchedulesTab.kt`; registro explícito de advertencias (`Log.w`) y propagación al usuario mediante Toast/Snackbar.

- [x] **Pilar 2: Purgado de Dependencias Muertas y Reducción de Bloat**
  * Eliminación total de artefactos Firebase obsoletos en `app/build.gradle.kts` (`firebase-firestore`, `firebase-auth`, `firebase-storage`) tras la migración a Room offline-first. Conservados únicamente Crashlytics y Analytics.
  * Remoción de proveedores muertos `provideFirebaseAuth` y `provideFirestore` en `AppModule.kt`.
  * Eliminación de 4 pantallas legacy redundantes (~862 LOC purgadas): `AddEvaluationScreen.kt`, `AddScheduleScreen.kt`, `EditEvaluationScreen.kt`, `EditScheduleScreen.kt`.
  * Purga de `logcat_dump.txt` (20.4 MB) del árbol git y reforzamiento de `.gitignore`.

- [x] **Pilar 3: Prevención de Crashes en Navegación (Búsqueda Global)**
  * Solución de ruta inválida `note_editor?noteId=...`: `NavGraph.kt` intercepta selecciones de notas, evaluaciones y horarios en `GlobalSearchBottomSheet` asignando reactivamente los argumentos a los BottomSheets modulares correspondientes.
  * Eliminación de rutas fantasmas borradas en `NavHost`.

- [x] **Pilar 4: Sanitización de UI, Internacionalización y Tokens de Diseño**
  * Cero strings quemados: Localización al 100% en `res/values/strings.xml` para feedback de feedback dialogs, edición de perfil, tarjetas de simulador de notas, contactos docentes, códigos QR, detalles de agenda y resúmenes de worker.
  * Cero colores arbitrarios: Sustitución de `Ruby` directo en `NavGraph.kt` por `MaterialTheme.colorScheme.primary`.
  * Cero wildcard imports: Erradicación del 100% de `import .*` en `SubjectDetailScreen.kt`, `SubjectDetailHeroBento.kt`, `SubjectNotesTab.kt`, `SubjectPanoramaTab.kt`, `SubjectSegmentedTabs.kt`.

- [x] **Pilar 5: Modularización Arquitectónica (Límite $\le 250$ LOC)**
  * `AgendaMonthCalendarView.kt` (269 LOC $\rightarrow$ 229 LOC): Extracción de `CalendarDayCell.kt` como componente atómico reusable.
  * `ImportSubjectBottomSheet.kt` (321 LOC $\rightarrow$ 244 LOC): Extracción de `ImportPayloadPreview.kt` e `ImportCameraScanButton.kt`.

- [x] **Pilar 6: Pipeline CI/CD Automatizado y Optimización Gradle**
  * Creación del workflow `.github/workflows/ci.yml` con ejecución paralela:
    1. `python-tests`: Validación de landing page y testing documental (18 tests).
    2. `android-tests`: Ejecución de suite unitaria (`./gradlew testDebugUnitTest`) con subida de reportes en caso de fallo.
    3. `android-build`: Ensamblado de release debuggable (`./gradlew assembleDebug`) con almacenamiento de APK compilada.
  * Activación de paralelismo y cache en `gradle.properties`: `org.gradle.parallel=true`, `org.gradle.caching=true`.
  * Configuración de `signingConfigs` en `build.gradle.kts` para compilación release con keystore debug fallback.

---

## ☁️ Copia de Seguridad y Sincronización en Google Drive — 100% Completado (15 Oct 2026)

> **Documento de Diseño:** [`docs/superpowers/specs/2026-10-15-cloud-backup-google-drive-design.md`](../superpowers/specs/2026-10-15-cloud-backup-google-drive-design.md)  
> **Plan de Ejecución Atómica:** [`docs/superpowers/plans/2026-10-15-cloud-backup-google-drive.md`](../superpowers/plans/2026-10-15-cloud-backup-google-drive.md)  
> **Matriz de Resultados:** [`docs/superpowers/specs/2026-10-15-cloud-backup-scenario-matrix-results.md`](../superpowers/specs/2026-10-15-cloud-backup-scenario-matrix-results.md)

**Objetivo:** Implementar la copia de seguridad y restauración manual bajo demanda por materia hacia el espacio oculto `appDataFolder` de Google Drive del estudiante, garantizando privacidad total (zero-server backend), resolución interactiva de colisiones ("Sobrescribir" vs "Agregar como Materia (1)"), borrado dual selectivo (solo en teléfono vs teléfono y nube), indicador visual honesto en el TopBar (`PhoneAndroid`, `CloudDone`, `CloudSync`) y coexistencia limpia con el Pack Académico P2P (QR/texto).

### Entregables Completados (21 / 21 Tareas):

1. **Infraestructura de Red & REST Client:**
   - Dependencias OkHttp 4.12.0 y Gson 2.11.0 integradas sin romper compatibilidad.
   - `GoogleAuthDataSource`: Autenticación OAuth2 Google Identity con scope restringido `drive.appdata`, manejo de resolución vía `PendingIntent` e invalidación automática de token ante 401.
   - `DriveRestClient`: Implementación directa contra Google Drive REST API v3 con operaciones atómicas multipart para `files.create`, `files.update`, `files.get`, `files.delete` y `files.list`.
   - `DriveSubjectBackupStore`: Manejo de archivos en la nube con subidas multipart atómicas y fallback automático a `POST` ante errores 404 en `PATCH`.

2. **Persistencia Local Room v5 & Migración Multi-Cuenta:**
   - `SubjectCloudStateEntity` y `SubjectCloudStateDao` con clave primaria compuesta `(accountId, subjectId)`.
   - `MIGRATION_4_5` implementada y verificada para la creación de la tabla `subject_cloud_state`.
   - Soporte multicuenta nativo: el cambio de cuenta en el dispositivo oculta los estados de la cuenta previa y los preserva intactos para cuando vuelva a iniciar sesión.

3. **Compresión Inteligente & Empaquetado Atómico:**
   - `SubjectBackupCodec`: Empaquetado de materias con notas, imágenes, evaluaciones y horarios en ZIP con `manifest.json`.
   - `ImageCompressor`: Redimensionamiento optimizado de fotografías adjuntas a apuntes ($\le 1600\,\text{px}$, JPEG 80) reduciendo el consumo de ancho de banda y cuota sin degradar la legibilidad.
   - `GlobalBackupData`: Serialización y restauración de perfil de usuario, preferencias y directorio de profesores (`global.nfbak`).

4. **Casos de Uso de Sincronización y Dominio:**
   - `ResolveSubjectNameUseCase`: Generador inteligente de nombres con sufijo incremental `Materia (1)`, `Materia (2)` y validación en tiempo real (longitud $\le 50$, caracteres en blanco, colisiones case-insensitive).
   - `UploadSubjectsUseCase` & `UploadGlobalDataUseCase`: Subida por lote con emisión de progreso reactivo `UploadProgress` y resultados atómicos por materia (`ItemOutcome`).
   - `RestoreSubjectsUseCase` & `RestoreGlobalDataUseCase`: Restauración en transacción única Room con reprogramación proactiva de notificaciones locales y purga limpia de imágenes obsoletas tras sobreescritura.
   - `DeleteCloudSubjectUseCase`: Borrado seguro solo en nube o coordinado teléfono + nube.
   - `ObserveLocalSyncSummaryUseCase`: Monitor reactivo del estado de sincronización global para la cabecera.

5. **Experiencia de Usuario en Compose (Límite $\le 250$ LOC):**
   - `CloudHubSheet`: Hoja modal principal con selección individual y global, cálculo dinámico de `canUpload`/`canRestore` y banner de materias solo en nube.
   - Subcomponentes modulares en `presentation/feature/cloud/components/`: `CloudAccountHeader`, `CloudConfirmDialog`, `CloudErrorDialog`, `CloudProgressOverlay`, `CloudStatusChip`, `CloudSubjectRow`, `CloudSummaryCard`, `GlobalSettingsRow`, `LinkAccountPrompt`, `RestoreConflictDialog`.
   - Diálogo de borrado dual en `NotesDialogs`: cuando una materia cuenta con copia en Drive, ofrece "Solo en este teléfono" vs "En el teléfono y en la nube", con confirmación de contingencia si falla la red.
   - Sección de cuenta Google en `SettingsScreen`: `SettingsCloudAccountSection` permite vincular, cambiar y desvincular la cuenta personal con diálogo de confirmación y preservación estricta de datos locales.
   - Indicador honesto en `NotiFastTopBar`: muestra `PhoneAndroid` (sin cuenta), `CloudDone` (todo sincronizado) o `CloudSync` con punto Ruby (cambios pendientes).

6. **Calidad y Verificación:**
   - Cobertura de tests unitarios: **570 tests pasando al 100%** (incluyendo suites completas para `CloudViewModelTest`, `SettingsViewModelTest`, `NotesViewModelTest`, use cases y stores).
   - Matriz completa de 51 escenarios (A1–A11, B1–B12, C1–C14, D1–D7, E1–E4, F) documentada y validada en `docs/superpowers/specs/2026-10-15-cloud-backup-scenario-matrix-results.md`.
   - Verificación de compilación release y debug: `./gradlew testDebugUnitTest assembleDebug` aprobados.

7. **Hardening de Casos Borde y Sincronización Inteligente (Oct 2026):**
   - **No-Op en Contenido Idéntico:** `UploadSubjectsUseCase` y `RestoreSubjectsUseCase` detectan concordancia de hash SHA-256 (`ContentHasher`) entre el snapshot local y los metadatos de Drive; actualizan el estado a `SYNCED` de inmediato sin descargar archivos, sin sobreescribir Room DB y sin disparar diálogos de conflicto innecesarios.
   - **Gestión de Errores por Materia:** Aislamiento de fallos transitorios en `failedSubjectIds` con chip de estado contextual (`cloud_status_error` en Ruby) y reseteo inmediato al refrescar (`Refresh`), reintentar (`RetryFailed`) o interactuar (`Toggle`).
   - **Ciclo de Vida de `CloudSummaryCard`:** Descarte automático con `delay(5000L)` para lotes exitosos, descarte al interactuar o salir de la hoja, y animación suave con `AnimatedVisibility(fadeIn() + expandVertically(), fadeOut() + shrinkVertically())`.
   - **Blindaje de Concurrencia:** Propagación estricta de `CancellationException` en todos los flujos de subida y descarga para garantizar rollbacks Room atómicos y prevenir estados inconsistentes en SQLite.
   - **Modularización Estricta:** Extracción de `CloudDialogsHost` para mantener `CloudHubSheet` en 234 líneas ($\le 250$ LOC).
   - **Refresco Automático de Materias:** Inclusión de `LaunchedEffect(Unit)` en `CloudHubSheet` para disparar `CloudEvent.Refresh` cada vez que se abre la hoja, listando materias recién creadas sin reiniciar la app.
   - **Sincronización Total de Horarios y Evaluaciones:** En `RoomSubjectBackupStore`, inclusión de fallback a `subjectName` cuando `subjectId` es blanco, y resolución automática de `subjectId` en `LocalScheduleRepositoryImpl` y `LocalEvaluationRepositoryImpl` al persistir, garantizando que los horarios y evaluaciones se respalden en el snapshot y se restauren integralmente.
   - **Foto de Perfil en Tiempo Real:** Exposición de `observeUserProfile(uid)` en `AuthRepository` y combinación reactiva con `googleAccountRepository.account` en `NotesViewModel`, `SchedulesViewModel` y `GradesViewModel`, permitiendo que la foto de Google se muestre inmediatamente al sincronizar por primera vez o al restaurar el perfil sin cerrar la app.
   - **Estabilidad de Hash ante Creación de Apuntes:** Se eliminó la divergencia de hash que causaba que una materia subida continuara mostrando "Cambios sin subir" (`CloudSubjectStatus.LOCAL_CHANGES`). Se normalizaron los identificadores de bloques en `ContentHasher` (`normId = "b_$index"`), se hizo determinista la generación de IDs en `MarkdownToBlocksParser`, se persistieron `blocksJson` y `schemaVersion = 2` en `LocalNoteRepositoryImpl.addNote` y `updateNote`, y se garantizó la actualización del `targetDriveId` en `UploadSubjectsUseCase`.
   - **Integración con Directorio de Profesores en Horarios:** Se reemplazó el campo de texto libre de profesor en `ScheduleEditorForm` por el selector táctil `ScheduleTeacherSelector`, conectándolo con `TeacherRepository`, incluyendo opción de crear un nuevo profesor directamente (`AddEditTeacherBottomSheet`) con preselección contextual de materia, y modularizando en `EmptyTeacherCard` y `TeacherDropdownMenuItem` ($\le 250$ LOC).

---

## 🚀 Rediseño y Reingeniería del Flujo de Acceso (Welcome & Onboarding UX) — 100% Completado (16 Oct 2026)

**Objetivo:** Transformar la pantalla de bienvenida/onboarding en una experiencia de acceso premium, honesta y sin fricciones, estableciendo el Modo Local (offline-first) como camino primario por defecto con transparencia total de privacidad, y ofreciendo Google Drive Cloud Backup como opción transparente con auto-despliegue de `CloudHubSheet` al completar la vinculación.

### Entregables Implementados:
1. **Acceso Primario Offline-First por Defecto:**
   - Sección de perfil local `LocalProfileSection` (selector de foto opcional, nombre requerido y apellido opcional) con botón de inicio local de alta prominencia táctil, sin barreras ni cuentas obligatorias.
   - Diagrama de red de conocimiento (`AcademicNodesMotion`): isotipo central NotiFast sobre superficie neutra, conectado mediante filamentos táctiles a nodos perimetrales de asignaturas (Matemáticas, Ciencias, Tecnología, Historia, Psicología, Artes) con pulsos sutiles de paquetes de datos y respiración armónica sin gradientes ruidosos ni sobrecarga cognitiva.
2. **Acceso Opcional y Honesto con Google Drive:**
   - Componente `GoogleDriveBackupCard` con tarjeta papercraft, icono `CloudSync` en contenedor secundario Aqua y badge "Opcional".
   - Mensaje honesto de valor: *"Inicia con Google solo si deseas activar la copia de seguridad automática de tus materias en tu Google Drive personal (carpeta privada appDataFolder)."*
   - Botón táctil secundario estilizado *"Vincular Google Drive para respaldo"* con gestión de estado de carga independiente (`isGoogleLoading`).
3. **Flujo de Integración Google OAuth2 y Auto-Despliegue de CloudHub:**
   - Ejecución nativa de Google Identity / Drive OAuth2 (`GoogleAccountRepository.signIn` y `completeAuthorization`).
   - Creación y prellenado automático del perfil local en Room utilizando el nombre y foto de Google (`parseGoogleName`), priorizando cualquier nombre ya tipeado por el estudiante.
   - Al completar la vinculación con Google, la navegación transiciona al dashboard y **despliega automáticamente `CloudHubSheet`** (`initialOpenCloudHub = true`), permitiendo al estudiante respaldar o restaurar materias inmediatamente.
4. **Higiene Arquitectónica y Suite de Pruebas:**
   - MVI Contract estricto con único StateFlow consolidado en `OnboardingContract.kt`.
   - Respeto del límite $\le 250$ LOC por archivo composable (OnboardingScreen en 150 LOC, AcademicNodesMotion en 200 LOC, CompactAvatarPicker en 167 LOC, LocalProfileSection en 156 LOC, GoogleDriveBackupCard en 148 LOC, OnboardingHeader en 49 LOC).
   - Avatar circular táctil de 76dp con badge flotante para cámara/edición y acción de descarte, eliminando el uploader rectangular de 160dp.
   - Entradas de nombre y apellido unificadas en fila ergonómica con acciones de teclado IME inmediatas.
   - Eliminación de tarjetas redundantes y chips con sobrecarga ("100% offline motor" y card offline) a favor de un layout sobrio, focalizado y de alta legibilidad.
   - Cero strings quemados: 100% de textos y badges en `res/values/strings.xml`.
   - Suite completa de 16 pruebas unitarias en `OnboardingViewModelTest` cubriendo el ciclo TDD completo (estados de carga, nombres compuestos Google, cancelación limpia de intent y autoOpenCloudHub). Suite global en 587 tests pasando al 100%.

---

## 🛡️ Normalización Global de Chips de Alertas & Sanitización de Errores AAA — 100% Completado (16 Oct 2026)

**Objetivo:** Erradicar todas las chips por defecto de Android, textos rojos desalineados y `Toast.makeText` huérfanos que exponían excepciones técnicas del sistema. Crear un motor de sanitización de errores en dominio puro (`ErrorSanitizer`), estandarizar componentes atómicos táctiles AAA (`NotiFastAlertChip`, `NotiFastAlertBanner`, `NotiFastChip`), y garantizar soporte completo de accesibilidad TalkBack, feedback háptico y estética papercraft en toda la aplicación.

### Entregables Implementados:
1. **Motor de Sanitización en Dominio Puro (`ErrorSanitizer`):**
   - Módulo Kotlin puro en `domain/util/ErrorSanitizer.kt` probado con 12 tests unitarios TDD (`ErrorSanitizerTest`).
   - Mapeo de errores de red (offline, timeout), autenticación (expiración de sesión, Google Auth 12500/10), entidades inexistentes, colisiones de claves SQLite y cuotas de almacenamiento a mensajes empáticos, claros y en español estandarizado.
   - Eliminación estricta de prefijos y trazas técnicas (`java.lang.*`, `Firebase*`, `ApiException:`, `SQLiteConstraintException`).
   - Normalización tipográfica (mayúscula inicial y puntuación de oración obligatoria).
2. **Sistema de Diseño Atómico de Alertas y Chips AAA (`presentation/atoms/`):**
   - **`AlertSeverity` & `AlertColors`:** 4 niveles de severidad (`ERROR`, `WARNING`, `SUCCESS`, `INFO`) con esquemas de color adaptativos para temas Claro y Oscuro, garantizando contraste WCAG AAA/AA sobre tokens corporativos Ruby y Aqua.
   - **`NotiFastAlertChip`:** Chip/píldora táctil con animación de escala con resorte (`0.96f`), feedback háptico, micro-botón de descarte opcional y semántica TalkBack con `liveRegion = LiveRegionMode.Polite`.
   - **`NotiFastAlertBanner`:** Tarjeta de advertencia/error de ancho completo con revelación animada (`expandVertically + fadeIn`), insignia circular para icono, título, mensaje descriptivo, botón de acción y botón de descarte.
   - **`NotiFastChip` Mejorado:** Actualizado con soporte para `leadingIcon`, `trailingIcon`, `contentPadding` personalizable y alineación centrada para reemplazar completamente las `FilterChip` e `InputChip` por defecto de Android.
3. **Refactorización Integral de Pantallas y Formularios:**
   - **Erradicación de Toasts y Snackbars por Defecto:** Creación de `NotiFastSnackbarHost` y `rememberNotiFastAlert()`. Integración en el `Scaffold` raíz de `NavGraph.kt` vía `CompositionLocalProvider(LocalNotiFastSnackbarHostState provides snackbarHostState)` para que todas las alertas floten armoniosamente **por encima** de `NotiFastBottomNav` y de la barra de navegación del sistema, erradicando que aparezcan detrás de la barra.
   - **Mapeo y Cancelación Silenciosa:** Detección de cancelaciones voluntarias en `ErrorSanitizer.isCancellation` (código 16 de Google Play Services, excepciones de descarte de Credential Manager), impidiendo que se muestren alertas espurias al cancelar cambios de cuenta o inicio de sesión.
   - **Reemplazo Completo de `Toast.makeText`:** 100% de los `Toast.makeText` de Android en `ScheduleEditorBottomSheet`, `EvaluationEditorBottomSheet`, `TeacherContactSection`, `NoteEstheticCard`, `SubjectSchedulesTab`, `SettingsSectionsContent`, `SchedulesScreen`, `QrCodeShareDialog`, `ShareSubjectBottomSheet` e `ImportSubjectBottomSheet` reemplazados por alertas papercraft AAA con `rememberNotiFastAlert()`.
   - **Formularios con Chips de Validación:** Reemplazo de textos rojos crudos por `NotiFastAlertChip` en `EvaluationEditorAddForm`, `EvaluationEditorEditForm` y `EditSubjectDialog`.
   - **Normalización de Chips de Selección:** Reemplazo de componentes Android genéricos por `NotiFastChip` en `NotiFastGradePresetChips`, `NotesFilterBar`, `SubjectNotesTab`, `AddEditTeacherBottomSheet` y `NoteTagsEditor`.
   - **Arquitectura Limpia y Modularidad:** Extracción de componentes y cumplimiento estricto del límite $\le 250$ LOC en todos los composables.
   - **Recursos de Cero Strings Hardcodeados:** Centralización de identificadores de error, prefijos de accesibilidad (`alert_severity_*_prefix`, `alert_dismiss_cd`) y etiquetas en `res/values/strings.xml`.
4. **Verificación y Cobertura:**
   - **614 pruebas unitarias ejecutadas y aprobadas al 100%** (0 fallos, 0 ignorados).
   - Compilación exitosa de build debug e instalación directa en dispositivo físico conectado (`TK6TVCRSMB7DPVAQ`).

---

## ☁️ Reingeniería Visual y Elevación AAA del Cloud Hub (Google Drive) — 100% Completado (16 Oct 2026)

**Objetivo:** Optimizar integralmente la experiencia visual, háptica, tipográfica y de rendimiento de la Copia de Seguridad en Google Drive (`CloudHubSheet`), consolidando la paleta corporativa AAA (Ruby reservado para errores/conflictos, Aqua para sincronización primaria, Emerald para estados al día, Amber para pendientes), eliminando redundancias cognitivas, e introduciendo filtros rápidos táctiles y micro-insignias compactas del desglose de datos a 120 FPS.

### Entregables Implementados:
1. **Jerarquía Visual, Tipografía y Paleta Corporativa AAA:**
   - Contraste WCAG AAA riguroso en Modo Claro y Oscuro.
   - **Ruby (`#BF092F` / `colorScheme.error`):** Restringido estrictamente a fallos críticos de red/autenticación y conflictos de versión (`BOTH_UNLINKED`, `isError`).
   - **Aqua (`#3B9797` / `colorScheme.secondary`):** Consolidado como acción primaria de sincronización ("Subir a la nube / Sincronizar ahora") y badges de copias en la nube.
   - **Emerald / ForestSage (`#15803D` / `#34D399`):** Estado 100% al día (`SYNCED`).
   - **Amber / Sunset (`#B45309` / `#FBBF24`):** Estados pendientes con cambios locales no subidos (`LOCAL_CHANGES`, `LOCAL_ONLY`, `CLOUD_NEWER`).
   - **Tipografía `JetBrainsMono`:** Tokens oficiales `monoAcademicBold` y `monoAcademicSmall` para marcas de tiempo, tamaños de archivo (KB/MB) y métricas de procesamiento.
2. **Rediseño Táctil de Filas (`CloudSubjectRow`) y Rendimiento 120 FPS:**
   - Estética papercraft táctil (`Surface` con borde adaptativo, esquinas redondeadas de 16dp y elevación suave).
   - Indicador dinámico y accesible del color de la asignatura mediante `ThemeUtils.getAccessibleColor(item.colorHex, isDark)`.
   - Micro-insignias compactas del desglose de datos (`DataBreakdownBadges`): conteo de apuntes (`X ap.`), evaluaciones (`Y eval.`), horarios (`Z hor.`) y peso en disco (`XX.X KB`).
   - Erradicación total de textos redundantes ("Sincronizado con Drive").
   - Barra de filtrado táctil rápido con `NotiFastChip` (`CloudFilterBar`: "Todas", "Pendientes", "Al día", "Conflictos").
   - Claves estables `key = { it.subjectId }` en `LazyColumn`, estados derivados con `remember { derivedStateOf { ... } }`, y animaciones de resorte fluidas a 120 FPS (`animateItem`).
3. **Micro-Interacciones y Feedback Inmersivo:**
   - Feedback visual inmediato durante el escaneo diferencial SHA-256 de metadatos mediante `CloudScanningSkeletonCard` con `rememberShimmerBrush()` y animación de rotación suave.
   - Micro-animaciones con resortes táctiles (`spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)`).
   - Respuesta táctil háptica (`LocalHapticFeedback`) al presionar "Subir a la nube", "Restaurar", alternar selección de materias y confirmar resolución de conflictos.
   - Enlace al sistema global `LocalNotiFastSnackbarHostState` para desplegar alertas flotantes con `NotiFastAlertBanner` por encima de la barra de navegación.
4. **Higiene de Código y Modularidad:**
   - 100% de los composables cumplen con el límite estricto de $\le 250$ LOC (`CloudHubSheet` en 245 LOC, `CloudSubjectRow` en 204 LOC, `CloudSummaryCard` en 194 LOC, `GlobalSettingsRow` en 159 LOC, `CloudScanningSkeletonCard` en 122 LOC, `DataBreakdownBadges` en 120 LOC, `CloudSheetBanners` en 94 LOC, `CloudSubjectList` en 80 LOC, `CloudFilterBar` en 69 LOC, `CloudSheetHeader` en 47 LOC).
   - Cero strings quemados: 100% de textos y badges centralizados en `res/values/strings.xml`.
   - Clean Architecture y MVI estricto.
5. **Apego Riguroso al Tema Activo de la App y Calibración Tipográfica:**
   - Erradicación de `isSystemInDarkTheme()` en favor de `MaterialTheme.colorScheme.isDark` (`background.luminance() < 0.5f`), garantizando que todos los elementos se apeguen al tema seleccionado por el usuario en la app (Claro u Oscuro) independientemente de la configuración global de Android.
   - Corrección de contraste en marcas de tiempo ("Último registro"): sustitución de `colorScheme.outline` por `colorScheme.onSurfaceVariant` con `monoAcademicSmall` (11.5sp, Medium), alcanzando ratio de contraste WCAG AAA > 6:1 en fondo claro y > 8:1 en oscuro.
   - Calibración de textos de estado local vacío (`cloud_subject_empty_local`) y tamaños de archivos para legibilidad óptima.
6. **Verificación Basada en Evidencia:**
   - **615 pruebas unitarias automatizadas aprobadas al 100%** (0 fallos, 0 ignorados).
   - Compilación exitosa de build debug (`./gradlew assembleDebug`).
   - Instalación y validación directa en dispositivo físico (`adb install -r app/build/outputs/apk/debug/app-debug.apk` en `TK6TVCRSMB7DPVAQ`).

---

## 🎓 Elevación AAA del Hub de Calificaciones y Analíticas Académicas — 100% Completado (16 Oct 2026)

**Objetivo:** Elevar a estándar AAA la experiencia de Calificaciones (`GradesScreen`), erradicando insets duplicados bajo el topbar, corrigiendo la inversión de colores semánticos, reestructurando las analíticas desde la perspectiva de estudiantes de secundaria y universidad (reemplazando métricas abstractas y sobrecargadas por el Simulador de Calificaciones Interactivo en tiempo real y la lista de salud académica scannable), y garantizando precisión matemática y estados vacíos empáticos.

### Entregables Implementados:
1. **Erradicación de Insets Duplicados y Espacio en Blanco Bajo el TopBar:**
   - Aislamiento de causa raíz mediante Systematic Debugging: `Scaffold(innerPadding)` ya gestionaba los insets de la barra de estado inyectados por `NotiFastTopBar`. El modificador `.safeDrawingPadding()` en el contenedor raíz duplicaba el espaciado empujando y cortando el contenido visual.
   - Eliminación del modificador redundante en `GradesScreen.kt`. Alineación perfecta de la cuadrícula base con 16dp de padding uniforme y 20dp de márgenes laterales.
2. **Corrección de Inversión Semántica de Colores y Apego al Tema de la App:**
   - **Verde / Tertiary / ForestSage (`#15803D` / `#34D399`):** Consolidado rigurosamente para notas aprobatorias, proyección al día y ritmos excelentes (`analytics_pulse_all_good`).
   - **Rojo / Ruby / Error (`#BF092F`):** Restringido exclusivamente a notas reprobadas, materias en riesgo crítico y fallos.
   - **Apego al Tema Activo:** Sustitución de `isSystemInDarkTheme()` por `MaterialTheme.colorScheme.isDark` (`background.luminance() < 0.5f`), garantizando que tanto `SubjectGradeCard` como los componentes de analíticas respeten el tema claro/oscuro interno de NotiFast sin depender del sistema Android.
   - **Sanitización de Contraste:** Sustitución de `outline` por `onSurfaceVariant` en estados sin pendientes del simulador, erradicando textos lavados.
3. **Reestructuración de Analíticas Basada en Casos de Uso Reales de Estudiantes:**
   - **Eliminación de Ruido Abstracto:** Se eliminaron el histograma gaussiano de 4 bandas y la correlación horas/nota semanal, identificados como sobrecarga cognitiva poco útil para el estudiante.
   - **Simulador Interactivo Directo (`IntegratedGradeSimulatorCard`):** Integración del simulador de notas en la pestaña principal de Analíticas, permitiendo alternar materias mediante chips táctiles horizontales y ajustar la nota meta en tiempo real para responder a la pregunta fundamental: *«¿Cuánto necesito sacar en lo que falta para aprobar?»*.
   - **Lista Visual de Salud Académica (`SubjectHealthListCard`):** Sustitución de gráficos de barras abstractos en Canvas por filas legibles y accesibles con icono de asignatura, nombre completo, puntos acumulados sobre la nota meta, insignias semánticas ("Al día", "En riesgo", "Sin notas") y tracks de progreso con contraste WCAG AAA.
4. **Corrección Matemática y UX Empática para Materias sin Evaluaciones:**
   - **Aislamiento de Causa Raíz en `CalculateAcademicStatsUseCase`:** Materias recién creadas con 0 evaluaciones generaban división entre 0 en `remainingWeight`, arrojando `minGradeNeeded = Double.POSITIVE_INFINITY` y marcando falsamente `CRITICAL_FAIL` con 0% de proyección en rojo.
   - **Solución Algorítmica:** Si una materia no tiene evaluaciones (`!hasEvaluated && remainingWeight <= 0.0`), se clasifica de forma justa como `ON_TRACK` con 100% de proyección aprobatoria y badge "Sin notas".
   - **Manejo de Promedio Nulo:** En `GradesViewModel`, `MainAverageDashboard` y `SubjectGradeCard`, si una materia o el estudiante no tienen notas aún, se presenta `—` en tipografía monoespaciada con tono neutro y el mensaje orientativo *"Registra notas en tus evaluaciones para calcular tu promedio"*, eliminando el `0.0` rojo alarmante.
   - **Inclusión Universal de Materias:** Todas las materias creadas en la app se muestran en la pestaña "Materias" con su tarjeta expandible y animada, permitiendo al estudiante ver su portafolio académico completo.
5. **Estandarización al Design System 3.0 (Papercraft Táctil) y Modularidad:**
   - Migración de tarjetas genéricas `Card` a `NotiFastCard` con micro-borde de 1dp `outlineVariant`, esquinas redondeadas de 16-20dp y elevación 0dp.
   - Selector táctil tipo píldora (`GradesTabSelector`) con transiciones de resorte (`spring`) y respuesta háptica (`LocalHapticFeedback`).
   - Micro-fichas de evaluación expandibles (`EvaluationDetailRow`) con chip monoespaciado (`JetBrainsMonoFamily`) e indicador de estado ("Evaluada" / "Pendiente").
   - Límite estricto de $\le 250$ LOC en todos los composables.
   - Cero strings quemados: 100% de cadenas extraídas en `res/values/strings.xml`.
6. **Verificación Basada en Evidencia:**
   - **614 tests unitarios automatizados pasando al 100%** (0 fallos, 0 errores, 0 ignorados).
   - Compilación exitosa (`./gradlew assembleDebug`).
   - Instalación y validación táctil y visual en dispositivo físico `TK6TVCRSMB7DPVAQ`.

---

## 🎨 Sistema de Temas Personalizados y Paletas Académicas Dinámicas (Theme Customizer Engine AAA) — 100% Completado (16 Oct 2026)

**Objetivo:** Diseñar e integrar el motor de personalización de temas y paletas dinámicas desde la pantalla de Ajustes (`SettingsScreen`), permitiendo a cada estudiante personalizar su entorno visual con máxima comodidad, serenidad e identidad académica, garantizando contraste WCAG AAA tanto en Modo Claro como en Modo Oscuro, preservando el lenguaje táctil papercraft y manteniendo sincronización bidireccional y resiliente con la nube (`Google Drive appDataFolder`).

### Entregables Implementados:
1. **Biblioteca de Colección de Estudio (6 Temas Académicos Predefinidos):**
   - **NotiFast Classic:** Ruby Crimson (`#9E1B32` / `#E05A6E`) + Aqua Studio (`#2A7B7B` / `#38B2AC`).
   - **Forest Sage (Campus Verde):** Verde Esmeralda Profundo (`#15803D` / `#34D399`) + Cálido Trigo/Tierra (`#854D0E` / `#FBBF24`). Ideal para estudiantes de ciencias, lectura prolongada y reducción de fatiga ocular.
   - **Midnight Indigo (Cyber Nocturno):** Azul Índigo Eléctrico (`#4338CA` / `#818CF8`) + Turquesa Suave (`#0891B2` / `#22D3EE`). Diseñado para sesiones de estudio nocturnas y alta concentración.
   - **Warm Mocha (Café Nórdico):** Terracota & Caramelo Cálido (`#C2410C` / `#FB923C`) + Vainilla / Papiro (`#B45309` / `#FCD34D`). Enfoque acogedor tipo biblioteca clásica.
   - **Sakura Zen (Lavanda & Pastel):** Lavanda Suave (`#6D28D9` / `#A78BFA`) + Rosa Cuarzo (`#BE185D` / `#F472B6`). Entorno visual sereno para reducir la ansiedad académica.
   - **Ocean Cyan (Minimalista Nórdico):** Azul Océano Profundo (`#0284C7` / `#38BDF8`) + Aqua Suave (`#0F766E` / `#2DD4BF`). Estilo limpio, moderno y estructurado.
2. **Motor Algorítmico Puro de Esquemas Material 3 (`ThemePaletteGenerator`):**
   - Función algorítmica pura que, a partir de cualquier color primario base (predefinido o personalizado por el estudiante), calcula dinámicamente los roles Material 3 (`primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondary`, `tertiary`).
   - **Calibración Dinámica de Luminancia y Contraste WCAG AAA:** En Modo Claro, si la luminancia sRGB relativa del color es elevada, ajusta iterativamente la luminosidad (`pLight -= 0.02f`) garantizando ratio de contraste $\ge 4.5:1$ con respecto a `onPrimary` (Blanco) y preservando legibilidad sobre superficies claras. En Modo Oscuro, ajusta la luminosidad (`pLight += 0.02f`) para garantizar ratio $\ge 4.5:1$ con respecto a fondos oscuros.
   - **Preservación Estricta de Superficies NotiFast 3.0 Papercraft:** Para el tema `CLASSIC` y cualquier tema derivado o custom, se preservan los tokens táctiles papercraft de superficie y bordes (`BackgroundLight`, `SurfaceLight`, `SurfaceContainerLight`, `OutlineVariantLight`, etc.).
3. **Estudio de Personalización Libre del Estudiante (`CustomThemePaletteSheet` & `ThemePreviewMiniCard`):**
   - Modal bottom sheet con cuadrícula táctil de 10 colores base académicos cuidadosamente balanceados.
   - **Previsualizador Táctil en Vivo (`ThemePreviewMiniCard`):** Micro-maqueta interactiva que renderiza en tiempo real cómo lucirá el tema (tarjeta `NotiFastCard`, botón `NotiFastButton`, insignia de evaluación y título de asignatura) antes de confirmar y aplicar los cambios.
4. **Persistencia Local y Resiliencia Cloud (Google Drive `appDataFolder`):**
   - Persistencia inmediata y reactiva en Jetpack DataStore mediante `UserPrefsRepository` (`theme_preset_id`, `custom_theme_primary_hex`).
   - Integración nativa en `GlobalSnapshot.prefs` para copias de seguridad en Google Drive. Serialización y deserialización con fallback seguro: si se restaura un backup antiguo sin claves de tema, asigna de forma transparente `CLASSIC / SYSTEM` sin arrojar `NullPointerException` ni alterar notas u horarios existentes.
5. **UI/UX en Pantalla de Ajustes (`SettingsScreen`):**
   - Tarjeta principal `ThemeSelectorCard` integrada en la sección "Apariencia" de Ajustes:
     - Selector de modo de luminancia por segmentos (`Sistema`, `Claro`, `Oscuro`).
     - Fila horizontal con desplazamiento de muestras circulares (swatches) con gradientes de color primario/secundario y animación de resorte táctil (`spring`).
     - Micro-insignia de check animada sobre el tema activo.
     - Botón de acceso al Estudio de Personalización con icono y gradiente de color.
     - Banner descriptivo contextual del tema activo.
     - Respuesta háptica (`LocalHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)`).
6. **Tonalidades de Fondo Independientes de Alto Contraste (`BackgroundPreset`):**
   - 6 paletas de fondo desacopladas del color de énfasis, adaptables a Modo Claro y Oscuro con contraste WCAG AAA ($\ge 4.5:1$ y $\ge 14:1$ en texto principal):
     - **Classic:** Lienzo pergamino neutral (`#FAF9F6`) / Pizarra oscura (`#0E131F`).
     - **Forest Sage:** Apuntes botánicos / Sage linen (`#EAF4ED`) / Bosque pino oscuro (`#0A1610`).
     - **Midnight Indigo:** Polar / Papel ártico (`#E6F0F8`) / Índigo cósmico (`#09101D`).
     - **Warm Mocha:** Papiro clásico / Café nórdico (`#F5ECE1`) / Espresso tostado (`#150F0B`).
     - **Sakura Zen:** Lavanda serena / Niebla lila (`#F2EAF7`) / Violeta crepúsculo (`#130B1C`).
     - **Ocean Cyan:** Brisa marina / Seafoam (`#E5F3F5`) / Abismo marino (`#061418`).
   - Tokens dinámicos de alto contraste: `background`, `surface`, `surfaceContainer`, `surfaceContainerHigh`, `surfaceVariant`, `outlineVariant`, `onBackground`, `onSurface`, `onSurfaceVariant`.
   - **Muestra Táctil Mini-Lienzo (`BackgroundSwatchItem`):** Squircle con previsualización en miniatura del lienzo (`bgColor`), ficha interior elevada (`surfaceContainer`), micro-borde nítido (`outlineVariant`) y pip cromático de identidad (`accentPipHex`).
   - **Cero Solapamiento de Cards:** Estructura Bento aplanada eliminando doble anidamiento de cards redundantes y asegurando padding armónico sin clipping horizontal.
7. **Higiene de Código, Clean Architecture y Modularidad:**
   - **Límite Estricto $\le 250$ LOC Cumplido:** `BackgroundSwatchItems.kt` (158 LOC), `ThemeSelectorCard.kt` (222 LOC), `SettingsSectionsContent.kt` (234 LOC), `BackgroundPreset.kt` (204 LOC), `CustomThemePaletteSheet.kt` (198 LOC), `ThemePreviewMiniCard.kt` (133 LOC).
   - **Cero Strings Quemados:** 100% de títulos, nombres de temas, descripciones y etiquetas accesibles centralizados en `res/values/strings.xml` en español.
   - **Cero Lógica en UI:** Estado MVI centralizado en un único StateFlow (`SettingsUiState`), interacción mediante eventos sellados (`SettingsEvent`), y lógica cromática pura en dominio/presentación.
8. **Verificación Basada en Evidencia:**
   - **645 pruebas unitarias automatizadas ejecutadas y aprobadas al 100%** (0 fallos, 0 errores, 0 ignorados), incluyendo tests de dominio (`ThemePresetTest`, `BackgroundPresetTest`), generador de color (`ThemePaletteGeneratorTest`), repositorio (`UserPrefsRepositoryTest`), casos de uso cloud (`BuildCloudOverviewUseCaseTest`) y ViewModel (`SettingsViewModelTest`).
   - Compilación exitosa de build debug (`./gradlew assembleDebug`).

---

### ⚪ Fase 4: Pulido, Experiencia, Accesibilidad y Rendimiento (Siguiente Fase)
*Meta: Micro-interacciones avanzadas, accesibilidad total (a11y), transiciones de pantalla con View Transitions en Compose, auditoría de rendimiento y depuración de memoria.*  
*Guía de Arranque:* [`docs/prd/KICKOFF_FASE_4.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_4.md)

---

### ⚪ Fase 5: Despliegue, Métricas de Tesis y Lanzamiento en Play Store (Pendiente)
*Meta: Testing en aula real, recolección de métricas de tesis y publicación en Google Play Store.*
