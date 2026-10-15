package com.notifast.app.data.backup

data class BackupManifest(
    val format: String = "notifast-backup",
    val schemaVersion: Int,
    val kind: String, // "subject" | "global"
    val createdAt: Long = System.currentTimeMillis()
)

class UnsupportedBackupException(val foundVersion: Int) :
    Exception("Unsupported backup schema version: $foundVersion")
