package com.notifast.app.data.local.backup

import com.notifast.app.data.local.dao.CoverDraftDao
import com.notifast.app.data.local.entity.CoverDraftEntity
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.repository.CoverBackupStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCoverBackupStore @Inject constructor(
    private val coverDraftDao: CoverDraftDao
) : CoverBackupStore {

    override suspend fun read(userId: String): Result<List<CoverDraft>> = try {
        val entities = coverDraftDao.observeDrafts(userId).first()
        Result.success(entities.map { it.toDomain() })
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun apply(userId: String, drafts: List<CoverDraft>): Result<Unit> = try {
        drafts.forEach { draft ->
            val entity = CoverDraftEntity.fromDomain(draft.copy(userId = userId))
            coverDraftDao.upsertDraft(entity)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
