package com.notifast.app.domain.model.cloud

enum class CloudSubjectStatus {
    SYNCED,
    LOCAL_CHANGES,
    CLOUD_NEWER,
    BOTH_UNLINKED,
    LOCAL_ONLY,
    CLOUD_ONLY
}

data class CloudSubjectItem(
    val subjectId: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val status: CloudSubjectStatus,
    val driveFileId: String?,
    val localHash: String?,
    val cloudFile: CloudFile?,
    val noteCount: Int = cloudFile?.noteCount ?: 0,
    val evaluationCount: Int = cloudFile?.evaluationCount ?: 0,
    val scheduleCount: Int = 0,
    val teacherCount: Int = cloudFile?.teacherCount ?: 0,
    val sizeBytes: Long = cloudFile?.sizeBytes ?: 0L,
    val lastSyncedAt: Long? = cloudFile?.uploadedAt,
    val section: String = ""
)

data class CloudOverview(
    val subjects: List<CloudSubjectItem>,
    val global: CloudFile?,
    val covers: CloudFile? = null,
    val localCoverCount: Int = 0,
    val coversStatus: CloudSubjectStatus = CloudSubjectStatus.LOCAL_ONLY
)

enum class LocalSyncSummary {
    NO_ACCOUNT,
    ALL_SYNCED,
    PENDING
}
