package com.notifast.app.domain.util

import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSpanOpsTest {

    @Test
    fun `toggle on empty adds new span`() {
        val result = TextSpanOps.toggleStyle(emptyList(), 0, 5, TextStyle.BOLD)
        assertEquals(listOf(TextSpan(0, 5, setOf(TextStyle.BOLD))), result)
    }

    @Test
    fun `toggle existing style removes it`() {
        val spans = listOf(TextSpan(0, 5, setOf(TextStyle.BOLD)))
        val result = TextSpanOps.toggleStyle(spans, 0, 5, TextStyle.BOLD)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toggle second style merges into same span`() {
        val spans = listOf(TextSpan(0, 5, setOf(TextStyle.BOLD)))
        val result = TextSpanOps.toggleStyle(spans, 0, 5, TextStyle.ITALIC)
        assertEquals(1, result.size)
        assertEquals(setOf(TextStyle.BOLD, TextStyle.ITALIC), result[0].styles)
    }

    @Test
    fun `partial overlap splits span`() {
        val spans = listOf(TextSpan(0, 10, setOf(TextStyle.BOLD)))
        val result = TextSpanOps.toggleStyle(spans, 3, 6, TextStyle.BOLD)
        // 0-3 keeps BOLD, 3-6 loses BOLD, 6-10 keeps BOLD
        assertEquals(2, result.size)
        assertEquals(TextSpan(0, 3, setOf(TextStyle.BOLD)), result[0])
        assertEquals(TextSpan(6, 10, setOf(TextStyle.BOLD)), result[1])
    }

    @Test
    fun `normalize merges adjacent same-style spans`() {
        val spans = listOf(
            TextSpan(0, 3, setOf(TextStyle.BOLD)),
            TextSpan(3, 6, setOf(TextStyle.BOLD))
        )
        val result = TextSpanOps.normalize(spans)
        assertEquals(listOf(TextSpan(0, 6, setOf(TextStyle.BOLD))), result)
    }

    @Test
    fun `toggle with start == end is no-op`() {
        val result = TextSpanOps.toggleStyle(emptyList(), 4, 4, TextStyle.BOLD)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `adjustForEdit shifts spans after insertion point`() {
        val spans = listOf(TextSpan(5, 10, setOf(TextStyle.BOLD)))
        // insert 3 chars at index 2
        val result = TextSpanOps.adjustForEdit(spans, editStart = 2, oldLength = 0, newLength = 3)
        assertEquals(listOf(TextSpan(8, 13, setOf(TextStyle.BOLD))), result)
    }

    @Test
    fun `adjustForEdit shrinks span when deletion overlaps it`() {
        val spans = listOf(TextSpan(0, 10, setOf(TextStyle.BOLD)))
        // delete 4 chars starting at index 2 → span should become 0-6
        val result = TextSpanOps.adjustForEdit(spans, editStart = 2, oldLength = 4, newLength = 0)
        assertEquals(listOf(TextSpan(0, 6, setOf(TextStyle.BOLD))), result)
    }

    @Test
    fun `adjustForEdit drops empty spans`() {
        val spans = listOf(TextSpan(2, 4, setOf(TextStyle.BOLD)))
        // delete exactly the span range
        val result = TextSpanOps.adjustForEdit(spans, editStart = 2, oldLength = 2, newLength = 0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `adjustForEdit expands span when typing inside it`() {
        val spans = listOf(TextSpan(0, 5, setOf(TextStyle.ITALIC)))
        // insert 2 chars at index 2 → span becomes 0-7
        val result = TextSpanOps.adjustForEdit(spans, editStart = 2, oldLength = 0, newLength = 2)
        assertEquals(listOf(TextSpan(0, 7, setOf(TextStyle.ITALIC))), result)
    }

    @Test
    fun `stylesAt returns only styles covering the range`() {
        val spans = listOf(
            TextSpan(0, 10, setOf(TextStyle.BOLD)),
            TextSpan(5, 8, setOf(TextStyle.ITALIC))
        )
        assertEquals(setOf(TextStyle.BOLD), TextSpanOps.stylesAt(spans, 0, 3))
        assertEquals(setOf(TextStyle.BOLD, TextStyle.ITALIC), TextSpanOps.stylesAt(spans, 5, 8))
    }
}
