package com.notifast.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "subjects",
    primaryKeys = ["userId", "id"],
    indices = [
        Index(value = ["userId", "name"], unique = true)
    ]
)
data class SubjectEntity(
    val userId: String = "",
    val id: String = "",
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String = "",
    val icon: String = "📚",
    val colorHex: String = "",
    val cycle: String = "",
    val createdAt: Date? = null
) {
    constructor(
        userId: String,
        name: String,
        icon: String,
        colorHex: String,
        createdAtTimestamp: Long?
    ) : this(
        userId = userId,
        id = "",
        name = name,
        icon = icon,
        colorHex = colorHex,
        cycle = "",
        createdAt = createdAtTimestamp?.let { Date(it) }
    )
}
