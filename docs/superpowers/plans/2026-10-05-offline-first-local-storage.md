# Fase 3.5: NotiFast 100% Offline-First — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transformar NotiFast en una aplicación 100% offline-first con persistencia local SQLite/Room (`notifast.db`), gestión de perfiles/sesiones locales sin depender de Firebase Auth, almacenamiento local de imágenes sin fugas, tipografías empaquetadas offline y preservación de todas las capacidades analíticas, agendas y notas sin conexión a internet.

**Architecture:** Clean Architecture estricta con Room Database como Single Source of Truth local. DAOs reactivos exponiendo `Flow`, repositorios locales (`LocalNoteRepositoryImpl`, `LocalEvaluationRepositoryImpl`, `LocalScheduleRepositoryImpl`, `LocalAuthRepositoryImpl`) que implementan las interfaces existentes del dominio sin alterar ViewModels de negocio ni use cases. Onboarding local sin credenciales remotas, tipografías offline en `res/font/` y reglas de backup Android para retención de base de datos e imágenes.

**Tech Stack:** Kotlin 2.0.21, Android Room 2.6.1 (KSP), Jetpack Compose Material 3, Android DataStore Preferences 1.1.2, Hilt 2.59, Coroutines Flow, ZXing Core 3.5.3.

**Spec:** [`docs/superpowers/specs/2026-10-05-offline-first-local-storage-design.md`](file:///home/analista/Documents/Projects/Notifast/docs/superpowers/specs/2026-10-05-offline-first-local-storage-design.md)

## Global Constraints
- Máximo ~250 líneas por archivo composable (modularizar en `components/` o `atoms/`).
- Cero strings hardcodeados en UI (100% en `res/values/strings.xml` en español).
- Cero colores hexadecimales quemados en UI (usar `MaterialTheme.colorScheme` y `ThemeUtils`).
- MVI estricto: un solo `StateFlow<UiState>` por ViewModel y eventos vía `onEvent()`.
- TDD estricto: pruebas unitarias para converters, DAOs, repositorios locales y ViewModels.
- Desacoplamiento total: ningún archivo de `data/local`, `data/repository`, `domain` o `presentation` debe importar `com.google.firebase.firestore` ni depender de conectividad a internet en runtime.
- Ejecutar y validar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` en cada tarea antes de darla por completada.

---

### Task 1: Dependencias de Room, Reglas de Backup y Tipografías Offline Bundled

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/res/xml/backup_rules.xml`
- Modify: `app/src/main/res/xml/data_extraction_rules.xml`
- Modify: `app/src/main/java/com/notifast/app/presentation/theme/type/Type.kt`
- Create: `app/src/main/res/font/plus_jakarta_sans_regular.ttf`
- Create: `app/src/main/res/font/plus_jakarta_sans_medium.ttf`
- Create: `app/src/main/res/font/plus_jakarta_sans_semibold.ttf`
- Create: `app/src/main/res/font/plus_jakarta_sans_bold.ttf`
- Create: `app/src/main/res/font/jetbrains_mono_regular.ttf`
- Create: `app/src/main/res/font/jetbrains_mono_medium.ttf`
- Create: `app/src/main/res/font/jetbrains_mono_semibold.ttf`
- Create: `app/src/main/res/font/jetbrains_mono_bold.ttf`
- Test: `app/src/test/java/com/notifast/app/presentation/theme/TypographyTest.kt`

**Interfaces:**
- Produces: Room 2.6.1 runtime y compiler configurados vía KSP.
- Produces: Tipografías locales offline disponibles en `R.font.*` sin necesidad de llamadas HTTP o Google Play Services Font Provider.

- [ ] **Step 1: Escribir prueba de carga de tipografía local**

En `app/src/test/java/com/notifast/app/presentation/theme/TypographyTest.kt`:
```kotlin
@Test
fun `typography scales are configured and non-null without network font provider`() {
    val displayLarge = Typography.displayLarge
    val bodyLarge = Typography.bodyLarge
    val monoAcademic = Typography.monoAcademic

    assertNotNull(displayLarge)
    assertNotNull(bodyLarge)
    assertNotNull(monoAcademic)
    assertEquals("tnum", monoAcademic.fontFeatureSettings)
}
```

- [ ] **Step 2: Ejecutar test para verificar estado actual**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.theme.TypographyTest"`
Expected: PASS o FAIL dependiendo del provider actual.

- [ ] **Step 3: Agregar Room 2.6.1 a libs.versions.toml y app/build.gradle.kts**

En `gradle/libs.versions.toml`:
```toml
[versions]
room = "2.6.1"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
```

En `app/build.gradle.kts`:
```kotlin
dependencies {
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)
    // ...
}
```

- [ ] **Step 4: Descargar y colocar las fuentes TTF en `res/font/`**

Descargar desde los repositorios oficiales OFL:
- `plus_jakarta_sans_regular.ttf`
- `plus_jakarta_sans_medium.ttf`
- `plus_jakarta_sans_semibold.ttf`
- `plus_jakarta_sans_bold.ttf`
- `jetbrains_mono_regular.ttf`
- `jetbrains_mono_medium.ttf`
- `jetbrains_mono_semibold.ttf`
- `jetbrains_mono_bold.ttf`

- [ ] **Step 5: Actualizar `Type.kt` para usar `Font(R.font.xxx)` estático**

En `app/src/main/java/com/notifast/app/presentation/theme/type/Type.kt`:
```kotlin
val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)
```

- [ ] **Step 6: Configurar reglas de Backup para base de datos e imágenes**

En `app/src/main/res/xml/backup_rules.xml` y `data_extraction_rules.xml`:
```xml
<full-backup-content>
    <include domain="database" path="notifast.db"/>
    <include domain="database" path="notifast.db-wal"/>
    <include domain="database" path="notifast.db-shm"/>
    <include domain="file" path="images/"/>
    <include domain="file" path="datastore/"/>
</full-backup-content>
```

- [ ] **Step 7: Ejecutar tests y validar compilación**

Run: `./gradlew testDebugUnitTest assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/res/font/ app/src/main/java/com/notifast/app/presentation/theme/type/Type.kt app/src/main/res/xml/
git commit -m "feat(offline): configure Room 2.6.1, bundle local fonts and backup rules"
```

---

### Task 2: Entidades Room, TypeConverters y Desacoplamiento de Modelos de Dominio

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/model/Note.kt` (remover `@ServerTimestamp`)
- Modify: `app/src/main/java/com/notifast/app/data/model/Evaluation.kt` (remover `@ServerTimestamp`)
- Modify: `app/src/main/java/com/notifast/app/data/model/Schedule.kt` (remover `@ServerTimestamp`)
- Create: `app/src/main/java/com/notifast/app/data/local/converter/RoomConverters.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/entity/UserEntity.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/entity/SubjectEntity.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/entity/NoteEntity.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/entity/EvaluationEntity.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/entity/ScheduleEntity.kt`
- Test: `app/src/test/java/com/notifast/app/data/local/converter/RoomConvertersTest.kt`

**Interfaces:**
- Produces: Entidades inmutables mapeadas a tablas SQLite con foreign keys virtuales o índices optimizados.
- Produces: `RoomConverters` que serializan `Date <-> Long` y `List<String> <-> JSON String`.

- [ ] **Step 1: Escribir tests unitarios para `RoomConverters` (TDD)**

En `app/src/test/java/com/notifast/app/data/local/converter/RoomConvertersTest.kt`:
```kotlin
package com.notifast.app.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class RoomConvertersTest {

    private val converters = RoomConverters()

    @Test
    fun `date to timestamp and timestamp to date roundtrip`() {
        val now = Date()
        val timestamp = converters.dateToTimestamp(now)
        val reconstructed = converters.fromTimestamp(timestamp)

        assertEquals(now.time, timestamp)
        assertEquals(now, reconstructed)
    }

    @Test
    fun `null date handles gracefully`() {
        assertNull(converters.dateToTimestamp(null))
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun `string list to json and json to string list roundtrip`() {
        val tags = listOf("Parcial", "Fórmula", "Urgente")
        val json = converters.fromStringList(tags)
        val reconstructed = converters.toStringList(json)

        assertEquals(tags, reconstructed)
    }

    @Test
    fun `empty and null string list roundtrip`() {
        val emptyJson = converters.fromStringList(emptyList())
        assertEquals(emptyList<String>(), converters.toStringList(emptyJson))
        assertEquals(emptyList<String>(), converters.toStringList(null))
    }
}
```

- [ ] **Step 2: Ejecutar test para verificar que falla (Red)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.converter.RoomConvertersTest"`
Expected: FAIL con "unresolved reference RoomConverters"

- [ ] **Step 3: Implementar `RoomConverters.kt`**

En `app/src/main/java/com/notifast/app/data/local/converter/RoomConverters.kt`:
```kotlin
package com.notifast.app.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
```

- [ ] **Step 4: Limpiar dependencias de `@ServerTimestamp` en modelos de dominio**

Eliminar `@ServerTimestamp` de:
- `app/src/main/java/com/notifast/app/data/model/Note.kt`
- `app/src/main/java/com/notifast/app/data/model/Evaluation.kt`
- `app/src/main/java/com/notifast/app/data/model/Schedule.kt`

- [ ] **Step 5: Crear data classes de entidades Room**

Crear:
- `UserEntity.kt` (tabla `users`, PK `uid: String`, `email`, `firstName`, `lastName`, `profilePictureUrl`, `createdAt`, `updatedAt`)
- `SubjectEntity.kt` (tabla `subjects`, composite PK `[userId, name]`, `icon`, `colorHex`, `createdAt`)
- `NoteEntity.kt` (tabla `notes`, PK `id: String`, `userId`, `subject`, `title`, `content`, `imageUrl`, `tags`, `timestamp`, `updatedAt`, índices en `userId` y `subject`)
- `EvaluationEntity.kt` (tabla `evaluations`, PK `id: String`, `userId`, `subjectName`, `title`, `description`, `date`, `noteIds`, `imageUrl`, `quickTips`, `type`, `grade`, `weight`, `isPresented`, `createdAt`, `updatedAt`, índices en `userId` y `subjectName`)
- `ScheduleEntity.kt` (tabla `schedules`, PK `id: String`, `userId`, `subjectName`, `modality`, `dayOfWeek`, `startTime`, `endTime`, `classroom`, `teacher`, `meetingUrl`, `color`, `createdAt`, `updatedAt`, índices en `userId` y `dayOfWeek`)

- [ ] **Step 6: Ejecutar test de converters para verificar que pasa (Green)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.converter.RoomConvertersTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/notifast/app/data/model/ app/src/main/java/com/notifast/app/data/local/
git commit -m "feat(offline): implement Room entities, converters and decouple domain models"
```

---

### Task 3: Base de Datos `NotiFastDatabase` y DAOs Reactivos con Flow

**Files:**
- Create: `app/src/main/java/com/notifast/app/data/local/dao/UserDao.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/dao/SubjectDao.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/dao/NoteDao.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/dao/EvaluationDao.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/dao/ScheduleDao.kt`
- Create: `app/src/main/java/com/notifast/app/data/local/NotiFastDatabase.kt`
- Create: `app/src/main/java/com/notifast/app/di/DatabaseModule.kt`
- Test: `app/src/test/java/com/notifast/app/data/local/dao/RoomDaoInMemoryTest.kt`

**Interfaces:**
- Produces: `NotiFastDatabase` (@Database(version = 1, exportSchema = false)).
- Produces: Queries reactivas con `Flow<List<*>>` que emiten cambios atómicos cada vez que la tabla correspondiente se actualiza.

- [ ] **Step 1: Escribir tests de integración en memoria para DAOs de Room (TDD)**

En `app/src/test/java/com/notifast/app/data/local/dao/RoomDaoInMemoryTest.kt`:
```kotlin
package com.notifast.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomDaoInMemoryTest {

    private lateinit var db: NotiFastDatabase
    private lateinit var userDao: UserDao
    private lateinit var subjectDao: SubjectDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NotiFastDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        subjectDao = db.subjectDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveUser() = runTest {
        val user = UserEntity("u1", "test@test.com", "Juan", "Perez", null, 1000L, 1000L)
        userDao.insertUser(user)

        val retrieved = userDao.getUserById("u1")
        assertEquals("Juan", retrieved?.firstName)
    }

    @Test
    fun insertAndObserveSubjects() = runTest {
        val subject = SubjectEntity("u1", "Matemáticas", "📐", "#BF092F", 1000L)
        subjectDao.insertSubject(subject)

        val list = subjectDao.getSubjects("u1").first()
        assertEquals(1, list.size)
        assertEquals("Matemáticas", list.first().name)
    }
}
```

- [ ] **Step 2: Ejecutar test para verificar que falla (Red)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.dao.RoomDaoInMemoryTest"`
Expected: FAIL por falta de interfaces DAO y base de datos.

- [ ] **Step 3: Crear las interfaces DAO con `@Dao`**

Implementar:
- `UserDao`:
  - `insertUser(user: UserEntity)`
  - `getUserById(uid: String): UserEntity?`
  - `observeUser(uid: String): Flow<UserEntity?>`
  - `updateUser(user: UserEntity)`
  - `deleteUser(uid: String)`
- `SubjectDao`:
  - `insertSubject(subject: SubjectEntity)`
  - `getSubjects(userId: String): Flow<List<SubjectEntity>>`
  - `getSubjectByName(userId: String, name: String): SubjectEntity?`
  - `deleteSubject(userId: String, name: String)`
- `NoteDao`:
  - `insertNote(note: NoteEntity)`
  - `updateNote(note: NoteEntity)`
  - `deleteNoteById(userId: String, noteId: String)`
  - `deleteNotesBySubject(userId: String, subject: String)`
  - `getNotes(userId: String): Flow<List<NoteEntity>>`
  - `getNotesBySubject(userId: String, subject: String): Flow<List<NoteEntity>>`
  - `getNoteById(userId: String, noteId: String): NoteEntity?`
- `EvaluationDao`:
  - `insertEvaluation(eval: EvaluationEntity)`
  - `updateEvaluation(eval: EvaluationEntity)`
  - `deleteEvaluationById(userId: String, evalId: String)`
  - `deleteEvaluationsBySubject(userId: String, subjectName: String)`
  - `getEvaluations(userId: String): Flow<List<EvaluationEntity>>`
  - `getEvaluationById(userId: String, evalId: String): Flow<EvaluationEntity?>`
- `ScheduleDao`:
  - `insertSchedule(schedule: ScheduleEntity)`
  - `updateSchedule(schedule: ScheduleEntity)`
  - `deleteScheduleById(userId: String, scheduleId: String)`
  - `deleteSchedulesBySubject(userId: String, subjectName: String)`
  - `getSchedules(userId: String): Flow<List<ScheduleEntity>>`
  - `getScheduleById(userId: String, scheduleId: String): ScheduleEntity?`

- [ ] **Step 4: Crear la clase abstracta `NotiFastDatabase`**

En `app/src/main/java/com/notifast/app/data/local/NotiFastDatabase.kt`:
```kotlin
package com.notifast.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.notifast.app.data.local.converter.RoomConverters
import com.notifast.app.data.local.dao.*
import com.notifast.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        NoteEntity::class,
        EvaluationEntity::class,
        ScheduleEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class NotiFastDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun noteDao(): NoteDao
    abstract fun evaluationDao(): EvaluationDao
    abstract fun scheduleDao(): ScheduleDao
}
```

- [ ] **Step 5: Crear el módulo Hilt `DatabaseModule`**

En `app/src/main/java/com/notifast/app/di/DatabaseModule.kt`:
```kotlin
package com.notifast.app.di

import android.content.Context
import androidx.room.Room
import com.notifast.app.data.local.NotiFastDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotiFastDatabase {
        return Room.databaseBuilder(
            context,
            NotiFastDatabase::class.java,
            "notifast.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides fun provideUserDao(db: NotiFastDatabase) = db.userDao()
    @Provides fun provideSubjectDao(db: NotiFastDatabase) = db.subjectDao()
    @Provides fun provideNoteDao(db: NotiFastDatabase) = db.noteDao()
    @Provides fun provideEvaluationDao(db: NotiFastDatabase) = db.evaluationDao()
    @Provides fun provideScheduleDao(db: NotiFastDatabase) = db.scheduleDao()
}
```

- [ ] **Step 6: Ejecutar test de integración y validar (Green)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.local.dao.RoomDaoInMemoryTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/notifast/app/data/local/ app/src/main/java/com/notifast/app/di/DatabaseModule.kt app/src/test/java/com/notifast/app/data/local/
git commit -m "feat(offline): define NotiFastDatabase and reactive Room DAOs with Hilt module"
```

---

### Task 4: Repositorios Locales (Note, Evaluation, Schedule e Image) con Limpieza de Fugas

**Files:**
- Create: `app/src/main/java/com/notifast/app/data/repository/LocalNoteRepositoryImpl.kt`
- Create: `app/src/main/java/com/notifast/app/data/repository/LocalEvaluationRepositoryImpl.kt`
- Create: `app/src/main/java/com/notifast/app/data/repository/LocalScheduleRepositoryImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/ImageRepositoryImpl.kt`
- Modify: `app/src/main/java/com/notifast/app/di/RepositoryModule.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/LocalNoteRepositoryImplTest.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/LocalEvaluationRepositoryImplTest.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/LocalScheduleRepositoryImplTest.kt`

**Interfaces:**
- Consumes: `NotiFastDatabase`, `NoteDao`, `SubjectDao`, `EvaluationDao`, `ScheduleDao`, `ImageRepository`.
- Produces: Implementaciones locales puras que sustituyen la necesidad de llamadas a Firestore en tiempo de ejecución.
- Invariants: `deleteSubject` ejecuta una transacción Room atómica para eliminar las notas, evaluaciones y horarios asociados, eliminando las imágenes en disco después del commit exitoso.

- [ ] **Step 1: Escribir tests unitarios para `LocalNoteRepositoryImpl` (TDD)**

En `app/src/test/java/com/notifast/app/data/repository/LocalNoteRepositoryImplTest.kt`:
```kotlin
package com.notifast.app.data.repository

import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.domain.repository.ImageRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalNoteRepositoryImplTest {

    private val db: NotiFastDatabase = mockk(relaxed = true)
    private val noteDao: NoteDao = mockk(relaxed = true)
    private val subjectDao: SubjectDao = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)

    private lateinit var repository: LocalNoteRepositoryImpl

    @Before
    fun setUp() {
        repository = LocalNoteRepositoryImpl(db, noteDao, subjectDao, imageRepository)
    }

    @Test
    fun `addSubject succeeds when subject does not exist`() = runTest {
        coEvery { subjectDao.getSubjectByName("u1", "Física") } returns null
        coEvery { subjectDao.insertSubject(any()) } just Runs

        val result = repository.addSubject("u1", "Física", "⚡", "#3B9797")
        assertTrue(result.isSuccess)
        coVerify { subjectDao.insertSubject(match { it.name == "Física" && it.userId == "u1" }) }
    }

    @Test
    fun `addSubject fails when subject already exists`() = runTest {
        coEvery { subjectDao.getSubjectByName("u1", "Física") } returns SubjectEntity("u1", "Física", "⚡", "#3B9797", 1L)

        val result = repository.addSubject("u1", "Física", "⚡", "#3B9797")
        assertTrue(result.isFailure)
        assertEquals("La materia ya existe", result.exceptionOrNull()?.message)
    }
}
```

- [ ] **Step 2: Ejecutar test para verificar que falla (Red)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.LocalNoteRepositoryImplTest"`
Expected: FAIL con "unresolved reference LocalNoteRepositoryImpl"

- [ ] **Step 3: Implementar `LocalNoteRepositoryImpl.kt`**

En `app/src/main/java/com/notifast/app/data/repository/LocalNoteRepositoryImpl.kt`:
- Mapea llamadas a `NoteDao` y `SubjectDao`.
- Convierte `NoteEntity <-> Note` y `SubjectEntity <-> SubjectData`.
- Genera UUIDs automáticos al agregar notas si vienen sin ID.
- Para `deleteSubject`: usa `db.withTransaction` para ejecutar borrados en lote y limpiar imágenes locales.

- [ ] **Step 4: Implementar `LocalEvaluationRepositoryImpl.kt` y `LocalScheduleRepositoryImpl.kt`**

- `LocalEvaluationRepositoryImpl`: usa `EvaluationDao` y `ImageRepository`. Gestiona borrado de fotos huérfanas al eliminar o sustituir la evaluación.
- `LocalScheduleRepositoryImpl`: usa `ScheduleDao`. Realiza operaciones CRUD 100% locales con persistencia síncrona en SQLite.

- [ ] **Step 5: Refactorizar `ImageRepositoryImpl.kt` para prevenir fugas de disco**

Asegurar que al sobrescribir una imagen en `uploadNoteImage` o `uploadProfileImage`, se borre cualquier archivo anterior para evitar que la memoria interna se llene con fotos desactualizadas.

- [ ] **Step 6: Actualizar `RepositoryModule.kt` para enlazar implementaciones locales**

Reemplazar `NoteRepositoryImpl`, `EvaluationRepositoryImpl`, `ScheduleRepositoryImpl` por sus contrapartes `Local*` en `RepositoryModule.kt`.

- [ ] **Step 7: Ejecutar suite de tests de repositorios locales (Green)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.*"`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/notifast/app/data/repository/ app/src/main/java/com/notifast/app/di/RepositoryModule.kt app/src/test/java/com/notifast/app/data/repository/
git commit -m "feat(offline): implement local room-backed repositories for notes, evaluations and schedules"
```

---

### Task 5: Sesión Local Autónoma (`LocalAuthRepositoryImpl`) y Retención DataStore

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/repository/AuthRepository.kt`
- Create: `app/src/main/java/com/notifast/app/data/repository/LocalAuthRepositoryImpl.kt`
- Create: `app/src/main/java/com/notifast/app/data/remote/FirebaseAuthDataSource.kt`
- Modify: `app/src/main/java/com/notifast/app/di/RepositoryModule.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/LocalAuthRepositoryImplTest.kt`

**Interfaces:**
- Produces: `AuthRepository.createLocalProfile(firstName: String, lastName: String, profileImageUri: Uri?): Result<String>`.
- Produces: `authState: StateFlow<Boolean?>` que emite `true` si existe un usuario activo local en DataStore y Room, o `false` si el dispositivo no tiene perfil creado aún.
- Produces: Aislamiento del código Firebase Auth en `FirebaseAuthDataSource` para re-conexión futura en Fase de Nube.

- [ ] **Step 1: Extender la interfaz `AuthRepository.kt`**

Agregar método para creación de perfil local:
```kotlin
suspend fun createLocalProfile(
    firstName: String,
    lastName: String,
    profileImageUri: Uri?
): Result<String>
```

- [ ] **Step 2: Escribir tests unitarios para `LocalAuthRepositoryImpl` (TDD)**

En `app/src/test/java/com/notifast/app/data/repository/LocalAuthRepositoryImplTest.kt`:
```kotlin
package com.notifast.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.notifast.app.data.local.dao.UserDao
import com.notifast.app.data.local.entity.UserEntity
import com.notifast.app.domain.repository.ImageRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocalAuthRepositoryImplTest {

    private val userDao: UserDao = mockk(relaxed = true)
    private val dataStore: DataStore<Preferences> = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)

    private val activeUserKey = stringPreferencesKey("active_local_user_id")

    @Test
    fun `getCurrentUserId returns active id from dataStore`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[activeUserKey] } returns "user_offline_1"
        every { dataStore.data } returns flowOf(prefs)

        val repository = LocalAuthRepositoryImpl(userDao, dataStore, imageRepository)
        val uid = repository.getCurrentUserId()

        assertEquals("user_offline_1", uid)
    }

    @Test
    fun `createLocalProfile stores user in Room and updates dataStore`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[activeUserKey] } returns null
        every { dataStore.data } returns flowOf(prefs)
        coEvery { dataStore.edit(any()) } returns prefs

        val repository = LocalAuthRepositoryImpl(userDao, dataStore, imageRepository)
        val result = repository.createLocalProfile("Carlos", "Gómez", null)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        coVerify { userDao.insertUser(match { it.firstName == "Carlos" && it.lastName == "Gómez" }) }
    }
}
```

- [ ] **Step 3: Ejecutar test para verificar que falla (Red)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.LocalAuthRepositoryImplTest"`
Expected: FAIL con "unresolved reference LocalAuthRepositoryImpl"

- [ ] **Step 4: Implementar `LocalAuthRepositoryImpl.kt`**

- Utiliza `DataStore<Preferences>` con la clave `active_local_user_id`.
- `init`: Inicializa `_authState.value = (activeId != null && userDao.getUserById(activeId) != null)`.
- `createLocalProfile`: Genera UUID local, guarda la imagen con `imageRepository.uploadProfileImage`, inserta `UserEntity` en `userDao`, guarda el UID en DataStore y actualiza `_authState.value = true`.
- `getUserProfile`: Consulta síncrona/suspendida a `userDao.getUserById(uid)`.
- `updateProfile`: Actualiza nombre, apellido y foto en `userDao` y `imageRepository`.
- `logout`: Borra la clave `active_local_user_id` de DataStore y actualiza `_authState.value = false`.

- [ ] **Step 5: Aislar `FirebaseAuthDataSource` y enlazar `LocalAuthRepositoryImpl` en `RepositoryModule.kt`**

Mover lógica remota de Firebase Auth a `data/remote/FirebaseAuthDataSource.kt` (sin inyectar en RepositoryModule).
Enlazar `LocalAuthRepositoryImpl` a `AuthRepository` en `RepositoryModule.kt`.

- [ ] **Step 6: Ejecutar test y verificar que pasa (Green)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.data.repository.LocalAuthRepositoryImplTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/notifast/app/data/repository/LocalAuthRepositoryImpl.kt app/src/main/java/com/notifast/app/data/remote/ app/src/main/java/com/notifast/app/domain/repository/AuthRepository.kt app/src/main/java/com/notifast/app/di/RepositoryModule.kt app/src/test/java/com/notifast/app/data/repository/LocalAuthRepositoryImplTest.kt
git commit -m "feat(offline): implement autonomous LocalAuthRepository using DataStore and Room"
```

---

### Task 6: Experiencia de Onboarding Local del Cuaderno (Reemplazo del Login Remoto)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/onboarding/OnboardingContract.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/onboarding/OnboardingViewModel.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/onboarding/OnboardingScreen.kt`
- Create: `app/src/main/java/com/notifast/app/presentation/feature/onboarding/components/OnboardingHeader.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/onboarding/OnboardingViewModelTest.kt`

**Interfaces:**
- Produces: Pantalla inicial táctil para crear el perfil del estudiante en el dispositivo sin pedir credenciales remotas.
- Produces: `OnboardingContract` con `OnboardingUiState` y eventos `OnboardingEvent`.

- [ ] **Step 1: Agregar cadenas de texto a `res/values/strings.xml`**

```xml
<string name="onboarding_welcome_title">Bienvenido a NotiFast</string>
<string name="onboarding_welcome_subtitle">Tu cuaderno académico inteligente, 100% privado y en tu dispositivo.</string>
<string name="onboarding_name_label">¿Cómo te llamas?</string>
<string name="onboarding_lastname_label">Apellido (opcional)</string>
<string name="onboarding_start_button">Comenzar mi Cuaderno</string>
<string name="onboarding_avatar_hint">Toca para añadir una foto a tu perfil</string>
<string name="onboarding_error_name_required">Por favor escribe tu nombre para continuar</string>
```

- [ ] **Step 2: Escribir tests unitarios para `OnboardingViewModel` (TDD)**

En `app/src/test/java/com/notifast/app/presentation/feature/onboarding/OnboardingViewModelTest.kt`:
```kotlin
package com.notifast.app.presentation.feature.onboarding

import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.util.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        viewModel = OnboardingViewModel(authRepository)
    }

    @Test
    fun `when submit with empty name, shows validation error`() = runTest {
        viewModel.onEvent(OnboardingEvent.FirstNameChanged("   "))
        viewModel.onEvent(OnboardingEvent.SubmitProfile)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `when submit with valid name, creates local profile and marks completed`() = runTest {
        coEvery { authRepository.createLocalProfile("Ana", "López", null) } returns Result.success("user_ana")

        viewModel.onEvent(OnboardingEvent.FirstNameChanged("Ana"))
        viewModel.onEvent(OnboardingEvent.LastNameChanged("López"))
        viewModel.onEvent(OnboardingEvent.SubmitProfile)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.isCompleted)
        coVerify { authRepository.createLocalProfile("Ana", "López", null) }
    }
}
```

- [ ] **Step 3: Ejecutar test para verificar que falla (Red)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.onboarding.OnboardingViewModelTest"`
Expected: FAIL con "unresolved reference OnboardingViewModel"

- [ ] **Step 4: Implementar `OnboardingContract.kt` y `OnboardingViewModel.kt`**

En `OnboardingContract.kt`:
```kotlin
package com.notifast.app.presentation.feature.onboarding

import android.net.Uri

data class OnboardingUiState(
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false
)

sealed interface OnboardingEvent {
    data class FirstNameChanged(val value: String) : OnboardingEvent
    data class LastNameChanged(val value: String) : OnboardingEvent
    data class ImageSelected(val uri: Uri?) : OnboardingEvent
    data object SubmitProfile : OnboardingEvent
    data object ClearError : OnboardingEvent
}
```

Implementar `OnboardingViewModel.kt` manejando los eventos y llamadas a `authRepository.createLocalProfile`.

- [ ] **Step 5: Crear composable `OnboardingScreen.kt` y `OnboardingHeader.kt` (≤ 200 LOC)**

- Utiliza átomos existentes: `NotiFastStudyBackground`, `NotiFastFormField`, `NotiFastImagePickerField`, `NotiFastButton`.
- Dispara `onOnboardingComplete()` cuando `uiState.isCompleted == true` usando `LaunchedEffect`.

- [ ] **Step 6: Ejecutar tests y validar (Green)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.onboarding.OnboardingViewModelTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/onboarding/ app/src/main/res/values/strings.xml app/src/test/java/com/notifast/app/presentation/feature/onboarding/
git commit -m "feat(offline): create OnboardingScreen and OnboardingViewModel for local student profile"
```

---

### Task 7: Grafo de Navegación, Ciclo de Sesión y Ajustes

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/navigation/Screen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/SettingsSectionsContent.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/SettingsDialogDispatcher.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editprofile/EditProfileScreen.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/settings/SettingsViewModelTest.kt`

**Interfaces:**
- Produces: Flujo de navegación limpio: si no hay perfil local (`authState == false`), va a `Screen.Onboarding`; si ya existe perfil local (`authState == true`), va a `Screen.Notes`.
- Produces: Eliminación del botón confuso de "Cerrar Sesión" en Ajustes y Perfil (reemplazado por opción de reset/limpieza local transparente o removido según YAGNI).

- [ ] **Step 1: Agregar `Screen.Onboarding` en `Screen.kt`**

```kotlin
data object Onboarding : Screen("onboarding")
```

- [ ] **Step 2: Actualizar `NavGraph.kt`**

- Si `authState == false`, navegar a `Screen.Onboarding.route`.
- En `Screen.Onboarding`, al completar la creación del perfil, navegar a `Screen.Notes.route` con `popUpTo(0) { inclusive = true }`.
- Retirar las llamadas de login remoto del NavHost principal.

- [ ] **Step 3: Adaptar `SettingsContract`, `SettingsSectionsContent` y `EditProfileScreen`**

- Remover o transformar la opción de `LOGOUT` en Ajustes y Perfil, evitando dejar datos huérfanos sin usuario activo en el dispositivo.
- Actualizar `SettingsViewModelTest.kt` para reflejar la eliminación o adaptación del evento.

- [ ] **Step 4: Ejecutar tests de settings y navegación**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.settings.*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/navigation/ app/src/main/java/com/notifast/app/presentation/feature/settings/ app/src/main/java/com/notifast/app/presentation/feature/editprofile/
git commit -m "feat(offline): integrate Onboarding into NavGraph and align settings to local session"
```

---

### Task 8: Indicador Honesto de Almacenamiento Local en TopBar y Optimización de Guardado de Notas

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/atoms/NotiFastTopBar.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/atoms/header/HeaderActionButtons.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/editnote/EditNoteViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/share/ImportSubjectBottomSheet.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/notifast/app/presentation/atoms/TopBarTypeTest.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/editnote/EditNoteViewModelTest.kt`

**Interfaces:**
- Produces: Header con indicador visual honesto: icono de almacenamiento local (`PhoneAndroid` o `CheckCircle`) que indica "Guardado en este dispositivo" en lugar de un error de "Nube desconectada".
- Produces: `EditNoteViewModel` optimizado que guarda la foto localmente solo cuando cambia y no re-copia el archivo JPEG en cada pulsación del autoguardado.
- Produces: `ImportSubjectBottomSheet` con mensaje de fallback claro si el escáner Play Services no cuenta con internet en la primera ejecución.

- [ ] **Step 1: Escribir test para indicador de TopBar y autoguardado en `EditNoteViewModel` (TDD)**

En `app/src/test/java/com/notifast/app/presentation/atoms/TopBarTypeTest.kt`:
```kotlin
@Test
fun `home top bar reports local storage state accurately`() {
    val home = TopBarType.Home(
        firstName = "María",
        email = "",
        profilePictureUrl = null,
        isCloudSynced = false,
        onProfileClick = {}
    )
    assertFalse(home.isCloudSynced)
}
```

En `app/src/test/java/com/notifast/app/presentation/feature/editnote/EditNoteViewModelTest.kt`:
Verificar que un cambio de texto no invoque re-copias de imagen si el `imageUri` no fue modificado.

- [ ] **Step 2: Ejecutar tests para verificar estado actual**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.atoms.TopBarTypeTest"`
Expected: PASS

- [ ] **Step 3: Actualizar `NotiFastTopBar.kt` y `HeaderActionButtons.kt`**

- Agregar texto accesible en `strings.xml`:
```xml
<string name="topbar_local_storage_cd">Almacenado localmente en tu dispositivo</string>
```
- Modificar el icono del header para mostrar un estado táctil verde/azul neutral ("Modo Local") en lugar de un icono rojo de alerta cuando `isCloudSynced == false`.

- [ ] **Step 4: Optimizar `EditNoteViewModel.kt`**

Al invocar `saveNote`, una vez que `noteRepository.updateNote(...)` concluye con éxito, resetear `imageUri = null` dentro de `uiState` (dejando la URL ya persistida en la nota) para que los autoguardados posteriores por cambios de texto no vuelvan a re-comprimir y re-escribir la imagen en el almacenamiento interno.

- [ ] **Step 5: Ejecutar suite de pruebas de UI y ViewModel (Green)**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.presentation.feature.editnote.*"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/atoms/ app/src/main/java/com/notifast/app/presentation/feature/editnote/ app/src/main/java/com/notifast/app/presentation/feature/share/ app/src/main/res/values/strings.xml
git commit -m "feat(offline): add honest local storage indicator and optimize note image autosave"
```

---

### Task 9: Test de Arquitectura Offline-First y Verificación Integral del Build

**Files:**
- Create: `app/src/test/java/com/notifast/app/architecture/OfflineArchitectureBoundaryTest.kt`
- Modify: `docs/prd/STATUS.md`

**Interfaces:**
- Produces: Test de arquitectura automatizado que falla si alguna clase en capas de presentación, dominio o datos locales re-introduce dependencias hacia Firestore.
- Produces: Suite completa de tests unitarios y build de release/debug 100% en verde.

- [ ] **Step 1: Escribir `OfflineArchitectureBoundaryTest.kt`**

En `app/src/test/java/com/notifast/app/architecture/OfflineArchitectureBoundaryTest.kt`:
```kotlin
package com.notifast.app.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OfflineArchitectureBoundaryTest {

    @Test
    fun `no active presentation, domain or local data files import firestore`() {
        val rootDir = File("src/main/java/com/notifast/app")
        val forbidden = "com.google.firebase.firestore"
        val violatingFiles = mutableListOf<String>()

        rootDir.walkTopDown().forEach { file ->
            if (file.extension == "kt" && !file.path.contains("data/remote") && !file.path.contains("data/repository/NoteRepositoryImpl") && !file.path.contains("data/repository/EvaluationRepositoryImpl") && !file.path.contains("data/repository/ScheduleRepositoryImpl") && !file.path.contains("data/repository/AuthRepositoryImpl")) {
                val content = file.readText()
                if (content.contains(forbidden)) {
                    violatingFiles.add(file.path)
                }
            }
        }

        assertTrue(
            "Found files importing Firestore in offline-first runtime: $violatingFiles",
            violatingFiles.isEmpty()
        )
    }
}
```

- [ ] **Step 2: Ejecutar el test de frontera de arquitectura**

Run: `./gradlew testDebugUnitTest --tests "com.notifast.app.architecture.OfflineArchitectureBoundaryTest"`
Expected: PASS

- [ ] **Step 3: Ejecutar la suite completa de pruebas unitarias del proyecto**

Run: `./gradlew testDebugUnitTest`
Expected: 100% de pruebas pasando (cero errores o regresiones).

- [ ] **Step 4: Ejecutar compilación completa de la app**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Actualizar `docs/prd/STATUS.md`**

Documentar la arquitectura Offline-First completada con Room y DataStore.

- [ ] **Step 6: Commit**

```bash
git add app/src/test/java/com/notifast/app/architecture/ docs/prd/STATUS.md
git commit -m "chore(offline): enforce architecture boundary test and update roadmap status"
```
