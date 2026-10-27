# AGENTS.md — Guía Maestra para Agentes AI en NotiFast

> Este archivo define las directivas operativas, convenciones de ingeniería, arquitectura, diseño y reglas de comportamiento agéntico que **TODO agente AI DEBE seguir estrictamente** al operar en este repositorio.

---

## 🎯 1. Contexto del Proyecto

**NotiFast** es una aplicación Android nativa **100% Offline-First** escrita en **Kotlin** con **Jetpack Compose**, acompañada de su **Landing Page Interactiva (`landing/`)**. Funciona como un **hub educativo integral y cuaderno académico inteligente** para estudiantes de secundaria y universidad.

### Propósito Dual
1. **Producto Comercial:** Distribución gratuita en Google Play Store con experiencia pulida, personalizable, moderna y sin fricciones para reducir la ansiedad académica.
2. **Proyecto Académico de Tesis:** Demostración rigurosa del ciclo completo de ingeniería de software, Clean Architecture + MVI, **684 pruebas unitarias automatizadas**, testing en aula real y despliegue.

### Stack Técnico Oficial Real (Actualizado Octubre 2026)
- **Lenguaje:** Kotlin `2.0.21` (con KSP `2.0.21-1.0.28`)
- **UI & Design System:** Jetpack Compose con Material 3 (`BOM 2024.09.00`), Google Fonts (`Plus Jakarta Sans` & `JetBrains Mono`) y Motor de Temas Dinámicos WCAG AAA (`ThemePreset`, `BackgroundPreset`, `ThemePaletteGenerator`, `HexColorStudioPicker`)
- **Persistencia Primaria (100% Offline-First):** Room SQLite `2.7.2` (Esquema `v4` con migraciones no destructivas `1➔2➔3➔4`) + Jetpack DataStore Preferences `1.1.2`
- **Cloud Backup Privado:** Google Drive REST API v3 (`appDataFolder`) vía `OkHttp 4.12.0`, `Google Credential Manager 1.3.0` y `Google ID 1.1.1` con diffing incremental SHA-256 (`CloudHubSheet`)
- **Transferencia P2P Offline:** Paquetes `.notifast` (`GZIP + Base64`) y Código QR Offline (`ZXing 3.5.3` + `Google Play Services Code Scanner 16.1.0`)
- **Motor de Notificaciones Proactivas:** Motor híbrido `AlarmManager.setExactAndAllowWhileIdle()` (`ExactAlarmScheduler` + `AlarmTriggerReceiver` + `BootCompletedReceiver`) + `Android WorkManager 2.10.0` (Hilt Work `1.2.0`)
- **Telemetría:** Firebase (`BOM 33.9.0`) exclusivamente para `Analytics` y `Crashlytics` (cero uso de Firestore o Firebase Storage para datos de usuario)
- **Inyección de Dependencias (DI):** Hilt `2.59` (Dagger)
- **Carga de Imágenes:** Coil `2.7.0`
- **Navegación:** Navigation Compose `2.8.5` (con soporte de Deep-Linking reactivo y BottomSheets modales unificados)
- **Testing:** JUnit 4, MockK `1.13.8`, Coroutines Test `1.9.0`, Turbine `1.0.0`, Room Testing, OkHttp MockWebServer y Robolectric `4.13` (**805 tests unitarios en verde**)
- **Build System:** Android Gradle Plugin (AGP) `9.0.1`, `compileSdk 36`, `minSdk 26`, R8/Minification

---

## 🧭 2. Referencias Maestras de Documentación

Antes de iniciar cualquier trabajo o responder sobre el roadmap, consulta siempre:

| Documento | Ubicación | Propósito |
|---|---|---|
| **Índice Maestro de Documentación** | [`docs/README.md`](docs/README.md) | Guía de navegación modular, convenciones y mapa de ingeniería. |
| **Visión de Producto & Alcance** | [`docs/00-vision/product-vision.md`](docs/00-vision/product-vision.md) | Propósito dual (comercial + tesis), problemas reales y propuesta de valor. |
| **PRD Maestro v3.2** | [`docs/01-product/PRD.md`](docs/01-product/PRD.md) | Requisitos funcionales (FR-XXX) y no funcionales (NFR-XXX) consolidados. |
| **Estado del Roadmap** | [`docs/01-product/roadmap.md`](docs/01-product/roadmap.md) | Tablero de control en tiempo real: Fases 0 a 4 completadas, Fase 5 tesis (805 tests pasando). |
| **Sistema de Diseño 3.2** | [`docs/02-design/design-system.md`](docs/02-design/design-system.md) | Tokens semánticos, 6 ThemePreset, 6 BackgroundPreset, Estudio Hex/HSL y tipografía. |
| **Impeccable Design JSON** | [`.impeccable/design.json`](.impeccable/design.json) | Esquema estructurado de tokens, componentes y reglas para auditoría visual Impeccable. |
| **Arquitectura del Sistema** | [`docs/03-architecture/system-overview.md`](docs/03-architecture/system-overview.md) | Clean Architecture + MVI, 3 capas, UDF, flujo de coroutines y Hilt DI. |
| **Modelo de Datos Room v9** | [`docs/03-architecture/data-model.md`](docs/03-architecture/data-model.md) | Esquema SQLite v9, entidades, DAOs, migraciones 1➔...➔9, DataStore y GlobalSnapshot. |
| **Registro de Decisiones (ADRs)** | [`docs/03-architecture/adr/`](docs/03-architecture/adr/) | Decisiones arquitecturales vigentes (ADR-0001 a ADR-0007). |
| **Estrategia de Pruebas** | [`docs/04-quality-privacy/testing-strategy.md`](docs/04-quality-privacy/testing-strategy.md) | Pirámide de pruebas y suite automatizada de 805 tests unitarios. |
| **Privacidad & Soberanía** | [`docs/04-quality-privacy/privacy-offline-first.md`](docs/04-quality-privacy/privacy-offline-first.md) | Principios de privacidad radical, cero Firestore y respaldo privado en Drive. |
| **Landing Page Showcase** | [`landing/index.html`](landing/index.html) • [`https://notifast.pages.dev/`](https://notifast.pages.dev/) | Web interactiva en Cloudflare Pages (CI/CD en `.github/workflows/ci.yml`) con Estudio de Temas en Vivo y Simulador de Notificaciones. |

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
- Toda feature nueva o corrección de cálculo matemático (ej. promedios `GradePolicy`, pesos, alarmas, generador de paletas) debe contar con tests unitarios reproducibles.

### 🟣 4. Verification Before Completion (Evidencia antes de Afirmaciones)
- **Nunca dar por terminada una tarea sin verificar:** Ejecutar `./gradlew assembleDebug` y las suites de test correspondientes (`./gradlew testDebugUnitTest`), o el detector de Impeccable para cambios web.
- Si el código no compila o no pasa pruebas, el trabajo **NO** está terminado.

### 📋 5. Mantenimiento de Estado y Generación de Kickoff Siguiente
- Tras culminar cualquier tarea del roadmap, es **obligatorio actualizar** el archivo [`docs/01-product/roadmap.md`](docs/01-product/roadmap.md).
- Al completar el **100% de una fase**, el agente DEBE **generar automáticamente el documento `docs/01-product/KICKOFF_FASE_{N+1}.md`** con el prompt detallado y la guía técnica para la fase subsiguiente.

---

## 🏗️ 4. Arquitectura del Sistema

El proyecto implementa **Clean Architecture** estricta dividida en tres capas principales (`360` archivos Kotlin de producción, `~44,260` LOC):

```
app/src/main/java/com/notifast/app/
├── data/                    # CAPA DE DATOS (Offline-First + Drive Cloud Backup)
│   ├── backup/              # Google Drive REST API v3 (appDataFolder), CloudDiffEngine, GlobalSnapshot
│   ├── local/               # Room Database v4 (Entities, DAOs, Migrations 1->2->3->4), DataStore Prefs
│   ├── model/               # Data classes inmutables (Note, NoteBlock, Evaluation, Schedule, SubjectData, Teacher)
│   ├── notification/        # ExactAlarmSchedulerImpl, AlarmTriggerReceiver, BootCompletedReceiver, Workers
│   └── repository/          # Implementaciones locales (Room + DataStore) y CloudBackupRepositoryImpl
│
├── domain/                  # CAPA DE DOMINIO (Reglas de Negocio Puras)
│   ├── model/               # Modelos de dominio (ThemePreset, BackgroundPreset, GradeScale, AcademicPack, etc.)
│   ├── notification/        # ExactAlarmScheduler, NotificationSender, NotificationChannelsManager
│   ├── policy/              # GradePolicy (fórmula única de promedios y ponderaciones)
│   ├── repository/          # Interfaces de repositorios (Inversión de dependencias)
│   ├── usecase/             # Casos de uso (CalculateAcademicStats, Export/ImportSubjectPack, ScheduleAlerts, etc.)
│   └── util/                # Utilidades de dominio (ReminderTimeCalculator, TeacherFormatters, TextSpanOps)
│
├── presentation/            # CAPA DE PRESENTACIÓN (UI + Estado MVI)
│   ├── atoms/               # Design System: Componentes atómicos reutilizables (NotiFastCard, NotiFastButton, etc.)
│   ├── feature/             # 17 Features modulares (MVVM + MVI Contract):
│   │   ├── notes/           # Hub principal: Cuaderno, Materias y Directorio de Profesores
│   │   ├── noteeditor/      # Editor por bloques (OneNote-like), Markdown, LaTeX, Fotos Anotadas (ModalBottomSheet)
│   │   ├── schedules/       # Agenda activa, Calendario mensual de evaluaciones y FAB expandible
│   │   ├── grades/          # Calificaciones multiescala, Salud Académica y Simulador Interactivo de Meta
│   │   ├── settings/        # Ajustes: Estudio de Temas AAA (Acentos/Lienzos/Hex), Notificaciones y Respaldo
│   │   ├── cloud/           # CloudHubSheet: Sincronización diferencial SHA-256 con Google Drive
│   │   ├── share/           # Exportación/Importación P2P (.notifast y Código QR Offline)
│   │   ├── search/          # Búsqueda Global instantánea multi-entidad
│   │   └── subjectdetail/   # Panorama de materia, cuerpo docente, apuntes y evaluaciones
│   ├── navigation/          # Rutas type-safe (Screen.kt), Deep-Linking y grafo de navegación (NavGraph.kt)
│   └── theme/               # Sistema de diseño 3.2: ThemePaletteGenerator, Color, Typography, ThemeUtils
│
└── di/                      # Inyección de Dependencias (Hilt Modules)
    ├── AppModule.kt         # Proveedores de Room, DataStore, WorkManager, CredentialManager y OkHttp
    ├── RepositoryModule.kt  # Binds de interfaces de repositorios a implementaciones locales/Drive
    └── UseCaseModule.kt     # Inyección de casos de uso
```

---

## 📐 5. Reglas de Código y Estándares de Higiene

### 🧹 1. Límite de Tamaño y Responsabilidad Única (SRP)
- **Máximo ~250 líneas por archivo composable.**
- Si una pantalla o componente supera las 250 líneas, **debe subdividirse** extrayendo sub-composables a `components/` o `atoms/`.
- Cero composables "Dios" que mezclen layout, lógica, filtros y diálogos en un solo bloque.

### 🧠 2. Principios KISS & YAGNI y Estado MVI
- **Un solo StateFlow por ViewModel:** `val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()`.
- **Prohibido:** Crear StateFlows paralelos o huérfanos (`_saved`, `_isLoading`) fuera del `UiState`.
- **Punto único de interacción UI ➔ ViewModel:** `onEvent(event: FeatureEvent)`.

### 🎨 3. Design Tokens, Motor de Temas y Componentes Atómicos
- **Apego al Tema de la App:** Usar siempre `MaterialTheme.colorScheme.isDark` (`background.luminance() < 0.5f`) en lugar de `isSystemInDarkTheme()` para respetar el modo Claro/Oscuro seleccionado dentro de NotiFast.
- **Prohibido:** Hardcodear colores arbitrarios o parsear strings Hex directamente en composables sin pasar por `ThemePaletteGenerator` o `ThemeUtils`.
- **Uso de Átomos:** Usar **SIEMPRE** los componentes existentes en `presentation/atoms/` (`NotiFastButton`, `NotiFastCard`, `NotiFastTextField`, `NotiFastDialog`, `NotiFastTopBar`, `NotiFastEmptyState`, `NotiFastExpandableFab`) antes de crear componentes ad-hoc.

### 🌐 4. Cero Strings Hardcodeados en UI
- Todo texto visible para el usuario DEBE residir en `res/values/strings.xml` y consumirse mediante `stringResource(R.string.xxx)`.
- Idioma de los recursos de UI: **Español**. Idioma del código fuente (variables, funciones, clases): **Inglés**.

### ⚖️ 5. Cero Lógica de Negocio en Composables
- Los `@Composable` son **exclusivamente declarativos**.
- Prohibido realizar cálculos matemáticos (promedios ponderados, proyecciones de notas, diffing SHA-256) dentro de la composición. Toda la lógica pertenece al ViewModel, `GradePolicy` o Use Cases.

### 🚨 6. Manejo Riguroso de Excepciones
- **Prohibido tragar errores:** Cero bloques `try { ... } catch (e: Exception) {}` vacíos.
- Toda operación de escritura o red debe envolverse en `try/catch` y retornar `Result<T>`.

### ⚡ 7. Compose y Ciclo de Vida
- Usar `collectAsStateWithLifecycle()` para observar StateFlows.
- Usar `LaunchedEffect` para side-effects (navegación, snackbars, permisos). **NUNCA** disparar navegación directamente durante la fase de composición.
- Cero wildcard imports (`import foo.bar.*`) — importar cada clase explícitamente.

---

## 🚫 6. Anti-Patrones Estrictamente Prohibidos

| # | Anti-Patrón | Acción Correctiva Obligatoria |
|---|---|---|
| 1 | Poner cálculos o lógica de negocio en Composables | Extraer a ViewModel, `GradePolicy` o a un Use Case en `domain/usecase/`. |
| 2 | Crear StateFlows dispersos fuera de `UiState` | Unificar todo el estado en un único `data class FeatureUiState`. |
| 3 | Usar `isSystemInDarkTheme()` en componentes UI | Usar `MaterialTheme.colorScheme.isDark` para respetar el tema interno de la app. |
| 4 | Tragar excepciones con `catch(e) {}` vacío | Loguear el error y propagar `Result.failure(e)` al ViewModel. |
| 5 | Hardcodear colores o textos en la UI | Usar `MaterialTheme.colorScheme` y `stringResource(R.string.*)`. |
| 6 | Archivos composable > 250 líneas | Modularizar y extraer sub-componentes a `components/` o `atoms/`. |
| 7 | Navegar durante la composición | Envolver la navegación en `LaunchedEffect(...)`. |
| 8 | Modificar código sin investigar la causa raíz | Seguir el protocolo `systematic-debugging`. |
| 9 | Dar una tarea por completada sin compilar | Ejecutar `./gradlew assembleDebug` y verificar tests (`684` tests). |
| 10 | Versionar credenciales o secrets | Mantener `google-services.json` y `local.properties` ignorados y variables en `BuildConfig`. |
