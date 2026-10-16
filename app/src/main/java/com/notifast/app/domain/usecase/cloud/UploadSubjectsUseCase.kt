package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.ItemOutcome
import com.notifast.app.domain.model.cloud.UploadProgress
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class UploadSubjectsUseCase @Inject constructor(
    private val store: SubjectBackupStore,
    private val hasher: ContentHasher,
    private val archiveCodec: BackupArchiveCodec,
    private val cloud: CloudBackupRepository,
    private val stateDao: SubjectCloudStateDao,
    private val accountRepository: GoogleAccountRepository
) {

    operator fun invoke(
        userId: String,
        items: List<CloudSubjectItem>,
        cacheDir: File
    ): Flow<UploadProgress> = flow {
        val account = accountRepository.account.first()
            ?: throw CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED)

        val total = items.size
        val outcomes = mutableListOf<ItemOutcome>()
        val uploadBaseDir = File(cacheDir, "upload_${System.currentTimeMillis()}").apply { mkdirs() }

        try {
            items.forEachIndexed { index, item ->
                emit(UploadProgress.Progress(done = index + 1, total = total, current = item.name))

                val itemDir = File(uploadBaseDir, item.subjectId).apply { mkdirs() }
                try {
                    val snapshotResult = store.readSnapshot(userId, item.subjectId)
                    val snapshot = snapshotResult.getOrThrow()
                    val hash = hasher.hash(snapshot)

                    val cloudHash = item.cloudFile?.contentHash
                    val targetDriveId = item.driveFileId ?: item.cloudFile?.driveFileId

                    // Detección inteligente de contenido idéntico: No-Op sin red
                    if (cloudHash != null && cloudHash == hash && targetDriveId != null) {
                        stateDao.upsert(
                            SubjectCloudStateEntity(
                                accountId = account.accountId,
                                subjectId = item.subjectId,
                                driveFileId = targetDriveId,
                                lastUploadedHash = hash,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        )
                        outcomes.add(ItemOutcome(id = item.subjectId, name = item.name, error = null, isNoOp = true))
                        return@forEachIndexed
                    }

                    val zipFile = File(itemDir, "subject_${item.subjectId}.nfbak")
                    archiveCodec.writeSubject(snapshot, zipFile, itemDir).getOrThrow()

                    val props = mapOf(
                        "kind" to "subject",
                        "subjectId" to item.subjectId,
                        "subjectName" to snapshot.subject.name,
                        "icon" to snapshot.subject.icon,
                        "colorHex" to snapshot.subject.colorHex,
                        "schemaVersion" to "1",
                        "contentHash" to hash,
                        "uploadedAt" to System.currentTimeMillis().toString(),
                        "deviceModel" to android.os.Build.MODEL,
                        "noteCount" to snapshot.notes.size.toString(),
                        "evaluationCount" to snapshot.evaluations.size.toString()
                    )

                    val driveFileId = cloud.upload(
                        existingFileId = targetDriveId,
                        fileName = "subject_${item.subjectId}.nfbak",
                        props = props,
                        file = zipFile
                    ).getOrThrow()

                    stateDao.upsert(
                        SubjectCloudStateEntity(
                            accountId = account.accountId,
                            subjectId = item.subjectId,
                            driveFileId = driveFileId,
                            lastUploadedHash = hash,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                    )

                    outcomes.add(ItemOutcome(id = item.subjectId, name = item.name, error = null))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    outcomes.add(ItemOutcome(id = item.subjectId, name = item.name, error = e))
                } finally {
                    itemDir.deleteRecursively()
                }
            }

            emit(UploadProgress.Done(BatchResult(outcomes)))
        } finally {
            uploadBaseDir.deleteRecursively()
        }
    }

    suspend fun estimateBytes(userId: String, items: List<CloudSubjectItem>): Long {
        var totalBytes = 0L
        for (item in items) {
            val snapshot = store.readSnapshot(userId, item.subjectId).getOrNull() ?: continue
            totalBytes += 50_000L
            for (file in snapshot.imageFiles.values) {
                if (file.exists() && file.isFile) {
                    totalBytes += file.length().coerceAtMost(300_000L)
                }
            }
        }
        return totalBytes
    }
}
