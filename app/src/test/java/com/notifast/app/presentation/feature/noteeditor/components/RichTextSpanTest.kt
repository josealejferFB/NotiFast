package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle as NoteTextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RichTextSpanTest {

    @Test
    fun `applyNoteSpans applies bold span`() {
        val result = applyNoteSpans(
            text = "hola mundo",
            spans = listOf(TextSpan(0, 4, setOf(NoteTextStyle.BOLD)))
        )
        assertEquals(1, result.spanStyles.size)
        assertEquals(FontWeight.Bold, result.spanStyles[0].item.fontWeight)
        assertEquals(0, result.spanStyles[0].start)
        assertEquals(4, result.spanStyles[0].end)
    }

    @Test
    fun `applyNoteSpans applies italic span`() {
        val result = applyNoteSpans(
            text = "hola",
            spans = listOf(TextSpan(0, 4, setOf(NoteTextStyle.ITALIC)))
        )
        assertEquals(1, result.spanStyles.size)
        assertEquals(FontStyle.Italic, result.spanStyles[0].item.fontStyle)
    }

    @Test
    fun `applyNoteSpans applies highlight as background`() {
        val result = applyNoteSpans(
            text = "abc",
            spans = listOf(TextSpan(1, 3, setOf(NoteTextStyle.HIGHLIGHT)))
        )
        assertEquals(1, result.spanStyles.size)
        assertNotNull(result.spanStyles[0].item.background)
    }

    @Test
    fun `applyNoteSpans applies underline and strikethrough`() {
        val result = applyNoteSpans(
            text = "abcdef",
            spans = listOf(
                TextSpan(0, 3, setOf(NoteTextStyle.UNDERLINE)),
                TextSpan(3, 6, setOf(NoteTextStyle.STRIKETHROUGH))
            )
        )
        assertEquals(2, result.spanStyles.size)
        assertEquals(TextDecoration.Underline, result.spanStyles[0].item.textDecoration)
        assertEquals(TextDecoration.LineThrough, result.spanStyles[1].item.textDecoration)
    }

    @Test
    fun `applyNoteSpans ignores out-of-range spans`() {
        val result = applyNoteSpans(
            text = "ab",
            spans = listOf(TextSpan(5, 9, setOf(NoteTextStyle.BOLD)))
        )
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `applyNoteSpans clamps spans to text bounds`() {
        val result = applyNoteSpans(
            text = "abcd",
            spans = listOf(TextSpan(2, 100, setOf(NoteTextStyle.BOLD)))
        )
        assertEquals(1, result.spanStyles.size)
        assertEquals(2, result.spanStyles[0].start)
        assertEquals(4, result.spanStyles[0].end)
    }
}
