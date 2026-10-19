package com.notifast.app.data.study

import android.content.Context
import com.notifast.app.R
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

class OpenXmlDocxGenerator(
    private val context: Context? = null
) {

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
                val wordRels = if (draft.templateType == CoverTemplateType.UNERG_DEGREE) {
                    WORD_RELS_UNERG_XML
                } else {
                    WORD_RELS_XML
                }
                zos.write(wordRels.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 4. word/styles.xml
                zos.putNextEntry(ZipEntry("word/styles.xml"))
                val fontName = draft.fontName.ifBlank { "Times New Roman" }
                val sizePt = if (draft.fontSizePt > 0) draft.fontSizePt else 12
                val szHalfPt = sizePt * 2
                val stylesXml = getStylesXml(fontName, szHalfPt)
                zos.write(stylesXml.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // Optional media images for UNERG
                if (draft.templateType == CoverTemplateType.UNERG_DEGREE) {
                    val aisBytes = getAisLogoBytes()
                    if (aisBytes != null && aisBytes.isNotEmpty()) {
                        zos.putNextEntry(ZipEntry("word/media/image1.png"))
                        zos.write(aisBytes)
                        zos.closeEntry()
                    }
                    val bannerBytes = getBannerLogoBytes()
                    if (bannerBytes != null && bannerBytes.isNotEmpty()) {
                        zos.putNextEntry(ZipEntry("word/media/image2.png"))
                        zos.write(bannerBytes)
                        zos.closeEntry()
                    }
                }

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
            CoverTemplateType.UNERG_DEGREE -> {
                val aisBase64 = getAisLogoBytes()?.let { Base64.getEncoder().encodeToString(it) }
                val bannerBase64 = getBannerLogoBytes()?.let { Base64.getEncoder().encodeToString(it) }

                val aisImgTag = if (aisBase64 != null) "<img src=\"data:image/png;base64,$aisBase64\" style=\"max-height: 70px;\" alt=\"AIS\" />" else ""
                val bannerImgTag = if (bannerBase64 != null) "<img src=\"data:image/png;base64,$bannerBase64\" style=\"max-height: 60px;\" alt=\"UNERG\" />" else ""

                val safeTutor = escapeXml(draft.tutorName)
                val fontName = draft.fontName.ifBlank { "Times New Roman" }
                val sizePt = if (draft.fontSizePt > 0) draft.fontSizePt else 12

                val authorsHtml = draft.authors.joinToString("<br>") { author ->
                    val (name, ci) = parseAuthor(author)
                    if (ci.isNotBlank()) "${escapeXml(name)}<br>C.I.: ${escapeXml(ci)}" else escapeXml(name)
                }

                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>$safeTitle</title>
                    <style>
                        body { font-family: '$fontName', serif; font-size: ${sizePt}pt; line-height: 1.4; margin: 1in; color: #000; }
                        .header-table { width: 100%; border-collapse: collapse; margin-bottom: 1.5in; border: none; }
                        .header-table td { border: none; vertical-align: middle; }
                        .membrete { text-align: center; font-weight: bold; font-size: ${sizePt}pt; text-transform: uppercase; }
                        .title-block { text-align: center; font-weight: bold; font-size: ${sizePt + 2}pt; text-transform: uppercase; margin-bottom: 1.5in; }
                        .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 1.5in; border: none; }
                        .meta-table td { border: none; vertical-align: top; }
                        .footer-block { text-align: center; font-weight: bold; font-size: ${sizePt}pt; }
                    </style>
                </head>
                <body>
                    <table class="header-table">
                        <tr>
                            <td style="width: 25%; text-align: left;">$bannerImgTag</td>
                            <td style="width: 50%;" class="membrete">
                                ${escapeXml(draft.countryLine).uppercase()}<br>
                                ${escapeXml(draft.ministryLine).uppercase()}<br>
                                ${escapeXml(draft.institutionName).uppercase()}<br>
                                ${escapeXml(draft.facultyOrDepartment).uppercase()}<br>
                                ${escapeXml(draft.programLine).uppercase()}<br>
                                ${escapeXml(draft.curricularUnit).uppercase()}
                            </td>
                            <td style="width: 25%; text-align: right;">$aisImgTag</td>
                        </tr>
                    </table>
                    <div class="title-block">
                        $safeTitle${if (safeSubtitle.isNotBlank()) "<br>$safeSubtitle" else ""}
                    </div>
                    <table class="meta-table">
                        <tr>
                            <td style="width: 50%; text-align: left;">
                                <strong>Docente:</strong><br>
                                $safeTeacher
                            </td>
                            <td style="width: 50%; text-align: right;">
                                <strong>${if (draft.authors.size > 1) "Autores:" else "Autor:"}</strong><br>
                                $authorsHtml
                                ${if (safeTutor.isNotBlank()) "<br><br><strong>Tutor Académico:</strong><br>$safeTutor" else ""}
                            </td>
                        </tr>
                    </table>
                    <div class="footer-block">
                        $safeCity - $safeDate
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
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
        sb.append("<w:body>")

        val fontName = draft.fontName.ifBlank { "Times New Roman" }
        val sizePt = if (draft.fontSizePt > 0) draft.fontSizePt else 12
        val szHalfPt = sizePt * 2

        when (draft.templateType) {
            CoverTemplateType.APA_7 -> {
                repeat(4) { sb.append("<w:p/>") }
                sb.append(pHeading(draft.workTitle, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                if (draft.subtitle.isNotBlank()) {
                    sb.append(pHeading(draft.subtitle, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                }
                sb.append("<w:p/>")
                sb.append(pText(draft.authors.joinToString(", "), isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText("${draft.facultyOrDepartment}, ${draft.institutionName}", isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText(draft.subjectName, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText(draft.teacherName, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText(draft.formattedDate, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
            }
            CoverTemplateType.IEEE -> {
                sb.append(pHeading(draft.workTitle, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                if (draft.subtitle.isNotBlank()) {
                    sb.append(pText(draft.subtitle, isItalic = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                }
                sb.append("<w:p/>")
                sb.append(pText(draft.authors.joinToString(", "), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText("${draft.facultyOrDepartment}, ${draft.institutionName}", isItalic = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText("${draft.subjectName} - ${draft.teacherName}", isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText("${draft.city}, ${draft.formattedDate}", isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append("<w:p/>")
                if (draft.abstractText.isNotBlank()) {
                    sb.append(pText("Abstract—${draft.abstractText}", isItalic = true, fontName = fontName, szHalfPt = szHalfPt))
                }
                if (draft.keywordsText.isNotBlank()) {
                    sb.append(pText("Index Terms—${draft.keywordsText}", isBold = true, fontName = fontName, szHalfPt = szHalfPt))
                }
            }
            CoverTemplateType.INSTITUTIONAL -> {
                sb.append(pText(draft.institutionName.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText(draft.facultyOrDepartment.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText(draft.subjectName.uppercase(), isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                repeat(4) { sb.append("<w:p/>") }
                sb.append(pHeading(draft.workTitle.uppercase(), isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                if (draft.subtitle.isNotBlank()) {
                    sb.append(pText(draft.subtitle.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                }
                repeat(4) { sb.append("<w:p/>") }
                sb.append(pText("Autor(es): ${draft.authors.joinToString(", ")}", isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                sb.append(pText("Profesor: ${draft.teacherName}", isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                repeat(4) { sb.append("<w:p/>") }
                sb.append(pText("${draft.city}, ${draft.formattedDate}", isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
            }
            CoverTemplateType.UNERG_DEGREE -> {
                // 1. República
                sb.append(pText(draft.countryLine.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                // 2. Ministerio with logos
                sb.append(pUnergMinistryWithLogos(draft.ministryLine.uppercase(), fontName = fontName, szHalfPt = szHalfPt))
                // 3. Universidad
                sb.append(pText(draft.institutionName.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                // 4. Área
                sb.append(pText(draft.facultyOrDepartment.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                // 5. Programa
                sb.append(pText(draft.programLine.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                // 6. Unidad Curricular
                sb.append(pText(draft.curricularUnit.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))

                repeat(4) { sb.append("<w:p/>") }

                // Title
                sb.append(pHeading(draft.workTitle.uppercase(), isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                if (draft.subtitle.isNotBlank()) {
                    sb.append(pText(draft.subtitle.uppercase(), isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
                }

                repeat(4) { sb.append("<w:p/>") }

                // 2-column borderless table: Docente (Left) vs Autores & Tutor (Right)
                val authorHeader = if (draft.authors.size > 1) "Autores:" else "Autor:"
                val rPrBase = "<w:rFonts w:ascii=\"$fontName\" w:hAnsi=\"$fontName\" w:cs=\"$fontName\"/><w:sz w:val=\"$szHalfPt\"/><w:szCs w:val=\"$szHalfPt\"/>"
                val rPrBold = "<w:b/><w:rFonts w:ascii=\"$fontName\" w:hAnsi=\"$fontName\" w:cs=\"$fontName\"/><w:sz w:val=\"$szHalfPt\"/><w:szCs w:val=\"$szHalfPt\"/>"

                val authorsXml = buildString {
                    draft.authors.forEach { rawAuthor ->
                        val (name, ci) = parseAuthor(rawAuthor)
                        append("""<w:p><w:pPr><w:jc w:val="right"/></w:pPr><w:r><w:rPr>$rPrBase</w:rPr><w:t>${escapeXml(name)}</w:t></w:r></w:p>""")
                        if (ci.isNotBlank()) {
                            append("""<w:p><w:pPr><w:jc w:val="right"/></w:pPr><w:r><w:rPr>$rPrBase</w:rPr><w:t>C.I.: ${escapeXml(ci)}</w:t></w:r></w:p>""")
                        }
                    }
                }

                val tutorXml = if (draft.tutorName.isNotBlank()) {
                    """<w:p><w:pPr><w:jc w:val="right"/></w:pPr><w:r><w:rPr>$rPrBold</w:rPr><w:t>Tutor Académico:</w:t></w:r></w:p><w:p><w:pPr><w:jc w:val="right"/></w:pPr><w:r><w:rPr>$rPrBase</w:rPr><w:t>${escapeXml(draft.tutorName)}</w:t></w:r></w:p>"""
                } else ""

                val sideBySideTable = """<w:tbl><w:tblPr><w:tblW w:w="9360" w:type="dxa"/><w:tblBorders><w:top w:val="none"/><w:left w:val="none"/><w:bottom w:val="none"/><w:right w:val="none"/><w:insideH w:val="none"/><w:insideV w:val="none"/></w:tblBorders></w:tblPr><w:tr><w:tc><w:tcPr><w:tcW w:w="4680" w:type="dxa"/><w:vAlign w:val="top"/></w:tcPr><w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr>$rPrBold</w:rPr><w:t>Docente:</w:t></w:r></w:p><w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr>$rPrBase</w:rPr><w:t>${escapeXml(draft.teacherName)}</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:w="4680" w:type="dxa"/><w:vAlign w:val="top"/></w:tcPr><w:p><w:pPr><w:jc w:val="right"/></w:pPr><w:r><w:rPr>$rPrBold</w:rPr><w:t>$authorHeader</w:t></w:r></w:p>$authorsXml$tutorXml</w:tc></w:tr></w:tbl>"""

                sb.append(sideBySideTable)

                repeat(4) { sb.append("<w:p/>") }

                // City and Date
                sb.append(pText("${draft.city} - ${draft.formattedDate}", isBold = true, isCentered = true, fontName = fontName, szHalfPt = szHalfPt))
            }
        }

        // Page break at the end of cover
        sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>")
        sb.append("</w:body></w:document>")
        return sb.toString()
    }

    private fun pUnergMinistryWithLogos(ministryText: String, fontName: String = "Times New Roman", szHalfPt: Int = 24): String {
        val safeMinistry = escapeXml(ministryText)
        val drawingBanner = """<w:drawing xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"><wp:anchor allowOverlap="1" behindDoc="0" distB="114300" distT="114300" distL="114300" distR="114300" hidden="0" layoutInCell="1" locked="0" relativeHeight="0" simplePos="0"><wp:simplePos x="0" y="0"/><wp:positionH relativeFrom="column"><wp:posOffset>-571499</wp:posOffset></wp:positionH><wp:positionV relativeFrom="paragraph"><wp:posOffset>209550</wp:posOffset></wp:positionV><wp:extent cx="1490663" cy="648877"/><wp:effectExtent b="0" l="0" r="0" t="0"/><wp:wrapNone/><wp:docPr id="3" name="image2.png"/><a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic><pic:nvPicPr><pic:cNvPr id="0" name="image2.png"/><pic:cNvPicPr preferRelativeResize="0"/></pic:nvPicPr><pic:blipFill><a:blip r:embed="rId7"/><a:srcRect b="0" l="0" r="0" t="0"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="1490663" cy="648877"/></a:xfrm><a:prstGeom prst="rect"/><a:ln/></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:anchor></w:drawing>"""
        val drawingAis = """<w:drawing xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"><wp:anchor allowOverlap="1" behindDoc="0" distB="114300" distT="114300" distL="114300" distR="114300" hidden="0" layoutInCell="1" locked="0" relativeHeight="0" simplePos="0"><wp:simplePos x="0" y="0"/><wp:positionH relativeFrom="column"><wp:posOffset>5238750</wp:posOffset></wp:positionH><wp:positionV relativeFrom="paragraph"><wp:posOffset>161925</wp:posOffset></wp:positionV><wp:extent cx="738188" cy="738188"/><wp:effectExtent b="0" l="0" r="0" t="0"/><wp:wrapNone/><wp:docPr id="1" name="image1.png"/><a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic><pic:nvPicPr><pic:cNvPr id="0" name="image1.png"/><pic:cNvPicPr preferRelativeResize="0"/></pic:nvPicPr><pic:blipFill><a:blip r:embed="rId6"/><a:srcRect b="0" l="0" r="0" t="0"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="738188" cy="738188"/></a:xfrm><a:prstGeom prst="rect"/><a:ln/></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:anchor></w:drawing>"""

        val rPr = "<w:rPr><w:b/><w:rFonts w:ascii=\"$fontName\" w:hAnsi=\"$fontName\" w:cs=\"$fontName\"/><w:sz w:val=\"$szHalfPt\"/><w:szCs w:val=\"$szHalfPt\"/></w:rPr>"
        return """<w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r>$drawingBanner$drawingAis</w:r><w:r>$rPr<w:t>$safeMinistry</w:t></w:r></w:p>"""
    }

    private fun pHeading(text: String, isCentered: Boolean = false, fontName: String = "Times New Roman", szHalfPt: Int = 24): String {
        val safe = escapeXml(text)
        val jc = if (isCentered) "<w:jc w:val=\"center\"/>" else ""
        return """<w:p><w:pPr><w:pStyle w:val="Heading1"/>$jc</w:pPr><w:r><w:rPr><w:b/><w:rFonts w:ascii="$fontName" w:hAnsi="$fontName" w:cs="$fontName"/><w:sz w:val="${szHalfPt + 4}"/><w:szCs w:val="${szHalfPt + 4}"/></w:rPr><w:t>$safe</w:t></w:r></w:p>"""
    }

    private fun pText(
        text: String,
        isBold: Boolean = false,
        isItalic: Boolean = false,
        isCentered: Boolean = false,
        fontName: String = "Times New Roman",
        szHalfPt: Int = 24
    ): String {
        val safe = escapeXml(text)
        val jc = if (isCentered) "<w:pPr><w:jc w:val=\"center\"/></w:pPr>" else ""
        val b = if (isBold) "<w:b/>" else ""
        val i = if (isItalic) "<w:i/>" else ""
        val rFonts = "<w:rFonts w:ascii=\"$fontName\" w:hAnsi=\"$fontName\" w:cs=\"$fontName\"/>"
        val sz = "<w:sz w:val=\"$szHalfPt\"/><w:szCs w:val=\"$szHalfPt\"/>"
        val rPr = "<w:rPr>$b$i$rFonts$sz</w:rPr>"
        return """<w:p>$jc<w:r>$rPr<w:t>$safe</w:t></w:r></w:p>"""
    }

    private fun getAisLogoBytes(): ByteArray? {
        try {
            context?.resources?.openRawResource(R.drawable.logo_unerg_ais)?.use { return it.readBytes() }
        } catch (_: Exception) {}
        try {
            javaClass.classLoader?.getResourceAsStream("logo_unerg_ais.png")?.use { return it.readBytes() }
        } catch (_: Exception) {}
        return null
    }

    private fun getBannerLogoBytes(): ByteArray? {
        try {
            context?.resources?.openRawResource(R.drawable.logo_unerg_banner)?.use { return it.readBytes() }
        } catch (_: Exception) {}
        try {
            javaClass.classLoader?.getResourceAsStream("logo_unerg_banner.png")?.use { return it.readBytes() }
        } catch (_: Exception) {}
        return null
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
        fun parseAuthor(raw: String): Pair<String, String> {
            val regex = Regex("""^(.*?)(?:\s*\((?:C\.?I\.?:?\s*)?([VEve\d\.\-]+)\))$""")
            val match = regex.find(raw.trimEnd())
            return if (match != null && match.groupValues[2].isNotBlank()) {
                val name = match.groupValues[1].trimEnd()
                val ci = match.groupValues[2].trim()
                name to ci
            } else {
                raw to ""
            }
        }

        fun getStylesXml(fontName: String, szHalfPt: Int): String {
            return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
    <w:rPr>
      <w:rFonts w:ascii="$fontName" w:hAnsi="$fontName" w:cs="$fontName"/>
      <w:sz w:val="$szHalfPt"/>
      <w:szCs w:val="$szHalfPt"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:rPr>
      <w:b/>
      <w:rFonts w:ascii="$fontName" w:hAnsi="$fontName" w:cs="$fontName"/>
      <w:sz w:val="${szHalfPt + 8}"/>
      <w:szCs w:val="${szHalfPt + 8}"/>
    </w:rPr>
  </w:style>
</w:styles>"""
        }

        private const val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
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

        private const val WORD_RELS_UNERG_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId6" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/image1.png"/>
  <Relationship Id="rId7" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/image2.png"/>
</Relationships>"""
    }
}
