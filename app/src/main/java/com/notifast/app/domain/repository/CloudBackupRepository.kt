package com.notifast.app.domain.repository

import com.notifast.app.domain.model.cloud.CloudFile
import java.io.File

interface CloudBackupRepository {
    suspend fun listFiles(): Result<List<CloudFile>>
    suspend fun upload(
        existingFileId: String?,
        fileName: String,
        props: Map<String, String>,
        file: File
    ): Result<String>
    suspend fun download(fileId: String, target: File): Result<Unit>
    suspend fun delete(fileId: String): Result<Unit>
}
