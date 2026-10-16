package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GlobalBackupStore
import com.notifast.app.domain.repository.GoogleAccountRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class UploadGlobalDataUseCase @Inject constructor(
    private val globalStore: GlobalBackupStore,
    private val hasher: ContentHasher,
    private val archiveCodec: BackupArchiveCodec,
    private val cloud: CloudBackupRepository,
    private val accountRepository: GoogleAccountRepository
) {

    suspend operator fun invoke(
        userId: String,
        existing: CloudFile?,
        cacheDir: File
    ): Result<Unit> {
        val account = accountRepository.account.first()
            ?: return Result.failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED))

        val snapshotResult = globalStore.read(userId)
        if (snapshotResult.isFailure) {
            return Result.failure(snapshotResult.exceptionOrNull()!!)
        }
        val snapshot = snapshotResult.getOrThrow()
        val hash = hasher.hashGlobal(snapshot)

        val tempDir = File(cacheDir, "upload_global_${System.currentTimeMillis()}").apply { mkdirs() }
        val zipFile = File(tempDir, "global.nfbak")

        return try {
            if (existing?.contentHash != null && existing.contentHash == hash) {
                return Result.success(Unit)
            }

            archiveCodec.writeGlobal(snapshot, zipFile, tempDir).getOrThrow()

            val props = mapOf(
                "kind" to "global",
                "schemaVersion" to "1",
                "contentHash" to hash,
                "uploadedAt" to System.currentTimeMillis().toString(),
                "deviceModel" to android.os.Build.MODEL
            )

            cloud.upload(
                existingFileId = existing?.driveFileId,
                fileName = "global.nfbak",
                props = props,
                file = zipFile
            ).map { }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
