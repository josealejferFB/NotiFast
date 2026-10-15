package com.notifast.app.domain.usecase.cloud

import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.domain.repository.CloudBackupRepository
import com.notifast.app.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteCloudSubjectUseCase @Inject constructor(
    private val cloud: CloudBackupRepository,
    private val stateDao: SubjectCloudStateDao,
    private val noteRepository: NoteRepository
) {
    suspend fun cloudOnly(driveFileId: String): Result<Unit> {
        val deleteResult = cloud.delete(driveFileId)
        if (deleteResult.isFailure) {
            return Result.failure(deleteResult.exceptionOrNull()!!)
        }
        return Result.success(Unit)
    }

    suspend fun localAndCloud(userId: String, subjectName: String, driveFileId: String): Result<Unit> {
        val cloudResult = cloud.delete(driveFileId)
        if (cloudResult.isFailure) {
            return Result.failure(CloudDeleteFailed)
        }
        val localResult = noteRepository.deleteSubject(userId, subjectName)
        if (localResult.isFailure) {
            return Result.failure(localResult.exceptionOrNull()!!)
        }
        return Result.success(Unit)
    }
}

object CloudDeleteFailed : Exception("Failed to delete cloud backup")
