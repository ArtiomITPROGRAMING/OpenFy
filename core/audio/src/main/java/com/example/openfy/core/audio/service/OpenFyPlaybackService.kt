package com.example.openfy.core.audio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionError
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
import com.example.openfy.core.audio.data.AudioScanner
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.core.audio.model.Song
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class OpenFyPlaybackService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private var exoPlayer: ExoPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentAlbumArtBitmap: Bitmap? = null

    private var audioNoisyReceiver: AudioNoisyReceiver? = null
    private var isAudioNoisyReceiverRegistered = false

    private lateinit var audioScanner: AudioScanner

    private val librarySessionCallback = object : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(MEDIA_ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("OpenFy Auto Library")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val playbackManager = PlaybackManager.getInstance(applicationContext)

            return when (parentId) {
                MEDIA_ROOT_ID -> {
                    val rootCategories = listOf(
                        createFolderItem(MEDIA_ALL_TRACKS, "Все треки", "Локальная музыка", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED),
                        createFolderItem(MEDIA_FAVORITES, "Избранное", "Любимые композиции", MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS),
                        createFolderItem(MEDIA_RECENT, "Недавние", "История прослушивания", MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS),
                        createFolderItem(MEDIA_ALBUMS, "Альбомы", "Коллекции по альбомам", MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
                        createFolderItem(MEDIA_PLAYLISTS, "Плейлисты", "Пользовательские подборки", MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                    )
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(rootCategories), params))
                }
                MEDIA_ALL_TRACKS -> {
                    val currentSongs = playbackManager.queue.value
                    val items = currentSongs.map { playbackManager.createMediaItem(it) }
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(items), params))
                }
                MEDIA_FAVORITES -> {
                    val favIds = playbackManager.playlistRepository.favorites.value
                    val allSongs = playbackManager.queue.value
                    val favSongs = allSongs.filter { favIds.contains(it.id) }
                    val items = favSongs.map { playbackManager.createMediaItem(it) }
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(items), params))
                }
                MEDIA_RECENT -> {
                    val recentIds = playbackManager.playlistRepository.recentlyPlayed.value
                    val allSongs = playbackManager.queue.value
                    val recentSongs = allSongs.filter { recentIds.contains(it.id) }
                    val items = recentSongs.map { playbackManager.createMediaItem(it) }
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(items), params))
                }
                MEDIA_PLAYLISTS -> {
                    val playlists = playbackManager.playlistRepository.playlists.value
                    val items = playlists.map { pl ->
                        createFolderItem("playlist_${pl.id}", pl.name, "${pl.songIds.size} треков", MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                    }
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(items), params))
                }
                else -> {
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of(), params))
                }
            }
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val playbackManager = PlaybackManager.getInstance(applicationContext)
            val currentSong = playbackManager.currentSong.value
            return if (currentSong != null && currentSong.id.toString() == mediaId) {
                Futures.immediateFuture(LibraryResult.ofItem(playbackManager.createMediaItem(currentSong), null))
            } else {
                Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
            }
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return Futures.immediateFuture(mediaItems)
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
            )
        }
    }

    private fun createFolderItem(id: String, title: String, subtitle: String, mediaType: Int): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(mediaType)
                    .build()
            )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioScanner = AudioScanner(applicationContext)

        val settingsRepo = PlaybackManager.getInstance(applicationContext).settingsRepository
        audioNoisyReceiver = AudioNoisyReceiver {
            if (settingsRepo.pauseOnUnplug.value) {
                exoPlayer?.pause()
            }
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val mediaSourceFactory = PlayerCacheProvider.getMediaSourceFactory(this)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(false)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exoPlayer = player

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val librarySession = MediaLibrarySession.Builder(this, player, librarySessionCallback)
            .setSessionActivity(pendingIntent)
            .build()

        mediaLibrarySession = librarySession

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    registerAudioNoisyReceiver()
                } else {
                    unregisterAudioNoisyReceiver()
                }
                updateSystemNotification()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                loadArtworkAndUpdateNotification(mediaItem)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateSystemNotification()
            }
        })

        PlaybackManager.getInstance(applicationContext).attachPlayer(player)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> exoPlayer?.play()
            ACTION_PAUSE -> exoPlayer?.pause()
            ACTION_NEXT -> PlaybackManager.getInstance(applicationContext).skipNext()
            ACTION_PREV -> PlaybackManager.getInstance(applicationContext).skipPrev()
        }
        updateSystemNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun loadArtworkAndUpdateNotification(mediaItem: MediaItem?) {
        val artUri = mediaItem?.mediaMetadata?.artworkUri
        if (artUri != null) {
            serviceScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        contentResolver.openInputStream(artUri)?.use { inputStream ->
                            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
                            val sampleSize = calculateInSampleSize(boundsOptions, 512, 512)
                            contentResolver.openInputStream(artUri)?.use { realStream ->
                                val decodeOptions = BitmapFactory.Options().apply {
                                    inSampleSize = sampleSize
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        inPreferredConfig = Bitmap.Config.HARDWARE
                                    }
                                }
                                BitmapFactory.decodeStream(realStream, null, decodeOptions)
                            }
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                currentAlbumArtBitmap = bitmap
                updateSystemNotification()
            }
        } else {
            currentAlbumArtBitmap = null
            updateSystemNotification()
        }
    }

    private fun updateSystemNotification() {
        val session = mediaLibrarySession ?: return
        val player = exoPlayer ?: return
        val currentItem = player.currentMediaItem ?: return

        val title = currentItem.mediaMetadata.title?.toString() ?: "OpenFy Player"
        val artist = currentItem.mediaMetadata.artist?.toString() ?: "Неизвестный исполнитель"
        val album = currentItem.mediaMetadata.albumTitle?.toString() ?: ""

        val isPlaying = player.isPlaying

        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, OpenFyPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, OpenFyPlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, OpenFyPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(session)
            .setShowActionsInCompactView(0, 1, 2)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(if (album.isNotBlank()) "$artist • $album" else artist)
            .setSubText(album)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .setStyle(mediaStyle)
            .addAction(
                android.R.drawable.ic_media_previous,
                "Назад",
                prevPendingIntent
            )
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Пауза" else "Играть",
                playPausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Вперед",
                nextPendingIntent
            )

        currentAlbumArtBitmap?.let {
            notificationBuilder.setLargeIcon(it)
        }

        val notification = notificationBuilder.build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OpenFy Воспроизведение",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Управление воспроизведением OpenFy для Google Pixel, One UI, HyperOS, Android Auto"
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun registerAudioNoisyReceiver() {
        if (!isAudioNoisyReceiverRegistered && audioNoisyReceiver != null) {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            ContextCompat.registerReceiver(this, audioNoisyReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
            isAudioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (isAudioNoisyReceiverRegistered && audioNoisyReceiver != null) {
            try {
                unregisterReceiver(audioNoisyReceiver)
            } catch (ignored: Exception) {
            }
            isAudioNoisyReceiverRegistered = false
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        unregisterAudioNoisyReceiver()
        audioNoisyReceiver = null
        try {
            PlaybackManager.getInstance(applicationContext).equalizerController.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        exoPlayer = null
        currentAlbumArtBitmap = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "openfy_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val MEDIA_ROOT_ID = "openfy_root"
        const val MEDIA_ALL_TRACKS = "category_tracks"
        const val MEDIA_FAVORITES = "category_favorites"
        const val MEDIA_RECENT = "category_recent"
        const val MEDIA_ALBUMS = "category_albums"
        const val MEDIA_PLAYLISTS = "category_playlists"

        const val ACTION_PLAY = "com.example.openfy.action.PLAY"
        const val ACTION_PAUSE = "com.example.openfy.action.PAUSE"
        const val ACTION_NEXT = "com.example.openfy.action.NEXT"
        const val ACTION_PREV = "com.example.openfy.action.PREV"
    }
}
