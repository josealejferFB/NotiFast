package com.notifast.app.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.AquaGreen
import com.notifast.app.presentation.theme.color.MutedCrimson
import com.notifast.app.presentation.theme.color.Ruby
import com.notifast.app.presentation.theme.color.toColorOrAqua
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeUtilsTest {

    @Test
    fun `toSafeComposeColor with valid 6-char hex returns correct Color`() {
        val hex = "#9E1B32"
        val result = hex.toSafeComposeColor()
        assertEquals(MutedCrimson, result)
    }

    @Test
    fun `toSafeComposeColor with 6-char hex without prefix returns correct Color`() {
        val hex = "2A7B7B"
        val result = hex.toSafeComposeColor()
        assertEquals(AquaGreen, result)
    }

    @Test
    fun `toSafeComposeColor with valid 8-char hex returns correct Color with alpha`() {
        val hex = "#809E1B32"
        val result = hex.toSafeComposeColor()
        val expected = Color(0x809E1B32)
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
        val result = "".toSafeComposeColor(MutedCrimson)
        assertEquals(MutedCrimson, result)
    }

    @Test
    fun `toSafeComposeColor with blank string returns AquaGreen default`() {
        val result = "   ".toSafeComposeColor()
        assertEquals(AquaGreen, result)
    }

    @Test
    fun `toSafeComposeColor with null string returns default color`() {
        val nullStr: String? = null
        val result = nullStr.toSafeComposeColor(MutedCrimson)
        assertEquals(MutedCrimson, result)
    }

    @Test
    fun `toSafeComposeColor with invalid characters returns default color`() {
        val result = "#ZZZZZZ".toSafeComposeColor(AquaGreen)
        assertEquals(AquaGreen, result)
    }

    @Test
    fun `toColorOrAqua legacy extension works consistently`() {
        assertEquals(Color(0xFF9E1B32), "#9E1B32".toColorOrAqua())
        assertEquals(AquaGreen, "invalid".toColorOrAqua())
        assertEquals(AquaGreen, "".toColorOrAqua())
    }

    @Test
    fun `getAccessibleColor produces high-contrast visible colors in dark mode`() {
        val darkColor = ThemeUtils.getAccessibleColor("#111111", isDark = true)
        val hsl = ThemeUtils.rgbToHsl(darkColor.red, darkColor.green, darkColor.blue)
        // Lightness must be between 0.68 and 0.80 for dark mode high readability
        assertTrue("Dark mode lightness ${hsl[2]} should be >= 0.68", hsl[2] >= 0.67f)
        assertTrue("Dark mode lightness ${hsl[2]} should be <= 0.80", hsl[2] <= 0.81f)
        // Saturation must be calibrated between 0.65 and 0.75
        assertTrue("Dark mode saturation ${hsl[1]} should be >= 0.65", hsl[1] >= 0.64f)
        assertTrue("Dark mode saturation ${hsl[1]} should be <= 0.75", hsl[1] <= 0.76f)
    }

    @Test
    fun `getAccessibleColor produces non-washed-out colors in light mode`() {
        val lightColor = ThemeUtils.getAccessibleColor("#FFFFFF", isDark = false)
        val hsl = ThemeUtils.rgbToHsl(lightColor.red, lightColor.green, lightColor.blue)
        // Lightness must be between 0.30 and 0.42 in light mode to prevent unreadable light text
        assertTrue("Light mode lightness ${hsl[2]} should be >= 0.30", hsl[2] >= 0.29f)
        assertTrue("Light mode lightness ${hsl[2]} should be <= 0.42", hsl[2] <= 0.43f)
        // Saturation must be calibrated between 0.65 and 0.75
        assertTrue("Light mode saturation ${hsl[1]} should be >= 0.65", hsl[1] >= 0.64f)
        assertTrue("Light mode saturation ${hsl[1]} should be <= 0.75", hsl[1] <= 0.76f)
    }

    @Test
    fun `rgbToHsl and hslToColor roundtrip accuracy`() {
        val original = Color(0xFF9E1B32)
        val hsl = ThemeUtils.rgbToHsl(original.red, original.green, original.blue)
        val reconstructed = ThemeUtils.hslToColor(hsl[0], hsl[1], hsl[2], original.alpha)

        assertEquals(original.red, reconstructed.red, 0.02f)
        assertEquals(original.green, reconstructed.green, 0.02f)
        assertEquals(original.blue, reconstructed.blue, 0.02f)
    }

    @Test
    fun `ColorScheme isDark correctly detects dark and light backgrounds`() {
        val darkScheme = darkColorScheme(background = Color(0xFF0E131F))
        val lightScheme = lightColorScheme(background = Color(0xFFFAF9F6))
        assertTrue("Dark scheme should be identified as isDark=true", darkScheme.isDark)
        assertFalse("Light scheme should be identified as isDark=false", lightScheme.isDark)
    }
}
