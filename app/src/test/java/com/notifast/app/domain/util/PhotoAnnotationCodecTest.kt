package com.notifast.app.domain.util

import com.notifast.app.data.model.AnnotationText
import com.notifast.app.data.model.Arrow
import com.notifast.app.data.model.DrawTool
import com.notifast.app.data.model.PhotoAnnotation
import com.notifast.app.data.model.Point
import com.notifast.app.data.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoAnnotationCodecTest {

    @Test
    fun `round trip preserves empty annotation`() {
        val original = PhotoAnnotation()
        val decoded = PhotoAnnotationCodec.fromJson(PhotoAnnotationCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves stroke with points`() {
        val original = PhotoAnnotation(
            strokes = listOf(
                Stroke(
                    points = listOf(Point(0f, 0f), Point(10f, 20f), Point(30f, 40f)),
                    color = -0x1000000,
                    width = 5f,
                    tool = DrawTool.PEN,
                    alpha = 1f
                )
            )
        )
        val decoded = PhotoAnnotationCodec.fromJson(PhotoAnnotationCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves highlighter stroke`() {
        val original = PhotoAnnotation(
            strokes = listOf(
                Stroke(
                    points = listOf(Point(5f, 5f), Point(15f, 15f)),
                    color = -0x100,
                    width = 12f,
                    tool = DrawTool.HIGHLIGHTER,
                    alpha = 0.4f
                )
            )
        )
        val decoded = PhotoAnnotationCodec.fromJson(PhotoAnnotationCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves annotation text`() {
        val original = PhotoAnnotation(
            texts = listOf(
                AnnotationText(x = 50f, y = 100f, text = "importante", color = -0xff0001, sizeSp = 16f)
            )
        )
        val decoded = PhotoAnnotationCodec.fromJson(PhotoAnnotationCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves arrow`() {
        val original = PhotoAnnotation(
            arrows = listOf(
                Arrow(startX = 10f, startY = 10f, endX = 50f, endY = 50f, color = -0x1000000, width = 3f)
            )
        )
        val decoded = PhotoAnnotationCodec.fromJson(PhotoAnnotationCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `round trip preserves mixed annotation`() {
        val original = PhotoAnnotation(
            strokes = listOf(
                Stroke(
                    points = listOf(Point(0f, 0f), Point(1f, 1f)),
                    color = 1, width = 2f, tool = DrawTool.PEN
                ),
                Stroke(
                    points = listOf(Point(2f, 2f), Point(3f, 3f)),
                    color = 2, width = 4f, tool = DrawTool.HIGHLIGHTER, alpha = 0.5f
                )
            ),
            texts = listOf(
                AnnotationText(x = 1f, y = 2f, text = "nota", color = 3, sizeSp = 14f)
            ),
            arrows = listOf(
                Arrow(startX = 0f, startY = 0f, endX = 5f, endY = 5f, color = 4, width = 2f)
            )
        )
        val decoded = PhotoAnnotationCodec.fromJson(PhotoAnnotationCodec.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `fromJson on empty string returns empty annotation`() {
        assertEquals(PhotoAnnotation(), PhotoAnnotationCodec.fromJson(""))
    }

    @Test
    fun `fromJson on blank string returns empty annotation`() {
        assertEquals(PhotoAnnotation(), PhotoAnnotationCodec.fromJson("   "))
    }
}
