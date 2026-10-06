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

package com.example.openfy.core.ui.components

import android.app.Activity
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.CarModeManager
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.R
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroPhosphorGreen
import kotlinx.coroutines.delay
import java.util.Date

private fun formatCarDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

@Composable
fun CarModePlayerScreen(
    playbackManager: PlaybackManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()
    val keepScreenOnSetting by playbackManager.settingsRepository.carModeKeepScreenOn.collectAsState()

    // Manage Screen WakeLock
    DisposableEffect(keepScreenOnSetting) {
        if (keepScreenOnSetting) {
            CarModeManager.keepScreenOn(activity?.window, true)
        }
        onDispose {
            CarModeManager.keepScreenOn(activity?.window, false)
        }
    }

    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = DateFormat.format("HH:mm", Date()).toString()
            delay(1000L)
        }
    }

    val accentColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Color.White
        AppThemeStyle.MIDNIGHT_NEON -> NeonCyan
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkRubyRed
        AppThemeStyle.RETRO_PIXEL -> RetroPhosphorGreen
        AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.primary
    }

    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "carPlayScale"
    )

    var dragOffsetAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .displayCutoutPadding()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        playbackManager.playPause()
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffsetAccumulator > 80f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackManager.skipPrev()
                        } else if (dragOffsetAccumulator < -80f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackManager.skipNext()
                        }
                        dragOffsetAccumulator = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffsetAccumulator += dragAmount
                    }
                )
            }
    ) {
        if (isLandscape) {
            // =========================================================================
            // LANDSCAPE CAR DASHBOARD LAYOUT (Horizontal car phone mounts)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Artwork + Marquee Track Info + Visualizer
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top header: Back button & Car Mode Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.car_mode_exit),
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = accentColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.car_mode_badge),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Text(
                            text = currentTimeString,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Cover Art & Title
                    if (currentSong != null) {
                        val song = currentSong!!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .shadow(20.dp, RoundedCornerShape(20.dp), spotColor = accentColor.copy(alpha = 0.5f))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1E1F28))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        playbackManager.playPause()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (song.albumArtUri != null || song.albumArtUriString != null) {
                                    AsyncImage(
                                        model = song.albumArtUri ?: song.albumArtUriString,
                                        contentDescription = song.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = AppIcons.album(iconPackStyle),
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = song.title,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(iterations = Int.MAX_VALUE)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = song.artist,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(iterations = Int.MAX_VALUE)
                                )
                            }
                        }

                        if (isPlaying) {
                            VisualizerCanvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp),
                                isPlaying = true,
                                barCount = 18,
                                primaryColor = accentColor,
                                secondaryColor = accentColor.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.car_mode_empty_title),
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Right Column: Scrub Bar + Giant Transport Controls + Presets
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. High contrast scrubber
                    if (currentSong != null) {
                        val song = currentSong!!
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                            Slider(
                                value = progress,
                                onValueChange = { frac ->
                                    if (durationMs > 0) {
                                        playbackManager.seekTo((frac * durationMs).toLong())
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatCarDuration(currentPositionMs),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (song.isStream) "LIVE" else formatCarDuration(durationMs),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // 2. Transport Buttons (Landscape: 88dp Play button, 68dp Prev/Next)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                playbackManager.toggleShuffle()
                            },
                            shape = CircleShape,
                            color = if (isShuffle) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.shuffle(iconPackStyle),
                                    contentDescription = stringResource(R.string.car_mode_shuffle),
                                    tint = if (isShuffle) accentColor else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                playbackManager.skipPrev()
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = stringResource(R.string.car_mode_prev),
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        FilledIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                playbackManager.playPause()
                            },
                            modifier = Modifier
                                .size(88.dp)
                                .scale(playButtonScale),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = accentColor,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) stringResource(R.string.car_mode_pause) else stringResource(R.string.car_mode_play),
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                playbackManager.skipNext()
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = stringResource(R.string.car_mode_next),
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        val isFav = currentSong?.let { favorites.contains(it.id) } == true
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentSong?.let { playbackManager.playlistRepository.toggleFavorite(it.id) }
                            },
                            shape = CircleShape,
                            color = if (isFav) NeonPink.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = stringResource(R.string.car_mode_favorite),
                                    tint = if (isFav) NeonPink else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    // 3. Quick Presets Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CarPresetButton(
                            icon = Icons.Default.Explore,
                            title = stringResource(R.string.car_mode_preset_road),
                            modifier = Modifier.weight(1f),
                            accentColor = accentColor,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val queue = playbackManager.queue.value
                                val tracks = if (queue.isNotEmpty()) queue else listOfNotNull(playbackManager.currentSong.value)
                                if (tracks.isNotEmpty()) {
                                    playbackManager.playSongs(tracks.shuffled(), 0)
                                }
                            }
                        )

                        CarPresetButton(
                            icon = Icons.Default.Favorite,
                            title = stringResource(R.string.car_mode_preset_favorites),
                            modifier = Modifier.weight(1f),
                            accentColor = accentColor,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val favIds = playbackManager.playlistRepository.favorites.value
                                val allSongs = playbackManager.queue.value.ifEmpty { listOfNotNull(playbackManager.currentSong.value) }
                                val favSongs = allSongs.filter { favIds.contains(it.id) }
                                if (favSongs.isNotEmpty()) {
                                    playbackManager.playSongs(favSongs, 0)
                                }
                            }
                        )

                        CarPresetButton(
                            icon = Icons.Default.Shuffle,
                            title = stringResource(R.string.car_mode_preset_mix),
                            modifier = Modifier.weight(1f),
                            accentColor = accentColor,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!playbackManager.isShuffle.value) {
                                    playbackManager.toggleShuffle()
                                }
                                playbackManager.skipNext()
                            }
                        )

                        CarPresetButton(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.car_mode_preset_recent),
                            modifier = Modifier.weight(1f),
                            accentColor = accentColor,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val recentIds = playbackManager.playlistRepository.recentlyPlayed.value
                                val allSongs = playbackManager.queue.value.ifEmpty { listOfNotNull(playbackManager.currentSong.value) }
                                val recentSongs = allSongs.filter { recentIds.contains(it.id) }
                                if (recentSongs.isNotEmpty()) {
                                    playbackManager.playSongs(recentSongs, 0)
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // =========================================================================
            // PORTRAIT CAR DASHBOARD LAYOUT (Standard vertical phone mounts)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. DRIVE-SAFE TOP HEADER (Exit, Car Badge, Live Clock)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.car_mode_exit),
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.car_mode_badge),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Text(
                        text = currentTimeString,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // 2. CENTER TRACK INFO & VISUALIZER
                if (currentSong != null) {
                    val song = currentSong!!

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Artwork Thumbnail with Glow
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = accentColor.copy(alpha = 0.5f))
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color(0xFF1E1F28))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    playbackManager.playPause()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (song.albumArtUri != null || song.albumArtUriString != null) {
                                AsyncImage(
                                    model = song.albumArtUri ?: song.albumArtUriString,
                                    contentDescription = song.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = AppIcons.album(iconPackStyle),
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = song.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(iterations = Int.MAX_VALUE)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = song.artist,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee(iterations = Int.MAX_VALUE)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isPlaying) {
                            VisualizerCanvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                isPlaying = true,
                                barCount = 20,
                                primaryColor = accentColor,
                                secondaryColor = accentColor.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // 3. SEEKBAR WITH HIGH CONTRAST
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                        Slider(
                            value = progress,
                            onValueChange = { frac ->
                                if (durationMs > 0) {
                                    playbackManager.seekTo((frac * durationMs).toLong())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatCarDuration(currentPositionMs),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (song.isStream) "LIVE" else formatCarDuration(durationMs),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.car_mode_empty_title),
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. DRIVE-SAFE GIANT CONTROLS ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackManager.toggleShuffle()
                        },
                        shape = CircleShape,
                        color = if (isShuffle) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = AppIcons.shuffle(iconPackStyle),
                                contentDescription = stringResource(R.string.car_mode_shuffle),
                                tint = if (isShuffle) accentColor else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Previous Track Button (Large 72dp)
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackManager.skipPrev()
                        },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = stringResource(R.string.car_mode_prev),
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Play / Pause Giant Button (92dp)
                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackManager.playPause()
                        },
                        modifier = Modifier
                            .size(92.dp)
                            .scale(playButtonScale),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) stringResource(R.string.car_mode_pause) else stringResource(R.string.car_mode_play),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Next Track Button (Large 72dp)
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            playbackManager.skipNext()
                        },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = stringResource(R.string.car_mode_next),
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Favorite Button
                    val isFav = currentSong?.let { favorites.contains(it.id) } == true
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentSong?.let { playbackManager.playlistRepository.toggleFavorite(it.id) }
                        },
                        shape = CircleShape,
                        color = if (isFav) NeonPink.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.car_mode_favorite),
                                tint = if (isFav) NeonPink else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. QUICK CAR PRESETS BAR (Grid of 4 Drive-Safe Buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarPresetButton(
                        icon = Icons.Default.Explore,
                        title = stringResource(R.string.car_mode_preset_road),
                        modifier = Modifier.weight(1f),
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val queue = playbackManager.queue.value
                            val tracks = if (queue.isNotEmpty()) queue else listOfNotNull(playbackManager.currentSong.value)
                            if (tracks.isNotEmpty()) {
                                playbackManager.playSongs(tracks.shuffled(), 0)
                            }
                        }
                    )

                    CarPresetButton(
                        icon = Icons.Default.Favorite,
                        title = stringResource(R.string.car_mode_preset_favorites),
                        modifier = Modifier.weight(1f),
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val favIds = playbackManager.playlistRepository.favorites.value
                            val allSongs = playbackManager.queue.value.ifEmpty { listOfNotNull(playbackManager.currentSong.value) }
                            val favSongs = allSongs.filter { favIds.contains(it.id) }
                            if (favSongs.isNotEmpty()) {
                                playbackManager.playSongs(favSongs, 0)
                            }
                        }
                    )

                    CarPresetButton(
                        icon = Icons.Default.Shuffle,
                        title = stringResource(R.string.car_mode_preset_mix),
                        modifier = Modifier.weight(1f),
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (!playbackManager.isShuffle.value) {
                                playbackManager.toggleShuffle()
                            }
                            playbackManager.skipNext()
                        }
                    )

                    CarPresetButton(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.car_mode_preset_recent),
                        modifier = Modifier.weight(1f),
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val recentIds = playbackManager.playlistRepository.recentlyPlayed.value
                            val allSongs = playbackManager.queue.value.ifEmpty { listOfNotNull(playbackManager.currentSong.value) }
                            val recentSongs = allSongs.filter { recentIds.contains(it.id) }
                            if (recentSongs.isNotEmpty()) {
                                playbackManager.playSongs(recentSongs, 0)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CarPresetButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}
