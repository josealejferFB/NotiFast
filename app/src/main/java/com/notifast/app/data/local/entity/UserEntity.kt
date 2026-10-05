package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePictureUrl: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    @Ignore
    constructor(
        uid: String,
        email: String,
        firstName: String,
        lastName: String,
        profilePictureUrl: String?,
        createdAtTimestamp: Long?,
        updatedAtTimestamp: Long?
    ) : this(
        uid = uid,
        email = email,
        firstName = firstName,
        lastName = lastName,
        profilePictureUrl = profilePictureUrl,
        createdAt = createdAtTimestamp?.let { Date(it) },
        updatedAt = updatedAtTimestamp?.let { Date(it) }
    )
}
