package com.notifast.app.domain.usecase

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ExportNotePdfUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val PAGE_WIDTH = 595 // A4 @ 72dpi
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 48f
        private const val LINE_HEIGHT = 22f
    }

    operator fun invoke(note: Note): Result<File> {
        return try {
            val document = PdfDocument()
            val titlePaint = Paint().apply {
                textSize = 20f
                isFakeBoldText = true
                color = AndroidColor.BLACK
            }
            val bodyPaint = Paint().apply {
                textSize = 12f
                color = AndroidColor.BLACK
            }
            val mutedPaint = Paint().apply {
                textSize = 11f
                color = AndroidColor.GRAY
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var y = MARGIN + 24f

            fun newPageIfNeeded(needed: Float) {
                if (y + needed > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN + 24f
                }
            }

            canvas.drawText(note.title, MARGIN, y, titlePaint)
            y += LINE_HEIGHT * 1.5f
            canvas.drawText("${note.subject}  ·  ${note.tags.joinToString(" #")}", MARGIN, y, mutedPaint)
            y += LINE_HEIGHT * 1.5f

            note.blocks.forEach { block ->
                when (block) {
                    is NoteBlock.Heading -> {
                        newPageIfNeeded(LINE_HEIGHT * 2)
                        y += LINE_HEIGHT * 0.5f
                        val paint = Paint().apply {
                            textSize = when (block.level) {
                                1 -> 18f; 2 -> 15f; else -> 13f
                            }
                            isFakeBoldText = true
                            color = AndroidColor.BLACK
                        }
                        canvas.drawText(block.text, MARGIN, y, paint)
                        y += LINE_HEIGHT * 1.3f
                    }
                    is NoteBlock.Paragraph -> {
                        block.text.lines().forEach { line ->
                            newPageIfNeeded(LINE_HEIGHT)
                            canvas.drawText(line, MARGIN, y, bodyPaint)
                            y += LINE_HEIGHT
                        }
                    }
                    is NoteBlock.Quote -> {
                        newPageIfNeeded(LINE_HEIGHT)
                        canvas.drawText("  « ${block.text} »", MARGIN, y, mutedPaint)
                        y += LINE_HEIGHT
                    }
                    is NoteBlock.BulletList -> {
                        block.items.forEachIndexed { index, item ->
                            newPageIfNeeded(LINE_HEIGHT)
                            val prefix = if (block.isOrdered) "${index + 1}." else "•"
                            canvas.drawText("  $prefix ${item.text}", MARGIN, y, bodyPaint)
                            y += LINE_HEIGHT
                        }
                    }
                    is NoteBlock.Checklist -> {
                        block.items.forEach { item ->
                            newPageIfNeeded(LINE_HEIGHT)
                            val mark = if (item.isChecked) "[✓]" else "[ ]"
                            canvas.drawText("  $mark ${item.text}", MARGIN, y, bodyPaint)
                            y += LINE_HEIGHT
                        }
                    }
                    is NoteBlock.MathBlock -> {
                        newPageIfNeeded(LINE_HEIGHT)
                        canvas.drawText("  ∑ ${block.source}", MARGIN, y, bodyPaint)
                        y += LINE_HEIGHT
                    }
                    is NoteBlock.Image -> {
                        newPageIfNeeded(LINE_HEIGHT)
                        canvas.drawText("  [Imagen: ${block.caption.ifBlank { "foto" }}]", MARGIN, y, mutedPaint)
                        y += LINE_HEIGHT
                    }
                    is NoteBlock.Divider -> {
                        newPageIfNeeded(LINE_HEIGHT)
                        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, mutedPaint)
                        y += LINE_HEIGHT
                    }
                }
            }

            document.finishPage(page)

            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, "nota_${note.id.take(8)}.pdf")
            FileOutputStream(file).use { out -> document.writeTo(out) }
            document.close()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
