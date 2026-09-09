package com.example.openfy.features.streamer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.features.streamer.data.OpenSourceMusicCatalog
import com.example.openfy.features.streamer.data.StreamRepository
import com.example.openfy.features.streamer.resolver.SafeStreamResolver
import com.example.openfy.features.streamer.resolver.StreamMetadata
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StreamScreen(
    playbackManager: PlaybackManager,
    initialGenre: String? = null,
    initialQuery: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val primaryAccent = MaterialTheme.colorScheme.primary
    val neonCyan = Color(0xFF00E5FF)
    val neonPurple = Color(0xFFB388FF)

    var selectedCategory by remember { mutableStateOf(initialGenre ?: "Все") }
    var searchQuery by remember { mutableStateOf(initialQuery ?: "") }
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    var favoriteStreams by remember { mutableStateOf(StreamRepository.getFavoriteStreams(context)) }
    val currentPlayingSong by playbackManager.currentSong.collectAsState()

    // Custom Link Input Expander
    var showCustomUrlCard by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var resolvedCustomMetadata by remember { mutableStateOf<StreamMetadata?>(null) }
    var isResolvingUrl by remember { mutableStateOf(false) }
    var customUrlError by remember { mutableStateOf<String?>(null) }
    var resolveJob by remember { mutableStateOf<Job?>(null) }

    fun refreshFavorites() {
        favoriteStreams = StreamRepository.getFavoriteStreams(context)
    }

    fun playTrack(song: Song) {
        StreamRepository.recordRecentStream(context, song)
        playbackManager.playStreamTrack(song)
    }

    fun performSearch(query: String) {
        searchJob?.cancel()
        val clean = query.trim()
        if (clean.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            return
        }
        searchJob = coroutineScope.launch {
            isSearching = true
            delay(250L)
            searchResults = StreamRepository.searchAll(clean)
            isSearching = false
        }
    }

    fun resolveCustomUrl(rawUrl: String) {
        val clean = rawUrl.trim()
        if (clean.isBlank()) {
            resolvedCustomMetadata = null
            customUrlError = null
            return
        }
        resolveJob?.cancel()
        resolveJob = coroutineScope.launch {
            isResolvingUrl = true
            customUrlError = null
            delay(200L)
            val result = SafeStreamResolver.resolveStream(clean)
            isResolvingUrl = false
            result.onSuccess { meta ->
                resolvedCustomMetadata = meta
            }.onFailure { err ->
                customUrlError = err.localizedMessage ?: "Не удалось распознать ссылку"
            }
        }
    }

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            searchQuery = initialQuery
            performSearch(initialQuery)
        }
    }

    val categories = remember {
        listOf("Все", "Lo-Fi", "Synthwave", "Chillout", "Ambient", "Electronic", "Classical", "Jazz", "Rock", "Избранное")
    }

    val currentCategoryTracks = remember(selectedCategory) {
        StreamRepository.getCatalogTracks(if (selectedCategory == "Все") null else selectedCategory)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Screen Header & Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Открытая музыка",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = neonCyan.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, neonCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "FOSS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = neonCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Свободные треки, альбомы и открытая медиатека",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showCustomUrlCard = !showCustomUrlCard },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (showCustomUrlCard) primaryAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Своя ссылка",
                        tint = if (showCustomUrlCard) neonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Interactive Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    performSearch(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Поиск по открытым трекам, жанрам и авторам...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = neonCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchResults = emptyList()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Очистить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) })
            )
        }

        // 3. Custom URL Collapsible Card (Fallback for direct audio links)
        if (showCustomUrlCard) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = GlassDarkSurface.copy(alpha = 0.9f),
                    borderColor = neonCyan.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Воспроизведение прямого аудиофайла (URL)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customUrlInput,
                            onValueChange = {
                                customUrlInput = it
                                resolveCustomUrl(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://example.com/song.mp3", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text?.trim() ?: ""
                                    if (clip.isNotBlank()) {
                                        customUrlInput = clip
                                        resolveCustomUrl(clip)
                                    }
                                }) {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Вставить", tint = primaryAccent)
                                }
                            }
                        )

                        if (isResolvingUrl) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = neonCyan, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Проверка трека...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (resolvedCustomMetadata != null && !isResolvingUrl) {
                            val meta = resolvedCustomMetadata!!
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { playTrack(meta.toSong()) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Играть «${meta.title}»")
                            }
                        }

                        if (customUrlError != null && !isResolvingUrl) {
                            Text(
                                text = customUrlError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Category / Genre Filter Chips
        if (searchQuery.isBlank()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = cat
                                if (cat == "Избранное") refreshFavorites()
                            },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (cat == "Избранное") {
                                { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFF4081)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryAccent.copy(alpha = 0.28f),
                                selectedLabelColor = neonCyan
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) neonCyan else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // 5. SEARCH RESULTS (Active Search)
        if (searchQuery.isNotBlank()) {
            if (isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = neonCyan, modifier = Modifier.size(32.dp))
                    }
                }
            } else if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "По запросу «$searchQuery» ничего не найдено",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "Результаты поиска (${searchResults.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(searchResults, key = { it.streamUrl ?: it.id }) { song ->
                    val isFav = StreamRepository.isFavoriteStream(context, song.streamUrl)
                    val isCurrent = currentPlayingSong?.streamUrl == song.streamUrl || currentPlayingSong?.contentUriString == song.streamUrl
                    StreamSongCard(
                        song = song,
                        isFavorite = isFav,
                        isPlayingCurrent = isCurrent,
                        badgeText = "CC",
                        onToggleFavorite = {
                            StreamRepository.toggleFavoriteStream(context, song)
                            refreshFavorites()
                        },
                        onPlay = { playTrack(song) }
                    )
                }
            }
        } else if (selectedCategory == "Избранное") {
            // FAVORITES VIEW
            if (favoriteStreams.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Нет сохраненных онлайн-треков",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Нажмите сердечко рядом с любым открытым треком",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(favoriteStreams, key = { it.streamUrl ?: it.id }) { song ->
                    val isCurrent = currentPlayingSong?.streamUrl == song.streamUrl || currentPlayingSong?.contentUriString == song.streamUrl
                    StreamSongCard(
                        song = song,
                        isFavorite = true,
                        isPlayingCurrent = isCurrent,
                        onToggleFavorite = {
                            StreamRepository.toggleFavoriteStream(context, song)
                            refreshFavorites()
                        },
                        onPlay = { playTrack(song) }
                    )
                }
            }
        } else {
            // STANDARD / GENRE VIEW
            items(currentCategoryTracks, key = { it.streamUrl ?: it.id }) { song ->
                val isFav = StreamRepository.isFavoriteStream(context, song.streamUrl)
                val isCurrent = currentPlayingSong?.streamUrl == song.streamUrl || currentPlayingSong?.contentUriString == song.streamUrl
                StreamSongCard(
                    song = song,
                    isFavorite = isFav,
                    isPlayingCurrent = isCurrent,
                    badgeText = song.formattedDuration,
                    onToggleFavorite = {
                        StreamRepository.toggleFavoriteStream(context, song)
                        refreshFavorites()
                    },
                    onPlay = { playTrack(song) }
                )
            }
        }

        // Bottom Safe Padding for MiniPlayer
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StreamSongCard(
    song: Song,
    isFavorite: Boolean = false,
    isPlayingCurrent: Boolean = false,
    badgeText: String? = null,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit
) {
    val primaryAccent = MaterialTheme.colorScheme.primary
    val neonCyan = Color(0xFF00E5FF)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isPlayingCurrent) primaryAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlayingCurrent) neonCyan.copy(alpha = 0.6f) else if (isFavorite) neonCyan.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Art
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1B4B)),
                contentAlignment = Alignment.Center
            ) {
                if (!song.albumArtUriString.isNullOrBlank()) {
                    AsyncImage(
                        model = song.albumArtUriString,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = neonCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlayingCurrent) neonCyan else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val badge = badgeText ?: song.formattedDuration
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Избранное",
                    tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Quick Play Button
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isPlayingCurrent) neonCyan else primaryAccent)
            ) {
                Icon(
                    imageVector = if (isPlayingCurrent) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = "Воспроизвести",
                    tint = if (isPlayingCurrent) Color.Black else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
