package com.notifast.app.data.model

data class NoteWithSubject(
    val note: Note,
    val subjectIcon: String = "📚",
    val subjectColorHex: String = ""
)
