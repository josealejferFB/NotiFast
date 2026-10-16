package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudOverview
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BuildCloudOverviewUseCase @Inject constructor(
    private val store: SubjectBackupStore,
    private val hasher: ContentHasher,
    private val cloud: CloudBackupRepository,
    private val stateDao: SubjectCloudStateDao,
    private val accountRepository: GoogleAccountRepository
) {

    suspend operator fun invoke(userId: String): Result<CloudOverview> {
        return try {
            val account = accountRepository.account.first()
            val cloudFilesResult = cloud.listFiles()
            if (cloudFilesResult.isFailure) {
                return Result.failure(cloudFilesResult.exceptionOrNull()!!)
            }
            val cloudFiles = cloudFilesResult.getOrThrow()

            val cloudSubjects = cloudFiles.filter { it.kind == "subject" && !it.subjectId.isNullOrBlank() }
            val globalFile = cloudFiles.firstOrNull { it.kind == "global" }

            val states = account?.let { stateDao.getAll(it.accountId) } ?: emptyList()
            val stateBySubjectId = states.associateBy { it.subjectId }

            val localSubjectIds = store.listSubjectIds(userId)
            val localSnapshots = mutableMapOf<String, SubjectSnapshot>()
            val localHashes = mutableMapOf<String, String>()

            for ((subjectId, _) in localSubjectIds) {
                val snap = store.readSnapshot(userId, subjectId).getOrNull()
                if (snap != null) {
                    localSnapshots[subjectId] = snap
                    localHashes[subjectId] = hasher.hash(snap)
                }
            }

            val cloudBySubjectId = cloudSubjects.associateBy { it.subjectId!! }
            val allSubjectIds = (localSubjectIds.map { it.first } + cloudBySubjectId.keys).distinct()

            val items = mutableListOf<CloudSubjectItem>()
            for (subjectId in allSubjectIds) {
                val localSnapshot = localSnapshots[subjectId]
                val localHash = localHashes[subjectId]
                val cloudFile = cloudBySubjectId[subjectId]
                val state = stateBySubjectId[subjectId]

                val status = classify(localHash, cloudFile, state)

                val name = localSnapshot?.subject?.name
                    ?: cloudFile?.subjectName
                    ?: localSubjectIds.firstOrNull { it.first == subjectId }?.second
                    ?: ""
                val icon = localSnapshot?.subject?.icon ?: cloudFile?.icon ?: "📚"
                val colorHex = localSnapshot?.subject?.colorHex ?: cloudFile?.colorHex ?: "#BF092F"
                val driveFileId = cloudFile?.driveFileId ?: state?.driveFileId

                val noteCount = localSnapshot?.notes?.size ?: cloudFile?.noteCount ?: 0
                val evaluationCount = localSnapshot?.evaluations?.size ?: cloudFile?.evaluationCount ?: 0
                val scheduleCount = localSnapshot?.schedules?.size ?: 0
                val sizeBytes = cloudFile?.sizeBytes ?: 0L
                val lastSyncedAt = cloudFile?.uploadedAt

                items.add(
                    CloudSubjectItem(
                        subjectId = subjectId,
                        name = name,
                        icon = icon,
                        colorHex = colorHex,
                        status = status,
                        driveFileId = driveFileId,
                        localHash = localHash,
                        cloudFile = cloudFile,
                        noteCount = noteCount,
                        evaluationCount = evaluationCount,
                        scheduleCount = scheduleCount,
                        sizeBytes = sizeBytes,
                        lastSyncedAt = lastSyncedAt
                    )
                )
            }

            val collator = java.text.Collator.getInstance()
            val sortedItems = items.sortedWith { a, b -> collator.compare(a.name, b.name) }
            Result.success(CloudOverview(subjects = sortedItems, global = globalFile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    internal fun classify(
        localHash: String?,
        cloud: CloudFile?,
        state: SubjectCloudStateEntity?
    ): CloudSubjectStatus {
        val isLocal = localHash != null
        val isCloud = cloud != null

        if (isLocal && !isCloud) {
            return CloudSubjectStatus.LOCAL_ONLY
        }
        if (!isLocal && isCloud) {
            return CloudSubjectStatus.CLOUD_ONLY
        }
        if (!isLocal && !isCloud) {
            return CloudSubjectStatus.LOCAL_ONLY
        }

        // Both local and cloud are present
        if (state != null && state.driveFileId == cloud!!.driveFileId) {
            val localMatchesSaved = localHash == state.lastUploadedHash
            val cloudMatchesSaved = cloud.contentHash == state.lastUploadedHash

            return when {
                localMatchesSaved && cloudMatchesSaved -> CloudSubjectStatus.SYNCED
                localHash == cloud.contentHash -> CloudSubjectStatus.SYNCED
                !localMatchesSaved && cloudMatchesSaved -> CloudSubjectStatus.LOCAL_CHANGES
                localMatchesSaved && !cloudMatchesSaved -> CloudSubjectStatus.CLOUD_NEWER
                else -> CloudSubjectStatus.BOTH_UNLINKED
            }
        } else {
            return if (localHash == cloud!!.contentHash) {
                CloudSubjectStatus.SYNCED
            } else {
                CloudSubjectStatus.BOTH_UNLINKED
            }
        }
    }
}
