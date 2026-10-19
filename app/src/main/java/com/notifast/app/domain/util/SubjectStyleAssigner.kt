package com.notifast.app.domain.util

import kotlin.math.abs

/**
 * Assigns Material 3 accessible colors and semantic icons based on subject name heuristics.
 */
object SubjectStyleAssigner {

    private val PALETTE = listOf(
        "#4338CA", // Studio Indigo
        "#0369A1", // Slate Blue
        "#047857", // Emerald
        "#B45309", // Amber Gold
        "#7E22CE", // Deep Purple
        "#BF092F", // Ruby Crimson
        "#3B9797", // Aqua Green
        "#C2410C", // Terracotta Warm
        "#15803D"  // Forest Sage
    )

    data class SubjectStyle(
        val icon: String,
        val colorHex: String
    )

    fun assignStyle(subjectName: String): SubjectStyle {
        val clean = subjectName.trim().lowercase()

        val (icon, color) = when {
            clean.contains("comput") || clean.contains("sistema") ||
            clean.contains("software") || clean.contains("compilador") ||
            clean.contains("program") || clean.contains("redes") ||
            clean.contains("distribuid") || clean.contains("operativ") -> {
                "Computer" to "#4338CA"
            }
            clean.contains("matemat") || clean.contains("calcul") ||
            clean.contains("algebra") || clean.contains("estadist") ||
            clean.contains("fisic") || clean.contains("geometric") -> {
                "Calculate" to "#0369A1"
            }
            clean.contains("quimic") || clean.contains("biolog") ||
            clean.contains("laborator") || clean.contains("cienc") -> {
                "Science" to "#047857"
            }
            clean.contains("histor") || clean.contains("social") ||
            clean.contains("filosof") || clean.contains("critic") -> {
                "History" to "#B45309"
            }
            clean.contains("idiom") || clean.contains("ingles") ||
            clean.contains("lengua") -> {
                "Translate" to "#3B9797"
            }
            clean.contains("dibuj") || clean.contains("arte") ||
            clean.contains("dise") -> {
                "Palette" to "#7E22CE"
            }
            else -> {
                val index = abs(subjectName.hashCode()) % PALETTE.size
                "School" to PALETTE[index]
            }
        }

        return SubjectStyle(icon = icon, colorHex = color)
    }
}
