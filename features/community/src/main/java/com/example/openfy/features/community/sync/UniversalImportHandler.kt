package com.example.openfy.features.community.sync

import android.content.Context
import android.net.Uri
import com.example.openfy.core.audio.data.PlaylistRepository
import com.example.openfy.features.themes.engine.ThemeEngine
import com.example.openfy.features.themes.engine.ThemeManager
import com.example.openfy.features.themes.engine.ThemeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream

sealed interface ImportResult {
    data class PlaylistImported(val playlistName: String, val trackCount: Int) : ImportResult
    data class ThemeImported(val themeName: String, val themeId: String) : ImportResult
    data class TrackMetaImported(val title: String, val artist: String) : ImportResult
}

@Serializable
private data class SharedPlaylistData(
    val name: String,
    val songIds: List<Long> = emptyList(),
    val songTitles: List<String> = emptyList()
)

object UniversalImportHandler {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Universal import entry point for raw strings, QR code contents, Deep Links and URIs.
     */
    suspend fun handleImport(
        context: Context,
        rawInput: String,
        playlistRepository: PlaylistRepository,
        themeManager: ThemeManager? = null
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val cleanInput = rawInput.trim()

            // 1. Try if input is a URI to a local file
            if (cleanInput.startsWith("content://") || cleanInput.startsWith("file://")) {
                val uri = Uri.parse(cleanInput)
                val stream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(IllegalArgumentException("Не удалось открыть URI: $cleanInput"))

                val fileBytes = stream.use { it.readBytes() }
                val fileContent = fileBytes.decodeToString()

                // Check if it is a .thm zip archive directly
                if (fileBytes.size >= 4 && fileBytes[0] == 0x50.toByte() && fileBytes[1] == 0x4B.toByte()) {
                    val themeResult = ThemeParser.parseAndExtractThm(
                        ByteArrayInputStream(fileBytes),
                        ThemeEngine.getThemesDirectory(context)
                    )
                    return@withContext themeResult.map { metadata ->
                        themeManager?.refreshInstalledThemes()
                        ImportResult.ThemeImported(metadata.name, metadata.id)
                    }
                }

                // Otherwise decode payload from file text
                return@withContext processPayloadString(context, fileContent, playlistRepository, themeManager)
            }

            // 2. Process payload string
            processPayloadString(context, cleanInput, playlistRepository, themeManager)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processPayloadString(
        context: Context,
        rawString: String,
        playlistRepository: PlaylistRepository,
        themeManager: ThemeManager?
    ): Result<ImportResult> {
        val payloadResult = SharePayload.fromCompressedString(rawString)
        if (payloadResult.isFailure) {
            return Result.failure(payloadResult.exceptionOrNull() ?: IllegalArgumentException("Неизвестный формат данных OpenFy"))
        }

        val payload = payloadResult.getOrThrow()

        return when (payload.type) {
            ShareType.PLAYLIST -> {
                try {
                    val plData = json.decodeFromString<SharedPlaylistData>(payload.jsonData)
                    val newPlaylist = playlistRepository.createPlaylist(
                        name = plData.name.ifBlank { payload.title },
                        initialSongIds = plData.songIds
                    )
                    Result.success(ImportResult.PlaylistImported(newPlaylist.name, newPlaylist.songIds.size))
                } catch (e: Exception) {
                    Result.failure(IllegalArgumentException("Ошибка импорта плейлиста: ${e.localizedMessage}"))
                }
            }

            ShareType.THEME -> {
                try {
                    // Try parsing as JSON theme directly
                    val sanitizedId = payload.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                    val targetDir = ThemeEngine.getThemesDirectory(context)
                    val themeFolder = java.io.File(targetDir, sanitizedId).apply { mkdirs() }

                    java.io.File(themeFolder, ThemeParser.THEME_CONFIG_FILE).writeText(payload.jsonData)
                    themeManager?.refreshInstalledThemes()

                    Result.success(ImportResult.ThemeImported(payload.title, sanitizedId))
                } catch (e: Exception) {
                    Result.failure(IllegalArgumentException("Ошибка импорта темы: ${e.localizedMessage}"))
                }
            }

            ShareType.TRACK_META -> {
                Result.success(ImportResult.TrackMetaImported(payload.title, payload.author))
            }
        }
    }
}
