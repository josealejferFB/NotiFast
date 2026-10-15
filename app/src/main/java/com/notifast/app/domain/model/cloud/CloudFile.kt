package com.notifast.app.domain.model.cloud

data class CloudFile(
    val driveFileId: String,
    val kind: String,
    val subjectId: String?,
    val subjectName: String?,
    val icon: String?,
    val colorHex: String?,
    val contentHash: String,
    val uploadedAt: Long,
    val deviceModel: String,
    val noteCount: Int,
    val evaluationCount: Int,
    val sizeBytes: Long,
    val schemaVersion: Int
)
