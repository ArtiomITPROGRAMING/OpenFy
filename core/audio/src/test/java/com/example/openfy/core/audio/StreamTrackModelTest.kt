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

package com.example.openfy.core.audio

import com.example.openfy.core.audio.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamTrackModelTest {

    @Test
    fun `createStreamTrack generates valid Song with stream flags`() {
        val streamUrl = "https://icecast.somafm.com/groovesalad-256-mp3"
        val song = Song.createStreamTrack(
            url = streamUrl,
            title = "Groove Salad",
            artist = "SomaFM",
            album = "Ambient Radio"
        )

        assertTrue(song.isStream)
        assertEquals(streamUrl, song.streamUrl)
        assertEquals("Groove Salad", song.title)
        assertEquals("SomaFM", song.artist)
        assertEquals("Ambient Radio", song.album)
        assertEquals("Live Stream", song.formattedDuration)
        assertNotEquals(0L, song.id)
    }
}
