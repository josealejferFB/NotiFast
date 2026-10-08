package com.notifast.app.domain.util

import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle

/**
 * Pure span algebra over [TextSpan] ranges. Ranges are [start, end) half-open.
 */
object TextSpanOps {

    fun toggleStyle(
        spans: List<TextSpan>,
        start: Int,
        end: Int,
        style: TextStyle
    ): List<TextSpan> {
        if (start >= end) return spans
        val covering = spans.filter { it.start < end && it.end > start && style in it.styles }
        val next = if (covering.isEmpty()) {
            addStyle(spans, start, end, style)
        } else {
            removeStyle(spans, start, end, style)
        }
        return normalize(next)
    }

    private fun addStyle(
        spans: List<TextSpan>,
        start: Int,
        end: Int,
        style: TextStyle
    ): List<TextSpan> {
        val result = mutableListOf<TextSpan>()
        for (span in spans) {
            if (span.end <= start || span.start >= end) {
                result += span
                continue
            }
            if (span.start < start) result += span.copy(end = start)
            val midStart = maxOf(span.start, start)
            val midEnd = minOf(span.end, end)
            if (midStart < midEnd) {
                result += TextSpan(midStart, midEnd, span.styles + style)
            }
            if (span.end > end) result += span.copy(start = end)
        }
        // Fill gaps inside [start, end) that no existing span covered
        val covered = result
            .filter { it.start < end && it.end > start && style in it.styles }
            .map { maxOf(it.start, start) to minOf(it.end, end) }
            .sortedBy { it.first }
        var pos = start
        for ((s, e) in covered) {
            if (pos < s) result += TextSpan(pos, s, setOf(style))
            pos = maxOf(pos, e)
        }
        if (pos < end) result += TextSpan(pos, end, setOf(style))
        return normalize(result)
    }

    private fun removeStyle(
        spans: List<TextSpan>,
        start: Int,
        end: Int,
        style: TextStyle
    ): List<TextSpan> {
        val result = mutableListOf<TextSpan>()
        for (span in spans) {
            if (span.start >= end || span.end <= start || style !in span.styles) {
                result += span
                continue
            }
            if (span.start < start) {
                result += span.copy(end = start)
            }
            if (span.end > end) {
                result += span.copy(start = end)
            }
            val remaining = span.styles - style
            if (remaining.isNotEmpty() && span.start < end && span.end > start) {
                val midStart = maxOf(span.start, start)
                val midEnd = minOf(span.end, end)
                if (midStart < midEnd) result += TextSpan(midStart, midEnd, remaining)
            }
        }
        return result
    }

    fun normalize(spans: List<TextSpan>): List<TextSpan> {
        val sorted = spans
            .filter { it.start < it.end && it.styles.isNotEmpty() }
            .sortedWith(compareBy({ it.start }, { it.end }))
        if (sorted.isEmpty()) return emptyList()
        val merged = mutableListOf<TextSpan>()
        var current = sorted.first()
        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (current.end == next.start && current.styles == next.styles) {
                current = current.copy(end = next.end)
            } else {
                merged += current
                current = next
            }
        }
        merged += current
        return merged
    }

    /**
     * Re-map span boundaries after a text edit.
     * [editStart] is the caret/anchor where the edit occurred.
     * [oldLength] / [newLength] are the replaced and inserted lengths (0 for pure insert/delete).
     */
    fun adjustForEdit(
        spans: List<TextSpan>,
        editStart: Int,
        oldLength: Int,
        newLength: Int
    ): List<TextSpan> {
        if (spans.isEmpty()) return emptyList()
        val delta = newLength - oldLength
        val editEndOld = editStart + oldLength
        val adjusted = spans.mapNotNull { span ->
            var start = span.start
            var end = span.end
            when {
                span.start >= editEndOld -> {
                    start += delta
                    end += delta
                }
                span.end <= editStart -> {
                    // entirely before edit — unchanged
                }
                else -> {
                    // overlaps the edit region
                    if (oldLength > 0 && newLength == 0 && span.start >= editStart && span.end <= editEndOld) {
                        return@mapNotNull null
                    }
                    if (start > editStart) {
                        start = editStart + newLength
                    }
                    if (end >= editEndOld) {
                        end += delta
                    } else {
                        end = editStart + newLength
                    }
                    if (start > end) {
                        start = editStart
                        end = editStart + newLength
                    }
                }
            }
            if (end <= start) null else span.copy(start = start, end = end)
        }
        return normalize(adjusted)
    }

    /** Styles that fully cover the [start, end) range (union across covering spans). */
    fun stylesAt(spans: List<TextSpan>, start: Int, end: Int): Set<TextStyle> {
        if (start >= end) return emptySet()
        val covering = spans.filter { it.start <= start && it.end >= end }
        if (covering.isEmpty()) return emptySet()
        return covering.flatMap { it.styles }.toSet()
    }
}
