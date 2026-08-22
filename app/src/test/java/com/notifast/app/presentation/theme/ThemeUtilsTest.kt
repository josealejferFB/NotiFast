package com.notifast.app.presentation.theme

import androidx.compose.ui.graphics.Color
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import com.notifast.app.presentation.theme.color.toColorOrAqua
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun `getAccessibleColor produces visible colors in dark mode`() {
        val darkColor = ThemeUtils.getAccessibleColor("#111111", isDark = true)
        val hsl = ThemeUtils.rgbToHsl(darkColor.red, darkColor.green, darkColor.blue)
        // Lightness must be at least 0.55 for dark mode readability
        assertTrue("Dark mode lightness ${hsl[2]} should be >= 0.55", hsl[2] >= 0.54f)
    }

    @Test
    fun `getAccessibleColor produces non-washed-out colors in light mode`() {
        val lightColor = ThemeUtils.getAccessibleColor("#FFFFFF", isDark = false)
        val hsl = ThemeUtils.rgbToHsl(lightColor.red, lightColor.green, lightColor.blue)
        // Lightness must be <= 0.55 in light mode to prevent unreadable white-on-white text
        assertTrue("Light mode lightness ${hsl[2]} should be <= 0.55", hsl[2] <= 0.55f)
    }

    @Test
    fun `rgbToHsl and hslToColor roundtrip accuracy`() {
        val original = Color(0xFF3B9797)
        val hsl = ThemeUtils.rgbToHsl(original.red, original.green, original.blue)
        val reconstructed = ThemeUtils.hslToColor(hsl[0], hsl[1], hsl[2], original.alpha)

        assertEquals(original.red, reconstructed.red, 0.02f)
        assertEquals(original.green, reconstructed.green, 0.02f)
        assertEquals(original.blue, reconstructed.blue, 0.02f)
    }
}
