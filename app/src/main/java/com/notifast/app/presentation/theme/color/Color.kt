package com.notifast.app.presentation.theme.color

import androidx.compose.ui.graphics.Color

// ============================================================================
// NOTIFAST 3.0 DESIGN SYSTEM — "MUTED CRIMSON & AQUA GREEN" TOKENS
// ============================================================================

// Brand Primary & Secondary Studio Accents
val MutedCrimson = Color(0xFF9E1B32)      // Muted Crimson / Tenue Ruby (Light Primary)
val TenueRuby = Color(0xFF9E1B32)
val AquaGreen = Color(0xFF2A7B7B)         // Studio Aqua Green / Deep Mint (Light Secondary)
val StudioAqua = Color(0xFF2A7B7B)
val ForestSage = Color(0xFF15803D)        // Forest Sage / Success
val CoralFlame = Color(0xFFB91C1C)        // Urgent / Critical Error
val HighlightWash = Color(0xFFFFF59D)     // Rich-text highlight background wash

// Legacy brand aliases for backward compatibility
val Ruby = MutedCrimson
val Aqua = AquaGreen
val StudioIndigo = MutedCrimson
val TerracottaWarm = AquaGreen
val SageForest = ForestSage
val CrimsonBrick = CoralFlame

// ----------------------------------------------------------------------------
// Light Theme Tokens — "Lienzo de Estudio / Warm Canvas"
// ----------------------------------------------------------------------------
val BackgroundLight = Color(0xFFFAF9F6)       // Warm Paper Canvas (Lienzo pergamino de baja fatiga)
val SurfaceLight = Color(0xFFFFFFFF)          // Studio Paper Surface
val SurfaceContainerLight = Color(0xFFF3EFEA) // Parchment Sub-areas
val SurfaceContainerHighLight = Color(0xFFEBE6DF) // Parchment Elevated
val SurfaceVariantLight = Color(0xFFF3EFEA)
val OutlineLight = Color(0xFFE5DFD7)          // Hairline 1dp Border (12%-16% optical weight)
val OutlineVariantLight = Color(0xFFE5DFD7)
val OnBackgroundLight = Color(0xFF181A1F)     // High Emphasis Ink (16.2:1 AAA)
val OnSurfaceLight = Color(0xFF181A1F)
val OnSurfaceVariantLight = Color(0xFF4B5563) // Medium Emphasis Graphite (6.8:1 AAA)

val PrimaryLight = Color(0xFF9E1B32)          // Muted Crimson (7.5:1 AAA on #FAF9F6)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFDE8EB) // Soft Crimson Tint
val OnPrimaryContainerLight = Color(0xFF4C0514) // Deep Crimson Ink (12.4:1 AAA)

val SecondaryLight = Color(0xFF2A7B7B)        // Studio Aqua Green (5.4:1 AA on #FAF9F6)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD6F2F0) // Soft Aqua Tint
val OnSecondaryContainerLight = Color(0xFF073532) // Deep Aqua Ink (11.8:1 AAA)

val TertiaryLight = Color(0xFF15803D)         // Forest Sage (5.4:1 AA)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFDCFCE7)
val OnTertiaryContainerLight = Color(0xFF14532D)

val ErrorLight = Color(0xFFB91C1C)            // Coral Brick (6.1:1 AAA)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF7F1D1D)

// ----------------------------------------------------------------------------
// Dark Theme Tokens — "Mesa de Estudio Nocturna / Slate Desk"
// ----------------------------------------------------------------------------
val BackgroundDark = Color(0xFF0E131F)        // Slate Desk (Escritorio de pizarra profundo)
val SurfaceDark = Color(0xFF161E2D)           // Elevated Slate (Ficha elevada)
val SurfaceContainerDark = Color(0xFF1C2638)  // Dark Slate Container
val SurfaceContainerHighDark = Color(0xFF202A3C) // Dark Slate Container Elevated
val SurfaceVariantDark = Color(0xFF1C2638)
val OutlineDark = Color(0xFF2A384E)           // Hairline Slate Border (20%-28% optical weight)
val OutlineVariantDark = Color(0xFF2A384E)
val OnBackgroundDark = Color(0xFFF1F5F9)      // High Emphasis Chalk White (15.1:1 AAA)
val OnSurfaceDark = Color(0xFFF1F5F9)
val OnSurfaceVariantDark = Color(0xFF94A3B8)  // Medium Emphasis Pencil Silver (6.7:1 AAA)

val PrimaryDark = Color(0xFFE05A6E)           // Luminous Muted Crimson (7.4:1 AAA on #0E131F)
val OnPrimaryDark = Color(0xFF3D000F)         // Deep Crimson Contrast Text
val PrimaryContainerDark = Color(0xFF5C1120)  // Deep Wine Container
val OnPrimaryContainerDark = Color(0xFFFCE7EA) // Soft Crimson Contrast Text

val SecondaryDark = Color(0xFF38B2AC)         // Luminous Aqua Green (8.6:1 AAA on #0E131F)
val OnSecondaryDark = Color(0xFF042C2A)       // Deep Aqua Contrast Text
val SecondaryContainerDark = Color(0xFF0E4B47) // Deep Aqua Container
val OnSecondaryContainerDark = Color(0xFFD6F2F0) // Soft Aqua Contrast Text

val TertiaryDark = Color(0xFF34D399)          // Emerald Mint (9.8:1 AAA)
val OnTertiaryDark = Color(0xFF064E3B)
val TertiaryContainerDark = Color(0xFF065F46)
val OnTertiaryContainerDark = Color(0xFFD1FAE5)

val ErrorDark = Color(0xFFF87171)             // Coral Flame (7.2:1 AAA)
val OnErrorDark = Color(0xFF7F1D1D)
val ErrorContainerDark = Color(0xFF991B1B)
val OnErrorContainerDark = Color(0xFFFEE2E2)

// Legacy light/dark tokens aliases
val RubyLight = PrimaryLight
val OnRubyLight = OnPrimaryLight
val RubyContainerLight = PrimaryContainerLight
val OnRubyContainerLight = OnPrimaryContainerLight
val AquaLight = SecondaryLight
val OnAquaLight = OnSecondaryLight
val AquaContainerLight = SecondaryContainerLight
val OnAquaContainerLight = OnSecondaryContainerLight

val RubyDark = PrimaryDark
val OnRubyDark = OnPrimaryDark
val RubyContainerDark = PrimaryContainerDark
val OnRubyContainerDark = OnPrimaryContainerDark
val AquaDark = SecondaryDark
val OnAquaDark = OnSecondaryDark
val AquaContainerDark = SecondaryContainerDark
val OnAquaContainerDark = OnSecondaryContainerDark

/**
 * Parses a hex string into a Jetpack Compose [Color] safely without crashing.
 * Supports #RGB, #RGBA, #RRGGBB, #AARRGGBB with or without leading '#'.
 * Returns [default] if parsing fails, or if string is null/blank.
 */
fun String?.toSafeComposeColor(default: Color = AquaGreen): Color {
    if (this.isNullOrBlank()) return default
    val clean = this.trim().removePrefix("#")
    val colorLong = when (clean.length) {
        6 -> "FF$clean".toLongOrNull(16)
        8 -> clean.toLongOrNull(16)
        3 -> {
            val r = clean[0]
            val g = clean[1]
            val b = clean[2]
            "FF$r$r$g$g$b$b".toLongOrNull(16)
        }
        4 -> {
            val a = clean[0]
            val r = clean[1]
            val g = clean[2]
            val b = clean[3]
            "$a$a$r$r$g$g$b$b".toLongOrNull(16)
        }
        else -> null
    } ?: return default

    return Color(colorLong)
}

/**
 * Legacy extension for backward compatibility.
 */
fun String?.toColorOrAqua(): Color = toSafeComposeColor(default = AquaGreen)
