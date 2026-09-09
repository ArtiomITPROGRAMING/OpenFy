package com.example.openfy.ui.screens

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.AudioScanner
import com.example.openfy.core.audio.model.Album
import com.example.openfy.core.audio.model.Artist
import com.example.openfy.core.audio.model.FolderItem
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.MiniPlayer
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.IconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.imePadding
import com.example.openfy.ui.components.ExpandedSidePlayerPane

@Composable
fun MainScreenWithBottomNav(
    playbackManager: PlaybackManager,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onOpenNowPlaying: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToDuplicateCleaner: () -> Unit = {},
    onNavigateToThemes: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToQrScanner: () -> Unit = {},
    onNavigateToPlaylist: (String) -> Unit = {},
    onAudioScanned: (List<Song>) -> Unit = {},
    onNavigateToCarMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val audioScanner = remember { AudioScanner(context) }

    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var artists by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var folders by remember { mutableStateOf<List<FolderItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var songPendingDeletion by remember { mutableStateOf<Song?>(null) }
    var scrollToSongTrigger by remember { mutableIntStateOf(0) }

    // Offline-First Default: Media library (tab 2) is active on cold start
    var selectedTab by rememberSaveable { mutableIntStateOf(2) }

    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()
    val ignoreShortTracks by playbackManager.settingsRepository.ignoreShortTracks.collectAsState()

    val scope = rememberCoroutineScope()

    fun loadAudio() {
        isLoading = true
        scope.launch {
            val scanned = audioScanner.scanLocalAudio(ignoreShortTracks)
            songs = scanned
            albums = audioScanner.groupSongsByAlbum(scanned)
            artists = audioScanner.groupSongsByArtist(scanned)
            folders = audioScanner.groupSongsByFolder(scanned)
            isLoading = false
            onAudioScanned(scanned)
        }
    }

    LaunchedEffect(ignoreShortTracks) {
        loadAudio()
    }

    var lastDeletedSong by remember { mutableStateOf<Song?>(null) }

    // Android 11+ System Delete Request Intent Launcher
    val deleteRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lastDeletedSong?.let { playbackManager.onSongDeleted(it.id) }
            Toast.makeText(context, "Трек успешно удален", Toast.LENGTH_SHORT).show()
            loadAudio()
        }
    }

    fun executeSingleSongDelete(song: Song) {
        lastDeletedSong = song
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, listOf(song.contentUri))
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                deleteRequestLauncher.launch(intentSenderRequest)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var deleted = false
        try {
            val rows = context.contentResolver.delete(song.contentUri, null, null)
            if (rows > 0) deleted = true
            else if (song.path.isNotBlank()) {
                val f = File(song.path)
                if (f.exists() && f.delete()) deleted = true
            }
        } catch (e: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                val intentSenderRequest = IntentSenderRequest.Builder(e.userAction.actionIntent.intentSender).build()
                deleteRequestLauncher.launch(intentSenderRequest)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (deleted) {
            playbackManager.onSongDeleted(song.id)
            Toast.makeText(context, "Трек успешно удален", Toast.LENGTH_SHORT).show()
            loadAudio()
        } else {
            Toast.makeText(context, "Не удалось удалить файл", Toast.LENGTH_SHORT).show()
        }
    }

    val primaryAccent = MaterialTheme.colorScheme.primary

    val pillGlassBg = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface.copy(alpha = 0.94f)
        AppThemeStyle.MIDNIGHT_NEON -> GlassDarkSurface.copy(alpha = 0.82f)
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg.copy(alpha = 0.90f)
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg.copy(alpha = 0.94f)
        AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    }

    val pillBorderBrush = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
        )
        AppThemeStyle.MIDNIGHT_NEON -> Brush.linearGradient(
            listOf(primaryAccent.copy(alpha = 0.65f), Color(0xFFBD00FF).copy(alpha = 0.35f))
        )
        AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(
            listOf(primaryAccent.copy(alpha = 0.75f), Color(0xFFFFD700).copy(alpha = 0.4f))
        )
        AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(
            listOf(primaryAccent.copy(alpha = 0.8f), primaryAccent.copy(alpha = 0.3f))
        )
        AppThemeStyle.MATERIAL_YOU -> Brush.linearGradient(
            listOf(primaryAccent.copy(alpha = 0.4f), Color.Transparent)
        )
    }

    val isTabletOrExpanded = windowWidthSizeClass != WindowWidthSizeClass.Compact

    DynamicBackground(isPlaying = isPlaying, themeStyle = themeStyle) {
        if (isTabletOrExpanded) {
            // =========================================================================
            // TABLET / FOLDABLE / EXPANDED: Master-Detail (NavigationRail + Master + Player)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .displayCutoutPadding()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // 1. Sleek Themed NavigationRail
                NavigationRail(
                    containerColor = pillGlassBg,
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(primaryAccent, ElectricPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppIcons.music(iconPackStyle),
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    val tabs = listOf(
                        Triple(0, "Главная", AppIcons.home(iconPackStyle)),
                        Triple(1, "Поиск", AppIcons.search(iconPackStyle)),
                        Triple(2, "Медиатека", AppIcons.library(iconPackStyle)),
                        Triple(3, "Настройки", AppIcons.settings(iconPackStyle))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    tabs.forEach { (index, title, icon) ->
                        val isSelected = selectedTab == index
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = primaryAccent,
                                selectedTextColor = primaryAccent,
                                indicatorColor = primaryAccent.copy(alpha = 0.2f),
                                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                unselectedTextColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onNavigateToEqualizer) {
                        Icon(
                            imageVector = AppIcons.equalizer(iconPackStyle),
                            contentDescription = "Эквалайзер",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onNavigateToThemes) {
                        Icon(
                            imageVector = AppIcons.palette(iconPackStyle),
                            contentDescription = "Темы оформления",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 2. Master Screen Pane (Active Library / Home / Search / Cloud / Settings)
                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            playbackManager = playbackManager,
                            allSongs = songs,
                            albums = albums,
                            onRefreshAudio = { loadAudio() },
                            onNavigateToPlaylist = onNavigateToPlaylist,
                            onDeleteSong = { songPendingDeletion = it }
                        )
                        1 -> SearchScreen(
                            playbackManager = playbackManager,
                            allSongs = songs,
                            albums = albums,
                            artists = artists,
                            folders = folders,
                            onDeleteSong = { songPendingDeletion = it }
                        )
                        2 -> LibraryScreen(
                            playbackManager = playbackManager,
                            songs = songs,
                            albums = albums,
                            artists = artists,
                            folders = folders,
                            isLoading = isLoading,
                            onRefreshAudio = { loadAudio() },
                            onNavigateToPlaylist = onNavigateToPlaylist,
                            onDeleteSong = { songPendingDeletion = it },
                            scrollToCurrentSongTrigger = scrollToSongTrigger
                        )
                        3 -> SettingsScreen(
                            playbackManager = playbackManager,
                            onNavigateToEqualizer = onNavigateToEqualizer,
                            onNavigateToDuplicateCleaner = onNavigateToDuplicateCleaner,
                            onNavigateToThemes = onNavigateToThemes,
                            onNavigateToProfile = onNavigateToProfile,
                            onNavigateToQrScanner = onNavigateToQrScanner,
                            onNavigateToCarMode = onNavigateToCarMode
                        )
                    }
                }

                // 3. Detail Pane: Expanded Side Player
                ExpandedSidePlayerPane(
                    playbackManager = playbackManager,
                    onNavigateToEqualizer = onNavigateToEqualizer,
                    onNavigateToLyrics = onOpenNowPlaying,
                    onNavigateToCarMode = onNavigateToCarMode,
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                )
            }
        } else {
            // =========================================================================
            // COMPACT (SMARTPHONES): Single Column with Bottom Floating Bar & MiniPlayer
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .displayCutoutPadding()
                    .imePadding()
            ) {
                // Main Content Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (currentSong != null) 150.dp else 84.dp)
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            playbackManager = playbackManager,
                            allSongs = songs,
                            albums = albums,
                            onRefreshAudio = { loadAudio() },
                            onNavigateToPlaylist = onNavigateToPlaylist,
                            onDeleteSong = { songPendingDeletion = it }
                        )
                        1 -> SearchScreen(
                            playbackManager = playbackManager,
                            allSongs = songs,
                            albums = albums,
                            artists = artists,
                            folders = folders,
                            onDeleteSong = { songPendingDeletion = it }
                        )
                        2 -> LibraryScreen(
                            playbackManager = playbackManager,
                            songs = songs,
                            albums = albums,
                            artists = artists,
                            folders = folders,
                            isLoading = isLoading,
                            onRefreshAudio = { loadAudio() },
                            onNavigateToPlaylist = onNavigateToPlaylist,
                            onDeleteSong = { songPendingDeletion = it },
                            scrollToCurrentSongTrigger = scrollToSongTrigger
                        )
                        3 -> SettingsScreen(
                            playbackManager = playbackManager,
                            onNavigateToEqualizer = onNavigateToEqualizer,
                            onNavigateToDuplicateCleaner = onNavigateToDuplicateCleaner,
                            onNavigateToThemes = onNavigateToThemes,
                            onNavigateToProfile = onNavigateToProfile,
                            onNavigateToQrScanner = onNavigateToQrScanner,
                            onNavigateToCarMode = onNavigateToCarMode
                        )
                    }
                }

                // Floating Liquid Glass Navigation Area & MiniPlayer
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Docked MiniPlayer floating above pill nav
                    MiniPlayer(
                        modifier = Modifier.fillMaxWidth(),
                        song = currentSong,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        themeStyle = themeStyle,
                        iconPackStyle = iconPackStyle,
                        onPlayPause = { playbackManager.playPause() },
                        onSkipNext = { playbackManager.skipNext() },
                        onSkipPrev = { playbackManager.skipPrev() },
                        onClick = onOpenNowPlaying,
                        onLongClick = {
                            selectedTab = 2
                            scrollToSongTrigger++
                        }
                    )

                    // Liquid Glass Floating Pill Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = CircleShape,
                                    spotColor = primaryAccent.copy(alpha = 0.35f)
                                )
                                .clip(CircleShape)
                                .background(pillGlassBg)
                                .border(1.dp, pillBorderBrush, CircleShape)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tabs = listOf(
                                    Triple(0, "Главная", AppIcons.home(iconPackStyle)),
                                    Triple(1, "Поиск", AppIcons.search(iconPackStyle)),
                                    Triple(2, "Медиатека", AppIcons.library(iconPackStyle)),
                                    Triple(3, "Настройки", AppIcons.settings(iconPackStyle))
                                )

                                tabs.forEach { (index, title, icon) ->
                                    val isSelected = selectedTab == index
                                    val itemScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.06f else 1.0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "tab_scale"
                                    )
                                    val iconColor by animateColorAsState(
                                        targetValue = if (isSelected) primaryAccent else Color.White.copy(alpha = 0.5f),
                                        label = "tab_color"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .scale(itemScale)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) primaryAccent.copy(alpha = 0.18f) else Color.Transparent
                                            )
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                selectedTab = index
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = iconColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                text = title,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = iconColor,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Single Song Delete Confirmation Dialog
        songPendingDeletion?.let { song ->
            AlertDialog(
                onDismissRequest = { songPendingDeletion = null },
                title = { Text("Удаление трека") },
                text = { Text("Вы действительно хотите удалить «${song.title}» с устройства?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val s = song
                            songPendingDeletion = null
                            executeSingleSongDelete(s)
                        }
                    ) {
                        Text("Удалить", color = NeonPink, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { songPendingDeletion = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
