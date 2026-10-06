package com.notifast.app.domain.util

/**
 * Single strict HH:mm parser. Returns null on invalid input (never 0).
 */
object TimeParser {

    fun toMinutes(time: String): Int? {
        val parts = time.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    fun isValidRange(start: String, end: String): Boolean {
        val s = toMinutes(start) ?: return false
        val e = toMinutes(end) ?: return false
        return e > s
    }

    /** Half-open interval overlap [start, end). */
    fun overlaps(aStart: String, aEnd: String, bStart: String, bEnd: String): Boolean {
        val aS = toMinutes(aStart) ?: return false
        val aE = toMinutes(aEnd) ?: return false
        val bS = toMinutes(bStart) ?: return false
        val bE = toMinutes(bEnd) ?: return false
        if (aE <= aS || bE <= bS) return false
        return maxOf(aS, bS) < minOf(aE, bE)
    }
}
