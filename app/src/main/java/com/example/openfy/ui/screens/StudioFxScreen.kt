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

package com.example.openfy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.audio.service.VocalRemoverController
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmberGlow
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioFxScreen(
    playbackManager: PlaybackManager,
    onBack: () -> Unit
) {
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val speed by playbackManager.speed.collectAsState()
    val pitch by playbackManager.pitch.collectAsState()
    val audioEnergy by playbackManager.audioEnergy.collectAsState()
    val abLoopStartMs by playbackManager.abLoopStartMs.collectAsState()
    val abLoopEndMs by playbackManager.abLoopEndMs.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()

    val isKaraokeEnabled by VocalRemoverController.isKaraokeEnabled.collectAsState()
    val vocalLevel by VocalRemoverController.vocalLevel.collectAsState()

    val scrollState = rememberScrollState()

    // Calculate semitones from pitch: pitch = 2^(semitones / 12) -> semitones = 12 * log2(pitch)
    val currentSemitones = remember(pitch) {
        if (pitch <= 0.01f) 0
        else {
            val semitones = (12.0 * kotlin.math.ln(pitch.toDouble()) / kotlin.math.ln(2.0)).roundToInt()
            semitones.coerceIn(-6, 6)
        }
    }

    DynamicBackground(isPlaying = isPlaying, themeStyle = themeStyle) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // =================================================================
            // Top Bar
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Студия FX & Караоке",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentSong?.title ?: "Трек не выбран",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        playbackManager.resetPlaybackParameters()
                        playbackManager.clearABLoop()
                        VocalRemoverController.setKaraokeEnabled(false)
                        VocalRemoverController.setVocalLevel(1.0f)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Сброс всех эффектов",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // =================================================================
            // Scrollable Content
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // =============================================================
                // 1. Apple Music Sing: Караоке & Подавление вокала
                // =============================================================
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isKaraokeEnabled) NeonPink.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isKaraokeEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                        contentDescription = null,
                                        tint = if (isKaraokeEnabled) NeonPink else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Караоке (Apple Music Sing)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isKaraokeEnabled) {
                                            if (vocalLevel <= 0.05f) "Минусовка (вокал убран)"
                                            else "Уровень голоса: ${(vocalLevel * 100).toInt()}%"
                                        } else "Подавление вокала в реальном времени",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = if (isKaraokeEnabled) NeonCyan else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Button(
                                onClick = { VocalRemoverController.toggleKaraoke() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isKaraokeEnabled) NeonPink else Color.White.copy(alpha = 0.12f),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isKaraokeEnabled) "ВКЛ" else "ВЫКЛ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isKaraokeEnabled,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Чистый минус",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Оригинал",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Slider(
                                    value = vocalLevel,
                                    onValueChange = { VocalRemoverController.setVocalLevel(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonPink,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                }

                // =============================================================
                // 2. Тональность (Key & Pitch Shifter)
                // =============================================================
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (currentSemitones != 0) AmberGlow.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (currentSemitones != 0) AmberGlow else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Тональность (Pitch)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (currentSemitones == 0) "Исходная тональность"
                                        else if (currentSemitones > 0) "+$currentSemitones полутонов"
                                        else "$currentSemitones полутонов",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = if (currentSemitones != 0) AmberGlow else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Quick reset
                            if (currentSemitones != 0) {
                                Button(
                                    onClick = {
                                        playbackManager.setPitch(1.0f)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.12f),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Сброс", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stepper buttons for semitones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val newSemi = (currentSemitones - 1).coerceAtLeast(-6)
                                    val newPitch = 2.0.pow(newSemi / 12.0).toFloat()
                                    playbackManager.setPitch(newPitch)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.12f)
                                )
                            ) {
                                Text("-1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Text(
                                text = if (currentSemitones == 0) "0" else if (currentSemitones > 0) "+$currentSemitones" else "$currentSemitones",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = if (currentSemitones != 0) AmberGlow else Color.White
                            )

                            Button(
                                onClick = {
                                    val newSemi = (currentSemitones + 1).coerceAtMost(6)
                                    val newPitch = 2.0.pow(newSemi / 12.0).toFloat()
                                    playbackManager.setPitch(newPitch)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.12f)
                                )
                            ) {
                                Text("+1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // =============================================================
                // 3. Скорость & Атмосферные Пресеты (Speed & Presets)
                // =============================================================
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (speed != 1.0f) NeonCyan.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = if (speed != 1.0f) NeonCyan else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Скорость воспроизведения",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = String.format(Locale.ROOT, "%.2fx", speed),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = if (speed != 1.0f) NeonCyan else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            if (speed != 1.0f) {
                                Button(
                                    onClick = { playbackManager.setSpeed(1.0f) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.12f),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("1.0x", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Slider(
                            value = speed,
                            onValueChange = { playbackManager.setSpeed((it * 20).roundToInt() / 20f) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Presets
                        Text(
                            text = "Фирменные пресеты",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Nightcore: 1.25x speed, +2 pitch
                            FilterChip(
                                selected = (speed == 1.25f && currentSemitones == 2),
                                onClick = {
                                    playbackManager.setPlaybackParameters(
                                        speed = 1.25f,
                                        pitch = 2.0.pow(2.0 / 12.0).toFloat()
                                    )
                                },
                                label = { Text("Nightcore", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPink.copy(alpha = 0.35f),
                                    selectedLabelColor = Color.White
                                )
                            )

                            // Slowed: 0.85x speed, -2 pitch
                            FilterChip(
                                selected = (speed == 0.85f && currentSemitones == -2),
                                onClick = {
                                    playbackManager.setPlaybackParameters(
                                        speed = 0.85f,
                                        pitch = 2.0.pow(-2.0 / 12.0).toFloat()
                                    )
                                },
                                label = { Text("Slowed", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.35f),
                                    selectedLabelColor = Color.White
                                )
                            )

                            // Speed Up: 1.15x
                            FilterChip(
                                selected = (speed == 1.15f && currentSemitones == 0),
                                onClick = {
                                    playbackManager.setPlaybackParameters(speed = 1.15f, pitch = 1.0f)
                                },
                                label = { Text("1.15x", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberGlow.copy(alpha = 0.35f),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // =============================================================
                // 4. A-B Зацикливание (Looper)
                // =============================================================
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (abLoopStartMs != null) NeonPink.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        tint = if (abLoopStartMs != null) NeonPink else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "A-B Зацикливание (Looper)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (abLoopStartMs != null && abLoopEndMs != null) {
                                            val s = abLoopStartMs!! / 1000
                                            val e = abLoopEndMs!! / 1000
                                            String.format(Locale.ROOT, "%02d:%02d ➔ %02d:%02d", s / 60, s % 60, e / 60, e % 60)
                                        } else if (abLoopStartMs != null) {
                                            val s = abLoopStartMs!! / 1000
                                            String.format(Locale.ROOT, "Точка A: %02d:%02d (выберите B)", s / 60, s % 60)
                                        } else {
                                            "Зацикливание соло или припева"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = if (abLoopStartMs != null) NeonPink else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            if (abLoopStartMs != null || abLoopEndMs != null) {
                                Button(
                                    onClick = { playbackManager.clearABLoop() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.12f),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Сброс", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { playbackManager.setPointA() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (abLoopStartMs != null) NeonPink.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f)
                                )
                            ) {
                                Text("Точка [A]", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { playbackManager.setPointB() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (abLoopEndMs != null) NeonCyan.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f)
                                )
                            ) {
                                Text("Точка [B]", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
