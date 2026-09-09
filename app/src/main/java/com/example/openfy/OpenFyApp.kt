package com.example.openfy

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.size.Precision
import coil.size.Scale
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.features.themes.engine.AppIconManager
import com.example.openfy.features.themes.engine.AppLauncherIcon

import android.os.Build

class OpenFyApp : Application(), ImageLoaderFactory {

    lateinit var playbackManager: PlaybackManager
        private set

    override fun onCreate() {
        super.onCreate()
        playbackManager = PlaybackManager.getInstance(this)

        val savedIconKey = playbackManager.settingsRepository.appLauncherIconKey.value
        val savedIcon = AppLauncherIcon.fromKey(savedIconKey)
        AppIconManager.ensureAppIconEnabled(this, savedIcon)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20) // Limit image memory cache to max 20% of process RAM
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05) // Max 5% disk cache
                    .build()
            }
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bitmapConfig(Bitmap.Config.HARDWARE)
                } else {
                    bitmapConfig(Bitmap.Config.ARGB_8888)
                }
            }
            .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            .allowRgb565(true)
            .crossfade(true)
            .respectCacheHeaders(false)
            .precision(Precision.AUTOMATIC)
            .build()
    }
}
