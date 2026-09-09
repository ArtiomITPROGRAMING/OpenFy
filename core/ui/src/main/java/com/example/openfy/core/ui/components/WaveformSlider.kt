package com.example.openfy.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun WaveformSlider(
    modifier: Modifier = Modifier,
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
) {
    val hapticFeedback = LocalHapticFeedback.current
    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val effectiveProgress = if (isDragging) dragProgress else progress
    val displayedCurrentMs = if (isDragging) (dragProgress * durationMs).toLong() else currentPositionMs

    val knobRadius by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 7.dp,
        label = "knobRadius"
    )

    fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current

            // Floating Time Bubble above finger during drag
            androidx.compose.animation.AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                val bubbleWidthDp = 56.dp
                val bubbleWidthPx = with(density) { bubbleWidthDp.toPx() }
                val targetBubbleXPx = (effectiveProgress * totalWidthPx - bubbleWidthPx / 2f)
                    .coerceIn(0f, (totalWidthPx - bubbleWidthPx).coerceAtLeast(0f))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = targetBubbleXPx.toInt(), y = -34.dp.roundToPx()) }
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1F28))
                        .border(1.dp, activeColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = formatTime(displayedCurrentMs),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        fontSize = 11.sp
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .pointerInput(durationMs) {
                        detectTapGestures { offset ->
                            val tapRatio = (offset.x / size.width).coerceIn(0f, 1f)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSeek((tapRatio * durationMs).toLong())
                        }
                    }
                    .pointerInput(durationMs) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            onDragEnd = {
                                isDragging = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSeek((dragProgress * durationMs).toLong())
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onHorizontalDrag = { change, _ ->
                                dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                            }
                        )
                    }
            ) {
                val width = size.width
                val height = size.height
                val numBars = 48
                val barWidth = (width / (numBars * 1.4f))
                val spacing = barWidth * 0.4f

                for (i in 0 until numBars) {
                    val barFraction = i.toFloat() / numBars
                    val x = i * (barWidth + spacing) + spacing / 2

                    val waveHeightRatio = ((sin(i * 0.45) * 0.4 + sin(i * 0.9) * 0.25 + 0.65).toFloat()).coerceIn(0.2f, 1.0f)
                    val barHeight = height * waveHeightRatio
                    val y = (height - barHeight) / 2

                    val isPlayed = barFraction <= effectiveProgress

                    if (isPlayed) {
                        drawRoundRect(
                            color = activeColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    } else {
                        drawRoundRect(
                            color = inactiveColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    }
                }

                val knobX = (effectiveProgress * width).coerceIn(0f, width)
                val knobPx = knobRadius.toPx()

                // Outer Halo
                if (isDragging) {
                    drawCircle(
                        color = activeColor.copy(alpha = 0.35f),
                        radius = knobPx * 1.5f,
                        center = Offset(knobX, height / 2)
                    )
                }

                // Inner White Knob with Accent Center
                drawCircle(
                    color = Color.White,
                    radius = knobPx,
                    center = Offset(knobX, height / 2)
                )
                drawCircle(
                    color = activeColor,
                    radius = knobPx * 0.55f,
                    center = Offset(knobX, height / 2)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(displayedCurrentMs),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDragging) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = formatTime(durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
