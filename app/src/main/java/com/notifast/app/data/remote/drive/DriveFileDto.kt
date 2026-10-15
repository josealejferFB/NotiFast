package com.notifast.app.data.remote.drive

data class DriveFileDto(
    val id: String,
    val name: String,
    val size: Long? = null,
    val modifiedTime: String? = null,
    val appProperties: Map<String, String> = emptyMap()
)
