# 🚀 Kickoff Maestro y Guía de Inicio — Fase 2: Experiencia Core (Cuaderno y Hub Académico)

> **Propósito:** Documento de arranque de máxima rigurosidad técnica y de diseño UI/UX para la Fase 2 de NotiFast v2.0.  
> **Ubicación:** `docs/prd/KICKOFF_FASE_2.md`  
> **Referencias Obligatorias:**
> - [PRD Maestro v2.0](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)
> - [Estado del Roadmap (STATUS.md)](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md)
> - [Guía Maestra para Agentes (AGENTS.md)](file:///home/p0xd/Documentos/NotiFast/AGENTS.md)
> - [Design System (DESIGN.md)](file:///home/p0xd/Documentos/NotiFast/DESIGN.md) y [Contexto de Producto (PRODUCT.md)](file:///home/p0xd/Documentos/NotiFast/PRODUCT.md)

---

## 📋 Prompt Copiable para Iniciar la Sesión de Fase 2

```markdown
Hola. Vamos a ejecutar la **Fase 2: Experiencia Core — Cuaderno y Hub Académico** de NotiFast v2.0 bajo el estándar de ingeniería y diseño UI/UX más riguroso del repositorio.

Por favor, interioriza y sigue estrictamente las siguientes directivas antes de redactar código:

1. 📚 **Contexto y Fundamentos:**
   - Asimila los documentos `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md`, `AGENTS.md`, `DESIGN.md` y `PRODUCT.md`.
   - Consulta `docs/prd/KICKOFF_FASE_2.md` como la hoja de ruta técnica y de diseño maestro.

2. 🧠 **Proceso de Ingeniería y Skills Obligatorias:**
   - `impeccable`: Aplicación exhaustiva del toolkit de diseño en cada vista (`shape`, `distill`, `clarify`, `harden`, `critique`, `audit`, `polish`).
   - `/grill-me`: Ejecutar rondas de alineación interactiva antes de programar cada vista compleja para validar decisiones de layout, flujo y ergonomía.
   - `test-driven-development`: Ciclo estricto Red ➔ Green ➔ Refactor antes de implementar Use Cases y algoritmos matemáticos.
   - `systematic-debugging`: Hipótesis y aislamiento de causa raíz ante cualquier anomalía de renderizado o estado.
   - `verification-before-completion`: Compilación reproducible con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` con evidencia antes de afirmaciones.
   - `codegraph`: Navegación estructural quirúrgica mediante `codegraph_explore` (cero barridos ciegos con grep/find).

3. 🎨 **Estándar de Calidad de Código, Higiene y UI/UX (Zero AI-Slop):**
   - **KISS & YAGNI:** Resolver exactamente la necesidad actual sin sobre-ingeniería.
   - **Límite de Tamaño:** Máximo ~250 líneas por archivo composable. Modularizar en `components/`.
   - **Modo Operate:** Interfaz estructurada, de máxima scanabilidad, jerarquía clara y sin fricción cognitiva.
   - **Tokens Impeccable:** Usar `MaterialTheme.colorScheme` (`Ruby` `#BF092F` y `Aqua` `#3B9797`) y contrastes HSL dinámicos (`ThemeUtils.getAccessibleColor`).
   - **Cero Strings Hardcodeados:** Todo texto de UI debe residir en `res/values/strings.xml`.
   - **Clean Architecture & MVI:** Lógica de negocio en Use Cases (`domain/usecase/`), repositorios retornando `Result<T>` y un único `UiState` por ViewModel.

4. 📅 **Desglose de Ejecución por Sprints y Vistas:**
   - **Sprint 2.1 (Gestión Integral de Horarios Académicos):**
     * TDD: `ValidateScheduleOverlapUseCase` (detección matemática de conflictos horarios).
     * UI/UX: `SchedulesScreen` (cuadrícula/bloques semanales limpios, `distill` de ruido visual).
     * Formularios: `AddScheduleScreen` y `EditScheduleScreen` (time pickers accesibles, descarte seguro `NotiFastDialog`).
     * Tests: Suite unitaria completa para validación de horarios y `SchedulesViewModel`.
   - **Sprint 2.2 (Cuaderno Inteligente y Editor de Apuntes):**
     * TDD: Parser seguro de Markdown y filtrado multivariable por etiquetas (`NoteTag`).
     * UI/UX: `AddNoteScreen` y `EditNoteScreen` con toolbar contextual de formato (`#`, `*`, `**B**`, `_I_`), selector de tags temáticos (`NotiFastChip`) y toggle edición/preview fluido (`shape`, `clarify`, `delight`).
     * UI/UX: `NotesScreen` con búsqueda instantánea por tags y contenido (`layout`, `typeset`).
     * Tests: Unit tests para parsing de Markdown, gestión de tags y `AddNoteViewModel`/`EditNoteViewModel`.
   - **Sprint 2.3 (Hub de Calificaciones y Simulador Académico):**
     * TDD: `CalculateRequiredGradeUseCase` (cálculo proactivo de notas requeridas para aprobar).
     * UI/UX: `GradeSimulatorCard` en `SubjectDetailScreen` con semáforo de viabilidad (Aqua = alcanzable, Ruby = en riesgo/imposible) y sliders reactivos (`colorize`, `clarify`, `critique`).
     * Configuración: Soporte de escalas de calificación (0-10, 0-20, 0-100, Letras) en `UserPrefsRepository`.
     * Tests: Unit tests para cálculo de promedios proyectados y escalas.

5. 🔍 **Auditoría Maestra de Cierre de Fase 2:**
   - Ejecutar `/impeccable critique` y `/impeccable audit` sobre todas las pantallas de la fase.
   - Validar suite de pruebas completas y compilar APK final (`./gradlew assembleDebug`).
   - Actualizar `docs/prd/STATUS.md` al 100% de Fase 2 y generar automáticamente `docs/prd/KICKOFF_FASE_3.md`.
```

---

## 🎨 Protocolo de Diseño & UX Exhaustivo con Impeccable por Pantalla

Para cada pantalla construida o refactorizada en la Fase 2, se debe seguir este flujo disciplinado:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. ALINEACIÓN PREVIA (/grill-me & shape)                                    │
│    Cuestionar supuestos de ergonomía, jerarquía y accesibilidad en el chat  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. REFINAMIENTO Y ESENCIA (distill & clarify)                               │
│    Eliminar ruido visual, cards redundantes y ambigüedades en textos de UI  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. CONSTRUCCIÓN MODULAR (≤ 250 LOC & Tokens)                                │
│    Implementar composables desacoplados con tokens de Color y ThemeUtils    │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. RESILIENCIA Y ACCESIBILIDAD (harden & audit)                             │
│    Validar estados de error, strings en XML, contraste WCAG AA y rotación   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5. AUDITORÍA HEURÍSTICA FINAL (critique & polish)                           │
│    Evaluar scanabilidad, micro-animaciones fluidas y feedback táctil        │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🧭 Matriz de Comandos Impeccable y Rondas `/grill-me` por Vista

### 📅 Sprint 2.1 — Horarios Académicos

| Pantalla / Componente | Comandos Impeccable Aplicables | Ronda `/grill-me` (Puntos Clave de Decisión UX) |
|---|---|---|
| **`SchedulesScreen`** (Vista Semanal) | `/impeccable shape`<br>`/impeccable distill`<br>`/impeccable critique` | - ¿Vista de pestañas por día vs cuadrícula semanal continua?<br>- ¿Cómo representar visualmente un solapamiento sin saturar?<br>- ¿Qué nivel de detalle mostrar por clase (aula, docente, tiempo restante)? |
| **`AddScheduleScreen` / `EditScheduleScreen`** | `/impeccable clarify`<br>`/impeccable harden`<br>`/impeccable polish` | - ¿TimePicker estilo rueda o selector por bloques de 15/30 min?<br>- ¿Cómo advertir de un solapamiento antes de guardar sin bloquear si el usuario insiste? |

---

### 📝 Sprint 2.2 — Cuaderno Inteligente & Editor Markdown

| Pantalla / Componente | Comandos Impeccable Aplicables | Ronda `/grill-me` (Puntos Clave de Decisión UX) |
|---|---|---|
| **`AddNoteScreen` / `EditNoteScreen`** (Editor de Apuntes) | `/impeccable shape`<br>`/impeccable clarify`<br>`/impeccable delight`<br>`/impeccable harden` | - ¿Barra de formato flotante sobre el teclado o anclada al BottomBar?<br>- ¿Modo split de edición/preview o pestaña toggle suave?<br>- ¿Cómo insertar fotos intercaladas en el apunte sin romper el flujo de lectura? |
| **`NoteDetailView` / Previsualización Markdown** | `/impeccable typeset`<br>`/impeccable layout`<br>`/impeccable distill` | - Escala tipográfica para H1, H2, H3, citas y bloques de código.<br>- Renderizado de listas de tareas interactivas (`[ ]` / `[x]`). |
| **`NotesScreen`** (Tags y Búsqueda) | `/impeccable layout`<br>`/impeccable colorize`<br>`/impeccable critique` | - ¿Fila de chips de etiquetas con scroll horizontal o filtro desplegable?<br>- Búsqueda en vivo: ¿destacar palabras coincidentes en el texto del apunte? |

---

### 🎓 Sprint 2.3 — Hub de Calificaciones & Simulador de Promedios

| Pantalla / Componente | Comandos Impeccable Aplicables | Ronda `/grill-me` (Puntos Clave de Decisión UX) |
|---|---|---|
| **`GradeSimulatorCard`** (en `SubjectDetailScreen`) | `/impeccable shape`<br>`/impeccable colorize`<br>`/impeccable clarify` | - ¿Cómo presentar la pregunta "¿Cuánto necesito para aprobar?" de forma instantánea y motivadora?<br>- Semáforo de dificultad: ¿Aqua para metas alcanzables vs Ruby para metas matemáticamente imposibles? |
| **`GradesScreen`** (Dashboard Académico) | `/impeccable distill`<br>`/impeccable critique`<br>`/impeccable audit` | - ¿Cómo jerarquizar materias en riesgo crítico sobre materias con promedio seguro?<br>- Visualización de la escala configurable por país/institución (0-10, 0-20, 0-100, Letras). |

---

## 🛠️ Especificaciones Técnicas y Criterios de Aceptación

### 1. Sprint 2.1 — Horarios Académicos
- **`ValidateScheduleOverlapUseCase`:**
  - Entrada: `userId: String`, `newSchedule: Schedule`, `excludeScheduleId: String?`.
  - Salida: `ScheduleOverlapResult` (`NoOverlap`, `Conflict(conflictingSchedules: List<Schedule>)`).
  - Algoritmo: Comparación por día de la semana y rangos $[t_{\text{inicio}}, t_{\text{fin}}[$ en minutos desde las 00:00.
- **`SchedulesScreen` & Subcomponentes:**
  - `ScheduleDayTabs.kt`, `ScheduleTimeBlockList.kt`, `ScheduleConflictBanner.kt`.
  - Cero hardcoding de colores; badges de materias consumen `ThemeUtils.getAccessibleColor()`.

### 2. Sprint 2.2 — Cuaderno Inteligente & Editor Markdown
- **Motor de Renderizado Markdown:**
  - Soporte para: `# Título 1`, `## Título 2`, `**Negrita**`, `*Cursiva*`, `* Viñeta`, `- [ ] Checklist`, `> Cita`.
  - Transición fluida con `AnimatedContent` entre modo edición y modo lectura.
- **Sistema de Tags (`NoteTag`):**
  - Chips interactivos en `NotiFastChip` con selector dinámico en creación de apuntes.
  - Filtro combinado: `Materia` + `Tag` + `Búsqueda de texto`.

### 3. Sprint 2.3 — Hub de Calificaciones y Simulador
- **`CalculateRequiredGradeUseCase`:**
  - Fórmula:
    $$\text{Nota Requerida} = \frac{\text{Nota Aprobatoria} \times W_{\text{total}} - \sum (\text{Nota}_i \times W_i)}{W_{\text{pendiente}}}$$
  - Donde $W$ representa el peso o ponderación porcentual.
  - Manejo de casos de borde: si $W_{\text{pendiente}} = 0$, si la nota requerida $> \text{Escala Máxima}$ (imposible aprobar), o si la nota requerida $\le 0$ (ya aprobó matemáticamente).
- **Escalas Configurables en `UserPrefsRepository`:**
  - Enum `GradeScale` (`TEN_SCALE` [0.0 - 10.0], `TWENTY_SCALE` [0 - 20], `HUNDRED_SCALE` [0 - 100], `LETTER_SCALE` [A, B, C, D, F]).
  - Nota mínima aprobatoria configurable (ej. 6.0, 7.0 o 51%).

---

## 🔒 Reglas de Oro de Ejecución en Fase 2

1. **Ronda `/grill-me` Obligatoria:** Ante cualquier ambigüedad de UX en una vista, abre un diálogo breve y estructurado en el chat con opciones claras antes de programar.
2. **Clean Architecture Estricta:** Las matemáticas del simulador, el cálculo de solapamientos y el formateo de notas van en `domain/usecase/`. Cero lógica de cálculo en `@Composable`.
3. **Límite de Líneas:** Todo archivo composable que supere ~200 líneas debe subdividirse en `components/`.
4. **Pruebas Automatizadas TDD:** Redactar los tests de use cases y ViewModels antes del código de producción.
5. **Verificación Continua:** Cada sprint debe cerrar con compilación limpia (`./gradlew assembleDebug`) y 100% de tests unitarios pasando.
