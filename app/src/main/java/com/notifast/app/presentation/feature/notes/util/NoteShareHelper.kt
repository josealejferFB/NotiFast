package com.notifast.app.presentation.feature.notes.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.domain.usecase.FormatNoteShareTextUseCase
import java.io.File

/**
 * Utility for sharing notes as plain text messages with optional image attachments
 * via Android Sharesheet (WhatsApp, Telegram, Gmail, Messages, etc.).
 */
object NoteShareHelper {
    private const val TAG = "NoteShareHelper"

    fun shareNote(
        context: Context,
        note: Note,
        formatNoteShareTextUseCase: FormatNoteShareTextUseCase = FormatNoteShareTextUseCase()
    ) {
        try {
            val shareText = formatNoteShareTextUseCase(note)
            val imageUris = collectImageUris(context, note)
            val shareIntent = createShareIntent(context, note, shareText, imageUris)

            val chooserTitle = context.getString(R.string.notes_share_note)
            val chooserIntent = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share note: ${note.id}", e)
        }
    }

    fun createShareIntent(
        context: Context,
        note: Note,
        shareText: String,
        imageUris: List<Uri>
    ): Intent {
        return if (imageUris.isEmpty()) {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, note.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
        } else if (imageUris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUris.first())
                putExtra(Intent.EXTRA_SUBJECT, note.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri(note.title, imageUris.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                putExtra(Intent.EXTRA_SUBJECT, note.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val clipData = ClipData.newRawUri(note.title, imageUris.first())
                for (i in 1 until imageUris.size) {
                    clipData.addItem(ClipData.Item(imageUris[i]))
                }
                this.clipData = clipData
            }
        }
    }

    fun collectCandidatePaths(note: Note): List<String> {
        return buildList {
            if (!note.imageUrl.isNullOrBlank()) {
                add(note.imageUrl)
            }
            note.blocks.forEach { block ->
                if (block is NoteBlock.Image && block.path.isNotBlank()) {
                    add(block.path)
                }
            }
        }.distinct()
    }

    fun collectImageUris(context: Context, note: Note): List<Uri> {
        val candidatePaths = collectCandidatePaths(note)
        return candidatePaths.mapNotNull { rawPath ->
            resolveImageUri(context, rawPath)
        }
    }

    fun resolveImageUri(context: Context, rawPath: String): Uri? {
        if (rawPath.isBlank()) return null
        return try {
            if (rawPath.startsWith("content://")) {
                return Uri.parse(rawPath)
            }
            val file: File = when {
                rawPath.startsWith("file://") -> {
                    val cleanPath = Uri.parse(rawPath).path ?: rawPath.removePrefix("file://")
                    File(cleanPath)
                }
                rawPath.startsWith("/") -> File(rawPath)
                else -> File(context.filesDir, rawPath)
            }
            if (!file.exists() || file.length() == 0L) {
                return null
            }
            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (_: IllegalArgumentException) {
                // If file is outside declared FileProvider roots, copy to shared cache dir
                val sharedDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
                val destFile = File(sharedDir, file.name)
                file.copyTo(destFile, overwrite = true)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destFile
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not resolve image URI for path: $rawPath", e)
            null
        }
    }
}
