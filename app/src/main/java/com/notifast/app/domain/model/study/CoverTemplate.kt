package com.notifast.app.domain.model.study

enum class CoverTemplateType(val id: String) {
    APA_7("APA_7"),
    IEEE("IEEE"),
    INSTITUTIONAL("INSTITUTIONAL");

    companion object {
        fun fromId(id: String): CoverTemplateType {
            return entries.firstOrNull { it.id == id } ?: APA_7
        }
    }
}

data class CoverDraft(
    val id: String,
    val userId: String,
    val templateType: CoverTemplateType,
    val subjectId: String = "",
    val subjectName: String = "",
    val workTitle: String = "",
    val subtitle: String = "",
    val authors: List<String> = emptyList(),
    val teacherName: String = "",
    val institutionName: String = "",
    val facultyOrDepartment: String = "",
    val city: String = "",
    val formattedDate: String = "",
    val abstractText: String = "",
    val keywordsText: String = "",
    val generatedLocalDocId: String? = null,
    val googleDocId: String? = null,
    val googleWebViewLink: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

sealed interface DocxPreviewBlock {
    data class Heading(
        val text: String,
        val level: Int = 1,
        val isCentered: Boolean = false
    ) : DocxPreviewBlock

    data class Paragraph(
        val text: String,
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isCentered: Boolean = false
    ) : DocxPreviewBlock

    data class Table(
        val rows: List<List<String>>
    ) : DocxPreviewBlock
}
