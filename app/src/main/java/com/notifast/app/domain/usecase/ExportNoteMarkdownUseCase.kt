package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Note
import javax.inject.Inject

/**
 * Builds a markdown document from a note for share/export.
 * Display copy stays in presentation; this is pure formatting (AGENTS §5).
 */
class ExportNoteMarkdownUseCase @Inject constructor() {

    operator fun invoke(note: Note): String {
        val tagsText = if (note.tags.isNotEmpty()) {
            "\nTags: ${note.tags.joinToString(", ") { "#$it" }}"
        } else {
            ""
        }
        return buildString {
            append("# ${note.title}\n")
            append("**Materia:** ${note.subject}$tagsText\n\n")
            append(note.content)
        }
    }
}
