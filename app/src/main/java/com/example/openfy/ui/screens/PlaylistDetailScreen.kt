package com.example.openfy.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.openfy.features.community.sync.SharePayload
import com.example.openfy.features.community.sync.ShareType
import com.example.openfy.features.community.ui.ShareBottomSheet
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.AddToPlaylistBottomSheet
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.components.SongListItem
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonPink

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    allSongs: List<Song>,
    playbackManager: PlaybackManager,
    onBack: () -> Unit,
    onDeleteSong: (Song) -> Unit = {}
) {
    val context = LocalContext.current
    val playlists by playbackManager.playlistRepository.playlists.collectAsState()
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()

    val playlist = remember(playlists, playlistId) {
        playlists.find { it.id == playlistId }
    }

    val playlistSongs = remember(playlist, allSongs) {
        if (playlist == null) emptyList()
        else playlist.songIds.mapNotNull { id -> allSongs.find { it.id == id } }
    }

    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBg = if (themeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface

    val totalDurationMs = remember(playlistSongs) { playlistSongs.sumOf { it.durationMs } }
    val totalMinutes = totalDurationMs / 1000 / 60

    if (playlist == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    DynamicBackground(isPlaying = isPlaying, themeStyle = themeStyle) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Плейлист",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться плейлистом",
                            tint = primaryAccent
                        )
                    }
                    IconButton(onClick = { showDeletePlaylistDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Playlist",
                            tint = NeonPink
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Spotify-Style Hero Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large Playlist Artwork
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(primaryAccent.copy(alpha = 0.8f), Color(0xFFBD00FF).copy(alpha = 0.5f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppIcons.playlist(iconPackStyle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(72.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Плейлист • ${playlistSongs.size} треков • $totalMinutes мин",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Action Row: Play & Shuffle (Spotify Style)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Shuffle Button
                                IconButton(
                                    onClick = {
                                        if (playlistSongs.isNotEmpty()) {
                                            playbackManager.playSongs(playlistSongs.shuffled(), 0)
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = primaryAccent,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            // Big Circular Play FAB
                            FilledIconButton(
                                onClick = {
                                    if (playlistSongs.isNotEmpty()) {
                                        playbackManager.playSongs(playlistSongs, 0)
                                    }
                                },
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else primaryAccent,
                                    contentColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Track List Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Треки",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                if (playlistSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "В этом плейлисте пока нет треков.\nДобавьте их через меню любого трека (3 точки -> В плейлист)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(playlistSongs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = currentSong?.id == song.id,
                            isFavorite = favorites.contains(song.id),
                            iconPackStyle = iconPackStyle,
                            onClick = {
                                playbackManager.playSongFromList(playlistSongs, song)
                            },
                            onFavoriteToggle = {
                                playbackManager.playlistRepository.toggleFavorite(song.id)
                            },
                            onAddToPlaylist = {
                                songForAddToPlaylist = song
                            },
                            onDeleteFromDevice = {
                                onDeleteSong(song)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }

        // Add To Playlist Modal
        songForAddToPlaylist?.let { song ->
            AddToPlaylistBottomSheet(
                song = song,
                playbackManager = playbackManager,
                onDismiss = { songForAddToPlaylist = null }
            )
        }

        // Share Playlist BottomSheet
        if (showShareSheet) {
            val playlistPayload = remember(playlist, playlistSongs) {
                val songTitles = playlistSongs.map { it.title }
                val songIds = playlistSongs.map { it.id }
                val jsonObject = buildJsonObject {
                    put("name", JsonPrimitive(playlist.name))
                    put("songIds", JsonArray(songIds.map { JsonPrimitive(it) }))
                    put("songTitles", JsonArray(songTitles.map { JsonPrimitive(it) }))
                }
                SharePayload(
                    type = ShareType.PLAYLIST,
                    title = playlist.name,
                    description = "Плейлист OpenFy • ${playlistSongs.size} треков",
                    jsonData = jsonObject.toString()
                )
            }
            ShareBottomSheet(
                payload = playlistPayload,
                onDismiss = { showShareSheet = false }
            )
        }

        // Delete Playlist Confirmation Dialog
        if (showDeletePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showDeletePlaylistDialog = false },
                title = { Text("Удалить плейлист") },
                text = { Text("Вы действительно хотите удалить плейлист «${playlist.name}»?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeletePlaylistDialog = false
                            playbackManager.playlistRepository.deletePlaylist(playlist.id)
                            Toast.makeText(context, "Плейлист удален", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    ) {
                        Text("Удалить", color = NeonPink, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletePlaylistDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
