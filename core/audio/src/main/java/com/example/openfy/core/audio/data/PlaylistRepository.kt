package com.example.openfy.core.audio.data

import android.content.Context
import android.content.SharedPreferences
import com.example.openfy.core.audio.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PlaylistRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("openfy_playlists", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _favorites = MutableStateFlow<Set<Long>>(emptySet())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Long>>(emptyList())
    val recentlyPlayed: StateFlow<List<Long>> = _recentlyPlayed.asStateFlow()

    private val _playCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val playCounts: StateFlow<Map<Long, Int>> = _playCounts.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        try {
            val favJson = prefs.getString(KEY_FAVORITES, null)
            if (favJson != null) {
                val type = object : TypeToken<Set<Long>>() {}.type
                _favorites.value = gson.fromJson(favJson, type) ?: emptySet()
            }
        } catch (_: Exception) {}

        try {
            val plJson = prefs.getString(KEY_PLAYLISTS, null)
            if (plJson != null) {
                val type = object : TypeToken<List<Playlist>>() {}.type
                _playlists.value = gson.fromJson(plJson, type) ?: emptyList()
            }
        } catch (_: Exception) {}

        try {
            val recJson = prefs.getString(KEY_RECENTLY_PLAYED, null)
            if (recJson != null) {
                val type = object : TypeToken<List<Long>>() {}.type
                _recentlyPlayed.value = gson.fromJson(recJson, type) ?: emptyList()
            }
        } catch (_: Exception) {}

        try {
            val pcJson = prefs.getString(KEY_PLAY_COUNTS, null)
            if (pcJson != null) {
                val type = object : TypeToken<Map<Long, Int>>() {}.type
                _playCounts.value = gson.fromJson(pcJson, type) ?: emptyMap()
            }
        } catch (_: Exception) {}
    }

    fun toggleFavorite(songId: Long) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        _favorites.value = current
        saveFavoritesAsync(current)
    }

    fun isFavorite(songId: Long): Boolean {
        return _favorites.value.contains(songId)
    }

    fun createPlaylist(name: String, initialSongIds: List<Long> = emptyList()): Playlist {
        val newPl = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            songIds = initialSongIds,
            createdAt = System.currentTimeMillis()
        )
        val list = _playlists.value.toMutableList()
        list.add(newPl)
        _playlists.value = list
        savePlaylists()
        return newPl
    }

    fun deletePlaylist(playlistId: String) {
        val list = _playlists.value.filter { it.id != playlistId }
        _playlists.value = list
        savePlaylists()
    }

    fun addSongToPlaylist(playlistId: String, songId: Long) {
        val list = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                if (!pl.songIds.contains(songId)) {
                    pl.copy(songIds = pl.songIds + songId)
                } else pl
            } else pl
        }
        _playlists.value = list
        savePlaylists()
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        val list = _playlists.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(songIds = pl.songIds.filter { it != songId })
            } else pl
        }
        _playlists.value = list
        savePlaylists()
    }

    fun recordSongPlayed(songId: Long) {
        val currentRec = _recentlyPlayed.value.toMutableList()
        currentRec.remove(songId)
        currentRec.add(0, songId)
        if (currentRec.size > 100) {
            currentRec.removeAt(currentRec.size - 1)
        }
        _recentlyPlayed.value = currentRec
        saveRecentlyPlayedAsync(currentRec)

        val counts = _playCounts.value.toMutableMap()
        val prev = counts[songId] ?: 0
        counts[songId] = prev + 1
        _playCounts.value = counts
        savePlayCountsAsync(counts)
    }

    fun onSongDeleted(songId: Long) {
        if (_favorites.value.contains(songId)) {
            toggleFavorite(songId)
        }
        _playlists.value.forEach { pl ->
            if (pl.songIds.contains(songId)) {
                removeSongFromPlaylist(pl.id, songId)
            }
        }
        val rec = _recentlyPlayed.value.filter { it != songId }
        _recentlyPlayed.value = rec
        saveRecentlyPlayedAsync(rec)

        val pc = _playCounts.value.toMutableMap()
        pc.remove(songId)
        _playCounts.value = pc
        savePlayCountsAsync(pc)
    }

    private fun savePlaylists() {
        savePlaylistsAsync(_playlists.value)
    }

    private fun saveFavoritesAsync(favs: Set<Long>) {
        ioScope.launch {
            try {
                prefs.edit().putString(KEY_FAVORITES, gson.toJson(favs)).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun savePlaylistsAsync(pls: List<Playlist>) {
        ioScope.launch {
            try {
                prefs.edit().putString(KEY_PLAYLISTS, gson.toJson(pls)).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveRecentlyPlayedAsync(rec: List<Long>) {
        ioScope.launch {
            try {
                prefs.edit().putString(KEY_RECENTLY_PLAYED, gson.toJson(rec)).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun savePlayCountsAsync(counts: Map<Long, Int>) {
        ioScope.launch {
            try {
                prefs.edit().putString(KEY_PLAY_COUNTS, gson.toJson(counts)).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val KEY_FAVORITES = "fav_song_ids"
        private const val KEY_PLAYLISTS = "playlists_data"
        private const val KEY_RECENTLY_PLAYED = "recently_played_ids"
        private const val KEY_PLAY_COUNTS = "play_counts_map"
    }
}
