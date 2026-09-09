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
