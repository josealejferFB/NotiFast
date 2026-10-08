package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import javax.inject.Inject

/**
 * Builds a markdown document from a note for share/export.
 * Walks [NoteBlock] structure when present; falls back to plain content for legacy notes.
 */
class ExportNoteMarkdownUseCase @Inject constructor() {

    operator fun invoke(note: Note): String {
        val tagsText = if (note.tags.isNotEmpty()) {
            "\nTags: ${note.tags.joinToString(", ") { "#$it" }}"
        } else {
            ""
        }
        val body = if (note.blocks.isNotEmpty()) {
            blocksToMarkdown(note.blocks)
        } else {
            note.content
        }
        return buildString {
            append("# ${note.title}\n")
            append("**Materia:** ${note.subject}$tagsText\n\n")
            append(body)
        }
    }

    private fun blocksToMarkdown(blocks: List<NoteBlock>): String =
        blocks.joinToString("\n\n") { block ->
            when (block) {
                is NoteBlock.Heading -> {
                    val prefix = "#".repeat(block.level.coerceIn(1, 3))
                    "$prefix ${spansToMarkdown(block.text, block.spans)}"
                }
                is NoteBlock.Paragraph -> spansToMarkdown(block.text, block.spans)
                is NoteBlock.Quote -> block.text.lines().joinToString("\n") { "> $it" }
                is NoteBlock.BulletList -> block.items.mapIndexed { i, item ->
                    val prefix = if (block.isOrdered) "${i + 1}." else "-"
                    "$prefix ${spansToMarkdown(item.text, item.spans)}"
                }.joinToString("\n")
                is NoteBlock.Checklist -> block.items.joinToString("\n") { item ->
                    val mark = if (item.isChecked) "x" else " "
                    "- [$mark] ${item.text}"
                }
                is NoteBlock.Image -> "![${block.caption}](${block.path})"
                is NoteBlock.MathBlock -> "\$\$\n${block.source}\n\$\$"
                is NoteBlock.Divider -> "---"
            }
        }

    private fun spansToMarkdown(text: String, spans: List<TextSpan>): String {
        if (spans.isEmpty()) return text
        // Build per-char style set, then emit markdown markers at boundaries.
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
                sb.append("**").append(text, i, end).append("**")
                i = end
            } else if (italic[i]) {
                val end = runLength(italic, i)
                sb.append("*").append(text, i, end).append("*")
                i = end
            } else if (strike[i]) {
                val end = runLength(strike, i)
                sb.append("~~").append(text, i, end).append("~~")
                i = end
            } else {
                sb.append(text[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun runLength(flags: BooleanArray, start: Int): Int {
        var i = start
        while (i < flags.size && flags[i]) i++
        return i
    }
}
