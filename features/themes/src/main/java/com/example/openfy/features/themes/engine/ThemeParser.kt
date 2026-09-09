package com.example.openfy.features.themes.engine

import android.content.Context
import android.net.Uri
import androidx.compose.material3.ColorScheme
import com.example.openfy.features.themes.model.ThemeColorsConfig
import com.example.openfy.features.themes.model.ThemeMetadata
import com.example.openfy.features.themes.model.toColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ThemeParser {

    const val THEME_CONFIG_FILE = "theme.json"
    const val MANIFEST_CONFIG_FILE = "manifest.json"
    const val PREVIEW_IMAGE_FILE = "preview.png"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    /**
     * Parses and extracts a `.thm` theme package from an [InputStream].
     * Unpacks `theme.json`, `manifest.json` and optional `preview.png` into [targetDir].
     */
    suspend fun parseAndExtractThm(
        inputStream: InputStream,
        targetDir: File
    ): Result<ThemeMetadata> = withContext(Dispatchers.IO) {
        try {
            val extractedFiles = mutableMapOf<String, ByteArray>()

            ZipInputStream(inputStream).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    val name = entry.name.substringAfterLast("/").substringAfterLast("\\")
                    if (!entry.isDirectory && name.isNotEmpty()) {
                        val buffer = ByteArrayOutputStream()
                        zip.copyTo(buffer)
                        extractedFiles[name.lowercase()] = buffer.toByteArray()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            val themeBytes = extractedFiles[THEME_CONFIG_FILE]
                ?: extractedFiles[MANIFEST_CONFIG_FILE]
                ?: return@withContext Result.failure(IllegalStateException("В архиве темы отсутствует '$THEME_CONFIG_FILE'"))

            val themeStr = themeBytes.decodeToString()

            // Try decoding ThemeMetadata or fallback to parsing from manifest
            val metadata: ThemeMetadata = try {
                json.decodeFromString<ThemeMetadata>(themeStr)
            } catch (_: Exception) {
                val manifestBytes = extractedFiles[MANIFEST_CONFIG_FILE]
                if (manifestBytes != null) {
                    json.decodeFromString<ThemeMetadata>(manifestBytes.decodeToString())
                } else {
                    return@withContext Result.failure(IllegalArgumentException("Не удалось извлечь метаданные темы из theme.json / manifest.json"))
                }
            }

            if (metadata.id.isBlank() || metadata.name.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Поля 'id' и 'name' темы не могут быть пустыми"))
            }

            // Path Traversal Sanity Check
            val sanitizedId = metadata.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")

            // Validate ThemeColorsConfig
            try {
                json.decodeFromString<ThemeColorsConfig>(themeStr)
            } catch (e: Exception) {
                return@withContext Result.failure(IllegalArgumentException("Ошибка в цветовой палитре темы: ${e.localizedMessage}"))
            }

            val destinationFolder = File(targetDir, sanitizedId)
            if (destinationFolder.exists()) {
                destinationFolder.deleteRecursively()
            }
            destinationFolder.mkdirs()

            // Save JSON config
            File(destinationFolder, THEME_CONFIG_FILE).writeText(themeStr)
            File(destinationFolder, MANIFEST_CONFIG_FILE).writeText(
                json.encodeToString(ThemeMetadata.serializer(), metadata.copy(id = sanitizedId))
            )

            // Save preview image if exists
            val previewBytes = extractedFiles[PREVIEW_IMAGE_FILE]
            if (previewBytes != null) {
                File(destinationFolder, PREVIEW_IMAGE_FILE).writeBytes(previewBytes)
            }

            Result.success(metadata.copy(id = sanitizedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Converts a raw JSON string into a Compose [ColorScheme].
     */
    fun parseColorSchemeFromJson(themeJson: String, isDark: Boolean = true): ColorScheme? {
        return try {
            val colorsConfig = json.decodeFromString<ThemeColorsConfig>(themeJson)
            colorsConfig.toColorScheme(isDark = isDark)
        } catch (_: Exception) {
            null
        }
    }
}
