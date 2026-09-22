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

package com.example.openfy.features.themes

import androidx.compose.ui.graphics.Color
import com.example.openfy.features.themes.engine.ThemeEngine
import com.example.openfy.features.themes.engine.ThemeParser
import com.example.openfy.features.themes.model.ThemeColors
import com.example.openfy.features.themes.model.ThemeColorsConfig
import com.example.openfy.features.themes.model.ThemeManifest
import com.example.openfy.features.themes.model.ThemeMetadata
import com.example.openfy.features.themes.model.parseHexColor
import com.example.openfy.features.themes.model.toColorScheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ThemeEngineTest {

    @Test
    fun `parseHexColor parses 6-digit, 8-digit, 3-digit hex strings and handles fallbacks`() {
        val cyan = parseHexColor("#00E5FF")
        assertEquals(Color(0xFF00E5FF), cyan)

        val alphaRed = parseHexColor("#80FF0000")
        assertEquals(Color(0x80FF0000), alphaRed)

        val shortHex = parseHexColor("#FFF")
        assertEquals(Color(0xFFFFFFFF), shortHex)

        val fallback = parseHexColor("invalid_hex", fallback = Color.Green)
        assertEquals(Color.Green, fallback)
    }

    @Test
    fun `toColorScheme produces dark and light Material 3 ColorSchemes`() {
        val themeColors = ThemeColors(
            primary = "#FF0077",
            onPrimary = "#FFFFFF",
            background = "#050508",
            surface = "#101015",
            secondary = "#00FFCC",
            tertiary = "#FFFF00"
        )

        val darkScheme = themeColors.toColorScheme(isDark = true)
        assertEquals(Color(0xFFFF0077), darkScheme.primary)
        assertEquals(Color(0xFFFFFFFF), darkScheme.onPrimary)
        assertEquals(Color(0xFF050508), darkScheme.background)
        assertEquals(Color(0xFF101015), darkScheme.surface)

        val lightScheme = themeColors.toColorScheme(isDark = false)
        assertEquals(Color(0xFFFF0077), lightScheme.primary)
    }

    @Test
    fun `ThemeMetadata and ThemeColorsConfig serialize and deserialize correctly`() {
        val metadata = ThemeMetadata(
            id = "synthwave_glow",
            name = "Synthwave Glow",
            author = "OpenFy Master",
            version = "1.2.0",
            minAppVersion = 1,
            isDark = true,
            description = "Neon retro theme for nighttime listening."
        )

        val jsonString = ThemeEngine.json.encodeToString(ThemeMetadata.serializer(), metadata)
        val decoded = ThemeEngine.json.decodeFromString<ThemeMetadata>(jsonString)

        assertEquals(metadata.id, decoded.id)
        assertEquals(metadata.name, decoded.name)
        assertEquals(metadata.author, decoded.author)
        assertEquals(metadata.version, decoded.version)
        assertEquals(metadata.isDark, decoded.isDark)
    }

    @Test
    fun `ThemeParser parseAndExtractThm unpacks and validates zip archive with preview`() = runTest {
        val themeJson = """
            {
                "id": "cyber_obsidian",
                "name": "Cyber Obsidian",
                "author": "Artiom",
                "version": "1.0",
                "isDark": true,
                "primary": "#FF1744",
                "background": "#0D0004",
                "surface": "#1A0008"
            }
        """.trimIndent()

        val fakePreviewPng = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry(ThemeParser.THEME_CONFIG_FILE))
            zip.write(themeJson.toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry(ThemeParser.PREVIEW_IMAGE_FILE))
            zip.write(fakePreviewPng)
            zip.closeEntry()
        }

        val tempDir = File.createTempFile("openfy_test_themes", "").apply {
            delete()
            mkdirs()
        }

        try {
            val result = ThemeParser.parseAndExtractThm(ByteArrayInputStream(baos.toByteArray()), tempDir)
            assertTrue(result.isSuccess)
            val metadata = result.getOrThrow()
            assertEquals("cyber_obsidian", metadata.id)
            assertEquals("Cyber Obsidian", metadata.name)

            val extractedDir = File(tempDir, "cyber_obsidian")
            assertTrue(File(extractedDir, ThemeParser.THEME_CONFIG_FILE).exists())
            assertTrue(File(extractedDir, ThemeParser.PREVIEW_IMAGE_FILE).exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `ThemeParser parseAndExtractThm also parses raw standalone JSON theme files`() = runTest {
        val themeJson = """
            {
                "id": "emerald_direct",
                "name": "Emerald Direct",
                "author": "OpenFy Team",
                "version": "1.0.0",
                "isDark": true,
                "primary": "#00FF66",
                "background": "#050B07",
                "surface": "#0C1810"
            }
        """.trimIndent()

        val tempDir = File.createTempFile("openfy_json_test", "").apply {
            delete()
            mkdirs()
        }

        try {
            val result = ThemeParser.parseAndExtractThm(ByteArrayInputStream(themeJson.toByteArray()), tempDir)
            assertTrue(result.isSuccess)
            val metadata = result.getOrThrow()
            assertEquals("emerald_direct", metadata.id)
            assertEquals("Emerald Direct", metadata.name)

            val extractedDir = File(tempDir, "emerald_direct")
            assertTrue(File(extractedDir, ThemeParser.THEME_CONFIG_FILE).exists())
            assertTrue(File(extractedDir, ThemeParser.MANIFEST_CONFIG_FILE).exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `ThemeCatalogRepository BUILT_IN_CATALOG contains 5 valid themes with valid hex colors`() {
        val catalog = com.example.openfy.features.themes.engine.ThemeCatalogRepository.BUILT_IN_CATALOG
        assertEquals(5, catalog.size)

        for (item in catalog) {
            assertTrue(item.id.isNotBlank())
            assertTrue(item.name.isNotBlank())
            val primaryColor = parseHexColor(item.colors.primary)
            assertNotNull(primaryColor)

            val scheme = item.colors.toColorScheme(isDark = item.isDark)
            assertNotNull(scheme)
        }
    }

    @Test
    fun `invalid JSON produces descriptive failure without crash`() {
        val malformedJson = "{ id: missing_quotes }"
        val result = runCatching {
            ThemeEngine.json.decodeFromString<ThemeManifest>(malformedJson)
        }
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
    }
}
