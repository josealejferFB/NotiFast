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
    val cloudFile: CloudFile?
)

data class CloudOverview(
    val subjects: List<CloudSubjectItem>,
    val global: CloudFile?
)

enum class LocalSyncSummary {
    NO_ACCOUNT,
    ALL_SYNCED,
    PENDING
}
