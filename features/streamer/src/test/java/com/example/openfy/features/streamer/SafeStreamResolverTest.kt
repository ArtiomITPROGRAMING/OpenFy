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

package com.example.openfy.features.streamer

import com.example.openfy.features.streamer.data.OpenSourceMusicCatalog
import com.example.openfy.features.streamer.data.StreamRepository
import com.example.openfy.features.streamer.resolver.SafeStreamResolver
import com.example.openfy.features.streamer.resolver.StreamMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeStreamResolverTest {

    @Test
    fun `sanitizeUrl prepends https if missing`() {
        assertEquals("https://stream.example.com/audio.mp3", SafeStreamResolver.sanitizeUrl("stream.example.com/audio.mp3"))
        assertEquals("http://custom.stream/track", SafeStreamResolver.sanitizeUrl("http://custom.stream/track"))
        assertEquals("https://secure.stream/song.flac", SafeStreamResolver.sanitizeUrl("  https://secure.stream/song.flac  "))
    }

    @Test
    fun `isValidStreamUrl validates urls correctly`() {
        assertTrue(SafeStreamResolver.isValidStreamUrl("https://audio.openfy.org/track.mp3"))
        assertTrue(SafeStreamResolver.isValidStreamUrl("http://cdn.pixabay.com/audio/song.mp3"))
        assertTrue(SafeStreamResolver.isValidStreamUrl("myhost.com/music.ogg"))

        assertFalse(SafeStreamResolver.isValidStreamUrl(""))
        assertFalse(SafeStreamResolver.isValidStreamUrl("not a url"))
    }

    @Test
    fun `StreamMetadata toSong converts to Song with isStream true`() {
        val meta = StreamMetadata(
            streamUrl = "https://stream.openfy.org/synthwave.mp3",
            title = "Midnight Drive",
            artist = "Retro Synth",
            album = "Online Music",
            coverUrl = "https://stream.openfy.org/cover.png",
            durationMs = 180000L,
            isLive = false
        )

        val song = meta.toSong()
        assertTrue(song.isStream)
        assertEquals("https://stream.openfy.org/synthwave.mp3", song.streamUrl)
        assertEquals("Midnight Drive", song.title)
        assertEquals("Retro Synth", song.artist)
        assertEquals("Online Music", song.album)
        assertEquals("3:00", song.formattedDuration)
    }

    @Test
    fun `OpenSourceMusicCatalog provides valid tracks and genre filters`() {
        val allTracks = OpenSourceMusicCatalog.getTracksByGenre("Все")
        assertTrue(allTracks.isNotEmpty())
        assertTrue(allTracks.all { it.isStream })
        assertTrue(allTracks.all { it.streamUrl?.startsWith("http") == true })

        val lofiTracks = OpenSourceMusicCatalog.getTracksByGenre("Lo-Fi")
        assertTrue(lofiTracks.isNotEmpty())
        assertTrue(lofiTracks.all { it.isStream })

        val synthwaveTracks = OpenSourceMusicCatalog.getTracksByGenre("Synthwave")
        assertTrue(synthwaveTracks.isNotEmpty())

        val searchResults = OpenSourceMusicCatalog.searchTracks("Debussy")
        assertTrue(searchResults.isNotEmpty())
        assertEquals("Clair de Lune (Debussy)", searchResults.first().title)
    }

    @Test
    fun `StreamRepository returns catalog and searches music properly`() {
        val tracks = StreamRepository.getCatalogTracks("Classical")
        assertTrue(tracks.isNotEmpty())
        assertTrue(tracks.all { it.isStream })

        val rockTracks = StreamRepository.getCatalogTracks("Rock")
        assertTrue(rockTracks.isNotEmpty())
    }
}
