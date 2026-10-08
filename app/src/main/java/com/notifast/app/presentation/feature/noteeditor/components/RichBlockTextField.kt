package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle as NoteTextStyle
import com.notifast.app.domain.util.TextSpanOps
import com.notifast.app.presentation.theme.color.HighlightWash

/**
 * Pure mapper: [TextSpan] list → [AnnotatedString] with SpanStyles applied.
 * Name is unique (R4) to avoid shadowing `androidx.compose.ui.text.buildAnnotatedString`.
 */
fun applyNoteSpans(text: String, spans: List<TextSpan>): AnnotatedString =
    buildAnnotatedString {
        append(text)
        spans.forEach { span ->
            val start = span.start.coerceIn(0, text.length)
            val end = span.end.coerceIn(0, text.length)
            if (start >= end) return@forEach
            span.styles.forEach { style ->
                addStyle(style.toSpanStyle(), start, end)
            }
        }
    }

private fun NoteTextStyle.toSpanStyle(): SpanStyle = when (this) {
    NoteTextStyle.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
    NoteTextStyle.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
    NoteTextStyle.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
    NoteTextStyle.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    NoteTextStyle.HIGHLIGHT -> SpanStyle(background = HighlightWash)
}

/**
 * Editable text field that renders and edits [TextSpan] styles over the text.
 *
 * R1: span indices are re-mapped via [TextSpanOps.adjustForEdit] on every text change.
 * R2: [TextFieldValue] is held in an un-keyed `remember` so keystrokes don't reset the caret.
 */
@Composable
fun RichBlockTextField(
    value: String,
    spans: List<TextSpan>,
    onValueChange: (String) -> Unit,
    onSpansChange: (List<TextSpan>) -> Unit,
    onFocus: () -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    textStyle: TextStyle,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    // R2: un-keyed state — only resync when parent sends a genuinely different text.
    var fieldValue by remember { mutableStateOf(TextFieldValue(text = value)) }
    if (value != fieldValue.text) {
        fieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
    }
    val annotated = remember(fieldValue.text, spans) { applyNoteSpans(fieldValue.text, spans) }

    BasicTextField(
        value = fieldValue.copy(annotatedString = annotated),
        onValueChange = { new ->
            val oldText = fieldValue.text
            val newText = new.text
            if (newText != oldText) {
                val editStart = findEditStart(oldText, newText)
                val suffix = commonSuffix(oldText, newText, editStart)
                val oldLen = oldText.length - editStart - suffix
                val newLen = newText.length - editStart - suffix
                val adjusted = TextSpanOps.adjustForEdit(spans, editStart, oldLen, newLen)
                fieldValue = new
                onValueChange(newText)
                onSpansChange(adjusted)
            } else {
                fieldValue = new
            }
            onSelectionChange(new.selection.min, new.selection.max)
        },
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier.onFocusChanged { if (it.isFocused) onFocus() },
        decorationBox = { inner ->
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    placeholder,
                    style = textStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
            inner()
        }
    )
}

internal fun findEditStart(a: String, b: String): Int {
    val max = minOf(a.length, b.length)
    var i = 0
    while (i < max && a[i] == b[i]) i++
    return i
}

internal fun commonSuffix(a: String, b: String, editStart: Int): Int {
    var i = 0
    while (i < a.length - editStart && i < b.length - editStart &&
        a[a.length - 1 - i] == b[b.length - 1 - i]
    ) i++
    return i
}
