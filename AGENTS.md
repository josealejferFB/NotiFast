# AGENTS.md — Guía Maestra para Agentes AI en NotiFast

> Este archivo define las directivas operativas, convenciones de ingeniería, arquitectura, diseño y reglas de comportamiento agéntico que **TODO agente AI DEBE seguir estrictamente** al operar en este repositorio.

---

## 🎯 1. Contexto del Proyecto

**NotiFast** es una aplicación Android nativa escrita en **Kotlin** con **Jetpack Compose**. Funciona como un **hub educativo integral y cuaderno académico inteligente** para estudiantes de secundaria y universidad.

### Propósito Dual
1. **Producto Comercial:** Distribución gratuita en Google Play Store con experiencia pulida, moderna y sin fricciones para reducir la ansiedad académica.
2. **Proyecto Académico de Tesis:** Demostración rigurosa del ciclo completo de ingeniería de software, arquitectura limpia, testing en aula y despliegue real.

### Stack Técnico Oficial
- **Lenguaje:** Kotlin 2.0.21
- **UI:** Jetpack Compose con Material 3 (BOM 2024.09.00)
- **Backend Cloud:** Firebase (Auth, Firestore, Cloud Storage, Analytics, Crashlytics)
- **Inyección de Dependencias (DI):** Hilt 2.59 (Dagger)
- **Carga de Imágenes:** Coil 2.7.0
- **Navegación:** Navigation Compose 2.8.5
- **Background Work:** Android WorkManager (notificaciones locales programadas)
- **Build System:** Gradle con KSP y R8/Minification

---

## 🧭 2. Referencias Maestras de Documentación

Antes de iniciar cualquier trabajo o responder sobre el roadmap, consulta siempre:

| Documento | Ubicación | Propósito |
|---|---|---|
| **PRD Maestro v2.0** | [`docs/prd/PRD-MAESTRO-NOTIFAST.md`](docs/prd/PRD-MAESTRO-NOTIFAST.md) | Especificación de requisitos de producto de Fases 0 a 5 (~22 semanas). |
| **Estado del Roadmap** | [`docs/prd/STATUS.md`](docs/prd/STATUS.md) | Tablero de control en tiempo real: tareas completadas, en progreso y pendientes. |
| **Kickoff Fase 0** | [`docs/prd/KICKOFF_FASE_0.md`](docs/prd/KICKOFF_FASE_0.md) | Guía de arranque y prompt de sesión para estabilización y resolución de bugs (Completada). |
| **Kickoff Fase 1** | [`docs/prd/KICKOFF_FASE_1.md`](docs/prd/KICKOFF_FASE_1.md) | Guía de arranque y prompt de sesión para Fundamentos de Arquitectura (Completada). |
| **Kickoff Fase 2** | [`docs/prd/KICKOFF_FASE_2.md`](docs/prd/KICKOFF_FASE_2.md) | Guía de arranque y prompt de sesión para Experiencia Core — Cuaderno y Hub Académico (Completada). |
| **Kickoff Fase 2.1** | [`docs/prd/KICKOFF_FASE_2.1.md`](docs/prd/KICKOFF_FASE_2.1.md) | Guía de arranque y prompt de sesión para Experiencia sin Fricción y Ergonomía de Formularios (Siguiente fase). |
| **Kickoff Fase 3** | [`docs/prd/KICKOFF_FASE_3.md`](docs/prd/KICKOFF_FASE_3.md) | Guía de arranque y prompt de sesión para Inteligencia Predictiva y Notificaciones Proactivas (Planificada). |
| **Auditoría Histórica** | [`docs/audit/2026-06-18-full-audit.md`](docs/audit/2026-06-18-full-audit.md) | Análisis base de seguridad, arquitectura y bugs iniciales. |
| **Planes y Specs** | [`docs/superpowers/plans/`](docs/superpowers/plans/) y [`docs/superpowers/specs/`](docs/superpowers/specs/) | Diseños y planes de ejecución atómicos. |

---

## 🤖 3. Directivas de Comportamiento Agéntico (Estándar de Élite)

Todo agente operando en este repositorio debe aplicar sin excepción estos 5 pilares operativos:

### 🟢 1. CodeGraph First (Navegación e Inteligencia Estructural)
- **Herramienta Primaria Obligatoria:** Toda exploración, búsqueda de símbolos, análisis de callers/callees y cálculo de radio de impacto DEBE hacerse mediante `codegraph_explore`.
- **Prohibido:** Realizar lecturas masivas o barridos ciegos con `grep`/`find` cuando la información estructural puede obtenerse en 1 sola llamada con CodeGraph.

### 🔵 2. Systematic Debugging (Cero Parches a Ciegas)
- Ante cualquier bug o comportamiento inesperado: **Hipótesis ➔ Aislamiento de causa raíz ➔ Caso reproducible ➔ Fix quirúrgico**.
- Prohibido aplicar "quick fixes" o modificaciones ad-hoc sin haber trazado el flujo de datos exacto.

### 🔴 3. Test-Driven Development (TDD)
- Construir lógica de negocio y casos de uso mediante el ciclo: **Red (Prueba que falla) ➔ Green (Implementación mínima) ➔ Refactor**.
- Toda feature nueva o corrección de cálculo matemático (ej. promedios, pesos, solapamientos) debe contar con tests unitarios reproducibles.

### 🟣 4. Verification Before Completion (Evidencia antes de Afirmaciones)
- **Nunca dar por terminada una tarea sin verificar:** Ejecutar `./gradlew assembleDebug` y las suites de test correspondientes.
- Si el código no compila o no pasa pruebas, el trabajo **NO** está terminado.

### 📋 5. Mantenimiento de Estado y Generación de Kickoff Siguiente
- Tras culminar cualquier tarea del roadmap, es **obligatorio actualizar** el archivo [`docs/prd/STATUS.md`](docs/prd/STATUS.md).
- Al completar el **100% de una fase**, el agente DEBE **generar automáticamente el documento `docs/prd/KICKOFF_FASE_{N+1}.md`** con el prompt detallado y la guía técnica para la fase subsiguiente (ver Protocolo en Sección 8).

---

## 🏗️ 4. Arquitectura del Sistema

El proyecto implementa **Clean Architecture** estricta dividida en tres capas principales:

```
app/src/main/java/com/example/notifast/
├── data/                    # CAPA DE DATOS
│   ├── model/               # Data classes inmutables (Note, Evaluation, Schedule, User, SubjectData, etc.)
│   └── repository/          # Implementaciones de repositorios (Firebase Firestore, Storage, Auth)
│
├── domain/                  # CAPA DE DOMINIO (Reglas de Negocio Puras)
│   ├── repository/          # Interfaces de repositorios (Inversión de dependencias)
│   ├── usecase/             # Casos de uso reutilizables (CalculateWeightedAverageUseCase, etc.)
│   └── util/                # Constantes y utilidades de dominio (GradeConstants)
│
├── presentation/            # CAPA DE PRESENTACIÓN (UI + Estado)
│   ├── atoms/               # Design System: 15+ componentes atómicos reutilizables (NotiFast*)
│   ├── feature/             # Features modulares (MVVM + MVI Contract)
│   ├── navigation/          # Rutas type-safe (Screen.kt) y grafo de navegación (NavGraph.kt)
│   └── theme/               # Sistema de diseño: Color, Typography, Shape, ThemeUtils
│
└── di/                      # Inyección de Dependencias (Hilt Modules)
    ├── AppModule.kt         # Proveedores de Firebase y servicios de sistema
    ├── RepositoryModule.kt  # Binds de interfaces de repositorios a implementaciones
    └── UseCaseModule.kt     # Inyección de casos de uso
```

### Patrón por Feature (MVVM + MVI Contract)

Cada feature reside en su propia carpeta bajo `presentation/feature/{nombre}/`:

```
feature/{nombre}/
├── {Nombre}Screen.kt        # Composable principal (declarativo, sin lógica)
├── {Nombre}ViewModel.kt     # ViewModel anotado con @HiltViewModel
├── {Nombre}Contract.kt      # UiState (data class única) + Event (sealed interface)
└── components/               # Sub-composables modulares (si la pantalla supera ~250 LOC)
```

---

## 📐 5. Reglas de Código y Estándares de Higiene

### 🧹 1. Límite de Tamaño y Responsabilidad Única (SRP)
- **Máximo ~250 líneas por archivo composable.**
- Si una pantalla o componente supera las 250 líneas (como ocurría con `NotesScreen` o `GradesScreen`), **debe subdividirse** extrayendo sub-composables a `components/` o `atoms/`.
- Cero composables "Dios" que mezclen layout, lógica, filtros y diálogos en un solo bloque.

### 🧠 2. Principios KISS & YAGNI y Estado MVI
- **Un solo StateFlow por ViewModel:** `val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()`.
- **Prohibido:** Crear StateFlows paralelos o huérfanos (`_saved`, `_isLoading`) fuera del `UiState`.
- **Punto único de interacción UI ➔ ViewModel:** `onEvent(event: FeatureEvent)`.
- Resolver estrictamente la necesidad actual sin sobre-diseñar ni introducir abstracciones especulativas.

### 🎨 3. Design Tokens, Colores y Componentes Atómicos
- **Colores Corporativos:**
  * **Ruby** (`#BF092F`): Acciones primarias, alertas, evaluaciones.
  * **Aqua** (`#3B9797`): Acciones secundarias, confirmaciones, estados de éxito.
- **Prohibido:** Hardcodear colores arbitrarios como `Color(0xFF...)`, `Color.Red` o parsear strings Hex directamente en composables.
- **Obligatorio:** Utilizar `MaterialTheme.colorScheme.*` y centralizar el parseo dinámico de colores de materias en `ThemeUtils`.
- **Uso de Átomos:** Usar **SIEMPRE** los componentes existentes en `presentation/atoms/` (`NotiFastButton`, `NotiFastCard`, `NotiFastTextField`, `NotiFastDialog`, `NotiFastTopBar`, `NotiFastEmptyState`) antes de crear componentes ad-hoc.

### 🌐 4. Cero Strings Hardcodeados en UI
- Todo texto visible para el usuario DEBE residir en `res/values/strings.xml` y consumirse mediante `stringResource(R.string.xxx)`.
- Idioma de los recursos de UI: **Español**. Idioma del código fuente (variables, funciones, clases): **Inglés**.

### ⚖️ 5. Cero Lógica de Negocio en Composables
- Los `@Composable` son **exclusivamente declarativos**.
- Prohibido realizar cálculos matemáticos (ej. promedios ponderados), ordenamientos pesados, validaciones de regex complejas o formateo de fechas dentro de la composición. Toda la lógica pertenece al ViewModel o Use Cases.

### 🚨 6. Manejo Riguroso de Excepciones
- **Prohibido tragar errores:** Cero bloques `try { ... } catch (e: Exception) {}` vacíos.
- Toda operación de escritura en repositorios debe envolverse en `try/catch` y retornar `Result<T>`.
- Las excepciones deben registrarse con `Log.e(...)` o enviarse a Crashlytics, y propagarse al `uiState.error` para dar feedback visual (Snackbar/Diálogo) al usuario.

### ⚡ 7. Compose y Ciclo de Vida
- Usar `collectAsStateWithLifecycle()` para observar StateFlows.
- Usar `LaunchedEffect` para side-effects (como navegación post-login o snackbars). **NUNCA** disparar navegación directamente durante la fase de composición.
- Cero wildcard imports (`import foo.bar.*`) — importar cada clase explícitamente.

---

## 🚫 6. Anti-Patrones Estrictamente Prohibidos

| # | Anti-Patrón | Acción Correctiva Obligatoria |
|---|---|---|
| 1 | Poner cálculos o lógica de negocio en Composables | Extraer a ViewModel o a un Use Case en `domain/usecase/`. |
| 2 | Crear StateFlows dispersos fuera de `UiState` | Unificar todo el estado en un único `data class FeatureUiState`. |
| 3 | Usar `.collect` infinito para cargas de un solo disparo | Usar `.first()` para operaciones one-shot (ej. carga en `EditEvaluation`). |
| 4 | Tragar excepciones con `catch(e) {}` vacío | Loguear el error y propagar `Result.failure(e)` al ViewModel. |
| 5 | Hardcodear colores o textos en la UI | Usar `MaterialTheme.colorScheme` y `stringResource(R.string.*)`. |
| 6 | Archivos composable > 250 líneas | Modularizar y extraer sub-componentes a `components/` o `atoms/`. |
| 7 | Navegar durante la composición | Envolver la navegación en `LaunchedEffect(uiState.isSuccess)`. |
| 8 | Modificar código sin investigar la causa raíz | Seguir el protocolo `systematic-debugging`. |
| 9 | Dar una tarea por completada sin compilar | Ejecutar `./gradlew assembleDebug` y verificar tests. |
| 10 | Versionar credenciales o secrets | Mantener `google-services.json` ignorado y variables en `BuildConfig`. |

---

## 🔄 7. Workflow Estándar para Ejecutar Tareas

```
┌─────────────────────────────────────────────────────────────┐
│ 1. INSPECCIÓN Y CONTEXTO                                    │
│    Consultar PRD Maestro, STATUS.md y explorar con CodeGraph│
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. TDD & DISEÑO QUIRÚRGICO                                  │
│    Escribir prueba unitaria que falle (Red) o aislar bug    │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. IMPLEMENTACIÓN (KISS / YAGNI / Clean Architecture)       │
│    Escribir código modular (≤ 250 LOC), sin anti-patrones   │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. VERIFICACIÓN BASADA EN EVIDENCIA                         │
│    Compilar con ./gradlew assembleDebug y ejecutar tests    │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. ACTUALIZACIÓN DE ESTADO & TRANSICIÓN                     │
│    Actualizar docs/prd/STATUS.md y generar KICKOFF siguiente│
└─────────────────────────────────────────────────────────────┘
```

---

## 🏁 8. Protocolo Obligatorio de Cierre de Fase y Transición

Al completar el 100% de las tareas de cualquier fase (Fase 0, Fase 1, Fase 2, Fase 3, Fase 4), el agente **DEBE ejecutar automáticamente** la siguiente secuencia de cierre antes de dar por concluida la sesión:

```
┌──────────────────────────────────────────────────────────────────┐
│ 1. VERIFICACIÓN INTEGRAL DE SUITE DE PRUEBAS Y BUILD             │
│    Ejecutar ./gradlew assembleDebug y validar cero regresiones   │
└────────────────────────────────┬─────────────────────────────────┘
                                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ 2. ACTUALIZACIÓN DE STATUS.md                                    │
│    Marcar fase al 100%, registrar fecha y actualizar métricas    │
└────────────────────────────────┬─────────────────────────────────┘
                                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ 3. GENERACIÓN DEL KICKOFF DE LA SIGUIENTE FASE                   │
│    Crear docs/prd/KICKOFF_FASE_{N+1}.md con prompt y guía técnica│
└────────────────────────────────┬─────────────────────────────────┘
                                 ▼
┌──────────────────────────────────────────────────────────────────┐
│ 4. ACTUALIZACIÓN DE ÍNDICES DE DOCUMENTACIÓN                     │
│    Enlazar el nuevo Kickoff en README.md y AGENTS.md             │
└──────────────────────────────────────────────────────────────────┘
```

### 📝 Estándar Obligatorio para `docs/prd/KICKOFF_FASE_{N}.md`

Cada nuevo archivo de Kickoff generado debe replicar con máxima rigurosidad la estructura de [`KICKOFF_FASE_0.md`](docs/prd/KICKOFF_FASE_0.md):

1. **Prompt Copiable de Arranque:**
   - Referencia obligatoria a `PRD-MAESTRO-NOTIFAST.md`, `STATUS.md` y `AGENTS.md`.
   - Invocación explícita de skills requeridas (`systematic-debugging`, `test-driven-development`, `verification-before-completion`, `codegraph`, `impeccable`).
   - Bloque de estándares de higiene de código (KISS, YAGNI, ≤ 250 LOC, tokens de diseño, cero strings quemados, cero lógica en UI, `Result<T>`).
   - Desglose sprint por sprint de las tareas exactas de la fase entrante.
2. **Buenas Prácticas y Anti-Patrones Críticos:**
   - Recordatorio específico de los errores comunes y directivas de diseño aplicables a las features de esa fase.
3. **Matriz de Skills Requeridas:**
   - Detalle de qué skill utilizar y en qué momento durante esa fase específica.
4. **Lista Detallada de Tareas y Criterios de Aceptación:**
   - Especificaciones técnicas por componente/feature.
5. **Reglas de Oro de Ejecución:**
   - Conservación de Clean Architecture, no componentes ad-hoc, no secrets versionados y preservación del idioma.

