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

import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.IconPackStyle
import com.example.openfy.core.audio.model.Album
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.AddToPlaylistBottomSheet
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AmoledSurfaceVariant
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CoralOrange
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.DarkSurfaceElevated
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.sin

enum class HomeFilter(val title: String) {
    ALL("Все"),
    FAVORITES("Любимые"),
    POPULAR("Часто слушаете")
}

private data class QuickBlockData(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector?,
    val imageUri: Uri?,
    val gradient: Brush,
    val onClick: () -> Unit,
    val onPlay: () -> Unit
)

@Composable
fun HomeScreen(
    playbackManager: PlaybackManager,
    allSongs: List<Song>,
    albums: List<Album> = emptyList(),
    onRefreshAudio: () -> Unit,
    onNavigateToPlaylist: (String) -> Unit = {},
    onOpenStreamDialog: (String) -> Unit = {},
    onDeleteSong: (Song) -> Unit = {},
    onNavigateToEqualizer: () -> Unit = {},
    onNavigateToCarMode: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {}
) {
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val playlists by playbackManager.playlistRepository.playlists.collectAsState()
    val playCounts by playbackManager.playlistRepository.playCounts.collectAsState()
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()

    var selectedFilter by remember { mutableStateOf(HomeFilter.ALL) }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    val favoriteSongs = remember(allSongs, favorites) {
        allSongs.filter { favorites.contains(it.id) }
    }

    val customPlaylists = remember(playlists) {
        playlists.filter { !it.isSystemFavorites }
    }

    val recommendedSongs = remember(allSongs, playCounts) {
        allSongs.sortedByDescending { playCounts[it.id] ?: 0 }.take(12)
    }

    // Dynamic Time-of-Day Greeting
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingText = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Доброе утро"
            in 12..17 -> "Добрый день"
            in 18..22 -> "Добрый вечер"
            else -> "Доброй ночи"
        }
    }

    val primaryAccent = MaterialTheme.colorScheme.primary

    val cardSurfaceColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
        else -> GlassDarkSurface
    }

    val tileSurfaceColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledSurfaceVariant
        AppThemeStyle.CYBERPUNK_BLOOD -> Color(0xFF1F070E)
        AppThemeStyle.RETRO_PIXEL -> Color(0xFF0A1C10)
        else -> DarkSurfaceElevated.copy(alpha = 0.75f)
    }

    // Prepare Quick Access Blocks: Favorites, Most Played, and Custom Playlists
    val quickBlocks = remember(favoriteSongs.size, recommendedSongs.size, customPlaylists, allSongs, iconPackStyle) {
        val list = mutableListOf<QuickBlockData>()

        // 1. Блок Любимые треки
        list.add(
            QuickBlockData(
                id = "favorites",
                title = "Любимые треки",
                subtitle = "${favoriteSongs.size} треков",
                icon = AppIcons.favoriteFilled(iconPackStyle),
                imageUri = null,
                gradient = Brush.linearGradient(listOf(NeonPink, ElectricPurple)),
                onClick = {
                    if (favoriteSongs.isNotEmpty()) {
                        playbackManager.playSongs(favoriteSongs, 0)
                    } else {
                        onNavigateToLibrary()
                    }
                },
                onPlay = {
                    if (favoriteSongs.isNotEmpty()) {
                        playbackManager.playSongs(favoriteSongs, 0)
                    }
                }
            )
        )

        // 2. Блок Часто слушаете
        list.add(
            QuickBlockData(
                id = "popular",
                title = "Часто слушаете",
                subtitle = "${recommendedSongs.size} треков",
                icon = AppIcons.flame,
                imageUri = null,
                gradient = Brush.linearGradient(listOf(CoralOrange, Color(0xFFFFB300))),
                onClick = {
                    if (recommendedSongs.isNotEmpty()) {
                        playbackManager.playSongs(recommendedSongs, 0)
                    } else if (allSongs.isNotEmpty()) {
                        playbackManager.playSongs(allSongs, 0)
                    }
                },
                onPlay = {
                    if (recommendedSongs.isNotEmpty()) {
                        playbackManager.playSongs(recommendedSongs, 0)
                    } else if (allSongs.isNotEmpty()) {
                        playbackManager.playSongs(allSongs, 0)
                    }
                }
            )
        )

        // 3. Созданные плейлисты в виде блоков
        customPlaylists.forEach { playlist ->
            val plSongs = playlist.songIds.mapNotNull { id -> allSongs.find { it.id == id } }
            list.add(
                QuickBlockData(
                    id = "pl_${playlist.id}",
                    title = playlist.name,
                    subtitle = "${plSongs.size} треков",
                    icon = AppIcons.playlist,
                    imageUri = playlist.customCoverUri?.let { Uri.parse(it) },
                    gradient = Brush.linearGradient(listOf(ElectricPurple, Color(0xFF7C3AED))),
                    onClick = { onNavigateToPlaylist(playlist.id) },
                    onPlay = {
                        if (plSongs.isNotEmpty()) {
                            playbackManager.playSongs(plSongs, 0)
                        }
                    }
                )
            )
        }

        list
    }

    val chunkedBlocks = remember(quickBlocks) { quickBlocks.chunked(2) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // =====================================================================
        // 1. HEADER (Контекстное приветствие + Быстрые кнопки)
        // =====================================================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${allSongs.size} треков • Офлайн-медиатека",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = AppIcons.search,
                            contentDescription = "Поиск",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToEqualizer) {
                        Icon(
                            imageVector = AppIcons.equalizer,
                            contentDescription = "Эквалайзер",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToCarMode) {
                        Icon(
                            imageVector = AppIcons.car,
                            contentDescription = "Режим авто",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onRefreshAudio) {
                        Icon(
                            imageVector = AppIcons.refresh,
                            contentDescription = "Обновить медиатеку",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 2. FILTER CHIPS
        // =====================================================================
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HomeFilter.values(), key = { it.name }) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccent,
                            selectedLabelColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                            containerColor = tileSurfaceColor,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) primaryAccent else Color.White.copy(alpha = 0.08f),
                            selectedBorderColor = primaryAccent,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        // =====================================================================
        // 3. БЛОКИ БЫСТРОГО ДОСТУПА (Любимые, Часто слушаете и Плейлисты)
        // =====================================================================
        if (selectedFilter == HomeFilter.ALL) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chunkedBlocks.forEach { rowBlocks ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowBlocks.forEach { block ->
                                QuickAccessTile(
                                    modifier = Modifier.weight(1f),
                                    title = block.title,
                                    subtitle = block.subtitle,
                                    icon = block.icon,
                                    imageUri = block.imageUri,
                                    gradient = block.gradient,
                                    tileBackground = tileSurfaceColor,
                                    primaryAccent = primaryAccent,
                                    onClick = block.onClick,
                                    onPlay = block.onPlay
                                )
                            }
                            if (rowBlocks.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 4. СЕКТОР: ЛЮБИМЫЕ ТРЕКИ
        // =====================================================================
        if ((selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.FAVORITES) && favoriteSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.favoriteFilled(iconPackStyle),
                            contentDescription = null,
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Любимые треки",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${favoriteSongs.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { playbackManager.playSongs(favoriteSongs, 0) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.play,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Играть",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccent
                        )
                    }
                }
            }

            items(favoriteSongs, key = { "fav_${it.id}" }) { song ->
                TrackWaveRow(
                    song = song,
                    isPlaying = currentSong?.id == song.id && isPlaying,
                    isFavorite = true,
                    playCount = playCounts[song.id] ?: 0,
                    primaryAccent = primaryAccent,
                    themeStyle = themeStyle,
                    iconPackStyle = iconPackStyle,
                    onClick = { playbackManager.playSongFromList(favoriteSongs, song) },
                    onFavoriteToggle = { playbackManager.playlistRepository.toggleFavorite(song.id) },
                    onAddToPlaylist = { songForAddToPlaylist = song },
                    onDeleteFromDevice = { onDeleteSong(song) }
                )
            }
        }

        // =====================================================================
        // 5. СЕКТОР: ЧАСТО ПРОСЛУШИВАЕМЫЕ ТРЕКИ
        // =====================================================================
        if ((selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.POPULAR) && recommendedSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.flame,
                            contentDescription = null,
                            tint = CoralOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Часто слушаете",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Топ прослушиваний",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(recommendedSongs, key = { "pop_${it.id}" }) { song ->
                TrackWaveRow(
                    song = song,
                    isPlaying = currentSong?.id == song.id && isPlaying,
                    isFavorite = favorites.contains(song.id),
                    playCount = playCounts[song.id] ?: 0,
                    primaryAccent = primaryAccent,
                    themeStyle = themeStyle,
                    iconPackStyle = iconPackStyle,
                    onClick = { playbackManager.playSongFromList(recommendedSongs, song) },
                    onFavoriteToggle = { playbackManager.playlistRepository.toggleFavorite(song.id) },
                    onAddToPlaylist = { songForAddToPlaylist = song },
                    onDeleteFromDevice = { onDeleteSong(song) }
                )
            }
        }

        // =====================================================================
        // 6. ПУСТАЯ МЕДИАТЕКА
        // =====================================================================
        if (allSongs.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = cardSurfaceColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.music(iconPackStyle),
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Медиатека пуста",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Добавьте аудиофайлы на устройство или запустите сканирование памяти.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onRefreshAudio,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccent)
                        ) {
                            Icon(AppIcons.refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Сканировать устройство")
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Add to Playlist
    songForAddToPlaylist?.let { song ->
        AddToPlaylistBottomSheet(
            song = song,
            playbackManager = playbackManager,
            onDismiss = { songForAddToPlaylist = null }
        )
    }
}

// =============================================================================
// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ
// =============================================================================

/**
 * Компактный блок 2x3 с векторной SVG иконкой и отдельной кнопкой Play.
 */
@Composable
fun QuickAccessTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    imageUri: Uri? = null,
    gradient: Brush,
    tileBackground: Color,
    primaryAccent: Color,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(tileBackground)
            .border(0.8.dp, Color.White.copy(alpha = 0.07f), shape)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Artwork / Icon Box
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title and Subtitle
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Circular Play Button on the right
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(primaryAccent.copy(alpha = 0.9f))
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppIcons.play,
                contentDescription = "Воспроизвести",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Строка трека с мини-визуализатором звуковой волны (Waveform) и SVG-иконками (без эмодзи).
 */
@Composable
fun TrackWaveRow(
    song: Song,
    isPlaying: Boolean,
    isFavorite: Boolean,
    playCount: Int,
    primaryAccent: Color,
    themeStyle: AppThemeStyle,
    iconPackStyle: IconPackStyle,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDeleteFromDevice: () -> Unit
) {
    val rowShape = RoundedCornerShape(16.dp)

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val waveAnimPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(rowShape)
            .background(
                if (isPlaying) primaryAccent.copy(alpha = 0.12f) else Color.Transparent
            )
            .then(
                if (isPlaying) Modifier.border(1.dp, primaryAccent, rowShape) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Artwork with Play/Pause Overlay
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUriString != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.album,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = AppIcons.music(iconPackStyle),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.pause,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title, Artist and Mini Waveform
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) primaryAccent else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (playCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.flame,
                            contentDescription = null,
                            tint = CoralOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$playCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Mini Waveform Visualizer Canvas
            val songHash = remember(song.id) { abs(song.id.hashCode()) }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                val numBars = 32
                val barWidth = 2.dp.toPx()
                val spacing = (size.width - (numBars * barWidth)) / (numBars - 1)
                val maxBarHeight = size.height

                for (i in 0 until numBars) {
                    val pseudoRandomFactor = (((songHash xor (i * 7919)) % 100) / 100f).coerceIn(0.2f, 1f)
                    val animatedFactor = if (isPlaying) {
                        val wave = (sin(waveAnimPhase + i * 0.35f) + 1f) / 2f
                        (pseudoRandomFactor * 0.6f + wave * 0.4f).coerceIn(0.2f, 1f)
                    } else {
                        pseudoRandomFactor
                    }

                    val barHeight = (maxBarHeight * animatedFactor).coerceAtLeast(3.dp.toPx())
                    val x = i * (barWidth + spacing)
                    val y = size.height - barHeight

                    val barColor = if (isPlaying) {
                        CoralOrange
                    } else {
                        primaryAccent.copy(alpha = 0.40f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Actions: Favorite Heart & Duration
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) AppIcons.favoriteFilled(iconPackStyle) else AppIcons.favoriteOutline(iconPackStyle),
                    contentDescription = "Избранное",
                    tint = if (isFavorite) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = song.formattedDuration,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}
