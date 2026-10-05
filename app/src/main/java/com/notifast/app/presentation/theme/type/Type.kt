package com.notifast.app.presentation.theme.type

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notifast.app.R

// ============================================================================
// OFFLINE BUNDLED FONT FAMILIES (100% LOCAL STORAGE & NO NETWORK DEPENDENCY)
// ============================================================================

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

// ============================================================================
// NOTIFAST 3.0 TYPOGRAPHY SCALE (TACTILE STUDIO NOTEBOOK)
// ============================================================================

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.15).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

// ============================================================================
// ACADEMIC & NUMERIC MONOSPACE STYLES (TABULAR NUMBERS)
// ============================================================================

val MonoAcademicTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

val MonoAcademicLargeTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.25).sp,
    fontFeatureSettings = "tnum"
)

val MonoAcademicBoldTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

val MonoAcademicSmallTextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

/** Standard academic monospace for calculations, percentages and tables */
val Typography.monoAcademic: TextStyle
    get() = MonoAcademicTextStyle

/** Large academic monospace for GPA hero displays and summary metrics */
val Typography.monoAcademicLarge: TextStyle
    get() = MonoAcademicLargeTextStyle

/** Bold academic monospace for table headers, weights and critical alerts */
val Typography.monoAcademicBold: TextStyle
    get() = MonoAcademicBoldTextStyle

/** Small academic monospace for chips, badges and timestamp tags */
val Typography.monoAcademicSmall: TextStyle
    get() = MonoAcademicSmallTextStyle
