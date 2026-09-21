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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Iconic Winamp Milkdrop-inspired reactive visualizer presets with AGSL (API 33+)
 * shaders and high-performance Canvas fallbacks for older Android versions.
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
                col += float3(0.01, 0.02, 0.04);

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
                uv.y = -uv.y; // Standard Cartesian coordinates: +Y is UP (sky), -Y is DOWN (floor)

                // Sun in the sky (top half)
                float sunDist = length(uv - float2(0.0, 0.18));
                float sun = smoothstep(0.28, 0.26, sunDist);
                // Sun horizontal retro blinds (lower half of the sun)
                if (uv.y < 0.18 && sun > 0.0) {
                    float bars = sin((uv.y - 0.18) * 55.0);
                    if (bars > 0.25) sun = 0.0;
                }

                // Grid on the floor (bottom half)
                float grid = 0.0;
                if (uv.y < -0.02) {
                    float depth = 0.45 / (-0.01 - uv.y);
                    float xLine = abs(sin(uv.x * depth * 3.5));
                    float yLine = abs(sin(depth * 4.0 - iTime * 3.5));
                    grid = max(1.0 - smoothstep(0.0, 0.08, xLine), 1.0 - smoothstep(0.0, 0.08, yLine)) * (0.4 + iEnergy * 0.8);
                }

                float3 col = sun * mix(iColor1, iColor2, clamp((uv.y - 0.0) * 3.0, 0.0, 1.0));
                col += grid * iColor2;
                col += float3(0.04, 0.01, 0.08);

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
    ),

    BEAT_SUBWOOFER(
        id = "beat_subwoofer",
        displayName = "Bass Cannon & Beat Shockwave",
        subtitle = "Мощный сабвуфер с пульсацией точно под бит трека",
        primaryColor = Color(0xFF00FF66), // Acid Neon Lime
        secondaryColor = Color(0xFFFF0055), // Electric Shock Pink
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

                // Subwoofer membrane pumping with kick beat
                float coneRadius = 0.32 + (iEnergy - 0.5) * 0.12;
                float dustCap = smoothstep(0.10, 0.08, r);
                float cone = smoothstep(coneRadius, coneRadius - 0.03, r) * (1.0 - dustCap);
                float rim = smoothstep(coneRadius + 0.04, coneRadius, r) - smoothstep(coneRadius, coneRadius - 0.03, r);

                // Concentric shockwaves propagating outwards on beat
                float shockDist = r * 16.0 - iTime * 6.0 - iEnergy * 3.0;
                float shockwave = abs(sin(shockDist)) * smoothstep(0.2, 0.7, r) * smoothstep(0.9, 0.6, r);

                // Radial bass cannons / frequency bars radiating around the subwoofer
                float spokes = abs(sin(a * 16.0));
                float spokeBars = smoothstep(0.5, 0.95, spokes) * smoothstep(coneRadius + 0.05, 0.75 + iEnergy * 0.25, r) * smoothstep(0.85 + iEnergy * 0.25, 0.75 + iEnergy * 0.25, r);

                // Shading the 3D cone
                float3 col = dustCap * float3(0.12, 0.12, 0.14) * (1.0 + iEnergy * 0.5);
                col += cone * mix(float3(0.06, 0.06, 0.08), iColor1 * 0.3, (r - 0.10) / (coneRadius - 0.10));
                col += rim * iColor2 * (1.2 + iEnergy * 1.5);
                col += shockwave * mix(iColor1, iColor2, sin(r * 10.0 + iTime)) * (0.8 + iEnergy * 2.0);
                col += spokeBars * iColor1 * (1.5 + iEnergy * 2.5);

                // Background bass strobe on strong kick
                if (iEnergy > 0.8) {
                    col += iColor2 * ((iEnergy - 0.8) * 1.5);
                }

                return half4(col, 1.0);
            }
        """.trimIndent()
    ),

    COSMIC_SUPERNOVA(
        id = "cosmic_supernova",
        displayName = "Cosmic Supernova & Black Hole",
        subtitle = "Гравитационная сингулярность и взрыв сверхновой",
        primaryColor = Color(0xFFFF6600), // Stellar Gold / Flame
        secondaryColor = Color(0xFF00E5FF), // Relativistic Cyan
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

                // Relativistic spiral accretion disk
                float spiral = a + 3.0 * log(r + 0.005) - iTime * 2.2;
                float arms = abs(sin(spiral * 3.0));
                float disk = arms * smoothstep(0.10, 0.22, r) * smoothstep(0.85, 0.35, r);

                // Event horizon (gravitational lensing ring)
                float horizonGlow = (0.025 * (1.0 + iEnergy * 2.5)) / (abs(r - 0.16) + 0.015);

                // Supernova explosive shockwave radiating on beat
                float blast = sin(r * 24.0 - iTime * 7.0 - iEnergy * 4.0);
                float blastRing = abs(blast) * smoothstep(0.18, 0.5, r) * smoothstep(0.9, 0.45, r) * iEnergy;

                // Cosmic starfield sparkles
                float stars = fract(sin(dot(fragCoord, float2(12.9898, 78.233))) * 43758.5453);
                float starGlow = step(0.995, stars) * (0.3 + iEnergy * 0.7);

                float3 col = disk * mix(iColor1, iColor2, sin(spiral + iTime) * 0.5 + 0.5) * (0.8 + iEnergy * 1.6);
                col += horizonGlow * iColor1;
                col += blastRing * iColor2 * 1.5;
                col += starGlow * float3(0.9, 0.9, 1.0);

                // Pitch black singularity at center
                if (r < 0.12) {
                    col *= smoothstep(0.08, 0.12, r);
                }

                return half4(col, 1.0);
            }
        """.trimIndent()
    ),

    CYBER_MATRIX(
        id = "cyber_matrix",
        displayName = "Cyber Matrix Rain",
        subtitle = "Цифровой каскад символов с лазерным сканированием",
        primaryColor = Color(0xFF00FF41), // Matrix Phosphor Green
        secondaryColor = Color(0xFFE0FFE5), // Bright Jade
        agslShader = """
            uniform float2 iResolution;
            uniform float iTime;
            uniform float iEnergy;
            uniform float3 iColor1;
            uniform float3 iColor2;

            half4 main(float2 fragCoord) {
                float2 uv = fragCoord / iResolution;

                // Column grid
                float cols = 36.0;
                float colIndex = floor(uv.x * cols);
                float colFrac = fract(uv.x * cols);

                // Fall speed modulated by beat energy
                float speed = 1.2 + sin(colIndex * 133.7) * 0.6 + iEnergy * 0.8;
                float streamY = fract(uv.y - iTime * speed * 0.4 + sin(colIndex * 77.3) * 10.0);

                // Digital glyph cells
                float glyphGrid = step(0.15, sin(uv.y * 120.0 + colIndex * 21.0));
                float charHead = smoothstep(0.92, 0.99, streamY);
                float charTrail = pow(streamY, 3.5);

                // Laser scanline sweeping down on beat
                float scanline = sin((uv.y - iTime * 0.8) * 80.0) * 0.1;

                // Color mixing: glowing head (white/cyan) + phosphorescent trail (green)
                float3 col = iColor2 * charHead * (1.5 + iEnergy * 2.0);
                col += iColor1 * (charTrail * glyphGrid) * (0.7 + iEnergy * 1.4);
                col += scanline * iColor1 * 0.3;

                // Side column fade
                col *= step(0.12, colFrac) * step(colFrac, 0.88);

                return half4(col, 1.0);
            }
        """.trimIndent()
    ),

    PLASMA_ORB(
        id = "plasma_orb",
        displayName = "Tesla Plasma Orb",
        subtitle = "Электрические разряды и дуги плазменного шара",
        primaryColor = Color(0xFFB000FF), // Electric Violet
        secondaryColor = Color(0xFF00F5FF), // Tesla Arc Cyan
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

                // Glass orb boundary
                float orbBoundary = smoothstep(0.46, 0.44, r);
                float glassRim = (0.015 * (1.0 + iEnergy)) / (abs(r - 0.44) + 0.01);

                // Central core electrode
                float core = smoothstep(0.08, 0.05, r) * (1.2 + iEnergy * 1.5);

                // Electric plasma filaments
                float arc1 = sin(a * 7.0 + sin(r * 20.0 - iTime * 5.0) * 3.0);
                float arc2 = cos(a * 11.0 - cos(r * 26.0 + iTime * 4.0) * 2.5);
                float filaments = pow(abs(arc1 * arc2), 6.0) * smoothstep(0.06, 0.2, r) * orbBoundary;

                float3 col = core * iColor1;
                col += filaments * mix(iColor1, iColor2, sin(a * 4.0 + iTime)) * (1.5 + iEnergy * 3.0);
                col += glassRim * iColor2 * 0.8;

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
                // Sun in the top half
                val sunRadius = minOf(w, h) * 0.22f
                val sunCenter = Offset(w / 2f, h * 0.35f)
                scope.drawCircle(
                    color = primaryColor.copy(alpha = (0.85f * energy).coerceIn(0.3f, 1f)),
                    radius = sunRadius,
                    center = sunCenter
                )
                // Sun horizontal blinds
                val blinds = 6
                for (b in 1..blinds) {
                    val blindY = sunCenter.y + (sunRadius / blinds) * (b - 1)
                    val blindH = 3f + b * 1.5f
                    scope.drawLine(
                        color = Color(0xFF0D021A),
                        start = Offset(sunCenter.x - sunRadius, blindY),
                        end = Offset(sunCenter.x + sunRadius, blindY),
                        strokeWidth = blindH
                    )
                }
                // Horizon line
                val horizonY = h * 0.52f
                scope.drawLine(
                    color = secondaryColor.copy(alpha = 0.9f),
                    start = Offset(0f, horizonY),
                    end = Offset(w, horizonY),
                    strokeWidth = 3f
                )
                // Floor perspective grid lines (converging to horizon, radiating downwards)
                val perspLines = 14
                for (i in 0..perspLines) {
                    val bottomX = (w / perspLines) * i
                    scope.drawLine(
                        color = secondaryColor.copy(alpha = (0.35f + energy * 0.35f).coerceIn(0.1f, 0.85f)),
                        start = Offset(w / 2f, horizonY),
                        end = Offset(bottomX, h),
                        strokeWidth = 2f
                    )
                }
                // Floor horizontal grid lines moving towards viewer
                val gridLines = 10
                for (i in 1..gridLines) {
                    val p = (i.toFloat() / gridLines)
                    val p2 = p * p // perspective compression
                    val y = horizonY + (h - horizonY) * p2
                    scope.drawLine(
                        color = secondaryColor.copy(alpha = (0.3f + energy * 0.4f * p).coerceIn(0.1f, 1f)),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.5f + p * 2f
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

            BEAT_SUBWOOFER -> {
                // Subwoofer speaker cone pumping with beat
                val baseR = minOf(w, h) * 0.22f
                val coneR = baseR * (0.85f + energy * 0.30f)

                // Concentric shockwaves radiating on beat
                val shockwaves = 3
                for (s in 0 until shockwaves) {
                    val phase = ((time * 1.5f + s * (1f / shockwaves)) % 1f)
                    val shockR = coneR + phase * (minOf(w, h) * 0.35f)
                    val alpha = ((1f - phase) * (0.3f + energy * 0.6f)).coerceIn(0f, 1f)
                    scope.drawCircle(
                        color = secondaryColor.copy(alpha = alpha),
                        radius = shockR,
                        center = center,
                        style = Stroke(width = 2f + energy * 4f)
                    )
                }

                // 32 Radial frequency bars / sound cannons
                val bars = 32
                for (i in 0 until bars) {
                    val angle = i * (2f * PI.toFloat() / bars)
                    val barLen = (15f + energy * (minOf(w, h) * 0.18f))
                    val startX = center.x + cos(angle.toDouble()).toFloat() * (coneR + 8f)
                    val startY = center.y + sin(angle.toDouble()).toFloat() * (coneR + 8f)
                    val endX = center.x + cos(angle.toDouble()).toFloat() * (coneR + 8f + barLen)
                    val endY = center.y + sin(angle.toDouble()).toFloat() * (coneR + 8f + barLen)
                    scope.drawLine(
                        color = primaryColor.copy(alpha = (0.5f + energy * 0.5f).coerceIn(0.2f, 1f)),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )
                }

                // Main cone diaphragm
                scope.drawCircle(
                    color = Color(0xFF141416),
                    radius = coneR,
                    center = center
                )
                scope.drawCircle(
                    color = secondaryColor.copy(alpha = (0.7f + energy * 0.3f).coerceIn(0.2f, 1f)),
                    radius = coneR,
                    center = center,
                    style = Stroke(width = 4f + energy * 3f)
                )

                // Center metallic dust cap
                scope.drawCircle(
                    color = primaryColor.copy(alpha = (0.4f + energy * 0.6f).coerceIn(0.2f, 1f)),
                    radius = coneR * 0.38f,
                    center = center
                )
            }

            COSMIC_SUPERNOVA -> {
                val maxR = minOf(w, h) * 0.45f

                // Swirling galaxy arms
                val stars = 36
                for (i in 0 until stars) {
                    val angle = (i * (2f * PI.toFloat() / 12) + time * 1.2f)
                    val r = (i.toFloat() / stars) * maxR
                    val x = center.x + cos(angle.toDouble()).toFloat() * r
                    val y = center.y + sin(angle.toDouble()).toFloat() * r
                    scope.drawCircle(
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        radius = 3f + energy * 5f,
                        center = Offset(x, y)
                    )
                }

                // Supernova blast ring
                val blastR = ((time * 2f) % 1f) * maxR
                scope.drawCircle(
                    color = secondaryColor.copy(alpha = (0.4f * energy).coerceIn(0.1f, 0.8f)),
                    radius = blastR,
                    center = center,
                    style = Stroke(width = 3f + energy * 3f)
                )

                // Center black hole event horizon
                scope.drawCircle(
                    color = Color.Black,
                    radius = maxR * 0.22f,
                    center = center
                )
                scope.drawCircle(
                    color = primaryColor.copy(alpha = (0.8f + energy * 0.2f).coerceIn(0.4f, 1f)),
                    radius = maxR * 0.22f,
                    center = center,
                    style = Stroke(width = 4f + energy * 3f)
                )
            }

            CYBER_MATRIX -> {
                val cols = 24
                val colWidth = w / cols
                for (c in 0 until cols) {
                    val colX = c * colWidth + colWidth / 2f
                    val dropY = ((time * (120f + (c % 5) * 40f) * (0.8f + energy * 0.8f) + c * 80f) % (h + 120f)) - 60f
                    // Glowing head
                    scope.drawCircle(
                        color = secondaryColor.copy(alpha = (0.8f + energy * 0.2f).coerceIn(0.4f, 1f)),
                        radius = 4f + energy * 3f,
                        center = Offset(colX, dropY)
                    )
                    // Fading trail
                    scope.drawLine(
                        color = primaryColor.copy(alpha = (0.4f + energy * 0.4f).coerceIn(0.1f, 0.8f)),
                        start = Offset(colX, dropY - 50f),
                        end = Offset(colX, dropY),
                        strokeWidth = 2.5f
                    )
                }
            }

            PLASMA_ORB -> {
                val orbR = minOf(w, h) * 0.38f
                // Outer glass orb rim
                scope.drawCircle(
                    color = secondaryColor.copy(alpha = 0.6f + energy * 0.3f),
                    radius = orbR,
                    center = center,
                    style = Stroke(width = 3f)
                )
                // Center electrode
                scope.drawCircle(
                    color = primaryColor,
                    radius = 24f + energy * 12f,
                    center = center
                )
                // Plasma lightning tendrils
                val tendrils = 8
                for (i in 0 until tendrils) {
                    val angle = (i * (2f * PI.toFloat() / tendrils) + sin((time * 3f + i).toDouble()).toFloat() * 0.4f)
                    val endX = center.x + cos(angle.toDouble()).toFloat() * orbR
                    val endY = center.y + sin(angle.toDouble()).toFloat() * orbR
                    scope.drawLine(
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2f + energy * 3f
                    )
                }
            }
        }
    }
}
