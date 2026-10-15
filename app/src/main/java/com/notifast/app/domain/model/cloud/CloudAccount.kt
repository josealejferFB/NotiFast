package com.notifast.app.domain.model.cloud

data class CloudAccount(
    val accountId: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?
)
