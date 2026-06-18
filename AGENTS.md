# AGENTS.md — Guía para Agentes AI en NotiFast

> Este archivo define las reglas, convenciones y contexto que cualquier agente AI debe seguir al trabajar en este repositorio.

---

## 🎯 Contexto del Proyecto

**NotiFast** es una aplicación Android nativa escrita en **Kotlin** con **Jetpack Compose**. Funciona como un hub educativo para estudiantes — un cuaderno académico avanzado que permite gestionar apuntes, materias, evaluaciones, calificaciones, horarios y calendario académico.

### Stack técnico
- **Lenguaje:** Kotlin 2.0.21
- **UI:** Jetpack Compose con Material 3
- **Backend:** Firebase (Auth + Firestore + Storage)
- **DI:** Hilt (Dagger)
- **Imágenes:** Coil 2.7
- **Navegación:** Navigation Compose
- **Build:** Gradle con KSP

---

## 🏗️ Arquitectura

### Capas (Clean Architecture)

```
data/       → Implementaciones, Firebase, modelos de datos
domain/     → Interfaces de repositorios (futuro: use cases)
presentation/ → UI, ViewModels, navegación, design system
```

### Patrón por Feature

Cada feature sigue el patrón **MVVM + MVI Contract**:

```
feature/{nombre}/
├── {Nombre}Screen.kt        # Composable principal
├── {Nombre}ViewModel.kt     # ViewModel con @HiltViewModel
├── {Nombre}Contract.kt      # UiState (data class) + Event (sealed class)
└── components/               # Sub-composables opcionales
```

### Convenciones de nombres

| Elemento | Convención | Ejemplo |
|---|---|---|
| Screen composable | `{Feature}Screen` | `NotesScreen`, `CalendarScreen` |
| ViewModel | `{Feature}ViewModel` | `NotesViewModel` |
| UI State | `{Feature}UiState` | `NotesUiState` |
| Events | `{Feature}Event` | `NotesEvent.Search(query)` |
| Repository interface | `{Domain}Repository` | `NoteRepository` |
| Repository impl | `{Domain}RepositoryImpl` | `NoteRepositoryImpl` |
| Atomic component | `NotiFast{Component}` | `NotiFastButton`, `NotiFastCard` |

---

## 📐 Reglas de Código

### General
1. **Idioma del código:** Inglés para nombres de variables, funciones, clases y archivos
2. **Idioma de la UI:** Español (los textos al usuario están en español)
3. **No usar wildcard imports** (`.*`) — importar explícitamente cada clase
4. **Máximo ~250 líneas** por archivo composable — extraer sub-composables si excede
5. **Todas las funciones de repository deben retornar `Result<T>`** para operaciones write, y `Flow<T>` para operaciones read en tiempo real

### Compose
1. Usar `collectAsStateWithLifecycle()` para observar StateFlows
2. Usar `LaunchedEffect` para side-effects de navegación — **NUNCA** ejecutar navegación directamente en composición
3. Usar `BackHandler` cuando haya estados expandidos que deban cerrarse primero
4. Usar `AnimatedVisibility` con `spring()` para transiciones
5. Usar los colores del `MaterialTheme.colorScheme` en vez de referenciar `Ruby`/`Aqua` directamente (esto es un objetivo futuro)

### ViewModels
1. Todo el estado observable va en **un solo `_uiState: MutableStateFlow<UiState>`** — no crear StateFlows separados para `saved`, `isLoading`, etc.
2. Usar `onEvent(event: Event)` como punto de entrada único desde la UI
3. **No poner lógica de negocio en los composables** — toda lógica va en ViewModels o Use Cases
4. Siempre verificar `authRepository.getCurrentUserId()` antes de operaciones con Firestore

### Repositorios
1. Las operaciones write deben ser envueltas en `try/catch` y retornar `Result<T>`
2. Las operaciones read en tiempo real usan `snapshots()` → `Flow<List<T>>`
3. Operaciones de borrado compuestas deben usar `WriteBatch` para atomicidad
4. **Nunca tragar excepciones** con `catch(e: Exception) {}` vacío — al menos loguear

### Firebase / Firestore
1. Las rutas siguen el patrón: `users/{userId}/collection/{docId}/subcollection/{subDocId}`
2. `SubjectData` usa el nombre como document ID (limitación conocida, documentada en auditoría)
3. Usar `@ServerTimestamp` para fechas de creación/modificación
4. **Siempre implementar Firestore Security Rules** que validen `request.auth.uid == userId`

---

## 🎨 Design System

### Colores corporativos
- **Ruby** (`#BF092F`) — Color primario, acciones destructivas, evaluaciones
- **Aqua** (`#3B9797`) — Color secundario, estados positivos, confirmaciones

### Componentes atómicos (en `presentation/atoms/`)

| Componente | Uso |
|---|---|
| `NotiFastButton` | Botón primario |
| `NotiFastCard` | Card contenedora |
| `NotiFastTextField` | Input de texto |
| `NotiFastFormField` | Campo de formulario con label e icono |
| `NotiFastDialog` | Diálogo de confirmación |
| `NotiFastEmptyState` | Estado vacío con icono y mensaje |
| `NotiFastCircularLoading` | Loading spinner a pantalla completa |
| `NotiFastCenterAlignedTopBar` | Top bar centrada |
| `NotiFastStudyBackground` | Background con gradientes |
| `NotiFastBottomNav` | Barra de navegación inferior |
| `NotiFastBackground` | Background base |

### Regla de uso
**SIEMPRE** usar los componentes atómicos disponibles antes de crear componentes ad-hoc. Si necesitas un componente nuevo, créalo en `atoms/` con el prefijo `NotiFast`.

---

## 📁 Estructura de Archivos

```
NotiFast/
├── AGENTS.md                          # ← Este archivo
├── README.md                          # Documentación del proyecto
├── app/
│   ├── build.gradle.kts               # Dependencias y configuración
│   ├── google-services.json           # ⚠️ NO commitear (debe estar en .gitignore)
│   └── src/main/java/com/example/notifast/
│       ├── data/
│       │   ├── model/                 # 5 data classes
│       │   └── repository/            # 5 implementaciones
│       ├── di/                        # 2 módulos Hilt
│       ├── domain/repository/         # 5 interfaces
│       ├── presentation/
│       │   ├── atoms/                 # 15 componentes base
│       │   ├── feature/               # 14 features (Screen + VM + Contract)
│       │   ├── navigation/            # NavGraph + Screen routes
│       │   └── theme/                 # Color, Type, Theme
│       ├── MainActivity.kt
│       └── NotiFastApplication.kt
├── docs/
│   ├── audit/                         # Auditorías de código
│   └── superpowers/
│       ├── plans/                     # Planes de implementación
│       └── specs/                     # Specs de diseño
└── gradle/libs.versions.toml          # Version catalog
```

---

## ⚠️ Problemas Conocidos (Prioridad Alta)

Estos son los problemas documentados en la [auditoría del 2026-06-18](docs/audit/2026-06-18-full-audit.md) que **deben tenerse en cuenta** al hacer cambios:

1. **Credenciales expuestas** — `google-services.json` y `WEB_CLIENT_ID` en código
2. **Sin Firestore Security Rules** — cualquier usuario puede leer/escribir datos ajenos
3. **Sin tests** — 0 tests unitarios o de integración reales
4. **Imágenes solo locales** — `ImageRepositoryImpl` no usa Firebase Storage
5. **N+1 listeners** — `getNotes()` crea un listener por materia
6. **Lógica duplicada** — Cálculo de promedio ponderado repetido en 4 lugares
7. **Bug en promedio global** — Calcula promedio de promedios en vez de promedio ponderado real
8. **Schedules incompleto** — No hay UI para CRUD de horarios

---

## 🔄 Workflow para Cambios

1. **Antes de empezar:** Lee este archivo y la auditoría
2. **Verificar si hay plan:** Revisa `docs/superpowers/plans/` para planes existentes
3. **Seguir la arquitectura:** Clean Architecture → no mezclar capas
4. **Seguir los patrones:** MVVM + MVI Contract para nuevas features
5. **Documentar cambios:** Crear un plan en `docs/superpowers/plans/` para cambios significativos
6. **Verificar:** Asegurarse de que el código compila antes de terminar

---

## 🚫 Anti-patrones a Evitar

1. ❌ Poner lógica de negocio en composables (cálculos, filtrado, transformaciones)
2. ❌ Crear StateFlows separados fuera del UiState principal
3. ❌ Usar `collect` infinito para cargas one-shot (usar `first()`)
4. ❌ Tragar excepciones con catch vacío
5. ❌ Navegar durante la fase de composición (usar `LaunchedEffect`)
6. ❌ Hardcodear credenciales o IDs de API
7. ❌ Borrar datos sin confirmación del usuario
8. ❌ Usar el nombre de materia como único identificador sin URL encoding
