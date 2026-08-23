package com.notifast.app.presentation.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notifast.app.presentation.theme.type.Typography
import com.notifast.app.presentation.theme.type.monoAcademic
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.theme.type.monoAcademicLarge
import com.notifast.app.presentation.theme.type.monoAcademicSmall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying NotiFast 3.0 Typography Scale & Tabular Numeric Settings.
 */
class TypographyTest {

    @Test
    fun `monoAcademic has tnum font feature setting for tabular numbers`() {
        val style = Typography.monoAcademic
        assertNotNull(style)
        assertEquals("tnum", style.fontFeatureSettings)
        assertEquals(14.sp, style.fontSize)
    }

    @Test
    fun `monoAcademicLarge has bold weight, 28sp size and tnum setting`() {
        val style = Typography.monoAcademicLarge
        assertNotNull(style)
        assertEquals("tnum", style.fontFeatureSettings)
        assertEquals(28.sp, style.fontSize)
        assertEquals(FontWeight.Bold, style.fontWeight)
    }

    @Test
    fun `monoAcademicBold has bold weight, 14sp size and tnum setting`() {
        val style = Typography.monoAcademicBold
        assertNotNull(style)
        assertEquals("tnum", style.fontFeatureSettings)
        assertEquals(14.sp, style.fontSize)
        assertEquals(FontWeight.Bold, style.fontWeight)
    }

    @Test
    fun `monoAcademicSmall has 12sp size and tnum setting`() {
        val style = Typography.monoAcademicSmall
        assertNotNull(style)
        assertEquals("tnum", style.fontFeatureSettings)
        assertEquals(12.sp, style.fontSize)
    }

    @Test
    fun `editorial headlines have negative tracking for paper print visual rhythm`() {
        assertTrue(Typography.displayLarge.letterSpacing.value < 0f)
        assertTrue(Typography.displayMedium.letterSpacing.value < 0f)
        assertTrue(Typography.displaySmall.letterSpacing.value < 0f)
        assertTrue(Typography.headlineLarge.letterSpacing.value < 0f)
        assertTrue(Typography.headlineMedium.letterSpacing.value < 0f)
    }
}
