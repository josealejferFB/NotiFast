package com.notifast.app.domain.repository

import com.notifast.app.domain.model.study.CoverDraft

interface CoverBackupStore {
    suspend fun read(userId: String): Result<List<CoverDraft>>
    suspend fun apply(userId: String, drafts: List<CoverDraft>): Result<Unit>
}
