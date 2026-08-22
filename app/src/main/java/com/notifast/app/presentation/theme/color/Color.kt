package com.notifast.app.presentation.theme.color

import androidx.compose.ui.graphics.Color

// COLORES CORPORATIVOS NOTIFAST
val Ruby = Color(0xFFBF092F)
val Aqua = Color(0xFF3B9797)

// Light
val RubyLight = Color(0xFFBF092F)
val AquaLight = Color(0xFF3B9797)
val SurfaceLight = Color(0xFFFAFAFA)
val OnSurfaceLight = Color(0xFF1C1B1F)

// Dark
val RubyDark = Color(0xFFCC5A68) // Muted Ruby
val AquaDark = Color(0xFF4A9696) // Muted Aqua
val SurfaceDark = Color(0xFF1B262C) // Slate
val OnSurfaceDark = Color(0xFFE6E1E5)

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
