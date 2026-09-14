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

package com.example.openfy.core.audio.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val year: Int = 0,
    val albumArtUriString: String? = null
) {
    val albumArtUri: Uri?
        get() = albumArtUriString?.let { Uri.parse(it) }
}

data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int = 0
)

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val customCoverUri: String? = null,
    val isSystemFavorites: Boolean = false
)

data class FolderItem(
    val path: String,
    val name: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class SortOption {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,
    ARTIST_DESC,
    DATE_ADDED_DESC,
    DATE_ADDED_ASC,
    DURATION_DESC,
    DURATION_ASC,
    YEAR_DESC
}

data class EqualizerPreset(
    val name: String,
    val bandLevelsMb: List<Short>
)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
