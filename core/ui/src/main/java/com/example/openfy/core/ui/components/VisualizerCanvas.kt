package com.example.openfy.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun VisualizerCanvas(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    barCount: Int = 10,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    minHeightFraction: Float = 0.15f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerTransition")

    // Phase-shifted animated heights for each frequency bar
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(540, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(280, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b4"
    )
    val bar5 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(490, easing = LinearEasing), RepeatMode.Reverse), label = "b5"
    )
    val bar6 by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(360, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b6"
    )
    val bar7 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.98f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b7"
    )
    val bar8 by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(330, easing = LinearEasing), RepeatMode.Reverse), label = "b8"
    )

    val gradientColors = remember(primaryColor, secondaryColor) {
        listOf(primaryColor, secondaryColor)
    }

    Spacer(
        modifier = modifier.drawWithCache {
            val totalWidth = size.width
            val canvasHeight = size.height
            val effectiveBars = barCount.coerceAtLeast(4)
            val spacing = totalWidth / (effectiveBars * 2.2f)
            val barWidth = (totalWidth - (effectiveBars - 1) * spacing) / effectiveBars
            val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

            val brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = 0f,
                endY = canvasHeight
            )

            onDrawBehind {
                val animatedValues = floatArrayOf(bar1, bar2, bar3, bar4, bar5, bar6, bar7, bar8)
                val valCount = animatedValues.size

                for (i in 0 until effectiveBars) {
                    val animFraction = if (isPlaying) {
                        val base = animatedValues[i % valCount]
                        base.coerceIn(minHeightFraction, 1.0f)
                    } else {
                        minHeightFraction
                    }

                    val barHeight = (canvasHeight * animFraction).coerceAtLeast(3f)
                    val left = i * (barWidth + spacing)
                    val top = (canvasHeight - barHeight) / 2f

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    )
}
