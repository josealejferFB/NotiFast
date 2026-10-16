package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GlobalBackupStore
import java.io.File
import javax.inject.Inject

class RestoreGlobalDataUseCase @Inject constructor(
    private val globalStore: GlobalBackupStore,
    private val archiveCodec: BackupArchiveCodec,
    private val cloud: CloudBackupRepository
) {

    suspend operator fun invoke(userId: String, file: CloudFile, cacheDir: File): Result<Unit> {
        val tempDir = File(cacheDir, "restore_global_${System.currentTimeMillis()}").apply { mkdirs() }
        val targetZip = File(tempDir, "global.nfbak")

        return try {
            cloud.download(file.driveFileId, targetZip).getOrThrow()
            val snapshot = archiveCodec.readGlobal(targetZip, tempDir).getOrThrow()
            globalStore.apply(userId, snapshot).getOrThrow()
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
