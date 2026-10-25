package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import javax.inject.Inject

/**
 * Formats a [Note] into a clean, human-readable plain text message for sharing
 * across messaging platforms (WhatsApp, Telegram, SMS, Email).
 */
class FormatNoteShareTextUseCase @Inject constructor() {

    operator fun invoke(note: Note): String {
        val metaParts = mutableListOf<String>()
        if (note.subject.isNotBlank()) {
            metaParts.add("Materia: ${note.subject.trim()}")
        }
        if (note.tags.isNotEmpty()) {
            val formattedTags = note.tags.joinToString(" ") { tag ->
                if (tag.startsWith("#")) tag else "#$tag"
            }
            metaParts.add("Tags: $formattedTags")
        }

        val body = if (note.blocks.isNotEmpty()) {
            formatBlocksToPlainText(note.blocks)
        } else {
            note.content.trim()
        }

        return buildString {
            if (note.title.isNotBlank()) {
                append("*${note.title.trim()}*")
            }
            if (metaParts.isNotEmpty()) {
                if (isNotEmpty()) append("\n")
                append(metaParts.joinToString(" • "))
            }
            if (body.isNotBlank()) {
                if (isNotEmpty()) append("\n\n")
                append(body)
            }
        }.trim()
    }

    private fun formatBlocksToPlainText(blocks: List<NoteBlock>): String {
        return blocks.mapNotNull { block ->
            when (block) {
                is NoteBlock.Heading -> {
                    val formatted = spansToShareText(block.text, block.spans).trim()
                    if (formatted.isNotBlank()) "*$formatted*" else null
                }
                is NoteBlock.Paragraph -> {
                    val formatted = spansToShareText(block.text, block.spans).trim()
                    if (formatted.isNotBlank()) formatted else null
                }
                is NoteBlock.Quote -> {
                    val trimmed = block.text.trim()
                    if (trimmed.isNotBlank()) {
                        trimmed.lines().joinToString("\n") { line -> "> $line" }
                    } else null
                }
                is NoteBlock.BulletList -> {
                    if (block.items.isEmpty()) null
                    else block.items.mapIndexed { index, item ->
                        val prefix = if (block.isOrdered) "${index + 1}." else "•"
                        val formatted = spansToShareText(item.text, item.spans)
                        "$prefix $formatted"
                    }.joinToString("\n")
                }
                is NoteBlock.Checklist -> {
                    if (block.items.isEmpty()) null
                    else block.items.joinToString("\n") { item ->
                        val mark = if (item.isChecked) "[✓]" else "[ ]"
                        val formatted = spansToShareText(item.text, item.spans)
                        "$mark $formatted"
                    }
                }
                is NoteBlock.MathBlock -> {
                    val source = block.source.trim()
                    if (source.isNotBlank()) "∑ $source" else null
                }
                is NoteBlock.Image -> {
                    if (block.caption.isNotBlank()) "[📷 ${block.caption.trim()}]" else null
                }
                is NoteBlock.Divider -> "──────────"
            }
        }.filter { it.isNotBlank() }.joinToString("\n\n")
    }

    private fun spansToShareText(text: String, spans: List<TextSpan>): String {
        if (spans.isEmpty()) return text
        val n = text.length
        if (n == 0) return text
        val bold = BooleanArray(n)
        val italic = BooleanArray(n)
        val strike = BooleanArray(n)
        spans.forEach { span ->
            val s = span.start.coerceIn(0, n)
            val e = span.end.coerceIn(0, n)
            for (i in s until e) {
                if (TextStyle.BOLD in span.styles) bold[i] = true
                if (TextStyle.ITALIC in span.styles) italic[i] = true
                if (TextStyle.STRIKETHROUGH in span.styles) strike[i] = true
            }
        }
        val sb = StringBuilder()
        var i = 0
        while (i < n) {
            if (bold[i]) {
                val end = runLength(bold, i)
                sb.append("*").append(text, i, end).append("*")
                i = end
            } else if (italic[i]) {
                val end = runLength(italic, i)
                sb.append("_").append(text, i, end).append("_")
                i = end
            } else if (strike[i]) {
                val end = runLength(strike, i)
                sb.append("~").append(text, i, end).append("~")
                i = end
            } else {
                sb.append(text[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun runLength(arr: BooleanArray, start: Int): Int {
        var j = start
        while (j < arr.size && arr[j]) j++
        return j
    }
}
