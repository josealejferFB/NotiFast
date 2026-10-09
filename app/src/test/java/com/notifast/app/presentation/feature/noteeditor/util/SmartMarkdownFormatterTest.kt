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
    fun `toggleFormat unwraps bold when cursor is inside bold text without selection`() {
        val initial = TextFieldValue("Hola **mundo** cruel", selection = TextRange(8))
        val result = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.BOLD)

        assertEquals("Hola mundo cruel", result.text)
        assertEquals(TextRange(6), result.selection)
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

        val insideItalic = TextFieldValue("Hola *mundo* fin", selection = TextRange(8))
        val unwrappedInside = SmartMarkdownFormatter.toggleFormat(insideItalic, FormatStyle.ITALIC)
        assertEquals("Hola mundo fin", unwrappedInside.text)

        val selected = TextFieldValue("hello", selection = TextRange(0, 5))
        val wrapped = SmartMarkdownFormatter.toggleFormat(selected, FormatStyle.ITALIC)
        assertEquals("*hello*", wrapped.text)

        val unwrapped = SmartMarkdownFormatter.toggleFormat(wrapped, FormatStyle.ITALIC)
        assertEquals("hello", unwrapped.text)
    }

    @Test
    fun `toggleFormat with latex centers cursor and unwraps`() {
        val empty = TextFieldValue("", selection = TextRange(0))
        val formatted = SmartMarkdownFormatter.toggleFormat(empty, FormatStyle.LATEX)
        assertEquals("$$$$", formatted.text)
        assertEquals(TextRange(2), formatted.selection)

        val toggledOff = SmartMarkdownFormatter.toggleFormat(formatted, FormatStyle.LATEX)
        assertEquals("", toggledOff.text)
        assertEquals(TextRange(0), toggledOff.selection)

        val insideMath = TextFieldValue("E = \$\$mc^2\$\$ formula", selection = TextRange(8))
        val unwrappedMath = SmartMarkdownFormatter.toggleFormat(insideMath, FormatStyle.LATEX)
        assertEquals("E = mc^2 formula", unwrappedMath.text)
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
    fun `toggleFormat with checkbox cycles unchecked, checked, and removed`() {
        val initial = TextFieldValue("Item", selection = TextRange(4))
        val unchecked = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.CHECKBOX)
        assertEquals("- [ ] Item", unchecked.text)

        val checked = SmartMarkdownFormatter.toggleFormat(unchecked, FormatStyle.CHECKBOX)
        assertEquals("- [x] Item", checked.text)

        val removed = SmartMarkdownFormatter.toggleFormat(checked, FormatStyle.CHECKBOX)
        assertEquals("Item", removed.text)
    }

    @Test
    fun `toggleFormat with indented checkbox preserves indent through cycle`() {
        val initial = TextFieldValue("  Item", selection = TextRange(6))
        val unchecked = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.CHECKBOX)
        assertEquals("  - [ ] Item", unchecked.text)

        val checked = SmartMarkdownFormatter.toggleFormat(unchecked, FormatStyle.CHECKBOX)
        assertEquals("  - [x] Item", checked.text)

        val removed = SmartMarkdownFormatter.toggleFormat(checked, FormatStyle.CHECKBOX)
        assertEquals("  Item", removed.text)
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

    @Test
    fun `toggleFormat with cursor at 0 on text starting with newline does not throw exception`() {
        val initial = TextFieldValue("\nhello", selection = TextRange(0))
        val styles = listOf(
            FormatStyle.H1,
            FormatStyle.H2,
            FormatStyle.CHECKBOX,
            FormatStyle.BULLET,
            FormatStyle.QUOTE,
            FormatStyle.DIVIDER
        )
        for (style in styles) {
            val result = SmartMarkdownFormatter.toggleFormat(initial, style)
            assertTrue("Text should not be empty after toggling $style", result.text.isNotEmpty())
        }
    }

    @Test
    fun `toggleFormat switching from checkbox to bullet yields bullet list item without corrupting line`() {
        val checkboxValue = TextFieldValue("- [ ] Item", selection = TextRange(10))
        val result = SmartMarkdownFormatter.toggleFormat(checkboxValue, FormatStyle.BULLET)

        assertEquals("- Item", result.text)
    }

    @Test
    fun `toggleFormat with divider inserts horizontal rule and toggles off`() {
        val empty = TextFieldValue("", selection = TextRange(0))
        val withDivider = SmartMarkdownFormatter.toggleFormat(empty, FormatStyle.DIVIDER)
        assertEquals("---\n", withDivider.text)

        val toggledOff = SmartMarkdownFormatter.toggleFormat(withDivider, FormatStyle.DIVIDER)
        assertEquals("", toggledOff.text)

        val textValue = TextFieldValue("hello", selection = TextRange(5))
        val withDividerAfterText = SmartMarkdownFormatter.toggleFormat(textValue, FormatStyle.DIVIDER)
        assertEquals("hello\n---\n", withDividerAfterText.text)
    }

    @Test
    fun `toggleFormat with compact no-space checkbox cycles unchecked to checked and removed`() {
        val initial = TextFieldValue("-[ ] Item", selection = TextRange(8))
        val checked = SmartMarkdownFormatter.toggleFormat(initial, FormatStyle.CHECKBOX)
        assertEquals("- [x] Item", checked.text)

        val removed = SmartMarkdownFormatter.toggleFormat(checked, FormatStyle.CHECKBOX)
        assertEquals("Item", removed.text)
    }

    @Test
    fun `detectActiveStyles detects checkbox with compact no-space syntax`() {
        val compactUnchecked = TextFieldValue("-[ ] Tarea", selection = TextRange(6))
        assertTrue(SmartMarkdownFormatter.detectActiveStyles(compactUnchecked).contains(FormatStyle.CHECKBOX))

        val compactChecked = TextFieldValue("-[x] Tarea", selection = TextRange(6))
        assertTrue(SmartMarkdownFormatter.detectActiveStyles(compactChecked).contains(FormatStyle.CHECKBOX))
    }

    @Test
    fun `handleSmartEnter with compact no-space checkbox continues list and clears empty`() {
        // Non-empty compact checkbox: continues list
        val oldNonEmpty = TextFieldValue("-[ ] Tarea", selection = TextRange(10))
        val newNonEmpty = TextFieldValue("-[ ] Tarea\n", selection = TextRange(11))
        val continued = SmartMarkdownFormatter.handleSmartEnter(oldNonEmpty, newNonEmpty)
        assertEquals("-[ ] Tarea\n- [ ] ", continued.text)

        // Empty compact checkbox: clears checkbox
        val oldEmpty = TextFieldValue("-[ ] ", selection = TextRange(5))
        val newEmpty = TextFieldValue("-[ ] \n", selection = TextRange(6))
        val cleared = SmartMarkdownFormatter.handleSmartEnter(oldEmpty, newEmpty)
        assertEquals("", cleared.text)
    }
}

