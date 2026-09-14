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
