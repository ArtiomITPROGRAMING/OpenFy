/*
 * Copyright (C) 2026 ArtiomITPROGRAMING
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.openfy.core.ui.components.visualizer

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

/**
 * Modern reactive visualizer inspired by Winamp Milkdrop.
 *
 * Employs hardware-accelerated AGSL (Android Graphics Shading Language) on Android 13+ (API 33+)
 * with an automatic, optimized Canvas fallback on Android 7.0–12 (API 24–32).
 */
@Composable
fun MilkdropVisualizer(
    preset: VisualizerPreset,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    audioEnergy: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MilkdropTime")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val effectiveTime = if (isPlaying) animTime else 0f
    val effectiveEnergy = if (isPlaying) audioEnergy.coerceIn(0.1f, 1.2f) else 0.15f

    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AgslShaderRenderer(
                preset = preset,
                time = effectiveTime,
                energy = effectiveEnergy
            )
        } else {
            CanvasFallbackRenderer(
                preset = preset,
                time = effectiveTime,
                energy = effectiveEnergy
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AgslShaderRenderer(
    preset: VisualizerPreset,
    time: Float,
    energy: Float
) {
    val shader = remember(preset.id) {
        try {
            RuntimeShader(preset.agslShader)
        } catch (e: Exception) {
            null
        }
    }

    if (shader != null) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w > 0f && h > 0f) {
                shader.setFloatUniform("iResolution", w, h)
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("iEnergy", energy)
                shader.setFloatUniform(
                    "iColor1",
                    preset.primaryColor.red,
                    preset.primaryColor.green,
                    preset.primaryColor.blue
                )
                shader.setFloatUniform(
                    "iColor2",
                    preset.secondaryColor.red,
                    preset.secondaryColor.green,
                    preset.secondaryColor.blue
                )
                drawRect(brush = ShaderBrush(shader))
            }
        }
    } else {
        CanvasFallbackRenderer(preset = preset, time = time, energy = energy)
    }
}

@Composable
private fun CanvasFallbackRenderer(
    preset: VisualizerPreset,
    time: Float,
    energy: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        preset.drawCanvasFallback(this, time, energy)
    }
}
