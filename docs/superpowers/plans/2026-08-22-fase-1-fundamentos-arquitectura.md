# 🚀 Plan de Implementación — Fase 1: Fundamentos de Arquitectura

> **Para agentes ejecutores:** REQUIRED SUB-SKILL: Usar `superpowers:subagent-driven-development` (recomendado) o `superpowers:executing-plans` para implementar este plan tarea por tarea. Cada paso utiliza sintaxis de checkbox (`- [ ]`) para tracking.

**Goal:** Consolidar la Clean Architecture en NotiFast mediante la introducción de Use Cases para lógica compuesta, refactorizar el sistema de diseño Material 3 con Dark Mode real y colores corporativos (`Ruby` & `Aqua`), modularizar pantallas monolíticas a ≤ 250 LOC, persistir preferencias con DataStore (`UserPrefsRepository`) y establecer un arnés exhaustivo de pruebas unitarias e integración con MockK, Turbine y Coroutines Test.

**Architecture:** Clean Architecture estricta en 3 capas (`data`, `domain`, `presentation`).
- Capa de dominio pura con Use Cases inyectables (`@Inject constructor`) e interfaces de repositorio.
- Capa de datos con implementaciones para Firebase Firestore y DataStore Preferences.
- Capa de presentación con arquitectura MVI/MVVM (UiState único + Eventos sellados), tokens de diseño Impeccable y componentes atómicos reutilizables.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3 (BOM 2024.09.00), Hilt 2.59, Jetpack DataStore Preferences 1.1.2, MockK 1.13.8, Turbine 1.0.0, Kotlinx Coroutines Test 1.9.0.

**Spec:** [`docs/prd/KICKOFF_FASE_1.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_1.md) y [`docs/prd/PRD-MAESTRO-NOTIFAST.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)

## Global Constraints

- **Version floors:** Kotlin 2.0.21, AGP 9.0.1, CompileSdk 36, MinSdk 26, JVM target 11.
- **Dependency limits:** Usar catálogo centralizado `gradle/libs.versions.toml`. No agregar librerías redundantes.
- **Modularidad de código:** Ningún archivo composable debe superar las ~250 líneas de código. Subdividir en `components/`.
- **Diseño Impeccable:** Paleta `Ruby` (`#BF092F`) y `Aqua` (`#3B9797`), Dark theme real (`#10161A` / `#1B242A`), touch targets ≥ 48dp y espaciado de 8dp.
- **Higiene de UI:** Cero strings hardcodeados; todo texto visible debe residir en `res/values/strings.xml`.
- **Manejo de Errores:** Operaciones puntuales retornan `Result<T>` y streams continuos retornan `Flow<T>`. Cero bloques `catch` vacíos.
- **TDD & Verificación:** Red-Green-Refactor en lógica de negocio. Compilar con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.

---

## 📋 Tareas de Implementación

### ─── BLOQUE A: SPRINT 1.1 (Arquitectura y Dominio) ───

---

### Tarea 1: Use Cases de Dominio Compuestos (1.1.1)

**Files:**
- Create: `app/src/main/java/com/notifast/app/data/model/SubjectDetailData.kt`
- Create: `app/src/main/java/com/notifast/app/data/model/NoteWithSubject.kt`
- Create: `app/src/main/java/com/notifast/app/domain/usecase/DeleteSubjectCascadeUseCase.kt`
- Create: `app/src/main/java/com/notifast/app/domain/usecase/GetSubjectDetailUseCase.kt`
- Create: `app/src/main/java/com/notifast/app/domain/usecase/GetNotesWithSubjectInfoUseCase.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/DeleteSubjectCascadeUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/GetSubjectDetailUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/GetNotesWithSubjectInfoUseCaseTest.kt`

**Interfaces:**
- Consumes: `NoteRepository`, `EvaluationRepository`, `ScheduleRepository`
- Produces:
  - `DeleteSubjectCascadeUseCase(userId: String, subjectName: String): Result<Unit>`
  - `GetSubjectDetailUseCase(userId: String, subjectName: String): Flow<SubjectDetailData>`
  - `GetNotesWithSubjectInfoUseCase(userId: String): Flow<List<NoteWithSubject>>`

- [ ] **Step 1: Write failing tests for DeleteSubjectCascadeUseCase, GetSubjectDetailUseCase, and GetNotesWithSubjectInfoUseCase**
  Escribir pruebas unitarias con MockK y Turbine validando emisión reactiva y cascada de eliminación.
- [ ] **Step 2: Run test to verify it fails**
  Run: `export JAVA_HOME=/home/p0xd/.jdks/temurin-17; export PATH=$JAVA_HOME/bin:$PATH; ./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.*"`
  Expected: FAIL con unresolved references para los nuevos Use Cases.
- [ ] **Step 3: Implement domain models and Use Cases**
  Implementar `DeleteSubjectCascadeUseCase`, `GetSubjectDetailUseCase`, `GetNotesWithSubjectInfoUseCase` anotados con `@Inject constructor`.
- [ ] **Step 4: Run test to verify it passes**
  Run: `export JAVA_HOME=/home/p0xd/.jdks/temurin-17; export PATH=$JAVA_HOME/bin:$PATH; ./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.*"`
  Expected: PASS (100% de tests en verde).
- [ ] **Step 5: Commit**
  `git add app/src/main/java/com/notifast/app/domain/usecase/ app/src/main/java/com/notifast/app/data/model/ app/src/test/java/com/notifast/app/domain/usecase/`
  `git commit -m "feat(domain): add composite use cases for subject cascade and details"`

---

### Tarea 2: Refactor Material 3 Theme con Impeccable & Dark Mode Real (1.1.2)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/theme/color/Color.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/theme/type/Type.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/theme/theme/Theme.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/theme/ThemeUtils.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/theme/ThemeUtilsTest.kt`

**Interfaces:**
- Produces: `DarkColorScheme`, `LightColorScheme`, `ThemeUtils.getAccessibleColor(hex: String, isDark: Boolean): Color`

- [ ] **Step 1: Write test for accessible color contrast in ThemeUtilsTest.kt**
  Escribir pruebas unitarias que verifiquen que `ThemeUtils.getAccessibleColor` retorne contrastes legibles tanto en Dark como en Light Mode.
- [ ] **Step 2: Run test to verify behavior**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.theme.ThemeUtilsTest"`
- [ ] **Step 3: Implement semantic palette in Color.kt and Theme.kt**
  Definir `Ruby`, `Aqua`, `BackgroundDark = Color(0xFF10161A)`, `SurfaceDark = Color(0xFF1B242A)`, `SurfaceVariantDark = Color(0xFF25333C)`, `OnSurfaceDark = Color(0xFFECEFF1)`, `OnSurfaceVariantDark = Color(0xFF94A3B8)`.
- [ ] **Step 4: Update Typography in Type.kt and verify build**
  Ajustar escala tipográfica y ejecutar `./gradlew testDebugUnitTest`.
- [ ] **Step 5: Commit**
  `git add app/src/main/java/com/notifast/app/presentation/theme/ app/src/test/java/com/notifast/app/presentation/theme/`
  `git commit -m "refactor(ui): update Material 3 theme with Impeccable tokens and dark mode"`

---

### Tarea 3: Version Catalog y DataStore Preferences (1.1.3 & 1.1.5 Preparación)

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add datastore preferences dependency in gradle/libs.versions.toml**
  Agregar `androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version = "1.1.2" }`.
- [ ] **Step 2: Add implementation in app/build.gradle.kts**
  Agregar `implementation(libs.androidx.datastore.preferences)` en bloque `dependencies`.
- [ ] **Step 3: Verify build synchronization**
  Run: `export JAVA_HOME=/home/p0xd/.jdks/temurin-17; export PATH=$JAVA_HOME/bin:$PATH; ./gradlew assembleDebug`
  Expected: BUILD SUCCESSFUL.
- [ ] **Step 4: Commit**
  `git add gradle/libs.versions.toml app/build.gradle.kts`
  `git commit -m "build: add androidx.datastore.preferences dependency"`

---

### Tarea 4: `UserPrefsRepository` con DataStore Preferences (1.1.5)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/repository/UserPrefsRepository.kt`
- Create: `app/src/main/java/com/notifast/app/data/repository/UserPrefsRepositoryImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/di/AppModule.kt`
- Modify: `app/src/main/java/com/notifast/app/di/RepositoryModule.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/UserPrefsRepositoryTest.kt`

**Interfaces:**
- Produces: `UserPrefsRepository` con:
  - `val isDarkModeFlow: Flow<Boolean?>`
  - `suspend fun setDarkMode(enabled: Boolean?): Result<Unit>`
  - `val defaultGradeScaleFlow: Flow<String>`
  - `suspend fun setDefaultGradeScale(scale: String): Result<Unit>`
  - `val showCompletedEvaluationsFlow: Flow<Boolean>`
  - `suspend fun setShowCompletedEvaluations(show: Boolean): Result<Unit>`

- [ ] **Step 1: Write failing unit test for UserPrefsRepository**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Implement UserPrefsRepository interface & UserPrefsRepositoryImpl**
  Implementar persistencia con `DataStore<Preferences>` y registrar en `AppModule` y `RepositoryModule`.
- [ ] **Step 4: Run test to verify it passes**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.UserPrefsRepositoryTest"`
  Expected: PASS.
- [ ] **Step 5: Commit**
  `git add app/src/main/java/com/notifast/app/domain/repository/UserPrefsRepository.kt app/src/main/java/com/notifast/app/data/repository/UserPrefsRepositoryImpl.kt app/src/main/java/com/notifast/app/di/ app/src/test/java/com/notifast/app/data/repository/`
  `git commit -m "feat(data): implement UserPrefsRepository with DataStore Preferences"`

---

### Tarea 5: Modularización de Pantallas Monolíticas a ≤ 250 LOC (1.1.4)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/SubjectCard.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/SubjectCardHeader.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/SubjectCardDashboard.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/SubjectCardEvaluations.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/AddEvaluationScreen.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/components/EvaluationFormFields.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editevaluation/EditEvaluationScreen.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/editevaluation/components/EditEvaluationFormFields.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/calendar/CalendarScreen.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/calendar/components/CalendarHeader.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/calendar/components/CalendarDayTimeline.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/calendar/components/EvaluationListItem.kt`

- [ ] **Step 1: Subdivide SubjectCard.kt extracting subcomponents to notes/components/**
- [ ] **Step 2: Subdivide AddEvaluationScreen.kt and EditEvaluationScreen.kt extracting form fields**
- [ ] **Step 3: Subdivide CalendarScreen.kt extracting header, day timeline and list item**
- [ ] **Step 4: Verify line count and build**
  Verificar que ningún archivo composable supere 250 LOC y compilar con `./gradlew assembleDebug`.
- [ ] **Step 5: Commit**
  `git add app/src/main/java/com/notifast/app/presentation/feature/`
  `git commit -m "refactor(presentation): modularize monolithic composables to <= 250 LOC"`

---

### Tarea 6: Limpieza y Estandarización de Firmas Reactivas (1.1.6)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/repository/NoteRepository.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/NoteRepositoryImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/repository/EvaluationRepository.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/EvaluationRepositoryImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/repository/ScheduleRepository.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/ScheduleRepositoryImpl.kt`

- [ ] **Step 1: Audit and standardize repository signatures**
  Asegurar que todas las consultas continuas retornen `Flow<T>` (sin `suspend`) y operaciones one-shot sean `suspend fun ...: Result<T>`.
- [ ] **Step 2: Update ViewModels to align with standardized signatures**
- [ ] **Step 3: Run all unit tests to confirm zero regressions**
  Run: `./gradlew testDebugUnitTest`
  Expected: PASS.
- [ ] **Step 4: Commit**
  `git add app/src/main/java/com/notifast/app/domain/repository/ app/src/main/java/com/notifast/app/data/repository/`
  `git commit -m "refactor(data): standardize reactive and suspend repository signatures"`

---

### ─── BLOQUE B: SPRINT 1.2 (Testing Automatizado) ───

---

### Tarea 7: Infraestructura de Testing y MainDispatcherRule (1.2.1)

**Files:**
- Create: `app/src/test/java/com/notifast/app/util/MainDispatcherRule.kt`
- Create: `app/src/test/java/com/notifast/app/util/TestModels.kt`

- [ ] **Step 1: Implement MainDispatcherRule using StandardTestDispatcher and UnconfinedTestDispatcher**
- [ ] **Step 2: Create test model factories for Note, Evaluation, Schedule, and SubjectData**
- [ ] **Step 3: Verify execution with sample test**
- [ ] **Step 4: Commit**
  `git add app/src/test/java/com/notifast/app/util/`
  `git commit -m "test: add MainDispatcherRule and test model fixtures"`

---

### Tarea 8: Unit Tests Exhaustivos para Use Cases (1.2.2)

**Files:**
- Modify/Audit: `app/src/test/java/com/notifast/app/domain/usecase/CalculateWeightedAverageUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/DeleteSubjectCascadeUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/GetSubjectDetailUseCaseTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/GetNotesWithSubjectInfoUseCaseTest.kt`

- [ ] **Step 1: Add edge cases for all Use Cases (empty lists, null grades, 0 weight, network errors)**
- [ ] **Step 2: Run test suite to verify coverage**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.*"`
  Expected: PASS.
- [ ] **Step 3: Commit**
  `git add app/src/test/java/com/notifast/app/domain/usecase/`
  `git commit -m "test(domain): complete edge case unit tests for all Use Cases"`

---

### Tarea 9: Unit Tests para ViewModels Core (1.2.3)

**Files:**
- Create: `app/src/test/java/com/notifast/app/presentation/feature/notes/NotesViewModelTest.kt`
- Modify: `app/src/test/java/com/notifast/app/presentation/feature/grades/GradesViewModelTest.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/calendar/CalendarViewModelTest.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/login/LoginViewModelTest.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModelTest.kt`

- [ ] **Step 1: Write NotesViewModelTest for search, filtering, and tab selection using Turbine**
- [ ] **Step 2: Write CalendarViewModelTest for date selection and evaluation timeline**
- [ ] **Step 3: Write LoginViewModelTest and AddScheduleViewModelTest**
- [ ] **Step 4: Run all ViewModel tests**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.*"`
  Expected: PASS.
- [ ] **Step 5: Commit**
  `git add app/src/test/java/com/notifast/app/presentation/feature/`
  `git commit -m "test(presentation): add unit test suites for core ViewModels with Turbine"`

---

### Tarea 10: Tests de Integración y Cierre de Fase 1 (1.2.4 & Transición)

**Files:**
- Create: `app/src/test/java/com/notifast/app/integration/SubjectCascadeIntegrationTest.kt`
- Create: `app/src/test/java/com/notifast/app/integration/UserPreferencesFlowIntegrationTest.kt`
- Modify: `docs/prd/STATUS.md`
- Create: `docs/prd/KICKOFF_FASE_2.md`
- Modify: `AGENTS.md`
- Modify: `README.md`

- [ ] **Step 1: Write integration tests for cascade deletion and preferences flow**
- [ ] **Step 2: Run complete test suite and assemble build**
  Run: `export JAVA_HOME=/home/p0xd/.jdks/temurin-17; export PATH=$JAVA_HOME/bin:$PATH; ./gradlew testDebugUnitTest && ./gradlew assembleDebug`
  Expected: ALL TESTS PASS & BUILD SUCCESSFUL.
- [ ] **Step 3: Update docs/prd/STATUS.md marking Phase 1 as 100% completed**
- [ ] **Step 4: Generate docs/prd/KICKOFF_FASE_2.md for Phase 2: Features Core**
- [ ] **Step 5: Update references in README.md and AGENTS.md**
- [ ] **Step 6: Commit**
  `git add app/src/test/java/com/notifast/app/integration/ docs/ AGENTS.md README.md`
  `git commit -m "chore(release): complete Phase 1 architecture foundations and kickoff Phase 2"`

---

## 🔍 Plan de Verificación

### Pruebas Automatizadas
```bash
export JAVA_HOME=/home/p0xd/.jdks/temurin-17; export PATH=$JAVA_HOME/bin:$PATH
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

### Verificación de Métricas e Higiene
- Validar con script/inspección que ningún archivo composable exceda ~250 líneas.
- Validar cero strings quemados en composables.
- Validar cumplimiento de contraste y paleta `Ruby` / `Aqua`.
