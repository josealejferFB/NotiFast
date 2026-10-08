package com.notifast.app.domain.usecase

import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportNoteMarkdownUseCaseTest {

    @Test
    fun `builds markdown with title subject and tags`() {
        val note = Note(
            title = "Derivadas",
            subject = "Cálculo III",
            tags = listOf("resumen", "examen"),
            content = "Contenido **importante**"
        )
        val md = ExportNoteMarkdownUseCase()(note)
        assertTrue(md.contains("# Derivadas"))
        assertTrue(md.contains("**Materia:** Cálculo III"))
        assertTrue(md.contains("#resumen"))
        assertTrue(md.contains("#examen"))
        assertTrue(md.contains("Contenido **importante**"))
    }

    @Test
    fun `omits tags line when empty`() {
        val md = ExportNoteMarkdownUseCase()(Note(title = "T", subject = "M", content = "C"))
        assertTrue(!md.contains("Tags:"))
    }

    @Test
    fun `export walks blocks and marks bold spans`() {
        val note = Note(
            title = "Tema 1",
            subject = "Física",
            tags = listOf("examen"),
            blocks = listOf(
                NoteBlock.Heading(id = "h", text = "Introducción", level = 1),
                NoteBlock.Paragraph(
                    id = "p",
                    text = "hola mundo",
                    spans = listOf(TextSpan(0, 4, setOf(TextStyle.BOLD)))
                ),
                NoteBlock.BulletList(
                    id = "l",
                    items = listOf(ListItem(text = "a"), ListItem(text = "b")),
                    isOrdered = true
                )
            )
        )
        val md = ExportNoteMarkdownUseCase()(note)
        assertTrue(md.contains("# Introducción"))
        assertTrue(md.contains("**hola**"))
        assertTrue(md.contains("1. a"))
        assertTrue(md.contains("2. b"))
    }

    @Test
    fun `export emits unordered bullets with dash`() {
        val note = Note(
            title = "T",
            subject = "M",
            blocks = listOf(
                NoteBlock.BulletList(
                    id = "l",
                    items = listOf(ListItem(text = "x"), ListItem(text = "y")),
                    isOrdered = false
                )
            )
        )
        val md = ExportNoteMarkdownUseCase()(note)
        assertTrue(md.contains("- x"))
        assertTrue(md.contains("- y"))
    }
}
