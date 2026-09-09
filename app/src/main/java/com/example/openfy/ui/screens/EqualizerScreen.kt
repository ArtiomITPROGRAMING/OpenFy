package com.example.openfy.ui.screens

import android.media.audiofx.PresetReverb
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.model.Quadruple
import com.example.openfy.core.audio.service.BandInfo
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AmoledPureBlack
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkGold
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.DarkBackground
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import com.example.openfy.core.ui.theme.RetroPhosphorGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    playbackManager: PlaybackManager,
    onBack: () -> Unit
) {
    val eqController = playbackManager.equalizerController
    val isEnabled by eqController.isEnabled.collectAsState()
    val bands by eqController.bands.collectAsState()
    val presets by eqController.presets.collectAsState()
    val currentPreset by eqController.currentPreset.collectAsState()
    val virtualizerStrength by eqController.virtualizerStrength.collectAsState()
    val bassBoostStrength by eqController.bassBoostStrength.collectAsState()
    val reverbPreset by eqController.reverbPreset.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        playbackManager.rebindEqualizer()
    }

    // 🎨 Unified Theme-Aware Palette for Equalizer Screen
    val (primaryColor, secondaryColor, accentColor, cardBgColor) = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Quadruple(
            Color.White,
            Color(0xFFB0B3C6),
            Color(0xFFE0E0E0),
            AmoledDarkSurface
        )
        AppThemeStyle.CYBERPUNK_BLOOD -> Quadruple(
            CyberpunkRubyRed,
            CyberpunkGold,
            Color(0xFFFF3366),
            GlassDarkSurface
        )
        AppThemeStyle.RETRO_PIXEL -> Quadruple(
            RetroPhosphorGreen,
            Color(0xFF00FF66),
            Color(0xFF7CFF00),
            Color(0xFF0D1E14)
        )
        AppThemeStyle.MATERIAL_YOU -> Quadruple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.surfaceVariant
        )
        else -> Quadruple(
            NeonCyan,
            ElectricPurple,
            NeonPink,
            GlassDarkSurface
        )
    }

    val sliderTrackInactive = if (themeStyle == AppThemeStyle.SERIOUS_DARK) {
        Color(0xFF282935)
    } else {
        Color.White.copy(alpha = 0.15f)
    }

    DynamicBackground(isPlaying = isPlaying, themeStyle = themeStyle) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .verticalScroll(scrollState)
        ) {
            // ==========================================
            // TOP APP BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column {
                        Text(
                            text = "DSP Эквалайзер",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isEnabled) "Аппаратный DSP активен" else "Звуковой движок выключен",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEnabled) primaryColor else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { eqController.setEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                        checkedTrackColor = primaryColor,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // 1. FREQUENCY RESPONSE VISUALIZER GRAPH
            // ==========================================
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = cardBgColor,
                hasGlowBorder = themeStyle.hasNeonGlow,
                glowColor = primaryColor.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "АЧХ Частотный спектр",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = currentPreset,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Canvas Graph
                    FrequencyCurveCanvas(
                        bands = bands,
                        isEnabled = isEnabled,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 2. PRESETS ROW + RESET FLAT BUTTON
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ПРЕСЕТЫ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = { eqController.resetToFlat() },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Сброс в 0 dB", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { presetName ->
                    val isSelected = currentPreset.equals(presetName, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { eqController.usePreset(presetName) },
                        label = { Text(presetName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK || themeStyle == AppThemeStyle.RETRO_PIXEL) Color.Black else Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 3. EQUALIZER BANDS CARD
            // ==========================================
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = cardBgColor,
                hasGlowBorder = themeStyle.hasNeonGlow,
                glowColor = secondaryColor.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Полосы частот эквалайзера",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (bands.isEmpty()) {
                        Text(
                            text = "Инициализация аудиосессии...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    } else {
                        bands.forEach { band ->
                            val freqLabel = if (band.centerFreqHz >= 1000) {
                                "${"%.1f".format(band.centerFreqHz / 1000f)} kHz"
                            } else {
                                "${band.centerFreqHz} Hz"
                            }
                            val gainDb = (band.currentLevelMb.toFloat() / 100f)

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = freqLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${if (gainDb > 0) "+" else ""}${"%.1f".format(gainDb)} dB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (gainDb != 0f) primaryColor else Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Slider(
                                    value = band.currentLevelMb.toFloat(),
                                    onValueChange = {
                                        eqController.setBandLevel(band.index, it.toInt().toShort())
                                    },
                                    valueRange = band.minLevelMb.toFloat()..band.maxLevelMb.toFloat(),
                                    enabled = isEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = primaryColor,
                                        activeTrackColor = primaryColor,
                                        inactiveTrackColor = sliderTrackInactive,
                                        disabledThumbColor = Color.Gray,
                                        disabledActiveTrackColor = Color.DarkGray
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 4. BASS BOOST CARD
            // ==========================================
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = cardBgColor,
                hasGlowBorder = themeStyle.hasNeonGlow,
                glowColor = primaryColor.copy(alpha = 0.25f)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SpeakerGroup,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Усилитель баса (Bass Boost)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${bassBoostStrength / 10}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = bassBoostStrength.toFloat(),
                        onValueChange = { eqController.setBassBoostStrength(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = sliderTrackInactive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 5. 3D VIRTUALIZER / SPATIAL AUDIO
            // ==========================================
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = cardBgColor,
                hasGlowBorder = themeStyle.hasNeonGlow,
                glowColor = accentColor.copy(alpha = 0.25f)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = null,
                                tint = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "3D Пространственный звук",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${virtualizerStrength / 10}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = virtualizerStrength.toFloat(),
                        onValueChange = { eqController.setVirtualizerStrength(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else accentColor,
                            activeTrackColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else accentColor,
                            inactiveTrackColor = sliderTrackInactive
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 6. REVERB PRESETS
            // ==========================================
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = cardBgColor
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Реверберация помещения (Reverb)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val reverbOptions = listOf(
                        "Выкл" to PresetReverb.PRESET_NONE,
                        "Комната" to PresetReverb.PRESET_SMALLROOM,
                        "Большая комната" to PresetReverb.PRESET_MEDIUMROOM,
                        "Зал" to PresetReverb.PRESET_LARGEROOM,
                        "Концертный холл" to PresetReverb.PRESET_MEDIUMHALL,
                        "Большой холл" to PresetReverb.PRESET_LARGEHALL,
                        "Пластина" to PresetReverb.PRESET_PLATE
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(reverbOptions) { (label, presetVal) ->
                            val isSelected = reverbPreset == presetVal
                            FilterChip(
                                selected = isSelected,
                                onClick = { eqController.setReverbPreset(presetVal) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK || themeStyle == AppThemeStyle.RETRO_PIXEL) Color.Black else Color.White,
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Renders a smooth interactive cubic curve visualizer representing the frequency response spectrum.
 */
@Composable
private fun FrequencyCurveCanvas(
    bands: List<BandInfo>,
    isEnabled: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2.0f

        // Draw Baseline & grid lines (+12dB, 0dB, -12dB)
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(0f, midY),
            end = Offset(w, midY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.05f),
            start = Offset(0f, h * 0.15f),
            end = Offset(w, h * 0.15f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.05f),
            start = Offset(0f, h * 0.85f),
            end = Offset(w, h * 0.85f),
            strokeWidth = 1.dp.toPx()
        )

        if (bands.isEmpty() || !isEnabled) {
            // Flat line when disabled or empty
            drawLine(
                color = if (isEnabled) primaryColor else Color.Gray.copy(alpha = 0.4f),
                start = Offset(0f, midY),
                end = Offset(w, midY),
                strokeWidth = 2.5.dp.toPx()
            )
            return@Canvas
        }

        // Calculate (x, y) coordinates for each band
        val n = bands.size
        val points = mutableListOf<Offset>()

        for (i in bands.indices) {
            val band = bands[i]
            val x = (i.toFloat() + 0.5f) * (w / n.toFloat())
            val range = (band.maxLevelMb - band.minLevelMb).toFloat().coerceAtLeast(1f)
            val normalized = (band.currentLevelMb.toFloat() - band.minLevelMb.toFloat()) / range
            // normalized: 0.0 (-15dB) -> 1.0 (+15dB). Invert for screen coords
            val y = h * (1.0f - normalized).coerceIn(0.08f, 0.92f)
            points.add(Offset(x, y))
        }

        // Construct smooth cubic spline path
        val path = Path()
        val fillPath = Path()

        path.moveTo(0f, midY)
        fillPath.moveTo(0f, midY)

        path.lineTo(points[0].x * 0.3f, (midY + points[0].y) / 2f)
        fillPath.lineTo(points[0].x * 0.3f, (midY + points[0].y) / 2f)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val cx = (p0.x + p1.x) / 2f
            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        }

        path.lineTo(w, (midY + points.last().y) / 2f)
        path.lineTo(w, midY)

        fillPath.lineTo(w, (midY + points.last().y) / 2f)
        fillPath.lineTo(w, midY)
        fillPath.lineTo(w, h)
        fillPath.lineTo(0f, h)
        fillPath.close()

        // Fill area under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(
                    primaryColor.copy(alpha = 0.25f),
                    secondaryColor.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
        )

        // Draw crisp glowing stroke curve
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor, primaryColor)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw glowing frequency points
        points.forEach { pt ->
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = 7.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = pt
            )
        }
    }
}
