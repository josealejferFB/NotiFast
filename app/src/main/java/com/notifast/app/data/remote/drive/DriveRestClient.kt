package com.notifast.app.data.remote.drive

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRestClient internal constructor(
    private val http: OkHttpClient,
    private val gson: Gson,
    private val baseUrl: HttpUrl,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Inject
    constructor(http: OkHttpClient, gson: Gson) : this(
        http = http,
        gson = gson,
        baseUrl = "https://www.googleapis.com/".toHttpUrl(),
        ioDispatcher = Dispatchers.IO
    )

    suspend fun list(token: String): List<DriveFileDto> = withContext(ioDispatcher) {
        val allFiles = mutableListOf<DriveFileDto>()
        var pageToken: String? = null

        do {
            val urlBuilder = baseUrl.newBuilder()
                .addPathSegments("drive/v3/files")
                .addQueryParameter("spaces", "appDataFolder")
                .addQueryParameter("fields", "nextPageToken,files(id,name,size,modifiedTime,appProperties)")
                .addQueryParameter("pageSize", "1000")

            if (!pageToken.isNullOrEmpty()) {
                urlBuilder.addQueryParameter("pageToken", pageToken)
            }

            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = executeRequest(request)
            val bodyString = response.body?.string().orEmpty()
            val listResponse = gson.fromJson(bodyString, DriveFileListResponse::class.java)

            listResponse?.files?.let { allFiles.addAll(it) }
            pageToken = listResponse?.nextPageToken
        } while (!pageToken.isNullOrEmpty())

        allFiles
    }

    suspend fun create(
        token: String,
        name: String,
        props: Map<String, String>,
        file: File
    ): DriveFileDto = withContext(ioDispatcher) {
        val url = baseUrl.newBuilder()
            .addPathSegments("upload/drive/v3/files")
            .addQueryParameter("uploadType", "multipart")
            .addQueryParameter("fields", "id,name,size,modifiedTime,appProperties")
            .build()

        val metadata = mapOf(
            "name" to name,
            "parents" to listOf("appDataFolder"),
            "appProperties" to props
        )
        val metadataJson = gson.toJson(metadata)

        val multipartBody = MultipartBody.Builder()
            .setType(MULTIPART_RELATED)
            .addPart(metadataJson.toRequestBody(MEDIA_TYPE_JSON))
            .addPart(file.asRequestBody(MEDIA_TYPE_OCTET_STREAM))
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .post(multipartBody)
            .build()

        val response = executeRequest(request)
        val bodyString = response.body?.string().orEmpty()
        gson.fromJson(bodyString, DriveFileDto::class.java)
    }

    suspend fun createGoogleDocFromHtml(
        token: String,
        title: String,
        htmlContent: String
    ): CreatedGoogleDocDto = withContext(ioDispatcher) {
        val url = baseUrl.newBuilder()
            .addPathSegments("upload/drive/v3/files")
            .addQueryParameter("uploadType", "multipart")
            .addQueryParameter("fields", "id,name,webViewLink")
            .build()

        val metadata = mapOf(
            "name" to title,
            "mimeType" to "application/vnd.google-apps.document"
        )
        val metadataJson = gson.toJson(metadata)

        val multipartBody = MultipartBody.Builder()
            .setType(MULTIPART_RELATED)
            .addPart(metadataJson.toRequestBody(MEDIA_TYPE_JSON))
            .addPart(htmlContent.toRequestBody("text/html; charset=utf-8".toMediaType()))
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .post(multipartBody)
            .build()

        val response = executeRequest(request)
        val bodyString = response.body?.string().orEmpty()
        gson.fromJson(bodyString, CreatedGoogleDocDto::class.java)
    }

    suspend fun update(
        token: String,
        fileId: String,
        props: Map<String, String>,
        file: File
    ): DriveFileDto = withContext(ioDispatcher) {
        val url = baseUrl.newBuilder()
            .addPathSegments("upload/drive/v3/files/$fileId")
            .addQueryParameter("uploadType", "multipart")
            .addQueryParameter("fields", "id,name,size,modifiedTime,appProperties")
            .build()

        val metadata = mapOf(
            "appProperties" to props
        )
        val metadataJson = gson.toJson(metadata)

        val multipartBody = MultipartBody.Builder()
            .setType(MULTIPART_RELATED)
            .addPart(metadataJson.toRequestBody(MEDIA_TYPE_JSON))
            .addPart(file.asRequestBody(MEDIA_TYPE_OCTET_STREAM))
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .patch(multipartBody)
            .build()

        val response = executeRequest(request)
        val bodyString = response.body?.string().orEmpty()
        gson.fromJson(bodyString, DriveFileDto::class.java)
    }

    suspend fun download(token: String, fileId: String, target: File): Unit = withContext(ioDispatcher) {
        val url = baseUrl.newBuilder()
            .addPathSegments("drive/v3/files/$fileId")
            .addQueryParameter("alt", "media")
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        val response = executeRequest(request)
        val body = response.body ?: throw DriveException(response.code, "Empty body")

        val parentDir = target.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val tempFile = File.createTempFile("drive_dl_", ".tmp", parentDir)
        try {
            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (target.exists()) {
                target.delete()
            }
            if (!tempFile.renameTo(target)) {
                tempFile.copyTo(target, overwrite = true)
                tempFile.delete()
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    suspend fun delete(token: String, fileId: String): Unit = withContext(ioDispatcher) {
        val url = baseUrl.newBuilder()
            .addPathSegments("drive/v3/files/$fileId")
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        val response = http.newCall(request).execute()
        if (response.code == 404) {
            // 404 treated as success per spec
            response.close()
            return@withContext
        }
        if (!response.isSuccessful) {
            val bodyString = response.body?.string()
            val reason = parseErrorReason(bodyString)
            response.close()
            throw DriveException(response.code, reason)
        }
        response.close()
    }

    private fun executeRequest(request: Request): Response {
        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            val bodyString = response.body?.string()
            val reason = parseErrorReason(bodyString)
            response.close()
            throw DriveException(response.code, reason)
        }
        return response
    }

    private fun parseErrorReason(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val root = gson.fromJson(body, JsonObject::class.java)
            val errorObj = root.getAsJsonObject("error")
            val errorsArray = errorObj?.getAsJsonArray("errors")
            if (errorsArray != null && errorsArray.size() > 0) {
                errorsArray.get(0).asJsonObject.get("reason")?.asString
            } else {
                errorObj?.get("message")?.asString
            }
        } catch (_: Exception) {
            null
        }
    }

    private data class DriveFileListResponse(
        val nextPageToken: String? = null,
        val files: List<DriveFileDto>? = null
    )

    companion object {
        private val MULTIPART_RELATED = "multipart/related".toMediaType()
        private val MEDIA_TYPE_JSON = "application/json; charset=UTF-8".toMediaType()
        private val MEDIA_TYPE_OCTET_STREAM = "application/octet-stream".toMediaType()
    }
}
