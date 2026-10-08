package com.notifast.app.data.model

sealed class NoteBlock {
    abstract val id: String

    data class Heading(
        override val id: String,
        val text: String,
        val level: Int,
        val spans: List<TextSpan> = emptyList()
    ) : NoteBlock()

    data class Paragraph(
        override val id: String,
        val text: String,
        val spans: List<TextSpan> = emptyList()
    ) : NoteBlock()

    data class BulletList(
        override val id: String,
        val items: List<ListItem> = emptyList(),
        val isOrdered: Boolean = false
    ) : NoteBlock()

    data class Checklist(
        override val id: String,
        val items: List<CheckItem> = emptyList()
    ) : NoteBlock()

    data class Quote(
        override val id: String,
        val text: String,
        val spans: List<TextSpan> = emptyList()
    ) : NoteBlock()

    data class Image(
        override val id: String,
        val path: String,
        val caption: String = "",
        val widthRatio: Float = 1f,
        val annotationJson: String? = null
    ) : NoteBlock()

    data class MathBlock(
        override val id: String,
        val source: String,
        val display: Boolean = true
    ) : NoteBlock()

    data class Divider(
        override val id: String
    ) : NoteBlock()
}

data class TextSpan(
    val start: Int,
    val end: Int,
    val styles: Set<TextStyle>
)

data class ListItem(
    val text: String,
    val spans: List<TextSpan> = emptyList()
)

data class CheckItem(
    val text: String,
    val isChecked: Boolean = false,
    val spans: List<TextSpan> = emptyList()
)

enum class TextStyle {
    BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, HIGHLIGHT
}
