# UNERG PDF Schedule Auto-Importer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Permitir al estudiante subir el PDF oficial de horario de clases de DACE-UNERG desde la vista Agenda para extraer automáticamente sus materias (con su sección y período) y sus bloques de clases presenciales (ignorando los no presenciales), mostrar una vista previa interactiva AAA y persistir tanto las materias (con `section` en Room v9) como los horarios y sus alarmas.

**Architecture:** Clean Architecture + MVI 100% Offline-First. En la capa de datos se migra Room de `v8` a `v9` (`MIGRATION_8_9`) añadiendo la columna `section` a `subjects` (`SubjectEntity` / `SubjectData`). En la capa de dominio se construye un parser puro en Kotlin sin dependencias externas (`UnergSchedulePdfParser`) que decodifica streams ReportLab (`ASCII85Decode` + `FlateDecode` y `WinAnsiEncoding` octal), filtra estrictamente bloques `NO PRESENCIAL`, asigna colores/íconos semánticos (`SubjectStyleAssigner`) y coordina la creación/fusión transaccional mediante `ImportUnergScheduleUseCase`. En presentación (`SchedulesScreen` + `SchedulesViewModel`) se integra el selector de PDF y el `UnergScheduleImportSheet` con vista previa y confirmación.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3, Room SQLite (`v8 ➔ v9`), Hilt DI, Coroutines/StateFlow, `java.util.zip.Inflater` (decodificación nativa PDF Flate + Ascii85), JUnit 4, MockK, Room In-Memory / Migration Test.

**Spec:** [`docs/superpowers/specs/2026-10-19-unerg-schedule-pdf-import-design.md`](../specs/2026-10-19-unerg-schedule-pdf-import-design.md)

## Global Constraints

- **100% Offline-First y Cero Librerías Pesadas:** No añadir PDFBox ni librerías externas al `build.gradle.kts`; el parser usa `java.util.zip.Inflater` y un decodificador `Ascii85` propio en Kotlin puro.
- **Ignorar Bloques No Presenciales:** Toda fila `NO PRESENCIAL` o cuya aula sea `A distancia` se descarta al 100% y nunca genera un `Schedule`.
- **Relación Materia-Sección en Base de Datos:** El campo `section: String = ""` vive en `SubjectEntity` (tabla `subjects`, Room `v9`) y `SubjectData`, propagándose a repositorios, respaldos Cloud/ZIP, paquetes `.notifast`, tarjetas de materias y tarjetas de horario.
- **Límite SRP de Composables:** Ningún archivo `@Composable` nuevo o modificado puede superar las ~250 líneas (`AGENTS.md` Regla 5.1).
- **Cero Strings Hardcodeados y Apego al Tema:** Todos los textos de UI van en `app/src/main/res/values/strings.xml` y se usa `MaterialTheme.colorScheme.isDark` (`AGENTS.md` Reglas 5.3 y 5.4).
- **Estado MVI Único:** Toda la vista previa e importación en Agenda vive dentro de `SchedulesUiState` y se dispara vía `SchedulesEvent` (`AGENTS.md` Regla 5.2).

## Review Focus

1. **Nombres de materias o aulas con acentos octales PDF (`\311`, `\323`, `\315`, `\321`) y paréntesis escapados (`\(`, `\)`):** `UnergSchedulePdfParser` debe decodificar secuencias octales de 1 a 3 dígitos en `WinAnsiEncoding`/`ISO-8859-1` para producir `"MIÉRCOLES"`, `"PERÍODO"`, `"SECCIÓN"` sin caracteres corruptos.
2. **Materias con nombres largos envueltos en múltiples líneas dentro de un bloque `BT ... ET`:** El parser debe concatenar con espacio los operadores `(...) Tj` consecutivos dentro de un mismo bloque `BT ... ET` antes de evaluar las celdas de la tabla.
3. **Orden variable entre filas `NO PRESENCIAL` y filas presenciales:** En `30961367_19 (2).pdf`, la primera materia tiene primero `MIÉRCOLES` y luego `NO PRESENCIAL`, mientras las demás tienen primero `NO PRESENCIAL` y luego el día presencial. El parser debe iterar todos los pares `(clase, aula)` de una materia sin asumir orden fijo.
4. **Re-importación del mismo PDF (`MERGE_SMART`):** Si el usuario importa dos veces el mismo PDF, las materias existentes deben actualizar su `section` y `cycle` sin duplicarse, y los bloques horarios idénticos (`misma materia + mismo dayOfWeek + mismo startTime + mismo endTime`) deben omitirse sin crear duplicados.
5. **Preservación de `section` al editar nombre/color/ícono de una materia o sincronizar Cloud Backup:** `LocalNoteRepositoryImpl.updateSubject`, `NotesDialogs`, `SubjectDetailViewModel` y `RoomSubjectBackupStore` deben preservar `section` para no sobreescribirlo con `""`.

---

## Mapa de Archivos a Crear y Modificar

### Archivos Nuevos a Crear
1. `app/src/main/java/com/notifast/app/domain/model/schedule/UnergScheduleImportModels.kt` — Modelos de dominio: `ParsedUnergScheduleDoc`, `ParsedUnergSubject`, `ParsedUnergClassBlock`, `UnergImportMode`, `UnergImportSummary`.
2. `app/src/main/java/com/notifast/app/domain/util/Ascii85Decoder.kt` — Decodificador puro en Kotlin para streams PDF `/ASCII85Decode` (maneja tuplas de 5 caracteres, shorthand `z` para 4 bytes nulos y delimitador `~>`).
3. `app/src/main/java/com/notifast/app/domain/util/SubjectStyleAssigner.kt` — Asignador determinista y semántico de ícono (`SubjectIconMapper`) y color (`ACCESSIBLE_STUDY_PALETTE`) según el nombre de la asignatura, más normalización de nombres (`Title Case` académico en español).
4. `app/src/main/java/com/notifast/app/domain/util/UnergSchedulePdfParser.kt` — Extractor y parser de flujos PDF ReportLab DACE-UNERG (descompresión ASCII85+Flate / Flate directo, decodificación octal WinAnsi, máquina de estados de tabla `COD. MATERIA | NOMBRE MATERIA | SECCIÓN | CLASE | AULA`, descarte de `NO PRESENCIAL`).
5. `app/src/main/java/com/notifast/app/domain/usecase/schedule/ParseUnergSchedulePdfUseCase.kt` — Caso de uso que valida el stream PDF, invoca `UnergSchedulePdfParser` y cruza las materias detectadas contra las materias existentes del usuario para poblar la vista previa.
6. `app/src/main/java/com/notifast/app/domain/usecase/schedule/ImportUnergScheduleUseCase.kt` — Caso de uso transaccional que crea/actualiza las materias seleccionadas (con `section` y `cycle`), inserta los bloques de horario según el `UnergImportMode` (`MERGE_SMART` o `REPLACE_ALL_SCHEDULES`) y programa las alarmas con `ScheduleClassAlertUseCase`.
7. `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/UnergScheduleImportSheet.kt` — `ModalBottomSheet` de vista previa en Agenda (resumen del documento DACE, selector de modo `Combinar` / `Reemplazar`, tarjetas de materias con sección y bloques horarios seleccionables, botón de confirmación).
8. `app/src/test/java/com/notifast/app/data/local/migrations/Migration8To9Test.kt` — Test unitario Robolectric/SQLite que verifica `MIGRATION_8_9` y preservación de datos en `subjects`.
9. `app/src/test/java/com/notifast/app/domain/util/UnergSchedulePdfParserTest.kt` — Suite de pruebas unitarias del parser PDF (incluyendo verificación contra los flujos reales de los 2 PDFs de la UNERG, filtrado de `NO PRESENCIAL`, decodificación octal y manejo de archivos inválidos).
10. `app/src/test/java/com/notifast/app/domain/usecase/schedule/ImportUnergScheduleUseCaseTest.kt` — Suite de pruebas unitarias para `ParseUnergSchedulePdfUseCase` e `ImportUnergScheduleUseCase` (`MERGE_SMART`, `REPLACE_ALL_SCHEDULES`, actualización de `section` y programación de alarmas).

### Archivos Existentes a Modificar
1. `app/src/main/java/com/notifast/app/data/model/Subject.kt` — Añadir `val section: String = ""` a `SubjectData`.
2. `app/src/main/java/com/notifast/app/data/local/entity/SubjectEntity.kt` — Añadir `val section: String = ""` a `SubjectEntity` y su constructor secundario.
3. `app/src/main/java/com/notifast/app/data/local/NotiFastDatabase.kt` — Subir `version = 8` a `version = 9`.
4. `app/src/main/java/com/notifast/app/data/local/migrations/NotiFastMigrations.kt` — Añadir `MIGRATION_8_9`.
5. `app/src/main/java/com/notifast/app/di/DatabaseModule.kt` — Registrar `NotiFastMigrations.MIGRATION_8_9` en `provideDatabase`.
6. `app/src/main/java/com/notifast/app/domain/repository/NoteRepository.kt` — Añadir `section: String = ""` a `addSubject`.
7. `app/src/main/java/com/notifast/app/data/repository/LocalNoteRepositoryImpl.kt` — Mapear `section` en `getSubjects()`, guardar `section` en `addSubject()` y actualizar/preservar `section` en `updateSubject()`.
8. `app/src/main/java/com/notifast/app/data/local/backup/RoomSubjectBackupStore.kt` — Mapear `section` en `SubjectEntity.toDomain()`, `insertSnapshot()` y `replaceSnapshot()`.
9. `app/src/main/java/com/notifast/app/domain/usecase/ImportSubjectPackageUseCase.kt` — Pasar `section = pkg.subject.section` al invocar `noteRepository.addSubject(...)`.
10. `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesContract.kt`, `NotesViewModel.kt`, `components/SubjectGridBento.kt`, `components/EditSubjectDialog.kt`, `components/NotesDialogs.kt` — Incluir `section` en `SubjectInfo`, mostrar chip `Sec. X` en `SubjectBentoCard` y permitir editar/preservar `section` en `EditSubjectDialog`.
11. `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectContract.kt`, `AddSubjectViewModel.kt`, `AddSubjectScreen.kt`, `AddSubjectBottomSheet.kt`, `components/SubjectEditorFormFields.kt` — Permitir ingresar opcionalmente `section` al crear una materia manualmente.
12. `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailContract.kt`, `SubjectDetailViewModel.kt`, `SubjectDetailScreen.kt`, `components/SubjectDetailHeroBento.kt` — Mostrar `Sección X` en el hero de detalle de materia y preservar `section` al editar.
13. `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesContract.kt`, `SchedulesViewModel.kt`, `SchedulesScreen.kt`, `SchedulesFabActions.kt`, `components/ScheduleTimelineView.kt`, `components/ScheduleCard.kt` — Añadir acción de importar PDF en el FAB expandible y en estado vacío, mostrar chip `Sec. X` en `ScheduleCard` y conectar `UnergScheduleImportSheet`.
14. `app/src/main/res/values/strings.xml` — Añadir todos los recursos de texto en español para sección e importación de horario PDF UNERG.

---

### Task 1: Migración de Base de Datos Room `v8 ➔ v9` y Campo `section` en `SubjectData` / `SubjectEntity`

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/model/Subject.kt:7-13`
- Modify: `app/src/main/java/com/notifast/app/data/local/entity/SubjectEntity.kt:15-40`
- Modify: `app/src/main/java/com/notifast/app/data/local/NotiFastDatabase.kt:38`
- Modify: `app/src/main/java/com/notifast/app/data/local/migrations/NotiFastMigrations.kt:261-268`
- Modify: `app/src/main/java/com/notifast/app/di/DatabaseModule.kt:31-39`
- Modify: `app/src/main/java/com/notifast/app/domain/repository/NoteRepository.kt:27-33`
- Modify: `app/src/main/java/com/notifast/app/data/repository/LocalNoteRepositoryImpl.kt:48-60,143-172,200-209`
- Modify: `app/src/main/java/com/notifast/app/data/local/backup/RoomSubjectBackupStore.kt:188-197,309-318,357-363`
- Modify: `app/src/main/java/com/notifast/app/data/backup/ContentHasher.kt:37-44`
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/ImportSubjectPackageUseCase.kt:53-59`
- Create: `app/src/test/java/com/notifast/app/data/local/migrations/Migration8To9Test.kt`
- Modify: `app/src/test/java/com/notifast/app/data/repository/LocalNoteRepositoryImplTest.kt`
- Modify: `app/src/test/java/com/notifast/app/data/backup/ContentHasherTest.kt`

**Interfaces:**
- Produces:
  - `SubjectData(val id: String = "", val name: String, val icon: String = "📚", val colorHex: String = "", val cycle: String = "", val section: String = "")`
  - `SubjectEntity(..., val cycle: String = "", val section: String = "", val createdAt: Date? = null)`
  - `NotiFastMigrations.MIGRATION_8_9: Migration`
  - `NoteRepository.addSubject(userId: String, subjectName: String, icon: String = "📚", colorHex: String = "", cycle: String = "", section: String = ""): Result<String>`

- [ ] **Step 1: Write the failing migration and repository tests**

En `app/src/test/java/com/notifast/app/data/local/migrations/Migration8To9Test.kt`:
```kotlin
package com.notifast.app.data.local.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class Migration8To9Test {

    @Test
    fun `migration 8 to 9 adds section column to subjects with empty default and preserves rows`() {
        val context = RuntimeEnvironment.getApplication()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(8) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `subjects` (
                            `userId` TEXT NOT NULL,
                            `id` TEXT NOT NULL,
                            `name` TEXT NOT NULL COLLATE NOCASE,
                            `icon` TEXT NOT NULL,
                            `colorHex` TEXT NOT NULL,
                            `cycle` TEXT NOT NULL,
                            `createdAt` INTEGER,
                            PRIMARY KEY(`userId`, `id`)
                        )
                        """.trimIndent()
                    )
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()

        val db = FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
        db.execSQL(
            "INSERT INTO `subjects` (`userId`, `id`, `name`, `icon`, `colorHex`, `cycle`, `createdAt`) VALUES ('u1', 's1', 'Sistemas Distribuidos', 'Computer', '#4338CA', '20262', 1000)"
        )

        NotiFastMigrations.MIGRATION_8_9.migrate(db)

        db.query("SELECT `name`, `cycle`, `section` FROM `subjects` WHERE `id` = 's1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Sistemas Distribuidos", cursor.getString(0))
            assertEquals("20262", cursor.getString(1))
            assertEquals("", cursor.getString(2))
        }
        db.close()
    }
}
```

Y en `LocalNoteRepositoryImplTest.kt`, añadir prueba que verifique que `addSubject(..., section = "3")`, `getSubjects` y `updateSubject` persisten y retornan `section = "3"`, y que `updateSubject` preserva `cycle` y `section` cuando no se sobreescriben. En `ContentHasherTest.kt`, añadir prueba de que `section` no vacío modifica el hash SHA-256 sin alterar el hash de materias con `section = ""`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.migrations.Migration8To9Test" --tests "com.notifast.app.data.repository.LocalNoteRepositoryImplTest" --tests "com.notifast.app.data.backup.ContentHasherTest"`  
Expected: FAIL (`Unresolved reference: MIGRATION_8_9` / `No parameter with name 'section'`).

- [ ] **Step 3: Implement `section` across `SubjectData`, `SubjectEntity`, `MIGRATION_8_9`, `LocalNoteRepositoryImpl`, `RoomSubjectBackupStore`, `ContentHasher`, and `ImportSubjectPackageUseCase`**

1. En `Subject.kt`: añadir `val section: String = ""` a `SubjectData`.
2. En `SubjectEntity.kt`: añadir `val section: String = ""` al constructor primario y `section = ""` al constructor secundario.
3. En `NotiFastMigrations.kt`:
   ```kotlin
   val MIGRATION_8_9 = object : Migration(8, 9) {
       override fun migrate(db: SupportSQLiteDatabase) {
           db.execSQL("ALTER TABLE `subjects` ADD COLUMN `section` TEXT NOT NULL DEFAULT ''")
       }
   }
   ```
4. En `NotiFastDatabase.kt`: actualizar `version = 9` y en `DatabaseModule.kt` añadir `NotiFastMigrations.MIGRATION_8_9` a `.addMigrations(...)`.
5. En `NoteRepository.kt` y `LocalNoteRepositoryImpl.kt`:
   - `addSubject` recibe `section: String = ""` y lo guarda en `SubjectEntity(..., section = section.trim())`.
   - `getSubjects` mapea `section = it.section`.
   - `updateSubject` preserva `cycle = newSubject.cycle.ifBlank { existing.cycle }` y `section = newSubject.section.trim()`.
6. En `RoomSubjectBackupStore.kt` e `ImportSubjectPackageUseCase.kt`: mapear `section` en `toDomain()`, `insertSnapshot()`, `replaceSnapshot()` e `invoke()`.
7. En `ContentHasher.kt`: dentro de `subjectJson`, agregar `if (snapshot.subject.section.isNotBlank()) addProperty("section", snapshot.subject.section.trim())` para mantener hashes idénticos cuando `section` es vacío y detectar cambios cuando tiene valor.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.migrations.Migration8To9Test" --tests "com.notifast.app.data.repository.LocalNoteRepositoryImplTest" --tests "com.notifast.app.data.local.backup.RoomSubjectBackupStoreTest"`  
Expected: PASS.

---

### Task 2: Decodificador PDF (`Ascii85Decoder`) y Motor `UnergSchedulePdfParser` + `SubjectStyleAssigner`

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/model/schedule/UnergScheduleImportModels.kt`
- Create: `app/src/main/java/com/notifast/app/domain/util/Ascii85Decoder.kt`
- Create: `app/src/main/java/com/notifast/app/domain/util/SubjectStyleAssigner.kt`
- Create: `app/src/main/java/com/notifast/app/domain/util/UnergSchedulePdfParser.kt`
- Create: `app/src/test/java/com/notifast/app/domain/util/UnergSchedulePdfParserTest.kt`

**Interfaces:**
- Produces:
  ```kotlin
  enum class UnergImportMode {
      MERGE_SMART,
      REPLACE_ALL_SCHEDULES
  }

  data class ParsedUnergClassBlock(
      val dayOfWeek: Int,       // 0=Domingo .. 6=Sábado (Schedule.DAY_NAMES)
      val dayLabel: String,     // "Lunes", "Martes", "Miércoles", etc.
      val startTime: String,    // "HH:mm"
      val endTime: String,      // "HH:mm"
      val classroom: String     // "Aula 2", "Aula 1 Auleta", "LAB 3"
  )

  data class ParsedUnergSubject(
      val code: String,         // "IT9241"
      val rawName: String,      // "SISTEMAS DISTRIBUIDOS"
      val displayName: String,  // "Sistemas Distribuidos"
      val section: String,      // "1"
      val suggestedIcon: String,// "Computer"
      val suggestedColorHex: String, // "#4338CA"
      val classBlocks: List<ParsedUnergClassBlock>,
      val isSelected: Boolean = true,
      val alreadyExists: Boolean = false
  )

  data class ParsedUnergScheduleDoc(
      val studentId: String,    // "30961367"
      val studentName: String,  // "Murillas Ramirez Gabrieli"
      val career: String,       // "(601) INGENIERIA EN INFORMATICA - INGENIERIA DE SISTEMAS"
      val period: String,       // "20262"
      val subjects: List<ParsedUnergSubject>,
      val ignoredNonPresencialCount: Int
  )
  ```
  - `Ascii85Decoder.decode(input: ByteArray): ByteArray`
  - `SubjectStyleAssigner.formatAcademicSubjectName(rawName: String): String`
  - `SubjectStyleAssigner.assignIcon(subjectName: String): String`
  - `SubjectStyleAssigner.assignColorHex(index: Int, existingColors: Set<String> = emptySet()): String`
  - `UnergSchedulePdfParser.parse(pdfBytes: ByteArray): Result<ParsedUnergScheduleDoc>`

- [ ] **Step 1: Write the failing unit tests in `UnergSchedulePdfParserTest.kt`**

Cubrir los siguientes escenarios con aserciones exactas:
1. **`parses first UNERG PDF (6 subjects) ignoring all NO PRESENCIAL rows`:**
   - Verifica `studentId == "30961367"`, `period == "20262"`, `subjects.size == 6`, `ignoredNonPresencialCount == 6`.
   - Verifica cada materia:
     - `EL` ➔ `displayName = "Electiva Libre II"`, `section = "2"`, 1 bloque: `dayOfWeek = 3` (Miércoles), `startTime = "12:31"`, `endTime = "14:00"`, `classroom = "Aula 2"`.
     - `PG0083` ➔ `displayName = "Proyecto de Grado II"`, `section = "3"`, 1 bloque: `dayOfWeek = 4` (Jueves), `startTime = "08:00"`, `endTime = "10:15"`, `classroom = "Aula 2"`.
     - `IS0222` ➔ `displayName = "Informática Educativa"` (o `"Informatica Educativa"` normalizado), `section = "3"`, 1 bloque: `dayOfWeek = 4` (Jueves), `startTime = "10:16"`, `endTime = "11:45"`, `classroom = "Aula 1 Auleta"`.
     - `ID0221` ➔ `displayName = "Gerencia de Proyecto"`, `section = "2"`, 1 bloque: `dayOfWeek = 3` (Miércoles), `startTime = "09:31"`, `endTime = "11:00"`, `classroom = "Aula 2"`.
     - `EA0244` ➔ `displayName = "Electiva de Area III"`, `section = "2"`, 1 bloque: `dayOfWeek = 2` (Martes), `startTime = "12:31"`, `endTime = "14:45"`, `classroom = "LAB 3"`.
     - `IT9241` ➔ `displayName = "Sistemas Distribuidos"`, `section = "1"`, 1 bloque: `dayOfWeek = 1` (Lunes), `startTime = "10:16"`, `endTime = "12:30"`, `classroom = "Aula 1 Auleta"`.
2. **`parses second UNERG PDF (4 subjects) accurately`:**
   - Verifica `studentId == "31053939"`, `period == "20262"`, `subjects.size == 4`, `ignoredNonPresencialCount == 4`.
3. **`concatenates multi-line subject names inside a single BT ET block`:**
   - Verifica que un bloque `BT ... (FUNDAMENTOS DE) Tj T* (INGENIERIA ECONOMICA) Tj T* ET` produzca `rawName == "FUNDAMENTOS DE INGENIERIA ECONOMICA"`.
4. **`decodes WinAnsi octal escapes and escaped parentheses accurately`:**
   - Verifica que `MI\311RCOLES` ➔ `MIÉRCOLES`, `PER\315ODO` ➔ `PERÍODO`, `SECCI\323N` ➔ `SECCIÓN`, `S\301BADO` ➔ `SÁBADO`.
5. **`returns failure when bytes are not a valid PDF or have no UNERG schedule table`:**
   - Verifica `Result.isFailure` con mensaje claro en español.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.util.UnergSchedulePdfParserTest"`  
Expected: FAIL (`Unresolved reference: UnergSchedulePdfParser`).

- [ ] **Step 3: Implement `Ascii85Decoder`, `SubjectStyleAssigner`, and `UnergSchedulePdfParser`**

Detalles clave del algoritmo en `UnergSchedulePdfParser`:
1. Validar cabecera `%PDF-`.
2. Extraer todos los bloques `stream ... endstream` del archivo binario.
3. Para cada stream:
   - Intentar decodificación `Ascii85Decoder.decode` + `Inflater` (con soporte tanto de `Inflater(false)` zlib header como `Inflater(true)` raw deflate).
   - Si falla, intentar `Inflater` directo sobre los bytes del stream.
   - Si el stream resultante contiene operadores `BT` y `ET`, extraer todos los bloques `BT ... ET`.
4. Dentro de cada bloque `BT ... ET`, extraer todos los literales `(...) Tj` o `[(...)] TJ`, decodificando escapes PDF (`\n`, `\r`, `\t`, `\(`, `\)`, `\\`, y octales `\ddd` en `ISO_8859_1`), y unir múltiples líneas del mismo bloque `BT ... ET` con un espacio.
5. Recorrer la lista de celdas de texto extraídas:
   - Extraer `C.I.:`, `CARRERA:` y `PERÍODO:` del encabezado.
   - Ubicar la secuencia de columnas `COD. MATERIA`, `NOMBRE MATERIA`, `SECCIÓN`, `CLASE`, `AULA`.
   - Leer las filas hasta encontrar `SCIH -` o `Este documento sin el sello`:
     - Cuando aparece un código de materia (`code`), seguido de `name`, `section`, `-`, `-`, abrir un nuevo `ParsedUnergSubject`.
     - Mientras los siguientes tokens sean pares `(clase, aula)` (donde `clase` contiene `:` y un rango `HH:mm(:ss)? - HH:mm(:ss)?`):
       - Si `clase` empieza con `NO PRESENCIAL` (ignorando mayúsculas) o `aula.equals("A distancia", ignoreCase = true)`, incrementar `ignoredNonPresencialCount` y continuar.
       - Si `clase` empieza con un día válido (`LUNES` ➔ 1, `MARTES` ➔ 2, `MIÉRCOLES`/`MIERCOLES` ➔ 3, `JUEVES` ➔ 4, `VIERNES` ➔ 5, `SÁBADO`/`SABADO` ➔ 6, `DOMINGO` ➔ 0), extraer `startTime = HH:mm` y `endTime = HH:mm` (quitando `:ss`), `classroom = aula.trim()` y anexar `ParsedUnergClassBlock`.
6. Aplicar `SubjectStyleAssigner` a cada materia para obtener `displayName` (en Title Case conservando números romanos `I`, `II`, `III`, `IV`, `V`, `VI` en mayúsculas), `suggestedIcon` y `suggestedColorHex`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.util.UnergSchedulePdfParserTest"`  
Expected: PASS.

---

### Task 3: Casos de Uso de Dominio (`ParseUnergSchedulePdfUseCase` e `ImportUnergScheduleUseCase`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/usecase/schedule/ParseUnergSchedulePdfUseCase.kt`
- Create: `app/src/main/java/com/notifast/app/domain/usecase/schedule/ImportUnergScheduleUseCase.kt`
- Create: `app/src/test/java/com/notifast/app/domain/usecase/schedule/ImportUnergScheduleUseCaseTest.kt`

**Interfaces:**
- Consumes:
  - `UnergSchedulePdfParser.parse(pdfBytes: ByteArray): Result<ParsedUnergScheduleDoc>`
  - `NoteRepository`, `ScheduleRepository`, `ScheduleClassAlertUseCase`
- Produces:
  ```kotlin
  data class UnergImportSummary(
      val createdSubjectsCount: Int,
      val updatedSubjectsCount: Int,
      val createdSchedulesCount: Int,
      val skippedDuplicateSchedulesCount: Int,
      val firstImportedDayOfWeek: Int?
  )
  ```
  - `ParseUnergSchedulePdfUseCase.invoke(userId: String, pdfBytes: ByteArray): Result<ParsedUnergScheduleDoc>`
  - `ImportUnergScheduleUseCase.invoke(userId: String, doc: ParsedUnergScheduleDoc, mode: UnergImportMode): Result<UnergImportSummary>`

- [ ] **Step 1: Write the failing tests in `ImportUnergScheduleUseCaseTest.kt`**

Probar los siguientes casos:
1. **`ParseUnergSchedulePdfUseCase marks alreadyExists true and reuses existing icon and color when subject exists ignoring case and accents`:**
   - Si el usuario ya tiene `"Informática Educativa"` con color `#15803D`, al parsear el PDF con `"INFORMATICA EDUCATIVA"`, el `ParsedUnergSubject` resultante tiene `alreadyExists = true`, `displayName = "Informática Educativa"`, `suggestedColorHex = "#15803D"`.
2. **`ImportUnergScheduleUseCase in MERGE_SMART mode creates new subjects with section and cycle, updates existing subject section, and skips duplicate schedules`:**
   - Verifica que las materias nuevas se crean con `section` y `cycle = doc.period`, las existentes actualizan su `section` si cambió, los horarios nuevos se insertan y agendan alarma con `ScheduleClassAlertUseCase`, y los horarios que ya coinciden en `(subjectName, dayOfWeek, startTime, endTime)` se omiten e incrementan `skippedDuplicateSchedulesCount`.
3. **`ImportUnergScheduleUseCase in REPLACE_ALL_SCHEDULES mode deletes previous schedules and cancels their alerts before inserting new ones`:**
   - Verifica que se eliminan los horarios previos del usuario (y se cancelan sus alarmas vía `CancelAlertUseCase`), conservando las materias existentes intactas, e insertando únicamente los bloques del PDF seleccionado.
4. **`ImportUnergScheduleUseCase only imports subjects where isSelected is true`:**
   - Si el usuario desmarca una materia en la vista previa (`isSelected = false`), ni la materia ni sus bloques se crean.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.schedule.ImportUnergScheduleUseCaseTest"`  
Expected: FAIL (`Unresolved reference: ParseUnergSchedulePdfUseCase`).

- [ ] **Step 3: Implement `ParseUnergSchedulePdfUseCase` and `ImportUnergScheduleUseCase`**

- En `ParseUnergSchedulePdfUseCase`:
  - Invoca `UnergSchedulePdfParser.parse(pdfBytes)`.
  - Obtiene `existingSubjects = noteRepository.getSubjects(userId).first()`.
  - Compara nombres normalizados (sin tildes, lowercase) para detectar coincidencias con materias ya creadas por el estudiante; si existe, preserva su nombre actual, ícono y color, y marca `alreadyExists = true`.
- En `ImportUnergScheduleUseCase`:
  - Filtra `doc.subjects.filter { it.isSelected }`.
  - Si `mode == UnergImportMode.REPLACE_ALL_SCHEDULES`, elimina todos los horarios actuales del usuario y cancela sus alarmas de clase.
  - Para cada materia seleccionada:
    - Si no existe, llama a `noteRepository.addSubject(userId, subject.displayName, subject.suggestedIcon, subject.suggestedColorHex, cycle = doc.period, section = subject.section)`.
    - Si ya existe, actualiza su `section = subject.section` (y `cycle = existing.cycle.ifBlank { doc.period }`) vía `noteRepository.updateSubject(...)`.
    - Para cada `ParsedUnergClassBlock` de la materia:
      - En modo `MERGE_SMART`, verifica si ya existe un horario para la misma materia, `dayOfWeek`, `startTime` y `endTime`. Si existe, incrementa `skippedDuplicateSchedulesCount`.
      - Si no existe, crea el `Schedule` con `modality = Schedule.MODALITY_PRESENCIAL`, `classroom = block.classroom`, `subjectId = resolvedSubjectId`, `subjectName = resolvedSubjectName` y llama a `scheduleClassAlertUseCase(createdSchedule)`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.domain.usecase.schedule.ImportUnergScheduleUseCaseTest"`  
Expected: PASS.

---

### Task 4: Visualización y Edición de `section` en Materias y Tarjetas de Horario

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesContract.kt:28-39,71`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesViewModel.kt:254-275,435-448`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/SubjectGridBento.kt:160-165`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/EditSubjectDialog.kt:42-55,85-95,121-126`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/components/NotesDialogs.kt:47-60`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/components/SubjectEditorFormFields.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addsubject/AddSubjectBottomSheet.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/components/SubjectDetailHeroBento.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesContract.kt:35-42`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleTimelineView.kt:91-101`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleCard.kt:50-61,143-155`
- Modify: `app/src/test/java/com/notifast/app/presentation/feature/addsubject/AddSubjectViewModelTest.kt`

**Interfaces:**
- Consumes: `SubjectData.section`
- Produces:
  - `SubjectInfo(..., val section: String = "")`
  - `ScheduleTimelineItem.ClassItem(..., val subjectSection: String = "")`
  - `ScheduleCard(..., subjectSection: String = "")` muestra `DetailChip` con `"Sec. $subjectSection"` junto al chip de aula.
  - `SubjectBentoCard` y `SubjectDetailHeroBento` muestran el badge `"Sec. X"` cuando `section.isNotBlank()`.
  - `SubjectEditorFormFields` y `EditSubjectDialog` incluyen campo opcional `"Sección (opcional)"`.

- [ ] **Step 1: Update `AddSubjectViewModelTest` with section state & persistence test**

Añadir prueba en `AddSubjectViewModelTest.kt` que verifique que `AddSubjectEvent.SectionChanged("2")` actualiza `uiState.section` y lo envía a `noteRepository.addSubject(..., section = "2")`.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.addsubject.AddSubjectViewModelTest"`  
Expected: FAIL (`Unresolved reference: SectionChanged`).

- [ ] **Step 3: Implement `section` across UI contracts, ViewModels, dialogs, and cards**

1. Añadir recursos en `strings.xml` (`subject_section_label`, `subject_section_badge`, `schedules_section_chip`, etc.).
2. Propagar `section` en `AddSubjectContract`, `AddSubjectViewModel`, `SubjectEditorFormFields`, `AddSubjectScreen`, `AddSubjectBottomSheet`.
3. Propagar `section` en `NotesContract` (`SubjectInfo`), `NotesViewModel`, `SubjectBentoCard`, `EditSubjectDialog`, `NotesDialogs`.
4. Propagar `section` en `SubjectDetailContract`, `SubjectDetailViewModel`, `SubjectDetailScreen` y `SubjectDetailHeroBento`.
5. Propagar `subjectSection` en `ScheduleTimelineItem.ClassItem`, `ScheduleCalculations.calculateTimelineItems`, `ScheduleTimelineView` y `ScheduleCard` (mostrando un `DetailChip` con `Sec. X` cuando la materia tiene sección asignada).

- [ ] **Step 4: Run unit tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.addsubject.AddSubjectViewModelTest" --tests "com.notifast.app.presentation.feature.notes.NotesViewModelTest"`  
Expected: PASS.

---

### Task 5: Hoja Modal de Vista Previa (`UnergScheduleImportSheet`), Host de Diálogos y Conexión No Bloqueante en `SchedulesScreen` / `SchedulesViewModel`

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/UnergScheduleImportSheet.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/SchedulesDialogsHost.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesFabActions.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/SchedulesScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/schedules/components/ScheduleTimelineView.kt`
- Create: `app/src/test/java/com/notifast/app/presentation/feature/schedules/SchedulesUnergImportViewModelTest.kt`

**Interfaces:**
- Consumes:
  - `ParseUnergSchedulePdfUseCase`
  - `ImportUnergScheduleUseCase`
- Produces:
  - En `SchedulesUiState`:
    ```kotlin
    val isParsingPdf: Boolean = false,
    val isImportingPdf: Boolean = false,
    val unergImportPreview: ParsedUnergScheduleDoc? = null,
    val unergImportMode: UnergImportMode = UnergImportMode.MERGE_SMART,
    val importSuccessMessage: String? = null
    ```
  - En `SchedulesEvent`:
    ```kotlin
    data class ParseUnergPdf(val pdfBytes: ByteArray) : SchedulesEvent
    data class ToggleUnergSubjectSelection(val subjectCode: String) : SchedulesEvent
    data class SetUnergImportMode(val mode: UnergImportMode) : SchedulesEvent
    data object ConfirmUnergImport : SchedulesEvent
    data object DismissUnergImportPreview : SchedulesEvent
    data object ClearImportSuccessMessage : SchedulesEvent
    ```

- [ ] **Step 1: Write the failing ViewModel tests in `SchedulesUnergImportViewModelTest.kt`**

Probar los siguientes casos:
1. **`ParseUnergPdf with valid UNERG PDF populates unergImportPreview in uiState without setting global isLoading`:**
   - Al enviar `SchedulesEvent.ParseUnergPdf(validBytes)`, `uiState.isLoading` permanece `false`, `uiState.unergImportPreview` contiene las materias detectadas y `isParsingPdf == false`.
2. **`ToggleUnergSubjectSelection toggles isSelected on matching subject code`:**
   - Al enviar `SchedulesEvent.ToggleUnergSubjectSelection("IT9241")`, esa materia cambia su estado `isSelected`.
3. **`ConfirmUnergImport invokes ImportUnergScheduleUseCase, clears preview, selects first imported day when non-null, and sets importSuccessMessage`:**
   - Verifica que tras confirmar, `unergImportPreview == null`, `isImportingPdf == false`, `selectedDay` salta a `firstImportedDayOfWeek` (o conserva el día actual si `firstImportedDayOfWeek == null`) y `importSuccessMessage` no es nulo.
4. **`ParseUnergPdf with invalid bytes sets error message, resets isParsingPdf in finally, and leaves preview null`:**
   - Verifica que `uiState.error` se llena con el mensaje sanitizado, `isParsingPdf == false` y `unergImportPreview == null`.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.schedules.SchedulesUnergImportViewModelTest"`  
Expected: FAIL (`Unresolved reference: ParseUnergPdf`).

- [ ] **Step 3: Implement `UnergScheduleImportSheet`, `SchedulesDialogsHost`, and wire `SchedulesViewModel` + `SchedulesScreen`**

1. En `SchedulesViewModel.kt`:
   - Inyectar `private val parseUnergSchedulePdfUseCase: ParseUnergSchedulePdfUseCase? = null` y `private val importUnergScheduleUseCase: ImportUnergScheduleUseCase? = null` con valor por defecto `= null` en el constructor primario para no romper el constructor secundario ni `SchedulesViewModelTest`.
   - En `parseUnergPdf` y `confirmUnergImport`, usar `try / finally` para garantizar que `isParsingPdf` e `isImportingPdf` siempre vuelvan a `false` incluso ante excepciones o cancelación de corrutinas, y nunca activar `uiState.isLoading`.
2. En `SchedulesFabActions.kt`: añadir `importPdfLabel: String = ""`, `importPdfColor: Color = Color.Unspecified`, `onImportPdfClick: (() -> Unit)? = null` y agregar la tercera acción `FabAction` con `Icons.Default.PictureAsPdf` cuando `onImportPdfClick != null`.
3. En `UnergScheduleImportSheet.kt` (< 240 líneas, usando átomos de NotiFast y `MaterialTheme.colorScheme.isDark`):
   - Cabecera con badge `DACE • UNERG`, período académico (`Período 20262`), estudiante y contadores en `JetBrainsMonoFamily` (`N materias • M clases presenciales`).
   - Selector segmentado para `UnergImportMode` (`Combinar sin duplicar` vs. `Reemplazar horario actual`).
   - Lista `LazyColumn` de materias detectadas: cada tarjeta muestra `Checkbox`, ícono circular con el color accesible sugerido, nombre de la materia, badge `Sección X`, chip `Nueva` o `Ya existe`, y cada bloque presencial con día, hora inicio-fin y aula.
   - Botón inferior `NotiFastButton` con `isLoading = isImporting`, `enabled = selectedCount > 0 && !isImporting` que dispara `SchedulesEvent.ConfirmUnergImport`, e impedir el cierre accidental del sheet mientras `isImporting == true`.
4. En `SchedulesDialogsHost.kt` y `SchedulesScreen.kt` (< 235 líneas):
   - Extraer los diálogos de confirmación de borrado y sheets de `SchedulesScreen` hacia `SchedulesDialogsHost.kt` para mantener `SchedulesScreen.kt` limpio y bajo el límite de 250 líneas (`AGENTS.md` Regla 5.1).
   - Registrar `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument())` para `"application/pdf"`. Si `uri == null` (usuario canceló el selector), retornar de inmediato sin alterar el estado.
   - Leer los bytes del `Uri` seleccionado en `Dispatchers.IO` (validando tamaño máximo de 5 MB) y despachar `SchedulesEvent.ParseUnergPdf(bytes)`.
   - Mostrar notificación con `rememberNotiFastAlert()` en `AlertSeverity.SUCCESS` dentro de `LaunchedEffect(uiState.importSuccessMessage)` y limpiar el mensaje con `SchedulesEvent.ClearImportSuccessMessage`.

- [ ] **Step 4: Run unit tests and full build verification**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.schedules.SchedulesUnergImportViewModelTest" --tests "com.notifast.app.presentation.feature.schedules.SchedulesViewModelTest"`  
Expected: PASS.

- [ ] **Step 5: Run full project compilation and complete unit test suite (`AGENTS.md` Pilar 4)**

Run: `./gradlew assembleDebug testDebugUnitTest`  
Expected: `BUILD SUCCESSFUL` con el 100% de los tests antiguos y nuevos en verde.

