package com.notifast.app.data.remote.drive

class DriveException(
    val code: Int,
    val reason: String? = null,
    cause: Throwable? = null
) : Exception("Drive API error $code: $reason", cause)
