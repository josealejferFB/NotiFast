package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import com.notifast.app.data.model.PhotoAnnotation
import com.notifast.app.domain.util.PhotoAnnotationCodec

@Composable
fun AnnotatedImage(
    path: String,
    annotationJson: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val annotation = remember(annotationJson) {
        if (annotationJson.isNullOrBlank()) null
        else PhotoAnnotationCodec.fromJson(annotationJson)
    }

    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        AsyncImage(
            model = path,
            contentDescription = contentDescription,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
        if (annotation != null && annotation.strokes.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxWidth()) {
                annotation.strokes.forEach { stroke ->
                    if (stroke.points.size < 2) return@forEach
                    val drawPath = Path().apply {
                        moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                    }
                    drawPath(
                        path = drawPath,
                        color = Color(stroke.color),
                        style = Stroke(
                            width = stroke.width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        ),
                        alpha = stroke.alpha
                    )
                }
            }
        }
    }
}
