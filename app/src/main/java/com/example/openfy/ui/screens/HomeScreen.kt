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
import androidx.compose.foundation.layout.aspectRatio
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
import com.example.openfy.core.audio.model.Playlist
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.AddToPlaylistBottomSheet
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmberGlow
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AmoledSurfaceVariant
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CoralOrange
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.DarkSurfaceElevated
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.sin

enum class HomeFilter(val title: String) {
    ALL("Все"),
    FAVORITES("Любимые"),
    PLAYLISTS("Плейлисты"),
    POPULAR("Часто слушаете"),
    RECENT("Недавние"),
    ALBUMS("Альбомы")
}

@Composable
fun HomeScreen(
    playbackManager: PlaybackManager,
    allSongs: List<Song>,
    albums: List<Album>,
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
    val recentlyPlayedIds by playbackManager.playlistRepository.recentlyPlayed.collectAsState()
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

    val recentSongs = remember(allSongs, recentlyPlayedIds) {
        recentlyPlayedIds.mapNotNull { id -> allSongs.find { it.id == id } }.take(15)
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // =====================================================================
        // 1. HEADER (Smart Greeting + Quick Action Buttons)
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
        // 3. 6-TILE QUICK ACCESS GRID (2x3 Compact Tiles with SVG Icons)
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
                    // Row 1: Избранное & Недавние
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAccessTile(
                            modifier = Modifier.weight(1f),
                            title = "Любимые треки",
                            subtitle = "${favoriteSongs.size} треков",
                            icon = AppIcons.favoriteFilled(iconPackStyle),
                            gradient = Brush.linearGradient(listOf(NeonPink, ElectricPurple)),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                if (favoriteSongs.isNotEmpty()) {
                                    playbackManager.playSongs(favoriteSongs, 0)
                                } else {
                                    onNavigateToLibrary()
                                }
                            }
                        )

                        QuickAccessTile(
                            modifier = Modifier.weight(1f),
                            title = "Слушали недавно",
                            subtitle = "${recentSongs.size} треков",
                            icon = AppIcons.queue(iconPackStyle),
                            imageUri = recentSongs.firstOrNull()?.albumArtUri,
                            gradient = Brush.linearGradient(listOf(NeonCyan, Color(0xFF0077FF))),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                if (recentSongs.isNotEmpty()) {
                                    playbackManager.playSongs(recentSongs, 0)
                                } else if (allSongs.isNotEmpty()) {
                                    playbackManager.playSongs(allSongs, 0)
                                }
                            }
                        )
                    }

                    // Row 2: Часто слушаете & Плейлисты
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAccessTile(
                            modifier = Modifier.weight(1f),
                            title = "Часто слушаете",
                            subtitle = "${recommendedSongs.size} треков",
                            icon = AppIcons.flame,
                            gradient = Brush.linearGradient(listOf(CoralOrange, AmberGlow)),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                if (recommendedSongs.isNotEmpty()) {
                                    playbackManager.playSongs(recommendedSongs, 0)
                                } else if (allSongs.isNotEmpty()) {
                                    playbackManager.playSongs(allSongs, 0)
                                }
                            }
                        )

                        QuickAccessTile(
                            modifier = Modifier.weight(1f),
                            title = "Мои плейлисты",
                            subtitle = "${customPlaylists.size} плейлистов",
                            icon = AppIcons.playlist,
                            gradient = Brush.linearGradient(listOf(ElectricPurple, Color(0xFF7C3AED))),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                if (customPlaylists.isNotEmpty()) {
                                    onNavigateToPlaylist(customPlaylists.first().id)
                                } else {
                                    onNavigateToLibrary()
                                }
                            }
                        )
                    }

                    // Row 3: Звуки природы & Перемешать всё
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAccessTile(
                            modifier = Modifier.weight(1f),
                            title = "Звуки природы",
                            subtitle = "Сон & Релакс",
                            icon = AppIcons.spa,
                            gradient = Brush.linearGradient(listOf(Color(0xFF00B09B), Color(0xFF96C93D))),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                playbackManager.zenNatureAudioEngine.startNatureSound(
                                    com.example.openfy.core.audio.service.NatureSoundType.NIGHT_RAIN,
                                    0.4f
                                )
                            }
                        )

                        QuickAccessTile(
                            modifier = Modifier.weight(1f),
                            title = "Перемешать всё",
                            subtitle = "Случайный порядок",
                            icon = AppIcons.shuffle(iconPackStyle),
                            gradient = Brush.linearGradient(listOf(Color(0xFF4776E6), Color(0xFF8E54E9))),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                if (allSongs.isNotEmpty()) {
                                    playbackManager.playSongs(allSongs.shuffled(), 0)
                                }
                            }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 4. БЛОК: ЛЮБИМЫЕ ТРЕКИ
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

            items(favoriteSongs.take(6), key = { "fav_${it.id}" }) { song ->
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
        // 5. БЛОК: СОЗДАННЫЕ ПЛЕЙЛИСТЫ
        // =====================================================================
        if (selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.PLAYLISTS) {
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
                            imageVector = AppIcons.playlist,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Созданные плейлисты",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${customPlaylists.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (customPlaylists.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(customPlaylists, key = { it.id }) { playlist ->
                            val playlistSongs = remember(allSongs, playlist.songIds) {
                                playlist.songIds.mapNotNull { id -> allSongs.find { it.id == id } }
                            }
                            PlaylistQuickCard(
                                playlist = playlist,
                                songCount = playlistSongs.size,
                                themeStyle = themeStyle,
                                primaryAccent = primaryAccent,
                                onClick = { onNavigateToPlaylist(playlist.id) },
                                onPlay = {
                                    if (playlistSongs.isNotEmpty()) {
                                        playbackManager.playSongs(playlistSongs, 0)
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (selectedFilter == HomeFilter.PLAYLISTS) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = cardSurfaceColor
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = AppIcons.playlist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Нет созданных плейлистов",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Создавайте плейлисты в медиатеке для удобной группировки треков.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 6. БЛОК: ЧАСТО ПРОСЛУШИВАЕМЫЕ ТРЕКИ (Most Played)
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

            items(recommendedSongs.take(8), key = { "pop_${it.id}" }) { song ->
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
        // 7. БЛОК: НЕДАВНО ПРОСЛУШАНО (Recently Played)
        // =====================================================================
        if ((selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.RECENT) && recentSongs.isNotEmpty()) {
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
                            imageVector = AppIcons.queue(iconPackStyle),
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Недавно прослушано",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentSongs, key = { "rec_${it.id}" }) { song ->
                        RecentSongCard(
                            song = song,
                            themeStyle = themeStyle,
                            iconPackStyle = iconPackStyle,
                            primaryAccent = primaryAccent,
                            isPlaying = currentSong?.id == song.id && isPlaying,
                            onClick = { playbackManager.playSongFromList(recentSongs, song) }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 8. БЛОК: АЛЬБОМЫ
        // =====================================================================
        if ((selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.ALBUMS) && albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.album(iconPackStyle),
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Альбомы",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${albums.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums.take(12), key = { "alb_${it.id}" }) { album ->
                        AlbumQuickCard(
                            album = album,
                            themeStyle = themeStyle,
                            iconPackStyle = iconPackStyle,
                            onClick = {
                                val albumSongs = allSongs.filter { it.albumId == album.id }
                                if (albumSongs.isNotEmpty()) {
                                    playbackManager.playSongs(albumSongs, 0)
                                }
                            }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 9. ПУСТАЯ МЕДИАТЕКА
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
// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ (БЕЗ ЭМОДЗИ, СО СТРОГИМИ SVG ИКОНКАМИ)
// =============================================================================

/**
 * Компактная плашка быстрого доступа 2x3 с векторной SVG иконкой и кнопкой Play.
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
    onClick: () -> Unit
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
                .background(primaryAccent.copy(alpha = 0.9f)),
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
 * Карточка созданного плейлиста для горизонтальной ленты.
 */
@Composable
fun PlaylistQuickCard(
    playlist: Playlist,
    songCount: Int,
    themeStyle: AppThemeStyle,
    primaryAccent: Color,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    val cardBg = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
        else -> GlassDarkSurface
    }

    GlassCard(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = cardBg
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(primaryAccent.copy(alpha = 0.8f), ElectricPurple.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.customCoverUri != null) {
                    AsyncImage(
                        model = playlist.customCoverUri,
                        contentDescription = playlist.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = AppIcons.playlist,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Floating Play Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.play,
                        contentDescription = "Играть плейлист",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$songCount треков",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
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

/**
 * Недавно прослушанный трек для горизонтальной карусели.
 */
@Composable
fun RecentSongCard(
    song: Song,
    themeStyle: AppThemeStyle,
    iconPackStyle: IconPackStyle,
    primaryAccent: Color,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    val cardBg = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
        else -> GlassDarkSurface
    }

    GlassCard(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = cardBg
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
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
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Overlay Floating Play / Pause Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) CoralOrange else primaryAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) AppIcons.pause else AppIcons.play,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) primaryAccent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Карточка альбома для горизонтальной карусели.
 */
@Composable
fun AlbumQuickCard(
    album: Album,
    themeStyle: AppThemeStyle,
    iconPackStyle: IconPackStyle,
    onClick: () -> Unit
) {
    val cardBg = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
        else -> GlassDarkSurface
    }

    GlassCard(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = cardBg
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
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
                if (album.albumArtUriString != null) {
                    AsyncImage(
                        model = album.albumArtUri,
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = AppIcons.album(iconPackStyle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${album.songCount} треков",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}
