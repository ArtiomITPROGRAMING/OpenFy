package com.example.openfy.core.audio

import com.example.openfy.core.audio.service.PlayerCacheProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerCacheProviderTest {

    @Test
    fun `PlayerCacheProvider configuration constants match specification`() {
        assertEquals(500L * 1024 * 1024, PlayerCacheProvider.MAX_CACHE_SIZE_BYTES)
        assertEquals("audio_cache", PlayerCacheProvider.CACHE_DIR_NAME)
        assertEquals("OpenFy-AudioPlayer", PlayerCacheProvider.USER_AGENT)
        assertEquals(15000, PlayerCacheProvider.CONNECT_TIMEOUT_MS)
        assertEquals(15000, PlayerCacheProvider.READ_TIMEOUT_MS)
    }
}
