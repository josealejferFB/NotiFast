package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class NotesSearchFilterTest {

    private val sampleNotes = listOf(
        Note(id = "1", subject = "Matemáticas", title = "Derivadas Parciales", content = "Regla de la cadena y gradiente", timestamp = Date(1000)),
        Note(id = "2", subject = "Física", title = "Leyes de Newton", content = "Fuerza, masa y aceleración", timestamp = Date(2000)),
        Note(id = "3", subject = "Química", title = "Enlaces Covalentes", content = "Estructura de Lewis y polaridad", timestamp = Date(3000))
    )

    private fun filterNotes(notes: List<Note>, query: String): List<Note> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return notes
        return notes.filter {
            it.title.contains(cleanQuery, ignoreCase = true) ||
            it.content.contains(cleanQuery, ignoreCase = true) ||
            it.subject.contains(cleanQuery, ignoreCase = true)
        }
    }

    @Test
    fun `search by title case insensitive`() {
        val results = filterNotes(sampleNotes, "derivadas")
        assertEquals(1, results.size)
        assertEquals("1", results[0].id)
    }

    @Test
    fun `search with leading and trailing whitespaces`() {
        val results = filterNotes(sampleNotes, "   newton   ")
        assertEquals(1, results.size)
        assertEquals("2", results[0].id)
    }

    @Test
    fun `search by subject name`() {
        val results = filterNotes(sampleNotes, "Química")
        assertEquals(1, results.size)
        assertEquals("3", results[0].id)
    }

    @Test
    fun `search by content keyword`() {
        val results = filterNotes(sampleNotes, "aceleración")
        assertEquals(1, results.size)
        assertEquals("2", results[0].id)
    }

    @Test
    fun `search with no matches returns empty list`() {
        val results = filterNotes(sampleNotes, "Programación Cuántica")
        assertTrue(results.isEmpty())
    }
}
