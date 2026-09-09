package com.example.openfy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.IconPackStyle
import com.example.openfy.core.audio.model.Album
import com.example.openfy.core.audio.model.Artist
import com.example.openfy.core.audio.model.FolderItem
import com.example.openfy.core.audio.model.Playlist
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.model.SortOption
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.AddToPlaylistBottomSheet
import com.example.openfy.core.ui.components.AlbumCard
import com.example.openfy.core.ui.components.ArtistCard
import com.example.openfy.core.ui.components.EmptyLibraryView
import com.example.openfy.core.ui.components.FolderCard
import com.example.openfy.core.ui.components.PlaylistCard
import com.example.openfy.core.ui.components.SongListItem
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    playbackManager: PlaybackManager,
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<FolderItem>,
    isLoading: Boolean,
    onRefreshAudio: () -> Unit,
    onNavigateToPlaylist: (String) -> Unit = {},
    onDeleteSong: (Song) -> Unit = {},
    initialTabIndex: Int = 0,
    scrollToCurrentSongTrigger: Int = 0
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    var currentSort by remember { mutableStateOf(SortOption.TITLE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    val currentSong by playbackManager.currentSong.collectAsState()
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val playlists by playbackManager.playlistRepository.playlists.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()

    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBg = if (themeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface
    val listState = rememberLazyListState()

    val tabs = listOf("Все треки", "Альбомы", "Исполнители", "Папки", "Плейлисты")

    // Filter & Sort Songs
    val filteredSongs = remember(songs, searchQuery, currentSort) {
        val list = if (searchQuery.isBlank()) songs
        else songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }

        when (currentSort) {
            SortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
            SortOption.ARTIST_ASC -> list.sortedBy { it.artist.lowercase() }
            SortOption.ARTIST_DESC -> list.sortedByDescending { it.artist.lowercase() }
            SortOption.DATE_ADDED_DESC -> list.sortedByDescending { it.dateAdded }
            SortOption.DATE_ADDED_ASC -> list.sortedBy { it.dateAdded }
            SortOption.DURATION_DESC -> list.sortedByDescending { it.durationMs }
            SortOption.DURATION_ASC -> list.sortedBy { it.durationMs }
            SortOption.YEAR_DESC -> list.sortedByDescending { it.dateAdded }
        }
    }

    // Scroll to currently playing song on long-press trigger
    LaunchedEffect(scrollToCurrentSongTrigger) {
        if (scrollToCurrentSongTrigger > 0 && currentSong != null) {
            selectedTabIndex = 0
            val idx = filteredSongs.indexOfFirst { it.id == currentSong!!.id }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header Row with Title and Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Медиатека",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${songs.size} треков на устройстве",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sort Menu Button
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Сортировка",
                            tint = primaryAccent
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("По названию (А-Я)") },
                            onClick = { currentSort = SortOption.TITLE_ASC; showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("По названию (Я-А)") },
                            onClick = { currentSort = SortOption.TITLE_DESC; showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("По исполнителю (А-Я)") },
                            onClick = { currentSort = SortOption.ARTIST_ASC; showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("По исполнителю (Я-А)") },
                            onClick = { currentSort = SortOption.ARTIST_DESC; showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Сначала новые") },
                            onClick = { currentSort = SortOption.DATE_ADDED_DESC; showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Сначала старые") },
                            onClick = { currentSort = SortOption.DATE_ADDED_ASC; showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("По длительности (длинные)") },
                            onClick = { currentSort = SortOption.DURATION_DESC; showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("По длительности (короткие)") },
                            onClick = { currentSort = SortOption.DURATION_ASC; showSortMenu = false }
                        )
                    }
                }
            }
        }

        // Search Bar in Library
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            placeholder = { Text("Поиск по медиатеке...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg,
                focusedBorderColor = primaryAccent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Tabs
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 20.dp,
            containerColor = Color.Transparent,
            contentColor = primaryAccent,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                    color = primaryAccent,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) primaryAccent else Color.White.copy(alpha = 0.7f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> { // All Tracks
                if (filteredSongs.isEmpty()) {
                    EmptyLibraryView(isLoading = isLoading)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        items(filteredSongs, key = { it.id }) { song ->
                            val isCurrentSongPlaying = currentSong?.id == song.id
                            SongListItem(
                                song = song,
                                isPlaying = isCurrentSongPlaying,
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
            }
            1 -> { // Albums Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            onClick = {
                                val albumSongs = songs.filter { it.albumId == album.id }
                                if (albumSongs.isNotEmpty()) {
                                    playbackManager.playSongs(albumSongs, 0)
                                }
                            }
                        )
                    }
                }
            }
            2 -> { // Artists
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(artists, key = { it.name }) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = {
                                val artistSongs = songs.filter { it.artist.equals(artist.name, ignoreCase = true) }
                                if (artistSongs.isNotEmpty()) {
                                    playbackManager.playSongs(artistSongs, 0)
                                }
                            }
                        )
                    }
                }
            }
            3 -> { // Folders
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(folders, key = { it.path }) { folder ->
                        FolderCard(
                            folder = folder,
                            onClick = {
                                if (folder.songs.isNotEmpty()) {
                                    playbackManager.playSongs(folder.songs, 0)
                                }
                            }
                        )
                    }
                }
            }
            4 -> { // Playlists (Spotify Style)
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(
                        onClick = { showCreatePlaylistDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else primaryAccent,
                            contentColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать новый плейлист", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (playlists.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Плейлистов пока нет.\nНажмите «Создать новый плейлист» выше!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(playlists, key = { it.id }) { playlist ->
                                val playlistSongs = songs.filter { playlist.songIds.contains(it.id) }
                                PlaylistCard(
                                    playlist = playlist,
                                    songCount = playlistSongs.size,
                                    onClick = {
                                        onNavigateToPlaylist(playlist.id)
                                    },
                                    onDelete = {
                                        playbackManager.playlistRepository.deletePlaylist(playlist.id)
                                    }
                                )
                            }
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

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Новый плейлист") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Название плейлиста") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newPlaylistName.trim()
                        if (name.isNotEmpty()) {
                            val newPl = playbackManager.playlistRepository.createPlaylist(name)
                            showCreatePlaylistDialog = false
                            newPlaylistName = ""
                            onNavigateToPlaylist(newPl.id)
                        }
                    }
                ) {
                    Text("Создать", color = primaryAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
