package com.notifast.app.presentation.feature.noteeditor.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CameraUriHelper {

    private const val CAMERA_CAPTURES_DIR = "camera_captures"
    private const val FILE_PREFIX = "note_camera_"
    private const val FILE_SUFFIX = ".jpg"

    fun createTempPictureFile(context: Context): Pair<File, Uri> {
        val cacheFolder = File(context.cacheDir, CAMERA_CAPTURES_DIR).apply { mkdirs() }
        val tempFile = File.createTempFile(FILE_PREFIX, FILE_SUFFIX, cacheFolder)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return Pair(tempFile, uri)
    }

    fun cleanupEmptyFile(file: File?) {
        if (file != null && file.exists() && file.length() == 0L) {
            runCatching { file.delete() }
        }
    }
}
