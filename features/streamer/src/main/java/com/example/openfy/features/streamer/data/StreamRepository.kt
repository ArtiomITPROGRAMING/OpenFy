package com.example.openfy.features.streamer.data

import android.content.Context
import com.example.openfy.core.audio.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object StreamRepository {

    private const val PREFS_NAME = "openfy_stream_repository_prefs"
    private const val KEY_FAVORITE_STREAMS = "key_favorite_streams_json"
    private const val KEY_RECENT_STREAMS = "key_recent_streams_json"
    private const val MAX_RECENTS = 30

    fun getCatalogTracks(genre: String? = null): List<Song> {
        return OpenSourceMusicCatalog.getTracksByGenre(genre)
    }

    suspend fun searchAll(query: String): List<Song> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isBlank()) {
            return@withContext OpenSourceMusicCatalog.getTracksByGenre(null)
        }
        OpenSourceMusicCatalog.searchTracks(clean).distinctBy { it.streamUrl ?: it.path }
    }

    fun getFavoriteStreams(context: Context): List<Song> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_FAVORITE_STREAMS, null) ?: return emptyList()
        return parseSongListFromJson(jsonString)
    }

    fun isFavoriteStream(context: Context, streamUrl: String?): Boolean {
        if (streamUrl.isNullOrBlank()) return false
        val favorites = getFavoriteStreams(context)
        return favorites.any { it.streamUrl == streamUrl || it.contentUriString == streamUrl }
    }

    fun toggleFavoriteStream(context: Context, song: Song): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentFavorites = getFavoriteStreams(context).toMutableList()
        val url = song.streamUrl ?: song.contentUriString
        val index = currentFavorites.indexOfFirst { (it.streamUrl ?: it.contentUriString) == url }

        val isNowFavorite = if (index != -1) {
            currentFavorites.removeAt(index)
            false
        } else {
            currentFavorites.add(0, song.copy(isFavorite = true))
            true
        }

        prefs.edit().putString(KEY_FAVORITE_STREAMS, serializeSongListToJson(currentFavorites)).apply()
        return isNowFavorite
    }

    fun recordRecentStream(context: Context, song: Song) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentRecents = getRecentStreams(context).toMutableList()
            val url = song.streamUrl ?: song.contentUriString
            currentRecents.removeAll { (it.streamUrl ?: it.contentUriString) == url }
            currentRecents.add(0, song)

            val trimmed = currentRecents.take(MAX_RECENTS)
            prefs.edit().putString(KEY_RECENT_STREAMS, serializeSongListToJson(trimmed)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRecentStreams(context: Context): List<Song> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_RECENT_STREAMS, null) ?: return emptyList()
        return parseSongListFromJson(jsonString)
    }

    private fun serializeSongListToJson(songs: List<Song>): String {
        val jsonArray = JSONArray()
        for (song in songs) {
            val obj = JSONObject().apply {
                put("id", song.id)
                put("title", song.title)
                put("artist", song.artist)
                put("album", song.album)
                put("durationMs", song.durationMs)
                put("path", song.path)
                put("contentUriString", song.contentUriString)
                put("albumArtUriString", song.albumArtUriString ?: "")
                put("isFavorite", song.isFavorite)
                put("isStream", song.isStream)
                put("streamUrl", song.streamUrl ?: "")
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun parseSongListFromJson(jsonString: String): List<Song> {
        return try {
            val jsonArray = JSONArray(jsonString)
            val result = mutableListOf<Song>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val url = obj.optString("streamUrl").ifBlank { obj.optString("contentUriString") }
                val song = Song.createStreamTrack(
                    url = url,
                    title = obj.optString("title", "Online Stream"),
                    artist = obj.optString("artist", "Web Artist"),
                    album = obj.optString("album", "OpenFy Streams"),
                    durationMs = obj.optLong("durationMs", 0L),
                    coverUrl = obj.optString("albumArtUriString").ifBlank { null }
                ).copy(
                    isFavorite = obj.optBoolean("isFavorite", false)
                )
                result.add(song)
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }
}
