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

package com.example.openfy.core.audio.service

import android.content.Context
import android.os.Environment
import com.example.openfy.core.audio.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Offline Download & Cache Manager.
 *
 * Provides 100% free unlimited offline listening for streaming and online tracks,
 * a capability traditionally locked behind paid subscriptions (Spotify Premium, Apple Music).
 */
object OfflineDownloadManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var appContext: Context? = null

    private val _downloadedSongIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedSongIds: StateFlow<Set<String>> = _downloadedSongIds.asStateFlow()

    private val _activeDownloads = MutableStateFlow<Map<String, Float>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, Float>> = _activeDownloads.asStateFlow()

    private val _isOfflineOnlyMode = MutableStateFlow(false)
    val isOfflineOnlyMode: StateFlow<Boolean> = _isOfflineOnlyMode.asStateFlow()

    private const val PREFS_NAME = "openfy_offline_downloads"
    private const val KEY_OFFLINE_ONLY = "offline_only_mode"

    fun init(context: Context) {
        val app = context.applicationContext
        appContext = app
        val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isOfflineOnlyMode.value = prefs.getBoolean(KEY_OFFLINE_ONLY, false)
        refreshDownloadedList()
    }

    fun setOfflineOnlyMode(enabled: Boolean) {
        _isOfflineOnlyMode.value = enabled
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean(KEY_OFFLINE_ONLY, enabled)
            ?.apply()
    }

    fun toggleOfflineOnlyMode() {
        setOfflineOnlyMode(!_isOfflineOnlyMode.value)
    }

    fun isDownloaded(songId: String): Boolean {
        return _downloadedSongIds.value.contains(songId)
    }

    fun getDownloadedFile(songId: String): File? {
        val dir = getOfflineDir() ?: return null
        val file = File(dir, "$songId.mp3")
        return if (file.exists() && file.length() > 0) file else null
    }

    private fun getOfflineDir(): File? {
        val ctx = appContext ?: return null
        val dir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "OpenFy_Offline")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun refreshDownloadedList() {
        val dir = getOfflineDir() ?: return
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".mp3") && f.length() > 0 } ?: emptyArray()
        val ids = files.map { it.name.removeSuffix(".mp3") }.toSet()
        _downloadedSongIds.value = ids
    }

    fun downloadSong(
        song: Song,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val urlStr = song.path
        if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
            // Already a local file
            onComplete?.invoke(true)
            return
        }

        val songIdStr = song.id.toString()
        if (isDownloaded(songIdStr)) {
            onComplete?.invoke(true)
            return
        }

        val dir = getOfflineDir()
        if (dir == null) {
            onComplete?.invoke(false)
            return
        }

        scope.launch {
            try {
                _activeDownloads.value = _activeDownloads.value + (songIdStr to 0.05f)
                val destFile = File(dir, "$songIdStr.mp3")
                val tempFile = File(dir, "$songIdStr.tmp")

                val url = URL(urlStr)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 30000
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                }

                val totalBytes = connection.contentLengthLong
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (totalBytes > 0) {
                                val progress = (totalRead.toFloat() / totalBytes).coerceIn(0f, 1f)
                                _activeDownloads.value = _activeDownloads.value + (songIdStr to progress)
                            }
                        }
                    }
                }

                tempFile.renameTo(destFile)
                refreshDownloadedList()
                _activeDownloads.value = _activeDownloads.value - songIdStr
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _activeDownloads.value = _activeDownloads.value - songIdStr
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            }
        }
    }

    fun deleteDownloadedSong(songId: String) {
        val dir = getOfflineDir() ?: return
        val file = File(dir, "$songId.mp3")
        if (file.exists()) {
            file.delete()
        }
        refreshDownloadedList()
    }

    fun clearAllOfflineDownloads() {
        val dir = getOfflineDir() ?: return
        dir.listFiles()?.forEach { it.delete() }
        refreshDownloadedList()
    }

    fun getTotalOfflineStorageBytes(): Long {
        val dir = getOfflineDir() ?: return 0L
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
