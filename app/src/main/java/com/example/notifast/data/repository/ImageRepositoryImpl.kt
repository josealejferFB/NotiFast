package com.example.notifast.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.notifast.domain.repository.ImageRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> {
        return uploadImage("users/$uid/profile/${UUID.randomUUID()}.jpg", uri)
    }

    override suspend fun uploadNoteImage(uid: String, uri: Uri): Result<String> {
        return uploadImage("users/$uid/notes/${UUID.randomUUID()}.jpg", uri)
    }

    override suspend fun deleteImage(url: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(url).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImage(path: String, uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open image URI"))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return Result.failure(Exception("Cannot decode image"))

            val tempFile = java.io.File(context.cacheDir, "upload_${UUID.randomUUID()}.jpg")
            java.io.FileOutputStream(tempFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
            }

            val uploadTask = storage.reference.child(path).putFile(Uri.fromFile(tempFile))
            uploadTask.await()

            val downloadUrl = storage.reference.child(path).downloadUrl.await()
            tempFile.delete()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
