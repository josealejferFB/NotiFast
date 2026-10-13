package com.notifast.app.data.model

import java.util.Date

data class Teacher(
    val id: String = "",
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val subjectNames: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    val formattedPhoneNumber: String
        get() = phoneNumber.filter { it.isDigit() || it == '+' }

    val hasPhoneNumber: Boolean
        get() = formattedPhoneNumber.isNotBlank()
}
