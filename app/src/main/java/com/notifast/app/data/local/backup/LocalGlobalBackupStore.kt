package com.notifast.app.data.local.backup

import android.net.Uri
import com.notifast.app.data.backup.GlobalSnapshot
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.GlobalBackupStore
import com.notifast.app.domain.repository.TeacherRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalGlobalBackupStore @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val teacherRepository: TeacherRepository
) : GlobalBackupStore {

    override suspend fun read(userId: String): Result<GlobalSnapshot> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            val user = userResult.getOrNull()

            val avatarFile = user?.profilePictureUrl?.let { url ->
                val rawPath = if (url.startsWith("file://")) url.removePrefix("file://") else url
                val f = File(rawPath)
                if (f.exists() && f.isFile) f else null
            }

            val prefs = userPrefsRepository.exportAll()
            val teachers = teacherRepository.getTeachers(userId).first()

            Result.success(
                GlobalSnapshot(
                    firstName = user?.firstName ?: "",
                    lastName = user?.lastName ?: "",
                    avatar = avatarFile,
                    prefs = prefs,
                    teachers = teachers
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun apply(userId: String, snapshot: GlobalSnapshot): Result<Unit> {
        return try {
            // 1. Update Profile (avatar and names)
            val avatarUri = snapshot.avatar?.takeIf { it.exists() }?.let { Uri.fromFile(it) }
            val profileResult = authRepository.updateProfile(
                uid = userId,
                firstName = snapshot.firstName,
                lastName = snapshot.lastName,
                profileImageUri = avatarUri
            )
            if (profileResult.isFailure) {
                return profileResult
            }

            // 2. Import preferences
            if (snapshot.prefs.isNotEmpty()) {
                val prefsResult = userPrefsRepository.importAll(snapshot.prefs)
                if (prefsResult.isFailure) {
                    return prefsResult
                }
            }

            // 3. Merge teachers
            val localTeachers = teacherRepository.getTeachers(userId).first()
            fun teacherKey(t: Teacher): String {
                val f = t.firstName.trim().lowercase()
                val l = t.lastName.trim().lowercase()
                return "${f}_${l}"
            }

            val localByKey = localTeachers.associateBy { teacherKey(it) }

            for (remote in snapshot.teachers) {
                val key = teacherKey(remote)
                val local = localByKey[key]
                if (local == null) {
                    // New teacher
                    val newTeacher = remote.copy(
                        userId = userId,
                        id = UUID.randomUUID().toString()
                    )
                    teacherRepository.saveTeacher(newTeacher)
                } else {
                    // Match: keep local fields, merge subjectNames (local U remote, deduped ignoring case)
                    val mergedSubjects = mutableListOf<String>()
                    val seen = mutableSetOf<String>()
                    for (name in local.subjectNames + remote.subjectNames) {
                        val trimmed = name.trim()
                        val lower = trimmed.lowercase()
                        if (lower.isNotBlank() && seen.add(lower)) {
                            mergedSubjects.add(trimmed)
                        }
                    }
                    val updatedTeacher = local.copy(
                        userId = userId,
                        subjectNames = mergedSubjects
                    )
                    teacherRepository.saveTeacher(updatedTeacher)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
