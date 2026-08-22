package com.notifast.app.presentation.theme.color

import androidx.compose.ui.graphics.Color

// COLORES CORPORATIVOS NOTIFAST (Brand Core)
val Ruby = Color(0xFFBF092F)
val Aqua = Color(0xFF3B9797)

// Light Theme Tokens
val RubyLight = Color(0xFFBF092F)
val OnRubyLight = Color(0xFFFFFFFF)
val RubyContainerLight = Color(0xFFFFDAD9)
val OnRubyContainerLight = Color(0xFF41000B)

val AquaLight = Color(0xFF3B9797)
val OnAquaLight = Color(0xFFFFFFFF)
val AquaContainerLight = Color(0xFFBCEBEB)
val OnAquaContainerLight = Color(0xFF002020)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF64748B)
val OutlineLight = Color(0xFFCBD5E1)

// Dark Theme Tokens (Impeccable High-Contrast Dark Mode)
val RubyDark = Color(0xFFFFB3B8)
val OnRubyDark = Color(0xFF680015)
val RubyContainerDark = Color(0xFF920021)
val OnRubyContainerDark = Color(0xFFFFDAD9)

val AquaDark = Color(0xFF7DDADA)
val OnAquaDark = Color(0xFF003737)
val AquaContainerDark = Color(0xFF1E4E4E)
val OnAquaContainerDark = Color(0xFFBCEBEB)

val BackgroundDark = Color(0xFF10161A)
val OnBackgroundDark = Color(0xFFECEFF1)
val SurfaceDark = Color(0xFF1B242A)
val OnSurfaceDark = Color(0xFFECEFF1)
val SurfaceVariantDark = Color(0xFF25333C)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF3D4D59)

// Error Tokens
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

/**
 * Parses a hex string into a Jetpack Compose [Color] safely without crashing.
 * Supports #RGB, #RGBA, #RRGGBB, #AARRGGBB with or without leading '#'.
 * Returns [default] if parsing fails, or if string is null/blank.
 */
fun String?.toSafeComposeColor(default: Color = Aqua): Color {
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
fun String?.toColorOrAqua(): Color = toSafeComposeColor(default = Aqua)
