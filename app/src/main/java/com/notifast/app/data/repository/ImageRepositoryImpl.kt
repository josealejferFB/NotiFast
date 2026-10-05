package com.notifast.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.notifast.app.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ImageRepositoryImpl"

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> {
        val fileName = "${uid}_${UUID.randomUUID()}.jpg"
        val relativePath = "images/profiles/$fileName"
        val result = saveImageLocally(relativePath, uri)

        if (result.isSuccess) {
            cleanUpOldProfileImages(uid, fileName)
        }
        return result
    }

    override suspend fun uploadNoteImage(uid: String, uri: Uri): Result<String> {
        val fileName = "${uid}_${UUID.randomUUID()}.jpg"
        return saveImageLocally("images/notes/$fileName", uri)
    }

    override suspend fun deleteImage(url: String): Result<Unit> {
        if (url.isBlank()) return Result.success(Unit)
        return try {
            val path = if (url.startsWith("file://")) {
                url.removePrefix("file://")
            } else {
                url
            }
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete image: $url", e)
            Result.success(Unit)
        }
    }

    private fun cleanUpOldProfileImages(uid: String, currentFileName: String) {
        try {
            val profileDir = File(context.filesDir, "images/profiles")
            if (profileDir.exists() && profileDir.isDirectory) {
                profileDir.listFiles { file ->
                    file.name.startsWith("${uid}_") && file.name != currentFileName
                }?.forEach { oldFile ->
                    try {
                        oldFile.delete()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete old profile image ${oldFile.name}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cleanup old profile images for user $uid", e)
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            Result.success(Uri.fromFile(outputFile).toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save image locally to $relativePath", e)
            Result.failure(e)
        }
    }
}
