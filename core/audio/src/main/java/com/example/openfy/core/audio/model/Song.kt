package com.example.openfy.core.audio.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val path: String,
    val contentUriString: String,
    val albumArtUriString: String? = null,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val dateAdded: Long = 0,
    val sizeBytes: Long = 0,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val isStream: Boolean = false,
    val streamUrl: String? = null
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return if (isStream) "Live Stream" else "0:00"
            val totalSec = durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return "%d:%02d".format(min, sec)
        }

    val contentUri: Uri
        get() = Uri.parse(contentUriString)

    val albumArtUri: Uri?
        get() = albumArtUriString?.let { Uri.parse(it) }

    companion object {
        fun createStreamTrack(
            url: String,
            title: String = "Online Audio Stream",
            artist: String = "Web Stream",
            album: String = "OpenFy Streamer",
            durationMs: Long = 0L,
            coverUrl: String? = null
        ): Song {
            val streamId = (url.hashCode().toLong() and 0x7FFFFFFF) or 0x4000000000000000L
            return Song(
                id = streamId,
                title = title,
                artist = artist,
                album = album,
                albumId = -1L,
                durationMs = durationMs,
                path = url,
                contentUriString = url,
                albumArtUriString = coverUrl,
                trackNumber = 1,
                year = 2026,
                dateAdded = System.currentTimeMillis() / 1000,
                sizeBytes = 0L,
                isFavorite = false,
                playCount = 1,
                isStream = true,
                streamUrl = url
            )
        }
    }
}
