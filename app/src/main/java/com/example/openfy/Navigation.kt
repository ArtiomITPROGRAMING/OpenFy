package com.example.openfy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.openfy.core.audio.data.AudioScanner
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.CarModeManager
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.CarModePlayerScreen
import com.example.openfy.ui.screens.DuplicateCleanerScreen
import com.example.openfy.ui.screens.EqualizerScreen
import com.example.openfy.ui.screens.LyricsScreen
import com.example.openfy.ui.screens.MainScreenWithBottomNav
import com.example.openfy.ui.screens.NowPlayingScreen
import com.example.openfy.ui.screens.PlaylistDetailScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@Composable
fun MainNavigation(
    playbackManager: PlaybackManager,
    profileViewModel: com.example.openfy.features.community.ui.ProfileViewModel,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    // Offline-First Default: Always launch into local tracks library on startup
    val initialKey = remember { LocalTracksKey }
    val backStack = rememberNavBackStack(initialKey)
    val context = LocalContext.current
    val audioScanner = remember { AudioScanner(context) }
    var allScannedSongs by remember { mutableStateOf<List<Song>>(emptyList()) }

    val carModeManager = remember { CarModeManager.getInstance(context) }
    val isCarModeActive by carModeManager.isCarModeActive.collectAsState()

    LaunchedEffect(isCarModeActive) {
        if (isCarModeActive && backStack.lastOrNull() != CarModeKey) {
            backStack.add(CarModeKey)
        }
    }

    val scope = rememberCoroutineScope()

    fun refreshAudio() {
        scope.launch {
            allScannedSongs = audioScanner.scanLocalAudio(playbackManager.settingsRepository.ignoreShortTracks.value)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize(),
        entryProvider = entryProvider {
            entry<AuthOnboardingKey> {
                com.example.openfy.features.community.ui.AuthOnboardingScreen(
                    viewModel = profileViewModel,
                    settingsRepository = playbackManager.settingsRepository,
                    onAuthSuccess = {
                        backStack.clear()
                        backStack.add(MainDashboardKey)
                    },
                    onNavigateToOtpVerification = { email ->
                        backStack.add(OtpVerificationKey(email))
                    },
                    onContinueWithoutAccount = {
                        backStack.clear()
                        backStack.add(MainDashboardKey)
                    }
                )
            }
            entry<OtpVerificationKey> { key ->
                com.example.openfy.features.community.ui.OtpVerificationScreen(
                    email = key.email,
                    viewModel = profileViewModel,
                    settingsRepository = playbackManager.settingsRepository,
                    onVerificationSuccess = {
                        backStack.clear()
                        backStack.add(MainDashboardKey)
                    },
                    onChangeEmail = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<LocalTracksKey> {
                MainScreenWithBottomNav(
                    playbackManager = playbackManager,
                    windowWidthSizeClass = windowWidthSizeClass,
                    onOpenNowPlaying = { backStack.add(NowPlayingKey) },
                    onNavigateToEqualizer = { backStack.add(EqualizerKey) },
                    onNavigateToDuplicateCleaner = { backStack.add(DuplicateCleanerKey) },
                    onNavigateToThemes = { backStack.add(ThemesKey) },
                    onNavigateToProfile = { backStack.add(CommunityProfileKey) },
                    onNavigateToQrScanner = { backStack.add(QrScannerKey) },
                    onNavigateToCarMode = { backStack.add(CarModeKey) },
                    onNavigateToPlaylist = { playlistId -> backStack.add(PlaylistDetailKey(playlistId)) },
                    onAudioScanned = { allScannedSongs = it }
                )
            }
            entry<MainDashboardKey> {
                MainScreenWithBottomNav(
                    playbackManager = playbackManager,
                    windowWidthSizeClass = windowWidthSizeClass,
                    onOpenNowPlaying = { backStack.add(NowPlayingKey) },
                    onNavigateToEqualizer = { backStack.add(EqualizerKey) },
                    onNavigateToDuplicateCleaner = { backStack.add(DuplicateCleanerKey) },
                    onNavigateToThemes = { backStack.add(ThemesKey) },
                    onNavigateToProfile = { backStack.add(CommunityProfileKey) },
                    onNavigateToQrScanner = { backStack.add(QrScannerKey) },
                    onNavigateToCarMode = { backStack.add(CarModeKey) },
                    onNavigateToPlaylist = { playlistId -> backStack.add(PlaylistDetailKey(playlistId)) },
                    onAudioScanned = { allScannedSongs = it }
                )
            }
            entry<NowPlayingKey> {
                NowPlayingScreen(
                    playbackManager = playbackManager,
                    onDismiss = { backStack.removeLastOrNull() },
                    onNavigateToEqualizer = { backStack.add(EqualizerKey) },
                    onNavigateToLyrics = { backStack.add(LyricsKey) },
                    onNavigateToCarMode = { backStack.add(CarModeKey) }
                )
            }
            entry<CarModeKey> {
                CarModePlayerScreen(
                    playbackManager = playbackManager,
                    onBack = {
                        carModeManager.setCarModeActive(false)
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<EqualizerKey> {
                EqualizerScreen(
                    playbackManager = playbackManager,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<LyricsKey> {
                LyricsScreen(
                    playbackManager = playbackManager,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<DuplicateCleanerKey> {
                DuplicateCleanerScreen(
                    playbackManager = playbackManager,
                    allSongs = allScannedSongs,
                    onBack = { backStack.removeLastOrNull() },
                    onRefreshAudio = { refreshAudio() }
                )
            }
            entry<ThemesKey> {
                com.example.openfy.features.themes.ui.ThemesScreen(
                    settingsRepository = playbackManager.settingsRepository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<CommunityProfileKey> {
                com.example.openfy.features.community.ui.CommunityProfileScreen(
                    viewModel = profileViewModel,
                    settingsRepository = playbackManager.settingsRepository,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToQrScanner = { backStack.add(QrScannerKey) },
                    onLogout = {
                        profileViewModel.logout()
                        backStack.clear()
                        backStack.add(AuthOnboardingKey)
                    }
                )
            }
            entry<QrScannerKey> {
                com.example.openfy.features.community.ui.QrScannerScreen(
                    playlistRepository = playbackManager.playlistRepository,
                    settingsRepository = playbackManager.settingsRepository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<PlaylistDetailKey> { key ->
                PlaylistDetailScreen(
                    playlistId = key.playlistId,
                    allSongs = allScannedSongs,
                    playbackManager = playbackManager,
                    onBack = { backStack.removeLastOrNull() },
                    onDeleteSong = { song ->
                        playbackManager.onSongDeleted(song.id)
                        refreshAudio()
                    }
                )
            }

        }
    )
}
