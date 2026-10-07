package com.notifast.app.data.model

data class PhotoAnnotation(
    val strokes: List<Stroke> = emptyList(),
    val texts: List<AnnotationText> = emptyList(),
    val arrows: List<Arrow> = emptyList()
)

data class Stroke(
    val points: List<Point> = emptyList(),
    val color: Int,
    val width: Float,
    val tool: DrawTool,
    val alpha: Float = 1f
)

data class Point(val x: Float, val y: Float)

data class AnnotationText(
    val x: Float,
    val y: Float,
    val text: String,
    val color: Int,
    val sizeSp: Float
)

data class Arrow(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: Int,
    val width: Float
)

enum class DrawTool {
    PEN, HIGHLIGHTER, ERASER
}
