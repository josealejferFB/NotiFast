# Sanitización de Materias y Directorio de Profesores — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar la sanitización y personalización completa de materias (nombre, color, icono con renombrado atómico en cascada), el nuevo Directorio de Profesores con sub-pestaña M3, ficha de contacto rápido con integración a WhatsApp y SMS, y la sincronización contextual en el creador de horarios.

**Architecture:** Clean Architecture estricta en 3 capas (Data / Domain / Presentation) con persistencia local Room offline-first (`TeacherEntity`, `TeacherDao`), migración segura `MIGRATION_3_4`, transacción multi-tabla para renombrado consistente de materias (`RoomDatabase.withTransaction`), y componentes Jetpack Compose M3 modulares (≤ 250 LOC por archivo).

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose BOM 2024.09.00 (Material 3), Room 2.6.1, Hilt 2.59, Coroutines Flow, Android Intents (WhatsApp, SMS, Tel).

**Spec:** [`docs/superpowers/specs/2026-10-13-subjects-professors-directory-design.md`](file:///home/p0xd/Documentos/proyectos/NotiFast/docs/superpowers/specs/2026-10-13-subjects-professors-directory-design.md)

## Global Constraints

- Máximo ~250 líneas por archivo composable; subdividir en sub-componentes si se excede (`AGENTS.md`).
- Colores mediante tokens Material 3 y `ThemeUtils.getAccessibleColor` (Ruby `#BF092F`, Aqua `#3B9797`); prohibido hardcodear colores arbitrarios.
- Cero strings hardcodeados en UI: todo texto visible debe residir en `res/values/strings.xml` en español.
- Código fuente en inglés (clases, funciones, variables); UI y mensajes al usuario en español.
- Cero excepciones tragadas (`try/catch` vacíos); operaciones de repositorio retornan `Result<T>`.
- Estado MVI unificado por pantalla en un único `StateFlow<FeatureUiState>`.

## Review Focus

1. **Renombrado en cascada atómico:** Renombrar una materia con 20 notas, 10 evaluaciones y 5 horarios no debe dejar registros huérfanos ni fallar a mitad del proceso.
2. **Colisión de nombres de materia:** Intentar renombrar una materia a un nombre ya existente (insensible a mayúsculas) debe abortar y reportar error sin modificar ningún registro.
3. **Profesor sin teléfono:** Un profesor creado desde el creador de horarios con solo el nombre no debe hacer crashear la acción de WhatsApp/SMS ni abrir intents inválidos.
4. **Fallback si WhatsApp no está instalado:** Al pulsar WhatsApp, si la app de WhatsApp no está instalada en el dispositivo del usuario, debe abrirse el navegador con `https://wa.me/...` sin lanzar `ActivityNotFoundException`.
5. **Migración no destructiva de Room v3 a v4:** Al actualizar la base de datos para agregar la tabla `teachers`, las materias, notas, evaluaciones y horarios existentes deben conservarse intactos.

---

### Task 1: Modelo de Dominio `Teacher` y Formateadores de Comunicación (TDD)

**Files:**
- Create: `app/src/main/java/com/notifast/app/data/model/Teacher.kt`
- Create: `app/src/main/java/com/notifast/app/domain/util/TeacherFormatters.kt`
- Test: `app/src/test/java/com/notifast/app/domain/util/TeacherFormattersTest.kt`

**Interfaces:**
- Produces:
  - `data class Teacher(val id: String, val userId: String, val firstName: String, val lastName: String, val phoneNumber: String, val email: String, val subjectNames: List<String>, val notes: String, val createdAt: Date?, val updatedAt: Date?)`
  - `TeacherFormatters.cleanPhoneNumber(phone: String): String`
  - `TeacherFormatters.buildWhatsAppUrl(phone: String, teacherName: String, subjectName: String): String`
  - `TeacherFormatters.buildWhatsAppMessage(teacherName: String, subjectName: String): String`

- [ ] **Step 1: Escribir pruebas unitarias que fallen para `TeacherFormatters`**
  Probar limpieza de teléfono con `+`, guiones y espacios, formateo del saludo de WhatsApp con y sin materia, y codificación URI segura.

- [ ] **Step 2: Ejecutar pruebas y verificar que fallen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.util.TeacherFormattersTest"`
  Expected: FAIL con símbolos no encontrados.

- [ ] **Step 3: Implementar `Teacher.kt` y `TeacherFormatters.kt`**
  Implementar el modelo inmutable `Teacher` y las funciones utilitarias en `domain/util/TeacherFormatters.kt`.

- [ ] **Step 4: Ejecutar pruebas y verificar que pasen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.util.TeacherFormattersTest"`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git add app/src/main/java/com/notifast/app/data/model/Teacher.kt app/src/main/java/com/notifast/app/domain/util/TeacherFormatters.kt app/src/test/java/com/notifast/app/domain/util/TeacherFormattersTest.kt`
  `git commit -m "feat(domain): add Teacher model and TeacherFormatters with unit tests"`

---

### Task 2: Entidad Room `TeacherEntity`, DAO y Migración de Base de Datos v4 (TDD)

**Files:**
- Create: `app/src/main/java/com/notifast/app/data/local/entity/TeacherEntity.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/dao/TeacherDao.kt`
- Modify: `app/src/main/java/com/notifast/app/data/local/NotiFastDatabase.kt`
- Modify: `app/src/main/java/com/notifast/app/data/local/converter/RoomConverters.kt`
- Modify: `app/src/main/java/com/notifast/app/di/DatabaseModule.kt`
- Test: `app/src/test/java/com/notifast/app/data/local/dao/RoomDaoInMemoryTest.kt`

**Interfaces:**
- Consumes: `Teacher` de Task 1.
- Produces:
  - `TeacherEntity` con primaryKeys `["userId", "id"]`.
  - `TeacherDao`: `getTeachers(userId: String): Flow<List<TeacherEntity>>`, `getTeacherById`, `insertTeacher`, `updateTeacher`, `deleteTeacher`.
  - `MIGRATION_3_4`: migración SQL para tabla `teachers`.

- [ ] **Step 1: Escribir pruebas unitarias que fallen para `TeacherDao`**
  En `RoomDaoInMemoryTest.kt`, agregar pruebas para inserción, consulta reactiva por `userId`, actualización y borrado de `TeacherEntity`.

- [ ] **Step 2: Ejecutar pruebas y verificar que fallen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.dao.RoomDaoInMemoryTest"`
  Expected: FAIL (clase `TeacherDao` no encontrada).

- [ ] **Step 3: Implementar `TeacherEntity`, `TeacherDao`, `RoomConverters` y `MIGRATION_3_4`**
  Incrementar la versión de `NotiFastDatabase` a 4, registrar `TeacherEntity`, añadir `teacherDao()`, y definir `MIGRATION_3_4` con `CREATE TABLE IF NOT EXISTS teachers ...`. Inyectar `TeacherDao` en `DatabaseModule.kt`.

- [ ] **Step 4: Ejecutar pruebas y verificar que pasen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.dao.RoomDaoInMemoryTest"`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git add app/src/main/java/com/notifast/app/data/local/entity/TeacherEntity.kt app/src/main/java/com/notifast/app/data/local/dao/TeacherDao.kt app/src/main/java/com/notifast/app/data/local/NotiFastDatabase.kt app/src/main/java/com/notifast/app/di/DatabaseModule.kt app/src/test/java/com/notifast/app/data/local/dao/RoomDaoInMemoryTest.kt`
  `git commit -m "feat(data): add TeacherEntity, TeacherDao and Room migration 3 to 4"`

---

### Task 3: Transacción Atómica de Renombrado en Cascada de Materias (TDD)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/local/dao/SubjectDao.kt`
- Modify: `app/src/main/java/com/notifast/app/data/local/dao/NoteDao.kt`
- Modify: `app/src/main/java/com/notifast/app/data/local/dao/EvaluationDao.kt`
- Modify: `app/src/main/java/com/notifast/app/data/local/dao/ScheduleDao.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/repository/NoteRepository.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/LocalNoteRepositoryImpl.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/LocalNoteRepositoryImplTest.kt`

**Interfaces:**
- Produces:
  - `NoteRepository.updateSubject(userId: String, oldName: String, newSubject: SubjectData): Result<Unit>`
  - Actualización atómica en Room de `subjects`, `notes`, `evaluations`, `schedules` y `teachers`.

- [ ] **Step 1: Escribir pruebas unitarias que fallen para `updateSubject` en cascada**
  Verificar que:
  a) Si `oldName == newSubject.name`, actualiza solo color e icono en `subjects`.
  b) Si `oldName != newSubject.name`, actualiza `subjects`, `notes.subject`, `evaluations.subjectName`, `schedules.subjectName` y `teachers.subjectNames`.
  c) Si `newSubject.name` ya existe en otra materia, retorna `Result.failure` y no modifica ningún registro.

- [ ] **Step 2: Ejecutar pruebas y verificar que fallen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.LocalNoteRepositoryImplTest"`
  Expected: FAIL con método no implementado o pruebas que fallan.

- [ ] **Step 3: Implementar queries de actualización en DAOs y la transacción en `LocalNoteRepositoryImpl`**
  Añadir `@Query("UPDATE notes SET subject = :newName WHERE userId = :userId AND subject = :oldName")` en `NoteDao` (y análogos en `EvaluationDao` y `ScheduleDao`). Implementar `updateSubject` usando `database.withTransaction`.

- [ ] **Step 4: Ejecutar pruebas y verificar que pasen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.LocalNoteRepositoryImplTest"`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git commit -m "feat(data): implement atomic cascading subject rename transaction"`

---

### Task 4: Repositorio de Profesores `TeacherRepository` (TDD)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/repository/TeacherRepository.kt`
- Create: `app/src/main/java/com/notifast/app/data/repository/LocalTeacherRepositoryImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/di/RepositoryModule.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/LocalTeacherRepositoryImplTest.kt`

**Interfaces:**
- Produces:
  - `interface TeacherRepository`:
    - `getTeachers(userId: String): Flow<List<Teacher>>`
    - `getTeacherById(userId: String, id: String): Flow<Teacher?>`
    - `getTeachersBySubject(userId: String, subjectName: String): Flow<List<Teacher>>`
    - `saveTeacher(teacher: Teacher): Result<Unit>`
    - `deleteTeacher(userId: String, teacherId: String): Result<Unit>`

- [ ] **Step 1: Escribir pruebas unitarias que fallen para `LocalTeacherRepositoryImpl`**
  Pruebas para CRUD, mapeo bidireccional entre `Teacher` y `TeacherEntity`, y filtrado reactivo de profesores por materia.

- [ ] **Step 2: Ejecutar pruebas y verificar que fallen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.LocalTeacherRepositoryImplTest"`
  Expected: FAIL.

- [ ] **Step 3: Implementar `TeacherRepository` y `LocalTeacherRepositoryImpl`**
  Implementar la interfaz en `domain/repository/` y la implementación local en `data/repository/`. Vincular en `RepositoryModule.kt` con `@Binds`.

- [ ] **Step 4: Ejecutar pruebas y verificar que pasen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.LocalTeacherRepositoryImplTest"`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git commit -m "feat(repository): add TeacherRepository and LocalTeacherRepositoryImpl with unit tests"`

---

### Task 5: Diálogo M3 "Editar Materia" e Integración en Listas y Detalle

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/EditSubjectDialog.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/SubjectGridBento.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/components/SubjectDetailHeroBento.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Consumes: `SubjectData`, `NoteRepository.updateSubject` de Task 3.
- Produces:
  - `EditSubjectDialog(subject: SubjectData, onDismiss: () -> Unit, onSave: (name: String, colorHex: String, icon: String) -> Unit)`
  - Botón de editar en `SubjectDetailHeroBento` y opción "Editar" en el menú de la tarjeta `SubjectGridBento`.

- [ ] **Step 1: Añadir strings requeridos en `strings.xml`**
  Textos en español para títulos, campos de edición, advertencias de duplicados y confirmaciones.

- [ ] **Step 2: Implementar composable `EditSubjectDialog.kt` (≤ 250 LOC)**
  Usa `NotiFastFormField` para el nombre, `SubjectColorPickerBento` para el color y `SubjectIconGridSelector` para el icono.

- [ ] **Step 3: Conectar eventos y ViewModels en `NotesViewModel` y `SubjectDetailViewModel`**
  Añadir `NotesEvent.UpdateSubject` y `SubjectDetailEvent.UpdateSubjectData`. Gestionar el renombrado sin pantallas en blanco.

- [ ] **Step 4: Integrar botón de editar en `SubjectDetailHeroBento` y menú en `SubjectGridBento`**
  Icono de lápiz accesible al lado del botón de compartir en detalles, y opción en el menú contextual de materias.

- [ ] **Step 5: Ejecutar pruebas unitarias de ViewModel**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.notes.NotesViewModelTest"`
  Expected: PASS.

- [ ] **Step 6: Commit**
  `git commit -m "feat(ui): add EditSubjectDialog and connect to NotesScreen and SubjectDetailScreen"`

---

### Task 6: Sincronización Contextual de Profesores en Creador y Editor de Horarios

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/model/Schedule.kt`
- Modify: `app/src/main/java/com/notifast/app/data/local/entity/ScheduleEntity.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/AddScheduleScreen.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/addschedule/components/ScheduleTeacherSelector.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editschedule/EditScheduleScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/addschedule/AddScheduleViewModelTest.kt`

**Interfaces:**
- Consumes: `TeacherRepository` de Task 4.
- Produces:
  - `ScheduleTeacherSelector`: Composable que renderiza dropdown si hay docentes asignados a la materia seleccionada, o tarjeta de aviso con campo rápido si no tiene docentes.
  - Al guardar horario con nuevo docente: crea automáticamente la entidad `Teacher` asociada a esa materia.

- [ ] **Step 1: Escribir pruebas unitarias que fallen en `AddScheduleViewModelTest`**
  Comprobar que al seleccionar una materia sin docentes se expone la bandera `hasConfiguredTeacher = false`, y que al guardar el horario se invoca `teacherRepository.saveTeacher`.

- [ ] **Step 2: Ejecutar pruebas y verificar que fallen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.addschedule.AddScheduleViewModelTest"`
  Expected: FAIL.

- [ ] **Step 3: Implementar `ScheduleTeacherSelector.kt` y actualizar ViewModels**
  Inyectar `TeacherRepository` en `AddScheduleViewModel` y `EditScheduleViewModel`. Actualizar `AddScheduleScreen` y `EditScheduleScreen`.

- [ ] **Step 4: Ejecutar pruebas y verificar que pasen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.addschedule.AddScheduleViewModelTest"`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git commit -m "feat(schedules): integrate contextual teacher selector and automatic linking"`

---

### Task 7: Sub-pestaña "Profesores", Directorio y Fichas de Contacto en `NotesScreen`

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/ProfessorDirectoryContent.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/ProfessorCard.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/TeacherDetailBottomSheet.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/AddEditTeacherBottomSheet.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/notes/NotesViewModelTest.kt`

**Interfaces:**
- Consumes: `TeacherRepository` de Task 4, `TeacherFormatters` de Task 1.
- Produces:
  - Sub-segmento en la pestaña Materias: `[ Materias | Profesores ]`.
  - Directorio docente con buscador y FAB de "+ Añadir Profesor".
  - Ficha de profesor con botones directos: WhatsApp (saludo prellenado), SMS y Llamar.
  - Formulario de creación/edición de profesor con selector de materias por chips.

- [ ] **Step 1: Escribir pruebas unitarias que fallen para `NotesViewModel` con profesores**
  Validar carga de profesores, filtrado por búsqueda, selección de docente para BottomSheet y eventos de borrado/guardado.

- [ ] **Step 2: Ejecutar pruebas y verificar que fallen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.notes.NotesViewModelTest"`
  Expected: FAIL.

- [ ] **Step 3: Implementar componentes composables modulares (≤ 250 LOC cada uno)**
  Crear `ProfessorCard`, `ProfessorDirectoryContent`, `TeacherDetailBottomSheet` y `AddEditTeacherBottomSheet` con tokens M3.

- [ ] **Step 4: Integrar en `NotesScreen.kt` y actualizar `NotesViewModel.kt`**
  Integrar el control segmentado en la pestaña Materias y la apertura de las hojas modales.

- [ ] **Step 5: Ejecutar pruebas y verificar que pasen**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.notes.NotesViewModelTest"`
  Expected: PASS.

- [ ] **Step 6: Commit**
  `git commit -m "feat(ui): add professors directory subtab and detail sheet with WhatsApp integration"`

---

### Task 8: Sección de Docentes en `SubjectDetailScreen` (`SubjectPanoramaTab`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/components/SubjectFacultyCard.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/components/SubjectPanoramaTab.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModelTest.kt`

**Interfaces:**
- Consumes: `TeacherRepository` de Task 4, `TeacherDetailBottomSheet` de Task 7.
- Produces:
  - `SubjectFacultyCard`: Tarjeta dentro del cuadrante Panorama que lista los profesores de la materia con avatar y botón rápido de WhatsApp, más botón para asignar otro docente.

- [ ] **Step 1: Escribir pruebas unitarias para `SubjectDetailViewModel` con profesores de la materia**
  Validar que `uiState.teachers` cargue los profesores filtrados por el nombre de la materia actual.

- [ ] **Step 2: Ejecutar pruebas y verificar que pasen/fallen según corresponda**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.subjectdetail.SubjectDetailViewModelTest"`

- [ ] **Step 3: Implementar `SubjectFacultyCard.kt` e integrarla en `SubjectPanoramaTab.kt`**
  Reemplazar/enriquecer la sección de profesor en el panorama para listar docentes reales y conectar el clic a `TeacherDetailBottomSheet`.

- [ ] **Step 4: Ejecutar suite de pruebas de `SubjectDetailViewModel`**
  Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.subjectdetail.SubjectDetailViewModelTest"`
  Expected: PASS.

- [ ] **Step 5: Commit**
  `git commit -m "feat(ui): integrate SubjectFacultyCard in SubjectPanoramaTab"`

---

### Task 9: Verificación Integral, Cero Regresiones y Build del Sistema

**Files:**
- Test: Todo el conjunto de pruebas unitarias del proyecto
- Build: `./gradlew assembleDebug`

- [ ] **Step 1: Ejecutar la suite completa de pruebas unitarias**
  Run: `./gradlew testDebugUnitTest`
  Expected: BUILD SUCCESSFUL con 0 fallos.

- [ ] **Step 2: Compilar el proyecto completo para verificar integración Compose y R8**
  Run: `./gradlew assembleDebug`
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Actualizar `STATUS.md` reflejando las nuevas capacidades añadidas**
  Registrar las tareas completadas en `docs/prd/STATUS.md`.

- [ ] **Step 4: Commit final de verificación**
  `git commit -m "chore(release): verify subjects sanitization and professors directory implementation"`
