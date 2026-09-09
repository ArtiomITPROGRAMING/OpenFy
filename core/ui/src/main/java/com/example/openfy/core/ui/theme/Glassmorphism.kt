package com.example.openfy.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassDarkSurface,
    borderColor: Color = GlassBorderStroke,
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 20.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.75f),
                backgroundColor.copy(alpha = 0.55f)
            )
        ),
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor.copy(alpha = 0.6f),
                borderColor.copy(alpha = 0.15f),
                borderColor.copy(alpha = 0.05f),
                borderColor.copy(alpha = 0.4f)
            )
        ),
        shape = shape
    )

fun Modifier.glowBorder(
    glowColor: Color = NeonCyan,
    shape: Shape = RoundedCornerShape(24.dp),
    strokeWidth: Dp = 1.5.dp
): Modifier = this
    .border(
        width = strokeWidth,
        brush = Brush.sweepGradient(
            listOf(
                glowColor.copy(alpha = 0.8f),
                NeonPink.copy(alpha = 0.6f),
                ElectricPurple.copy(alpha = 0.8f),
                glowColor.copy(alpha = 0.8f)
            )
        ),
        shape = shape
    )
