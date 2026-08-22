package com.notifast.app.presentation.theme

import androidx.compose.ui.graphics.Color
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import com.notifast.app.presentation.theme.color.toColorOrAqua
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeUtilsTest {

    @Test
    fun `toSafeComposeColor with valid 6-char hex returns correct Color`() {
        val hex = "#BF092F"
        val result = hex.toSafeComposeColor()
        assertEquals(Ruby, result)
    }

    @Test
    fun `toSafeComposeColor with 6-char hex without prefix returns correct Color`() {
        val hex = "BF092F"
        val result = hex.toSafeComposeColor()
        assertEquals(Ruby, result)
    }

    @Test
    fun `toSafeComposeColor with valid 8-char hex returns correct Color with alpha`() {
        val hex = "#803B9797"
        val result = hex.toSafeComposeColor()
        val expected = Color(0x803B9797)
        assertEquals(expected, result)
    }

    @Test
    fun `toSafeComposeColor with valid 3-char shorthand returns expanded Color`() {
        val hex = "#F00"
        val result = hex.toSafeComposeColor()
        val expected = Color(0xFFFF0000)
        assertEquals(expected, result)
    }

    @Test
    fun `toSafeComposeColor with empty string returns default color`() {
        val result = "".toSafeComposeColor(Ruby)
        assertEquals(Ruby, result)
    }

    @Test
    fun `toSafeComposeColor with blank string returns Aqua default`() {
        val result = "   ".toSafeComposeColor()
        assertEquals(Aqua, result)
    }

    @Test
    fun `toSafeComposeColor with null string returns default color`() {
        val nullStr: String? = null
        val result = nullStr.toSafeComposeColor(Ruby)
        assertEquals(Ruby, result)
    }

    @Test
    fun `toSafeComposeColor with invalid characters returns default color`() {
        val result = "#ZZZZZZ".toSafeComposeColor(Aqua)
        assertEquals(Aqua, result)
    }

    @Test
    fun `toColorOrAqua legacy extension works consistently`() {
        assertEquals(Ruby, "#BF092F".toColorOrAqua())
        assertEquals(Aqua, "invalid".toColorOrAqua())
        assertEquals(Aqua, "".toColorOrAqua())
    }
}
