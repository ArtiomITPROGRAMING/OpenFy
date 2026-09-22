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

package com.example.openfy.features.themes.engine

import android.content.Context
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.features.themes.model.ThemeColorsConfig
import com.example.openfy.features.themes.model.ThemeMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class CatalogThemeItem(
    val id: String,
    val name: String,
    val author: String = "OpenFy Team",
    val version: String = "1.0.0",
    val isDark: Boolean = true,
    val description: String = "",
    val previewAccentHex: String = "#00FF66",
    val previewColors: List<String> = emptyList(),
    val downloadUrl: String = "",
    val rawJsonUrl: String = "",
    val colors: ThemeColorsConfig
)

object ThemeCatalogRepository {

    const val GITHUB_THEMES_REPO_URL = "https://github.com/ArtiomITPROGRAMING/OpenFy/tree/main/themes"
    const val GITHUB_CATALOG_RAW_URL = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/catalog.json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Built-in themes from the GitHub catalog.
     * Guarantees 100% offline availability and instant 1-click installation without waiting for internet.
     */
    val BUILT_IN_CATALOG = listOf(
        CatalogThemeItem(
            id = "emerald-matrix",
            name = "Изумрудная Матрица (Cyber Emerald)",
            author = "OpenFy Team",
            version = "1.0.0",
            isDark = true,
            description = "Кибернетический изумрудный терминал в стиле научной фантастики и матричного кода",
            previewAccentHex = "#00FF66",
            previewColors = listOf("#00FF66", "#00E5FF", "#050B07", "#0C1810"),
            downloadUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/emerald-matrix.thm",
            rawJsonUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/emerald-matrix/theme.json",
            colors = ThemeColorsConfig(
                primary = "#00FF66",
                onPrimary = "#000000",
                primaryContainer = "#003816",
                onPrimaryContainer = "#85FFAB",
                secondary = "#00E5FF",
                onSecondary = "#000000",
                secondaryContainer = "#003A42",
                onSecondaryContainer = "#8CEBFF",
                tertiary = "#76FF03",
                onTertiary = "#000000",
                background = "#050B07",
                onBackground = "#E0F5E6",
                surface = "#0C1810",
                onSurface = "#E0F5E6",
                surfaceVariant = "#152419",
                onSurfaceVariant = "#7FA388",
                outline = "#1C3623",
                outlineVariant = "#0F2115",
                accent = "#00FF66",
                cardColor = "#0F2115"
            )
        ),
        CatalogThemeItem(
            id = "nordic-frost",
            name = "Северное Сияние (Nordic Frost)",
            author = "OpenFy Team",
            version = "1.0.0",
            isDark = true,
            description = "Холодная арктическая эстетика с мерцанием полярного сияния и ледяным акцентом",
            previewAccentHex = "#38BDF8",
            previewColors = listOf("#38BDF8", "#A855F7", "#0B1120", "#131D33"),
            downloadUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/nordic-frost.thm",
            rawJsonUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/nordic-frost/theme.json",
            colors = ThemeColorsConfig(
                primary = "#38BDF8",
                onPrimary = "#001E2E",
                primaryContainer = "#00354E",
                onPrimaryContainer = "#C3E8FF",
                secondary = "#A855F7",
                onSecondary = "#FFFFFF",
                secondaryContainer = "#4C1D95",
                onSecondaryContainer = "#E9D5FF",
                tertiary = "#2DD4BF",
                onTertiary = "#00201C",
                background = "#0B1120",
                onBackground = "#E2E8F0",
                surface = "#131D33",
                onSurface = "#E2E8F0",
                surfaceVariant = "#1E293B",
                onSurfaceVariant = "#94A3B8",
                outline = "#334155",
                outlineVariant = "#1E293B",
                accent = "#38BDF8",
                cardColor = "#17233D"
            )
        ),
        CatalogThemeItem(
            id = "sunset-synthwave",
            name = "Закатный Синтвейв (Sunset Synthwave)",
            author = "OpenFy Team",
            version = "1.0.0",
            isDark = true,
            description = "Неоновые закаты Майами 80-х, тёплый янтарь и винтажная лазерная сетка",
            previewAccentHex = "#FF7A00",
            previewColors = listOf("#FF7A00", "#FF007A", "#12091A", "#1F102B"),
            downloadUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/sunset-synthwave.thm",
            rawJsonUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/sunset-synthwave/theme.json",
            colors = ThemeColorsConfig(
                primary = "#FF7A00",
                onPrimary = "#000000",
                primaryContainer = "#471D00",
                onPrimaryContainer = "#FFDCC2",
                secondary = "#FF007A",
                onSecondary = "#FFFFFF",
                secondaryContainer = "#5C002B",
                onSecondaryContainer = "#FFD8E4",
                tertiary = "#BD00FF",
                onTertiary = "#FFFFFF",
                background = "#12091A",
                onBackground = "#F5E9FF",
                surface = "#1F102B",
                onSurface = "#F5E9FF",
                surfaceVariant = "#2C173D",
                onSurfaceVariant = "#A990BA",
                outline = "#44245E",
                outlineVariant = "#261336",
                accent = "#FF7A00",
                cardColor = "#241333"
            )
        ),
        CatalogThemeItem(
            id = "tokyo-night",
            name = "Токийская Ночь (Tokyo Night & Sakura)",
            author = "OpenFy Team",
            version = "1.0.0",
            isDark = true,
            description = "Атмосфера ночного Сибуя: неоновые вывески, лепестки сакуры и глубокая полночь",
            previewAccentHex = "#F43F5E",
            previewColors = listOf("#F43F5E", "#818CF8", "#0D0F18", "#161926"),
            downloadUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/tokyo-night.thm",
            rawJsonUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/tokyo-night/theme.json",
            colors = ThemeColorsConfig(
                primary = "#F43F5E",
                onPrimary = "#FFFFFF",
                primaryContainer = "#540018",
                onPrimaryContainer = "#FFD9E0",
                secondary = "#818CF8",
                onSecondary = "#00105C",
                secondaryContainer = "#1E2A78",
                onSecondaryContainer = "#E0E0FF",
                tertiary = "#38BDF8",
                onTertiary = "#002030",
                background = "#0D0F18",
                onBackground = "#E4E6F0",
                surface = "#161926",
                onSurface = "#E4E6F0",
                surfaceVariant = "#22273B",
                onSurfaceVariant = "#8E94AA",
                outline = "#363C57",
                outlineVariant = "#1D2132",
                accent = "#F43F5E",
                cardColor = "#1A1E2E"
            )
        ),
        CatalogThemeItem(
            id = "pure-gold-luxury",
            name = "Королевский Оникс (Obsidian & Royal Gold)",
            author = "OpenFy Team",
            version = "1.0.0",
            isDark = true,
            description = "Премиальный минимализм чистого золота на бархатном ониксовом фоне",
            previewAccentHex = "#FFD700",
            previewColors = listOf("#FFD700", "#FFA000", "#080808", "#141414"),
            downloadUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/pure-gold-luxury.thm",
            rawJsonUrl = "https://raw.githubusercontent.com/ArtiomITPROGRAMING/OpenFy/main/themes/pure-gold-luxury/theme.json",
            colors = ThemeColorsConfig(
                primary = "#FFD700",
                onPrimary = "#000000",
                primaryContainer = "#423700",
                onPrimaryContainer = "#FFE266",
                secondary = "#FFA000",
                onSecondary = "#000000",
                secondaryContainer = "#452B00",
                onSecondaryContainer = "#FFDF9E",
                tertiary = "#FFF8E1",
                onTertiary = "#261900",
                background = "#080808",
                onBackground = "#F0EBE1",
                surface = "#141414",
                onSurface = "#F0EBE1",
                surfaceVariant = "#212121",
                onSurfaceVariant = "#A19B91",
                outline = "#333333",
                outlineVariant = "#1C1C1C",
                accent = "#FFD700",
                cardColor = "#171717"
            )
        )
    )

    /**
     * Fetches catalog themes from GitHub if online, or immediately falls back to [BUILT_IN_CATALOG].
     */
    suspend fun getCatalogThemes(): List<CatalogThemeItem> = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_CATALOG_RAW_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 4000
                requestMethod = "GET"
                instanceFollowRedirects = true
            }

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val parsed = json.decodeFromString<List<CatalogThemeItem>>(jsonString)
                if (parsed.isNotEmpty()) return@withContext parsed
            }
        } catch (_: Exception) {
            // Offline or timeout, safely fall back to built-in catalog
        }

        BUILT_IN_CATALOG
    }

    /**
     * Installs a catalog theme onto the phone, saves it in private app storage,
     * and sets it as the active theme in [SettingsRepository].
     */
    suspend fun installCatalogTheme(
        context: Context,
        settingsRepository: SettingsRepository,
        item: CatalogThemeItem
    ): Result<ThemeMetadata> = withContext(Dispatchers.IO) {
        try {
            val metadata = ThemeMetadata(
                id = item.id,
                name = item.name,
                author = item.author,
                version = item.version,
                isDark = item.isDark,
                description = item.description,
                previewAccentHex = item.previewAccentHex
            )

            val manifestJson = json.encodeToString(ThemeMetadata.serializer(), metadata)
            val colorsJson = json.encodeToString(ThemeColorsConfig.serializer(), item.colors)

            val targetDir = ThemeEngine.getThemesDirectory(context)
            val themeFolder = java.io.File(targetDir, item.id)
            if (themeFolder.exists()) {
                themeFolder.deleteRecursively()
            }
            themeFolder.mkdirs()

            java.io.File(themeFolder, ThemeParser.MANIFEST_CONFIG_FILE).writeText(manifestJson)
            java.io.File(themeFolder, ThemeParser.THEME_CONFIG_FILE).writeText(colorsJson)

            settingsRepository.setCustomThemeId(item.id)
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads and installs a theme (.thm or .json) from any direct URL.
     */
    suspend fun downloadAndInstallThemeFromUrl(
        context: Context,
        settingsRepository: SettingsRepository,
        urlStr: String
    ): Result<ThemeMetadata> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr.trim())
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 15000
                requestMethod = "GET"
                instanceFollowRedirects = true
            }

            if (connection.responseCode !in 200..299) {
                return@withContext Result.failure(IllegalStateException("HTTP ${connection.responseCode}: ${connection.responseMessage}"))
            }

            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Получен пустой файл темы"))
            }

            val targetDir = ThemeEngine.getThemesDirectory(context)
            val parseResult = ThemeParser.parseAndExtractThm(ByteArrayInputStream(bytes), targetDir)

            parseResult.onSuccess { metadata ->
                settingsRepository.setCustomThemeId(metadata.id)
            }

            parseResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
