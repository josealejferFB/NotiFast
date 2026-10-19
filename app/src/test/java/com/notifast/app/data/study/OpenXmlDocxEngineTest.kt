package com.notifast.app.data.study

import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.model.study.DocxPreviewBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class OpenXmlDocxEngineTest {

    private lateinit var parser: OpenXmlDocxParser
    private lateinit var generator: OpenXmlDocxGenerator

    @Before
    fun setUp() {
        parser = OpenXmlDocxParser()
        generator = OpenXmlDocxGenerator()
    }

    @Test
    fun `generateDocx and parse roundtrip preserves APA_7 title, author, subject, teacher and institution`() {
        val draft = CoverDraft(
            id = "d1",
            userId = "u1",
            templateType = CoverTemplateType.APA_7,
            subjectId = "s1",
            subjectName = "Física II",
            workTitle = "Análisis de Campos Electromagnéticos",
            subtitle = "Práctica de Laboratorio 3",
            authors = listOf("María González", "Carlos Pérez"),
            teacherName = "Prof. Alberto Maxwell",
            institutionName = "Universidad Simón Bolívar",
            facultyOrDepartment = "Departamento de Física",
            city = "Caracas",
            formattedDate = "19 de octubre de 2026",
            abstractText = "",
            keywordsText = "",
            generatedLocalDocId = null,
            googleDocId = null,
            googleWebViewLink = null,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val baos = ByteArrayOutputStream()
        val genResult = generator.generateDocx(draft, baos)
        assertTrue(genResult.isSuccess)

        val parseResult = parser.parse(ByteArrayInputStream(baos.toByteArray()))
        assertTrue(parseResult.isSuccess)
        val blocks = parseResult.getOrThrow()

        assertTrue(blocks.any { it is DocxPreviewBlock.Heading && it.text.contains("Análisis de Campos Electromagnéticos") })
        assertTrue(blocks.any { it is DocxPreviewBlock.Paragraph && it.text.contains("María González") })
        assertTrue(blocks.any { it is DocxPreviewBlock.Paragraph && it.text.contains("Prof. Alberto Maxwell") })
        assertTrue(blocks.any { it is DocxPreviewBlock.Paragraph && it.text.contains("Universidad Simón Bolívar") })
    }

    @Test
    fun `parse returns InvalidDocumentFormatException when zip has no word document xml`() {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("dummy.txt"))
            zos.write("hello world".toByteArray())
            zos.closeEntry()
        }

        val result = parser.parse(ByteArrayInputStream(baos.toByteArray()))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidDocumentFormatException)
    }

    @Test
    fun `parse returns InvalidDocumentFormatException on corrupt stream`() {
        val corruptStream = ByteArrayInputStream("not-a-valid-zip-stream".toByteArray())
        val result = parser.parse(corruptStream)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidDocumentFormatException)
    }

    @Test
    fun `parse truncates blocks when document exceeds MAX_PREVIEW_BLOCKS`() {
        // Construct a DOCX with 350 paragraphs
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("word/document.xml"))
            val sb = java.lang.StringBuilder()
            sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>""")
            for (i in 1..350) {
                sb.append("<w:p><w:r><w:t>Párrafo $i</w:t></w:r></w:p>")
            }
            sb.append("""</w:body></w:document>""")
            zos.write(sb.toString().toByteArray())
            zos.closeEntry()
        }

        val result = parser.parse(ByteArrayInputStream(baos.toByteArray()))
        assertTrue(result.isSuccess)
        val blocks = result.getOrThrow()
        assertEquals(301, blocks.size) // 300 parsed + 1 notice block
        val lastBlock = blocks.last() as DocxPreviewBlock.Paragraph
        assertTrue(lastBlock.text.contains("Vista previa truncada"))
    }

    @Test
    fun `generateSemanticHtml formats IEEE abstract and keywords and INSTITUTIONAL layout`() {
        val ieeeDraft = CoverDraft(
            id = "d2",
            userId = "u1",
            templateType = CoverTemplateType.IEEE,
            subjectId = "s2",
            subjectName = "Sistemas Digitales",
            workTitle = "Diseño de ALU de 32 bits",
            subtitle = "",
            authors = listOf("Ana Rivas"),
            teacherName = "Ing. Alan Turing",
            institutionName = "Instituto Tecnológico",
            facultyOrDepartment = "Escuela de Computación",
            city = "Caracas",
            formattedDate = "2026",
            abstractText = "Este trabajo presenta una ALU optimizada.",
            keywordsText = "ALU, VHDL, FPGA",
            generatedLocalDocId = null,
            googleDocId = null,
            googleWebViewLink = null,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val ieeeHtml = generator.generateSemanticHtml(ieeeDraft)
        assertTrue(ieeeHtml.contains("Diseño de ALU de 32 bits"))
        assertTrue(ieeeHtml.contains("Abstract"))
        assertTrue(ieeeHtml.contains("Este trabajo presenta una ALU optimizada."))
        assertTrue(ieeeHtml.contains("Index Terms"))
        assertTrue(ieeeHtml.contains("ALU, VHDL, FPGA"))

        val instDraft = ieeeDraft.copy(
            templateType = CoverTemplateType.INSTITUTIONAL,
            workTitle = "Propuesta de Tesis de Grado",
            abstractText = "",
            keywordsText = ""
        )
        val instHtml = generator.generateSemanticHtml(instDraft)
        assertTrue(instHtml.contains("INSTITUTO TECNOLÓGICO"))
        assertTrue(instHtml.contains("ESCUELA DE COMPUTACIÓN"))
        assertTrue(instHtml.contains("PROPUESTA DE TESIS DE GRADO"))
    }

    @Test
    fun `generateDocx and parse roundtrip preserves UNERG_DEGREE membrete, title, author with CI, and tutor`() {
        val unergDraft = CoverDraft(
            id = "d-unerg-1",
            userId = "u1",
            templateType = CoverTemplateType.UNERG_DEGREE,
            subjectId = "sub-1",
            subjectName = "Proyecto de Grado I",
            workTitle = "Desarrollo Móvil de un Hub Educativo con Arquitectura Asíncrona",
            subtitle = "",
            authors = listOf("José Fernandez (C.I.: V-27.665.738)", "María Gómez (C.I.: V-28.123.456)"),
            teacherName = "Prof. Miguel Padrón",
            institutionName = "UNIVERSIDAD NACIONAL EXPERIMENTAL DE LOS LLANOS CENTRALES RÓMULO GALLEGOS",
            facultyOrDepartment = "ÁREA DE INGENIERÍA DE SISTEMAS",
            city = "San Juan de los Morros",
            formattedDate = "Mayo 2026",
            countryLine = "REPÚBLICA BOLIVARIANA DE VENEZUELA",
            ministryLine = "MINISTERIO DEL PODER POPULAR PARA LA EDUCACIÓN UNIVERSITARIA",
            programLine = "PROGRAMA DE INGENIERÍA EN INFORMÁTICA",
            curricularUnit = "UNIDAD CURRICULAR: PROYECTO DE GRADO I"
        )

        val baos = ByteArrayOutputStream()
        val genResult = generator.generateDocx(unergDraft, baos)
        assertTrue(genResult.isSuccess)

        // Verify media images in zip
        val zipBytes = baos.toByteArray()
        val zipIn = java.util.zip.ZipInputStream(ByteArrayInputStream(zipBytes))
        val entryNames = mutableListOf<String>()
        var entry = zipIn.nextEntry
        while (entry != null) {
            entryNames.add(entry.name)
            entry = zipIn.nextEntry
        }
        assertTrue(entryNames.contains("word/media/image1.png"))
        assertTrue(entryNames.contains("word/media/image2.png"))

        // Verify parsed blocks
        val parseResult = parser.parse(ByteArrayInputStream(zipBytes))
        assertTrue(parseResult.isSuccess)
        val blocks = parseResult.getOrThrow()

        assertTrue(blocks.any { it is DocxPreviewBlock.Paragraph && it.text.contains("REPÚBLICA BOLIVARIANA DE VENEZUELA") })
        assertTrue(blocks.any { it is DocxPreviewBlock.Paragraph && it.text.contains("MINISTERIO DEL PODER POPULAR") })
        assertTrue(blocks.any { it is DocxPreviewBlock.Paragraph && it.text.contains("ÁREA DE INGENIERÍA DE SISTEMAS") })
        assertTrue(blocks.any { it is DocxPreviewBlock.Heading && it.text.contains("DESARROLLO MÓVIL DE UN HUB EDUCATIVO") })

        val containsText = { query: String ->
            blocks.any { block ->
                when (block) {
                    is DocxPreviewBlock.Paragraph -> block.text.contains(query)
                    is DocxPreviewBlock.Table -> block.rows.any { row -> row.any { cell -> cell.contains(query) } }
                    else -> false
                }
            }
        }

        assertTrue(containsText("José Fernandez"))
        assertTrue(containsText("V-27.665.738"))
        assertTrue(containsText("Prof. Miguel Padrón"))
        assertTrue(blocks.any { it is DocxPreviewBlock.Paragraph && it.text.contains("San Juan de los Morros - Mayo 2026") })
    }

    @Test
    fun `generateSemanticHtml formats UNERG_DEGREE with logos, membrete, title and authors`() {
        val draft = CoverDraft(
            id = "d-unerg-2",
            userId = "u1",
            templateType = CoverTemplateType.UNERG_DEGREE,
            subjectId = "sub-1",
            subjectName = "Proyecto de Grado I",
            workTitle = "Sistema de Gestión Académica",
            authors = listOf("José Fernandez (C.I.: V-27.665.738)"),
            teacherName = "Prof. Miguel Padrón",
            institutionName = "UNIVERSIDAD NACIONAL EXPERIMENTAL RÓMULO GALLEGOS",
            facultyOrDepartment = "ÁREA DE INGENIERÍA DE SISTEMAS",
            city = "San Juan de los Morros",
            formattedDate = "Mayo 2026"
        )

        val html = generator.generateSemanticHtml(draft)
        assertTrue(html.contains("REPÚBLICA BOLIVARIANA DE VENEZUELA"))
        assertTrue(html.contains("ÁREA DE INGENIERÍA DE SISTEMAS"))
        assertTrue(html.contains("Sistema de Gestión Académica"))
        assertTrue(html.contains("José Fernandez"))
        assertTrue(html.contains("V-27.665.738"))
        assertTrue(html.contains("Prof. Miguel Padrón"))
        assertTrue(html.contains("San Juan de los Morros - Mayo 2026"))
    }

    @Test
    fun `parseAuthor extracts name and CI correctly across formats`() {
        val (name1, ci1) = OpenXmlDocxGenerator.parseAuthor("José Fernandez (C.I.: V-27.665.738)")
        assertEquals("José Fernandez", name1)
        assertEquals("V-27.665.738", ci1)

        val (name2, ci2) = OpenXmlDocxGenerator.parseAuthor("Carlos Pérez (V-12345678)")
        assertEquals("Carlos Pérez", name2)
        assertEquals("V-12345678", ci2)

        val (name3, ci3) = OpenXmlDocxGenerator.parseAuthor("Ana Gómez")
        assertEquals("Ana Gómez", name3)
        assertEquals("", ci3)
    }
}
