package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "subject_cloud_state",
    primaryKeys = ["accountId", "subjectId"],
    indices = [
        Index("subjectId")
    ]
)
data class SubjectCloudStateEntity(
    val accountId: String,
    val subjectId: String,
    val driveFileId: String,
    val lastUploadedHash: String,
    val lastSyncedAt: Long
)
