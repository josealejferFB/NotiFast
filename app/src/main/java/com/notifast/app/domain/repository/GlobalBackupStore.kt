package com.notifast.app.domain.repository

import com.notifast.app.data.backup.GlobalSnapshot

interface GlobalBackupStore {
    suspend fun read(userId: String): Result<GlobalSnapshot>
    suspend fun apply(userId: String, snapshot: GlobalSnapshot): Result<Unit>
}
