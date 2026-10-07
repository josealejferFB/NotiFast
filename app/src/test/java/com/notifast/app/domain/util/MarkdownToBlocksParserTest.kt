package com.notifast.app.domain.util

import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownToBlocksParserTest {

    @Test
    fun `empty markdown produces empty block list`() {
        assertEquals(emptyList<NoteBlock>(), MarkdownToBlocksParser.parse(""))
    }

    @Test
    fun `blank markdown produces empty block list`() {
        assertEquals(emptyList<NoteBlock>(), MarkdownToBlocksParser.parse("   \n  \n"))
    }

    @Test
    fun `simple paragraph is parsed`() {
        val blocks = MarkdownToBlocksParser.parse("Hola mundo")
        assertEquals(1, blocks.size)
        val p = blocks[0] as NoteBlock.Paragraph
        assertEquals("Hola mundo", p.text)
    }

    @Test
    fun `headers at levels 1 2 3 become heading blocks`() {
        val blocks = MarkdownToBlocksParser.parse("# T1\n## T2\n### T3")
        assertEquals(3, blocks.size)
        assertEquals(1, (blocks[0] as NoteBlock.Heading).level)
        assertEquals("T1", (blocks[0] as NoteBlock.Heading).text)
        assertEquals(2, (blocks[1] as NoteBlock.Heading).level)
        assertEquals("T2", (blocks[1] as NoteBlock.Heading).text)
        assertEquals(3, (blocks[2] as NoteBlock.Heading).level)
        assertEquals("T3", (blocks[2] as NoteBlock.Heading).text)
    }

    @Test
    fun `consecutive bullet items become one bullet list block`() {
        val blocks = MarkdownToBlocksParser.parse("- uno\n- dos\n- tres")
        assertEquals(1, blocks.size)
        val list = blocks[0] as NoteBlock.BulletList
        assertEquals(3, list.items.size)
        assertEquals("uno", list.items[0].text)
        assertEquals("dos", list.items[1].text)
        assertEquals("tres", list.items[2].text)
    }

    @Test
    fun `star bullets are also parsed`() {
        val blocks = MarkdownToBlocksParser.parse("* uno\n* dos")
        val list = blocks[0] as NoteBlock.BulletList
        assertEquals(2, list.items.size)
    }

    @Test
    fun `task items become checklist block with checked state`() {
        val blocks = MarkdownToBlocksParser.parse("- [ ] pendiente\n- [x] hecho")
        assertEquals(1, blocks.size)
        val checklist = blocks[0] as NoteBlock.Checklist
        assertEquals(2, checklist.items.size)
        assertEquals("pendiente", checklist.items[0].text)
        assertEquals(false, checklist.items[0].isChecked)
        assertEquals("hecho", checklist.items[1].text)
        assertEquals(true, checklist.items[1].isChecked)
    }

    @Test
    fun `quote lines become quote block`() {
        val blocks = MarkdownToBlocksParser.parse("> esto es una cita")
        val quote = blocks[0] as NoteBlock.Quote
        assertEquals("esto es una cita", quote.text)
    }

    @Test
    fun `code block becomes paragraph with monospace marker`() {
        val blocks = MarkdownToBlocksParser.parse("```kotlin\nval x = 1\n```")
        assertEquals(1, blocks.size)
        val p = blocks[0] as NoteBlock.Paragraph
        assertEquals("val x = 1", p.text)
    }

    @Test
    fun `image url becomes trailing image block`() {
        val blocks = MarkdownToBlocksParser.parse("texto", imageUrl = "file:///foto.jpg")
        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is NoteBlock.Paragraph)
        val img = blocks[1] as NoteBlock.Image
        assertEquals("file:///foto.jpg", img.path)
    }

    @Test
    fun `image url alone becomes single image block`() {
        val blocks = MarkdownToBlocksParser.parse("", imageUrl = "file:///foto.jpg")
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is NoteBlock.Image)
    }

    @Test
    fun `null image url produces no image block`() {
        val blocks = MarkdownToBlocksParser.parse("texto", imageUrl = null)
        assertEquals(1, blocks.size)
    }

    @Test
    fun `bold inline formatting becomes span`() {
        val blocks = MarkdownToBlocksParser.parse("esto es **negrita** fin")
        val p = blocks[0] as NoteBlock.Paragraph
        assertEquals("esto es negrita fin", p.text)
        assertEquals(1, p.spans.size)
        assertEquals(8, p.spans[0].start)
        assertEquals(15, p.spans[0].end)
        assertTrue(p.spans[0].styles.contains(TextStyle.BOLD))
    }

    @Test
    fun `italic inline formatting becomes span`() {
        val blocks = MarkdownToBlocksParser.parse("esto es *cursiva* fin")
        val p = blocks[0] as NoteBlock.Paragraph
        assertEquals("esto es cursiva fin", p.text)
        assertEquals(1, p.spans.size)
        assertEquals(8, p.spans[0].start)
        assertEquals(15, p.spans[0].end)
        assertTrue(p.spans[0].styles.contains(TextStyle.ITALIC))
    }

    @Test
    fun `mixed document produces ordered blocks`() {
        val md = """
            # Título
            Introducción
            - punto a
            - punto b
            > cita
            - [ ] tarea
        """.trimIndent()
        val blocks = MarkdownToBlocksParser.parse(md)
        assertEquals(5, blocks.size)
        assertTrue(blocks[0] is NoteBlock.Heading)
        assertTrue(blocks[1] is NoteBlock.Paragraph)
        assertTrue(blocks[2] is NoteBlock.BulletList)
        assertTrue(blocks[3] is NoteBlock.Quote)
        assertTrue(blocks[4] is NoteBlock.Checklist)
    }

    @Test
    fun `text with existing image url is preserved as paragraph plus image`() {
        val blocks = MarkdownToBlocksParser.parse("## Nota\ncontenido", imageUrl = "file:///img.png")
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is NoteBlock.Heading)
        assertTrue(blocks[1] is NoteBlock.Paragraph)
        assertTrue(blocks[2] is NoteBlock.Image)
    }
}
