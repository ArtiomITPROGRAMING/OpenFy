package com.example.openfy

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object LocalTracksKey : NavKey
@Serializable data object AuthOnboardingKey : NavKey
@Serializable data class OtpVerificationKey(val email: String) : NavKey
@Serializable data object MainDashboardKey : NavKey
@Serializable data object NowPlayingKey : NavKey
@Serializable data object EqualizerKey : NavKey
@Serializable data object LyricsKey : NavKey
@Serializable data object DuplicateCleanerKey : NavKey
@Serializable data object ThemesKey : NavKey
@Serializable data object CommunityProfileKey : NavKey
@Serializable data object QrScannerKey : NavKey
@Serializable data object CarModeKey : NavKey
@Serializable data class PlaylistDetailKey(val playlistId: String) : NavKey
