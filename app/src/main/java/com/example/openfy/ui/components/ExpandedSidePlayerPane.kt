package com.example.openfy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.IconPackStyle
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.VisualizerCanvas
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import com.example.openfy.core.ui.theme.RetroPhosphorGreen

private fun formatSideDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

@Composable
fun ExpandedSidePlayerPane(
    playbackManager: PlaybackManager,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    onNavigateToCarMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()

    val accentColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Color.White
        AppThemeStyle.MIDNIGHT_NEON -> NeonCyan
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkRubyRed
        AppThemeStyle.RETRO_PIXEL -> RetroPhosphorGreen
        AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.primary
    }

    val paneBg = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface.copy(alpha = 0.95f)
        AppThemeStyle.MIDNIGHT_NEON -> GlassDarkSurface.copy(alpha = 0.85f)
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg.copy(alpha = 0.95f)
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg.copy(alpha = 0.95f)
        AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }

    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "playScale"
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(12.dp),
        shape = RoundedCornerShape(28.dp),
        color = paneBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)),
        shadowElevation = 12.dp
    ) {
        if (currentSong == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Выберите трек для воспроизведения",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val song = currentSong!!
            val isFavorite = favorites.contains(song.id)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (song.isStream) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ONLINE STREAM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Сейчас играет",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }

                    Row {
                        IconButton(onClick = onNavigateToCarMode) {
                            Icon(
                                imageVector = AppIcons.car,
                                contentDescription = "Автомобильный режим",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onNavigateToLyrics) {
                            Icon(
                                imageVector = Icons.Default.Lyrics,
                                contentDescription = "Текст песни",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onNavigateToEqualizer) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = "Эквалайзер",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Album Art with Shadow and Neon Glow
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = accentColor.copy(alpha = 0.4f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
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
                            tint = accentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }

                // Track Info & Visualizer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isPlaying) {
                        VisualizerCanvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp),
                            isPlaying = true,
                            barCount = 20,
                            primaryColor = accentColor,
                            secondaryColor = accentColor.copy(alpha = 0.4f)
                        )
                    }
                }

                // Seekbar
                Column(modifier = Modifier.fillMaxWidth()) {
                    val sliderValue = if (isDraggingSlider) {
                        sliderDragPosition
                    } else {
                        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                    }

                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            isDraggingSlider = true
                            sliderDragPosition = it
                        },
                        onValueChangeFinished = {
                            if (durationMs > 0) {
                                val seekTarget = (sliderDragPosition * durationMs).toLong()
                                playbackManager.seekTo(seekTarget)
                            }
                            isDraggingSlider = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val displayPos = if (isDraggingSlider) (sliderDragPosition * durationMs).toLong() else currentPositionMs
                        Text(
                            text = formatSideDuration(displayPos),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (song.isStream) "Live" else formatSideDuration(durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { playbackManager.toggleShuffle() }) {
                        Icon(
                            imageVector = AppIcons.shuffle(iconPackStyle),
                            contentDescription = "Перемешать",
                            tint = if (isShuffle) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    IconButton(onClick = { playbackManager.skipPrev() }) {
                        Icon(
                            imageVector = AppIcons.previous(iconPackStyle),
                            contentDescription = "Предыдущий трек",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = { playbackManager.playPause() },
                        modifier = Modifier
                            .size(56.dp)
                            .scale(playButtonScale),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = accentColor,
                            contentColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK || themeStyle == AppThemeStyle.RETRO_PIXEL) Color.Black else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) AppIcons.pause(iconPackStyle) else AppIcons.play(iconPackStyle),
                            contentDescription = if (isPlaying) "Пауза" else "Воспроизведение",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = { playbackManager.skipNext() }) {
                        Icon(
                            imageVector = AppIcons.next(iconPackStyle),
                            contentDescription = "Следующий трек",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(onClick = { playbackManager.playlistRepository.toggleFavorite(song.id) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Избранное",
                            tint = if (isFavorite) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
