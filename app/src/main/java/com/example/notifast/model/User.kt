package com.example.notifast.model

data class User(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePictureUrl: String? = null
)
