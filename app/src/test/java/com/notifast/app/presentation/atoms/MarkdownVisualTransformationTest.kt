package com.notifast.app.presentation.atoms

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarkdownVisualTransformationTest {

    private lateinit var transformation: MarkdownVisualTransformation
    private val colorScheme = lightColorScheme()

    @Before
    fun setup() {
        transformation = MarkdownVisualTransformation(colorScheme)
    }

    @Test
    fun `filter preserves exact character count invariant`() {
        val input = AnnotatedString("# Título\n**Negrita** y *cursiva*\n- [ ] Tarea\n> Cita\n`código`")
        val result = transformation.filter(input)
        assertEquals(input.text.length, result.text.length)
        assertEquals(input.text, result.text.text)
    }

    @Test
    fun `filter applies bold style to double asterisks`() {
        val input = AnnotatedString("Hola **mundo** cruel")
        val result = transformation.filter(input)
        assertEquals(input.text.length, result.text.length)
        val styles = result.text.spanStyles
        assertTrue("Debe existir al menos un estilo aplicado", styles.isNotEmpty())
        val boldSpan = styles.find { it.item.fontWeight == FontWeight.Bold }
        assertTrue("Debe existir Span con FontWeight.Bold", boldSpan != null)
    }

    @Test
    fun `filter applies italic style to single asterisks and underscores`() {
        val input = AnnotatedString("Palabra *cursiva* y _otra_")
        val result = transformation.filter(input)
        val italicSpans = result.text.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
        assertEquals(2, italicSpans.size)
    }

    @Test
    fun `filter handles unclosed tokens without throwing exception`() {
        val input = AnnotatedString("**sin cerrar *otra # y nada mas")
        val result = transformation.filter(input)
        assertEquals(input.text.length, result.text.length)
    }
}
