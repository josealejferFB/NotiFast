package com.notifast.app.domain.model.study

enum class CoverTemplateType(val id: String) {
    APA_7("APA_7"),
    IEEE("IEEE"),
    INSTITUTIONAL("INSTITUTIONAL"),
    UNERG_DEGREE("UNERG_DEGREE");

    companion object {
        fun fromId(id: String): CoverTemplateType {
            return entries.firstOrNull { it.id == id } ?: APA_7
        }
    }
}

enum class ApaFont(
    val id: String,
    val fontName: String,
    val sizePt: Int,
    val isSerif: Boolean
) {
    TIMES_NEW_ROMAN("TIMES_NEW_ROMAN", "Times New Roman", 12, true),
    ARIAL("ARIAL", "Arial", 11, false),
    CALIBRI("CALIBRI", "Calibri", 11, false),
    GEORGIA("GEORGIA", "Georgia", 11, true),
    LUCIDA_SANS("LUCIDA_SANS", "Lucida Sans Unicode", 10, false);

    companion object {
        fun fromFontName(name: String): ApaFont {
            return entries.firstOrNull {
                it.fontName.equals(name, ignoreCase = true) || it.id.equals(name, ignoreCase = true)
            } ?: TIMES_NEW_ROMAN
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
    val tutorName: String = "",
    val fontName: String = ApaFont.TIMES_NEW_ROMAN.fontName,
    val fontSizePt: Int = ApaFont.TIMES_NEW_ROMAN.sizePt,
    val institutionName: String = "",
    val facultyOrDepartment: String = "",
    val city: String = "",
    val formattedDate: String = "",
    val abstractText: String = "",
    val keywordsText: String = "",
    val countryLine: String = "REPÚBLICA BOLIVARIANA DE VENEZUELA",
    val ministryLine: String = "MINISTERIO DEL PODER POPULAR PARA LA EDUCACIÓN UNIVERSITARIA",
    val programLine: String = "PROGRAMA DE INGENIERÍA EN INFORMÁTICA",
    val curricularUnit: String = "UNIDAD CURRICULAR: PROYECTO DE GRADO I",
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
