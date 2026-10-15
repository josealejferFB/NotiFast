package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.BackupArchiveCodec
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.data.model.package_data.SubjectPackage
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.ItemOutcome
import com.notifast.app.domain.model.cloud.RestoreConflict
import com.notifast.app.domain.model.cloud.RestoreDecision
import com.notifast.app.domain.model.cloud.UploadProgress
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase
import com.notifast.app.domain.usecase.ResolveSubjectNameUseCase
import com.notifast.app.domain.usecase.validatePackage
import com.notifast.app.domain.util.NameValidation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.UUID
import javax.inject.Inject

class RestoreSubjectsUseCase @Inject constructor(
    private val store: SubjectBackupStore,
    private val hasher: ContentHasher,
    private val archiveCodec: BackupArchiveCodec,
    private val cloud: CloudBackupRepository,
    private val stateDao: SubjectCloudStateDao,
    private val accountRepository: GoogleAccountRepository,
    private val resolveSubjectNameUseCase: ResolveSubjectNameUseCase,
    private val rescheduleAllAlertsUseCase: RescheduleAllAlertsUseCase
) {

    suspend fun findConflicts(userId: String, items: List<CloudSubjectItem>): List<RestoreConflict> {
        val account = accountRepository.account.first()
        val states = account?.let { stateDao.getAll(it.accountId) } ?: emptyList()
        val stateMap = states.associateBy { it.subjectId }

        val localSubjects = store.listSubjectIds(userId)
        val localById = localSubjects.associateBy { it.first }
        val localByName = localSubjects.associateBy { it.second.trim().lowercase() }

        val existingNames = localSubjects.map { it.second }.toMutableList()
        val seenBatchNames = mutableSetOf<String>()
        val seenBatchOriginalNames = mutableMapOf<String, String>()
        val conflicts = mutableListOf<RestoreConflict>()

        for (item in items) {
            val trimmedName = item.name.trim().lowercase()
            val localMatchById = localById[item.subjectId]
            val localMatchByName = localByName[trimmedName]
            val batchCollision = seenBatchNames.contains(trimmedName)

            if (localMatchById != null || localMatchByName != null || batchCollision) {
                val localMatch = localMatchById ?: localMatchByName
                val localSubjectId = localMatch?.first ?: item.subjectId
                val localName = localMatch?.second ?: seenBatchOriginalNames[trimmedName] ?: item.name

                val state = stateMap[localSubjectId]
                val hasLocalChanges = if (localMatch != null) {
                    val localSnap = store.readSnapshot(userId, localSubjectId).getOrNull()
                    val currentHash = localSnap?.let { hasher.hash(it) }
                    state == null || currentHash != state.lastUploadedHash
                } else {
                    false
                }

                val suggested = resolveSubjectNameUseCase.suggest(localName, existingNames)
                existingNames.add(suggested)
                conflicts.add(
                    RestoreConflict(
                        item = item,
                        localSubjectId = localSubjectId,
                        localName = localName,
                        hasLocalChanges = hasLocalChanges,
                        suggestedName = suggested
                    )
                )
            } else {
                seenBatchNames.add(trimmedName)
                seenBatchOriginalNames[trimmedName] = item.name.trim()
                existingNames.add(item.name)
            }
        }
        return conflicts
    }

    operator fun invoke(
        userId: String,
        items: List<CloudSubjectItem>,
        decisions: Map<String, RestoreDecision>,
        cacheDir: File
    ): Flow<UploadProgress> = flow {
        val account = accountRepository.account.first()
            ?: throw CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED)

        val total = items.size
        val outcomes = mutableListOf<ItemOutcome>()
        val restoreBaseDir = File(cacheDir, "restore_${System.currentTimeMillis()}").apply { mkdirs() }

        try {
            items.forEachIndexed { index, item ->
                emit(UploadProgress.Progress(done = index + 1, total = total, current = item.name))

                val decision = decisions[item.subjectId]
                if (decision == RestoreDecision.Skip) {
                    outcomes.add(ItemOutcome(id = item.subjectId, name = item.name, error = null))
                    return@forEachIndexed
                }

                val itemDir = File(restoreBaseDir, item.subjectId).apply { mkdirs() }
                val targetZip = File(itemDir, "downloaded.nfbak")

                try {
                    val driveFileId = item.driveFileId
                        ?: throw IllegalArgumentException("Falta identificador en la nube para ${item.name}")

                    cloud.download(driveFileId, targetZip).getOrThrow()

                    val snapshotResult = archiveCodec.readSubject(targetZip, itemDir)
                    val snapshot = snapshotResult.getOrThrow()

                    val pkg = SubjectPackage(
                        schemaVersion = 1,
                        subject = snapshot.subject,
                        schedules = snapshot.schedules,
                        evaluations = snapshot.evaluations,
                        notes = snapshot.notes
                    )
                    validatePackage(pkg).getOrThrow()

                    when (decision) {
                        is RestoreDecision.Overwrite -> {
                            val locals = store.listSubjectIds(userId)
                            val targetLocalId = locals.firstOrNull { it.first == item.subjectId }?.first
                                ?: locals.firstOrNull { it.second.equals(item.name, ignoreCase = true) }?.first
                                ?: item.subjectId

                            store.replaceSnapshot(userId, targetLocalId, snapshot).getOrThrow()

                            val recomputedSnap = store.readSnapshot(userId, snapshot.subject.id).getOrNull()
                            val recomputedHash = recomputedSnap?.let { hasher.hash(it) } ?: item.cloudFile?.contentHash.orEmpty()

                            stateDao.upsert(
                                SubjectCloudStateEntity(
                                    accountId = account.accountId,
                                    subjectId = snapshot.subject.id,
                                    driveFileId = driveFileId,
                                    lastUploadedHash = recomputedHash,
                                    lastSyncedAt = System.currentTimeMillis()
                                )
                            )
                        }
                        is RestoreDecision.AddAs -> {
                            val existingNames = store.listSubjectIds(userId).map { it.second }
                            val validation = resolveSubjectNameUseCase.validate(decision.name, existingNames)
                            if (validation != NameValidation.Valid) {
                                throw IllegalArgumentException("Nombre de materia inválido: $validation")
                            }

                            val newSubjectId = UUID.randomUUID().toString()
                            store.insertSnapshot(
                                userId = userId,
                                snapshot = snapshot,
                                newSubjectId = newSubjectId,
                                newName = decision.name.trim()
                            ).getOrThrow()
                        }
                        RestoreDecision.Skip -> {
                            // Handled above
                        }
                        null -> {
                            store.insertSnapshot(userId, snapshot).getOrThrow()

                            val recomputedSnap = store.readSnapshot(userId, snapshot.subject.id).getOrNull()
                            val recomputedHash = recomputedSnap?.let { hasher.hash(it) } ?: item.cloudFile?.contentHash.orEmpty()

                            stateDao.upsert(
                                SubjectCloudStateEntity(
                                    accountId = account.accountId,
                                    subjectId = snapshot.subject.id,
                                    driveFileId = driveFileId,
                                    lastUploadedHash = recomputedHash,
                                    lastSyncedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }

                    outcomes.add(ItemOutcome(id = item.subjectId, name = item.name, error = null))
                } catch (e: Exception) {
                    outcomes.add(ItemOutcome(id = item.subjectId, name = item.name, error = e))
                } finally {
                    itemDir.deleteRecursively()
                }
            }

            try {
                rescheduleAllAlertsUseCase()
            } catch (_: Exception) {}

            emit(UploadProgress.Done(BatchResult(outcomes)))
        } finally {
            restoreBaseDir.deleteRecursively()
        }
    }
}
