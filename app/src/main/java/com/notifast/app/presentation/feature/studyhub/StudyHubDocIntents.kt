package com.notifast.app.presentation.feature.studyhub

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object StudyHubDocIntents {

    fun openExternalDoc(context: Context, fileName: String) {
        try {
            val file = File(File(context.filesDir, "study_docs"), fileName)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val mimeType = if (fileName.endsWith(".pdf", ignoreCase = true)) "application/pdf"
            else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir con"))
        } catch (_: Exception) {}
    }

    fun shareDoc(context: Context, fileName: String, title: String) {
        try {
            val file = File(File(context.filesDir, "study_docs"), fileName)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val mimeType = if (fileName.endsWith(".pdf", ignoreCase = true)) "application/pdf"
            else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir documento"))
        } catch (_: Exception) {}
    }
}
