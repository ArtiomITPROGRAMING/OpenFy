package com.example.openfy.core.audio.service

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.io.File

/**
 * Thread-safe singleton provider managing AndroidX Media3 disk caching via [SimpleCache]
 * and creating caching [DefaultDataSource.Factory] for ExoPlayer.
 */
@OptIn(UnstableApi::class)
object PlayerCacheProvider {

    const val MAX_CACHE_SIZE_BYTES: Long = 500L * 1024 * 1024 // 500 MB LRU limit
    const val CACHE_DIR_NAME: String = "audio_cache"
    const val USER_AGENT: String = "OpenFy-AudioPlayer"
    const val CONNECT_TIMEOUT_MS: Int = 15_000
    const val READ_TIMEOUT_MS: Int = 15_000

    @Volatile
    private var simpleCacheInstance: SimpleCache? = null

    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var defaultDataSourceFactoryInstance: DefaultDataSource.Factory? = null

    /**
     * Retrieves or initializes the single process-wide [SimpleCache] instance.
     */
    @Synchronized
    fun getSimpleCache(context: Context): SimpleCache {
        return simpleCacheInstance ?: run {
            val cacheDir = File(context.applicationContext.cacheDir, CACHE_DIR_NAME).apply {
                if (!exists()) mkdirs()
            }
            val databaseProvider = StandaloneDatabaseProvider(context.applicationContext)
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
            SimpleCache(cacheDir, evictor, databaseProvider).also {
                simpleCacheInstance = it
            }
        }
    }

    /**
     * Builds and caches the configured [DefaultDataSource.Factory] wrapping network caching
     * with local file compatibility (file://, content://, etc.).
     */
    @Synchronized
    fun getDefaultDataSourceFactory(context: Context): DefaultDataSource.Factory {
        return defaultDataSourceFactoryInstance ?: run {
            val appContext = context.applicationContext
            val simpleCache = getSimpleCache(appContext)

            // 1. Upstream network DataSource with timeouts and custom User-Agent
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(USER_AGENT)
                .setConnectTimeoutMs(CONNECT_TIMEOUT_MS)
                .setReadTimeoutMs(READ_TIMEOUT_MS)
                .setAllowCrossProtocolRedirects(true)

            // 2. Cache Data Sink for writing received stream bytes into disk cache
            val cacheDataSinkFactory = CacheDataSink.Factory()
                .setCache(simpleCache)

            // 3. CacheDataSource.Factory combining disk cache, upstream network, and sink
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setCacheWriteDataSinkFactory(cacheDataSinkFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

            // 4. Wrap with DefaultDataSource.Factory for full local file and asset compatibility
            val defaultFactory = DefaultDataSource.Factory(appContext, cacheDataSourceFactory)
            defaultDataSourceFactoryInstance = defaultFactory
            defaultFactory
        }
    }

    /**
     * Creates a configured [DefaultMediaSourceFactory] for [androidx.media3.exoplayer.ExoPlayer.Builder].
     */
    fun getMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(getDefaultDataSourceFactory(context))
    }

    /**
     * Returns total size of cached audio files in bytes.
     */
    fun getCacheSizeBytes(context: Context): Long {
        simpleCacheInstance?.let {
            return it.cacheSpace
        }
        val cacheDir = File(context.applicationContext.cacheDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) return 0L
        return cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    /**
     * Clears all cached audio data from disk and resets the SimpleCache instance.
     */
    @Synchronized
    fun clearAudioCache(context: Context) {
        try {
            simpleCacheInstance?.release()
            simpleCacheInstance = null
            defaultDataSourceFactoryInstance = null

            val cacheDir = File(context.applicationContext.cacheDir, CACHE_DIR_NAME)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
                cacheDir.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
