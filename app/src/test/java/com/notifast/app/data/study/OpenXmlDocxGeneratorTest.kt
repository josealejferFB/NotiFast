package com.notifast.app.data.study

import com.notifast.app.domain.model.study.ApaFont
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class OpenXmlDocxGeneratorTest {

    private val generator = OpenXmlDocxGenerator(null)

    @Test
    fun parseAuthor_preserves_spaces_when_user_is_typing() {
        val (name1, ci1) = OpenXmlDocxGenerator.parseAuthor("José ")
        assertEquals("José ", name1)
        assertEquals("", ci1)

        val (name2, ci2) = OpenXmlDocxGenerator.parseAuthor("José Miguel ")
        assertEquals("José Miguel ", name2)
        assertEquals("", ci2)
    }

    @Test
    fun parseAuthor_extracts_name_and_ci_cleanly() {
        val (name1, ci1) = OpenXmlDocxGenerator.parseAuthor("José Miguel (C.I.: 30.123.456)")
        assertEquals("José Miguel", name1)
        assertEquals("30.123.456", ci1)

        val (name2, ci2) = OpenXmlDocxGenerator.parseAuthor("Ana Gomez (C.I.: V-12.345.678)")
        assertEquals("Ana Gomez", name2)
        assertEquals("V-12.345.678", ci2)
    }

    @Test
    fun apaFont_has_correct_official_sizes() {
        assertEquals(12, ApaFont.TIMES_NEW_ROMAN.sizePt)
        assertEquals(11, ApaFont.ARIAL.sizePt)
        assertEquals(11, ApaFont.CALIBRI.sizePt)
        assertEquals(11, ApaFont.GEORGIA.sizePt)
        assertEquals(10, ApaFont.LUCIDA_SANS.sizePt)

        assertEquals(ApaFont.CALIBRI, ApaFont.fromFontName("Calibri"))
        assertEquals(ApaFont.TIMES_NEW_ROMAN, ApaFont.fromFontName("Times New Roman"))
    }

    @Test
    fun generateDocx_unerg_creates_2_column_layout_for_docente_and_autores() {
        val draft = CoverDraft(
            id = "d-unerg",
            userId = "u1",
            templateType = CoverTemplateType.UNERG_DEGREE,
            workTitle = "SISTEMA INTEGRAL DE APUNTES",
            subtitle = "TRABAJO ESPECIAL DE GRADO",
            authors = listOf("José Miguel (C.I.: 30.123.456)", "Ana María (C.I.: 29.876.543)"),
            teacherName = "Prof. Carlos Morales",
            tutorName = "Dra. Carmen Rivas",
            fontName = "Arial",
            fontSizePt = 11,
            city = "San Juan de los Morros",
            formattedDate = "Octubre de 2026"
        )

        val baos = ByteArrayOutputStream()
        val result = generator.generateDocx(draft, baos)
        assertTrue(result.isSuccess)
        assertTrue(baos.size() > 0)

        // Read entries from ZIP
        val zipIn = ZipInputStream(baos.toByteArray().inputStream())
        var documentXmlContent = ""
        var stylesXmlContent = ""

        var entry = zipIn.nextEntry
        while (entry != null) {
            if (entry.name == "word/document.xml") {
                documentXmlContent = zipIn.bufferedReader(Charsets.UTF_8).readText()
            } else if (entry.name == "word/styles.xml") {
                stylesXmlContent = zipIn.bufferedReader(Charsets.UTF_8).readText()
            }
            entry = zipIn.nextEntry
        }

        // Verify document.xml contains the 2-column table and both Docente and Autores/Tutor
        assertTrue(documentXmlContent.contains("<w:tbl>"))
        assertTrue(documentXmlContent.contains("Docente:"))
        assertTrue(documentXmlContent.contains("Prof. Carlos Morales"))
        assertTrue(documentXmlContent.contains("Autores:"))
        assertTrue(documentXmlContent.contains("José Miguel"))
        assertTrue(documentXmlContent.contains("30.123.456"))
        assertTrue(documentXmlContent.contains("Tutor Académico:"))
        assertTrue(documentXmlContent.contains("Dra. Carmen Rivas"))

        // Verify styles and document runs reflect the chosen Arial 11 pt (22 half-pts)
        assertTrue(stylesXmlContent.contains("Arial"))
        assertTrue(stylesXmlContent.contains("22")) // 11 pt * 2
        assertTrue(documentXmlContent.contains("Arial"))
    }

    @Test
    fun generateSemanticHtml_unerg_includes_side_by_side_table() {
        val draft = CoverDraft(
            id = "d-unerg-html",
            userId = "u1",
            templateType = CoverTemplateType.UNERG_DEGREE,
            workTitle = "TITULO",
            teacherName = "Prof. Gomez",
            tutorName = "Prof. Rodriguez",
            authors = listOf("Pedro (C.I.: 123)"),
            fontName = "Georgia",
            fontSizePt = 11
        )

        val html = generator.generateSemanticHtml(draft)
        assertTrue(html.contains("Docente:"))
        assertTrue(html.contains("Prof. Gomez"))
        assertTrue(html.contains("Tutor Académico:"))
        assertTrue(html.contains("Prof. Rodriguez"))
        assertTrue(html.contains("Georgia"))
        assertTrue(html.contains("11pt"))
    }
}
