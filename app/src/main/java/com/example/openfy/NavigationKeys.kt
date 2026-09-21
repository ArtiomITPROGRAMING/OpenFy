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

package com.example.openfy

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object LocalTracksKey : NavKey
@Serializable data object AuthOnboardingKey : NavKey
@Serializable data class OtpVerificationKey(val email: String) : NavKey
@Serializable data object MainDashboardKey : NavKey
@Serializable data object NowPlayingKey : NavKey
@Serializable data object EqualizerKey : NavKey
@Serializable data object StudioFxKey : NavKey
@Serializable data object DuplicateCleanerKey : NavKey
@Serializable data object ThemesKey : NavKey
@Serializable data object CommunityProfileKey : NavKey
@Serializable data object QrScannerKey : NavKey
@Serializable data object CarModeKey : NavKey
@Serializable data class PlaylistDetailKey(val playlistId: String) : NavKey
