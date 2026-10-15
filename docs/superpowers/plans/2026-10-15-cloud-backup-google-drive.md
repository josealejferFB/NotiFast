# Cloud Backup (Google Drive) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a local-profile user link a Google account on demand and manually back up / restore each subject (plus profile, prefs and teachers) to the hidden Drive `appDataFolder`, with conflict-safe restore, dual delete and a fully functional QR Pack.

**Architecture:** Room stays the source of truth. A new transactional `SubjectBackupStore` reads and writes whole subject snapshots, which `BackupArchiveCodec` packs into ZIP `.nfbak` files. A thin OkHttp `DriveRestClient` stores them in `appDataFolder` with metadata in `appProperties`. A per-account `subject_cloud_state` table plus a canonical `ContentHasher` drive the status classification. Identity uses Credential Manager and Drive access uses `AuthorizationClient` (scope `drive.appdata`).

**Tech Stack:** Kotlin 2.0.21, Compose M3, Hilt 2.59, Room 2.7.2, DataStore, Gson 2.11.0, OkHttp 4.12, androidx.credentials 1.3, googleid 1.1.1, play-services-auth 21.2.0; tests: JUnit4, MockK, Turbine, coroutines-test, Robolectric 4.13, MockWebServer 4.12, room-testing.

**Spec:** [`docs/superpowers/specs/2026-10-15-cloud-backup-google-drive-design.md`](../specs/2026-10-15-cloud-backup-google-drive-design.md) (§14 overrides earlier sections) + [`2026-10-15-cloud-backup-scenario-matrix.md`](../specs/2026-10-15-cloud-backup-scenario-matrix.md). Executors read both.

Path aliases used below: `P` = `app/src/main/java/com/notifast/app`, `T` = `app/src/test/java/com/notifast/app`, `R` = `app/src/main/res`.

## Global Constraints

- Drive scope: exactly `https://www.googleapis.com/auth/drive.appdata`; all Drive calls use `spaces=appDataFolder` / parent `appDataFolder`.
- No Firebase Auth / Firestore / Storage. Analytics and Crashlytics stay.
- Tokens are never persisted. Only `accountId`, `email`, `displayName` and `photoUrl` go to DataStore key `linked_google_account`.
- File names: `subject_<subjectId>.nfbak`, `global.nfbak`. Backup `manifest.json.format = "notifast-backup"`, `BACKUP_SCHEMA_VERSION = 1`.
- Image optimization: long side ≤ **1600 px**, JPEG quality **80**, EXIF stripped, orientation applied; keep the original bytes if the recompressed file is not smaller.
- Subject name: trimmed, 1..**50** chars (`SubjectNameRules.MAX_LENGTH = 50`), unique per user ignoring case. Collision suffix `" (n)"`, n starting at 1.
- Mobile-data warning threshold: **20 MB** estimated batch size.
- Room: version 4 → **5** via explicit `MIGRATION_4_5`. Never `fallbackToDestructiveMigration`.
- Pack P2P wire format (GZIP+Base64, `SubjectPackageCodec.CURRENT_SCHEMA_VERSION`) must not change.
- AGENTS.md rules apply: one `UiState` StateFlow per ViewModel, `onEvent`, ≤ 250 LOC per composable file, `stringResource` only (Spanish), `MaterialTheme.colorScheme` only, `Result<T>` + `Log.e` on every write, no wildcard imports, no logic in composables, `collectAsStateWithLifecycle`.
- Status chip tokens: Synced → `tertiary` (Aqua); NeedsAttention (dirty / cloud newer / unlinked-both) → `primary` (Ruby); LocalOnly → `onSurfaceVariant`; CloudOnly → outlined `outline`.
- Commit after every task. `./gradlew testDebugUnitTest` green at the end of each task, `./gradlew assembleDebug` green at the end of each stage.

## Review Focus

1. **Restoring a subject whose notes reference an image file missing from the ZIP** → the note restores without that image and nothing crashes (pinned in Task 8).
2. **Two subjects whose names differ only by case/whitespace in the cloud batch** ("Física" and "física ") → the second one triggers a conflict instead of a Room unique-index crash (pinned in Task 15).
3. **The user leaves the app or rotates while an upload batch runs** → the batch keeps running in `viewModelScope`, the state survives rotation and temp files are deleted in `finally` (pinned in Task 17).
4. **Hash must be stable right after a restore**, so a restored subject is not shown as "Cambios sin subir" (pinned in Task 15).
5. **Account switched while the panel is open** → the overview reloads for the new `accountId` and never shows another account's states (pinned in Task 17).

---

## Stage E0 — Setup

### Task 1: Dependencies, BuildConfig and external setup

**Files:**
- Modify: `gradle/libs.versions.toml` (remove `firebase-auth`, `firebase-firestore`, `firebase-storage`; add `credentials = "1.3.0"`, `googleid = "1.1.1"`, `okhttp = "4.12.0"`, `robolectric = "4.13"`, and libraries `androidx-credentials`, `androidx-credentials-play-services-auth`, `googleid`, `okhttp`, `okhttp-mockwebserver`, `robolectric`)
- Modify: `app/build.gradle.kts` (implementation: credentials ×2, googleid, okhttp, play-services-auth; testImplementation: mockwebserver, robolectric; `testOptions.unitTests.isIncludeAndroidResources = true`; `buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProps["google.webClientId"] ?: ""}\"")`)
- Modify: `app/proguard-rules.pro` (keep `com.notifast.app.data.backup.**` and `com.notifast.app.data.remote.drive.**` DTO fields for Gson)
- Create: `docs/setup/google-drive-oauth.md` (spec §9 steps: enable Drive API, consent screen with `drive.appdata`, Android clients for debug/release SHA-1, Web client → `local.properties` key `google.webClientId`)

- [ ] **Step 1:** Apply the changes above.
- [ ] **Step 2:** Run `./gradlew assembleDebug testDebugUnitTest`. Expected: BUILD SUCCESSFUL, existing tests green.
- [ ] **Step 3:** Commit `chore(cloud): add credentials/drive deps and drop unused firebase entries`.

---

## Stage E1 — Data core

### Task 2: Subject name rules + `ResolveSubjectNameUseCase`

**Files:**
- Create: `P/domain/util/SubjectNameRules.kt`, `P/domain/usecase/ResolveSubjectNameUseCase.kt`
- Test: `T/domain/usecase/ResolveSubjectNameUseCaseTest.kt`

**Interfaces — Produces:**
```kotlin
object SubjectNameRules { const val MAX_LENGTH = 50 }
sealed interface NameValidation { data object Valid : NameValidation; data object Blank : NameValidation; data object TooLong : NameValidation; data object Duplicate : NameValidation }
class ResolveSubjectNameUseCase @Inject constructor() {
    fun suggest(baseName: String, existing: Collection<String>): String
    fun validate(candidate: String, existing: Collection<String>): NameValidation
}
```

- [ ] **Step 1: Write failing tests:**
  - `suggest("Cálculo", ["Cálculo"]) == "Cálculo (1)"`
  - `suggest("Cálculo", ["cálculo","Cálculo (1)"]) == "Cálculo (2)"`
  - `suggest("Física", []) == "Física"`
  - `suggest` with a 50-char base truncates it so the result is ≤ 50 (`"${base.take(46)} (1)"`)
  - `validate("  ", …) == Blank`, `validate("a".repeat(51), …) == TooLong`, `validate("cálculo ", ["Cálculo"]) == Duplicate`, `validate("Álgebra", ["Cálculo"]) == Valid`
- [ ] **Step 2:** `./gradlew testDebugUnitTest --tests "*ResolveSubjectNameUseCaseTest"` → FAIL (unresolved).
- [ ] **Step 3:** Implement. Compare with `trim().lowercase(Locale.ROOT)`. Truncate the base before adding the suffix.
- [ ] **Step 4:** Re-run → PASS.
- [ ] **Step 5:** Apply `validate` in the add/edit subject ViewModel(s) (find with `codegraph explore "addSubject updateSubject ViewModel"`), mapping `TooLong` to the new string `subject_name_too_long`. Run the full test suite.
- [ ] **Step 6:** Commit `feat(subjects): shared subject name rules and collision resolver`.

### Task 3: Functional QR Pack (suffix unification + no local paths)

**Files:**
- Modify: `P/domain/usecase/ImportSubjectPackageUseCase.kt` (inject `ResolveSubjectNameUseCase`; the RENAME branch calls `suggest(rawName, existing names)`)
- Modify: `P/domain/usecase/ExportSubjectPackageUseCase.kt` (notes: `blocks = blocks.filterNot { it is NoteBlock.Image }`, `imageUrl = null`, `userId = ""`)
- Modify: `T/domain/usecase/ImportSubjectPackageUseCaseTest.kt`, `T/domain/usecase/ExportSubjectPackageUseCaseTest.kt`, and the Hilt/manual constructions of `ImportSubjectPackageUseCase`

**Interfaces — Consumes:** `ResolveSubjectNameUseCase.suggest` (Task 2).

- [ ] **Step 1: Failing tests:**
  - Import with an existing "Cálculo" and RENAME → created subject named `"Cálculo (1)"` (replaces the `"(Copia)"` assertion).
  - `export_strips_image_blocks_and_paths`: a note with `[Paragraph, Image(path="file:///data/x.jpg")]` and `includeNotes=true` → the decoded package's note has no `Image` block and contains no `"file://"` or `"/data/"` substring anywhere in the JSON.
  - `export_then_import_roundtrip_with_notes`: export (MockK repos) → import into fakes → same counts of schedules/evaluations/notes, the note blocks decode, and the target name is correct.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement the two modifications.
- [ ] **Step 4:** Run the whole suite → PASS. Verify no other Pack test changed except the suffix one.
- [ ] **Step 5:** Commit `fix(pack): unify rename suffix and strip local image paths from QR export`.

### Task 4: `ContentHasher`

**Files:**
- Create: `P/data/backup/SubjectSnapshot.kt`, `P/data/backup/ContentHasher.kt`
- Test: `T/data/backup/ContentHasherTest.kt`

**Interfaces — Produces:**
```kotlin
data class SubjectSnapshot(
    val subject: SubjectData,
    val schedules: List<Schedule>,
    val evaluations: List<Evaluation>,
    val notes: List<Note>,
    val imageFiles: Map<String, File>   // key = local path string exactly as stored in the model
)
class ContentHasher @Inject constructor() {
    fun hash(snapshot: SubjectSnapshot): String          // lowercase hex SHA-256
    fun sha1(file: File): String
}
```

- [ ] **Step 1: Failing tests:**
  - Same snapshot with lists in a different order → equal hash.
  - Changing any `updatedAt`, `userId` or the package `exportTimestamp` → equal hash.
  - Changing a note title → different hash.
  - The same image bytes at two different paths → equal hash. Different bytes → different hash.
  - A missing image file is ignored (its reference counts as `"missing"`) and does not throw.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement: build a Gson `JsonElement` of the snapshot with `userId` and `updatedAt` removed and every image path (`Note.imageUrl`, `Evaluation.imageUrl`, `NoteBlock.Image.path`) replaced by `sha1(file)`. Sort lists by `id` and object keys alphabetically, then SHA-256 the UTF-8 string. Serialize blocks with `NoteBlockCodec`'s adapter.
- [ ] **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat(backup): canonical content hasher`.

### Task 5: `ImageOptimizer`

**Files:**
- Create: `P/data/backup/ImageOptimizer.kt`
- Test: `T/data/backup/ImageOptimizerTest.kt` (`@RunWith(RobolectricTestRunner::class)`)

**Interfaces — Produces:** `class ImageOptimizer @Inject constructor() { fun optimize(source: File, target: File): Result<File> }`

- [ ] **Step 1: Failing tests:** a 4000×3000 bitmap → output long side == 1600 with aspect ratio kept. A 800×600 already-small JPEG whose recompressed file is larger → output bytes == source bytes. A non-image file → `Result.failure`.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement with `BitmapFactory` (`inSampleSize` + scale), `ExifInterface` orientation, `compress(JPEG, 80)`.
- [ ] **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat(backup): image optimizer 1600px/q80`.

### Task 6: `BackupArchiveCodec` (ZIP)

**Files:**
- Create: `P/data/backup/BackupManifest.kt`, `P/data/backup/BackupArchiveCodec.kt`, `P/data/backup/GlobalSnapshot.kt`
- Test: `T/data/backup/BackupArchiveCodecTest.kt`

**Interfaces — Produces:**
```kotlin
data class BackupManifest(val format: String = "notifast-backup", val schemaVersion: Int, val kind: String /* "subject"|"global" */, val createdAt: Long)
data class GlobalSnapshot(val firstName: String, val lastName: String, val avatar: File?, val prefs: Map<String, String>, val teachers: List<Teacher>)
class UnsupportedBackupException(val foundVersion: Int) : Exception()
class BackupArchiveCodec @Inject constructor(private val optimizer: ImageOptimizer) {
    companion object { const val BACKUP_SCHEMA_VERSION = 1 }
    fun writeSubject(snapshot: SubjectSnapshot, out: File, workDir: File): Result<Unit>   // images optimized into images/<sha1>.jpg; JSON refs rewritten to "images/<sha1>.jpg"
    fun readSubject(zip: File, extractDir: File): Result<SubjectSnapshot>              // imageFiles keys = "images/<sha1>.jpg" -> extracted File
    fun writeGlobal(snapshot: GlobalSnapshot, out: File, workDir: File): Result<Unit>
    fun readGlobal(zip: File, extractDir: File): Result<GlobalSnapshot>
}
```

- [ ] **Step 1: Failing tests (Robolectric):** subject round-trip keeps ids, counts and blocks, and each image ref resolves to an existing extracted file. A manifest with `schemaVersion = 2` → `Result.failure` whose exception is `UnsupportedBackupException(2)`. A ZIP without `manifest.json` → failure. A zip-slip entry (`../evil`) → failure. The global round-trip keeps prefs and teachers.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement with `java.util.zip` + Gson (UTC Date adapter from `SubjectPackageCodec` and `NoteBlockCodec` adapter). Entries: `manifest.json`, `subject.json` (a `SubjectPackage` built from the snapshot), `images/…`; for global, `global.json` + `avatar.jpg`.
- [ ] **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat(backup): nfbak zip archive codec`.

### Task 7: Room v5 — `subject_cloud_state`

**Files:**
- Create: `P/data/local/entity/SubjectCloudStateEntity.kt` (as spec §5.3), `P/data/local/dao/SubjectCloudStateDao.kt`
- Modify: `P/data/local/NotiFastDatabase.kt` (entity, `version = 5`, abstract dao), `P/data/local/migrations/NotiFastMigrations.kt` (`MIGRATION_4_5`), `P/di/DatabaseModule.kt` (register the migration, provide the DAO)
- Test: `T/data/local/migrations/Migration4To5Test.kt`, extend `T/data/local/dao/RoomDaoInMemoryTest.kt`

**Interfaces — Produces:**
```kotlin
@Dao interface SubjectCloudStateDao {
    @Query("SELECT * FROM subject_cloud_state WHERE accountId = :accountId") fun observe(accountId: String): Flow<List<SubjectCloudStateEntity>>
    @Query("SELECT * FROM subject_cloud_state WHERE accountId = :accountId") suspend fun getAll(accountId: String): List<SubjectCloudStateEntity>
    @Upsert suspend fun upsert(state: SubjectCloudStateEntity)
    @Query("DELETE FROM subject_cloud_state WHERE subjectId = :subjectId") suspend fun deleteBySubject(subjectId: String)
    @Query("DELETE FROM subject_cloud_state WHERE accountId = :accountId") suspend fun deleteByAccount(accountId: String)
}
```

- [ ] **Step 1: Failing tests:** `MigrationTestHelper` creates v4 with a subject row, migrates to 5 and validates; the subject row survives and the new table exists. DAO in-memory: upsert/observe/deleteBySubject/deleteByAccount scoped correctly.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement. Migration SQL must match Room's generated schema exactly (copy from `app/schemas/.../5.json` after the first compile).
- [ ] **Step 4:** Run → PASS. Commit the `app/schemas` 5.json.
- [ ] **Step 5:** Commit `feat(db): v5 subject_cloud_state`.

### Task 8: `SubjectBackupStore` (transactional read/insert/replace)

**Files:**
- Create: `P/domain/repository/SubjectBackupStore.kt`, `P/data/local/backup/RoomSubjectBackupStore.kt`
- Modify: `P/di/RepositoryModule.kt` (bind)
- Test: `T/data/local/backup/RoomSubjectBackupStoreTest.kt` (Robolectric + in-memory Room)

**Interfaces — Produces:**
```kotlin
interface SubjectBackupStore {
    suspend fun readSnapshot(userId: String, subjectId: String): Result<SubjectSnapshot>          // imageFiles keyed by stored path; only existing files
    suspend fun listSubjectIds(userId: String): List<Pair<String, String>>                       // (subjectId, name)
    suspend fun insertSnapshot(userId: String, snapshot: SubjectSnapshot, newSubjectId: String? = null, newName: String? = null): Result<String>
    suspend fun replaceSnapshot(userId: String, oldSubjectId: String, snapshot: SubjectSnapshot): Result<String>
}
```
`insert`/`replace`: copy `imageFiles` into `filesDir/images/<uuid>.jpg`, rewrite refs to `Uri.fromFile(f).toString()` (the `ImageRepositoryImpl` format), assign `userId`, set `subjectName`/`subject` on children to the final name and give children new UUIDs when `newSubjectId` is set. Run everything in `db.withTransaction`. On failure delete the copied files. On `replace`, delete the old images after commit and call `SubjectCloudStateDao.deleteBySubject(oldSubjectId)`. `readSnapshot` accepts both `file://` and plain paths.

- [ ] **Step 1: Failing tests:** a read→insert round-trip into a second user preserves the subjectId and counts. `insertSnapshot(newSubjectId="n", newName="Cálculo (1)")` → children point to "n" and the new name. `replaceSnapshot` removes old notes/images and inserts the new ones. A forced failure (duplicate name) → rollback with no rows and no copied files left. **Review Focus 1:** a note whose image ref has no file → the note is inserted with `imageUrl = null` and the block removed.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement using the existing DAOs plus the entity mappers in `P/data/local/mapper`.
- [ ] **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat(backup): transactional subject snapshot store`.

### Task 9: `GlobalBackupStore` (profile, prefs, teachers)

**Files:**
- Create: `P/domain/repository/GlobalBackupStore.kt`, `P/data/local/backup/LocalGlobalBackupStore.kt`
- Modify: `P/domain/repository/UserPrefsRepository.kt` + `P/data/repository/UserPrefsRepositoryImpl.kt` (add `suspend fun exportAll(): Map<String,String>` and `suspend fun importAll(values: Map<String,String>): Result<Unit>`, typed by the `PreferencesKeys` table; unknown keys ignored)
- Test: `T/data/local/backup/LocalGlobalBackupStoreTest.kt`, extend `T/data/repository/UserPrefsRepositoryImplTest.kt` if present

**Interfaces — Produces:**
```kotlin
interface GlobalBackupStore {
    suspend fun read(userId: String): Result<GlobalSnapshot>
    suspend fun apply(userId: String, snapshot: GlobalSnapshot): Result<Unit>
}
```
`apply`: update the profile via `AuthRepository.updateProfile` (avatar copied through `ImageRepository`), `importAll(prefs)`, and merge teachers: match on `firstName+lastName` trimmed/ignore-case; for a new teacher, `saveTeacher(copy(userId=userId, id=UUID))`; for a match, keep the local fields and set `subjectNames = (local ∪ remote)` deduped ignoring case. Never change `userId`.

- [ ] **Step 1: Failing tests:** prefs round-trip; teacher merge (1 new + 1 matching "juan pérez" vs "Juan Pérez" → 2 total with merged subjects); the profile name is updated and the userId unchanged.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement. **Step 4:** Run → PASS.
- [ ] **Step 5:** `./gradlew assembleDebug`. Commit `feat(backup): global profile/prefs/teachers store`.

---

## Stage E2 — Google account

### Task 10: `GoogleAccountRepository`

**Files:**
- Create: `P/domain/model/cloud/CloudAccount.kt`, `P/domain/repository/GoogleAccountRepository.kt`, `P/data/remote/google/GoogleAuthDataSource.kt`, `P/data/repository/GoogleAccountRepositoryImpl.kt`
- Modify: `P/di/RepositoryModule.kt`, `P/di/AppModule.kt` (provide `CredentialManager`, `AuthorizationClient`)
- Test: `T/data/repository/GoogleAccountRepositoryImplTest.kt` (MockK the data source + a test DataStore)

**Interfaces — Produces:**
```kotlin
data class CloudAccount(val accountId: String, val email: String, val displayName: String?, val photoUrl: String?)
sealed interface AuthStep { data class NeedsResolution(val pendingIntent: PendingIntent) : AuthStep; data class Linked(val account: CloudAccount) : AuthStep }
class CloudAuthException(val reason: Reason) : Exception() { enum class Reason { CANCELLED, NO_PLAY_SERVICES, PERMISSION_DENIED, REAUTH_REQUIRED } }
interface GoogleAccountRepository {
    val account: Flow<CloudAccount?>
    suspend fun signIn(activityContext: Context): Result<AuthStep>            // Credential Manager (serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID, filterByAuthorizedAccounts=false) then authorize(drive.appdata)
    suspend fun completeAuthorization(data: Intent?): Result<CloudAccount>   // after the PendingIntent result
    suspend fun accessToken(forceRefresh: Boolean = false): Result<String>   // AuthorizationClient; REAUTH_REQUIRED if it needs a resolution
    suspend fun unlink(): Result<Unit>                                       // ClearCredentialState + revokeAccess (logged on failure) + clear DataStore + SubjectCloudStateDao.deleteByAccount
}
```
The account is persisted only after Drive authorization succeeds (matrix A4).

- [ ] **Step 1: Failing tests:** sign-in with authorization granted → `Linked` and the account persisted. Authorization needs resolution → `NeedsResolution` and nothing persisted. `GetCredentialCancellationException` → `CloudAuthException(CANCELLED)`. `unlink` clears DataStore and calls `deleteByAccount(accountId)`.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement. **Step 4:** Run → PASS.
- [ ] **Step 5:** Manual check on a device with `local.properties` configured: link → logcat shows a token was obtained (never log the token itself). Commit `feat(cloud): google account linking with drive.appdata`.

---

## Stage E3 — Drive

### Task 11: `DriveRestClient`

**Files:**
- Create: `P/data/remote/drive/DriveFileDto.kt`, `P/data/remote/drive/DriveRestClient.kt`, `P/data/remote/drive/DriveException.kt`
- Modify: `P/di/AppModule.kt` (provide `OkHttpClient`, base URL `https://www.googleapis.com/`)
- Test: `T/data/remote/drive/DriveRestClientTest.kt` (MockWebServer)

**Interfaces — Produces:**
```kotlin
data class DriveFileDto(val id: String, val name: String, val size: Long?, val modifiedTime: String?, val appProperties: Map<String, String> = emptyMap())
class DriveException(val code: Int, val reason: String?) : Exception()   // reason "storageQuotaExceeded" etc.
class DriveRestClient(private val http: OkHttpClient, private val baseUrl: HttpUrl) {
    suspend fun list(token: String): List<DriveFileDto>                                   // GET drive/v3/files?spaces=appDataFolder&fields=files(id,name,size,modifiedTime,appProperties)&pageSize=1000 (follows nextPageToken)
    suspend fun create(token: String, name: String, props: Map<String,String>, file: File): DriveFileDto   // POST upload/drive/v3/files?uploadType=multipart, parents=["appDataFolder"]
    suspend fun update(token: String, fileId: String, props: Map<String,String>, file: File): DriveFileDto // PATCH upload/drive/v3/files/{id}?uploadType=multipart
    suspend fun download(token: String, fileId: String, target: File)                     // GET drive/v3/files/{id}?alt=media
    suspend fun delete(token: String, fileId: String)                                     // DELETE; 404 treated as success
}
```
All calls run on `Dispatchers.IO` and throw `DriveException` for non-2xx responses.

- [ ] **Step 1: Failing tests:** list sends `spaces=appDataFolder` + `Authorization: Bearer t` and parses two files, also across two pages. The create body is `multipart/related` with metadata JSON containing `"parents":["appDataFolder"]` and appProperties. 401 → `DriveException(401)`. 403 with `storageQuotaExceeded` → reason parsed. Delete 404 → no throw.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement with Gson. **Step 4:** Run → PASS.
- [ ] **Step 5:** Commit `feat(drive): thin REST v3 client for appDataFolder`.

### Task 12: `CloudBackupRepository`

**Files:**
- Create: `P/domain/model/cloud/CloudFile.kt`, `P/domain/repository/CloudBackupRepository.kt`, `P/data/repository/DriveCloudBackupRepositoryImpl.kt`
- Modify: `P/di/RepositoryModule.kt`
- Test: `T/data/repository/DriveCloudBackupRepositoryImplTest.kt`

**Interfaces — Produces:**
```kotlin
data class CloudFile(val driveFileId: String, val kind: String, val subjectId: String?, val subjectName: String?, val icon: String?, val colorHex: String?,
                     val contentHash: String, val uploadedAt: Long, val deviceModel: String, val noteCount: Int, val evaluationCount: Int, val sizeBytes: Long, val schemaVersion: Int)
interface CloudBackupRepository {
    suspend fun listFiles(): Result<List<CloudFile>>
    suspend fun upload(existingFileId: String?, fileName: String, props: Map<String,String>, file: File): Result<String>  // returns driveFileId; on update 404 -> create
    suspend fun download(fileId: String, target: File): Result<Unit>
    suspend fun delete(fileId: String): Result<Unit>
}
```
Every call gets a token via `GoogleAccountRepository.accessToken()`; on `DriveException(401)` it retries once with `forceRefresh = true`, then fails with `CloudAuthException(REAUTH_REQUIRED)`. Files with an unknown `kind` or unparsable props are skipped and logged.

- [ ] **Step 1: Failing tests (MockK client + account repo):** props mapping both ways; update-404 → create; 401 → retry once → success; double 401 → `REAUTH_REQUIRED`.
- [ ] **Step 2:** FAIL → **Step 3:** implement → **Step 4:** PASS.
- [ ] **Step 5:** `./gradlew assembleDebug`. Commit `feat(cloud): drive backup repository with token refresh`.

---

## Stage E4 — Use cases

### Task 13: `BuildCloudOverviewUseCase` + local dirty summary

**Files:**
- Create: `P/domain/model/cloud/CloudSubjectStatus.kt`, `P/domain/model/cloud/CloudSubjectItem.kt`, `P/domain/usecase/cloud/BuildCloudOverviewUseCase.kt`, `P/domain/usecase/cloud/ObserveLocalSyncSummaryUseCase.kt`
- Test: `T/domain/usecase/cloud/BuildCloudOverviewUseCaseTest.kt`, `T/domain/usecase/cloud/ObserveLocalSyncSummaryUseCaseTest.kt`

**Interfaces — Produces:**
```kotlin
enum class CloudSubjectStatus { SYNCED, LOCAL_CHANGES, CLOUD_NEWER, BOTH_UNLINKED, LOCAL_ONLY, CLOUD_ONLY }
data class CloudSubjectItem(val subjectId: String, val name: String, val icon: String, val colorHex: String, val status: CloudSubjectStatus,
                            val driveFileId: String?, val localHash: String?, val cloudFile: CloudFile?)
data class CloudOverview(val subjects: List<CloudSubjectItem>, val global: CloudFile?)
class BuildCloudOverviewUseCase @Inject constructor(store: SubjectBackupStore, hasher: ContentHasher, cloud: CloudBackupRepository, stateDao: SubjectCloudStateDao, account: GoogleAccountRepository) {
    suspend operator fun invoke(userId: String): Result<CloudOverview>
    internal fun classify(localHash: String?, cloud: CloudFile?, state: SubjectCloudStateEntity?): CloudSubjectStatus   // pure
}
enum class LocalSyncSummary { NO_ACCOUNT, ALL_SYNCED, PENDING }
class ObserveLocalSyncSummaryUseCase @Inject constructor(...) { operator fun invoke(userId: String): Flow<LocalSyncSummary> }   // no network: account flow + stateDao.observe + subject hashes
```

- [ ] **Step 1: Failing tests:** one test per row of the spec §5.4 table for `classify`. The overview orders items by name and matches the cloud by `subjectId`. Summary: no account → `NO_ACCOUNT`; any subject without a state or with a hash mismatch → `PENDING`.
- [ ] **Step 2:** FAIL → **Step 3:** implement → **Step 4:** PASS.
- [ ] **Step 5:** Commit `feat(cloud): status classification and local sync summary`.

### Task 14: `UploadSubjectsUseCase` + `UploadGlobalDataUseCase`

**Files:**
- Create: `P/domain/model/cloud/BatchResult.kt`, `P/domain/usecase/cloud/UploadSubjectsUseCase.kt`, `P/domain/usecase/cloud/UploadGlobalDataUseCase.kt`
- Test: `T/domain/usecase/cloud/UploadSubjectsUseCaseTest.kt`

**Interfaces — Produces:**
```kotlin
data class ItemOutcome(val id: String, val name: String, val error: Throwable?)
data class BatchResult(val outcomes: List<ItemOutcome>) { val failed get() = outcomes.filter { it.error != null } }
class UploadSubjectsUseCase @Inject constructor(...) {
    operator fun invoke(userId: String, items: List<CloudSubjectItem>, cacheDir: File): Flow<UploadProgress>  // emits Progress(done,total,currentName) then Done(BatchResult)
    suspend fun estimateBytes(userId: String, items: List<CloudSubjectItem>): Long
}
sealed interface UploadProgress { data class Progress(val done: Int, val total: Int, val current: String) : UploadProgress; data class Done(val result: BatchResult) : UploadProgress }
class UploadGlobalDataUseCase @Inject constructor(...) { suspend operator fun invoke(userId: String, existing: CloudFile?, cacheDir: File): Result<Unit> }
```
Per item: `readSnapshot` → `hash` → `writeSubject` into `cacheDir/upload/<id>` → `upload(item.driveFileId, "subject_<id>.nfbak", props, zip)` → `stateDao.upsert(accountId, id, fileId, hash, now)`. Delete temp files in `finally`. One failing item does not stop the batch. The use case does not confirm CLOUD_NEWER/BOTH_UNLINKED; the ViewModel confirms before calling it.

- [ ] **Step 1: Failing tests:** 3 items where the 2nd fails → outcomes [ok, err, ok], with state upserted only for 1 and 3. Props contain `kind=subject`, `subjectId`, `contentHash`, `noteCount`. The temp dir is empty after the run, even on failure.
- [ ] **Step 2:** FAIL → **Step 3:** implement → **Step 4:** PASS.
- [ ] **Step 5:** Commit `feat(cloud): batch upload use cases`.

### Task 15: `RestoreSubjectsUseCase` + `RestoreGlobalDataUseCase`

**Files:**
- Create: `P/domain/model/cloud/RestoreDecision.kt`, `P/domain/usecase/cloud/RestoreSubjectsUseCase.kt`, `P/domain/usecase/cloud/RestoreGlobalDataUseCase.kt`
- Test: `T/domain/usecase/cloud/RestoreSubjectsUseCaseTest.kt`

**Interfaces — Produces:**
```kotlin
data class RestoreConflict(val item: CloudSubjectItem, val localSubjectId: String, val localName: String, val hasLocalChanges: Boolean, val suggestedName: String)
sealed interface RestoreDecision { data object Overwrite : RestoreDecision; data class AddAs(val name: String) : RestoreDecision; data object Skip : RestoreDecision }
class RestoreSubjectsUseCase @Inject constructor(...) {
    suspend fun findConflicts(userId: String, items: List<CloudSubjectItem>): List<RestoreConflict>   // match by subjectId OR name ignore-case/trim, including collisions among the batch items themselves
    operator fun invoke(userId: String, items: List<CloudSubjectItem>, decisions: Map<String, RestoreDecision>, cacheDir: File): Flow<UploadProgress>
}
class RestoreGlobalDataUseCase @Inject constructor(...) { suspend operator fun invoke(userId: String, file: CloudFile, cacheDir: File): Result<Unit> }
```
Per item: download → `readSubject` (`UnsupportedBackupException` → outcome error) → reuse `ImportSubjectPackageUseCase`'s validation (extract it into `internal fun validatePackage(pkg: SubjectPackage): Result<Unit>` in the same file) → no conflict: `insertSnapshot`; `Overwrite`: `replaceSnapshot(localSubjectId)`; `AddAs(name)`: re-validate with `ResolveSubjectNameUseCase.validate`, then `insertSnapshot(newSubjectId = UUID, newName = name)` with **no** state row; `Skip`: outcome ok-skipped. After insert/replace, recompute the local hash via `readSnapshot` + `hash` and upsert the state with that hash (spec §14.3). After the batch, call `RescheduleAllAlertsUseCase`.

- [ ] **Step 1: Failing tests:** no conflict → `insertSnapshot` with the cloud subjectId and the state upserted with the **recomputed** hash (**Review Focus 4**). Overwrite → `replaceSnapshot` with the old id. AddAs with a duplicate name → outcome error and no insert. AddAs valid → new id, no state. Schema v2 → outcome error. Weight 150 → outcome error. **Review Focus 2:** batch items "Física" and "física " with no local subjects → `findConflicts` returns a conflict for the second. Reschedule is called once per batch.
- [ ] **Step 2:** FAIL → **Step 3:** implement → **Step 4:** PASS (including existing `ImportSubjectPackageUseCaseTest`).
- [ ] **Step 5:** Commit `feat(cloud): restore with conflict decisions`.

### Task 16: Dual delete + cloud delete use cases

**Files:**
- Create: `P/domain/usecase/cloud/DeleteCloudSubjectUseCase.kt`
- Modify: the subject delete path (`DeleteSubjectCascadeUseCase` / `LocalNoteRepositoryImpl.deleteSubject`; locate with `codegraph explore "deleteSubject DeleteSubjectCascadeUseCase"`) so a local delete also calls `SubjectCloudStateDao.deleteBySubject(subjectId)` (name → id lookup)
- Test: `T/domain/usecase/cloud/DeleteCloudSubjectUseCaseTest.kt`, extend the existing delete-subject test

**Interfaces — Produces:**
```kotlin
class DeleteCloudSubjectUseCase @Inject constructor(cloud: CloudBackupRepository, stateDao: SubjectCloudStateDao, noteRepository: NoteRepository) {
    suspend fun cloudOnly(driveFileId: String): Result<Unit>
    suspend fun localAndCloud(userId: String, subjectName: String, driveFileId: String): Result<Unit>   // cloud first; local only if cloud OK/404; otherwise Result.failure(CloudDeleteFailed) and local untouched
}
object CloudDeleteFailed : Exception()
```

- [ ] **Step 1: Failing tests:** cloud error → local `deleteSubject` never called and failure is `CloudDeleteFailed`. Cloud OK → local deleted and state removed. A local-only delete removes the state row.
- [ ] **Step 2:** FAIL → **Step 3:** implement → **Step 4:** PASS.
- [ ] **Step 5:** `./gradlew assembleDebug`. Commit `feat(cloud): dual delete`.

---

## Stage E5 — Cloud hub UI

### Task 17: `CloudContract` + `CloudViewModel`

**Files:**
- Create: `P/presentation/feature/cloud/CloudContract.kt`, `P/presentation/feature/cloud/CloudViewModel.kt`
- Test: `T/presentation/feature/cloud/CloudViewModelTest.kt` (fakes, Turbine, `StandardTestDispatcher`)

**Interfaces — Produces:**
```kotlin
data class CloudUiState(
    val account: CloudAccount? = null, val isLoading: Boolean = false, val isOffline: Boolean = false,
    val global: CloudFile? = null, val globalSelected: Boolean = false,
    val items: List<CloudSubjectItem> = emptyList(), val selected: Set<String> = emptySet(),
    val canUpload: Boolean = false, val canRestore: Boolean = false, val cloudOnlyBanner: Int = 0,
    val progress: UploadProgress.Progress? = null, val summary: BatchResult? = null,
    val pendingConflicts: List<RestoreConflict> = emptyList(),     // shown one at a time
    val pendingConfirm: CloudConfirm? = null,                      // CloudNewer / MobileDataLarge(bytes) / RestoreGlobal / DeleteCloud(item)
    val pendingAuthIntent: PendingIntent? = null, val error: CloudError? = null
)
sealed interface CloudEvent { data class SignIn(val activity: Context) : CloudEvent; data class AuthResult(val data: Intent?) : CloudEvent
  data class Toggle(val id: String) : CloudEvent; data object ToggleGlobal : CloudEvent; data object SelectAll : CloudEvent
  data object Upload : CloudEvent; data object Restore : CloudEvent; data class Confirm(val accepted: Boolean) : CloudEvent
  data class ResolveConflict(val decision: RestoreDecision, val applyToRest: Boolean) : CloudEvent
  data class DeleteFromCloud(val id: String) : CloudEvent; data object RetryFailed : CloudEvent; data object Refresh : CloudEvent; data object DismissSummary : CloudEvent }
enum class CloudError { CANCELLED, NO_PLAY_SERVICES, PERMISSION_DENIED, REAUTH_REQUIRED, QUOTA_FULL, OFFLINE, GENERIC }
```
Rules: `canUpload` = selection contains an item that is not CLOUD_ONLY/SYNCED, or the global item; `canRestore` = selection contains an item with a `cloudFile`. Upload of CLOUD_NEWER/BOTH_UNLINKED → `pendingConfirm = CloudNewer` first. Estimate > 20 MB on a metered network (`ConnectivityManager.isActiveNetworkMetered`, wrapped in an injectable `NetworkMonitor` with `isOnline: Flow<Boolean>` and `isMetered(): Boolean`) → `MobileDataLarge`. `applyToRest` applies only to Overwrite/Skip. Reload the overview when `account` changes (**Review Focus 5**). Batches run in `viewModelScope` (**Review Focus 3**).

- [ ] **Step 1: Failing tests:** selection → `canUpload`/`canRestore` per status. A CLOUD_NEWER upload asks for confirmation and Confirm(false) leaves the repos uncalled. Conflicts are queued and `applyToRest` with Overwrite resolves the rest. Offline → actions disabled and `isOffline`. Account switch → overview reloaded with the new account and old items cleared. The summary lists failures and `RetryFailed` re-runs only those.
- [ ] **Step 2:** FAIL → **Step 3:** implement → **Step 4:** PASS.
- [ ] **Step 5:** Commit `feat(cloud): cloud hub view model`.

### Task 18: `CloudHubSheet` UI + dashboard entry + honest TopBar

**Files:**
- Create: `P/presentation/feature/cloud/CloudHubSheet.kt`, `components/LinkAccountPrompt.kt`, `components/CloudAccountHeader.kt`, `components/CloudSubjectRow.kt`, `components/CloudStatusChip.kt`, `components/RestoreConflictDialog.kt`, `components/CloudConfirmDialog.kt`, `components/CloudProgressOverlay.kt`, `components/CloudSummaryCard.kt` (each ≤ 250 LOC)
- Modify: `P/presentation/atoms/NotiFastTopBar.kt` + `atoms/header/HeaderActionButtons.kt` + `atoms/header/HeaderAvatar.kt` (replace `isCloudSynced: Boolean = true` with `syncSummary: LocalSyncSummary = LocalSyncSummary.NO_ACCOUNT` and an `onCloudClick` callback; icons: `PhoneAndroid` / `CloudDone` / `CloudSync` with a badge dot; tints per Global Constraints)
- Modify: `P/presentation/feature/notes/NotesScreen.kt` + `NotesViewModel.kt` + `NotesContract.kt` (expose `syncSummary` from `ObserveLocalSyncSummaryUseCase`, `showCloudHub` flag, open the sheet), plus the other screens using `TopBarType.Home` (pass the summary or default)
- Modify: `R/values/strings.xml` (all cloud copy, Spanish; key prefix `cloud_`)

`RestoreConflictDialog`: title "Ya existe «%s»", radio Sobrescribir / Agregar como / Omitir, `NotiFastTextField` prefilled with `suggestedName`, live error from `ResolveSubjectNameUseCase.validate` (exposed by the VM as `conflictNameError`), a warning line when `hasLocalChanges`, and the "Aplicar a las demás" checkbox hidden for AddAs. The auth `PendingIntent` is launched via `rememberLauncherForActivityResult(StartIntentSenderForResult)` in a `LaunchedEffect(pendingAuthIntent)`.

- [ ] **Step 1:** Build the components with `@Preview` for each status chip and each dialog state, using `NotiFast*` atoms only.
- [ ] **Step 2:** `./gradlew assembleDebug testDebugUnitTest` → green.
- [ ] **Step 3:** On a device, run matrix A2, A3, A5, A6, B1–B4, C1–C3 and F. Record the results in the commit message.
- [ ] **Step 4:** Commit `feat(cloud): cloud hub sheet and honest sync indicator`.

---

## Stage E6 — Delete & Settings

### Task 19: Dual delete dialog

**Files:**
- Modify: `P/presentation/feature/notes/components/NotesDialogs.kt`, `NotesContract.kt`, `NotesViewModel.kt` (state `deleteTarget: DeleteTarget?` with `hasCloudCopy: Boolean, driveFileId: String?`; events `DeleteLocalOnly`, `DeleteLocalAndCloud`, `DeleteFallbackLocal`), `R/values/strings.xml`
- Test: extend `T/presentation/feature/notes/NotesViewModelTest.kt` (or create it)

- [ ] **Step 1: Failing VM tests:** no copy → the single confirmation path as today. With a copy, `DeleteLocalAndCloud` with the cloud failing → state `cloudDeleteFailed = true` and the subject still present; `DeleteFallbackLocal` → deleted locally.
- [ ] **Step 2:** FAIL → **Step 3:** implement (VM uses `DeleteCloudSubjectUseCase`) → **Step 4:** PASS.
- [ ] **Step 5:** Manual matrix D1–D7. Commit `feat(cloud): dual delete dialog`.

### Task 20: Settings → Google account section

**Files:**
- Create: `P/presentation/feature/settings/components/SettingsCloudAccountSection.kt`
- Modify: `SettingsScreen.kt`, `SettingsViewModel.kt`, `SettingsContract.kt` (events `LinkAccount(activity)`, `SwitchAccount(activity)` = unlink then sign in, `UnlinkAccount` behind a confirm), `components/SettingsProfileHeroCard.kt` (cloud badge only when `account != null`), `R/values/strings.xml`
- Test: extend `T/presentation/feature/settings/SettingsViewModelTest.kt`

- [ ] **Step 1: Failing tests:** unlink calls `GoogleAccountRepository.unlink` and leaves local repos untouched. Switch performs unlink and then sign-in.
- [ ] **Step 2:** FAIL → **Step 3:** implement → **Step 4:** PASS.
- [ ] **Step 5:** Manual matrix A8, A9. `./gradlew assembleDebug`. Commit `feat(settings): google account link/switch/unlink`.

---

## Stage E7 — Hardening & close

### Task 21: Release hardening, matrix and docs

**Files:**
- Modify: `app/proguard-rules.pro` (as needed), `docs/prd/STATUS.md`, `README.md` and `AGENTS.md` index (link the spec and plan)

- [ ] **Step 1:** `./gradlew assembleRelease` with a release SHA-1 registered. Run link + upload + restore on the release build to confirm R8 keeps the Gson DTOs.
- [ ] **Step 2:** Run the **whole scenario matrix** on two devices/emulators with the same account. Tick each row in a copy saved as `docs/superpowers/specs/2026-10-15-cloud-backup-scenario-matrix-results.md`.
- [ ] **Step 3:** Accessibility pass: `contentDescription` on status chips, ≥ 48 dp touch targets, TalkBack reads the chip status text.
- [ ] **Step 4:** `./gradlew testDebugUnitTest assembleDebug` → green. Update `STATUS.md` (phase 100 %, date, metrics) and generate the next KICKOFF per the AGENTS.md §8 protocol.
- [ ] **Step 5:** Commit `docs(cloud): close cloud backup phase`.
