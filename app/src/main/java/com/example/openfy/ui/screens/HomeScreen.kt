package com.example.openfy.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroPhosphorGreen
import com.example.openfy.core.ui.theme.TitaniumSilver

@Composable
fun HomeScreen(
    playbackManager: PlaybackManager,
    allSongs: List<Song>,
    albums: List<Album>,
    onRefreshAudio: () -> Unit,
    onNavigateToPlaylist: (String) -> Unit = {},
    onOpenStreamDialog: (String) -> Unit = {},
    onDeleteSong: (Song) -> Unit = {}
) {
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val recentlyPlayedIds by playbackManager.playlistRepository.recentlyPlayed.collectAsState()
    val playCounts by playbackManager.playlistRepository.playCounts.collectAsState()
    val currentSong by playbackManager.currentSong.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()

    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    val favoriteSongs = remember(allSongs, favorites) {
        allSongs.filter { favorites.contains(it.id) }
    }

    val recentSongs = remember(allSongs, recentlyPlayedIds) {
        recentlyPlayedIds.mapNotNull { id -> allSongs.find { it.id == id } }.take(15)
    }

    val recommendedSongs = remember(allSongs, playCounts) {
        allSongs.sortedByDescending { playCounts[it.id] ?: 0 }.take(10)
    }

    val primaryAccent = MaterialTheme.colorScheme.primary

    val logoBrush = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(listOf(Color(0xFF383944), Color(0xFF16171D)))
        AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(listOf(CyberpunkRubyRed, Color(0xFFFFB700)))
        AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(listOf(RetroPhosphorGreen, Color(0xFF008833)))
        else -> Brush.linearGradient(listOf(NeonCyan, ElectricPurple))
    }

    val heroCardBrush = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(listOf(Color(0xFF32333D), Color(0xFF181920)))
        AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(listOf(CyberpunkRubyRed, Color(0xFF6B0E1E)))
        AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(listOf(Color(0xFF0E2E1B), Color(0xFF05140B)))
        else -> Brush.linearGradient(listOf(NeonPink, ElectricPurple))
    }

    val heroGlowColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Color.Transparent
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkRubyRed.copy(alpha = 0.5f)
        AppThemeStyle.RETRO_PIXEL -> RetroPhosphorGreen.copy(alpha = 0.5f)
        else -> NeonPink.copy(alpha = 0.5f)
    }

    val heroPlayBtnColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Color.White
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkRubyRed
        AppThemeStyle.RETRO_PIXEL -> RetroPhosphorGreen
        else -> NeonPink
    }

    val heroPlayTextColor = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Color.Black
        AppThemeStyle.RETRO_PIXEL -> Color.Black
        else -> Color.White
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(logoBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.music(iconPackStyle),
                            contentDescription = "OpenFy Logo",
                            tint = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "OpenFy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${allSongs.size} треков в офлайн-медиатеке",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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

        // 1. Hero Card: Избранные / Любимые треки
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface,
                hasGlowBorder = themeStyle.hasNeonGlow,
                glowColor = heroGlowColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(heroCardBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.favoriteFilled(iconPackStyle),
                            contentDescription = "Favorites",
                            tint = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Любимые треки",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${favoriteSongs.size} треков в избранном",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (favoriteSongs.isNotEmpty()) {
                                        playbackManager.playSongs(favoriteSongs, 0)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = heroPlayBtnColor,
                                    contentColor = heroPlayTextColor
                                )
                            ) {
                                Icon(AppIcons.play(iconPackStyle), contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Играть", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (favoriteSongs.isNotEmpty()) {
                                        playbackManager.playSongs(favoriteSongs.shuffled(), 0)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(AppIcons.shuffle(iconPackStyle), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 2. Недавно прослушано (Recently Played)
        if (recentSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Недавно прослушано",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentSongs, key = { it.id }) { song ->
                        RecentSongCard(
                            song = song,
                            themeStyle = themeStyle,
                            iconPackStyle = iconPackStyle,
                            onClick = { playbackManager.playSongFromList(recentSongs, song) }
                        )
                    }
                }
            }
        }

        // 3. Рекомендации / Часто слушаете
        if (recommendedSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Часто слушаете",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            items(recommendedSongs.take(5), key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = currentSong?.id == song.id,
                    isFavorite = favorites.contains(song.id),
                    iconPackStyle = iconPackStyle,
                    onClick = { playbackManager.playSongFromList(recommendedSongs, song) },
                    onFavoriteToggle = { playbackManager.playlistRepository.toggleFavorite(song.id) },
                    onAddToPlaylist = { songForAddToPlaylist = song },
                    onDeleteFromDevice = { onDeleteSong(song) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // 4. Альбомы медиатеки (Горизонтальная лента)
        if (albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Альбомы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums.take(10), key = { it.id }) { album ->
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
    }

    // Add To Playlist Modal Sheet
    songForAddToPlaylist?.let { song ->
        AddToPlaylistBottomSheet(
            song = song,
            playbackManager = playbackManager,
            onDismiss = { songForAddToPlaylist = null }
        )
    }
}

@Composable
fun RecentSongCard(
    song: Song,
    themeStyle: AppThemeStyle,
    iconPackStyle: IconPackStyle,
    onClick: () -> Unit
) {
    val placeholderBrush = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(listOf(Color(0xFF282932), Color(0xFF121318)))
        AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(listOf(CyberpunkRubyRed.copy(alpha = 0.4f), Color(0xFF2E0914)))
        AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(listOf(Color(0xFF0F331D), Color(0xFF06150C)))
        else -> Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.3f), ElectricPurple.copy(alpha = 0.4f)))
    }

    GlassCard(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(placeholderBrush),
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
                    Icon(AppIcons.music(iconPackStyle), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
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

@Composable
fun AlbumQuickCard(
    album: Album,
    themeStyle: AppThemeStyle,
    iconPackStyle: IconPackStyle,
    onClick: () -> Unit
) {
    val placeholderBrush = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(listOf(Color(0xFF282932), Color(0xFF121318)))
        AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(listOf(Color(0xFF4A0A17), Color(0xFF1F040A)))
        AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(listOf(Color(0xFF0F331D), Color(0xFF06150C)))
        else -> Brush.linearGradient(listOf(ElectricPurple.copy(alpha = 0.4f), NeonPink.copy(alpha = 0.3f)))
    }

    GlassCard(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(placeholderBrush),
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
                    Icon(AppIcons.album(iconPackStyle), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
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
