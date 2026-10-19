package com.notifast.app.data.study

import com.notifast.app.domain.model.study.DocxPreviewBlock
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

class InvalidDocumentFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Singleton
class OpenXmlDocxParser @Inject constructor() {

    companion object {
        const val MAX_PREVIEW_BLOCKS = 300
        private const val MAX_XML_BYTES = 10 * 1024 * 1024 // 10 MB
    }

    fun parse(inputStream: InputStream): Result<List<DocxPreviewBlock>> {
        return try {
            val xmlBytes = extractDocumentXml(inputStream)
                ?: return Result.failure(InvalidDocumentFormatException("El archivo no contiene 'word/document.xml'"))

            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                try {
                    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                } catch (_: Exception) {
                    // Ignored on platforms where feature is unsupported
                }
            }

            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(ByteArrayInputStream(xmlBytes))
            val bodyList = doc.getElementsByTagNameNS("*", "body")
            if (bodyList.length == 0) {
                return Result.success(emptyList())
            }

            val body = bodyList.item(0) as Element
            val blocks = mutableListOf<DocxPreviewBlock>()
            var truncated = false

            val childNodes = body.childNodes
            for (i in 0 until childNodes.length) {
                if (blocks.size >= MAX_PREVIEW_BLOCKS) {
                    truncated = true
                    break
                }
                val node = childNodes.item(i)
                if (node.nodeType != Node.ELEMENT_NODE) continue
                val elem = node as Element

                when (elem.localName ?: elem.nodeName.substringAfterLast(':')) {
                    "p" -> {
                        val block = parseParagraph(elem)
                        if (block != null) {
                            blocks.add(block)
                        }
                    }
                    "tbl" -> {
                        val table = parseTable(elem)
                        if (table != null) {
                            blocks.add(table)
                        }
                    }
                }
            }

            if (truncated) {
                blocks.add(
                    DocxPreviewBlock.Paragraph(
                        text = "[... Vista previa truncada a los primeros $MAX_PREVIEW_BLOCKS bloques. Abre el documento en Google Docs o app externa para verlo completo ...]",
                        isItalic = true,
                        isCentered = true
                    )
                )
            }

            Result.success(blocks)
        } catch (e: InvalidDocumentFormatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(InvalidDocumentFormatException("Error procesando estructura DOCX: ${e.message}", e))
        }
    }

    private fun extractDocumentXml(inputStream: InputStream): ByteArray? {
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name.replace('\\', '/')
                if (name == "word/document.xml") {
                    val baos = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var read: Int
                    var totalRead = 0
                    while (zis.read(buffer).also { read = it } != -1) {
                        totalRead += read
                        if (totalRead > MAX_XML_BYTES) {
                            break
                        }
                        baos.write(buffer, 0, read)
                    }
                    return baos.toByteArray()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return null
    }

    private fun parseParagraph(pElem: Element): DocxPreviewBlock? {
        val pPr = getFirstChildElement(pElem, "pPr")
        val pStyleElem = pPr?.let { getFirstChildElement(it, "pStyle") }
        val styleVal = pStyleElem?.let { getAttr(it, "val") }.orEmpty()

        val jcElem = pPr?.let { getFirstChildElement(it, "jc") }
        val jcVal = jcElem?.let { getAttr(it, "val") }.orEmpty()
        val isCentered = jcVal.equals("center", ignoreCase = true)

        val textBuilder = StringBuilder()
        var hasBold = false
        var hasItalic = false

        val runs = pElem.getElementsByTagNameNS("*", "r")
        val runsToProcess = if (runs.length > 0) {
            (0 until runs.length).map { runs.item(it) as Element }
        } else {
            // Fallback for non-namespace queries
            val list = mutableListOf<Element>()
            val children = pElem.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val elem = node as Element
                    val name = elem.localName ?: elem.nodeName.substringAfterLast(':')
                    if (name.equals("r", ignoreCase = true)) list.add(elem)
                }
            }
            list
        }

        for (rElem in runsToProcess) {
            val rPr = getFirstChildElement(rElem, "rPr")
            if (rPr != null) {
                if (getFirstChildElement(rPr, "b") != null) hasBold = true
                if (getFirstChildElement(rPr, "i") != null) hasItalic = true
            }

            val textNodes = rElem.getElementsByTagNameNS("*", "t")
            if (textNodes.length > 0) {
                for (j in 0 until textNodes.length) {
                    val tElem = textNodes.item(j) as Element
                    textBuilder.append(tElem.textContent.orEmpty())
                }
            } else {
                val children = rElem.childNodes
                for (j in 0 until children.length) {
                    val node = children.item(j)
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        val elem = node as Element
                        val name = elem.localName ?: elem.nodeName.substringAfterLast(':')
                        if (name.equals("t", ignoreCase = true)) {
                            textBuilder.append(elem.textContent.orEmpty())
                        }
                    }
                }
            }
        }

        val text = textBuilder.toString()
        if (text.isBlank()) return null

        val isHeading = styleVal.contains("Heading", ignoreCase = true) ||
            styleVal.contains("Title", ignoreCase = true) ||
            styleVal.contains("SubTitle", ignoreCase = true)

        return if (isHeading) {
            val level = when {
                styleVal.contains("1") -> 1
                styleVal.contains("2") -> 2
                styleVal.contains("3") -> 3
                styleVal.contains("Title", ignoreCase = true) -> 1
                else -> 2
            }
            DocxPreviewBlock.Heading(text = text.trim(), level = level, isCentered = isCentered)
        } else {
            DocxPreviewBlock.Paragraph(
                text = text.trim(),
                isBold = hasBold,
                isItalic = hasItalic,
                isCentered = isCentered
            )
        }
    }

    private fun getAttr(elem: Element, localName: String): String {
        val ns = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
        val v1 = elem.getAttributeNS(ns, localName)
        if (!v1.isNullOrEmpty()) return v1
        val v2 = elem.getAttribute("w:$localName")
        if (!v2.isNullOrEmpty()) return v2
        val v3 = elem.getAttribute(localName)
        if (!v3.isNullOrEmpty()) return v3
        val attrs = elem.attributes
        if (attrs != null) {
            for (i in 0 until attrs.length) {
                val attr = attrs.item(i)
                val name = attr.localName ?: attr.nodeName.substringAfterLast(':')
                if (name.equals(localName, ignoreCase = true)) {
                    return attr.nodeValue.orEmpty()
                }
            }
        }
        return ""
    }

    private fun parseTable(tblElem: Element): DocxPreviewBlock.Table? {
        val rows = mutableListOf<List<String>>()
        val trList = tblElem.getElementsByTagNameNS("*", "tr")

        for (i in 0 until trList.length) {
            val trElem = trList.item(i) as Element
            val rowCells = mutableListOf<String>()
            val tcList = trElem.getElementsByTagNameNS("*", "tc")

            for (j in 0 until tcList.length) {
                val tcElem = tcList.item(j) as Element
                val cellText = StringBuilder()
                val textNodes = tcElem.getElementsByTagNameNS("*", "t")
                for (k in 0 until textNodes.length) {
                    val tElem = textNodes.item(k) as Element
                    cellText.append(tElem.textContent.orEmpty()).append(" ")
                }
                rowCells.add(cellText.toString().trim())
            }
            if (rowCells.isNotEmpty()) {
                rows.add(rowCells)
            }
        }

        return if (rows.isNotEmpty()) DocxPreviewBlock.Table(rows) else null
    }

    private fun getFirstChildElement(parent: Element, localName: String): Element? {
        val children = parent.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE) {
                val elem = child as Element
                val name = elem.localName ?: elem.nodeName.substringAfterLast(':')
                if (name.equals(localName, ignoreCase = true)) {
                    return elem
                }
            }
        }
        return null
    }
}
