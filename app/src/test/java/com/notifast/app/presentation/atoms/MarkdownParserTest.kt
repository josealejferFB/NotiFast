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

    @Test
    fun `parse math block formula correctly`() {
        val markdown = "$$ E = mc^2 $$"
        val blocks = MarkdownParser.parse(markdown)

        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.MathBlock)
        assertEquals("E = mc^2", (blocks[0] as MarkdownBlock.MathBlock).formula)
    }

    @Test
    fun `toggleTaskCheckbox flips unchecked to checked and vice versa`() {
        val markdown = """
            # Tareas
            - [ ] Primera tarea
            - [x] Segunda tarea
        """.trimIndent()

        val toggled1 = MarkdownParser.toggleTaskCheckbox(markdown, targetIndex = 0, isChecked = true)
        assertTrue(toggled1.contains("- [x] Primera tarea"))
        assertTrue(toggled1.contains("- [x] Segunda tarea"))

        val toggled2 = MarkdownParser.toggleTaskCheckbox(toggled1, targetIndex = 1, isChecked = false)
        assertTrue(toggled2.contains("- [x] Primera tarea"))
        assertTrue(toggled2.contains("- [ ] Segunda tarea"))
    }

    @Test
    fun `parse task items with compact no-space format and edge cases`() {
        val markdown = """
            -[ ] Tarea sin espacio
            -[x] Completada sin espacio
            -[ ]
            - [ ]
            * [ ] Viñeta asterisco
            * [x] Viñeta completada
        """.trimIndent()

        val blocks = MarkdownParser.parse(markdown)

        assertEquals(6, blocks.size)

        assertTrue(blocks[0] is MarkdownBlock.TaskItem)
        val task0 = blocks[0] as MarkdownBlock.TaskItem
        assertFalse(task0.isChecked)
        assertEquals("Tarea sin espacio", task0.text)

        assertTrue(blocks[1] is MarkdownBlock.TaskItem)
        val task1 = blocks[1] as MarkdownBlock.TaskItem
        assertTrue(task1.isChecked)
        assertEquals("Completada sin espacio", task1.text)

        assertTrue(blocks[2] is MarkdownBlock.TaskItem)
        val task2 = blocks[2] as MarkdownBlock.TaskItem
        assertFalse(task2.isChecked)
        assertEquals("", task2.text)

        assertTrue(blocks[3] is MarkdownBlock.TaskItem)
        val task3 = blocks[3] as MarkdownBlock.TaskItem
        assertFalse(task3.isChecked)
        assertEquals("", task3.text)

        assertTrue(blocks[4] is MarkdownBlock.TaskItem)
        val task4 = blocks[4] as MarkdownBlock.TaskItem
        assertFalse(task4.isChecked)
        assertEquals("Viñeta asterisco", task4.text)

        assertTrue(blocks[5] is MarkdownBlock.TaskItem)
        val task5 = blocks[5] as MarkdownBlock.TaskItem
        assertTrue(task5.isChecked)
        assertEquals("Viñeta completada", task5.text)
    }

    @Test
    fun `toggleTaskCheckbox handles compact no-space task items cleanly`() {
        val markdown = """
            -[ ] Tarea compacta
            -[x] Tarea hecha
        """.trimIndent()

        val toggled = MarkdownParser.toggleTaskCheckbox(markdown, targetIndex = 0, isChecked = true)
        assertTrue(toggled.contains("- [x] Tarea compacta"))

        val toggledBack = MarkdownParser.toggleTaskCheckbox(toggled, targetIndex = 1, isChecked = false)
        assertTrue(toggledBack.contains("- [ ] Tarea hecha"))
    }
}

