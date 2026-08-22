# 🚀 Kickoff y Guía de Inicio — Fase 0: Estabilización

> **Propósito:** Documento de arranque rápido para la siguiente sesión de desarrollo.  
> **Ubicación:** `docs/prd/KICKOFF_FASE_0.md`  
> **Referencias:**
> - [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)
> - [Estado del Roadmap](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md)
> - [Guía de Reglas de Agente (AGENTS.md)](file:///home/p0xd/Documentos/NotiFast/AGENTS.md)

---

## 📋 Prompt para Copiar y Pegar en la Siguiente Sesión

```markdown
Hola. Vamos a iniciar la ejecución de la **Fase 0: Estabilización** del PRD Maestro de NotiFast v2.0.

Por favor, sigue estrictamente las siguientes directivas antes de tocar cualquier archivo:
1. Lee y asimila los documentos en `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md` y `AGENTS.md`.
2. Activa y sigue los protocolos de las siguientes skills:
   - `systematic-debugging` (para investigar causas raíz de bugs y no poner parches ciegos).
   - `test-driven-development` (escribir pruebas unitarias que fallen antes del fix).
   - `verification-before-completion` (compilar con `./gradlew assembleDebug` y verificar antes de dar por terminada una tarea).
   - Uso obligatorio de CodeGraph (`codegraph_explore`) para navegar símbolos en lugar de lecturas ciegas.
3. Aplica de forma obligatoria el estándar de **Calidad de Código y Limpieza de Anti-Patrones**:
   - **KISS & YAGNI:** Soluciones directas y simples; cero sobre-ingeniería ni abstracciones especulativas.
   - **Límite de tamaño:** Máximo ~250 líneas por archivo composable. Si un archivo supera este límite, divide y extrae sub-composables a `components/` o `atoms/`.
   - **Tokens de Diseño:** Prohibido hardcodear colores (`Color(0xFF...)` o `Color.Red`) en composables. Usar `MaterialTheme.colorScheme` o tokens corporativos centralizados.
   - **Cero Strings Hardcodeados:** Todo texto visible para el usuario debe usar `stringResource(R.string.xxx)`.
   - **SRP y Lógica Fuera de UI:** Toda lógica de negocio, cálculo o transformación va en ViewModels o Use Cases, NUNCA en composables.
   - **Manejo de Errores:** Prohibido tragar excepciones (`catch(e) {}` vacío). Toda operación de repositorio retorna `Result<T>` y se maneja en el ViewModel con feedback al usuario.
4. Ejecuta de forma secuencial y quirúrgica las tareas pendientes de la Fase 0:
   - **Sprint 0.1 (Bugs Críticos):**
     * 0.1.2: Habilitar R8 + minificación y ProGuard rules en release.
     * 0.1.5: Verificar y asegurar punto único de navegación post-login en NavGraph.
     * 0.1.7: Añadir diálogo de confirmación `NotiFastDialog` al borrar notas en `NoteEstheticCard`.
     * 0.1.8: Reemplazar bloques `catch` silenciosos por logs/manejo visible.
   - **Sprint 0.2 (Deuda Técnica Esencial):**
     * 0.2.2: Optimizar `getNotes()` en Firestore para eliminar el patrón de N+1 listeners.
     * 0.2.5: Estandarizar convención de pesos de evaluaciones (`weight`: factor 0.0 a 1.0 vs porcentaje en UI).
     * 0.2.6: Unificar parseo seguro de colores Hex a `Color` en `ThemeUtils`.
     * 0.2.7: Configurar e inicializar Firebase Crashlytics.
     * 0.2.8: Configurar e inicializar Firebase Analytics.
5. Tras completar cada tarea, ejecuta la verificación con `./gradlew assembleDebug` y actualiza `docs/prd/STATUS.md`.
```

---

## 🧹 Buenas Prácticas Integrales y Errores a Erradicar

Esta sección documenta los anti-patrones detectados en la auditoría y establece la pauta obligatoria de corrección para cualquier archivo que sea modificado o creado:

### 1. Tamaño de Archivos y Responsabilidad Única (SRP)
- **Problema actual:** Pantallas de 400+ líneas (`NotesScreen`, `CalendarScreen`, `GradesScreen`, `SubjectCard`).
- **Pauta obligatoria:** 
  - Máximo **~250 líneas** por archivo composable.
  - Extraer componentes secundarios a la subcarpeta `components/` de la feature o a `presentation/atoms/` si son reutilizables globalmente.
  - Cada composable debe tener una sola responsabilidad: presentar estado y delegar eventos.

### 2. Principios KISS & YAGNI y Estado MVI Limpio
- **Problema actual:** Estados paralelos (`_saved`, `isLoading` dispersos fuera del `UiState`), llamadas asíncronas redundantes.
- **Pauta obligatoria:**
  - **Un solo StateFlow por ViewModel:** `val uiState: StateFlow<FeatureUiState>`.
  - **Punto único de entrada:** `onEvent(event: FeatureEvent)`.
  - Resolver exclusivamente el requerimiento actual sin añadir capas innecesarias ("You Aren't Gonna Need It").

### 3. Tokens de Diseño, Colores y Tema Material 3
- **Problema actual:** Colores arbitrarios hardcodeados (`Color.Red.copy(...)`, `Color.LightGray`, strings Hex parseados en línea en 5+ archivos).
- **Pauta obligatoria:**
  - Usar colores del tema: `MaterialTheme.colorScheme.primary`, `surface`, `onSurfaceVariant`, `error`.
  - Centralizar el parseo de colores dinámicos (de materias) en `ThemeUtils` con fallbacks seguros.
  - Usar siempre los componentes atómicos existentes (`NotiFastButton`, `NotiFastCard`, `NotiFastDialog`, `NotiFastTopBar`) antes de construir layouts ad-hoc.

### 4. Cero Strings Hardcodeados en UI
- **Problema actual:** Textos como `"¿Ya eres parte de NotiFast?"`, `"Eliminar materia"`, `"Calificaciones"` quemados en el código Kotlin.
- **Pauta obligatoria:**
  - Todo texto presentado al usuario debe provenir de `res/values/strings.xml` usando `stringResource(R.string.nombre_recurso)`.

### 5. Cero Lógica de Negocio en Composables
- **Problema actual:** Cálculos de promedio ponderado, conversiones de fecha y filtros ejecutados directamente durante la composición.
- **Pauta obligatoria:**
  - La composición es puramente declarativa.
  - Todo cálculo, ordenamiento o transformación debe ocurrir en el ViewModel o en un Use Case en `domain/usecase/`.

### 6. Manejo Riguroso de Errores y Excepciones
- **Problema actual:** `try { ... } catch(e: Exception) {}` vacío que traga errores impidiendo depurar fallos en producción.
- **Pauta obligatoria:**
  - Todas las funciones de repositorio de escritura retornan `Result<T>`.
  - Los errores deben loguearse (`Log.e(...)` / Crashlytics) y propagarse al `UiState.error` para mostrar Snackbar o feedback al usuario.

---

## 🛠️ Matriz de Skills Requeridas para Fase 0

| Skill | Cuándo y Cómo Usarla |
|---|---|
| **CodeGraph** (`codegraph_explore`) | **Obligatorio** antes de abrir o editar cualquier archivo. Localizar símbolos, ver referencias y callers con 1 sola llamada. |
| **Systematic Debugging** (`systematic-debugging`) | Al abordar errores de navegación (0.1.5) o reestructuración de listeners (0.2.2). Cero suposiciones; aislar causa raíz. |
| **Test-Driven Development** (`test-driven-development`) | Escribir tests unitarios en Kotlin (ej. para parseo de colores, cálculo de pesos, repositorios) antes de la implementación. |
| **Verification Before Completion** (`verification-before-completion`) | Ejecutar `./gradlew assembleDebug` o tests unitarios antes de afirmar que la tarea está completada. |
| **Executing Plans** (`executing-plans`) | Para mantener el checklist actualizado en `docs/prd/STATUS.md` tras cada sub-tarea. |

---

## 🎯 Lista de Trabajo Restante en Fase 0 (10 Tareas)

### Bloque A: Sprint 0.1 (Estabilidad Inmediata)
1. **0.1.2 — Minificación R8 y ProGuard:**
   - En `app/build.gradle.kts`, configurar `isMinifyEnabled = true` en el bloque `release`.
   - Agregar directivas ProGuard para Firebase Firestore, Firebase Auth, Hilt y Coil en `app/proguard-rules.pro`.
2. **0.1.5 — Validación de Navegación Post-Login:**
   - Auditar `LoginScreen.kt` y `NavGraph.kt` para asegurar que el flujo de redirección no compita en recomposición.
3. **0.1.7 — Diálogo de Confirmación en Borrado de Notas:**
   - En `NoteEstheticCard.kt` o `NotesScreen.kt`, implementar confirmación con `NotiFastDialog` antes de disparar `onDelete`.
4. **0.1.8 — Saneamiento de Error Handling:**
   - Auditar repositorios (`NoteRepositoryImpl`, `EvaluationRepositoryImpl`, `ScheduleRepositoryImpl`) eliminando `catch (e: Exception) {}` vacíos y asegurando logs y propagación limpia.

### Bloque B: Sprint 0.2 (Deuda Técnica y Observabilidad)
5. **0.2.2 — Optimización de Firestore `getNotes()`:**
   - Evaluar consulta optimizada o agrupada para no instanciar 1 listener reactivo por cada materia de forma recursiva.
6. **0.2.5 — Estandarización de `weight`:**
   - Documentar y asegurar que los modelos y validadores traten `weight` como factor `0.0 .. 1.0` y la UI muestre `(weight * 100).toInt() %`.
7. **0.2.6 — Extension function para colores:**
   - Crear helper `String.toSafeComposeColor()` o consolidar en `ThemeUtils.kt` eliminando el try/catch manual disperso en 5+ archivos.
8. **0.2.7 — Firebase Crashlytics:**
   - Agregar plugins y dependencias en `build.gradle.kts` y `libs.versions.toml`.
9. **0.2.8 — Firebase Analytics:**
   - Agregar eventos estándar para logging de actividad académica.

---

## 🛡️ Reglas de Oro para la Ejecución
- **No romper la arquitectura:** Respetar Clean Architecture (`data/` → `domain/` → `presentation/`).
- **No introducir componentes ad-hoc:** Usar los átomos existentes en `presentation/atoms/NotiFast*`.
- **No commitear credenciales:** Mantener `google-services.json` ignorado y strings sensibles en `BuildConfig`.
- **Mantener el idioma:** Código en inglés, UI y recursos en español.
