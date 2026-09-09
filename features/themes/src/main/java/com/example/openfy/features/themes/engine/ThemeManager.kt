package com.example.openfy.features.themes.engine

import android.content.Context
import android.net.Uri
import androidx.compose.material3.ColorScheme
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.features.themes.model.ThemeColorsConfig
import com.example.openfy.features.themes.model.ThemeMetadata
import com.example.openfy.features.themes.model.toColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

data class ThemeState(
    val currentThemeId: String? = null,
    val customColorScheme: ColorScheme? = null,
    val installedThemes: List<ThemeMetadata> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ThemeManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val _themeState = MutableStateFlow(ThemeState(isLoading = true))
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        coroutineScope.launch {
            settingsRepository.customThemeId.collect { themeId ->
                val colorScheme = if (themeId != null) loadColorScheme(themeId) else null
                val installed = loadInstalledThemesInternal()
                _themeState.value = _themeState.value.copy(
                    currentThemeId = themeId,
                    customColorScheme = colorScheme,
                    installedThemes = installed,
                    isLoading = false
                )
            }
        }
    }

    fun getThemesDirectory(): File {
        val dir = File(context.filesDir, CUSTOM_THEMES_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Imports a `.thm` theme file from [Uri] and refreshes installed list.
     */
    suspend fun importTheme(uri: Uri): Result<ThemeMetadata> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IllegalArgumentException("Не удалось открыть файл темы по URI: $uri"))

            val result = ThemeParser.parseAndExtractThm(inputStream, getThemesDirectory())
            result.onSuccess {
                refreshInstalledThemes()
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Applies a custom theme by its [themeId] (or null to reset to default built-in styles).
     */
    fun applyTheme(themeId: String?) {
        settingsRepository.setCustomThemeId(themeId)
    }

    /**
     * Deletes an installed theme by [themeId].
     */
    fun deleteTheme(themeId: String): Boolean {
        val sanitizedId = themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val themeFolder = File(getThemesDirectory(), sanitizedId)
        val deleted = if (themeFolder.exists()) {
            themeFolder.deleteRecursively()
        } else false

        if (settingsRepository.customThemeId.value == themeId) {
            applyTheme(null)
        }
        refreshInstalledThemes()
        return deleted
    }

    fun refreshInstalledThemes() {
        val installed = loadInstalledThemesInternal()
        val currentId = settingsRepository.customThemeId.value
        val colorScheme = if (currentId != null) loadColorScheme(currentId) else null
        _themeState.value = _themeState.value.copy(
            currentThemeId = currentId,
            customColorScheme = colorScheme,
            installedThemes = installed
        )
    }

    fun getThemePreviewFile(themeId: String): File? {
        val sanitizedId = themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val themeFolder = File(getThemesDirectory(), sanitizedId)
        val previewFile = File(themeFolder, ThemeParser.PREVIEW_IMAGE_FILE)
        return if (previewFile.exists()) previewFile else null
    }

    fun loadColorScheme(themeId: String): ColorScheme? {
        val sanitizedId = themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val themeFolder = File(getThemesDirectory(), sanitizedId)
        val themeConfigFile = File(themeFolder, ThemeParser.THEME_CONFIG_FILE)
        if (!themeConfigFile.exists()) return null

        return try {
            val content = themeConfigFile.readText()
            val colors = json.decodeFromString<ThemeColorsConfig>(content)
            val manifestFile = File(themeFolder, ThemeParser.MANIFEST_CONFIG_FILE)
            val isDark = if (manifestFile.exists()) {
                try {
                    json.decodeFromString<ThemeMetadata>(manifestFile.readText()).isDark
                } catch (_: Exception) { true }
            } else true

            colors.toColorScheme(isDark = isDark)
        } catch (_: Exception) {
            null
        }
    }

    private fun loadInstalledThemesInternal(): List<ThemeMetadata> {
        val themesDir = getThemesDirectory()
        val dirs = themesDir.listFiles { f -> f.isDirectory } ?: return emptyList()

        return dirs.mapNotNull { dir ->
            val manifestFile = File(dir, ThemeParser.MANIFEST_CONFIG_FILE)
            val themeFile = File(dir, ThemeParser.THEME_CONFIG_FILE)

            val fileToRead = if (manifestFile.exists()) manifestFile else if (themeFile.exists()) themeFile else null
            if (fileToRead != null) {
                try {
                    json.decodeFromString<ThemeMetadata>(fileToRead.readText())
                } catch (_: Exception) {
                    null
                }
            } else null
        }
    }

    companion object {
        const val CUSTOM_THEMES_DIR_NAME = "custom_themes"
    }
}
