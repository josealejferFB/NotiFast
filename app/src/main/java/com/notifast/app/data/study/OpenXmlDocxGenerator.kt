package com.notifast.app.data.study

import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenXmlDocxGenerator @Inject constructor() {

    fun generateDocx(draft: CoverDraft, outputStream: OutputStream): Result<Unit> {
        return try {
            ZipOutputStream(outputStream).use { zos ->
                // 1. [Content_Types].xml
                zos.putNextEntry(ZipEntry("[Content_Types].xml"))
                zos.write(CONTENT_TYPES_XML.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 2. _rels/.rels
                zos.putNextEntry(ZipEntry("_rels/.rels"))
                zos.write(ROOT_RELS_XML.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 3. word/_rels/document.xml.rels
                zos.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
                zos.write(WORD_RELS_XML.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 4. word/styles.xml
                zos.putNextEntry(ZipEntry("word/styles.xml"))
                zos.write(STYLES_XML.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 5. word/document.xml
                zos.putNextEntry(ZipEntry("word/document.xml"))
                val docXml = buildDocumentXml(draft)
                zos.write(docXml.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateSemanticHtml(draft: CoverDraft): String {
        val safeTitle = escapeXml(draft.workTitle)
        val safeSubtitle = escapeXml(draft.subtitle)
        val safeSubject = escapeXml(draft.subjectName)
        val safeTeacher = escapeXml(draft.teacherName)
        val safeInstitution = escapeXml(draft.institutionName)
        val safeFaculty = escapeXml(draft.facultyOrDepartment)
        val safeCity = escapeXml(draft.city)
        val safeDate = escapeXml(draft.formattedDate)
        val authorsFormatted = draft.authors.joinToString(", ") { escapeXml(it) }

        return when (draft.templateType) {
            CoverTemplateType.APA_7 -> {
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>$safeTitle</title>
                    <style>
                        body { font-family: 'Times New Roman', serif; font-size: 12pt; line-height: 2.0; margin: 1in; color: #000; }
                        .center { text-align: center; }
                        .title { font-weight: bold; margin-top: 2in; margin-bottom: 24pt; }
                        .meta { margin-top: 0; margin-bottom: 0; }
                    </style>
                </head>
                <body>
                    <div class="center">
                        <p class="title">$safeTitle${if (safeSubtitle.isNotBlank()) "<br>$safeSubtitle" else ""}</p>
                        <p class="meta">$authorsFormatted</p>
                        <p class="meta">$safeFaculty, $safeInstitution</p>
                        <p class="meta">$safeSubject</p>
                        <p class="meta">$safeTeacher</p>
                        <p class="meta">$safeDate</p>
                    </div>
                </body>
                </html>
                """.trimIndent()
            }
            CoverTemplateType.IEEE -> {
                val safeAbstract = escapeXml(draft.abstractText)
                val safeKeywords = escapeXml(draft.keywordsText)
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>$safeTitle</title>
                    <style>
                        body { font-family: 'Times New Roman', serif; font-size: 10pt; line-height: 1.2; margin: 0.75in; color: #000; }
                        .title { text-align: center; font-size: 24pt; font-weight: bold; margin-bottom: 12pt; }
                        .authors { text-align: center; font-size: 11pt; margin-bottom: 6pt; }
                        .affil { text-align: center; font-style: italic; font-size: 10pt; margin-bottom: 18pt; }
                        .section-title { font-weight: bold; font-style: italic; font-size: 10pt; display: inline; }
                        .abstract-box { margin-top: 18pt; text-align: justify; }
                    </style>
                </head>
                <body>
                    <h1 class="title">$safeTitle</h1>
                    <p class="authors">$authorsFormatted</p>
                    <p class="affil">$safeFaculty, $safeInstitution<br>$safeSubject - $safeTeacher<br>$safeCity, $safeDate</p>
                    <div class="abstract-box">
                        <p><span class="section-title">Abstract</span>—$safeAbstract</p>
                        <p><span class="section-title">Index Terms</span>—$safeKeywords</p>
                    </div>
                </body>
                </html>
                """.trimIndent()
            }
            CoverTemplateType.INSTITUTIONAL -> {
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>$safeTitle</title>
                    <style>
                        body { font-family: Arial, sans-serif; font-size: 12pt; line-height: 1.5; margin: 1in; color: #000; text-align: center; }
                        .header { font-weight: bold; text-transform: uppercase; font-size: 13pt; margin-bottom: 2in; }
                        .title { font-weight: bold; text-transform: uppercase; font-size: 16pt; margin-bottom: 1.5in; }
                        .authors-block { margin-bottom: 1.5in; font-size: 12pt; }
                        .footer { font-size: 12pt; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        ${safeInstitution.uppercase()}<br>
                        ${safeFaculty.uppercase()}<br>
                        ${safeSubject.uppercase()}
                    </div>
                    <div class="title">
                        ${safeTitle.uppercase()}${if (safeSubtitle.isNotBlank()) "<br>${safeSubtitle.uppercase()}" else ""}
                    </div>
                    <div class="authors-block">
                        <strong>Autor(es):</strong><br>
                        $authorsFormatted<br><br>
                        <strong>Tutor / Docente:</strong><br>
                        $safeTeacher
                    </div>
                    <div class="footer">
                        $safeCity, $safeDate
                    </div>
                </body>
                </html>
                """.trimIndent()
            }
        }
    }

    private fun buildDocumentXml(draft: CoverDraft): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        sb.append("<w:body>")

        when (draft.templateType) {
            CoverTemplateType.APA_7 -> {
                // Spacing paragraphs
                repeat(4) { sb.append("<w:p/>") }
                // Title (Heading 1, Bold, Centered)
                sb.append(pHeading(draft.workTitle, isCentered = true))
                if (draft.subtitle.isNotBlank()) {
                    sb.append(pHeading(draft.subtitle, isCentered = true))
                }
                sb.append("<w:p/>")
                // Metadata paragraphs (Centered)
                sb.append(pText(draft.authors.joinToString(", "), isCentered = true))
                sb.append(pText("${draft.facultyOrDepartment}, ${draft.institutionName}", isCentered = true))
                sb.append(pText(draft.subjectName, isCentered = true))
                sb.append(pText(draft.teacherName, isCentered = true))
                sb.append(pText(draft.formattedDate, isCentered = true))
            }
            CoverTemplateType.IEEE -> {
                sb.append(pHeading(draft.workTitle, isCentered = true))
                if (draft.subtitle.isNotBlank()) {
                    sb.append(pText(draft.subtitle, isItalic = true, isCentered = true))
                }
                sb.append("<w:p/>")
                sb.append(pText(draft.authors.joinToString(", "), isBold = true, isCentered = true))
                sb.append(pText("${draft.facultyOrDepartment}, ${draft.institutionName}", isItalic = true, isCentered = true))
                sb.append(pText("${draft.subjectName} - ${draft.teacherName}", isCentered = true))
                sb.append(pText("${draft.city}, ${draft.formattedDate}", isCentered = true))
                sb.append("<w:p/>")
                if (draft.abstractText.isNotBlank()) {
                    sb.append(pText("Abstract—${draft.abstractText}", isItalic = true))
                }
                if (draft.keywordsText.isNotBlank()) {
                    sb.append(pText("Index Terms—${draft.keywordsText}", isBold = true))
                }
            }
            CoverTemplateType.INSTITUTIONAL -> {
                sb.append(pText(draft.institutionName.uppercase(), isBold = true, isCentered = true))
                sb.append(pText(draft.facultyOrDepartment.uppercase(), isBold = true, isCentered = true))
                sb.append(pText(draft.subjectName.uppercase(), isCentered = true))
                repeat(4) { sb.append("<w:p/>") }
                sb.append(pHeading(draft.workTitle.uppercase(), isCentered = true))
                if (draft.subtitle.isNotBlank()) {
                    sb.append(pText(draft.subtitle.uppercase(), isBold = true, isCentered = true))
                }
                repeat(4) { sb.append("<w:p/>") }
                sb.append(pText("Autor(es): ${draft.authors.joinToString(", ")}", isCentered = true))
                sb.append(pText("Profesor: ${draft.teacherName}", isCentered = true))
                repeat(4) { sb.append("<w:p/>") }
                sb.append(pText("${draft.city}, ${draft.formattedDate}", isBold = true, isCentered = true))
            }
        }

        // Page break at the end of cover
        sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>")
        sb.append("</w:body></w:document>")
        return sb.toString()
    }

    private fun pHeading(text: String, isCentered: Boolean = false): String {
        val safe = escapeXml(text)
        val jc = if (isCentered) "<w:jc w:val=\"center\"/>" else ""
        return """<w:p><w:pPr><w:pStyle w:val="Heading1"/>$jc</w:pPr><w:r><w:rPr><w:b/></w:rPr><w:t>$safe</w:t></w:r></w:p>"""
    }

    private fun pText(text: String, isBold: Boolean = false, isItalic: Boolean = false, isCentered: Boolean = false): String {
        val safe = escapeXml(text)
        val jc = if (isCentered) "<w:pPr><w:jc w:val=\"center\"/></w:pPr>" else ""
        val b = if (isBold) "<w:b/>" else ""
        val i = if (isItalic) "<w:i/>" else ""
        val rPr = if (b.isNotEmpty() || i.isNotEmpty()) "<w:rPr>$b$i</w:rPr>" else ""
        return """<w:p>$jc<w:r>$rPr<w:t>$safe</w:t></w:r></w:p>"""
    }

    private fun escapeXml(str: String): String {
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    companion object {
        private const val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""

        private const val ROOT_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

        private const val WORD_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

        private const val STYLES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:rPr>
      <w:b/>
      <w:sz w:val="32"/>
    </w:rPr>
  </w:style>
</w:styles>"""
    }
}
