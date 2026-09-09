package com.example.openfy.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.ui.theme.AmoledPureBlack
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkGold
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.DarkBackground
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DynamicBackground(
    modifier: Modifier = Modifier,
    themeStyle: AppThemeStyle = AppThemeStyle.SERIOUS_DARK,
    accentColor: Color? = null,
    isPlaying: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "theme_bg_anim")

    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 12000 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_time"
    )

    val baseBackgroundColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledPureBlack
        AppThemeStyle.MIDNIGHT_NEON -> DarkBackground
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
        AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.background
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackgroundColor)
    ) {
        when (themeStyle) {
            AppThemeStyle.SERIOUS_DARK -> {}

            AppThemeStyle.MIDNIGHT_NEON -> {
                val primaryBlobColor = accentColor ?: NeonCyan
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(80.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val x1 = w * 0.25f + cos(animTime.toDouble()).toFloat() * (w * 0.15f)
                    val y1 = h * 0.25f + sin(animTime.toDouble()).toFloat() * (h * 0.12f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryBlobColor.copy(alpha = 0.55f), Color.Transparent),
                            center = Offset(x1, y1),
                            radius = w * 0.45f
                        ),
                        center = Offset(x1, y1),
                        radius = w * 0.45f
                    )

                    val x2 = w * 0.75f - sin(animTime.toDouble() * 0.8).toFloat() * (w * 0.18f)
                    val y2 = h * 0.35f + cos(animTime.toDouble() * 0.8).toFloat() * (h * 0.14f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPink.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(x2, y2),
                            radius = w * 0.5f
                        ),
                        center = Offset(x2, y2),
                        radius = w * 0.5f
                    )

                    val x3 = w * 0.5f + cos(animTime.toDouble() * 1.2).toFloat() * (w * 0.2f)
                    val y3 = h * 0.75f - sin(animTime.toDouble() * 1.2).toFloat() * (h * 0.15f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ElectricPurple.copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(x3, y3),
                            radius = w * 0.55f
                        ),
                        center = Offset(x3, y3),
                        radius = w * 0.55f
                    )
                }
            }

            AppThemeStyle.CYBERPUNK_BLOOD -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(85.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val x1 = w * 0.3f + cos(animTime.toDouble() * 0.9).toFloat() * (w * 0.15f)
                    val y1 = h * 0.3f + sin(animTime.toDouble() * 0.9).toFloat() * (h * 0.15f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(CyberpunkRubyRed.copy(alpha = 0.6f), Color.Transparent),
                            center = Offset(x1, y1),
                            radius = w * 0.5f
                        ),
                        center = Offset(x1, y1),
                        radius = w * 0.5f
                    )

                    val x2 = w * 0.7f - sin(animTime.toDouble() * 1.1).toFloat() * (w * 0.15f)
                    val y2 = h * 0.7f + cos(animTime.toDouble() * 1.1).toFloat() * (h * 0.15f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(CyberpunkGold.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(x2, y2),
                            radius = w * 0.45f
                        ),
                        center = Offset(x2, y2),
                        radius = w * 0.45f
                    )
                }
            }

            AppThemeStyle.RETRO_PIXEL -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val h = size.height
                    val step = 8f
                    var y = 0f
                    while (y < h) {
                        drawLine(
                            color = Color(0x1500FF66),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }
                }
            }

            AppThemeStyle.MATERIAL_YOU -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
            }
        }

        if (themeStyle.hasBlobs) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
        }

        content()
    }
}
