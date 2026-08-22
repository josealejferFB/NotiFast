# 🚀 Kickoff y Guía de Inicio — Fase 1: Fundamentos de Arquitectura

> **Propósito:** Documento de arranque rápido para la sesión de desarrollo de la Fase 1.  
> **Ubicación:** `docs/prd/KICKOFF_FASE_1.md`  
> **Referencias:**
> - [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)
> - [Estado del Roadmap](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md)
> - [Guía de Reglas de Agente (AGENTS.md)](file:///home/p0xd/Documentos/NotiFast/AGENTS.md)

---

## 📋 Prompt para Copiar y Pegar en la Siguiente Sesión

```markdown
Hola. Vamos a iniciar la ejecución de la **Fase 1: Fundamentos de Arquitectura** del PRD Maestro de NotiFast v2.0.

Por favor, sigue estrictamente las siguientes directivas antes de tocar cualquier archivo:
1. Lee y asimila los documentos en `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md` y `AGENTS.md`.
2. Activa y sigue los protocolos de las siguientes skills:
   - `test-driven-development` (ciclo Red-Green-Refactor escribiendo pruebas unitarias que fallen antes de implementar Use Cases y ViewModels).
   - `systematic-debugging` (aislar causa raíz con hipótesis y reproducción ante cualquier fallo en corrutinas o inyección).
   - `verification-before-completion` (compilar con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` antes de dar por terminada una tarea).
   - `codegraph` (uso obligatorio de `codegraph_explore` para navegar símbolos y callers en lugar de lecturas ciegas).
   - `impeccable` (aplicación rigurosa del estándar de diseño UI/UX para el refactor de Material 3, Dark Mode, jerarquía visual y modularización de pantallas).
3. Aplica de forma obligatoria el estándar de **Calidad de Código y Limpieza de Anti-Patrones**:
   - **KISS & YAGNI:** Soluciones directas y simples; cero sobre-ingeniería ni abstracciones especulativas.
   - **Límite de tamaño:** Máximo ~250 líneas por archivo composable. Subdividir pantallas monolíticas restantes (`SubjectCard`, `CalendarScreen`, `GradesScreen`, `AddEvaluationScreen`).
   - **Tokens de Diseño Impeccable:** Refactorizar paleta Material 3 con Ruby (`#BF092F`) y Aqua (`#3B9797`), garantizando Dark Mode real con contraste óptimo (WCAG AA ≥ 4.5:1).
   - **Cero Strings Hardcodeados:** Todo texto visible para el usuario debe usar `stringResource(R.string.xxx)`.
   - **SRP y Clean Architecture:** Toda lógica compuesta va en Use Cases en `domain/usecase/`.
   - **Manejo de Errores:** Operaciones de repositorio retornan `Result<T>` y propagan errores al `UiState`.
4. Ejecuta de forma secuencial y quirúrgica las tareas de la Fase 1:
   - **Sprint 1.1 (Arquitectura y Refactor de Dominio/UI):**
     * 1.1.1: Capa de Use Cases en `domain/usecase/` para operaciones compuestas (`DeleteSubjectCascadeUseCase`, `GetSubjectDetailUseCase`, `GetNotesWithSubjectInfoUseCase`).
     * 1.1.2: Refactorizar Material 3 Theme con paleta corporativa `Ruby` & `Aqua` + Dark Mode real (usando directivas `impeccable` de color, tipografía y contraste).
     * 1.1.3: Actualizar Compose BOM y version catalog a versiones estables.
     * 1.1.4: Modularizar y dividir pantallas monolíticas restantes (>250 LOC) aplicando principios de jerarquía visual y scanability.
     * 1.1.5: Crear `UserPrefsRepository` con DataStore para configuraciones del estudiante.
     * 1.1.6: Limpieza de firmas de métodos reactivos (`Flow` vs `suspend`).
   - **Sprint 1.2 (Arnés de Pruebas Unitarias e Integración):**
     * 1.2.1: Consolidar infraestructura de tests (MockK, Turbine, Coroutines Test).
     * 1.2.2: Tests unitarios para todos los Use Cases nuevos y existentes.
     * 1.2.3: Tests unitarios para ViewModels core (`NotesViewModel`, `GradesViewModel`, `CalendarViewModel`, `LoginViewModel`).
     * 1.2.4: Tests de integración con emuladores/mocks de repositorios.
5. Tras completar cada tarea, ejecuta la verificación con `./gradlew testDebugUnitTest` y actualiza `docs/prd/STATUS.md`. Al completar el 100%, genera automáticamente `docs/prd/KICKOFF_FASE_2.md`.
```

---

## 🎨 Directivas de Diseño UI/UX con Impeccable (Estándar de Élite)

Para las tareas de UI de la Fase 1 (**1.1.2: Refactor de Tema** y **1.1.4: Modularización de Pantallas**), se deben aplicar rigurosamente estos 6 principios de `impeccable`:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                               MODO OPERATE (NotiFast UI)                               │
│  El estudiante interactúa para gestionar apuntes, horarios y notas.                    │
│  Prioridad: Reducir carga cognitiva, máxima scanabilidad y retroalimentación inmediata.│
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### 1. 🔤 Tipografía y Jerarquía Visual (`/impeccable typeset`)
- **Escala Tipográfica Clara:**
  * **Títulos Principales:** `titleLarge` / `headlineSmall` con `FontWeight.Bold` o `FontWeight.ExtraBold` para títulos de pantalla y notas.
  * **Metadatos y Badges:** `labelMedium` / `bodySmall` con `FontWeight.SemiBold` y espaciado de letras (`letterSpacing = 0.5.sp`).
  * **Cuerpo de Texto:** `bodyLarge` / `bodyMedium` con interlineado cómodo para lectura prolongada de apuntes.
- **Cero Fuentes Genéricas sin Escala:** Centralizar todos los estilos tipográficos en `presentation/theme/Type.kt`.

### 2. 🎨 Paleta Semántica y Dark Mode Real (`/impeccable colorize`)
- **Colores Corporativos:**
  * **Ruby (`#BF092F`):** Acciones primarias, llamadas de atención, evaluaciones próximas, alertas de baja calificación.
  * **Aqua (`#3B9797`):** Acciones secundarias, estados aprobados, confirmaciones, selectores activos.
- **Dark Theme Nativo de Alto Contraste:**
  * `BackgroundDark = #10161A` (Gris noche profundo, no negro puro agresivo).
  * `SurfaceDark = #1B242A` (Superficie de tarjetas con contraste distinguible).
  * `SurfaceVariantDark = #25333C` (Campos de texto y chips inactivos).
  * `OnSurfaceDark = #ECEFF1` (Texto principal de máxima legibilidad).
  * `OnSurfaceVariantDark = #94A3B8` (Texto secundario y placeholders legibles).
- **Prohibido:** Fondos quemados con `Color.White`, textos oscuros sobre superficies oscuras o tarjetas sin elevación/borde en Dark Mode.

### 3. 📐 Ritmo Espacial y Contenedores (`/impeccable layout`)
- **Sistema de Espaciado 8dp:** Todos los paddings, márgenes y spacers deben seguir la cuadrícula de 8dp (`4.dp`, `8.dp`, `16.dp`, `24.dp`, `32.dp`).
- **Márgenes de Pantalla:** `24.dp` horizontal para formularios y vistas de detalle; `16.dp` para listas scrolleables.
- **Bordes Redondeados Consistentes:**
  * Botones e Inputs: `28.dp` (Pill shape suave).
  * Tarjetas de Apuntes y Materias: `16.dp` - `20.dp` con elevación tonal sutil (`2.dp`).

### 4. ⚡ Micro-Animaciones y Estados de Carga (`/impeccable animate` & `polish`)
- **Transiciones de Pantalla y Diálogos:** `AnimatedVisibility` con `fadeIn(tween(300))` y `slideInVertically(tween(300))`.
- **Estados de Carga:** Feedback visual inmediato con `NotiFastCircularLoading` (color Ruby/Aqua) y botones con estado de progreso interno deshabilitando doble tap.
- **Empty States Memorables:** Toda lista vacía debe utilizar `NotiFastEmptyState` con ícono ilustrativo, título empático y botón de acción directa.

### 5. ♿ Accesibilidad y Calidad Técnica (`/impeccable audit`)
- **Objetivos Táctiles (Touch Targets):** Mínimo `48.dp x 48.dp` para todos los botones interactivos e íconos (`IconButton`, `FloatingActionButton`, tabs).
- **Insets del Sistema:** Uso estricto de `.safeDrawingPadding()` o `WindowInsets.safeDrawing` para evitar superposición con barras de estado y gestos de Android.
- **Semántica:** Atributos `contentDescription` descriptivos en todos los `Icon` interactivos.

---

## 🛠️ Matriz de Skills Requeridas para Fase 1

| Skill | Cuándo y Cómo Usarla |
|---|---|
| **CodeGraph** (`codegraph_explore`) | **Obligatorio** antes de abrir o editar cualquier archivo para mapear callers y dependencias estructurales. |
| **Test-Driven Development** (`test-driven-development`) | Ciclo Red-Green-Refactor al construir los Use Cases en `domain/usecase/` y ViewModels. |
| **Impeccable** (`impeccable`) | Al rediseñar y pulir el tema corporativo `Ruby` / `Aqua`, Dark Mode y modularizar pantallas complejas. |
| **Systematic Debugging** (`systematic-debugging`) | Ante cualquier fallo en corrutinas, DataStore o inyección con Hilt. |
| **Verification Before Completion** (`verification-before-completion`) | Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` antes de cerrar cada tarea. |
| **Executing Plans** (`executing-plans`) | Mantener el checklist actualizado en `docs/prd/STATUS.md`. |

---

## 🎯 Lista Detallada de Tareas en Fase 1 (10 Tareas)

### Bloque A: Sprint 1.1 (Arquitectura y Dominio)
1. **1.1.1 — Capa de Use Cases en `domain/usecase/`:**
   - Implementar `DeleteSubjectCascadeUseCase` (eliminación atómica de materia con sus notas asociadas).
   - Implementar `GetSubjectDetailUseCase` (agrupación reactiva de evaluaciones y notas por materia).
   - Implementar `GetNotesWithSubjectInfoUseCase` (enlace reactivo de color e ícono en la lista de apuntes).
2. **1.1.2 — Refactor Material 3 Theme con Impeccable:**
   - Actualizar `Theme.kt`, `Color.kt` y `Type.kt` con tokens semánticos completos para Light y Dark Mode.
   - Eliminar todo parseo de color inline y centralizar la asignación dinámica de colores de materias en `ThemeUtils`.
3. **1.1.3 — Actualización y Auditoría de Version Catalog:**
   - Auditar `libs.versions.toml` asegurando compatibilidad óptima con Kotlin 2.0.21, AGP 9 y Gradle 9.
4. **1.1.4 — Modularización de Pantallas Monolíticas (>250 LOC):**
   - Subdividir `SubjectCard.kt`, `AddEvaluationScreen.kt`, `EditEvaluationScreen.kt` y `CalendarScreen.kt` extrayendo sub-componentes a sus respectivas carpetas `components/`.
5. **1.1.5 — `UserPrefsRepository` (DataStore Preferences):**
   - Crear abstracción e implementación de persistencia local para preferencias del estudiante (tema oscuro/claro, escala de notas por defecto, filtros favoritos).
6. **1.1.6 — Limpieza de Firmas Reactivas:**
   - Asegurar que consultas continuas retornen `Flow<T>` y escrituras/cargas puntuales sean `suspend fun ...: Result<T>`.

### Bloque B: Sprint 1.2 (Testing Automatizado)
7. **1.2.1 — Infraestructura de Testing:**
   - Configurar `MainDispatcherRule` y utilidades de test con MockK, Coroutines Test y Turbine.
8. **1.2.2 — Unit Tests para Use Cases:**
   - Tests unitarios exhaustivos para todos los Use Cases en `domain/usecase/` (cobertura 100% de casos límite).
9. **1.2.3 — Unit Tests para ViewModels:**
   - Tests para `NotesViewModelTest`, `GradesViewModelTest`, `CalendarViewModelTest`, `LoginViewModelTest`.
10. **1.2.4 — Tests de Integración:**
    - Validar flujos de interacción repositorio-viewmodel con mocks de Firestore y Auth.

---

## 🛡️ Reglas de Oro de Ejecución
- **Clean Architecture estricta:** `data/` implementa interfaces de `domain/`; `presentation/` consume `domain/`.
- **KISS & YAGNI:** Implementar solo la lógica necesaria para los casos de uso reales del estudiante.
- **≤ 250 líneas por archivo composable.**
- **Preservar el idioma:** Código en inglés, UI en español.
- **Evidencia antes de afirmaciones:** Toda tarea termina con tests ejecutados y build exitoso.

