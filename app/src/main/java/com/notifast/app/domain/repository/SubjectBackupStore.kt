package com.notifast.app.domain.repository

import com.notifast.app.data.backup.SubjectSnapshot

interface SubjectBackupStore {
    suspend fun readSnapshot(userId: String, subjectId: String): Result<SubjectSnapshot>
    suspend fun listSubjectIds(userId: String): List<Pair<String, String>>
    suspend fun insertSnapshot(
        userId: String,
        snapshot: SubjectSnapshot,
        newSubjectId: String? = null,
        newName: String? = null
    ): Result<String>
    suspend fun replaceSnapshot(
        userId: String,
        oldSubjectId: String,
        snapshot: SubjectSnapshot
    ): Result<String>
}
