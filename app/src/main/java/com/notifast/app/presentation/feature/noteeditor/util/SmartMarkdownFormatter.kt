package com.notifast.app.presentation.feature.noteeditor.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

enum class FormatStyle {
    BOLD,
    ITALIC,
    QUOTE,
    H1,
    H2,
    CHECKBOX,
    BULLET,
    LATEX,
    DIVIDER
}

/**
 * Pure functional markdown formatting algebra for [TextFieldValue].
 * Handles wrapping, unwrapping, cursor placement, active style detection, and smart enter list continuation.
 */
object SmartMarkdownFormatter {

    private val KNOWN_LINE_PREFIXES = listOf(
        "- [ ] ",
        "- [x] ",
        "- [X] ",
        "## ",
        "# ",
        "> ",
        "- "
    )

    fun toggleFormat(value: TextFieldValue, style: FormatStyle): TextFieldValue {
        val text = value.text
        val selStart = value.selection.min
        val selEnd = value.selection.max

        return when (style) {
            FormatStyle.BOLD -> toggleInline(value, "**", "**")
            FormatStyle.ITALIC -> toggleItalic(value)
            FormatStyle.LATEX -> toggleInline(value, "$$", "$$")
            FormatStyle.H1 -> toggleLinePrefix(value, "# ")
            FormatStyle.H2 -> toggleLinePrefix(value, "## ")
            FormatStyle.QUOTE -> toggleLinePrefix(value, "> ")
            FormatStyle.CHECKBOX -> toggleLinePrefix(value, "- [ ] ")
            FormatStyle.BULLET -> toggleLinePrefix(value, "- ")
            FormatStyle.DIVIDER -> toggleDivider(value)
        }
    }

    fun detectActiveStyles(value: TextFieldValue): Set<FormatStyle> {
        val text = value.text
        val cursor = value.selection.start
        val active = mutableSetOf<FormatStyle>()

        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = if (lineStart <= lineEnd && lineEnd <= text.length) text.substring(lineStart, lineEnd) else ""
        val trimmed = line.trimStart()

        when {
            trimmed.startsWith("## ") -> active.add(FormatStyle.H2)
            trimmed.startsWith("# ") -> active.add(FormatStyle.H1)
        }

        if (trimmed.startsWith("> ")) active.add(FormatStyle.QUOTE)

        when {
            trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ", ignoreCase = true) -> active.add(FormatStyle.CHECKBOX)
            trimmed.startsWith("- ") -> active.add(FormatStyle.BULLET)
        }

        if (trimmed == "---" || line.trim() == "---") active.add(FormatStyle.DIVIDER)

        // Bold detection
        var i = 0
        while (i < line.length) {
            val start = line.indexOf("**", i)
            if (start == -1) break
            val end = line.indexOf("**", start + 2)
            if (end != -1) {
                if (cursor in (lineStart + start)..(lineStart + end + 2)) {
                    active.add(FormatStyle.BOLD)
                }
                i = end + 2
            } else {
                break
            }
        }

        // LaTeX detection
        i = 0
        while (i < line.length) {
            val start = line.indexOf("$$", i)
            if (start == -1) break
            val end = line.indexOf("$$", start + 2)
            if (end != -1) {
                if (cursor in (lineStart + start)..(lineStart + end + 2)) {
                    active.add(FormatStyle.LATEX)
                }
                i = end + 2
            } else {
                break
            }
        }

        // Italic detection (single asterisk, not double)
        i = 0
        while (i < line.length) {
            if (line[i] == '*' && (i + 1 == line.length || line[i + 1] != '*') && (i == 0 || line[i - 1] != '*')) {
                val end = findSingleAsterisk(line, i + 1)
                if (end != -1) {
                    if (cursor in (lineStart + i)..(lineStart + end + 1)) {
                        active.add(FormatStyle.ITALIC)
                    }
                    i = end + 1
                    continue
                }
            }
            i++
        }

        return active
    }

    fun handleSmartEnter(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
        if (oldValue.selection.min != oldValue.selection.max) return newValue
        if (newValue.text.length != oldValue.text.length + 1) return newValue
        val cursor = newValue.selection.start
        if (cursor <= 0 || cursor > newValue.text.length) return newValue
        val newlinePos = cursor - 1
        if (newValue.text[newlinePos] != '\n') return newValue

        val lineStart = newValue.text.lastIndexOf('\n', newlinePos - 1).let { if (it == -1) 0 else it + 1 }
        val prevLine = newValue.text.substring(lineStart, newlinePos)
        val indent = prevLine.takeWhile { it == ' ' || it == '\t' }
        val trimmed = prevLine.trim()

        // Empty task item
        if (trimmed.equals("- [ ]", ignoreCase = true) || trimmed.equals("- [x]", ignoreCase = true)) {
            val before = newValue.text.substring(0, lineStart)
            val after = newValue.text.substring(newlinePos + 1)
            val newText = before + after
            val newCursor = lineStart.coerceAtMost(newText.length)
            return TextFieldValue(newText, selection = TextRange(newCursor))
        }

        // Empty bullet item
        if (trimmed == "-") {
            val before = newValue.text.substring(0, lineStart)
            val after = newValue.text.substring(newlinePos + 1)
            val newText = before + after
            val newCursor = lineStart.coerceAtMost(newText.length)
            return TextFieldValue(newText, selection = TextRange(newCursor))
        }

        // Non-empty task item
        val isTaskItem = trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ", ignoreCase = true)
        if (isTaskItem) {
            val nextPrefix = "$indent- [ ] "
            val before = newValue.text.substring(0, newlinePos + 1)
            val after = newValue.text.substring(newlinePos + 1)
            val newText = before + nextPrefix + after
            val newCursor = newlinePos + 1 + nextPrefix.length
            return TextFieldValue(newText, selection = TextRange(newCursor))
        }

        // Non-empty bullet item
        val isBulletItem = trimmed.startsWith("- ")
        if (isBulletItem) {
            val nextPrefix = "$indent- "
            val before = newValue.text.substring(0, newlinePos + 1)
            val after = newValue.text.substring(newlinePos + 1)
            val newText = before + nextPrefix + after
            val newCursor = newlinePos + 1 + nextPrefix.length
            return TextFieldValue(newText, selection = TextRange(newCursor))
        }

        return newValue
    }

    private fun toggleInline(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
        val text = value.text
        val selStart = value.selection.min
        val selEnd = value.selection.max

        if (selStart < selEnd) {
            val selected = text.substring(selStart, selEnd)
            // Case 1: selection already contains the tokens
            if (selected.length >= prefix.length + suffix.length &&
                selected.startsWith(prefix) && selected.endsWith(suffix)
            ) {
                val unwrapped = selected.substring(prefix.length, selected.length - suffix.length)
                val newText = text.substring(0, selStart) + unwrapped + text.substring(selEnd)
                return TextFieldValue(newText, selection = TextRange(selStart, selStart + unwrapped.length))
            }

            // Case 2: outer text surrounds selection with tokens
            val hasPrefixBefore = selStart >= prefix.length && text.substring(selStart - prefix.length, selStart) == prefix
            val hasSuffixAfter = selEnd + suffix.length <= text.length && text.substring(selEnd, selEnd + suffix.length) == suffix
            if (hasPrefixBefore && hasSuffixAfter) {
                val newText = text.substring(0, selStart - prefix.length) + selected + text.substring(selEnd + suffix.length)
                val newStart = selStart - prefix.length
                return TextFieldValue(newText, selection = TextRange(newStart, newStart + selected.length))
            }

            // Case 3: wrap selection
            val wrapped = prefix + selected + suffix
            val newText = text.substring(0, selStart) + wrapped + text.substring(selEnd)
            return TextFieldValue(newText, selection = TextRange(selStart + prefix.length, selStart + prefix.length + selected.length))
        } else {
            val cursor = selStart
            // Toggle off between empty tokens
            if (cursor >= prefix.length && cursor + suffix.length <= text.length &&
                text.substring(cursor - prefix.length, cursor) == prefix &&
                text.substring(cursor, cursor + suffix.length) == suffix
            ) {
                val newText = text.substring(0, cursor - prefix.length) + text.substring(cursor + suffix.length)
                return TextFieldValue(newText, selection = TextRange(cursor - prefix.length))
            }

            // Insert empty tokens and place cursor in middle
            val newText = text.substring(0, cursor) + prefix + suffix + text.substring(cursor)
            return TextFieldValue(newText, selection = TextRange(cursor + prefix.length))
        }
    }

    private fun toggleItalic(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val selStart = value.selection.min
        val selEnd = value.selection.max

        if (selStart < selEnd) {
            val selected = text.substring(selStart, selEnd)
            if (selected.length >= 2 && selected.startsWith("*") && !selected.startsWith("**") &&
                selected.endsWith("*") && !selected.endsWith("**")
            ) {
                val unwrapped = selected.substring(1, selected.length - 1)
                val newText = text.substring(0, selStart) + unwrapped + text.substring(selEnd)
                return TextFieldValue(newText, selection = TextRange(selStart, selStart + unwrapped.length))
            }

            val hasPrefixBefore = selStart >= 1 && text[selStart - 1] == '*' && (selStart < 2 || text[selStart - 2] != '*')
            val hasSuffixAfter = selEnd < text.length && text[selEnd] == '*' && (selEnd + 1 >= text.length || text[selEnd + 1] != '*')
            if (hasPrefixBefore && hasSuffixAfter) {
                val newText = text.substring(0, selStart - 1) + selected + text.substring(selEnd + 1)
                val newStart = selStart - 1
                return TextFieldValue(newText, selection = TextRange(newStart, newStart + selected.length))
            }

            val wrapped = "*$selected*"
            val newText = text.substring(0, selStart) + wrapped + text.substring(selEnd)
            return TextFieldValue(newText, selection = TextRange(selStart + 1, selStart + 1 + selected.length))
        } else {
            val cursor = selStart
            val isBetweenSingleAsterisks = cursor >= 1 && cursor < text.length &&
                text[cursor - 1] == '*' && text[cursor] == '*' &&
                (cursor < 2 || text[cursor - 2] != '*') &&
                (cursor + 1 >= text.length || text[cursor + 1] != '*')

            if (isBetweenSingleAsterisks) {
                val newText = text.substring(0, cursor - 1) + text.substring(cursor + 1)
                return TextFieldValue(newText, selection = TextRange(cursor - 1))
            }

            val newText = text.substring(0, cursor) + "**" + text.substring(cursor)
            return TextFieldValue(newText, selection = TextRange(cursor + 1))
        }
    }

    private fun toggleLinePrefix(value: TextFieldValue, targetPrefix: String): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        // Check if line already has targetPrefix
        val matchesTarget = when (targetPrefix) {
            "- [ ] " -> line.startsWith("- [ ] ") || line.startsWith("- [x] ", ignoreCase = true)
            "# " -> line.startsWith("# ") && !line.startsWith("## ")
            else -> line.startsWith(targetPrefix)
        }

        if (matchesTarget) {
            val prefixLen = if (targetPrefix == "- [ ] " && line.startsWith("- [x] ", ignoreCase = true)) 6 else targetPrefix.length
            val newText = text.substring(0, lineStart) + text.substring(lineStart + prefixLen)
            val newCursor = if (cursor <= lineStart + prefixLen) lineStart else cursor - prefixLen
            return TextFieldValue(newText, selection = TextRange(newCursor))
        }

        // Check if line starts with any other known prefix
        val existingPrefix = KNOWN_LINE_PREFIXES.find { line.startsWith(it) }
        if (existingPrefix != null) {
            val newText = text.substring(0, lineStart) + targetPrefix + text.substring(lineStart + existingPrefix.length)
            val newCursor = if (cursor <= lineStart + existingPrefix.length) {
                lineStart + targetPrefix.length
            } else {
                cursor - existingPrefix.length + targetPrefix.length
            }
            return TextFieldValue(newText, selection = TextRange(newCursor))
        }

        // Prepend target prefix
        val newText = text.substring(0, lineStart) + targetPrefix + text.substring(lineStart)
        val newCursor = cursor + targetPrefix.length
        return TextFieldValue(newText, selection = TextRange(newCursor))
    }

    private fun toggleDivider(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val cursor = value.selection.start
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        if (line.trim() == "---") {
            val before = text.substring(0, lineStart)
            val after = if (lineEnd < text.length && text[lineEnd] == '\n') text.substring(lineEnd + 1) else text.substring(lineEnd)
            val newText = before + after
            return TextFieldValue(newText, selection = TextRange(lineStart.coerceAtMost(newText.length)))
        }

        return if (line.isEmpty()) {
            val newText = text.substring(0, lineStart) + "---\n" + text.substring(lineStart)
            TextFieldValue(newText, selection = TextRange(lineStart + 4))
        } else {
            val newText = text.substring(0, cursor) + "\n---\n" + text.substring(cursor)
            TextFieldValue(newText, selection = TextRange(cursor + 5))
        }
    }

    private fun findSingleAsterisk(text: String, fromIndex: Int): Int {
        var j = fromIndex
        while (j < text.length) {
            if (text[j] == '*' && (j + 1 == text.length || text[j + 1] != '*') && text[j - 1] != '*') {
                return j
            }
            j++
        }
        return -1
    }
}
