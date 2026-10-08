package com.notifast.app.presentation.atoms

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

class MarkdownVisualTransformation(
    private val colorScheme: ColorScheme
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val annotated = buildAnnotatedString {
            append(raw)
            val tokenDimStyle = SpanStyle(
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                fontSize = 13.sp
            )

            // Line-based formatting (H1, H2, H3, quotes, lists)
            var lineStart = 0
            val lines = raw.split("\n")
            for (line in lines) {
                val lineEnd = lineStart + line.length
                val trimmed = line.trimStart()
                val indent = line.length - trimmed.length

                when {
                    trimmed.startsWith("### ") -> {
                        addStyle(tokenDimStyle, lineStart + indent, lineStart + indent + 4)
                        addStyle(
                            SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary),
                            lineStart + indent + 4,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("## ") -> {
                        addStyle(tokenDimStyle, lineStart + indent, lineStart + indent + 3)
                        addStyle(
                            SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ruby),
                            lineStart + indent + 3,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("# ") -> {
                        addStyle(tokenDimStyle, lineStart + indent, lineStart + indent + 2)
                        addStyle(
                            SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = colorScheme.onSurface),
                            lineStart + indent + 2,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("> ") -> {
                        addStyle(SpanStyle(color = Aqua, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + 2)
                        addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic, color = colorScheme.onSurfaceVariant),
                            lineStart + indent + 2,
                            lineEnd
                        )
                    }
                    trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ", ignoreCase = true) -> {
                        addStyle(SpanStyle(color = colorScheme.primary, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + 6)
                    }
                    trimmed.startsWith("- ") -> {
                        addStyle(SpanStyle(color = colorScheme.primary, fontWeight = FontWeight.Bold), lineStart + indent, lineStart + indent + 2)
                    }
                }
                lineStart = lineEnd + 1
            }

            // Inline formatting: Bold (**text**)
            var i = 0
            while (i < raw.length) {
                if (raw.startsWith("**", i)) {
                    val end = raw.indexOf("**", i + 2)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 2)
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), i + 2, end)
                        addStyle(tokenDimStyle, end, end + 2)
                        i = end + 2
                        continue
                    }
                } else if (raw.startsWith("`", i)) {
                    val end = raw.indexOf("`", i + 1)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 1)
                        addStyle(
                            SpanStyle(fontFamily = FontFamily.Monospace, background = colorScheme.surfaceContainerHighest),
                            i + 1,
                            end
                        )
                        addStyle(tokenDimStyle, end, end + 1)
                        i = end + 1
                        continue
                    }
                } else if (raw[i] == '*' && i + 1 < raw.length && raw[i + 1] != ' ' && raw[i + 1] != '*') {
                    val end = raw.indexOf('*', i + 1)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 1)
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), i + 1, end)
                        addStyle(tokenDimStyle, end, end + 1)
                        i = end + 1
                        continue
                    }
                } else if (raw[i] == '_' && i + 1 < raw.length && raw[i + 1] != ' ') {
                    val end = raw.indexOf('_', i + 1)
                    if (end != -1) {
                        addStyle(tokenDimStyle, i, i + 1)
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), i + 1, end)
                        addStyle(tokenDimStyle, end, end + 1)
                        i = end + 1
                        continue
                    }
                }
                i++
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
