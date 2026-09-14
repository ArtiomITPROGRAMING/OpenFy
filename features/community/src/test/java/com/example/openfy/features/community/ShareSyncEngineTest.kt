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

package com.example.openfy.features.community

import com.example.openfy.features.community.sync.SharePayload
import com.example.openfy.features.community.sync.ShareType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareSyncEngineTest {

    @Test
    fun `SharePayload compresses to GZIP Base64 and decompresses cleanly`() {
        val originalPayload = SharePayload(
            version = 1,
            type = ShareType.PLAYLIST,
            title = "Cyber Synthwave 2026",
            description = "Selected electronic tracks",
            jsonData = """{"name":"Cyber Synthwave 2026","songIds":[1001,1002,1003,1004]}""",
            author = "Artiom"
        )

        // Encode
        val compressed = originalPayload.toCompressedString()
        assertTrue(compressed.startsWith("openfy://share?data="))

        // Decode
        val decodedResult = SharePayload.fromCompressedString(compressed)
        assertTrue(decodedResult.isSuccess)

        val restored = decodedResult.getOrThrow()
        assertEquals(originalPayload.version, restored.version)
        assertEquals(originalPayload.type, restored.type)
        assertEquals(originalPayload.title, restored.title)
        assertEquals(originalPayload.description, restored.description)
        assertEquals(originalPayload.jsonData, restored.jsonData)
        assertEquals(originalPayload.author, restored.author)
    }

    @Test
    fun `SharePayload handles raw JSON string gracefully`() {
        val rawJson = """
            {
                "version": 1,
                "type": "THEME",
                "title": "Amoled Steel",
                "description": "Minimal titanium theme",
                "jsonData": "{\"primary\":\"#FFFFFF\",\"background\":\"#000000\"}",
                "author": "FOSS Master"
            }
        """.trimIndent()

        val decodedResult = SharePayload.fromCompressedString(rawJson)
        assertTrue(decodedResult.isSuccess)

        val restored = decodedResult.getOrThrow()
        assertEquals(ShareType.THEME, restored.type)
        assertEquals("Amoled Steel", restored.title)
    }
}
