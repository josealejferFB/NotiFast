package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.CoverBackupStore
import com.notifast.app.domain.repository.GoogleAccountRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class UploadCoversUseCase @Inject constructor(
    private val coverStore: CoverBackupStore,
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

        val draftsResult = coverStore.read(userId)
        if (draftsResult.isFailure) {
            return Result.failure(draftsResult.exceptionOrNull()!!)
        }
        val drafts = draftsResult.getOrThrow()
        val hash = hasher.hashCovers(drafts)

        if (existing?.contentHash != null && existing.contentHash == hash) {
            return Result.success(Unit)
        }

        val tempDir = File(cacheDir, "upload_covers_${System.currentTimeMillis()}").apply { mkdirs() }
        val zipFile = File(tempDir, "covers.nfbak")

        return try {
            archiveCodec.writeCovers(drafts, zipFile, tempDir).getOrThrow()

            val props = mapOf(
                "kind" to "covers",
                "schemaVersion" to "1",
                "contentHash" to hash,
                "coverCount" to drafts.size.toString(),
                "uploadedAt" to System.currentTimeMillis().toString(),
                "deviceModel" to (android.os.Build.MODEL ?: "Android")
            )

            cloud.upload(
                existingFileId = existing?.driveFileId,
                fileName = "covers.nfbak",
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
