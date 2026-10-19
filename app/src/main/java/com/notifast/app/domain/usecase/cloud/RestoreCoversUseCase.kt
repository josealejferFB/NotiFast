package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.CoverBackupStore
import java.io.File
import javax.inject.Inject

class RestoreCoversUseCase @Inject constructor(
    private val coverStore: CoverBackupStore,
    private val archiveCodec: BackupArchiveCodec,
    private val cloud: CloudBackupRepository
) {

    suspend operator fun invoke(userId: String, file: CloudFile, cacheDir: File): Result<Unit> {
        val tempDir = File(cacheDir, "restore_covers_${System.currentTimeMillis()}").apply { mkdirs() }
        val targetZip = File(tempDir, "covers.nfbak")

        return try {
            cloud.download(file.driveFileId, targetZip).getOrThrow()
            val drafts = archiveCodec.readCovers(targetZip, tempDir).getOrThrow()
            coverStore.apply(userId, drafts).getOrThrow()
            Result.success(Unit)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
