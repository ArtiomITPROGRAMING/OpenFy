package com.example.openfy.core.audio.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.openfy.core.audio.model.Album
import com.example.openfy.core.audio.model.Artist
import com.example.openfy.core.audio.model.FolderItem
import com.example.openfy.core.audio.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AudioScanner(private val context: Context) {

    private val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanLocalAudio(ignoreShortTracks: Boolean = true): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val contentResolver: ContentResolver = context.contentResolver

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )

        val selection = if (ignoreShortTracks) {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        } else {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        }
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Title"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdCol)
                    val duration = cursor.getLong(durationCol)
                    val path = cursor.getString(dataCol) ?: ""
                    val year = cursor.getInt(yearCol)
                    val trackNumber = cursor.getInt(trackCol)
                    val dateAdded = cursor.getLong(dateAddedCol)
                    val sizeBytes = cursor.getLong(sizeCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val albumArtUri = ContentUris.withAppendedId(albumArtBaseUri, albumId).toString()

                    val cleanArtist = if (artist.trim().equals("<unknown>", ignoreCase = true)) "Unknown Artist" else artist
                    val cleanAlbum = if (album.trim().equals("<unknown>", ignoreCase = true)) "Unknown Album" else album

                    songList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = cleanArtist,
                            album = cleanAlbum,
                            albumId = albumId,
                            durationMs = duration,
                            path = path,
                            contentUriString = contentUri,
                            albumArtUriString = albumArtUri,
                            trackNumber = trackNumber,
                            year = year,
                            dateAdded = dateAdded,
                            sizeBytes = sizeBytes
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songList
    }

    fun groupSongsByAlbum(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId }.map { (albumId, albumSongs) ->
            val first = albumSongs.first()
            Album(
                id = albumId,
                title = first.album,
                artist = first.artist,
                songCount = albumSongs.size,
                year = albumSongs.maxOfOrNull { it.year } ?: 0,
                albumArtUriString = first.albumArtUriString
            )
        }.sortedBy { it.title.lowercase() }
    }

    fun groupSongsByArtist(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist.lowercase() }.map { (_, artistSongs) ->
            val first = artistSongs.first()
            val albums = artistSongs.map { it.albumId }.distinct().size
            Artist(
                id = first.id,
                name = first.artist,
                songCount = artistSongs.size,
                albumCount = albums
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun groupSongsByFolder(songs: List<Song>): List<FolderItem> {
        return songs.groupBy { song ->
            val file = File(song.path)
            file.parent ?: "Root"
        }.map { (folderPath, folderSongs) ->
            val folderName = File(folderPath).name.ifEmpty { "Storage" }
            FolderItem(
                path = folderPath,
                name = folderName,
                songCount = folderSongs.size,
                songs = folderSongs.sortedBy { it.title.lowercase() }
            )
        }.sortedBy { it.name.lowercase() }
    }
}
