# NotiFast — MVVM + SOLID Migration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor NotiFast from monolithic architecture to MVVM + SOLID atómico with Hilt DI, repository interfaces, and 12 feature-specific ViewModels.

**Architecture:** Feature-based MVVM with `Contract (UiState+Events)` → `ViewModel` → `Repository (interface)` → `RepositoryImpl (Firebase)`. Shared atomic components in `presentation/atoms/`. Hilt for DI.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose BOM 2024.09.00, Hilt 2.51.1, Firebase Auth/Firestore/Storage, Coil 2.7.0, Material 3

---

## File Structure

All paths relative to `app/src/main/java/com/example/notifast/`.

### Create:

```
NotiFastApplication.kt
di/
├── AppModule.kt
└── RepositoryModule.kt
domain/repository/
├── AuthRepository.kt
├── NoteRepository.kt
├── EvaluationRepository.kt
└── ImageRepository.kt
data/repository/
├── AuthRepositoryImpl.kt
├── NoteRepositoryImpl.kt
├── EvaluationRepositoryImpl.kt
└── ImageRepositoryImpl.kt
presentation/atoms/
├── NotiFastButton.kt
├── NotiFastTextField.kt
├── NotiFastCard.kt
├── NotiFastChip.kt
├── NotiFastTopBar.kt
├── NotiFastCircularLoading.kt
├── NotiFastEmptyState.kt
├── NotiFastDialog.kt
└── NotiFastBackground.kt
presentation/feature/auth/
├── LoginContract.kt
├── LoginViewModel.kt
├── LoginScreen.kt
└── components/LoginForm.kt
presentation/feature/notes/
├── NotesContract.kt
├── NotesViewModel.kt
├── NotesScreen.kt
└── components/
    ├── NoteCard.kt
    ├── NoteSearchBar.kt
    └── NoteFilterBar.kt
presentation/feature/addnote/
├── AddNoteContract.kt
├── AddNoteViewModel.kt
├── AddNoteScreen.kt
└── components/
presentation/feature/editnote/
├── EditNoteContract.kt
├── EditNoteViewModel.kt
├── EditNoteScreen.kt
└── components/
presentation/feature/subjectdetail/
├── SubjectDetailContract.kt
├── SubjectDetailViewModel.kt
├── SubjectDetailScreen.kt
└── components/
presentation/feature/calendar/
├── CalendarContract.kt
├── CalendarViewModel.kt
├── CalendarScreen.kt
└── components/
    ├── CalendarGrid.kt
    ├── EvaluationItem.kt
    └── NoteItem.kt
presentation/feature/grades/
├── GradesContract.kt
├── GradesViewModel.kt
├── GradesScreen.kt
└── components/
    ├── GradeCard.kt
    └── GradeRevampDialog.kt
presentation/feature/settings/
├── SettingsContract.kt
├── SettingsViewModel.kt
├── SettingsScreen.kt
└── components/
presentation/feature/editprofile/
├── EditProfileContract.kt
├── EditProfileViewModel.kt
├── EditProfileScreen.kt
└── components/
presentation/feature/addevaluation/
├── AddEvaluationContract.kt
├── AddEvaluationViewModel.kt
├── AddEvaluationScreen.kt
└── components/
presentation/feature/editevaluation/
├── EditEvaluationContract.kt
├── EditEvaluationViewModel.kt
├── EditEvaluationScreen.kt
└── components/
presentation/feature/schedules/
├── SchedulesContract.kt
├── SchedulesViewModel.kt
├── SchedulesScreen.kt
└── components/
```

### Modify:
```
app/build.gradle.kts                          (add Hilt, Storage dependencies)
app/src/main/AndroidManifest.xml               (add Application class)
MainActivity.kt                                (@AndroidEntryPoint, reduce)
presentation/navigation/Screen.kt              (add route args)
presentation/navigation/NavGraph.kt            (fix data passing with IDs)
data/model/Note.kt                             (no changes needed)
data/model/Evaluation.kt                       (no changes needed)
data/model/User.kt                             (no changes needed)
presentation/theme/color/Color.kt              (no changes needed)
presentation/theme/theme/Theme.kt              (no changes needed)
presentation/theme/type/Type.kt                (no changes needed)
```

### Delete (after migration):
```
data/repository/AuthRepository.kt              (old concrete class)
data/repository/NotesRepository.kt             (old monolithic class)
presentation/common/viewmodel/AuthViewModel.kt (old ViewModel)
presentation/common/viewmodel/NotesViewModel.kt(old ViewModel)
presentation/common/components/AttractiveTextField.kt (replaced by atoms)
presentation/common/components/EmptyStateView.kt      (replaced by atoms)
presentation/common/components/StudyBackground.kt     (replaced by atoms)
All old screen files under presentation/ui/
```

---

## Plan Tasks

### Task 1: Add Hilt + Firebase Storage dependencies

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `NotiFastApplication.kt`

- [ ] **Step 1: Add Hilt and Storage dependencies to build.gradle.kts**

In `app/build.gradle.kts`, add the Hilt plugin at the top and dependencies:

```kotlin
plugins {
    id("com.google.dagger.hilt.android") version "2.51.1"
}
```

In `gradle/libs.versions.toml`, add:
```toml
[versions]
hilt = "2.51.1"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
```

Back in `app/build.gradle.kts`, update dependencies block:
```kotlin
dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    // ... existing dependencies unchanged
}
```

In root `build.gradle.kts`, apply Hilt plugin:
```kotlin
plugins {
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
```

Also ensure `kotlin("plugin.serialization")` is not needed — we won't use kotlinx.serialization for nav args (we'll use `navArgument` with `NavType.StringType`).

- [ ] **Step 2: Create Application class**

Create `NotiFastApplication.kt`:
```kotlin
package com.notifast.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotiFastApplication : Application()
```

- [ ] **Step 3: Register Application in AndroidManifest.xml**

```xml
<application
    android:name=".NotiFastApplication"
    ...rest unchanged>
```

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (Hilt generates Dagger components)

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: add Hilt DI and Firebase Storage dependencies"
```

---

### Task 2: Create repository interfaces (domain layer)

**Files:**
- Create: `domain/repository/AuthRepository.kt`
- Create: `domain/repository/NoteRepository.kt`
- Create: `domain/repository/EvaluationRepository.kt`
- Create: `domain/repository/ImageRepository.kt`

- [ ] **Step 1: Create AuthRepository interface**

```kotlin
package com.notifast.app.domain.repository

import android.net.Uri
import com.notifast.app.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String> // returns uid
    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String> // returns uid
    suspend fun logout()
    suspend fun getCurrentUserId(): String?
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun updateProfile(
        uid: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<Unit>
}
```

- [ ] **Step 2: Create NoteRepository interface**

```kotlin
package com.notifast.app.domain.repository

import com.notifast.app.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(userId: String): Flow<List<Note>>
    fun getSubjects(userId: String): Flow<List<String>>
    suspend fun addNote(
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUri: android.net.Uri?
    ): Result<Unit>
    suspend fun addSubject(userId: String, subjectName: String): Result<Unit>
    suspend fun deleteNote(userId: String, note: Note): Result<Unit>
    suspend fun updateNote(userId: String, note: Note, imageUri: android.net.Uri?): Result<Unit>
    suspend fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>>
}
```

- [ ] **Step 3: Create EvaluationRepository interface**

```kotlin
package com.notifast.app.domain.repository

import com.notifast.app.data.model.Evaluation
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    fun getEvaluations(userId: String): Flow<List<Evaluation>>
    suspend fun addEvaluation(userId: String, evaluation: Evaluation, imageUri: android.net.Uri?): Result<Unit>
    suspend fun updateEvaluation(userId: String, evaluation: Evaluation, imageUri: android.net.Uri?): Result<Unit>
    suspend fun deleteEvaluation(userId: String, evaluation: Evaluation): Result<Unit>
}
```

- [ ] **Step 4: Create ImageRepository interface**

```kotlin
package com.notifast.app.domain.repository

import android.net.Uri

interface ImageRepository {
    suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String>
    suspend fun uploadNoteImage(uid: String, uri: Uri): Result<String>
    suspend fun deleteImage(url: String): Result<Unit>
}
```

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: add repository interfaces (domain layer)"
```

---

### Task 3: Create Hilt modules

**Files:**
- Create: `di/AppModule.kt`
- Create: `di/RepositoryModule.kt`

- [ ] **Step 1: Create AppModule**

```kotlin
package com.notifast.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
```

- [ ] **Step 2: Create RepositoryModule**

```kotlin
package com.notifast.app.di

import com.notifast.app.data.repository.AuthRepositoryImpl
import com.notifast.app.data.repository.EvaluationRepositoryImpl
import com.notifast.app.data.repository.ImageRepositoryImpl
import com.notifast.app.data.repository.NoteRepositoryImpl
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindEvaluationRepository(impl: EvaluationRepositoryImpl): EvaluationRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (may warn about missing implementations — fine)

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: add Hilt DI modules"
```

---

### Task 4: Implement repository implementations

**Files:**
- Create: `data/repository/AuthRepositoryImpl.kt`
- Create: `data/repository/NoteRepositoryImpl.kt`
- Create: `data/repository/EvaluationRepositoryImpl.kt`
- Create: `data/repository/ImageRepositoryImpl.kt`

- [ ] **Step 1: Implement AuthRepositoryImpl**

Copy logic from old `data/repository/AuthRepository.kt`, but:
- Remove direct `FirebaseAuth.getInstance()` — inject via constructor
- Remove `!!` assertions — use `?: return Result.failure(...)`
- Extract image compression logic to `ImageRepositoryImpl`
- Use `@Inject constructor`

```kotlin
package com.notifast.app.data.repository

import android.content.Context
import android.net.Uri
import com.notifast.app.data.model.User
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed: no user"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("SignUp failed: no user"))

            var profilePictureUrl: String? = null
            if (profileImageUri != null) {
                val uploadResult = imageRepository.uploadProfileImage(uid, profileImageUri)
                if (uploadResult.isSuccess) profilePictureUrl = uploadResult.getOrNull()
            }

            val userMap = mapOf(
                "uid" to uid,
                "email" to email,
                "firstName" to firstName,
                "lastName" to lastName,
                "profilePictureUrl" to (profilePictureUrl ?: "")
            )
            db.collection("users").document(uid).set(userMap).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = db.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
                ?: return Result.failure(Exception("User not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        uid: String,
        firstName: String,
        lastName: String,
        profileImageUri: Uri?
    ): Result<Unit> {
        return try {
            var profilePictureUrl: String? = null
            if (profileImageUri != null) {
                val uploadResult = imageRepository.uploadProfileImage(uid, profileImageUri)
                if (uploadResult.isSuccess) profilePictureUrl = uploadResult.getOrNull()
            }
            val updates = mutableMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName
            )
            if (profilePictureUrl != null) {
                updates["profilePictureUrl"] = profilePictureUrl
            }
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

- [ ] **Step 2: Implement NoteRepositoryImpl**

Split from old monolithic `NotesRepository.kt`. Extract only note + subject logic. Use `@Inject constructor`.

```kotlin
package com.notifast.app.data.repository

import android.net.Uri
import com.notifast.app.data.model.Note
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : NoteRepository {

    private val notesCollection: String = "notes"
    private val subjectsCollection: String = "subjects"
    private val usersCollection: String = "users"

    override fun getNotes(userId: String): Flow<List<Note>> {
        return db.collectionGroup(notesCollection)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Note::class.java) }
            }
    }

    override fun getSubjects(userId: String): Flow<List<String>> {
        return db.collection(usersCollection).document(userId)
            .collection(subjectsCollection)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("name") }
            }
    }

    override suspend fun addNote(
        userId: String,
        subject: String,
        title: String,
        content: String,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val noteMap = mapOf(
                "userId" to userId,
                "subject" to subject,
                "title" to title,
                "content" to content,
                "imageUrl" to (imageUrl ?: "")
            )
            db.collection(usersCollection).document(userId)
                .collection(subjectsCollection).document(subject)
                .collection(notesCollection).add(noteMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addSubject(userId: String, subjectName: String): Result<Unit> {
        return try {
            db.collection(usersCollection).document(userId)
                .collection(subjectsCollection).document(subjectName)
                .set(mapOf("name" to subjectName, "createdAt" to com.google.firebase.Timestamp.now()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(userId: String, note: Note): Result<Unit> {
        return try {
            if (!note.imageUrl.isNullOrEmpty()) {
                imageRepository.deleteImage(note.imageUrl)
            }
            // Find the subject document and delete the note
            val subjectDoc = db.collection(usersCollection).document(userId)
                .collection(subjectsCollection).document(note.subject)
                .collection(notesCollection).document(note.id)
            subjectDoc.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(
        userId: String,
        note: Note,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl = note.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val updates = mutableMapOf<String, Any>(
                "title" to note.title,
                "content" to note.content,
                "subject" to note.subject
            )
            if (imageUrl != null) updates["imageUrl"] = imageUrl
            db.collection(usersCollection).document(userId)
                .collection(subjectsCollection).document(note.subject)
                .collection(notesCollection).document(note.id)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotesBySubject(userId: String, subject: String): Flow<List<Note>> {
        return db.collection(usersCollection).document(userId)
            .collection(subjectsCollection).document(subject)
            .collection(notesCollection)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Note::class.java) }
            }
    }
}
```

- [ ] **Step 3: Implement EvaluationRepositoryImpl**

Extract from old `NotesRepository.kt`.

```kotlin
package com.notifast.app.data.repository

import android.net.Uri
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluationRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val imageRepository: ImageRepository
) : EvaluationRepository {

    override fun getEvaluations(userId: String): Flow<List<Evaluation>> {
        return db.collection("users").document(userId)
            .collection("evaluations")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Evaluation::class.java) }
            }
    }

    override suspend fun addEvaluation(
        userId: String,
        evaluation: Evaluation,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl = evaluation.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val evalMap = mapOf(
                "userId" to evaluation.userId,
                "subjectName" to evaluation.subjectName,
                "title" to evaluation.title,
                "description" to evaluation.description,
                "date" to (evaluation.date ?: ""),
                "noteIds" to evaluation.noteIds,
                "imageUrl" to (imageUrl ?: ""),
                "quickTips" to evaluation.quickTips,
                "grade" to (evaluation.grade ?: 0.0),
                "weight" to (evaluation.weight ?: 0.0),
                "isPresented" to evaluation.isPresented,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            db.collection("users").document(userId)
                .collection("evaluations").add(evalMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEvaluation(
        userId: String,
        evaluation: Evaluation,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            var imageUrl = evaluation.imageUrl
            if (imageUri != null) {
                val uploadResult = imageRepository.uploadNoteImage(userId, imageUri)
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull()
            }
            val updates = mutableMapOf<String, Any>(
                "title" to evaluation.title,
                "description" to evaluation.description,
                "subjectName" to evaluation.subjectName,
                "date" to (evaluation.date ?: ""),
                "quickTips" to evaluation.quickTips,
                "grade" to (evaluation.grade ?: 0.0),
                "weight" to (evaluation.weight ?: 0.0),
                "isPresented" to evaluation.isPresented
            )
            if (imageUrl != null) updates["imageUrl"] = imageUrl
            db.collection("users").document(userId)
                .collection("evaluations").document(evaluation.id)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvaluation(userId: String, evaluation: Evaluation): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("evaluations").document(evaluation.id)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

- [ ] **Step 4: Implement ImageRepositoryImpl**

New implementation — uploads to Firebase Storage instead of local filesystem.

```kotlin
package com.notifast.app.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.notifast.app.domain.repository.ImageRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> {
        return uploadImage("users/$uid/profile/${UUID.randomUUID()}.jpg", uri)
    }

    override suspend fun uploadNoteImage(uid: String, uri: Uri): Result<String> {
        return uploadImage("users/$uid/notes/${UUID.randomUUID()}.jpg", uri)
    }

    override suspend fun deleteImage(url: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(url).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImage(path: String, uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open image URI"))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return Result.failure(Exception("Cannot decode image"))

            // Compress to JPEG
            val tempFile = java.io.File(context.cacheDir, "upload_${UUID.randomUUID()}.jpg")
            java.io.FileOutputStream(tempFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
            }

            val uploadTask = storage.reference.child(path).putFile(Uri.fromFile(tempFile))
            uploadTask.await()

            val downloadUrl = storage.reference.child(path).downloadUrl.await()
            tempFile.delete()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: implement repository layer with Firebase Storage"
```

---

### Task 5: Create shared atomic components

**Files:**
- Create: `presentation/atoms/NotiFastButton.kt`
- Create: `presentation/atoms/NotiFastTextField.kt`
- Create: `presentation/atoms/NotiFastCard.kt`
- Create: `presentation/atoms/NotiFastChip.kt`
- Create: `presentation/atoms/NotiFastTopBar.kt`
- Create: `presentation/atoms/NotiFastCircularLoading.kt`
- Create: `presentation/atoms/NotiFastEmptyState.kt`
- Create: `presentation/atoms/NotiFastDialog.kt`
- Create: `presentation/atoms/NotiFastBackground.kt`

- [ ] **Step 1: Create NotiFastButton.kt**

```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotiFastButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.height(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text)
        }
    }
}
```

- [ ] **Step 2: Create NotiFastTextField.kt**

```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotiFastTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
```

- [ ] **Step 3: Create NotiFastCard.kt**

```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotiFastCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}
```

- [ ] **Step 4: Create remaining atoms (NotiFastChip, NotiFastTopBar, NotiFastCircularLoading, NotiFastEmptyState, NotiFastDialog, NotiFastBackground)**

`NotiFastChip.kt`:
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotiFastChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    )
}
```

`NotiFastTopBar.kt`:
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotiFastTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = if (onBackClick != null) {
            {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        } else null,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
```

`NotiFastCircularLoading.kt`:
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun NotiFastCircularLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
```

`NotiFastEmptyState.kt`:
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotiFastEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
```

`NotiFastDialog.kt`:
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotiFastDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Aceptar",
    dismissText: String = "Cancelar"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        }
    )
}
```

`NotiFastBackground.kt` — simplified version of StudyBackground (fewer particles for performance):
```kotlin
package com.notifast.app.presentation.atoms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.geometry.Size
import kotlin.math.sin

@Composable
fun NotiFastBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val particles = rememberParticles()

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = particle.duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_${particle.id}"
            )
            drawCircle(
                color = Color(particle.color).copy(alpha = alpha * 0.15f),
                radius = particle.size,
                center = Offset(
                    x = particle.baseX * size.width + sin(System.currentTimeMillis() / 2000.0 + particle.phase).toFloat() * 20f,
                    y = particle.baseY * size.height + sin(System.currentTimeMillis() / 3000.0 + particle.phase).toFloat() * 20f
                )
            )
        }
    }
}

private data class Particle(
    val id: Int,
    val baseX: Float,
    val baseY: Float,
    val size: Float,
    val duration: Int,
    val phase: Double,
    val color: Long
)

@Composable
private fun rememberParticles(): List<Particle> {
    return androidx.compose.runtime.remember {
        listOf(
            Particle(0, 0.1f, 0.1f, 30f, 3000, 0.0, 0xFF3B9797),
            Particle(1, 0.9f, 0.2f, 25f, 4000, 2.0, 0xFFBF092F),
            Particle(2, 0.2f, 0.8f, 35f, 3500, 1.5, 0xFF3B9797),
            Particle(3, 0.8f, 0.85f, 20f, 4500, 3.0, 0xFFBF092F),
            Particle(4, 0.5f, 0.5f, 40f, 5000, 0.5, 0xFF3B9797),
            Particle(5, 0.15f, 0.5f, 20f, 2800, 1.0, 0xFFBF092F),
            Particle(6, 0.7f, 0.4f, 28f, 3800, 2.5, 0xFF3B9797),
            Particle(7, 0.35f, 0.7f, 22f, 4200, 3.5, 0xFFBF092F)
        )
    }
}
```

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: add shared atomic components (atoms)"
```

---

### Task 6: Update MainActivity with @AndroidEntryPoint and clean scaffold

**Files:**
- Modify: `MainActivity.kt`

- [ ] **Step 1: Rewrite MainActivity.kt**

```kotlin
package com.notifast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.notifast.app.presentation.navigation.MainContent
import com.notifast.app.presentation.theme.theme.NotiFastTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiFastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
}
```

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: update MainActivity with Hilt @AndroidEntryPoint"
```

---

### Task 7: Fix navigation — Screen routes with typed args

**Files:**
- Modify: `presentation/navigation/Screen.kt`
- Modify: `presentation/navigation/NavGraph.kt`

- [ ] **Step 1: Update Screen.kt with argument-based routes**

```kotlin
package com.notifast.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String = "",
    val icon: ImageVector? = null
) {
    data object Login : Screen("login")

    data object Notes : Screen("notes", "Apuntes", Icons.Default.MenuBook)
    data object AddNote : Screen("add_note")
    data object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String) = "edit_note/$noteId"
    }
    data object AddSubject : Screen("add_subject")

    data object Calendar : Screen("calendar", "Calendario", Icons.Default.CalendarMonth)
    data object Schedules : Screen("horarios", "Horarios", Icons.Default.Schedule)
    data object Grades : Screen("calificaciones", "Calificaciones", Icons.Default.Grade)
    data object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)

    data object AddEvaluation : Screen("add_evaluation")
    data object EditEvaluation : Screen("edit_evaluation/{evalId}") {
        fun createRoute(evalId: String) = "edit_evaluation/$evalId"
    }

    data object SubjectDetail : Screen("subject_detail/{subjectName}") {
        fun createRoute(subjectName: String) = "subject_detail/$subjectName"
    }

    data object EditProfile : Screen("edit_profile")

    companion object {
        val bottomNavItems = listOf(Notes, Calendar, Schedules, Grades, Settings)
    }
}
```

- [ ] **Step 2: Update NavGraph.kt — remove mutableStateOf data passing, use navArguments**

```kotlin
package com.notifast.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = Screen.bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                // Will be implemented in Task 8
            }
            composable(Screen.Notes.route) {
                // Will be implemented in Task 9
            }
            composable(Screen.AddNote.route) {
                // Will be implemented in Task 10
            }
            composable(
                route = Screen.EditNote.route,
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) {
                // Will be implemented in Task 11
            }
            composable(Screen.AddSubject.route) {
                // Will be implemented in Task 9
            }
            composable(
                route = Screen.SubjectDetail.route,
                arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
            ) {
                // Will be implemented in Task 12
            }
            composable(Screen.Calendar.route) {
                // Will be implemented in Task 13
            }
            composable(Screen.Schedules.route) {
                // Will be implemented in Task 14
            }
            composable(Screen.Grades.route) {
                // Will be implemented in Task 15
            }
            composable(Screen.Settings.route) {
                // Will be implemented in Task 16
            }
            composable(Screen.EditProfile.route) {
                // Will be implemented in Task 17
            }
            composable(Screen.AddEvaluation.route) {
                // Will be implemented in Task 18
            }
            composable(
                route = Screen.EditEvaluation.route,
                arguments = listOf(navArgument("evalId") { type = NavType.StringType })
            ) {
                // Will be implemented in Task 18
            }
        }
    }
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: refactor navigation with typed route arguments"
```

---

### Task 8: Migrate auth feature (Login)

**Files:**
- Create: `presentation/feature/auth/LoginContract.kt`
- Create: `presentation/feature/auth/LoginViewModel.kt`
- Create: `presentation/feature/auth/LoginScreen.kt`
- Create: `presentation/feature/auth/components/LoginForm.kt`

- [ ] **Step 1: Create LoginContract.kt**

```kotlin
package com.notifast.app.presentation.feature.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object LoginClicked : LoginEvent
    data object SignUpClicked : LoginEvent
    data object ClearError : LoginEvent
}
```

- [ ] **Step 2: Create LoginViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _uiState.update { it.copy(email = event.value) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value) }
            is LoginEvent.LoginClicked -> login()
            is LoginEvent.SignUpClicked -> signUp()
            is LoginEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(
                _uiState.value.email,
                _uiState.value.password
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { _uiState.update { it.copy(isLoading = false, error = it.message) } }
            )
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signUp(
                _uiState.value.email,
                _uiState.value.password,
                "", // firstName — will be set in edit profile
                "", // lastName
                null
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { _uiState.update { it.copy(isLoading = false, error = it.message) } }
            )
        }
    }
}
```

- [ ] **Step 3: Create LoginScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastBackground
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCircularLoading

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoggedIn) {
        onLoginSuccess()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NotiFast",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(32.dp))

            LoginForm(
                email = uiState.email,
                password = uiState.password,
                onEmailChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                onPasswordChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                error = uiState.error,
                onClearError = { viewModel.onEvent(LoginEvent.ClearError) }
            )

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                NotiFastCircularLoading()
            } else {
                NotiFastButton(
                    text = "Iniciar Sesión",
                    onClick = { viewModel.onEvent(LoginEvent.LoginClicked) }
                )

                Spacer(Modifier.height(8.dp))

                NotiFastButton(
                    text = "Registrarse",
                    onClick = { viewModel.onEvent(LoginEvent.SignUpClicked) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
```

- [ ] **Step 4: Create LoginForm molecule**

```kotlin
package com.notifast.app.presentation.feature.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.atoms.NotiFastTextField

@Composable
fun LoginForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    error: String?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        NotiFastTextField(
            value = email,
            onValueChange = { onEmailChange(it); onClearError() },
            label = "Correo electrónico",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        NotiFastTextField(
            value = password,
            onValueChange = { onPasswordChange(it); onClearError() },
            label = "Contraseña",
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

Note: add `import androidx.compose.material3.MaterialTheme` to LoginForm.kt.

- [ ] **Step 5: Wire LoginScreen in NavGraph**

In `NavGraph.kt`, replace the login composable placeholder:
```kotlin
composable(Screen.Login.route) {
    LoginScreen(
        onLoginSuccess = {
            navController.navigate(Screen.Notes.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        },
        onSignUpSuccess = {
            navController.navigate(Screen.Notes.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    )
}
```

Add import: `import com.notifast.app.presentation.feature.auth.LoginScreen`

- [ ] **Step 6: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat: migrate auth feature to MVVM contract pattern"
```

---

### Task 9: Migrate notes feature (main list)

**Files:**
- Create: `presentation/feature/notes/NotesContract.kt`
- Create: `presentation/feature/notes/NotesViewModel.kt`
- Create: `presentation/feature/notes/NotesScreen.kt`
- Create: `presentation/feature/notes/components/NoteCard.kt`
- Create: `presentation/feature/notes/components/NoteSearchBar.kt`
- Create: `presentation/feature/notes/components/NoteFilterBar.kt`

- [ ] **Step 1: Create NotesContract.kt**

```kotlin
package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Note

enum class NoteFilter { RECIENTES, ANTIGUAS, POR_MATERIA }

data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: Map<String, List<Note>> = emptyMap(),
    val allNotesList: List<Note> = emptyList(),
    val subjects: List<String> = emptyList(),
    val currentFilter: NoteFilter = NoteFilter.RECIENTES,
    val searchQuery: String = "",
    val syncMessage: String? = null,
    val error: String? = null
)

sealed interface NotesEvent {
    data object LoadNotes : NotesEvent
    data class SetFilter(val filter: NoteFilter) : NotesEvent
    data class Search(val query: String) : NotesEvent
    data class DeleteNote(val note: Note) : NotesEvent
    data object ClearSyncMessage : NotesEvent
}
```

- [ ] **Step 2: Create NotesViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Note
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    private val _currentFilter = MutableStateFlow(NoteFilter.RECIENTES)
    private val _searchQuery = MutableStateFlow("")

    init {
        loadNotes()
    }

    private fun loadNotes() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                noteRepository.getNotes(userId),
                noteRepository.getSubjects(userId),
                _currentFilter,
                _searchQuery
            ) { notes, subjects, filter, query ->
                val filtered = filterNotes(notes, filter, query)
                NotesUiState(
                    isLoading = false,
                    notes = filtered,
                    allNotesList = notes,
                    subjects = subjects,
                    currentFilter = filter,
                    searchQuery = query
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun filterNotes(
        notes: List<Note>,
        filter: NoteFilter,
        query: String
    ): Map<String, List<Note>> {
        var filtered = notes
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            }
        }
        return when (filter) {
            NoteFilter.RECIENTES -> filtered
                .sortedByDescending { it.timestamp }
                .groupBy { it.subject }
            NoteFilter.ANTIGUAS -> filtered
                .sortedBy { it.timestamp ?: java.util.Date() }
                .groupBy { it.subject }
            NoteFilter.POR_MATERIA -> filtered.groupBy { it.subject }
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.LoadNotes -> loadNotes()
            is NotesEvent.SetFilter -> _currentFilter.value = event.filter
            is NotesEvent.Search -> _searchQuery.value = event.query
            is NotesEvent.DeleteNote -> deleteNote(event.note)
            is NotesEvent.ClearSyncMessage -> _uiState.update { it.copy(syncMessage = null) }
        }
    }

    private fun deleteNote(note: Note) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(syncMessage = "Eliminando...") }
            val result = noteRepository.deleteNote(userId, note)
            result.fold(
                onSuccess = { _uiState.update { it.copy(syncMessage = null) } },
                onFailure = {
                    _uiState.update {
                        it.copy(syncMessage = "Error al eliminar: ${it.message}")
                    }
                }
            )
        }
    }
}
```

- [ ] **Step 3: Create NotesScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.feature.notes.components.NoteCard
import com.notifast.app.presentation.feature.notes.components.NoteFilterBar
import com.notifast.app.presentation.feature.notes.components.NoteSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onEditProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            com.notifast.app.presentation.atoms.NotiFastTopBar(title = "NotiFast")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NoteSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onEvent(NotesEvent.Search(it)) }
            )

            NoteFilterBar(
                currentFilter = uiState.currentFilter,
                onFilterChange = { viewModel.onEvent(NotesEvent.SetFilter(it)) }
            )

            if (uiState.isLoading) {
                NotiFastCircularLoading()
            } else if (uiState.notes.isEmpty()) {
                NotiFastEmptyState(message = "No hay apuntes aún")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.notes.forEach { (subject, notes) ->
                        item {
                            SubjectHeader(
                                subjectName = subject,
                                onClick = { onSubjectClick(subject) }
                            )
                        }
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note) },
                                onDelete = { viewModel.onEvent(NotesEvent.DeleteNote(note)) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectHeader(
    subjectName: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(
            text = subjectName,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
    }
}
```

- [ ] **Step 4: Create NoteCard molecule**

```kotlin
package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastCard

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotiFastCard(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar nota",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 5: Create NoteSearchBar molecule**

```kotlin
package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NoteSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar apuntes...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true
    )
}
```

- [ ] **Step 6: Create NoteFilterBar molecule**

```kotlin
package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.atoms.NotiFastChip
import com.notifast.app.presentation.feature.notes.NoteFilter

@Composable
fun NoteFilterBar(
    currentFilter: NoteFilter,
    onFilterChange: (NoteFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NoteFilter.entries.forEach { filter ->
            val label = when (filter) {
                NoteFilter.RECIENTES -> "Recientes"
                NoteFilter.ANTIGUAS -> "Antiguas"
                NoteFilter.POR_MATERIA -> "Por materia"
            }
            NotiFastChip(
                label = label,
                selected = currentFilter == filter,
                onClick = { onFilterChange(filter) }
            )
        }
    }
}
```

- [ ] **Step 7: Wire NotesScreen in NavGraph**

```kotlin
composable(Screen.Notes.route) {
    NotesScreen(
        onNoteClick = { note ->
            navController.navigate(Screen.EditNote.createRoute(note.id))
        },
        onAddNoteClick = {
            navController.navigate(Screen.AddNote.route)
        },
        onSubjectClick = { subject ->
            navController.navigate(Screen.SubjectDetail.createRoute(subject))
        },
        onEditProfileClick = {
            navController.navigate(Screen.EditProfile.route)
        }
    )
}
```

Add import: `import com.notifast.app.presentation.feature.notes.NotesScreen`

- [ ] **Step 8: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add -A && git commit -m "feat: migrate notes feature to MVVM contract pattern"
```

---

### Task 10: Migrate AddNote screen

**Files:**
- Create: `presentation/feature/addnote/AddNoteContract.kt`
- Create: `presentation/feature/addnote/AddNoteViewModel.kt`
- Create: `presentation/feature/addnote/AddNoteScreen.kt`
- Create: `presentation/feature/addnote/components/`

- [ ] **Step 1: Create AddNoteContract.kt**

```kotlin
package com.notifast.app.presentation.feature.addnote

import android.net.Uri

data class AddNoteUiState(
    val subject: String = "",
    val title: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val subjects: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface AddNoteEvent {
    data class SubjectChanged(val value: String) : AddNoteEvent
    data class TitleChanged(val value: String) : AddNoteEvent
    data class ContentChanged(val value: String) : AddNoteEvent
    data class ImageSelected(val uri: Uri?) : AddNoteEvent
    data object SaveClicked : AddNoteEvent
    data object ClearError : AddNoteEvent
}
```

- [ ] **Step 2: Create AddNoteViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddNoteEvent) {
        when (event) {
            is AddNoteEvent.SubjectChanged -> _uiState.update { it.copy(subject = event.value) }
            is AddNoteEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is AddNoteEvent.ContentChanged -> _uiState.update { it.copy(content = event.value) }
            is AddNoteEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is AddNoteEvent.SaveClicked -> saveNote()
            is AddNoteEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state.subject.isBlank() || state.title.isBlank()) {
            _uiState.update { it.copy(error = "Materia y título son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }
            val result = noteRepository.addNote(
                userId, state.subject, state.title,
                state.content, state.imageUri
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = {
                    _uiState.update {
                        it.copy(isLoading = false, error = it.message ?: "Error al guardar")
                    }
                }
            )
        }
    }
}
```

- [ ] **Step 3: Create AddNoteScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.addnote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isSaved) {
        onNoteAdded()
        return
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(AddNoteEvent.ImageSelected(uri))
    }

    Scaffold(
        topBar = { NotiFastTopBar(title = "Nuevo Apunte", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            NotiFastTextField(
                value = uiState.subject,
                onValueChange = { viewModel.onEvent(AddNoteEvent.SubjectChanged(it)) },
                label = "Materia",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(
                value = uiState.title,
                onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                label = "Título",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(
                value = uiState.content,
                onValueChange = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) },
                label = "Contenido",
                modifier = Modifier.fillMaxWidth().height(200.dp),
                singleLine = false
            )
            Spacer(Modifier.height(12.dp))
            NotiFastButton(
                text = if (uiState.imageUri == null) "Agregar Imagen" else "Imagen seleccionada",
                onClick = { imagePickerLauncher.launch("image/*") }
            )
            Spacer(Modifier.height(16.dp))
            if (uiState.error != null) {
                androidx.compose.material3.Text(
                    text = uiState.error,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }
            NotiFastButton(
                text = "GUARDAR APUNTE",
                onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) },
                isLoading = uiState.isLoading
            )
        }
    }
}
```

- [ ] **Step 4: Wire AddNoteScreen in NavGraph**

```kotlin
composable(Screen.AddNote.route) {
    AddNoteScreen(
        onBackClick = { navController.popBackStack() },
        onNoteAdded = { navController.popBackStack() }
    )
}
```

Add import: `import com.notifast.app.presentation.feature.addnote.AddNoteScreen`

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: migrate add-note feature to MVVM contract pattern"
```

---

### Task 11: Migrate EditNote screen

**Files:**
- Create: `presentation/feature/editnote/EditNoteContract.kt`
- Create: `presentation/feature/editnote/EditNoteViewModel.kt`
- Create: `presentation/feature/editnote/EditNoteScreen.kt`

- [ ] **Step 1: Create EditNoteContract.kt**

```kotlin
package com.notifast.app.presentation.feature.editnote

import android.net.Uri
import com.notifast.app.data.model.Note

data class EditNoteUiState(
    val note: Note? = null,
    val subject: String = "",
    val title: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface EditNoteEvent {
    data class SubjectChanged(val value: String) : EditNoteEvent
    data class TitleChanged(val value: String) : EditNoteEvent
    data class ContentChanged(val value: String) : EditNoteEvent
    data class ImageSelected(val uri: Uri?) : EditNoteEvent
    data object SaveClicked : EditNoteEvent
    data object ClearError : EditNoteEvent
}
```

- [ ] **Step 2: Create EditNoteViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.editnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Note
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    private val _uiState = MutableStateFlow(EditNoteUiState())
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val notes = noteRepository.getNotes(userId).first()
            val note = notes.find { it.id == noteId }
            if (note != null) {
                _uiState.update {
                    it.copy(
                        note = note,
                        subject = note.subject,
                        title = note.title,
                        content = note.content
                    )
                }
            }
        }
    }

    fun onEvent(event: EditNoteEvent) {
        when (event) {
            is EditNoteEvent.SubjectChanged -> _uiState.update { it.copy(subject = event.value) }
            is EditNoteEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is EditNoteEvent.ContentChanged -> _uiState.update { it.copy(content = event.value) }
            is EditNoteEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is EditNoteEvent.SaveClicked -> saveNote()
            is EditNoteEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        val note = state.note ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado") }
                return@launch
            }
            val updatedNote = note.copy(
                subject = state.subject,
                title = state.title,
                content = state.content
            )
            val result = noteRepository.updateNote(userId, updatedNote, state.imageUri)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = {
                    _uiState.update {
                        it.copy(isLoading = false, error = it.message ?: "Error al guardar")
                    }
                }
            )
        }
    }
}
```

- [ ] **Step 3: Create EditNoteScreen.kt**

(similar to AddNoteScreen but pre-populated with note data)

```kotlin
package com.notifast.app.presentation.feature.editnote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    viewModel: EditNoteViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isSaved) {
        onNoteSaved()
        return
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(EditNoteEvent.ImageSelected(uri))
    }

    Scaffold(
        topBar = { NotiFastTopBar(title = "Editar Apunte", onBackClick = onBackClick) }
    ) { innerPadding ->
        if (uiState.note == null) {
            NotiFastCircularLoading()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                NotiFastTextField(
                    value = uiState.subject,
                    onValueChange = { viewModel.onEvent(EditNoteEvent.SubjectChanged(it)) },
                    label = "Materia",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                NotiFastTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(EditNoteEvent.TitleChanged(it)) },
                    label = "Título",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                NotiFastTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.onEvent(EditNoteEvent.ContentChanged(it)) },
                    label = "Contenido",
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    singleLine = false
                )
                Spacer(Modifier.height(12.dp))
                NotiFastButton(
                    text = if (uiState.imageUri == null) "Cambiar Imagen" else "Imagen seleccionada",
                    onClick = { imagePickerLauncher.launch("image/*") }
                )
                Spacer(Modifier.height(16.dp))
                if (uiState.error != null) {
                    androidx.compose.material3.Text(
                        text = uiState.error,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }
                NotiFastButton(
                    text = "GUARDAR CAMBIOS",
                    onClick = { viewModel.onEvent(EditNoteEvent.SaveClicked) },
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}
```

- [ ] **Step 4: Wire EditNoteScreen in NavGraph**

```kotlin
composable(
    route = Screen.EditNote.route,
    arguments = listOf(navArgument("noteId") { type = NavType.StringType })
) {
    EditNoteScreen(
        onBackClick = { navController.popBackStack() },
        onNoteSaved = { navController.popBackStack() }
    )
}
```

Add import: `import com.notifast.app.presentation.feature.editnote.EditNoteScreen`

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: migrate edit-note feature to MVVM contract pattern"
```

---

### Task 12: Migrate SubjectDetail screen

**Files:**
- Create: `presentation/feature/subjectdetail/SubjectDetailContract.kt`
- Create: `presentation/feature/subjectdetail/SubjectDetailViewModel.kt`
- Create: `presentation/feature/subjectdetail/SubjectDetailScreen.kt`

- [ ] **Step 1: Create SubjectDetailContract.kt**

```kotlin
package com.notifast.app.presentation.feature.subjectdetail

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note

data class SubjectDetailUiState(
    val subjectName: String = "",
    val notes: List<Note> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
}
```

- [ ] **Step 2: Create SubjectDetailViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val subjectName: String = checkNotNull(savedStateHandle["subjectName"])

    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                noteRepository.getNotesBySubject(userId, subjectName),
                evaluationRepository.getEvaluations(userId)
            ) { notes, evaluations ->
                val filteredEvals = evaluations.filter { it.subjectName == subjectName }
                SubjectDetailUiState(
                    subjectName = subjectName,
                    notes = notes,
                    evaluations = filteredEvals,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: SubjectDetailEvent) {
        when (event) {
            is SubjectDetailEvent.LoadData -> loadData()
        }
    }
}
```

- [ ] **Step 3: Create SubjectDetailScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.subjectdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { NotiFastTopBar(title = uiState.subjectName, onBackClick = onBackClick) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            NotiFastCircularLoading()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Apuntes",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (uiState.notes.isEmpty()) {
                    item { Text("Sin apuntes") }
                } else {
                    items(uiState.notes) { note ->
                        Text(note.title)
                        Spacer(Modifier.height(4.dp))
                    }
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Evaluaciones",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (uiState.evaluations.isEmpty()) {
                    item {
                        NotiFastEmptyState(message = "Sin evaluaciones")
                    }
                } else {
                    items(uiState.evaluations) { eval ->
                        Text(eval.title)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Wire in NavGraph**

```kotlin
composable(
    route = Screen.SubjectDetail.route,
    arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
) {
    SubjectDetailScreen(
        onBackClick = { navController.popBackStack() }
    )
}
```

Add import: `import com.notifast.app.presentation.feature.subjectdetail.SubjectDetailScreen`

- [ ] **Step 5: Verify build && commit**

---

### Task 13: Migrate Calendar screen

**Files:**
- Create: `presentation/feature/calendar/CalendarContract.kt`
- Create: `presentation/feature/calendar/CalendarViewModel.kt`
- Create: `presentation/feature/calendar/CalendarScreen.kt`
- Create: `presentation/feature/calendar/components/CalendarGrid.kt`

- [ ] **Step 1: Create CalendarContract.kt**

```kotlin
package com.notifast.app.presentation.feature.calendar

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import java.util.Date

data class CalendarUiState(
    val selectedDate: Date = Date(),
    val notesOnDate: List<Note> = emptyList(),
    val evaluationsOnDate: List<Evaluation> = emptyList(),
    val evaluationsByDate: Map<String, List<Evaluation>> = emptyMap(),
    val isLoading: Boolean = false
)

sealed interface CalendarEvent {
    data class DateSelected(val date: Date) : CalendarEvent
    data object LoadData : CalendarEvent
}
```

- [ ] **Step 2: Create CalendarViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                noteRepository.getNotes(userId),
                evaluationRepository.getEvaluations(userId)
            ) { notes, evaluations ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedKey = dateFormat.format(_uiState.value.selectedDate)

                CalendarUiState(
                    selectedDate = _uiState.value.selectedDate,
                    notesOnDate = notes.filter {
                        it.timestamp?.let { t ->
                            dateFormat.format(t) == selectedKey
                        } ?: false
                    },
                    evaluationsOnDate = evaluations.filter {
                        it.date?.let { d ->
                            dateFormat.format(d) == selectedKey
                        } ?: false
                    },
                    evaluationsByDate = evaluations.groupBy {
                        it.date?.let { d -> dateFormat.format(d) } ?: "unknown"
                    },
                    isLoading = false
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.DateSelected -> {
                _uiState.update { it.copy(selectedDate = event.date) }
                loadData()
            }
            is CalendarEvent.LoadData -> loadData()
        }
    }
}
```

- [ ] **Step 3: Create CalendarScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.feature.calendar.components.CalendarGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { NotiFastTopBar(title = "Calendario") }
    ) { innerPadding ->
        if (uiState.isLoading) {
            NotiFastCircularLoading()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                item {
                    CalendarGrid(
                        selectedDate = uiState.selectedDate,
                        evaluationsByDate = uiState.evaluationsByDate,
                        onDateSelected = { viewModel.onEvent(CalendarEvent.DateSelected(it)) }
                    )
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Evaluaciones", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }
                if (uiState.evaluationsOnDate.isEmpty()) {
                    item { Text("Sin evaluaciones en esta fecha") }
                } else {
                    items(uiState.evaluationsOnDate) { eval ->
                        NotiFastCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column {
                                Text(eval.title, style = MaterialTheme.typography.titleSmall)
                                Text(eval.subjectName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Apuntes", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }
                if (uiState.notesOnDate.isEmpty()) {
                    item { Text("Sin apuntes en esta fecha") }
                } else {
                    items(uiState.notesOnDate) { note ->
                        NotiFastCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column {
                                Text(note.title, style = MaterialTheme.typography.titleSmall)
                                Text(note.subject, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Create CalendarGrid molecule**

```kotlin
package com.notifast.app.presentation.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarGrid(
    selectedDate: Date,
    evaluationsByDate: Map<String, List<Any>>,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    calendar.time = selectedDate
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply {
        set(year, month, 1)
    }.get(Calendar.DAY_OF_WEEK) - 1

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val days = remember(firstDayOfWeek, daysInMonth) {
        List(firstDayOfWeek) { null } + (1..daysInMonth).map { it }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb").forEach { day ->
                Text(day, style = MaterialTheme.typography.labelSmall)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { day ->
                if (day != null) {
                    val date = Calendar.getInstance().apply {
                        set(year, month, day)
                    }.time
                    val dateKey = dateFormat.format(date)
                    val hasEvents = evaluationsByDate.containsKey(dateKey)
                    val isSelected = dateFormat.format(date) == dateFormat.format(selectedDate)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                            )
                            if (hasEvents) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = evaluationsByDate[dateKey]?.size?.toString() ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

Note: The `evaluationsByDate` type above uses `List<Any>` for simplicity; in the actual call site (CalendarScreen) the grid receives the map from the ViewModel correctly typed. Adjust as needed during implementation.

- [ ] **Step 5: Wire in NavGraph**

```kotlin
composable(Screen.Calendar.route) {
    CalendarScreen()
}
```

Add import: `import com.notifast.app.presentation.feature.calendar.CalendarScreen`

- [ ] **Step 6: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat: migrate calendar feature to MVVM contract pattern"
```

---

### Task 14: Implement Schedules screen (not placeholder)

**Files:**
- Create: `data/model/Schedule.kt`
- Create: `domain/repository/ScheduleRepository.kt`
- Create: `data/repository/ScheduleRepositoryImpl.kt`
- Create: `presentation/feature/schedules/SchedulesContract.kt`
- Create: `presentation/feature/schedules/SchedulesViewModel.kt`
- Create: `presentation/feature/schedules/SchedulesScreen.kt`
- Modify: `di/RepositoryModule.kt`

- [ ] **Step 1: Create Schedule data class in data/model/Schedule.kt**

```kotlin
package com.notifast.app.data.model

data class Schedule(
    val id: String = "",
    val userId: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val subject: String = "",
    val classroom: String = ""
)
```

- [ ] **Step 2: Create domain/repository/ScheduleRepository.kt**

```kotlin
package com.notifast.app.domain.repository

import com.notifast.app.data.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getSchedules(userId: String): Flow<List<Schedule>>
    suspend fun addSchedule(userId: String, schedule: Schedule): Result<Unit>
    suspend fun deleteSchedule(userId: String, schedule: Schedule): Result<Unit>
}
```

- [ ] **Step 3: Create data/repository/ScheduleRepositoryImpl.kt**

```kotlin
package com.notifast.app.data.repository

import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.ScheduleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ScheduleRepository {

    override fun getSchedules(userId: String): Flow<List<Schedule>> {
        return db.collection("users").document(userId)
            .collection("schedules")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Schedule::class.java) }
            }
    }

    override suspend fun addSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("schedules").add(schedule).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("schedules").document(schedule.id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

- [ ] **Step 4: Add ScheduleRepository binding to RepositoryModule**

Add to `RepositoryModule.kt`:
```kotlin
@Binds
@Singleton
abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository
```

- [ ] **Step 5: Create SchedulesContract.kt**

```kotlin
package com.notifast.app.presentation.feature.schedules

import com.notifast.app.data.model.Schedule

data class SchedulesUiState(
    val schedules: List<Schedule> = emptyList(),
    val groupedByDay: Map<String, List<Schedule>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface SchedulesEvent {
    data object LoadSchedules : SchedulesEvent
}
```

- [ ] **Step 6: Create SchedulesViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulesViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulesUiState())
    val uiState: StateFlow<SchedulesUiState> = _uiState.asStateFlow()

    init { loadSchedules() }

    private fun loadSchedules() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            scheduleRepository.getSchedules(userId).map { schedules ->
                SchedulesUiState(
                    schedules = schedules,
                    groupedByDay = schedules.groupBy { it.dayOfWeek },
                    isLoading = false
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: SchedulesEvent) {
        when (event) {
            is SchedulesEvent.LoadSchedules -> loadSchedules()
        }
    }
}
```

- [ ] **Step 7: Create SchedulesScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.schedules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    viewModel: SchedulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { NotiFastTopBar(title = "Horarios") }
    ) { innerPadding ->
        if (uiState.isLoading) {
            NotiFastCircularLoading()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                val dayOrder = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO")
                dayOrder.forEach { day ->
                    val daySchedules = uiState.groupedByDay[day] ?: emptyList()
                    item {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    if (daySchedules.isEmpty()) {
                        item {
                            Text(
                                "Sin horarios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(daySchedules) { schedule ->
                            NotiFastCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${schedule.startTime} - ${schedule.endTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(schedule.subject, style = MaterialTheme.typography.titleSmall)
                                        if (schedule.classroom.isNotBlank()) {
                                            Text(
                                                schedule.classroom,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 8: Wire in NavGraph**

```kotlin
composable(Screen.Schedules.route) {
    SchedulesScreen()
}
```

Add import: `import com.notifast.app.presentation.feature.schedules.SchedulesScreen`

- [ ] **Step 9: Verify build && commit**

---

### Task 15: Migrate Grades screen

**Files:**
- Create: `presentation/feature/grades/GradesContract.kt`
- Create: `presentation/feature/grades/GradesViewModel.kt`
- Create: `presentation/feature/grades/GradesScreen.kt`
- Create: `presentation/feature/grades/components/GradeCard.kt`

- [ ] **Step 1: Create GradesContract.kt**

```kotlin
package com.notifast.app.presentation.feature.grades

import com.notifast.app.data.model.Evaluation

data class GradesUiState(
    val evaluationsBySubject: Map<String, List<Evaluation>> = emptyMap(),
    val overallAverage: Double = 0.0,
    val isLoading: Boolean = false
)

sealed interface GradesEvent {
    data object LoadGrades : GradesEvent
}
```

- [ ] **Step 2: Create GradesViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    init { loadGrades() }

    private fun loadGrades() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            evaluationRepository.getEvaluations(userId).map { evaluations ->
                val graded = evaluations.filter { it.grade != null && it.weight != null }
                val weightedSum = graded.sumOf { (it.grade ?: 0.0) * (it.weight ?: 0.0) }
                val totalWeight = graded.sumOf { it.weight ?: 0.0 }
                val average = if (totalWeight > 0) weightedSum / totalWeight else 0.0

                GradesUiState(
                    evaluationsBySubject = evaluations.groupBy { it.subjectName },
                    overallAverage = Math.round(average * 100.0) / 100.0,
                    isLoading = false
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onEvent(event: GradesEvent) {
        when (event) {
            is GradesEvent.LoadGrades -> loadGrades()
        }
    }
}
```

- [ ] **Step 3: Create GradeCard molecule**

```kotlin
package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifast.app.data.model.Evaluation
import com.notifast.app.presentation.atoms.NotiFastCard

@Composable
fun GradeCard(
    evaluation: Evaluation,
    modifier: Modifier = Modifier
) {
    NotiFastCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(evaluation.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    evaluation.subjectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                evaluation.grade?.let {
                    Text(
                        "${it}/10",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                evaluation.weight?.let {
                    Text(
                        "Peso: ${it}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 4: Create GradesScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.grades

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.feature.grades.components.GradeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    viewModel: GradesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { NotiFastTopBar(title = "Calificaciones") }
    ) { innerPadding ->
        if (uiState.isLoading) {
            NotiFastCircularLoading()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                item {
                    NotiFastCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(
                                "Promedio General",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${uiState.overallAverage}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                uiState.evaluationsBySubject.forEach { (subject, evals) ->
                    item {
                        Text(subject, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(evals) { eval ->
                        GradeCard(evaluation = eval)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Wire in NavGraph** and verify build

```kotlin
composable(Screen.Grades.route) {
    GradesScreen()
}
```

Add import: `import com.notifast.app.presentation.feature.grades.GradesScreen`

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: migrate grades feature to MVVM contract pattern"
```

---

### Task 16: Migrate Settings screen

**Files:**
- Create: `presentation/feature/settings/SettingsContract.kt`
- Create: `presentation/feature/settings/SettingsViewModel.kt`
- Create: `presentation/feature/settings/SettingsScreen.kt`

- [ ] **Step 1: Create SettingsContract.kt**

```kotlin
package com.notifast.app.presentation.feature.settings

import com.notifast.app.data.model.User

data class SettingsUiState(
    val user: User? = null,
    val isLoading: Boolean = false
)

sealed interface SettingsEvent {
    data object LoadProfile : SettingsEvent
    data object Logout : SettingsEvent
}
```

- [ ] **Step 2: Create SettingsViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                authRepository.getUserProfile(uid).fold(
                    onSuccess = { _uiState.update { it.copy(user = it, isLoading = false) } },
                    onFailure = { _uiState.update { it.copy(isLoading = false) } }
                )
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadProfile -> loadProfile()
            is SettingsEvent.Logout -> {
                authRepository.logout()
                _isLoggedOut.value = true
            }
        }
    }
}
```

- [ ] **Step 3: Create SettingsScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedOut by viewModel.isLoggedOut.collectAsStateWithLifecycle()

    if (isLoggedOut) {
        onLogout()
        return
    }

    Scaffold(
        topBar = { NotiFastTopBar(title = "Ajustes") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            uiState.user?.let { user ->
                NotiFastCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            NotiFastButton(
                text = "Editar Perfil",
                onClick = onEditProfileClick
            )
            Spacer(Modifier.height(8.dp))
            NotiFastButton(
                text = "Cerrar Sesión",
                onClick = { viewModel.onEvent(SettingsEvent.Logout) }
            )
        }
    }
}
```

- [ ] **Step 4: Wire in NavGraph**

```kotlin
composable(Screen.Settings.route) {
    SettingsScreen(
        onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
        onLogout = {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}
```

Add import: `import com.notifast.app.presentation.feature.settings.SettingsScreen`

- [ ] **Step 5: Verify build && commit**

---

### Task 17: Migrate EditProfile screen

**Files:**
- Create: `presentation/feature/editprofile/EditProfileContract.kt`
- Create: `presentation/feature/editprofile/EditProfileViewModel.kt`
- Create: `presentation/feature/editprofile/EditProfileScreen.kt`

- [ ] **Step 1: Create EditProfileContract.kt**

```kotlin
package com.notifast.app.presentation.feature.editprofile

import android.net.Uri

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface EditProfileEvent {
    data class FirstNameChanged(val value: String) : EditProfileEvent
    data class LastNameChanged(val value: String) : EditProfileEvent
    data class ImageSelected(val uri: Uri?) : EditProfileEvent
    data object SaveClicked : EditProfileEvent
    data object ClearError : EditProfileEvent
}
```

- [ ] **Step 2: Create EditProfileViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(uid).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(firstName = user.firstName, lastName = user.lastName)
                    }
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.FirstNameChanged -> _uiState.update { it.copy(firstName = event.value) }
            is EditProfileEvent.LastNameChanged -> _uiState.update { it.copy(lastName = event.value) }
            is EditProfileEvent.ImageSelected -> _uiState.update { it.copy(profileImageUri = event.uri) }
            is EditProfileEvent.SaveClicked -> saveProfile()
            is EditProfileEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, error = "No autenticado") }
                return@launch
            }
            val result = authRepository.updateProfile(
                uid, _uiState.value.firstName,
                _uiState.value.lastName, _uiState.value.profileImageUri
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = {
                    _uiState.update {
                        it.copy(isLoading = false, error = it.message ?: "Error")
                    }
                }
            )
        }
    }
}
```

- [ ] **Step 3: Create EditProfileScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.editprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onProfileSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isSaved) {
        onProfileSaved()
        return
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onEvent(EditProfileEvent.ImageSelected(uri)) }

    Scaffold(
        topBar = { NotiFastTopBar(title = "Editar Perfil", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            NotiFastTextField(
                value = uiState.firstName,
                onValueChange = { viewModel.onEvent(EditProfileEvent.FirstNameChanged(it)) },
                label = "Nombre",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(
                value = uiState.lastName,
                onValueChange = { viewModel.onEvent(EditProfileEvent.LastNameChanged(it)) },
                label = "Apellido",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastButton(
                text = if (uiState.profileImageUri == null) "Cambiar Foto" else "Foto seleccionada",
                onClick = { imagePickerLauncher.launch("image/*") }
            )
            Spacer(Modifier.height(16.dp))
            if (uiState.error != null) {
                androidx.compose.material3.Text(
                    text = uiState.error,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }
            NotiFastButton(
                text = "GUARDAR",
                onClick = { viewModel.onEvent(EditProfileEvent.SaveClicked) },
                isLoading = uiState.isLoading
            )
        }
    }
}
```

- [ ] **Step 4: Wire in NavGraph**

```kotlin
composable(Screen.EditProfile.route) {
    EditProfileScreen(
        onBackClick = { navController.popBackStack() },
        onProfileSaved = { navController.popBackStack() }
    )
}
```

Add import: `import com.notifast.app.presentation.feature.editprofile.EditProfileScreen`

- [ ] **Step 5: Verify build && commit**

---

### Task 18: Migrate AddEvaluation + EditEvaluation screens

**Files:**
- Create: `presentation/feature/addevaluation/AddEvaluationContract.kt`
- Create: `presentation/feature/addevaluation/AddEvaluationViewModel.kt`
- Create: `presentation/feature/addevaluation/AddEvaluationScreen.kt`
- Create: `presentation/feature/editevaluation/EditEvaluationContract.kt`
- Create: `presentation/feature/editevaluation/EditEvaluationViewModel.kt`
- Create: `presentation/feature/editevaluation/EditEvaluationScreen.kt`

- [ ] **Step 1: Create AddEvaluationContract.kt**

```kotlin
package com.notifast.app.presentation.feature.addevaluation

import android.net.Uri
import com.notifast.app.data.model.Evaluation
import java.util.Date

data class AddEvaluationUiState(
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date? = null,
    val quickTips: String = "",
    val weight: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface AddEvaluationEvent {
    data class SubjectChanged(val value: String) : AddEvaluationEvent
    data class TitleChanged(val value: String) : AddEvaluationEvent
    data class DescriptionChanged(val value: String) : AddEvaluationEvent
    data class DateChanged(val value: Date?) : AddEvaluationEvent
    data class QuickTipsChanged(val value: String) : AddEvaluationEvent
    data class WeightChanged(val value: String) : AddEvaluationEvent
    data class ImageSelected(val uri: Uri?) : AddEvaluationEvent
    data object SaveClicked : AddEvaluationEvent
    data object ClearError : AddEvaluationEvent
}
```

- [ ] **Step 2: Create AddEvaluationViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.addevaluation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEvaluationViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEvaluationUiState())
    val uiState: StateFlow<AddEvaluationUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddEvaluationEvent) {
        when (event) {
            is AddEvaluationEvent.SubjectChanged -> _uiState.update { it.copy(subjectName = event.value) }
            is AddEvaluationEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is AddEvaluationEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is AddEvaluationEvent.DateChanged -> _uiState.update { it.copy(date = event.value) }
            is AddEvaluationEvent.QuickTipsChanged -> _uiState.update { it.copy(quickTips = event.value) }
            is AddEvaluationEvent.WeightChanged -> _uiState.update { it.copy(weight = event.value) }
            is AddEvaluationEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is AddEvaluationEvent.SaveClicked -> saveEvaluation()
            is AddEvaluationEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveEvaluation() {
        val state = _uiState.value
        if (state.title.isBlank() || state.subjectName.isBlank()) {
            _uiState.update { it.copy(error = "Título y materia son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No autenticado") }
                return@launch
            }
            val evaluation = Evaluation(
                userId = userId,
                subjectName = state.subjectName,
                title = state.title,
                description = state.description,
                date = state.date,
                quickTips = state.quickTips,
                weight = state.weight.toDoubleOrNull(),
                grade = 0.0
            )
            val result = evaluationRepository.addEvaluation(userId, evaluation, state.imageUri)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = it.message) }
                }
            )
        }
    }
}
```

- [ ] **Step 3: Create AddEvaluationScreen.kt**

```kotlin
package com.notifast.app.presentation.feature.addevaluation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvaluationScreen(
    viewModel: AddEvaluationViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onEvaluationAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isSaved) {
        onEvaluationAdded()
        return
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onEvent(AddEvaluationEvent.ImageSelected(uri)) }

    Scaffold(
        topBar = { NotiFastTopBar(title = "Nueva Evaluación", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            NotiFastTextField(
                value = uiState.subjectName,
                onValueChange = { viewModel.onEvent(AddEvaluationEvent.SubjectChanged(it)) },
                label = "Materia",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(
                value = uiState.title,
                onValueChange = { viewModel.onEvent(AddEvaluationEvent.TitleChanged(it)) },
                label = "Título",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(
                value = uiState.description,
                onValueChange = { viewModel.onEvent(AddEvaluationEvent.DescriptionChanged(it)) },
                label = "Descripción",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(
                value = uiState.quickTips,
                onValueChange = { viewModel.onEvent(AddEvaluationEvent.QuickTipsChanged(it)) },
                label = "Consejos rápidos",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(
                value = uiState.weight,
                onValueChange = { viewModel.onEvent(AddEvaluationEvent.WeightChanged(it)) },
                label = "Peso (ej: 0.3)",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            NotiFastButton(
                text = if (uiState.imageUri == null) "Agregar Imagen" else "Imagen seleccionada",
                onClick = { imagePickerLauncher.launch("image/*") }
            )
            Spacer(Modifier.height(16.dp))
            if (uiState.error != null) {
                androidx.compose.material3.Text(
                    text = uiState.error,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }
            NotiFastButton(
                text = "GUARDAR EVALUACIÓN",
                onClick = { viewModel.onEvent(AddEvaluationEvent.SaveClicked) },
                isLoading = uiState.isLoading
            )
        }
    }
}
```

- [ ] **Step 4: Create EditEvaluationContract.kt**

```kotlin
package com.notifast.app.presentation.feature.editevaluation

import android.net.Uri
import com.notifast.app.data.model.Evaluation

data class EditEvaluationUiState(
    val evaluation: Evaluation? = null,
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val quickTips: String = "",
    val weight: String = "",
    val grade: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface EditEvaluationEvent {
    data class SubjectChanged(val value: String) : EditEvaluationEvent
    data class TitleChanged(val value: String) : EditEvaluationEvent
    data class DescriptionChanged(val value: String) : EditEvaluationEvent
    data class QuickTipsChanged(val value: String) : EditEvaluationEvent
    data class WeightChanged(val value: String) : EditEvaluationEvent
    data class GradeChanged(val value: String) : EditEvaluationEvent
    data class ImageSelected(val uri: Uri?) : EditEvaluationEvent
    data object SaveClicked : EditEvaluationEvent
    data object ClearError : EditEvaluationEvent
}
```

- [ ] **Step 5: Create EditEvaluationViewModel.kt**

```kotlin
package com.notifast.app.presentation.feature.editevaluation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditEvaluationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val evalId: String = checkNotNull(savedStateHandle["evalId"])

    private val _uiState = MutableStateFlow(EditEvaluationUiState())
    val uiState: StateFlow<EditEvaluationUiState> = _uiState.asStateFlow()

    init { loadEvaluation() }

    private fun loadEvaluation() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val evaluations = evaluationRepository.getEvaluations(userId).first()
            val eval = evaluations.find { it.id == evalId }
            if (eval != null) {
                _uiState.update {
                    it.copy(
                        evaluation = eval,
                        subjectName = eval.subjectName,
                        title = eval.title,
                        description = eval.description,
                        quickTips = eval.quickTips,
                        weight = eval.weight?.toString() ?: "",
                        grade = eval.grade?.toString() ?: ""
                    )
                }
            }
        }
    }

    fun onEvent(event: EditEvaluationEvent) {
        when (event) {
            is EditEvaluationEvent.SubjectChanged -> _uiState.update { it.copy(subjectName = event.value) }
            is EditEvaluationEvent.TitleChanged -> _uiState.update { it.copy(title = event.value) }
            is EditEvaluationEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is EditEvaluationEvent.QuickTipsChanged -> _uiState.update { it.copy(quickTips = event.value) }
            is EditEvaluationEvent.WeightChanged -> _uiState.update { it.copy(weight = event.value) }
            is EditEvaluationEvent.GradeChanged -> _uiState.update { it.copy(grade = event.value) }
            is EditEvaluationEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is EditEvaluationEvent.SaveClicked -> saveEvaluation()
            is EditEvaluationEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveEvaluation() {
        val state = _uiState.value
        val eval = state.evaluation ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No autenticado") }
                return@launch
            }
            val updated = eval.copy(
                subjectName = state.subjectName,
                title = state.title,
                description = state.description,
                quickTips = state.quickTips,
                weight = state.weight.toDoubleOrNull(),
                grade = state.grade.toDoubleOrNull()
            )
            val result = evaluationRepository.updateEvaluation(userId, updated, state.imageUri)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { _uiState.update { it.copy(isLoading = false, error = it.message) } }
            )
        }
    }
}
```

- [ ] **Step 6: Create EditEvaluationScreen.kt** (similar to AddEvaluationScreen but pre-populated)

```kotlin
package com.notifast.app.presentation.feature.editevaluation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEvaluationScreen(
    viewModel: EditEvaluationViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onEvaluationSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isSaved) {
        onEvaluationSaved()
        return
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onEvent(EditEvaluationEvent.ImageSelected(uri)) }

    Scaffold(
        topBar = { NotiFastTopBar(title = "Editar Evaluación", onBackClick = onBackClick) }
    ) { innerPadding ->
        if (uiState.evaluation == null) {
            NotiFastCircularLoading()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                NotiFastTextField(
                    value = uiState.subjectName,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.SubjectChanged(it)) },
                    label = "Materia",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                NotiFastTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.TitleChanged(it)) },
                    label = "Título",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                NotiFastTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.DescriptionChanged(it)) },
                    label = "Descripción",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                Spacer(Modifier.height(12.dp))
                NotiFastTextField(
                    value = uiState.quickTips,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.QuickTipsChanged(it)) },
                    label = "Consejos rápidos",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                NotiFastTextField(
                    value = uiState.weight,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.WeightChanged(it)) },
                    label = "Peso (ej: 0.3)",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                NotiFastTextField(
                    value = uiState.grade,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.GradeChanged(it)) },
                    label = "Nota (ej: 8.5)",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                NotiFastButton(
                    text = if (uiState.imageUri == null) "Cambiar Imagen" else "Imagen seleccionada",
                    onClick = { imagePickerLauncher.launch("image/*") }
                )
                Spacer(Modifier.height(16.dp))
                if (uiState.error != null) {
                    androidx.compose.material3.Text(
                        text = uiState.error,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }
                NotiFastButton(
                    text = "GUARDAR CAMBIOS",
                    onClick = { viewModel.onEvent(EditEvaluationEvent.SaveClicked) },
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}
```

- [ ] **Step 7: Wire both in NavGraph**

```kotlin
composable(Screen.AddEvaluation.route) {
    AddEvaluationScreen(
        onBackClick = { navController.popBackStack() },
        onEvaluationAdded = { navController.popBackStack() }
    )
}
composable(
    route = Screen.EditEvaluation.route,
    arguments = listOf(navArgument("evalId") { type = NavType.StringType })
) {
    EditEvaluationScreen(
        onBackClick = { navController.popBackStack() },
        onEvaluationSaved = { navController.popBackStack() }
    )
}
```

Add imports:
```kotlin
import com.notifast.app.presentation.feature.addevaluation.AddEvaluationScreen
import com.notifast.app.presentation.feature.editevaluation.EditEvaluationScreen
```

- [ ] **Step 8: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add -A && git commit -m "feat: migrate add/edit evaluation features to MVVM"
```

---

### Task 19: Clean up old files

**Files to delete:**
```
data/repository/AuthRepository.kt          (old)
data/repository/NotesRepository.kt         (old)
presentation/common/                       (entire directory — old viewmodels + old components)
presentation/ui/                           (entire directory — all old screens)
```

- [ ] **Step 1: Delete old directories**

Verify each old file is no longer referenced anywhere before deleting. Search for imports.

```bash
rm -rf app/src/main/java/com/example/notifast/presentation/common
rm -rf app/src/main/java/com/example/notifast/presentation/ui
rm -f app/src/main/java/com/example/notifast/data/repository/AuthRepository.kt
rm -f app/src/main/java/com/example/notifast/data/repository/NotesRepository.kt
```

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "chore: remove old monolithic files"
```

---

### Task 20: Final lint and verification

- [ ] **Step 1: Run lint**

```bash
./gradlew lint
```

- [ ] **Step 2: Run assembleDebug**

```bash
./gradlew assembleDebug
```

- [ ] **Step 3: Verify no warnings about deprecated or unused resources**

- [ ] **Step 4: Commit final clean-up**

```bash
git add -A && git commit -m "chore: final cleanup after MVVM SOLID migration"
```
