# Sincronización de Portadas, Profesores y Feedback Granular Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reforzar la sincronización con Google Drive permitiendo sincronizar las portadas del área de estudio (sin archivos pesados), sincronizar los profesores asignados a cada materia, y dotar a `CloudHubSheet` de mayor feedback y granularidad para que el usuario elija y visualice lo que sincroniza.

**Architecture:** Clean Architecture + MVI. Se extiende el pipeline de snapshots (`SubjectSnapshot` y `SubjectPackage`) para incluir a los docentes vinculados a cada materia, se crea el subsistema de respaldo para portadas (`CoverBackupStore`, `writeCovers`/`readCovers`, `UploadCoversUseCase`, `RestoreCoversUseCase`), y se actualiza `BuildCloudOverviewUseCase` y la interfaz MVI de `CloudHubSheet` con el componente `CoverDraftsRow`, micro-insignias de profesores y feedback dinámico de selección y progreso.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3, Room SQLite Database v8, Google Drive REST API v3 (`appDataFolder`), Coroutines & Flow, Turbine, JUnit 4, MockK, Robolectric.

**Spec:** `docs/superpowers/specs/2026-10-19-cloud-backup-covers-and-teachers-sync-design.md`

## Global Constraints
- **100% Offline-First:** Toda la funcionalidad local permanece operativa sin conexión a internet.
- **Cuota de nube protegida:** En el Área de Estudio **únicamente se sincronizan los borradores de portadas (`CoverDraft`)**, bajo ninguna circunstancia se sincronizan archivos binarios o documentos pesados de `study_docs/`.
- **Deduplicación de profesores:** Al restaurar materias, los profesores se fusionan sin duplicar utilizando la clave única (`phone`, `email` o `nombre completo`).
- **Compatibilidad hacia atrás:** Los respaldos existentes sin lista de profesores o sin portadas deben seguir siendo leídos sin errores (valores por defecto vacíos).
- **Límite de Composables:** Máximo ~250 líneas por archivo composable. Separar componentes en archivos atómicos.
- **Cero strings hardcodeados:** Todos los textos en español en `res/values/strings.xml`.

## Review Focus
1. Archivo de respaldo de materia antiguo (sin campo `teachers`): Debe deserializarse correctamente con `teachers = emptyList()`.
2. Restauración de materia con profesor ya existente en el dispositivo: No debe crear un profesor duplicado; debe conservar sus datos y agregar la materia a sus `subjectNames`.
3. Sincronización de portadas cuando no hay borradores locales: No debe fallar; debe reportar 0 portadas locales y permitir restaurar si existen en la nube.
4. Archivos pesados en el área de estudio: Ningún `.docx` o `.pdf` debe incluirse en el archivo `covers.nfbak`.
5. Selección granular en UI: El usuario puede seleccionar solo portadas, solo preferencias, solo materias o cualquier combinación, y los botones de subir/restaurar y la barra de estado deben reaccionar con precisión.

---

### Task 1: Soporte de Profesores en `SubjectSnapshot`, `SubjectPackage`, `ContentHasher` y `BackupArchiveCodec`

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/backup/SubjectSnapshot.kt`
- Modify: `app/src/main/java/com/notifast/app/data/model/package_data/SubjectPackage.kt`
- Modify: `app/src/main/java/com/notifast/app/data/backup/ContentHasher.kt`
- Modify: `app/src/main/java/com/notifast/app/data/backup/BackupArchiveCodec.kt`
- Test: `app/src/test/java/com/notifast/app/data/backup/SubjectSnapshotTeachersTest.kt`

**Interfaces:**
- Produces: `SubjectSnapshot.teachers: List<Teacher>`, `SubjectPackage.teachers: List<Teacher>`, `ContentHasher.hash(snapshot: SubjectSnapshot)` con docentes, `writeSubject`/`readSubject` con docentes.

- [ ] **Step 1: Escribir el test que falla para verificar serialización y hash de snapshot con profesores**
Crear `app/src/test/java/com/notifast/app/data/backup/SubjectSnapshotTeachersTest.kt` verificando que un snapshot con profesores se escribe en `writeSubject`, se lee idéntico en `readSubject` y genera hashes deterministas que cambian si cambian los datos del docente.

- [ ] **Step 2: Ejecutar el test y comprobar que falla**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.backup.SubjectSnapshotTeachersTest"`
Expected: FAIL (campos faltantes en `SubjectSnapshot` o `SubjectPackage`).

- [ ] **Step 3: Implementar soporte de profesores en los modelos y codecs**
1. Agregar `val teachers: List<Teacher> = emptyList()` a `SubjectSnapshot` y `SubjectPackage`.
2. En `BackupArchiveCodec.writeSubject`: pasar `teachers = snapshot.teachers` a `SubjectPackage`.
3. En `BackupArchiveCodec.readSubject`: pasar `teachers = pkg.teachers` a `SubjectSnapshot`.
4. En `ContentHasher.hash(snapshot)`: agregar sección `teachers` (ordenada por `id`, con nombre, apellido, teléfono, email normalizados).

- [ ] **Step 4: Ejecutar el test y comprobar que pasa**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.backup.SubjectSnapshotTeachersTest"`
Expected: PASS.

- [ ] **Step 5: Ejecutar suite de pruebas de backup para asegurar no-regresión**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.backup.*"`
Expected: PASS.

---

### Task 2: Integración de Profesores en `RoomSubjectBackupStore` (Lectura y Fusión al Restaurar)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/local/backup/RoomSubjectBackupStore.kt`
- Test: `app/src/test/java/com/notifast/app/data/local/backup/RoomSubjectBackupStoreTeachersTest.kt`

**Interfaces:**
- Consumes: `SubjectSnapshot.teachers`, `TeacherDao`, `TeacherEntity`.
- Produces: `RoomSubjectBackupStore.readSnapshot` incluye profesores asociados; `insertSnapshot` y `replaceSnapshot` fusionan los profesores en Room sin duplicados.

- [ ] **Step 1: Escribir el test que falla para lectura y restauración de materia con docentes**
Crear `app/src/test/java/com/notifast/app/data/local/backup/RoomSubjectBackupStoreTeachersTest.kt` comprobando que:
a) Al leer el snapshot de una materia, se obtienen los docentes que imparten esa materia o dictan sus clases.
b) Al restaurar/insertar el snapshot, los docentes se guardan en la tabla `teachers` del usuario, y si ya existían, no se duplican sino que se actualizan sus `subjectNames`.

- [ ] **Step 2: Ejecutar el test y comprobar que falla**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.backup.RoomSubjectBackupStoreTeachersTest"`
Expected: FAIL.

- [ ] **Step 3: Implementar lectura y fusión de profesores en `RoomSubjectBackupStore`**
1. Añadir `teacherDao: TeacherDao` al constructor de `RoomSubjectBackupStore` (ya disponible en Room / DatabaseModule).
2. En `readSnapshot`: consultar docentes del usuario, filtrar los que tengan `subject.name` en `subjectNames` o coincidan con `schedules.mapNotNull { it.teacher }`, y mapear a domain `Teacher`.
3. Crear función de fusión `mergeTeachers(userId, teachers, subjectName)` usando la clave de deduplicación de profesores.
4. Invocar `mergeTeachers` dentro de la transacción de `insertSnapshot` y `replaceSnapshot`.

- [ ] **Step 4: Ejecutar el test y verificar que pasa**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.backup.RoomSubjectBackupStoreTeachersTest"`
Expected: PASS.

- [ ] **Step 5: Ejecutar suite de `RoomSubjectBackupStoreTest`**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.backup.*"`
Expected: PASS.

---

### Task 3: Infraestructura de Respaldo de Portadas (`CoverBackupStore`, `BackupArchiveCodec`, `ContentHasher` y Drive Repo)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/repository/CoverBackupStore.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/backup/RoomCoverBackupStore.kt`
- Modify: `app/src/main/java/com/notifast/app/data/backup/BackupArchiveCodec.kt`
- Modify: `app/src/main/java/com/notifast/app/data/backup/ContentHasher.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/DriveCloudBackupRepositoryImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/di/RepositoryModule.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/model/cloud/CloudFile.kt`
- Test: `app/src/test/java/com/notifast/app/data/backup/CoverBackupArchiveTest.kt`

**Interfaces:**
- Produces: `CoverBackupStore` interface e implementación `RoomCoverBackupStore`, `BackupArchiveCodec.writeCovers` / `readCovers`, `ContentHasher.hashCovers`, constante `KIND_COVERS = "covers"`.

- [ ] **Step 1: Escribir el test que falla para archivo de portadas**
Crear `app/src/test/java/com/notifast/app/data/backup/CoverBackupArchiveTest.kt` comprobando el ciclo completo de `writeCovers` y `readCovers`, asegurando que almacena únicamente los borradores de plantilla (`CoverDraft`) con su formato (APA7, IEEE, etc.) y ningún archivo binario.

- [ ] **Step 2: Ejecutar el test y verificar que falla**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.backup.CoverBackupArchiveTest"`
Expected: FAIL.

- [ ] **Step 3: Implementar almacenamiento y códec de portadas**
1. Crear `CoverBackupStore` en dominio con `read(userId: String): Result<List<CoverDraft>>` y `apply(userId: String, drafts: List<CoverDraft>): Result<Unit>`.
2. Implementar `RoomCoverBackupStore` inyectando `CoverDraftDao`. En `apply`, upsertar los borradores recibidos para el `userId` destino.
3. Vincular `CoverBackupStore` en `RepositoryModule.kt`.
4. En `BackupArchiveCodec`: implementar `writeCovers` (empaquetando `manifest.json` con `kind = "covers"` y `covers.json`) y `readCovers`.
5. En `ContentHasher`: implementar `hashCovers(drafts: List<CoverDraft>): String`.
6. En `DriveCloudBackupRepositoryImpl`: añadir `const val KIND_COVERS = "covers"` y parsear `coverCount` en `listFiles()`.
7. En `CloudFile`: agregar `coverCount: Int = 0` y `teacherCount: Int = 0`.

- [ ] **Step 4: Ejecutar el test y verificar que pasa**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.backup.CoverBackupArchiveTest"`
Expected: PASS.

---

### Task 4: Casos de Uso `UploadCoversUseCase` y `RestoreCoversUseCase`

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/usecase/cloud/UploadCoversUseCase.kt`
- Create: `app/src/main/java/com/notifast/app/domain/usecase/cloud/RestoreCoversUseCase.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/cloud/UploadAndRestoreCoversUseCaseTest.kt`

**Interfaces:**
- Consumes: `CoverBackupStore`, `ContentHasher`, `BackupArchiveCodec`, `CloudBackupRepository`, `GoogleAccountRepository`.
- Produces: `UploadCoversUseCase(userId, existing, cacheDir): Result<Unit>`, `RestoreCoversUseCase(userId, file, cacheDir): Result<Unit>`.

- [ ] **Step 1: Escribir el test que falla para UploadCovers y RestoreCovers**
Crear `app/src/test/java/com/notifast/app/domain/usecase/cloud/UploadAndRestoreCoversUseCaseTest.kt` mockeando el repositorio en la nube y verificando:
a) `UploadCoversUseCase` omite la subida si el hash local coincide con el de la nube (no-op).
b) `UploadCoversUseCase` sube `covers.nfbak` con metadatos apropiados (`kind="covers"`, `coverCount`, hash) si hay cambios.
c) `RestoreCoversUseCase` descarga el zip y aplica los borradores a través de `CoverBackupStore`.

- [ ] **Step 2: Ejecutar el test y verificar que falla**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.cloud.UploadAndRestoreCoversUseCaseTest"`
Expected: FAIL.

- [ ] **Step 3: Implementar `UploadCoversUseCase` y `RestoreCoversUseCase`**
1. Crear `UploadCoversUseCase` siguiendo el patrón limpio de `UploadGlobalDataUseCase`.
2. Crear `RestoreCoversUseCase` siguiendo el patrón de `RestoreGlobalDataUseCase`.

- [ ] **Step 4: Ejecutar el test y verificar que pasa**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.cloud.UploadAndRestoreCoversUseCaseTest"`
Expected: PASS.

---

### Task 5: Integración en `BuildCloudOverviewUseCase` (Estado de Portadas y Conteo de Profesores)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/model/cloud/CloudSubjectStatus.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/cloud/BuildCloudOverviewUseCase.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/cloud/UploadSubjectsUseCase.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/cloud/BuildCloudOverviewCoversAndTeachersTest.kt`

**Interfaces:**
- Consumes: `CoverBackupStore`, `hasher`, `cloud.listFiles()`.
- Produces: `CloudOverview(subjects, global, covers, localCoverCount, coversStatus)`, `CloudSubjectItem.teacherCount`.

- [ ] **Step 1: Escribir el test que falla para overview con portadas y conteo de profesores**
Crear `app/src/test/java/com/notifast/app/domain/usecase/cloud/BuildCloudOverviewCoversAndTeachersTest.kt` verificando que:
a) Cada `CloudSubjectItem` tiene el `teacherCount` exacto obtenido de los datos locales o remotos.
b) `CloudOverview` incluye el `CloudFile` de portadas (`kind = "covers"`), la cantidad de portadas locales y el `coversStatus` correcto (SYNCED, LOCAL_ONLY, LOCAL_CHANGES, CLOUD_ONLY).

- [ ] **Step 2: Ejecutar el test y verificar que falla**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.cloud.BuildCloudOverviewCoversAndTeachersTest"`
Expected: FAIL.

- [ ] **Step 3: Actualizar `BuildCloudOverviewUseCase` y `UploadSubjectsUseCase`**
1. En `CloudOverview`: añadir `val covers: CloudFile? = null`, `val localCoverCount: Int = 0`, `val coversStatus: CloudSubjectStatus = CloudSubjectStatus.LOCAL_ONLY`.
2. En `CloudSubjectItem`: agregar `val teacherCount: Int = 0`.
3. En `BuildCloudOverviewUseCase`: inyectar `CoverBackupStore`. Calcular `localCovers`, `localCoversHash`, obtener `cloudCovers` y clasificar `coversStatus`. Asignar `teacherCount` en `CloudSubjectItem`.
4. En `UploadSubjectsUseCase`: añadir `props["teacherCount"] = snapshot.teachers.size.toString()`.

- [ ] **Step 4: Ejecutar el test y verificar que pasa**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.cloud.BuildCloudOverviewCoversAndTeachersTest"`
Expected: PASS.

- [ ] **Step 5: Ejecutar suite de pruebas de `BuildCloudOverviewUseCaseTest` y `UploadSubjectsUseCaseTest`**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.cloud.*"`
Expected: PASS.

---

### Task 6: UI y Feedback MVI en `CloudHubSheet` (Strings, Micro-Badges, CoverDraftsRow y Selección Dinámica)

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/cloud/CloudContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/cloud/CloudViewModel.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/cloud/components/CoverDraftsRow.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/cloud/components/DataBreakdownBadges.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/cloud/components/CloudSubjectList.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/cloud/components/CloudActionBar.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/cloud/CloudHubSheet.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/cloud/CloudViewModelCoversTest.kt`

**Interfaces:**
- Produces: `CloudUiState.coversSelected`, `coversStatus`, `localCoverCount`; `CloudEvent.ToggleCovers`; nuevo composable `CoverDraftsRow`; badge de docentes en `DataBreakdownBadges`; feedback dinámico en `CloudActionBar`.

- [ ] **Step 1: Escribir el test que falla para `CloudViewModel` con selección de portadas**
Crear `app/src/test/java/com/notifast/app/presentation/feature/cloud/CloudViewModelCoversTest.kt` verificando que al emitir `CloudEvent.ToggleCovers` el estado actualiza `coversSelected`, recalcula `canUpload` y `canRestore`, y al pulsar `Upload` o `Restore` se ejecutan los casos de uso respectivos y se reporta en el summary.

- [ ] **Step 2: Ejecutar el test y comprobar que falla**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.cloud.CloudViewModelCoversTest"`
Expected: FAIL.

- [ ] **Step 3: Agregar strings de UI en `res/values/strings.xml`**
Añadir strings oficiales:
- `cloud_badge_teachers`: `"%1$d doc."`
- `cloud_covers_item_title`: `"Portadas de Estudio"`
- `cloud_covers_item_desc`: `"Plantillas APA 7, IEEE, UNERG y tesis institucional • Sin archivos pesados"`
- `cloud_covers_item_count`: `"%1$d portadas guardadas"`
- `cloud_covers_item_count_single`: `"1 portada guardada"`
- `cloud_covers_item_count_none`: `"Sin portadas guardadas"`
- `cloud_confirm_restore_covers_title`: `"¿Restaurar portadas de estudio?"`
- `cloud_confirm_restore_covers_msg`: `"Esto agregará o actualizará tus plantillas de portada con las guardadas en Google Drive."`
- `cloud_selection_feedback_full`: `"%1$d materias, ajustes y portadas seleccionadas"`
- `cloud_selection_feedback_no_covers`: `"%1$d materias y ajustes seleccionados"`
- `cloud_selection_feedback_no_global`: `"%1$d materias y portadas seleccionadas"`
- `cloud_selection_feedback_covers_only`: `"Portadas de estudio seleccionadas"`
- `cloud_selection_feedback_global_only`: `"Perfil y preferencias seleccionados"`

- [ ] **Step 4: Implementar `CoverDraftsRow.kt`**
Crear `app/src/main/java/com/notifast/app/presentation/feature/cloud/components/CoverDraftsRow.kt` (< 170 líneas), siguiendo la misma estética papercraft táctil AAA de `GlobalSettingsRow` con icono `Icons.Rounded.AutoStories` o `Description`, checkbox, título, descripción, conteo de portadas y `CloudStatusChip`.

- [ ] **Step 5: Actualizar `DataBreakdownBadges.kt`**
Agregar micro-badge para `item.teacherCount > 0` con icono `Icons.Rounded.School` o `Person` y string `cloud_badge_teachers`.

- [ ] **Step 6: Actualizar `CloudContract.kt` y `CloudViewModel.kt`**
1. En `CloudUiState`: agregar `val covers: CloudFile? = null`, `val coversSelected: Boolean = false`, `val coversStatus: CloudSubjectStatus = CloudSubjectStatus.LOCAL_ONLY`, `val localCoverCount: Int = 0`.
2. En `CloudEvent`: agregar `data object ToggleCovers : CloudEvent`.
3. En `CloudViewModel`: inyectar `UploadCoversUseCase` y `RestoreCoversUseCase`. Manejar `ToggleCovers`, recalcular `canUpload` y `canRestore` considerando `coversSelected`, ejecutar subida y restauración de portadas con su respectivo `ItemOutcome` en el `summary`.
4. En `CloudSubjectList.kt`: incorporar `CoverDraftsRow`.
5. En `CloudActionBar.kt`: adaptar el texto de feedback de selección para reflejar materias, preferencias y portadas.
6. En `CloudHubSheet.kt`: conectar el evento `onToggleCovers` con el ViewModel.

- [ ] **Step 7: Ejecutar el test y comprobar que pasa**
Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.cloud.CloudViewModelCoversTest"`
Expected: PASS.

---

### Task 7: Verificación Global, Compilación y Actualización de Documentación

**Files:**
- Modify: `docs/prd/STATUS.md`

- [ ] **Step 1: Ejecutar la suite completa de pruebas unitarias**
Run: `./gradlew testDebugUnitTest`
Expected: Todos los tests pasan (684+ tests en verde).

- [ ] **Step 2: Ejecutar compilación completa de la app**
Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL con 0 errores.

- [ ] **Step 3: Actualizar `docs/prd/STATUS.md`**
Registrar la culminación de la sincronización de Portadas, Profesores y el feedback granular en CloudHubSheet.
