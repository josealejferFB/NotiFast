package com.notifast.app.domain.util

import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteBlockCodecTest {

    @Test
    fun `round trip preserves empty block list`() {
        val original = emptyList<NoteBlock>()
        val json = NoteBlockCodec.toJson(original)
        val decoded = NoteBlockCodec.fromJson(json)
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves heading block`() {
        val original = listOf(
            NoteBlock.Heading(id = "h1", text = "Título", level = 2)
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves paragraph with spans`() {
        val original = listOf(
            NoteBlock.Paragraph(
                id = "p1",
                text = "Hola mundo",
                spans = listOf(
                    TextSpan(start = 0, end = 4, styles = setOf(TextStyle.BOLD)),
                    TextSpan(start = 5, end = 10, styles = setOf(TextStyle.ITALIC, TextStyle.UNDERLINE))
                )
            )
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves bullet list`() {
        val original = listOf(
            NoteBlock.BulletList(
                id = "b1",
                items = listOf(
                    ListItem(text = "uno"),
                    ListItem(text = "dos", spans = listOf(TextSpan(0, 3, setOf(TextStyle.BOLD))))
                )
            )
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves checklist`() {
        val original = listOf(
            NoteBlock.Checklist(
                id = "c1",
                items = listOf(
                    CheckItem(text = "pendiente", isChecked = false),
                    CheckItem(text = "hecho", isChecked = true)
                )
            )
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves quote block`() {
        val original = listOf(
            NoteBlock.Quote(id = "q1", text = "cita")
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves image block`() {
        val original = listOf(
            NoteBlock.Image(
                id = "i1",
                path = "file:///data/img.jpg",
                caption = "Foto de pizarra",
                widthRatio = 0.8f
            )
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves math block`() {
        val original = listOf(
            NoteBlock.MathBlock(id = "m1", source = "frac{a}{b}", display = true)
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves divider`() {
        val original = listOf(NoteBlock.Divider(id = "d1"))
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves mixed block list with all types`() {
        val original = listOf(
            NoteBlock.Heading(id = "h1", text = "Intro", level = 1),
            NoteBlock.Paragraph(id = "p1", text = "Texto"),
            NoteBlock.BulletList(id = "b1", items = listOf(ListItem("item"))),
            NoteBlock.Checklist(id = "c1", items = listOf(CheckItem("tarea"))),
            NoteBlock.Quote(id = "q1", text = "cita"),
            NoteBlock.Image(id = "i1", path = "file:///x.jpg"),
            NoteBlock.MathBlock(id = "m1", source = "x^{2}"),
            NoteBlock.Divider(id = "d1")
        )
        val decoded = NoteBlockCodec.fromJson(NoteBlockCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `extractPlainText concatenates text from text-bearing blocks`() {
        val blocks = listOf(
            NoteBlock.Heading(id = "h1", text = "Título", level = 1),
            NoteBlock.Paragraph(id = "p1", text = "Párrafo"),
            NoteBlock.BulletList(id = "b1", items = listOf(ListItem("uno"), ListItem("dos"))),
            NoteBlock.Checklist(id = "c1", items = listOf(CheckItem("tarea", isChecked = true))),
            NoteBlock.Quote(id = "q1", text = "cita"),
            NoteBlock.Image(id = "i1", path = "file:///x.jpg", caption = "foto"),
            NoteBlock.MathBlock(id = "m1", source = "frac{a}{b}"),
            NoteBlock.Divider(id = "d1")
        )
        val plain = NoteBlockCodec.extractPlainText(blocks)
        assertTrue(plain.contains("Título"))
        assertTrue(plain.contains("Párrafo"))
        assertTrue(plain.contains("uno"))
        assertTrue(plain.contains("dos"))
        assertTrue(plain.contains("tarea"))
        assertTrue(plain.contains("cita"))
        assertTrue(plain.contains("foto"))
        assertTrue(plain.contains("frac{a}{b}"))
    }

    @Test
    fun `fromJson on empty array returns empty list`() {
        assertEquals(emptyList<NoteBlock>(), NoteBlockCodec.fromJson("[]"))
    }

    @Test
    fun `fromJson on empty string returns empty list`() {
        assertEquals(emptyList<NoteBlock>(), NoteBlockCodec.fromJson(""))
    }
}
