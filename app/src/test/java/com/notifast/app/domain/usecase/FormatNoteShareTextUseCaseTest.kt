package com.notifast.app.domain.usecase

import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatNoteShareTextUseCaseTest {

    private val useCase = FormatNoteShareTextUseCase()

    @Test
    fun `formats note with title, subject, tags, and plain content`() {
        val note = Note(
            title = "Cinemática",
            subject = "Física I",
            tags = listOf("movimiento", "MRU"),
            content = "Velocidad constante y aceleración nula."
        )

        val result = useCase(note)

        assertTrue(result.contains("*Cinemática*"))
        assertTrue(result.contains("Materia: Física I"))
        assertTrue(result.contains("Tags: #movimiento #MRU"))
        assertTrue(result.contains("Velocidad constante y aceleración nula."))
    }

    @Test
    fun `omits subject and tags when they are blank or empty`() {
        val note = Note(
            title = "Apunte Rápido",
            content = "Solo contenido sin materia ni etiquetas."
        )

        val result = useCase(note)

        assertTrue(result.startsWith("*Apunte Rápido*"))
        assertFalse(result.contains("Materia:"))
        assertFalse(result.contains("Tags:"))
        assertTrue(result.contains("Solo contenido sin materia ni etiquetas."))
    }

    @Test
    fun `formats blocks correctly with messaging markers`() {
        val note = Note(
            title = "Estructuras de Datos",
            subject = "Algoritmos",
            blocks = listOf(
                NoteBlock.Heading(id = "h1", text = "Árboles Binarios", level = 1),
                NoteBlock.Paragraph(
                    id = "p1",
                    text = "Un árbol AVL es balanceado.",
                    spans = listOf(TextSpan(3, 12, setOf(TextStyle.BOLD)))
                ),
                NoteBlock.BulletList(
                    id = "b1",
                    items = listOf(ListItem("Inserción"), ListItem("Eliminación")),
                    isOrdered = true
                ),
                NoteBlock.BulletList(
                    id = "b2",
                    items = listOf(ListItem("Rojo-Negro"), ListItem("B-Tree")),
                    isOrdered = false
                ),
                NoteBlock.Checklist(
                    id = "c1",
                    items = listOf(
                        CheckItem("Implementar rotaciones", isChecked = true),
                        CheckItem("Calcular factor de balance", isChecked = false)
                    )
                ),
                NoteBlock.Quote(id = "q1", text = "La complejidad es O(log n)"),
                NoteBlock.MathBlock(id = "m1", source = "h <= 1.44 \\log_2(n)"),
                NoteBlock.Image(id = "img1", path = "file:///images/avl.jpg", caption = "Rotación simple"),
                NoteBlock.Image(id = "img2", path = "file:///images/photo.jpg", caption = ""),
                NoteBlock.Divider(id = "d1")
            )
        )

        val result = useCase(note)

        // Heading formatted as bold
        assertTrue(result.contains("*Árboles Binarios*"))
        // Bold span in paragraph formatted with single asterisk for messaging
        assertTrue(result.contains("Un *árbol AVL* es balanceado."))
        // Ordered list with numbers
        assertTrue(result.contains("1. Inserción"))
        assertTrue(result.contains("2. Eliminación"))
        // Unordered list with bullets
        assertTrue(result.contains("• Rojo-Negro"))
        assertTrue(result.contains("• B-Tree"))
        // Checklist with checkmarks
        assertTrue(result.contains("[✓] Implementar rotaciones"))
        assertTrue(result.contains("[ ] Calcular factor de balance"))
        // Quote with >
        assertTrue(result.contains("> La complejidad es O(log n)"))
        // Math with ∑
        assertTrue(result.contains("∑ h <= 1.44 \\log_2(n)"))
        // Image with caption formatted, image without caption omitted from text
        assertTrue(result.contains("[📷 Rotación simple]"))
        assertFalse(result.contains("file:///images/photo.jpg"))
        // Divider
        assertTrue(result.contains("──────────"))
    }

    @Test
    fun `handles italic and strikethrough spans in paragraphs`() {
        val note = Note(
            title = "Test Estilos",
            blocks = listOf(
                NoteBlock.Paragraph(
                    id = "p1",
                    text = "Texto con cursiva y tachado",
                    spans = listOf(
                        TextSpan(10, 17, setOf(TextStyle.ITALIC)),
                        TextSpan(20, 27, setOf(TextStyle.STRIKETHROUGH))
                    )
                )
            )
        )

        val result = useCase(note)
        assertTrue(result.contains("_cursiva_"))
        assertTrue(result.contains("~tachado~"))
    }

    @Test
    fun `formats empty note gracefully`() {
        val note = Note(title = "")
        val result = useCase(note)
        assertEquals("", result)
    }
}
