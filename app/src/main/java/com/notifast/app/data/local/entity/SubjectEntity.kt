package com.notifast.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import java.util.Date

@Entity(
    tableName = "subjects",
    primaryKeys = ["userId", "name"]
)
data class SubjectEntity(
    val userId: String = "",
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String = "",
    val icon: String = "📚",
    val colorHex: String = "",
    val createdAt: Date? = null
) {
    @Ignore
    constructor(
        userId: String,
        name: String,
        icon: String,
        colorHex: String,
        createdAtTimestamp: Long?
    ) : this(
        userId = userId,
        name = name,
        icon = icon,
        colorHex = colorHex,
        createdAt = createdAtTimestamp?.let { Date(it) }
    )
}
