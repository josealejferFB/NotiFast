package com.notifast.app.domain.model.study

enum class StudyDocType {
    PDF,
    DOCX
}

enum class StudyDocSource {
    IMPORTED,
    DUPLICATED,
    GENERATED_TEMPLATE
}

data class StudyDocument(
    val id: String,
    val userId: String,
    val subjectId: String,
    val subjectName: String,
    val title: String,
    val fileName: String,
    val docType: StudyDocType,
    val sizeBytes: Long,
    val pageCount: Int,
    val sourceType: StudyDocSource,
    val googleDocId: String? = null,
    val googleWebViewLink: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
