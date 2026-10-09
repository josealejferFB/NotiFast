package com.notifast.app.presentation.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class TaskItem(val isChecked: Boolean, val text: String, val index: Int) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data class CodeBlock(val code: String, val language: String = "") : MarkdownBlock
    data class MathBlock(val formula: String) : MarkdownBlock
}

object MarkdownParser {

    private val TASK_REGEX = Regex("""^[-*]\s*\[([ xX]?)]\s*(.*)$""")

    fun parse(rawText: String): List<MarkdownBlock> {
        if (rawText.isBlank()) return emptyList()

        val lines = rawText.lines()
        val blocks = mutableListOf<MarkdownBlock>()
        var inCodeBlock = false
        var codeLanguage = ""
        val codeBuffer = StringBuilder()
        var taskIndex = 0

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    blocks.add(MarkdownBlock.CodeBlock(code = codeBuffer.toString().trimEnd(), language = codeLanguage))
                    codeBuffer.clear()
                    codeLanguage = ""
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                    codeLanguage = trimmed.removePrefix("```").trim()
                }
                continue
            }

            if (inCodeBlock) {
                codeBuffer.appendLine(line)
                continue
            }

            if (trimmed.isEmpty()) {
                continue
            }

            val taskMatch = TASK_REGEX.matchEntire(trimmed)
            when {
                trimmed.startsWith("### ") -> {
                    blocks.add(MarkdownBlock.Header(level = 3, text = trimmed.removePrefix("### ").trim()))
                }
                trimmed.startsWith("## ") -> {
                    blocks.add(MarkdownBlock.Header(level = 2, text = trimmed.removePrefix("## ").trim()))
                }
                trimmed.startsWith("# ") -> {
                    blocks.add(MarkdownBlock.Header(level = 1, text = trimmed.removePrefix("# ").trim()))
                }
                taskMatch != null -> {
                    val isChecked = taskMatch.groupValues[1].equals("x", ignoreCase = true)
                    val taskText = taskMatch.groupValues[2].trim()
                    blocks.add(MarkdownBlock.TaskItem(isChecked = isChecked, text = taskText, index = taskIndex++))
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    val content = if (trimmed.startsWith("* ")) trimmed.removePrefix("* ") else trimmed.removePrefix("- ")
                    blocks.add(MarkdownBlock.BulletItem(text = content.trim()))
                }
                trimmed.startsWith("$$") -> {
                    val formula = if (trimmed.endsWith("$$") && trimmed.length > 4) {
                        trimmed.removePrefix("$$").removeSuffix("$$").trim()
                    } else {
                        trimmed.removePrefix("$$").trim()
                    }
                    blocks.add(MarkdownBlock.MathBlock(formula))
                }
                trimmed.startsWith("> ") -> {
                    blocks.add(MarkdownBlock.Quote(text = trimmed.removePrefix("> ").trim()))
                }
                else -> {
                    blocks.add(MarkdownBlock.Paragraph(text = trimmed))
                }
            }
        }

        if (inCodeBlock) {
            blocks.add(MarkdownBlock.CodeBlock(code = codeBuffer.toString().trimEnd(), language = codeLanguage))
        }

        return blocks
    }

    fun toggleTaskCheckbox(rawText: String, targetIndex: Int, isChecked: Boolean): String {
        val lines = rawText.lines()
        var currentIndex = 0
        val newLines = lines.map { line ->
            val trimmed = line.trimStart()
            val match = TASK_REGEX.matchEntire(trimmed)
            if (match != null) {
                if (currentIndex == targetIndex) {
                    currentIndex++
                    val indent = line.substring(0, line.length - trimmed.length)
                    val rest = match.groupValues[2].trim()
                    val check = if (isChecked) "x" else " "
                    "$indent- [$check] $rest"
                } else {
                    currentIndex++
                    line
                }
            } else {
                line
            }
        }
        return newLines.joinToString("\n")
    }

    fun parseInlineFormatting(text: String): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                if (text.startsWith("**", i)) {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                        continue
                    }
                } else if (text[i] == '*' && (i + 1 < text.length) && text[i + 1] != ' ') {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                        continue
                    }
                } else if (text[i] == '_' && (i + 1 < text.length) && text[i + 1] != ' ') {
                    val end = text.indexOf('_', i + 1)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                        continue
                    }
                } else if (text[i] == '`') {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Gray.copy(alpha = 0.15f)))
                        append(" ${text.substring(i + 1, end)} ")
                        pop()
                        i = end + 1
                        continue
                    }
                }
                append(text[i])
                i++
            }
        }
    }
}

@Composable
fun NotiFastMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    onTaskCheckedChange: ((taskIndex: Int, isChecked: Boolean) -> Unit)? = null
) {
    val blocks = remember(content) { MarkdownParser.parse(content) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val (style, color) = when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold) to MaterialTheme.colorScheme.onSurface
                        2 -> MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold) to Ruby
                        else -> MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold) to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = MarkdownParser.parseInlineFormatting(block.text),
                        style = style,
                        color = color,
                        modifier = Modifier.padding(top = if (block.level == 1) 6.dp else 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = MarkdownParser.parseInlineFormatting(block.text),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "•",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = MarkdownParser.parseInlineFormatting(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is MarkdownBlock.TaskItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = onTaskCheckedChange != null) {
                                onTaskCheckedChange?.invoke(block.index, !block.isChecked)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = block.isChecked,
                            onCheckedChange = { isChecked -> onTaskCheckedChange?.invoke(block.index, isChecked) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = MarkdownParser.parseInlineFormatting(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (block.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is MarkdownBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                .padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = MarkdownParser.parseInlineFormatting(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = block.code,
                            fontFamily = com.notifast.app.presentation.theme.type.JetBrainsMonoFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is MarkdownBlock.MathBlock -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "∑",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = block.formula,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
