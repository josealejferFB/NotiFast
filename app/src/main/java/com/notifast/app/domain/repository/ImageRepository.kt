package com.notifast.app.domain.repository

import android.net.Uri

interface ImageRepository {
    suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String>
    suspend fun uploadNoteImage(uid: String, uri: Uri): Result<String>
    suspend fun deleteImage(url: String): Result<Unit>
}
