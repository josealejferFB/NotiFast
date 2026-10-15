package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.domain.model.cloud.LocalSyncSummary
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveLocalSyncSummaryUseCase @Inject constructor(
    private val accountRepository: GoogleAccountRepository,
    private val stateDao: SubjectCloudStateDao,
    private val store: SubjectBackupStore,
    private val hasher: ContentHasher
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(userId: String): Flow<LocalSyncSummary> {
        return accountRepository.account.flatMapLatest { account ->
            if (account == null) {
                flowOf(LocalSyncSummary.NO_ACCOUNT)
            } else {
                stateDao.observe(account.accountId).map { states ->
                    val localIds = store.listSubjectIds(userId)
                    if (localIds.isEmpty()) {
                        return@map LocalSyncSummary.ALL_SYNCED
                    }
                    val stateMap = states.associateBy { it.subjectId }
                    var allSynced = true
                    for ((subjectId, _) in localIds) {
                        val state = stateMap[subjectId]
                        if (state == null) {
                            allSynced = false
                            break
                        }
                        val snapshot = store.readSnapshot(userId, subjectId).getOrNull()
                        val currentHash = snapshot?.let { hasher.hash(it) }
                        if (currentHash == null || currentHash != state.lastUploadedHash) {
                            allSynced = false
                            break
                        }
                    }
                    if (allSynced) LocalSyncSummary.ALL_SYNCED else LocalSyncSummary.PENDING
                }
            }
        }
    }
}
