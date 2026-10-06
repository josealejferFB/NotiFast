package com.notifast.app.presentation.feature.settings

import java.io.File
import java.util.Locale

internal object SettingsCacheSupport {

    fun folderSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) folderSize(file) else file.length()
        }
        return size
    }

    fun formatBytesToMb(bytes: Long): String {
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }

    fun deleteFolderContents(dir: File?) {
        if (dir == null || !dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) deleteFolderContents(file)
            file.delete()
        }
    }
}
