package com.example.openfy.features.themes.engine

import android.content.Context
import android.net.Uri
import androidx.compose.material3.ColorScheme
import com.example.openfy.features.themes.model.ThemeColors
import com.example.openfy.features.themes.model.ThemeColorsConfig
import com.example.openfy.features.themes.model.ThemeManifest
import com.example.openfy.features.themes.model.ThemeMetadata
import com.example.openfy.features.themes.model.toColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ThemeEngine {

    const val THEMES_DIR_NAME = "custom_themes"
    const val LEGACY_THEMES_DIR_NAME = "themes"
    const val MANIFEST_FILE_NAME = "manifest.json"
    const val THEME_FILE_NAME = "theme.json"
    const val PREVIEW_FILE_NAME = "preview.png"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    fun getThemesDirectory(context: Context): File {
        val dir = File(context.filesDir, THEMES_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Imports a `.thm` ZIP theme from a [Uri].
     * Extracts files to `context.filesDir/custom_themes/{theme_id}/` and validates contents.
     */
    suspend fun importThemeFromUri(context: Context, uri: Uri): Result<ThemeManifest> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IllegalArgumentException("Не удалось открыть файл темы по URI: $uri"))

            importThemeFromStream(context, inputStream)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Imports a `.thm` theme from an [InputStream].
     */
    suspend fun importThemeFromStream(context: Context, inputStream: InputStream): Result<ThemeManifest> = withContext(Dispatchers.IO) {
        val targetDir = getThemesDirectory(context)
        val parseResult = ThemeParser.parseAndExtractThm(inputStream, targetDir)
        parseResult.map { metadata ->
            ThemeManifest(
                id = metadata.id,
                name = metadata.name,
                author = metadata.author,
                version = metadata.version,
                minAppVersion = metadata.minAppVersion,
                isDark = metadata.isDark,
                description = metadata.description,
                previewAccentHex = metadata.previewAccentHex
            )
        }
    }

    /**
     * Reads and parses [ThemeColors] for a given [themeId].
     */
    fun loadThemeColorsRaw(context: Context, themeId: String): ThemeColors? {
        val sanitizedId = themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val themeDir = File(getThemesDirectory(context), sanitizedId)
        val legacyThemeDir = File(File(context.filesDir, LEGACY_THEMES_DIR_NAME), sanitizedId)

        val targetDir = if (themeDir.exists()) themeDir else legacyThemeDir
        val themeFile = File(targetDir, THEME_FILE_NAME)
        if (!themeFile.exists()) return null

        return try {
            json.decodeFromString<ThemeColors>(themeFile.readText())
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Loads Compose [ColorScheme] for a given [themeId].
     */
    fun loadThemeColors(context: Context, themeId: String): ColorScheme? {
        val manifest = getManifest(context, themeId)
        val colors = loadThemeColorsRaw(context, themeId) ?: return null
        return colors.toColorScheme(isDark = manifest?.isDark ?: true)
    }

    /**
     * Gets manifest of a specific installed theme.
     */
    fun getManifest(context: Context, themeId: String): ThemeManifest? {
        val sanitizedId = themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val themeDir = File(getThemesDirectory(context), sanitizedId)
        val legacyThemeDir = File(File(context.filesDir, LEGACY_THEMES_DIR_NAME), sanitizedId)

        val targetDir = if (themeDir.exists()) themeDir else legacyThemeDir
        val manifestFile = File(targetDir, MANIFEST_FILE_NAME)
        val themeFile = File(targetDir, THEME_FILE_NAME)

        val fileToRead = if (manifestFile.exists()) manifestFile else if (themeFile.exists()) themeFile else null
        if (fileToRead == null) return null

        return try {
            val metadata = json.decodeFromString<ThemeMetadata>(fileToRead.readText())
            ThemeManifest(
                id = metadata.id,
                name = metadata.name,
                author = metadata.author,
                version = metadata.version,
                minAppVersion = metadata.minAppVersion,
                isDark = metadata.isDark,
                description = metadata.description,
                previewAccentHex = metadata.previewAccentHex
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Returns a list of all installed themes.
     */
    fun getInstalledThemes(context: Context): List<ThemeManifest> {
        val themesDir = getThemesDirectory(context)
        val legacyThemesDir = File(context.filesDir, LEGACY_THEMES_DIR_NAME)

        val allDirs = (themesDir.listFiles { f -> f.isDirectory }?.toList() ?: emptyList()) +
                (legacyThemesDir.listFiles { f -> f.isDirectory }?.toList() ?: emptyList())

        val themesMap = mutableMapOf<String, ThemeManifest>()

        allDirs.forEach { dir ->
            val manifestFile = File(dir, MANIFEST_FILE_NAME)
            val themeFile = File(dir, THEME_FILE_NAME)
            val fileToRead = if (manifestFile.exists()) manifestFile else if (themeFile.exists()) themeFile else null

            if (fileToRead != null) {
                try {
                    val metadata = json.decodeFromString<ThemeMetadata>(fileToRead.readText())
                    if (!themesMap.containsKey(metadata.id)) {
                        themesMap[metadata.id] = ThemeManifest(
                            id = metadata.id,
                            name = metadata.name,
                            author = metadata.author,
                            version = metadata.version,
                            minAppVersion = metadata.minAppVersion,
                            isDark = metadata.isDark,
                            description = metadata.description,
                            previewAccentHex = metadata.previewAccentHex
                        )
                    }
                } catch (_: Exception) {}
            }
        }

        return themesMap.values.toList()
    }

    /**
     * Deletes an installed theme by [themeId].
     */
    fun deleteTheme(context: Context, themeId: String): Boolean {
        val sanitizedId = themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val themeDir = File(getThemesDirectory(context), sanitizedId)
        val legacyThemeDir = File(File(context.filesDir, LEGACY_THEMES_DIR_NAME), sanitizedId)

        var deleted = false
        if (themeDir.exists()) {
            deleted = themeDir.deleteRecursively() || deleted
        }
        if (legacyThemeDir.exists()) {
            deleted = legacyThemeDir.deleteRecursively() || deleted
        }
        return deleted
    }

    fun getPreviewFile(context: Context, themeId: String): File? {
        val sanitizedId = themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val themeDir = File(getThemesDirectory(context), sanitizedId)
        val preview = File(themeDir, PREVIEW_FILE_NAME)
        if (preview.exists()) return preview

        val legacyPreview = File(File(context.filesDir, LEGACY_THEMES_DIR_NAME), "$sanitizedId/$PREVIEW_FILE_NAME")
        return if (legacyPreview.exists()) legacyPreview else null
    }

    /**
     * Packs [ThemeManifest] and [ThemeColors] into a `.thm` ZIP file.
     */
    fun exportThemeToZip(manifest: ThemeManifest, colors: ThemeColors, outputFile: File, previewImage: File? = null): Result<File> {
        return try {
            val manifestJson = json.encodeToString(ThemeManifest.serializer(), manifest)
            val themeJson = json.encodeToString(ThemeColors.serializer(), colors)

            ZipOutputStream(FileOutputStream(outputFile)).use { zip ->
                zip.putNextEntry(ZipEntry(MANIFEST_FILE_NAME))
                zip.write(manifestJson.toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry(THEME_FILE_NAME))
                zip.write(themeJson.toByteArray())
                zip.closeEntry()

                if (previewImage != null && previewImage.exists()) {
                    zip.putNextEntry(ZipEntry(PREVIEW_FILE_NAME))
                    zip.write(previewImage.readBytes())
                    zip.closeEntry()
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
