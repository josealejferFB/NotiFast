package com.notifast.app.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.notifast.app.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> {
        return saveImageLocally("images/profiles/${uid}_${UUID.randomUUID()}.jpg", uri)
    }

    override suspend fun uploadNoteImage(uid: String, uri: Uri): Result<String> {
        return saveImageLocally("images/notes/${uid}_${UUID.randomUUID()}.jpg", uri)
    }

    override suspend fun deleteImage(url: String): Result<Unit> {
        return try {
            val file = if (url.startsWith("file://")) {
                File(Uri.parse(url).path!!)
            } else {
                File(url)
            }
            if (file.exists()) file.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveImageLocally(relativePath: String, uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open image URI"))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return Result.failure(Exception("Cannot decode image"))

            val outputFile = File(context.filesDir, relativePath)
            outputFile.parentFile?.mkdirs()

            FileOutputStream(outputFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
            }

            Result.success(Uri.fromFile(outputFile).toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
