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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * 5 iconic Winamp Milkdrop-inspired presets with AGSL (API 33+) shaders
 * and high-performance Canvas fallbacks for older Android versions.
 */
enum class VisualizerPreset(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val agslShader: String
) {
    CYBER_TUNNEL(
        id = "cyber_tunnel",
        displayName = "Cyber Neon Tunnel",
        subtitle = "Фрактальный тоннель сквозь киберпространство",
        primaryColor = Color(0xFF00E5FF), // Neon Cyan
        secondaryColor = Color(0xFFFF0055), // Neon Pink
        agslShader = """
            uniform float2 iResolution;
            uniform float iTime;
            uniform float iEnergy;
            uniform float3 iColor1;
            uniform float3 iColor2;

            half4 main(float2 fragCoord) {
                float2 uv = (fragCoord - 0.5 * iResolution) / min(iResolution.x, iResolution.y);
                float r = length(uv);
                float a = atan(uv.y, uv.x);

                float depth = 0.3 / (r + 0.05);
                float pulse = sin(depth * 6.0 - iTime * 4.0 + a * 3.0) * (0.5 + iEnergy * 0.8);
                float rings = abs(sin(depth * 12.0 - iTime * 3.0));

                float3 col = mix(iColor1, iColor2, 0.5 + 0.5 * sin(a * 4.0 + iTime));
                col *= (pulse * 1.5 + (1.0 - rings) * 0.6) * iEnergy;
                col += (0.05 / (r + 0.02)) * iColor1 * (0.5 + iEnergy);

                return half4(col, 1.0);
            }
        """.trimIndent()
    ),

    AURORA_BOREALIS(
        id = "aurora",
        displayName = "Aurora Borealis",
        subtitle = "Текучие волны северного сияния",
        primaryColor = Color(0xFF00FF88), // Aurora Green
        secondaryColor = Color(0xFF7000FF), // Deep Violet
        agslShader = """
            uniform float2 iResolution;
            uniform float iTime;
            uniform float iEnergy;
            uniform float3 iColor1;
            uniform float3 iColor2;

            half4 main(float2 fragCoord) {
                float2 uv = fragCoord / iResolution;
                float wave1 = sin(uv.x * 7.0 + iTime * 1.5) * 0.15;
                float wave2 = cos(uv.x * 11.0 - iTime * 2.0) * 0.1;
                float yTarget = 0.5 + wave1 + wave2;

                float dist = abs(uv.y - yTarget);
                float glow = (0.035 * (1.0 + iEnergy * 1.8)) / (dist + 0.02);

                float3 col = mix(iColor1, iColor2, uv.x + 0.2 * sin(iTime));
                col *= glow;
                col += float3(0.01, 0.02, 0.04); // subtle deep sky base

                return half4(col, 1.0);
            }
        """.trimIndent()
    ),

    LIQUID_METAL(
        id = "liquid_metal",
        displayName = "Liquid Metal / Chrome",
        subtitle = "Расплавленная ртуть с реактивной рябью",
        primaryColor = Color(0xFFE0E0E0), // Liquid Chrome
        secondaryColor = Color(0xFF00B0FF), // Electric Blue
        agslShader = """
            uniform float2 iResolution;
            uniform float iTime;
            uniform float iEnergy;
            uniform float3 iColor1;
            uniform float3 iColor2;

            half4 main(float2 fragCoord) {
                float2 uv = (fragCoord - 0.5 * iResolution) / min(iResolution.x, iResolution.y);
                float t = iTime * 0.8;

                float ripple1 = sin(length(uv + float2(sin(t)*0.2, cos(t)*0.2)) * 14.0 - t * 4.0);
                float ripple2 = cos(length(uv - float2(cos(t)*0.3, sin(t)*0.3)) * 18.0 + t * 3.0);
                float surf = 0.5 + 0.25 * ripple1 + 0.25 * ripple2;

                float spec = pow(surf, 4.0) * (0.8 + iEnergy * 1.2);
                float3 col = mix(iColor1, iColor2, surf) * spec;

                return half4(col, 1.0);
            }
        """.trimIndent()
    ),

    SYNTHWAVE_SUNSET(
        id = "synthwave",
        displayName = "Synthwave Sunset 80s",
        subtitle = "Винтажная ретро-сетка и неоновое солнце",
        primaryColor = Color(0xFFFF8800), // Solar Orange
        secondaryColor = Color(0xFFFF007F), // Neon Magenta
        agslShader = """
            uniform float2 iResolution;
            uniform float iTime;
            uniform float iEnergy;
            uniform float3 iColor1;
            uniform float3 iColor2;

            half4 main(float2 fragCoord) {
                float2 uv = (fragCoord - 0.5 * iResolution) / min(iResolution.x, iResolution.y);

                // Sun in the top half
                float sunDist = length(uv - float2(0.0, 0.15));
                float sun = smoothstep(0.28, 0.26, sunDist);
                // Sun horizontal blinds
                if (uv.y > 0.15 && sun > 0.0) {
                    float bars = sin((uv.y - 0.15) * 60.0);
                    if (bars > 0.3) sun = 0.0;
                }

                // Grid in the bottom half
                float grid = 0.0;
                if (uv.y < 0.05) {
                    float depth = 0.4 / (0.06 - uv.y);
                    float xLine = abs(sin(uv.x * depth * 3.0));
                    float yLine = abs(sin(depth * 4.0 - iTime * 3.0));
                    grid = max(1.0 - smoothstep(0.0, 0.08, xLine), 1.0 - smoothstep(0.0, 0.08, yLine)) * (0.4 + iEnergy * 0.8);
                }

                float3 col = sun * mix(iColor1, iColor2, (uv.y - 0.15) * 3.0);
                col += grid * iColor2;
                col += float3(0.04, 0.02, 0.08);

                return half4(col, 1.0);
            }
        """.trimIndent()
    ),

    HYPNOTIC_KALEIDOSCOPE(
        id = "kaleidoscope",
        displayName = "Hypnotic Kaleidoscope",
        subtitle = "Психоделическая сакральная геометрия",
        primaryColor = Color(0xFF9D00FF), // Ultraviolet
        secondaryColor = Color(0xFF00FFCC), // Turquoise
        agslShader = """
            uniform float2 iResolution;
            uniform float iTime;
            uniform float iEnergy;
            uniform float3 iColor1;
            uniform float3 iColor2;

            half4 main(float2 fragCoord) {
                float2 uv = (fragCoord - 0.5 * iResolution) / min(iResolution.x, iResolution.y);
                float a = atan(uv.y, uv.x);
                float r = length(uv);

                // 8-fold symmetry
                float segments = 8.0;
                a = mod(a + iTime * 0.2, 6.28318 / segments);
                a = abs(a - 3.14159 / segments);

                float2 p = float2(cos(a), sin(a)) * r;
                float pattern = sin(p.x * 16.0 + iTime * 2.0) * cos(p.y * 16.0 - iTime * 2.0);
                pattern = abs(pattern) * (0.6 + iEnergy * 1.0);

                float3 col = mix(iColor1, iColor2, 0.5 + 0.5 * sin(r * 12.0 - iTime * 3.0));
                col *= pattern / (r + 0.1);

                return half4(col, 1.0);
            }
        """.trimIndent()
    );

    /**
     * Renders high-performance dynamic Canvas visuals for Android 7.0–12 devices
     * where AGSL is not available natively.
     */
    fun drawCanvasFallback(
        scope: DrawScope,
        time: Float,
        energy: Float
    ) {
        val w = scope.size.width
        val h = scope.size.height
        val center = Offset(w / 2f, h / 2f)

        when (this) {
            CYBER_TUNNEL -> {
                val rings = 8
                val maxRadius = minOf(w, h) * 0.48f
                for (i in 1..rings) {
                    val phase = (i.toFloat() / rings + time * 0.3f) % 1f
                    val r = phase * maxRadius
                    val alpha = (1f - phase).coerceIn(0.1f, 1f) * energy
                    val color = if (i % 2 == 0) primaryColor else secondaryColor
                    scope.drawCircle(
                        color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
                        radius = r,
                        center = center,
                        style = Stroke(width = 3f + energy * 4f)
                    )
                }
            }

            AURORA_BOREALIS -> {
                val points = 20
                for (layer in 0..2) {
                    val color = if (layer % 2 == 0) primaryColor else secondaryColor
                    for (i in 0 until points) {
                        val x = (w / (points - 1)) * i
                        val normX = i.toFloat() / points
                        val y = h * 0.5f + sin((normX * 6f + time * 2f + layer).toDouble()).toFloat() * (40f + energy * 60f)
                        val radius = 12f + energy * 18f
                        scope.drawCircle(
                            color = color.copy(alpha = 0.35f * energy),
                            radius = radius,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            LIQUID_METAL -> {
                val blobs = 6
                for (i in 0 until blobs) {
                    val angle = (time * 0.8f + i * (6.28f / blobs))
                    val r = 60f + sin(time * 2f + i) * 30f * energy
                    val x = center.x + cos(angle.toDouble()).toFloat() * (w * 0.22f)
                    val y = center.y + sin(angle.toDouble()).toFloat() * (h * 0.22f)
                    scope.drawCircle(
                        color = primaryColor.copy(alpha = (0.4f + energy * 0.4f).coerceIn(0f, 1f)),
                        radius = r,
                        center = Offset(x, y)
                    )
                }
            }

            SYNTHWAVE_SUNSET -> {
                // Sun
                val sunRadius = minOf(w, h) * 0.22f
                val sunCenter = Offset(w / 2f, h * 0.45f)
                scope.drawCircle(
                    color = primaryColor.copy(alpha = (0.85f * energy).coerceIn(0.3f, 1f)),
                    radius = sunRadius,
                    center = sunCenter
                )
                // Grid lines
                val gridLines = 10
                for (i in 0..gridLines) {
                    val y = h * 0.6f + (h * 0.4f / gridLines) * i
                    scope.drawLine(
                        color = secondaryColor.copy(alpha = (0.5f + energy * 0.4f).coerceIn(0f, 1f)),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 2f
                    )
                }
            }

            HYPNOTIC_KALEIDOSCOPE -> {
                val petals = 12
                val maxR = minOf(w, h) * 0.35f
                for (i in 0 until petals) {
                    val angle = (i * (6.28f / petals) + time * 0.5f)
                    val x = center.x + cos(angle.toDouble()).toFloat() * maxR * energy
                    val y = center.y + sin(angle.toDouble()).toFloat() * maxR * energy
                    scope.drawCircle(
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        radius = 20f + energy * 25f,
                        center = Offset(x, y),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}
