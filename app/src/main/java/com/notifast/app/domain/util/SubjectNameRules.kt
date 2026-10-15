package com.notifast.app.domain.util

object SubjectNameRules {
    const val MAX_LENGTH = 50
}

sealed interface NameValidation {
    data object Valid : NameValidation
    data object Blank : NameValidation
    data object TooLong : NameValidation
    data object Duplicate : NameValidation
}
