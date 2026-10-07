package com.notifast.app.domain.util

import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import java.util.UUID

object MarkdownToBlocksParser {

    fun parse(markdown: String, imageUrl: String? = null): List<NoteBlock> {
        val blocks = mutableListOf<NoteBlock>()
        val lines = markdown.lines()
        var inCodeBlock = false
        val codeBuffer = StringBuilder()
        val bulletBuffer = mutableListOf<ListItem>()
        val checklistBuffer = mutableListOf<CheckItem>()

        fun flushBullets() {
            if (bulletBuffer.isNotEmpty()) {
                blocks.add(NoteBlock.BulletList(id = newId(), items = bulletBuffer.toList()))
                bulletBuffer.clear()
            }
        }

        fun flushChecklist() {
            if (checklistBuffer.isNotEmpty()) {
                blocks.add(NoteBlock.Checklist(id = newId(), items = checklistBuffer.toList()))
                checklistBuffer.clear()
            }
        }

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    flushBullets()
                    flushChecklist()
                    blocks.add(NoteBlock.Paragraph(id = newId(), text = codeBuffer.toString().trimEnd()))
                    codeBuffer.clear()
                    inCodeBlock = false
                } else {
                    flushBullets()
                    flushChecklist()
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBuffer.appendLine(line)
                continue
            }

            if (trimmed.isEmpty()) {
                flushBullets()
                flushChecklist()
                continue
            }

            when {
                trimmed.startsWith("### ") -> {
                    flushBullets()
                    flushChecklist()
                    val raw = trimmed.removePrefix("### ").trim()
                    val (text, spans) = parseInline(raw)
                    blocks.add(NoteBlock.Heading(id = newId(), text = text, level = 3, spans = spans))
                }
                trimmed.startsWith("## ") -> {
                    flushBullets()
                    flushChecklist()
                    val raw = trimmed.removePrefix("## ").trim()
                    val (text, spans) = parseInline(raw)
                    blocks.add(NoteBlock.Heading(id = newId(), text = text, level = 2, spans = spans))
                }
                trimmed.startsWith("# ") -> {
                    flushBullets()
                    flushChecklist()
                    val raw = trimmed.removePrefix("# ").trim()
                    val (text, spans) = parseInline(raw)
                    blocks.add(NoteBlock.Heading(id = newId(), text = text, level = 1, spans = spans))
                }
                trimmed.startsWith("- [ ] ") -> {
                    flushBullets()
                    val raw = trimmed.removePrefix("- [ ] ").trim()
                    val (text, spans) = parseInline(raw)
                    checklistBuffer.add(CheckItem(text = text, isChecked = false, spans = spans))
                }
                trimmed.startsWith("- [x] ", ignoreCase = true) -> {
                    flushBullets()
                    val raw = trimmed.substring(6).trim()
                    val (text, spans) = parseInline(raw)
                    checklistBuffer.add(CheckItem(text = text, isChecked = true, spans = spans))
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    flushChecklist()
                    val raw = if (trimmed.startsWith("* ")) {
                        trimmed.removePrefix("* ")
                    } else {
                        trimmed.removePrefix("- ")
                    }.trim()
                    val (text, spans) = parseInline(raw)
                    bulletBuffer.add(ListItem(text = text, spans = spans))
                }
                trimmed.startsWith("> ") -> {
                    flushBullets()
                    flushChecklist()
                    val raw = trimmed.removePrefix("> ").trim()
                    val (text, spans) = parseInline(raw)
                    blocks.add(NoteBlock.Quote(id = newId(), text = text, spans = spans))
                }
                else -> {
                    flushBullets()
                    flushChecklist()
                    val (text, spans) = parseInline(trimmed)
                    blocks.add(NoteBlock.Paragraph(id = newId(), text = text, spans = spans))
                }
            }
        }

        if (inCodeBlock) {
            flushBullets()
            flushChecklist()
            blocks.add(NoteBlock.Paragraph(id = newId(), text = codeBuffer.toString().trimEnd()))
        } else {
            flushBullets()
            flushChecklist()
        }

        if (!imageUrl.isNullOrBlank()) {
            blocks.add(NoteBlock.Image(id = newId(), path = imageUrl))
        }

        return blocks
    }

    private fun newId(): String = UUID.randomUUID().toString()

    internal data class InlineResult(val text: String, val spans: List<TextSpan>)

    internal fun parseInline(raw: String): InlineResult {
        val spans = mutableListOf<TextSpan>()
        val sb = StringBuilder()
        var i = 0

        while (i < raw.length) {
            when {
                raw.startsWith("**", i) -> {
                    val end = raw.indexOf("**", i + 2)
                    if (end != -1) {
                        val start = sb.length
                        sb.append(raw.substring(i + 2, end))
                        spans.add(TextSpan(start, sb.length, setOf(TextStyle.BOLD)))
                        i = end + 2
                    } else {
                        sb.append(raw[i]); i++
                    }
                }
                raw[i] == '*' && i + 1 < raw.length && raw[i + 1] != ' ' && raw[i + 1] != '*' -> {
                    val end = raw.indexOf('*', i + 1)
                    if (end != -1 && end > i + 1) {
                        val start = sb.length
                        sb.append(raw.substring(i + 1, end))
                        spans.add(TextSpan(start, sb.length, setOf(TextStyle.ITALIC)))
                        i = end + 1
                    } else {
                        sb.append(raw[i]); i++
                    }
                }
                raw[i] == '_' && i + 1 < raw.length && raw[i + 1] != ' ' -> {
                    val end = raw.indexOf('_', i + 1)
                    if (end != -1 && end > i + 1) {
                        val start = sb.length
                        sb.append(raw.substring(i + 1, end))
                        spans.add(TextSpan(start, sb.length, setOf(TextStyle.ITALIC)))
                        i = end + 1
                    } else {
                        sb.append(raw[i]); i++
                    }
                }
                raw[i] == '`' -> {
                    val end = raw.indexOf('`', i + 1)
                    if (end != -1 && end > i + 1) {
                        val start = sb.length
                        sb.append(raw.substring(i + 1, end))
                        spans.add(TextSpan(start, sb.length, setOf(TextStyle.HIGHLIGHT)))
                        i = end + 1
                    } else {
                        sb.append(raw[i]); i++
                    }
                }
                else -> {
                    sb.append(raw[i]); i++
                }
            }
        }

        return InlineResult(sb.toString(), spans)
    }
}
