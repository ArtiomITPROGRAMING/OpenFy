package com.example.openfy.core.audio.service

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object StreamCacheManager {

    fun getSimpleCache(context: Context): SimpleCache = PlayerCacheProvider.getSimpleCache(context)

    fun getCacheDataSourceFactory(context: Context): DataSource.Factory = PlayerCacheProvider.getDefaultDataSourceFactory(context)

    fun getCacheSizeBytes(context: Context): Long = PlayerCacheProvider.getCacheSizeBytes(context)

    fun clearStreamCache(context: Context) = PlayerCacheProvider.clearAudioCache(context)
}
