package com.notifast.app.presentation.feature.noteeditor.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartMarkdownFormatterTest {

    @Test
    fun `toggleFormat with empty selection inserts bold tokens and centers cursor`() {
        val initial = TextFieldValue("", selection = TextRange(0))
        val result = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.BOLD)

        assertEquals("****", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun `toggleFormat with cursor between empty bold tokens removes tokens`() {
        val initial = TextFieldValue("****", selection = TextRange(2))
        val result = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.BOLD)

        assertEquals("", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `toggleFormat wraps selected text with bold`() {
        val initial = TextFieldValue("selected", selection = TextRange(0, 8))
        val result = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.BOLD)

        assertEquals("**selected**", result.text)
    }

    @Test
    fun `toggleFormat unwraps selected text already in bold`() {
        val initial = TextFieldValue("**selected**", selection = TextRange(0, 12))
        val result = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.BOLD)

        assertEquals("selected", result.text)
    }

    @Test
    fun `toggleFormat unwraps text when selection is inside bold tokens`() {
        val initial = TextFieldValue("**selected**", selection = TextRange(2, 10))
        val result = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.BOLD)

        assertEquals("selected", result.text)
    }

    @Test
    fun `toggleFormat with italic centers cursor and unwraps`() {
        val empty = TextFieldValue("", selection = TextRange(0))
        val formatted = SmartMarkdownFormatter.toggleFormat(empty, FormatStyle.ITALIC)
        assertEquals("**", formatted.text)
        assertEquals(TextRange(1), formatted.selection)

        val toggledOff = SmartMarkdownFormatter.toggleFormat(formatted, FormatStyle.ITALIC)
        assertEquals("", toggledOff.text)
        assertEquals(TextRange(0), toggledOff.selection)

        val selected = TextFieldValue("hello", selection = TextRange(0, 5))
        val wrapped = SmartMarkdownFormatter.toggleFormat(selected, FormatStyle.ITALIC)
        assertEquals("*hello*", wrapped.text)

        val unwrapped = SmartMarkdownFormatter.toggleFormat(wrapped, FormatStyle.ITALIC)
        assertEquals("hello", unwrapped.text)
    }

    @Test
    fun `toggleFormat with latex centers cursor and toggles off`() {
        val empty = TextFieldValue("", selection = TextRange(0))
        val formatted = SmartMarkdownFormatter.toggleFormat(empty, FormatStyle.LATEX)
        assertEquals("$$$$", formatted.text)
        assertEquals(TextRange(2), formatted.selection)

        val toggledOff = SmartMarkdownFormatter.toggleFormat(formatted, FormatStyle.LATEX)
        assertEquals("", toggledOff.text)
        assertEquals(TextRange(0), toggledOff.selection)
    }

    @Test
    fun `toggleFormat with H1 toggles line prefix on and off`() {
        val initial = TextFieldValue("Heading", selection = TextRange(7))
        val formatted = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.H1)
        assertEquals("# Heading", formatted.text)

        val toggledOff = SmartMarkdownFormatter.toggleFormat(formatted, FormatStyle.H1)
        assertEquals("Heading", toggledOff.text)
    }

    @Test
    fun `toggleFormat replaces existing prefix when switching from H1 to H2`() {
        val initial = TextFieldValue("# Heading", selection = TextRange(9))
        val switched = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.H2)
        assertEquals("## Heading", switched.text)
    }

    @Test
    fun `toggleFormat with quote toggles line prefix`() {
        val initial = TextFieldValue("Quote text", selection = TextRange(5))
        val formatted = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.QUOTE)
        assertEquals("> Quote text", formatted.text)

        val toggledOff = SmartMarkdownFormatter.toggleFormat(formatted, FormatStyle.QUOTE)
        assertEquals("Quote text", toggledOff.text)
    }

    @Test
    fun `toggleFormat with checkbox and bullet replaces and toggles`() {
        val initial = TextFieldValue("Item", selection = TextRange(4))
        val bullet = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.BULLET)
        assertEquals("- Item", bullet.text)

        val checkbox = SmartMarkdownFormatter.toggleFormat(bullet, FormatStyle.CHECKBOX)
        assertEquals("- [ ] Item", checkbox.text)

        val toggledOff = SmartMarkdownFormatter.toggleFormat(checkbox, FormatStyle.CHECKBOX)
        assertEquals("Item", toggledOff.text)
    }

    @Test
    fun `detectActiveStyles detects active inline and line styles`() {
        val boldValue = TextFieldValue("Hola **mundo** cruel", selection = TextRange(8))
        val boldStyles = SmartMarkdownFormatter.detectActiveStyles(boldValue)
        assertTrue(boldStyles.contains(FormatStyle.BOLD))
        assertFalse(boldStyles.contains(FormatStyle.ITALIC))

        val h1Value = TextFieldValue("# Título principal", selection = TextRange(4))
        val h1Styles = SmartMarkdownFormatter.detectActiveStyles(h1Value)
        assertTrue(h1Styles.contains(FormatStyle.H1))
        assertFalse(h1Styles.contains(FormatStyle.H2))

        val taskValue = TextFieldValue("- [ ] Comprar leche", selection = TextRange(8))
        val taskStyles = SmartMarkdownFormatter.detectActiveStyles(taskValue)
        assertTrue(taskStyles.contains(FormatStyle.CHECKBOX))
        assertFalse(taskStyles.contains(FormatStyle.BULLET))

        val bulletValue = TextFieldValue("- Elemento de lista", selection = TextRange(4))
        val bulletStyles = SmartMarkdownFormatter.detectActiveStyles(bulletValue)
        assertTrue(bulletStyles.contains(FormatStyle.BULLET))
        assertFalse(bulletStyles.contains(FormatStyle.CHECKBOX))
    }

    @Test
    fun `handleSmartEnter on non-empty list item adds bullet on new line`() {
        val oldValue = TextFieldValue("- Item", selection = TextRange(6))
        val newValue = TextFieldValue("- Item\n", selection = TextRange(7))
        val result = SmartMarkdownFormatter.handleSmartEnter(oldValue, newValue)

        assertEquals("- Item\n- ", result.text)
        assertEquals(TextRange(9), result.selection)
    }

    @Test
    fun `handleSmartEnter on empty list item removes bullet and exits list`() {
        val oldValue = TextFieldValue("- ", selection = TextRange(2))
        val newValue = TextFieldValue("- \n", selection = TextRange(3))
        val result = SmartMarkdownFormatter.handleSmartEnter(oldValue, newValue)

        assertEquals("", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleSmartEnter on non-empty task item adds checkbox on new line`() {
        val oldValue = TextFieldValue("- [ ] Task", selection = TextRange(10))
        val newValue = TextFieldValue("- [ ] Task\n", selection = TextRange(11))
        val result = SmartMarkdownFormatter.handleSmartEnter(oldValue, newValue)

        assertEquals("- [ ] Task\n- [ ] ", result.text)
        assertEquals(TextRange(17), result.selection)
    }

    @Test
    fun `handleSmartEnter on empty task item removes checkbox and exits list`() {
        val oldValue = TextFieldValue("- [ ] ", selection = TextRange(6))
        val newValue = TextFieldValue("- [ ] \n", selection = TextRange(7))
        val result = SmartMarkdownFormatter.handleSmartEnter(oldValue, newValue)

        assertEquals("", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleSmartEnter on regular text preserves normal enter behavior`() {
        val oldValue = TextFieldValue("Hello", selection = TextRange(5))
        val newValue = TextFieldValue("Hello\n", selection = TextRange(6))
        val result = SmartMarkdownFormatter.handleSmartEnter(oldValue, newValue)

        assertEquals("Hello\n", result.text)
        assertEquals(TextRange(6), result.selection)
    }

    @Test
    fun `handleSmartEnter exits multi-line list cleanly`() {
        val oldValue = TextFieldValue("- Item 1\n- ", selection = TextRange(11))
        val newValue = TextFieldValue("- Item 1\n- \n", selection = TextRange(12))
        val result = SmartMarkdownFormatter.handleSmartEnter(oldValue, newValue)

        assertEquals("- Item 1\n", result.text)
        assertEquals(TextRange(9), result.selection)
    }
}
