package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Note
import com.notifast.app.domain.util.NoteBlockCodec
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
            NoteBlockCodec.blocksToMarkdown(note.blocks)
        } else {
            note.content
        }
        return buildString {
            append("# ${note.title}\n")
            append("**Materia:** ${note.subject}$tagsText\n\n")
            append(body)
        }
    }
}
