package com.example.openfy.core.audio.data

import android.content.Context
import com.example.openfy.core.audio.model.Song
import java.io.File
import java.util.Locale

data class DuplicateGroup(
    val id: String,
    val baseTitle: String,
    val artist: String,
    val songs: List<Song>
) {
    val totalWastedBytes: Long
        get() = if (songs.size > 1) {
            val sizes = songs.map { song ->
                try {
                    File(song.path).length()
                } catch (e: Exception) {
                    0L
                }
            }
            val maxSize = sizes.maxOrNull() ?: 0L
            sizes.sum() - maxSize
        } else 0L
}

class DuplicateScanner(private val context: Context) {

    fun normalizeTitle(rawTitle: String): String {
        var clean = rawTitle.lowercase(Locale.ROOT)

        clean = clean.replace(Regex("\\[.*?\\]|\\(.*?\\)"), " ")

        val keywords = listOf(
            "ultra slowed", "super slowed", "slowed & reverb", "slowed and reverb",
            "slowed", "sped up", "speed up", "nightcore", "daycore", "bass boosted",
            "reverb", "remix", "edit", "instrumental", "acoustic", "live", "copy"
        )
        for (kw in keywords) {
            clean = clean.replace(kw, " ")
        }

        clean = clean.replace(Regex("[_\\-#0-9]+"), " ")
        clean = clean.replace(Regex("\\s+"), " ").trim()

        return if (clean.length >= 2) clean else rawTitle.trim().lowercase(Locale.ROOT)
    }

    fun findDuplicates(songs: List<Song>): List<DuplicateGroup> {
        val groups = mutableMapOf<String, MutableList<Song>>()

        for (song in songs) {
            val normTitle = normalizeTitle(song.title)
            val normArtist = song.artist.trim().lowercase(Locale.ROOT)
            val groupKey = "$normArtist::$normTitle"

            val list = groups.getOrPut(groupKey) { mutableListOf() }
            list.add(song)
        }

        return groups.filter { it.value.size > 1 }
            .map { (key, songList) ->
                val first = songList.first()
                DuplicateGroup(
                    id = key,
                    baseTitle = first.title,
                    artist = first.artist,
                    songs = songList.sortedByDescending { it.durationMs }
                )
            }
    }

    fun deleteSong(song: Song): Boolean {
        var success = false
        try {
            val deletedRows = context.contentResolver.delete(song.contentUri, null, null)
            if (deletedRows > 0) success = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!success && song.path.isNotBlank()) {
            try {
                val file = File(song.path)
                if (file.exists()) {
                    success = file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return success
    }
}
