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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.AddToPlaylistBottomSheet
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.components.SongListItem
import com.example.openfy.core.ui.theme.AmberGlow
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AmoledSurfaceElevated
import com.example.openfy.core.ui.theme.AmoledSurfaceVariant
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CoralOrange
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.DarkSurfaceElevated
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import com.example.openfy.core.ui.theme.RetroPhosphorGreen
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.sin

enum class HomeFilter(val title: String) {
    ALL("Все"),
    FLOW("Flow"),
    SOUNDCLOUD("В фокусе • Wave"),
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

    val recentSongs = remember(allSongs, recentlyPlayedIds) {
        recentlyPlayedIds.mapNotNull { id -> allSongs.find { it.id == id } }.take(15)
    }

    val recommendedSongs = remember(allSongs, playCounts) {
        allSongs.sortedByDescending { playCounts[it.id] ?: 0 }.take(12)
    }

    val streamSongs = remember(allSongs, recommendedSongs) {
        (recommendedSongs.take(4) + allSongs.take(6)).distinctBy { it.id }
    }

    // Spotify-style Dynamic Time-of-Day Greeting
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

    // Theme-tailored Card Surface & Glows
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
        // 1. HEADER (Spotify Smart Greeting + Quick Action Buttons)
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
        // 2. FILTER CHIPS (Deezer / Modern Minimalist Filter Bar)
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
        // 3. SPOTIFY-STYLE 6-TILE QUICK ACCESS GRID (2x3 Compact Tiles)
        // =====================================================================
        if (selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.FLOW) {
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
                        SpotifyQuickTile(
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

                        SpotifyQuickTile(
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

                    // Row 2: Топ треков & Deezer Flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpotifyQuickTile(
                            modifier = Modifier.weight(1f),
                            title = "Топ треков",
                            subtitle = "Heavy Rotation",
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

                        SpotifyQuickTile(
                            modifier = Modifier.weight(1f),
                            title = "Deezer Flow",
                            subtitle = "Умный микс",
                            icon = AppIcons.stream,
                            gradient = Brush.linearGradient(listOf(ElectricPurple, NeonPink)),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                val flowList = (favoriteSongs + recommendedSongs.shuffled() + allSongs.shuffled()).distinctBy { it.id }
                                if (flowList.isNotEmpty()) {
                                    playbackManager.playSongs(flowList, 0)
                                }
                            }
                        )
                    }

                    // Row 3: Zen & Природа & Перемешать всё
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpotifyQuickTile(
                            modifier = Modifier.weight(1f),
                            title = "Zen & Природа",
                            subtitle = "Сон & Релакс",
                            icon = AppIcons.spa,
                            gradient = Brush.linearGradient(listOf(Color(0xFF00B09B), Color(0xFF96C93D))),
                            tileBackground = tileSurfaceColor,
                            primaryAccent = primaryAccent,
                            onClick = {
                                // Start procedural night rain ambient soundscape
                                playbackManager.zenNatureAudioEngine.startNatureSound(
                                    com.example.openfy.core.audio.service.NatureSoundType.NIGHT_RAIN,
                                    0.4f
                                )
                            }
                        )

                        SpotifyQuickTile(
                            modifier = Modifier.weight(1f),
                            title = "Перемешать всё",
                            subtitle = "Случайный микс",
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
        // 4. DEEZER "FLOW" HERO CARD (Interactive Smart Stream & Mood Pills)
        // =====================================================================
        if (selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.FLOW) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                DeezerFlowHeroCard(
                    playbackManager = playbackManager,
                    allSongs = allSongs,
                    favoriteSongs = favoriteSongs,
                    recommendedSongs = recommendedSongs,
                    themeStyle = themeStyle,
                    cardBackground = cardSurfaceColor,
                    primaryAccent = primaryAccent
                )
            }
        }

        // =====================================================================
        // 5. "СЛУШАТЬ СНОВА" (Jump Back In - Spotify Horizontal Carousel)
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
                    Column {
                        Text(
                            text = "Слушать снова",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Недавно прослушанные треки",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentSongs, key = { it.id }) { song ->
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
        // 6. SOUNDCLOUD "THE DROP / В ФОКУСЕ" WAVEFORM STREAM FEED
        // =====================================================================
        if (selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.SOUNDCLOUD) {
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
                        Text(
                            text = "В фокусе",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(CoralOrange, AmberGlow)))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "SOUNDCLOUD WAVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            items(streamSongs, key = { it.id }) { song ->
                SoundCloudWaveTrackRow(
                    song = song,
                    isPlaying = currentSong?.id == song.id && isPlaying,
                    isFavorite = favorites.contains(song.id),
                    playCount = playCounts[song.id] ?: 0,
                    primaryAccent = primaryAccent,
                    themeStyle = themeStyle,
                    iconPackStyle = iconPackStyle,
                    onClick = { playbackManager.playSongFromList(streamSongs, song) },
                    onFavoriteToggle = { playbackManager.playlistRepository.toggleFavorite(song.id) },
                    onAddToPlaylist = { songForAddToPlaylist = song },
                    onDeleteFromDevice = { onDeleteSong(song) }
                )
            }
        }

        // =====================================================================
        // 7. "АЛЬБОМЫ МЕДИАТЕКИ" (Deezer & Spotify Horizontal Carousel)
        // =====================================================================
        if ((selectedFilter == HomeFilter.ALL || selectedFilter == HomeFilter.ALBUMS) && albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Альбомы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums.take(12), key = { it.id }) { album ->
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
        // 8. EMPTY STATE (When library is empty)
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
// SUB-COMPONENTS
// =============================================================================

/**
 * Spotify-Style 2x3 Compact Quick Tile with artwork, title, and integrated 1-tap play action.
 */
@Composable
fun SpotifyQuickTile(
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
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Deezer "Flow" Hero Card: Dynamic intelligent mix generator with Mood Pills.
 */
@Composable
fun DeezerFlowHeroCard(
    playbackManager: PlaybackManager,
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    recommendedSongs: List<Song>,
    themeStyle: AppThemeStyle,
    cardBackground: Color,
    primaryAccent: Color
) {
    val flowGradient = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(
            listOf(Color(0xFF2B2D38), Color(0xFF14151B))
        )
        AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(
            listOf(CyberpunkRubyRed.copy(alpha = 0.6f), Color(0xFF380712))
        )
        AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(
            listOf(Color(0xFF0E301A), Color(0xFF041209))
        )
        else -> Brush.linearGradient(
            listOf(ElectricPurple.copy(alpha = 0.45f), NeonCyan.copy(alpha = 0.35f))
        )
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = cardBackground,
        hasGlowBorder = themeStyle.hasNeonGlow,
        glowColor = primaryAccent.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(flowGradient)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonCyan, ElectricPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.stream,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DEEZER FLOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Твой персональный поток",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Big Flow Play Button
                Button(
                    onClick = {
                        val flowMix = (favoriteSongs.shuffled() + recommendedSongs.shuffled() + allSongs.shuffled()).distinctBy { it.id }
                        if (flowMix.isNotEmpty()) {
                            playbackManager.playSongs(flowMix, 0)
                        }
                    },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccent)
                ) {
                    Icon(
                        imageVector = AppIcons.play,
                        contentDescription = "Запустить Flow",
                        tint = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Умный алгоритм комбинирует любимые треки с редкими жемчужинами вашей медиатеки.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Deezer Mood Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowMoodPill(
                    text = "⚡ Энергия",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val mix = recommendedSongs.ifEmpty { allSongs }
                        if (mix.isNotEmpty()) playbackManager.playSongs(mix, 0)
                    }
                )
                FlowMoodPill(
                    text = "☕ Чилл & Zen",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        playbackManager.zenNatureAudioEngine.startNatureSound(
                            com.example.openfy.core.audio.service.NatureSoundType.FOREST_WIND,
                            0.35f
                        )
                    }
                )
                FlowMoodPill(
                    text = "❤️ Любимое",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (favoriteSongs.isNotEmpty()) playbackManager.playSongs(favoriteSongs.shuffled(), 0)
                    }
                )
                FlowMoodPill(
                    text = "🎲 Микс",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (allSongs.isNotEmpty()) playbackManager.playSongs(allSongs.shuffled(), 0)
                    }
                )
            }
        }
    }
}

@Composable
fun FlowMoodPill(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.8.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * SoundCloud-Style Track Row with animated Waveform Visualizer bars and 1-tap quick actions.
 */
@Composable
fun SoundCloudWaveTrackRow(
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

    // Animated pulse for waveform when song is actively playing
    val infiniteTransition = rememberInfiniteTransition(label = "sc_wave")
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
        // Album Artwork with Play Overlay
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

        // Center Content: Title, Artist, and SoundCloud Mini Waveform
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
                    Text(
                        text = "🔥 $playCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberGlow,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
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

            // SoundCloud Mini Waveform Visualizer Canvas
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
                        primaryAccent.copy(alpha = 0.45f)
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

        // Right Actions: Favorite Heart & Duration
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
 * Recent Song Card for Spotify/Deezer style horizontal carousel.
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
 * Album Quick Card for horizontal carousel.
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
