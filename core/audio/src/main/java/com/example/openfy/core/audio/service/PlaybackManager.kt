package com.example.openfy.core.audio.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.openfy.core.audio.data.DuplicateScanner
import com.example.openfy.core.audio.data.PlaylistRepository
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.core.audio.model.RepeatMode
import com.example.openfy.core.audio.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class PlaybackManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())

    val playlistRepository = PlaylistRepository(context)
    val equalizerController = EqualizerController(context)
    val settingsRepository = SettingsRepository(context)
    private val duplicateScanner = DuplicateScanner(context)
    val zenNatureAudioEngine = ZenNatureAudioEngine()
    val smartSleepGuard = SmartSleepGuard(context, this, zenNatureAudioEngine)

    private var exoPlayer: ExoPlayer? = null
    val player: ExoPlayer?
        get() = exoPlayer

    private var originalQueue: List<Song> = emptyList()


    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow<Int?>(null)
    val sleepTimerSecondsLeft: StateFlow<Int?> = _sleepTimerSecondsLeft.asStateFlow()
    private var sleepTimerJob: Job? = null
    private var stopAfterCurrentTrack: Boolean = false

    private var pendingPlayAction: (() -> Unit)? = null

    private val positionUpdateRunnable = object : Runnable {
        override fun run() {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    _currentPositionMs.value = pos
                    if (dur > 0 && _durationMs.value != dur) {
                        _durationMs.value = dur
                    }

                    // Smooth fade out in the last 4 seconds if stopping after current track
                    if (stopAfterCurrentTrack && dur > 0) {
                        val remainingMs = dur - pos
                        if (remainingMs in 1..4000L) {
                            player.volume = (remainingMs.toFloat() / 4000f).coerceIn(0f, 1f)
                        }
                    }
                }
            }
            handler.postDelayed(this, 200)
        }
    }

    init {
        handler.post(positionUpdateRunnable)
        _isShuffle.value = settingsRepository.isShuffle.value
        _repeatMode.value = settingsRepository.repeatMode.value
        _speed.value = settingsRepository.playbackSpeed.value
        smartSleepGuard.start()
    }

    @OptIn(UnstableApi::class)
    fun attachPlayer(player: ExoPlayer) {
        exoPlayer = player
        player.addListener(playerListener)

        player.repeatMode = when (_repeatMode.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        player.playbackParameters = PlaybackParameters(_speed.value, _pitch.value)

        val audioSessionId = player.audioSessionId
        if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId > 0) {
            equalizerController.bindAudioSession(audioSessionId)
        }

        pendingPlayAction?.invoke()
        pendingPlayAction = null
    }

    @OptIn(UnstableApi::class)
    fun rebindEqualizer() {
        val player = exoPlayer ?: return
        val sessionId = player.audioSessionId
        if (sessionId != C.AUDIO_SESSION_ID_UNSET && sessionId > 0) {
            equalizerController.bindAudioSession(sessionId)
        }
    }

    /**
     * Retrieves the attached ExoPlayer or creates one pre-configured with [PlayerCacheProvider] caching.
     */
    @OptIn(UnstableApi::class)
    fun getOrCreatePlayer(): ExoPlayer {
        exoPlayer?.let { return it }
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val mediaSourceFactory = PlayerCacheProvider.getMediaSourceFactory(context)
        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        attachPlayer(player)
        return player
    }

    /**
     * Clears all cached network audio streams from disk.
     */
    fun clearAudioCache() {
        PlayerCacheProvider.clearAudioCache(context)
    }

    /**
     * Returns total disk space occupied by cached audio streams in bytes.
     */
    fun getAudioCacheSizeBytes(): Long {
        return PlayerCacheProvider.getCacheSizeBytes(context)
    }

    private fun ensureServiceStarted() {
        try {
            val serviceIntent = Intent(context, OpenFyPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSongFromList(songs: List<Song>, targetSong: Song) {
        val targetIndex = songs.indexOfFirst { it.id == targetSong.id }.coerceAtLeast(0)
        playSongs(songs, targetIndex)
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        ensureServiceStarted()

        val safeIndex = startIndex.coerceIn(0, songs.size - 1)
        originalQueue = songs

        val finalQueue = if (_isShuffle.value) {
            val selectedSong = songs[safeIndex]
            val otherSongs = songs.filter { it.id != selectedSong.id }
            listOf(selectedSong) + smartShuffle(otherSongs)
        } else {
            songs
        }

        val finalIndex = if (_isShuffle.value) 0 else safeIndex
        _queue.value = finalQueue
        _currentIndex.value = finalIndex

        val currentSelected = finalQueue[finalIndex]
        _currentSong.value = currentSelected
        _durationMs.value = currentSelected.durationMs
        _currentPositionMs.value = 0L

        playlistRepository.recordSongPlayed(currentSelected.id)

        val player = exoPlayer
        if (player == null) {
            pendingPlayAction = {
                executePlayInPlayer(finalQueue, finalIndex, 0L)
            }
        } else {
            executePlayInPlayer(finalQueue, finalIndex, 0L)
        }
    }

    fun playStreamTrack(streamSong: Song) {
        ensureServiceStarted()
        val currentList = _queue.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == streamSong.id || (it.isStream && it.streamUrl == streamSong.streamUrl) }
        val targetIndex = if (existingIndex != -1) {
            currentList[existingIndex] = streamSong
            existingIndex
        } else {
            val insertPos = if (currentList.isEmpty()) 0 else (_currentIndex.value + 1).coerceAtMost(currentList.size)
            currentList.add(insertPos, streamSong)
            insertPos
        }
        originalQueue = currentList
        _queue.value = currentList
        _currentIndex.value = targetIndex
        _currentSong.value = streamSong
        _durationMs.value = streamSong.durationMs
        _currentPositionMs.value = 0L

        val player = exoPlayer
        if (player == null) {
            pendingPlayAction = {
                executePlayInPlayer(currentList, targetIndex, 0L)
            }
        } else {
            executePlayInPlayer(currentList, targetIndex, 0L)
        }
    }

    fun createMediaItem(song: Song): MediaItem {
        val metaBuilder = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)

        try {
            song.albumArtUri?.let { uri ->
                val scheme = uri.scheme?.lowercase(Locale.ROOT)
                if (scheme == "content" || scheme == "file" || scheme == "android.resource") {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bytes = stream.readBytes()
                        if (bytes.isNotEmpty()) {
                            metaBuilder.setArtworkData(bytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore if art is unavailable
        }

        val meta = metaBuilder.build()

        return MediaItem.Builder()
            .setUri(song.contentUri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(meta)
            .build()
    }

    fun onSongDeleted(songId: Long) {
        val currentList = _queue.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == songId }
        if (index != -1) {
            val wasPlaying = _currentSong.value?.id == songId
            currentList.removeAt(index)
            originalQueue = originalQueue.filter { it.id != songId }
            _queue.value = currentList

            val player = exoPlayer
            if (player != null && index < player.mediaItemCount) {
                try {
                    player.removeMediaItem(index)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (wasPlaying) {
                if (currentList.isNotEmpty()) {
                    val nextIdx = index.coerceIn(0, currentList.size - 1)
                    playTrackAtIndex(nextIdx)
                } else {
                    pause()
                    _currentSong.value = null
                    _durationMs.value = 0L
                    _currentPositionMs.value = 0L
                }
            }
        }
        playlistRepository.onSongDeleted(songId)
    }

    @OptIn(UnstableApi::class)
    private fun executePlayInPlayer(songs: List<Song>, targetIndex: Int, startPositionMs: Long) {
        val player = exoPlayer ?: return
        val mediaItems = songs.map { createMediaItem(it) }

        player.setMediaItems(mediaItems, targetIndex, startPositionMs)
        player.playbackParameters = PlaybackParameters(_speed.value, _pitch.value)
        player.prepare()
        val sid = player.audioSessionId
        if (sid != C.AUDIO_SESSION_ID_UNSET && sid > 0) {
            equalizerController.bindAudioSession(sid)
        }
        player.play()
    }

    private var fadeJob: Job? = null

    fun playWithFade(durationMs: Long = 350L) {
        val player = exoPlayer ?: return
        fadeJob?.cancel()
        if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
            player.prepare()
        }
        player.volume = 0f
        player.play()
        fadeJob = scope.launch {
            val steps = 15
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            for (i in 1..steps) {
                if (!isActive) break
                delay(stepDelay)
                player.volume = (i.toFloat() / steps).coerceIn(0f, 1f)
            }
            player.volume = 1f
        }
    }

    fun pauseWithFade(durationMs: Long = 350L, onFinished: (() -> Unit)? = null) {
        val player = exoPlayer ?: return
        fadeJob?.cancel()
        val startVol = player.volume
        fadeJob = scope.launch {
            val steps = 15
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            for (i in (steps - 1) downTo 0) {
                if (!isActive) break
                delay(stepDelay)
                player.volume = (startVol * (i.toFloat() / steps)).coerceIn(0f, 1f)
            }
            player.pause()
            player.volume = 1f
            onFinished?.invoke()
        }
    }

    fun playPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            if (settingsRepository.fadeOnPlayPause.value) {
                pauseWithFade()
            } else {
                player.pause()
            }
        } else {
            if (settingsRepository.fadeOnPlayPause.value) {
                playWithFade()
            } else {
                if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
                    player.prepare()
                }
                player.play()
            }
        }
    }

    fun play() {
        if (settingsRepository.fadeOnPlayPause.value) {
            playWithFade()
        } else {
            exoPlayer?.let {
                if (it.playbackState == Player.STATE_IDLE || it.playbackState == Player.STATE_ENDED) {
                    it.prepare()
                }
                it.play()
            }
        }
    }

    fun pause() {
        if (settingsRepository.fadeOnPlayPause.value) {
            pauseWithFade()
        } else {
            exoPlayer?.pause()
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        exoPlayer?.seekTo(positionMs)
    }

    fun skipNext() {
        val currentList = _queue.value
        if (currentList.isEmpty()) return

        val nextIndex = _currentIndex.value + 1
        if (nextIndex in currentList.indices) {
            playTrackAtIndex(nextIndex)
        } else if (_repeatMode.value == RepeatMode.ALL) {
            playTrackAtIndex(0)
        }
    }

    fun skipPrev() {
        val currentList = _queue.value
        if (currentList.isEmpty()) return

        val player = exoPlayer
        if (player != null && player.currentPosition > 3000L) {
            player.seekTo(0L)
            _currentPositionMs.value = 0L
            return
        }

        val prevIndex = _currentIndex.value - 1
        if (prevIndex in currentList.indices) {
            playTrackAtIndex(prevIndex)
        } else if (_repeatMode.value == RepeatMode.ALL) {
            playTrackAtIndex(currentList.size - 1)
        } else {
            player?.seekTo(0L)
        }
    }

    fun playTrackAtIndex(index: Int) {
        val currentList = _queue.value
        if (index !in currentList.indices) return

        _currentIndex.value = index
        val targetSong = currentList[index]
        _currentSong.value = targetSong
        _durationMs.value = targetSong.durationMs
        _currentPositionMs.value = 0L

        playlistRepository.recordSongPlayed(targetSong.id)

        val player = exoPlayer ?: return
        if (player.mediaItemCount == currentList.size) {
            player.seekToDefaultPosition(index)
            player.play()
        } else {
            executePlayInPlayer(currentList, index, 0L)
        }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = nextMode
        settingsRepository.setRepeatMode(nextMode)

        exoPlayer?.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffle.value
        _isShuffle.value = newShuffle
        settingsRepository.setIsShuffle(newShuffle)

        val currentTrack = _currentSong.value ?: return
        val currentOrig = originalQueue

        if (newShuffle) {
            val activeOrig = if (currentOrig.isEmpty()) _queue.value else currentOrig
            if (activeOrig.isNotEmpty()) {
                val others = activeOrig.filter { it.id != currentTrack.id }
                val shuffledOthers = smartShuffle(others)
                val newQueue = listOf(currentTrack) + shuffledOthers

                originalQueue = activeOrig
                _queue.value = newQueue
                _currentIndex.value = 0

                seamlesslyUpdateUpcomingItems(newQueue, 0)
            }
        } else {
            if (currentOrig.isNotEmpty()) {
                val restoredIndex = currentOrig.indexOfFirst { it.id == currentTrack.id }.coerceAtLeast(0)
                originalQueue = currentOrig
                _queue.value = currentOrig
                _currentIndex.value = restoredIndex

                seamlesslyUpdateUpcomingItems(currentOrig, restoredIndex)
            }
        }
    }

    private fun seamlesslyUpdateUpcomingItems(newSongs: List<Song>, activeIndex: Int) {
        val player = exoPlayer ?: return
        try {
            if (player.mediaItemCount > 0 && activeIndex in newSongs.indices) {
                val currentMediaIndex = player.currentMediaItemIndex

                if (player.mediaItemCount > currentMediaIndex + 1) {
                    player.removeMediaItems(currentMediaIndex + 1, player.mediaItemCount)
                }

                if (currentMediaIndex > 0) {
                    player.removeMediaItems(0, currentMediaIndex)
                }

                val upcomingSongs = newSongs.subList((activeIndex + 1).coerceAtMost(newSongs.size), newSongs.size)
                if (upcomingSongs.isNotEmpty()) {
                    val upcomingMediaItems = upcomingSongs.map { createMediaItem(it) }
                    player.addMediaItems(upcomingMediaItems)
                }
            } else {
                executePlayInPlayer(newSongs, activeIndex, player.currentPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun smartShuffle(songs: List<Song>): List<Song> {
        if (songs.size <= 2) return songs.shuffled()

        val randomized = songs.shuffled().toMutableList()
        val result = mutableListOf<Song>()

        while (randomized.isNotEmpty()) {
            val lastSong = result.lastOrNull()
            val candidateIndex = if (lastSong != null) {
                val lastNormTitle = duplicateScanner.normalizeTitle(lastSong.title)
                val lastArtist = lastSong.artist.trim().lowercase(Locale.ROOT)

                val bestIdx = randomized.indexOfFirst { candidate ->
                    val candNormTitle = duplicateScanner.normalizeTitle(candidate.title)
                    val candArtist = candidate.artist.trim().lowercase(Locale.ROOT)
                    candNormTitle != lastNormTitle && candArtist != lastArtist
                }
                if (bestIdx != -1) bestIdx else 0
            } else {
                0
            }

            result.add(randomized.removeAt(candidateIndex))
        }

        return result
    }

    fun setPlaybackSpeed(speed: Float) {
        _speed.value = speed
        settingsRepository.setPlaybackSpeed(speed)
        exoPlayer?.playbackParameters = PlaybackParameters(speed, _pitch.value)
    }

    fun setPlaybackPitch(pitch: Float) {
        _pitch.value = pitch
        exoPlayer?.playbackParameters = PlaybackParameters(_speed.value, pitch)
    }

    fun startSleepTimer(minutes: Int, smoothFadeSeconds: Int = 30) {
        cancelSleepTimer()
        stopAfterCurrentTrack = false
        val totalSeconds = minutes * 60
        _sleepTimerSecondsLeft.value = totalSeconds

        val effectiveFadeSec = smoothFadeSeconds.coerceAtMost(totalSeconds / 2).coerceAtLeast(5)

        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _sleepTimerSecondsLeft.value = remaining

                // Smooth gradual volume reduction in the final fade window
                if (remaining <= effectiveFadeSec) {
                    val fadeRatio = (remaining.toFloat() / effectiveFadeSec).coerceIn(0f, 1f)
                    exoPlayer?.volume = fadeRatio
                }
            }
            if (remaining <= 0) {
                exoPlayer?.pause()
                exoPlayer?.volume = 1f
                _sleepTimerSecondsLeft.value = null
            }
        }
    }

    fun setSleepTimerAfterCurrentTrack() {
        cancelSleepTimer()
        stopAfterCurrentTrack = true
        _sleepTimerSecondsLeft.value = -1
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        stopAfterCurrentTrack = false
        _sleepTimerSecondsLeft.value = null
        exoPlayer?.volume = 1f
    }

    fun removeQueueItem(index: Int) {
        val current = _queue.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _queue.value = current
            exoPlayer?.removeMediaItem(index)
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        originalQueue = emptyList()
        _currentSong.value = null
        _currentIndex.value = 0
        exoPlayer?.clearMediaItems()
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                if (stopAfterCurrentTrack) {
                    exoPlayer?.pause()
                    exoPlayer?.volume = 1f
                    cancelSleepTimer()
                    return
                }

                when (_repeatMode.value) {
                    RepeatMode.ONE -> {
                        exoPlayer?.seekTo(0L)
                        exoPlayer?.play()
                    }
                    RepeatMode.ALL -> {
                        skipNext()
                    }
                    RepeatMode.OFF -> {
                        if (_currentIndex.value < _queue.value.size - 1) {
                            skipNext()
                        } else {
                            _isPlaying.value = false
                        }
                    }
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val player = exoPlayer ?: return
            val newIndex = player.currentMediaItemIndex
            if (newIndex in _queue.value.indices) {
                _currentIndex.value = newIndex
                val rawSong = _queue.value[newIndex]
                _currentSong.value = rawSong
                _durationMs.value = rawSong.durationMs
                playlistRepository.recordSongPlayed(rawSong.id)
            }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            _speed.value = playbackParameters.speed
            _pitch.value = playbackParameters.pitch
        }

        @OptIn(UnstableApi::class)
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                equalizerController.bindAudioSession(audioSessionId)
            }
        }
    }

    companion object {
        @android.annotation.SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            return instance ?: synchronized(this) {
                instance ?: PlaybackManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
