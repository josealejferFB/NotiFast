package com.notifast.app.presentation.atoms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun `parse headers correctly`() {
        val markdown = """
            # Título Principal
            ## Subtítulo Secundario
            ### Sección Pequeña
        """.trimIndent()

        val blocks = MarkdownParser.parse(markdown)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Header && (blocks[0] as MarkdownBlock.Header).level == 1)
        assertEquals("Título Principal", (blocks[0] as MarkdownBlock.Header).text)

        assertTrue(blocks[1] is MarkdownBlock.Header && (blocks[1] as MarkdownBlock.Header).level == 2)
        assertEquals("Subtítulo Secundario", (blocks[1] as MarkdownBlock.Header).text)

        assertTrue(blocks[2] is MarkdownBlock.Header && (blocks[2] as MarkdownBlock.Header).level == 3)
        assertEquals("Sección Pequeña", (blocks[2] as MarkdownBlock.Header).text)
    }

    @Test
    fun `parse bullet and task checklist items correctly`() {
        val markdown = """
            * Elemento con viñeta
            - [ ] Tarea pendiente
            - [x] Tarea completada
        """.trimIndent()

        val blocks = MarkdownParser.parse(markdown)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.BulletItem)
        assertEquals("Elemento con viñeta", (blocks[0] as MarkdownBlock.BulletItem).text)

        assertTrue(blocks[1] is MarkdownBlock.TaskItem)
        val task1 = blocks[1] as MarkdownBlock.TaskItem
        assertFalse(task1.isChecked)
        assertEquals("Tarea pendiente", task1.text)

        assertTrue(blocks[2] is MarkdownBlock.TaskItem)
        val task2 = blocks[2] as MarkdownBlock.TaskItem
        assertTrue(task2.isChecked)
        assertEquals("Tarea completada", task2.text)
    }

    @Test
    fun `parse blockquotes and code blocks correctly`() {
        val markdown = """
            > Esta es una cita importante
            ```kotlin
            val x = 10
            ```
        """.trimIndent()

        val blocks = MarkdownParser.parse(markdown)

        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Quote)
        assertEquals("Esta es una cita importante", (blocks[0] as MarkdownBlock.Quote).text)

        assertTrue(blocks[1] is MarkdownBlock.CodeBlock)
        val codeBlock = blocks[1] as MarkdownBlock.CodeBlock
        assertEquals("val x = 10", codeBlock.code.trim())
        assertEquals("kotlin", codeBlock.language)
    }

    @Test
    fun `parse regular paragraphs`() {
        val markdown = "Este es un párrafo de texto normal para estudiar."
        val blocks = MarkdownParser.parse(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Paragraph)
        assertEquals("Este es un párrafo de texto normal para estudiar.", (blocks[0] as MarkdownBlock.Paragraph).text)
    }
}
