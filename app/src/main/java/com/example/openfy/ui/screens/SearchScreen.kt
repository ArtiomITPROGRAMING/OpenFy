package com.example.openfy.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.model.Album
import com.example.openfy.core.audio.model.Artist
import com.example.openfy.core.audio.model.FolderItem
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.AddToPlaylistBottomSheet
import com.example.openfy.core.ui.components.AlbumCard
import com.example.openfy.core.ui.components.ArtistCard
import com.example.openfy.core.ui.components.SongListItem
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.GlassDarkSurface

enum class SearchFilter(val title: String) {
    ALL("Все"),
    TRACKS("Треки"),
    ALBUMS("Альбомы"),
    ARTISTS("Исполнители"),
    FOLDERS("Папки")
}

@Composable
fun SearchScreen(
    playbackManager: PlaybackManager,
    allSongs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<FolderItem>,
    onOpenStreamDialog: (String) -> Unit = {},
    onDeleteSong: (Song) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    val currentSong by playbackManager.currentSong.collectAsState()
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()
    val primaryAccent = MaterialTheme.colorScheme.primary

    val filteredSongs = remember(allSongs, query) {
        if (query.isBlank()) emptyList()
        else allSongs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }

    val filteredAlbums = remember(albums, query) {
        if (query.isBlank()) emptyList()
        else albums.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true)
        }
    }

    val filteredArtists = remember(artists, query) {
        if (query.isBlank()) emptyList()
        else artists.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Search Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Поиск",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                placeholder = { Text("Название трека, артист, альбом...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.search(iconPackStyle),
                        contentDescription = "Search",
                        tint = primaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = AppIcons.clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SearchFilter.values()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccent,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Search Results List
        if (query.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.search(iconPackStyle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Поиск по трекам, альбомам и исполнителям",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (filteredSongs.isEmpty() && filteredAlbums.isEmpty() && filteredArtists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "По запросу «$query» ничего не найдено в медиатеке",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Tracks Section
                if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.TRACKS) {
                    if (filteredSongs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Треки (${filteredSongs.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        items(filteredSongs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = currentSong?.id == song.id,
                                isFavorite = favorites.contains(song.id),
                                iconPackStyle = iconPackStyle,
                                onClick = {
                                    playbackManager.playSongFromList(filteredSongs, song)
                                },
                                onFavoriteToggle = {
                                    playbackManager.playlistRepository.toggleFavorite(song.id)
                                },
                                onAddToPlaylist = {
                                    songForAddToPlaylist = song
                                },
                                onDeleteFromDevice = {
                                    onDeleteSong(song)
                                }
                            )
                        }
                    }
                }

                // Albums Section
                if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ALBUMS) {
                    if (filteredAlbums.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Альбомы (${filteredAlbums.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        items(filteredAlbums, key = { it.id }) { album ->
                            AlbumCard(
                                album = album,
                                onClick = {
                                    val albumSongs = allSongs.filter { it.albumId == album.id }
                                    if (albumSongs.isNotEmpty()) {
                                        playbackManager.playSongs(albumSongs, 0)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Artists Section
                if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.ARTISTS) {
                    if (filteredArtists.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Исполнители (${filteredArtists.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        items(filteredArtists, key = { it.name }) { artist ->
                            ArtistCard(
                                artist = artist,
                                onClick = {
                                    val artistSongs = allSongs.filter { it.artist.equals(artist.name, ignoreCase = true) }
                                    if (artistSongs.isNotEmpty()) {
                                        playbackManager.playSongs(artistSongs, 0)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
