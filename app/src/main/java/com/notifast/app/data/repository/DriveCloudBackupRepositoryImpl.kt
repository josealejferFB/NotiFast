package com.notifast.app.data.repository

import android.util.Log
import com.notifast.app.data.remote.drive.DriveException
import com.notifast.app.data.remote.drive.DriveRestClient
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveCloudBackupRepositoryImpl @Inject constructor(
    private val client: DriveRestClient,
    private val accountRepository: GoogleAccountRepository
) : CloudBackupRepository {

    override suspend fun listFiles(): Result<List<CloudFile>> = withToken { token ->
        val dtoList = client.list(token)
        val cloudFiles = mutableListOf<CloudFile>()

        for (dto in dtoList) {
            val props = dto.appProperties
            val kind = props["kind"]
            if (kind != KIND_SUBJECT && kind != KIND_GLOBAL && kind != KIND_COVERS) {
                Log.w(TAG, "Skipping Drive file ${dto.id} with unknown kind: $kind")
                continue
            }

            val contentHash = props["contentHash"]
            val schemaVersionStr = props["schemaVersion"]
            val schemaVersion = schemaVersionStr?.toIntOrNull()
            if (contentHash.isNullOrBlank() || schemaVersion == null) {
                Log.w(TAG, "Skipping Drive file ${dto.id} with missing/invalid hash or schemaVersion")
                continue
            }

            val uploadedAt = props["uploadedAt"]?.toLongOrNull() ?: 0L
            val deviceModel = props["deviceModel"].orEmpty()
            val sizeBytes = dto.size ?: 0L

            when (kind) {
                KIND_SUBJECT -> {
                    val subjectId = props["subjectId"]
                    val subjectName = props["subjectName"]
                    val icon = props["icon"]
                    val colorHex = props["colorHex"]
                    val noteCount = props["noteCount"]?.toIntOrNull() ?: 0
                    val evaluationCount = props["evaluationCount"]?.toIntOrNull() ?: 0
                    val teacherCount = props["teacherCount"]?.toIntOrNull() ?: 0
                    val section = props["section"]

                    cloudFiles.add(
                        CloudFile(
                            driveFileId = dto.id,
                            kind = kind,
                            subjectId = subjectId,
                            subjectName = subjectName,
                            icon = icon,
                            colorHex = colorHex,
                            contentHash = contentHash,
                            uploadedAt = uploadedAt,
                            deviceModel = deviceModel,
                            noteCount = noteCount,
                            evaluationCount = evaluationCount,
                            teacherCount = teacherCount,
                            section = section,
                            sizeBytes = sizeBytes,
                            schemaVersion = schemaVersion
                        )
                    )
                }
                KIND_COVERS -> {
                    val coverCount = props["coverCount"]?.toIntOrNull() ?: 0
                    cloudFiles.add(
                        CloudFile(
                            driveFileId = dto.id,
                            kind = kind,
                            subjectId = null,
                            subjectName = "Portadas de Estudio",
                            icon = "📄",
                            colorHex = null,
                            contentHash = contentHash,
                            uploadedAt = uploadedAt,
                            deviceModel = deviceModel,
                            noteCount = 0,
                            evaluationCount = 0,
                            coverCount = coverCount,
                            teacherCount = 0,
                            sizeBytes = sizeBytes,
                            schemaVersion = schemaVersion
                        )
                    )
                }
                else -> {
                    cloudFiles.add(
                        CloudFile(
                            driveFileId = dto.id,
                            kind = kind,
                            subjectId = null,
                            subjectName = null,
                            icon = null,
                            colorHex = null,
                            contentHash = contentHash,
                            uploadedAt = uploadedAt,
                            deviceModel = deviceModel,
                            noteCount = 0,
                            evaluationCount = 0,
                            sizeBytes = sizeBytes,
                            schemaVersion = schemaVersion
                        )
                    )
                }
            }
        }
        cloudFiles
    }

    override suspend fun upload(
        existingFileId: String?,
        fileName: String,
        props: Map<String, String>,
        file: File
    ): Result<String> = withToken { token ->
        if (existingFileId != null) {
            try {
                val updated = client.update(token, existingFileId, props, file)
                updated.id
            } catch (e: DriveException) {
                if (e.code == 404) {
                    val created = client.create(token, fileName, props, file)
                    created.id
                } else {
                    throw e
                }
            }
        } else {
            val created = client.create(token, fileName, props, file)
            created.id
        }
    }

    override suspend fun download(fileId: String, target: File): Result<Unit> = withToken { token ->
        client.download(token, fileId, target)
    }

    override suspend fun delete(fileId: String): Result<Unit> = withToken { token ->
        client.delete(token, fileId)
    }

    private suspend fun <T> withToken(block: suspend (token: String) -> T): Result<T> {
        val initialToken = accountRepository.accessToken(forceRefresh = false).getOrElse { error ->
            return Result.failure(error)
        }

        return try {
            Result.success(block(initialToken))
        } catch (e: DriveException) {
            if (e.code == 401) {
                val refreshedToken = accountRepository.accessToken(forceRefresh = true).getOrElse {
                    return Result.failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED, it))
                }
                try {
                    Result.success(block(refreshedToken))
                } catch (e2: DriveException) {
                    if (e2.code == 401) {
                        Result.failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED, e2))
                    } else {
                        Result.failure(e2)
                    }
                } catch (e2: Exception) {
                    Result.failure(e2)
                }
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "DriveCloudBackupRepo"
        const val KIND_SUBJECT = "subject"
        const val KIND_GLOBAL = "global"
        const val KIND_COVERS = "covers"
    }
}
