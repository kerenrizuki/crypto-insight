package com.example.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SparklineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surface))
        return
    }

    val maxVal = data.maxOrNull() ?: 1.0
    val minVal = data.minOrNull() ?: 0.0
    val range = if (maxVal - minVal == 0.0) 1.0 else (maxVal - minVal)

    // Animation progress for clean reveals
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "chartAnim"
    )

    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        val width = size.width
        val height = size.height

        val points = data.mapIndexed { index, value ->
            val x = (index.toFloat() / (data.size - 1)) * width
            // Compute Y inverted (Compose y decreases upwards)
            val normalizedY = (value - minVal) / range
            val y = height - (normalizedY.toFloat() * height)
            Offset(x, y)
        }

        // Draw grid boundaries
        val gridLines = 3
        val gridColor = Color(0xFFE2E8F0) // slate-200 border matching Geometric Balance
        for (i in 0..gridLines) {
            val hY = (height / gridLines) * i
            drawLine(
                color = gridColor,
                start = Offset(0f, hY),
                end = Offset(width, hY),
                strokeWidth = 1f
            )
        }

        if (points.isNotEmpty()) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val currentPoint = points[i]
                    val previousPoint = points[i - 1]
                    // Add smooth cubic bezier curve
                    val controlX1 = previousPoint.x + (currentPoint.x - previousPoint.x) / 2f
                    val controlY1 = previousPoint.y
                    val controlX2 = previousPoint.x + (currentPoint.x - previousPoint.x) / 2f
                    val controlY2 = currentPoint.y

                    cubicTo(
                        controlX1, controlY1,
                        controlX2, controlY2,
                        currentPoint.x, currentPoint.y
                    )
                }
            }

            // Slice path matching progress
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width * animatedProgress, height)
                lineTo(0f, height)
                close()
            }

            // Draw Area Gradient Filling
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Main Stroke Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw indicator dot on active endpoint
            val lastPoint = points.last()
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = lastPoint
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = lastPoint
            )
        }
    }
}

@Composable
fun FullInteractiveChart(
    data: List<Double>,
    labels: List<String>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        SparklineChart(
            data = data,
            lineColor = lineColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}
