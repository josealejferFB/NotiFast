package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Note
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
}
