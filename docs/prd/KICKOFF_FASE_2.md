# 🚀 Kickoff y Guía de Inicio — Fase 2: Experiencia Core — Cuaderno y Hub Académico

> **Propósito:** Documento de arranque rápido para la sesión de desarrollo de la Fase 2.  
> **Ubicación:** `docs/prd/KICKOFF_FASE_2.md`  
> **Referencias:**
> - [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)
> - [Estado del Roadmap](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md)
> - [Guía de Reglas de Agente (AGENTS.md)](file:///home/p0xd/Documentos/NotiFast/AGENTS.md)

---

## 📋 Prompt para Copiar y Pegar en la Siguiente Sesión

```markdown
Hola. Vamos a iniciar la ejecución de la **Fase 2: Experiencia Core — Cuaderno y Hub Académico** del PRD Maestro de NotiFast v2.0.

Por favor, sigue estrictamente las siguientes directivas antes de tocar cualquier archivo:
1. Lee y asimila los documentos en `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md` y `AGENTS.md`.
2. Activa y sigue los protocolos de las siguientes skills:
   - `test-driven-development` (ciclo Red-Green-Refactor: redactar tests que fallen antes de implementar Use Cases de horarios y notas).
   - `systematic-debugging` (aislar causa raíz con hipótesis ante fallos de renderizado o solapamientos).
   - `verification-before-completion` (compilar con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` antes de dar por terminada una tarea).
   - `codegraph` (uso obligatorio de `codegraph_explore` para navegar símbolos y estructuras).
   - `impeccable` (diseño de interfaz de alta scanabilidad para la cuadrícula horaria y el editor de apuntes).
3. Aplica de forma obligatoria el estándar de **Calidad de Código y Limpieza de Anti-Patrones**:
   - **KISS & YAGNI:** Resolver estrictamente la necesidad actual sin sobre-ingeniería.
   - **Límite de tamaño:** Máximo ~250 líneas por archivo composable. Modularizar en `components/`.
   - **Tokens de Diseño:** Usar `MaterialTheme.colorScheme` (`Ruby` `#BF092F` y `Aqua` `#3B9797`) y átomos de `presentation/atoms/`.
   - **Cero Strings Quemados:** Todo texto de UI debe residir en `res/values/strings.xml`.
   - **Lógica de Negocio Pura:** Validaciones matemáticas y algoritmos de solapamiento van en `domain/usecase/`.
   - **Manejo de Errores:** Retorno de `Result<T>` en repositorios y feedback visual en `UiState`.
4. Ejecuta de forma secuencial y quirúrgica las tareas de la Fase 2:
   - **Sprint 2.1 (Gestión Completa de Horarios Académicos):**
     * 2.1.1: `ValidateScheduleOverlapUseCase` para detectar conflictos de horarios entre materias.
     * 2.1.2: `AddScheduleScreen` con selector de día, time picker (inicio/fin) y aula.
     * 2.1.3: `EditScheduleScreen` con edición reactiva y protección de descarte.
     * 2.1.4: Refactor visual de `SchedulesScreen` con visualización por bloques temporales limpios.
     * 2.1.5: Tests unitarios exhaustivos para validación de horarios y `SchedulesViewModel`.
   - **Sprint 2.2 (Cuaderno Inteligente y Editor de Apuntes):**
     * 2.2.1: Soporte de formato Markdown (negrita, cursiva, encabezados, viñetas) en apuntes.
     * 2.2.2: Modo preview/edición interactivo en `AddNoteScreen` y `EditNoteScreen`.
     * 2.2.3: Sistema de etiquetas / tags temáticos por apunte para filtrado rápido.
     * 2.2.4: Búsqueda avanzada por etiquetas y contenido.
     * 2.2.5: Tests unitarios para parser de Markdown y filtrado de notas.
   - **Sprint 2.3 (Hub de Calificaciones y Personalización):**
     * 2.3.1: Escalas de calificación configurables (0-10, 0-20, 0-100, Letras) en `UserPrefsRepository`.
     * 2.3.2: `CalculateRequiredGradeUseCase` para simular la nota necesaria para aprobar la materia.
     * 2.3.3: Integración de simulador de notas en `SubjectDetailScreen` y `GradesScreen`.
     * 2.3.4: Tests unitarios para cálculo de notas requeridas y escalas.
5. Tras completar cada tarea, ejecuta la verificación con `./gradlew testDebugUnitTest` y actualiza `docs/prd/STATUS.md`. Al completar el 100%, genera automáticamente `docs/prd/KICKOFF_FASE_3.md`.
```

---

## 🎨 Directivas de Diseño UI/UX con Impeccable

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              MODO OPERATE (NotiFast Fase 2)                            │
│  El estudiante gestiona sus clases semanales, edita apuntes y proyecta su promedio.  │
│  Prioridad: Visualización sin fricciones, feedback inmediato y cero saturación visual. │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

1. **Horarios en Bloques Semanales:**
   - Cada clase debe mostrar el badge de color de la materia, aula física y rango de hora con tipografía `titleSmall` legible.
   - Si existe conflicto de horarios, resaltar con borde sutil de alerta (`Ruby`) y texto explicativo claro.
2. **Editor de Apuntes Enriquecido:**
   - Barra de herramientas flotante o anclada al teclado con accesos rápidos: `#` (Encabezado), `*` (Viñeta), `**B**` (Negrita), `_I_` (Cursiva).
   - Transición fluida entre modo edición y modo previsualización sin saltos de scroll.
3. **Simulador de Promedios Académicos:**
   - Selector interactivo ("¿Cuánto necesito para sacar X?") con slider o input dinámico que actualiza en tiempo real la nota requerida en las evaluaciones pendientes.
   - Color semántico: Verde/Aqua si la nota requerida es alcanzable (≤ escala máxima), Rojo/Ruby si requiere puntuación superior a la escala máxima disponible.

---

## 🛠️ Matriz de Skills Requeridas en Fase 2

| Skill | Cuándo Usarla |
|---|---|
| `codegraph` | Al inicio de cada tarea para mapear modelos (`Schedule`, `Note`), ViewModels y repositorios. |
| `test-driven-development` | Antes de implementar `ValidateScheduleOverlapUseCase` y `CalculateRequiredGradeUseCase`. |
| `impeccable` | Al estructurar los componentes de UI en `SchedulesScreen` y el editor Markdown. |
| `systematic-debugging` | Ante cualquier anomalía en parseo de fechas, horas o renderizado de texto enriquecido. |
| `verification-before-completion` | Al finalizar cada sprint, ejecutando `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`. |

---

## 📋 Lista Detallada de Tareas y Criterios de Aceptación

### 📅 Sprint 2.1 — Gestión de Horarios
- **2.1.1 `ValidateScheduleOverlapUseCase`:**
  - Detectar si un nuevo bloque horario se cruza en día y rango temporal con un horario existente.
  - Soportar exclusión del horario que se está editando (`excludeScheduleId`).
- **2.1.2 `AddScheduleScreen` & `EditScheduleScreen`:**
  - Selector de materia, selector de día (Lunes a Domingo), campos de hora inicio y fin con validación `inicio < fin`.
  - Descarte seguro con `NotiFastDialog` y `BackHandler`.
  - Modularizados a ≤ 250 LOC por archivo.

### 📝 Sprint 2.2 — Cuaderno Inteligente
- **2.2.1 - 2.2.4 Formato Markdown & Tags:**
  - Renderizado de Markdown en notas con Compose `AnnotatedString` o componente nativo optimizado.
  - Tags en chips seleccionables (`NotiFastChip`).
  - Modularizados a ≤ 250 LOC por archivo.

### 🎓 Sprint 2.3 — Hub de Calificaciones y Simulador
- **2.3.1 `CalculateRequiredGradeUseCase`:**
  - Fórmula matemática: $\text{Nota Requerida} = \frac{\text{Nota Objetivo} \times 100 - \sum (\text{Nota}_i \times \text{Peso}_i)}{\sum \text{Peso Pendiente}}$.
  - Manejo de casos de borde: pesos totales distintos a 100%, todas las evaluaciones ya presentadas, notas negativas.
- **2.3.2 Simulador en `SubjectDetailScreen`:**
  - Componente `GradeSimulatorCard` interactivo y reactivo.

---

## 🔒 Reglas de Oro de Ejecución

1. **Clean Architecture Estricta:** Lógica de cálculo en `domain/usecase/`, estado en `UiState` único, composables puramente declarativos.
2. **Higiene de Código:** Cero archivos composable > 250 líneas. Extraer subcomponentes a `components/`.
3. **Internacionalización:** Todos los strings en `res/values/strings.xml` (Español).
4. **Verificación Continua:** Cada commit debe compilar limpiamente y pasar la suite de tests unitarios.
