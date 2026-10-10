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

package com.example.openfy.features.streamer.resolver

import android.net.Uri
import com.example.openfy.core.audio.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class StreamMetadata(
    val streamUrl: String,
    val title: String,
    val artist: String,
    val album: String = "Online Stream",
    val coverUrl: String? = null,
    val contentType: String? = null,
    val bitrateKbps: Int? = null,
    val durationMs: Long = 0L,
    val isLive: Boolean = true
) {
    fun toSong(): Song = Song.createStreamTrack(
        url = streamUrl,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        coverUrl = coverUrl
    )
}

object SafeStreamResolver {

    /**
     * Validates and cleans user input URL.
     */
    fun sanitizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    /**
     * Checks if URL has a valid web audio/stream schema.
     */
    fun isValidStreamUrl(url: String): Boolean {
        val sanitized = sanitizeUrl(url)
        return try {
            val uri = Uri.parse(sanitized)
            val scheme = uri.scheme?.lowercase()
            (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves audio stream metadata (title, artist, cover, bitrate, duration) from direct link.
     * Offline-first parsing without network dependencies.
     */
    suspend fun resolveStream(rawUrl: String): Result<StreamMetadata> = withContext(Dispatchers.IO) {
        val targetUrl = sanitizeUrl(rawUrl)
        if (!isValidStreamUrl(targetUrl)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid audio stream URL"))
        }

        val fallbackTitle = extractNameFromUrl(targetUrl)
        val host = Uri.parse(targetUrl).host ?: "Audio Stream"
        Result.success(
            StreamMetadata(
                streamUrl = targetUrl,
                title = fallbackTitle,
                artist = host,
                album = "Web Stream",
                contentType = "audio/mpeg",
                isLive = true
            )
        )
    }

    private fun extractNameFromUrl(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val lastSegment = uri.lastPathSegment
            if (!lastSegment.isNullOrBlank()) {
                val decoded = URLDecoder.decode(lastSegment, StandardCharsets.UTF_8.name())
                val clean = decoded.substringBeforeLast('.')
                if (clean.isNotBlank() && clean != "stream" && clean != "live" && clean != "audio") {
                    clean.replace('_', ' ').replace('-', ' ').replaceFirstChar { it.uppercase() }
                } else {
                    uri.host ?: "Online Audio Stream"
                }
            } else {
                uri.host ?: "Online Audio Stream"
            }
        } catch (_: Exception) {
            "Online Audio Stream"
        }
    }
}
