# 🚀 Kickoff Oficial — Fase 2.1: Experiencia sin Fricción y Ergonomía de Formularios

> **Propósito:** Guía maestra de arranque y prompt copiable de sesión para ejecutar la **Fase 2.1** de NotiFast v2.0, enfocada en la eliminación de fricción, reducción drástica de la carga cognitiva y rediseño ergonómico de los formularios de creación/edición (Materias, Horarios, Apuntes y Evaluaciones).  
> **Referencias Obligatorias:** [`PRD-MAESTRO-NOTIFAST.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md), [`STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md), [`AGENTS.md`](file:///home/p0xd/Documentos/NotiFast/AGENTS.md) y [`DESIGN.md`](file:///home/p0xd/Documentos/NotiFast/DESIGN.md).

---

## 📋 1. Prompt Copiable para Iniciar la Sesión de Fase 2.1

Copia y pega el siguiente bloque para iniciar de inmediato el desarrollo de la Fase 2.1 en una nueva sesión:

```text
Actúa como Ingeniero de Software IA de Élite y Diseñador UX/UI de Producto de NotiFast. Vamos a ejecutar al 100% la Fase 2.1: "Experiencia sin Fricción y Ergonomía de Formularios", siguiendo estrictamente las especificaciones de docs/prd/KICKOFF_FASE_2.1.md, docs/prd/PRD-MAESTRO-NOTIFAST.md, docs/prd/STATUS.md, DESIGN.md y AGENTS.md.

Skills obligatorias requeridas:
- `impeccable`: Aplicación estricta de las directivas de diseño `clarify`, `distill`, `layout`, `harden`, `critique` y `polish` para erradicar la sobrecarga cognitiva en formularios.
- `systematic-debugging`: Aislamiento de causa raíz y trazabilidad ante cualquier fallo de estado o navegación.
- `test-driven-development`: Red ➔ Green ➔ Refactor para todas las validaciones de formulario y ViewModels.
- `verification-before-completion`: Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` antes de dar por terminada cualquier tarea.
- `codegraph`: Navegación estructural mediante `codegraph_explore`.

Estándares de higiene y diseño no negociables:
1. Máximo ~250 líneas por archivo composable; modularizar campos y selectores en `components/`.
2. Erradicar acordeones de pasos obligatorios que oculten campos; usar formularios continuos y limpios tipo One-Page.
3. Cero inputs de texto crudo para horas/fechas: usar `Material 3 TimePicker` y `DatePicker` nativos con selector de chips rápidos.
4. Cero dropdowns toscos cuando los chips o barras segmentadas horizontales permitan selección a 1 solo toque.
5. Cero strings o colores quemados (100% `res/values/strings.xml`, `Ruby` #BF092F, `Aqua` #3B9797 y tema M3).
6. Un solo StateFlow inmutable `uiState` por ViewModel y flujo MVI `onEvent()`.

Plan de Trabajo de Fase 2.1 a ejecutar:
- Sprint 2.1.1 — Formulario Fluido de Materias (AddSubjectScreen & EditSubjectScreen sin acordeones, con vista previa viva y cuadrícula compacta de color/icono).
- Sprint 2.1.2 — Ergonomía de Horarios (AddScheduleScreen & EditScheduleScreen con selector segmentado de días L-D, Material 3 TimePicker nativo y chips de materia a 1 toque).
- Sprint 2.1.3 — Simplificación de Apuntes y Evaluaciones (AddNoteScreen con carrusel superior de materias y tags rápidos; AddEvaluationScreen con formulario continuo, selector de tipo de evaluación y slider de ponderación).
- Protocolo de Cierre: Ejecutar suite de pruebas completa, compilar APK debug, actualizar docs/prd/STATUS.md e integrar en la rama main.

Comienza analizando los componentes actuales con codegraph y ejecutando el Sprint 2.1.1 con TDD.
```

---

## 🎯 2. Objetivos y Diagnóstico Heurístico (`critique` & `clarify`)

El objetivo de la **Fase 2.1** es transformar los formularios de entrada de datos de NotiFast en una experiencia ágil, intuitiva y placentera para el estudiante mediante:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           MATRIZ DE TRANSFORMACIÓN UX                           │
├──────────────────────────┬─────────────────────────────┬────────────────────────┤
│ Formulario               │ Fricción Actual             │ Solución Fase 2.1      │
├──────────────────────────┼─────────────────────────────┼────────────────────────┤
│ 1. Crear/Editar Materia  │ Acordeón de 2 pasos forzado │ One-Page Form continuo │
│ 2. Crear/Editar Horario  │ Dropdowns e input crudo     │ Segmented Bar + Time   │
│                          │ de texto para horas         │ Picker de Material 3   │
│ 3. Crear/Editar Apunte   │ Dropdown y tags lentos      │ Chips materia + Tags 1 │
│ 4. Crear Evaluación      │ Acordeón de 3 pasos pesado  │ Tarjeta continua tipo  │
│                          │ con campos ocultos          │ One-Page + Sliders %   │
└──────────────────────────┴─────────────────────────────┴────────────────────────┘
```

---

## 🛠️ 3. Desglose Detallado de Tareas y Criterios de Aceptación

### 🔹 Sprint 2.1.1 — Formulario Fluido de Materias

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **2.1.1.1** | `AddSubjectScreen.kt` | Eliminar acordeón de pasos. Presentar formulario continuo: nombre de materia en campo principal, selector compacto de paleta de 12 colores con selección visual y cuadrícula de iconos temáticos. |
| **2.1.1.2** | `SubjectPreviewBadge.kt` | Sub-componente interactivo en `components/` que actualiza en tiempo real el icono y color con animación `animateColorAsState`. |
| **2.1.1.3** | `AddSubjectViewModelTest.kt` | Tests unitarios para validación de nombres vacíos, selección de colores e iconos y persistencia. |

---

### 🔹 Sprint 2.1.2 — Ergonomía en Horarios

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **2.1.2.1** | `ScheduleDaySegmentedBar.kt` | Barra horizontal de selección de días a 1 toque (`L`, `M`, `M`, `J`, `V`, `S`, `D`) con animación de selección. |
| **2.1.2.2** | `ScheduleTimeRangePicker.kt` | Selector interactivo de horas con diálogo `Material 3 TimePicker` y chips de rangos rápidos ("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "Personalizado"). |
| **2.1.2.3** | `ScheduleSubjectChips.kt` | Selector horizontal con chips de materias registradas para asignación instantánea sin dropdowns. |
| **2.1.2.4** | `AddScheduleScreen.kt` & `EditScheduleScreen.kt` | Integración de los nuevos selectores ergonómicos, manteniendo el banner dinámico de detección de solapamientos en vivo. |

---

### 🔹 Sprint 2.1.3 — Simplificación de Apuntes y Evaluaciones

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **2.1.3.1** | `AddNoteScreen.kt` & `EditNoteScreen.kt` | Reemplazar dropdown de materias por carrusel superior con chips de materias; etiquetas rápidas predefinidas a 1 toque (`#resumen`, `#examen`, `#fórmulas`, `#tarea`). |
| **2.1.3.2** | `AddEvaluationScreen.kt` | Eliminar acordeón de 3 pasos. Crear formulario continuo con selector visual de tipo de evaluación (`Examen`, `Taller`, `Quiz`, `Proyecto`), selector de fecha accesible y slider de ponderación (%) de 0 a 100%. |
| **2.1.3.3** | Tests de Integración UX | Pruebas unitarias para `AddNoteViewModelTest`, `EditNoteViewModelTest`, `AddEvaluationViewModelTest`. |

---

## 🚫 4. Buenas Prácticas y Anti-Patrones Críticos para Fase 2.1

| # | Anti-Patrón Prohibido | Práctica Correcta Obligatoria |
|---|---|---|
| 1 | Forzar acordeones y pasos en formularios cortos | Usar formularios de 1 sola página con jerarquía visual clara. |
| 2 | Solicitar horas o fechas como texto libre | Usar `TimePicker` y `DatePicker` nativos de Material 3 con validación automática. |
| 3 | Usar dropdown menus para listas pequeñas (< 7 opciones) | Usar barras segmentadas horizontales o chips a 1 toque. |
| 4 | Archivos composable > 250 líneas | Modularizar cada selector a `presentation/feature/{nombre}/components/`. |
| 5 | Perder cambios accidentales | Mantener siempre el diálogo seguro `NotiFastDialog` de confirmación al presionar "Atrás". |

---

## 🔄 5. Protocolo de Transición y Cierre de Fase 2.1

Al completar el 100% de las tareas de la Fase 2.1:
1. Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.
2. Instalar y verificar en dispositivo físico Android vía ADB (`adb install -r app-debug.apk`).
3. Actualizar [`docs/prd/STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md).
4. Hacer merge limpio de los cambios en la rama `main`.
