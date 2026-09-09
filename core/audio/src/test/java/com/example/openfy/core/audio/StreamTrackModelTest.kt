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
