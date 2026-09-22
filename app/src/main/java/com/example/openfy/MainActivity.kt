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

package com.example.openfy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.ui.theme.OpenFyTheme
import com.example.openfy.features.community.storage.AuthStorage
import com.example.openfy.features.community.ui.ProfileViewModel
import com.example.openfy.features.themes.engine.ThemeEngine
import kotlinx.coroutines.launch

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

class MainActivity : ComponentActivity() {

    private val authStorage by lazy { AuthStorage(this) }
    private val profileViewModel by lazy { ProfileViewModel(authStorage) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        if (audioGranted) {
            recreate()
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)
        requestRequiredPermissions()

        val playbackManager = (application as OpenFyApp).playbackManager

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
            val customThemeId by playbackManager.settingsRepository.customThemeId.collectAsState()
            val context = LocalContext.current

            val customColorScheme = remember(customThemeId) {
                if (customThemeId != null) {
                    ThemeEngine.loadThemeColors(context, customThemeId!!)
                } else null
            }

            OpenFyTheme(
                themeStyle = themeStyle,
                customColorScheme = customColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        playbackManager = playbackManager,
                        profileViewModel = profileViewModel,
                        windowWidthSizeClass = windowSizeClass.widthSizeClass
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        // 1. Handle Shared Streaming Links via ACTION_SEND
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: ""
            if (sharedText.isNotBlank()) {
                val playbackManager = (application as OpenFyApp).playbackManager
                lifecycleScope.launch {
                    val resolved = com.example.openfy.features.streamer.resolver.SafeStreamResolver.resolveStream(sharedText)
                    resolved.onSuccess { meta ->
                        playbackManager.playStreamTrack(meta.toSong())
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "Воспроизведение потока «${meta.title}»",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure {
                        val fallbackSong = com.example.openfy.core.audio.model.Song.createStreamTrack(sharedText)
                        playbackManager.playStreamTrack(fallbackSong)
                    }
                }
            }
            return
        }

        val uri: Uri = intent.data ?: return
        val scheme = uri.scheme ?: ""
        val host = uri.host ?: ""
        val path = uri.path ?: ""

        // 2. Handle Direct .thm theme file opening
        if (path.endsWith(".thm", ignoreCase = true) || uri.toString().endsWith(".thm", ignoreCase = true)) {
            val playbackManager = (application as OpenFyApp).playbackManager
            val themeManager = com.example.openfy.features.themes.engine.ThemeManager(this, playbackManager.settingsRepository)
            lifecycleScope.launch {
                val result = themeManager.importTheme(uri)
                result.onSuccess { meta ->
                    themeManager.applyTheme(meta.id)
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Тема «${meta.name}» успешно импортирована и применена!",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }.onFailure { err ->
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Ошибка импорта темы: ${err.localizedMessage}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
            return
        }

        if (scheme == "openfy" && (host == "oauth-callback" || host == "discord-callback" || host == "github-callback")) {
            profileViewModel.handleOAuthRedirect(uri)
        } else if (scheme == "openfy" && host == "theme") {
            // 3. Handle theme installation deep link: openfy://theme/install?id=...&url=...
            val themeId = uri.getQueryParameter("id") ?: ""
            val downloadUrl = uri.getQueryParameter("url") ?: ""
            val applyTheme = uri.getBooleanQueryParameter("apply", true)

            val playbackManager = (application as OpenFyApp).playbackManager
            val settingsRepo = playbackManager.settingsRepository

            lifecycleScope.launch {
                val result = if (downloadUrl.isNotBlank()) {
                    com.example.openfy.features.themes.engine.ThemeCatalogRepository.downloadAndInstallThemeFromUrl(
                        context = this@MainActivity,
                        settingsRepository = settingsRepo,
                        urlStr = downloadUrl
                    )
                } else if (themeId.isNotBlank()) {
                    com.example.openfy.features.themes.engine.ThemeCatalogRepository.installCatalogThemeById(
                        context = this@MainActivity,
                        settingsRepository = settingsRepo,
                        themeId = themeId
                    )
                } else {
                    Result.failure(IllegalArgumentException("Не указан идентификатор или ссылка на тему"))
                }

                result.onSuccess { meta ->
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Тема «${meta.name}» успешно установлена и применена!",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }.onFailure { err ->
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Ошибка установки темы: ${err.localizedMessage}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else if (scheme == "openfy" && host == "share") {
            val playbackManager = (application as OpenFyApp).playbackManager
            val themeManager = com.example.openfy.features.themes.engine.ThemeManager(this, playbackManager.settingsRepository)
            lifecycleScope.launch {
                val result = com.example.openfy.features.community.sync.UniversalImportHandler.handleImport(
                    context = this@MainActivity,
                    rawInput = uri.toString(),
                    playlistRepository = playbackManager.playlistRepository,
                    themeManager = themeManager
                )
                result.onSuccess { importResult ->
                    val msg = when (importResult) {
                        is com.example.openfy.features.community.sync.ImportResult.PlaylistImported ->
                            "Плейлист «${importResult.playlistName}» успешно импортирован (${importResult.trackCount} треков)!"
                        is com.example.openfy.features.community.sync.ImportResult.ThemeImported ->
                            "Тема «${importResult.themeName}» успешно импортирована!"
                        is com.example.openfy.features.community.sync.ImportResult.TrackMetaImported ->
                            "Трек «${importResult.title}» получен"
                    }
                    android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    android.widget.Toast.makeText(this@MainActivity, "Ошибка импорта: ${err.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
