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

package com.example.openfy.ui.mood

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodDialSheet(
    allSongs: List<Song>,
    playbackManager: PlaybackManager,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hapticFeedback = LocalHapticFeedback.current

    // Current pointer coordinates (-1f..+1f)
    var targetValence by remember { mutableFloatStateOf(0.45f) }
    var targetEnergy by remember { mutableFloatStateOf(0.60f) }

    val currentQuadrant by remember {
        derivedStateOf { MoodQuadrant.fromCoordinates(targetValence, targetEnergy) }
    }

    val matchedSongs by remember(targetValence, targetEnergy, allSongs) {
        derivedStateOf {
            MoodClassifier.findMatchingSongs(allSongs, targetValence, targetEnergy, limit = 25)
        }
    }

    val animatedAccent by animateColorAsState(
        targetValue = currentQuadrant.accentColor,
        label = "AccentAnim"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF10121A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ЗВУКОВОЙ КОМПАС",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = animatedAccent,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Музыка под ваше настроение",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 2D CIRCULAR RADAR COMPASS
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Color(0xFF090A0F))
                    .border(2.dp, animatedAccent.copy(alpha = 0.35f), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val maxR = size.width / 2f * 0.88f

                            val dx = offset.x - cx
                            val dy = offset.y - cy
                            val dist = hypot(dx, dy)
                            val angle = atan2(dy, dx)
                            val clampedR = dist.coerceAtMost(maxR)

                            val nx = (clampedR * cos(angle)) / maxR
                            val ny = -(clampedR * sin(angle)) / maxR

                            targetValence = nx.coerceIn(-1f, 1f)
                            targetEnergy = ny.coerceIn(-1f, 1f)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val maxR = size.width / 2f * 0.88f

                            val dx = change.position.x - cx
                            val dy = change.position.y - cy
                            val dist = hypot(dx, dy)
                            val angle = atan2(dy, dx)
                            val clampedR = dist.coerceAtMost(maxR)

                            val nx = (clampedR * cos(angle)) / maxR
                            val ny = -(clampedR * sin(angle)) / maxR

                            targetValence = nx.coerceIn(-1f, 1f)
                            targetEnergy = ny.coerceIn(-1f, 1f)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background Coordinate Axes & Radar Circles
                Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)
                    val r = w / 2f * 0.88f

                    // Concentric rings
                    drawCircle(Color.White.copy(alpha = 0.05f), radius = r * 0.33f, style = Stroke(1.5f))
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = r * 0.66f, style = Stroke(1.5f))
                    drawCircle(Color.White.copy(alpha = 0.12f), radius = r, style = Stroke(1.5f))

                    // Cross Axes (Valence X, Energy Y)
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(center.x - r, center.y),
                        end = Offset(center.x + r, center.y),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(center.x, center.y - r),
                        end = Offset(center.x, center.y + r),
                        strokeWidth = 1.5f
                    )

                    // Glowing pin position
                    val pinX = center.x + (targetValence * r)
                    val pinY = center.y - (targetEnergy * r)

                    // Line to center
                    drawLine(
                        color = animatedAccent.copy(alpha = 0.5f),
                        start = center,
                        end = Offset(pinX, pinY),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Pin Outer Aura
                    drawCircle(
                        color = animatedAccent.copy(alpha = 0.35f),
                        radius = 20.dp.toPx(),
                        center = Offset(pinX, pinY)
                    )
                    // Pin Core
                    drawCircle(
                        color = animatedAccent,
                        radius = 10.dp.toPx(),
                        center = Offset(pinX, pinY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(pinX, pinY)
                    )
                }

                // Axis Labels
                Text(
                    text = "ЭНЕРГИЯ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                )
                Text(
                    text = "РЕЛАКС",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                )
                Text(
                    text = "ТЬМА",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp)
                )
                Text(
                    text = "СВЕТ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CURRENT MOOD BADGE & DESCRIPTION
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = animatedAccent.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, animatedAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentQuadrant.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = animatedAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentQuadrant.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "В очереди: ${matchedSongs.size} треков",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PLAY MOOD FLOW BUTTON
            Button(
                onClick = {
                    if (matchedSongs.isNotEmpty()) {
                        playbackManager.playSongs(matchedSongs, 0)
                        onDismiss()
                    }
                },
                enabled = matchedSongs.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = animatedAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Запустить поток настроения",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
