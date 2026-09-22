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

package com.example.openfy.features.themes.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.openfy.features.themes.engine.CatalogThemeItem
import com.example.openfy.features.themes.engine.ThemeCatalogRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkGold
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.DarkBackground
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import com.example.openfy.core.ui.theme.RetroPhosphorGreen
import com.example.openfy.features.themes.engine.ThemeEngine
import com.example.openfy.features.themes.model.ThemeManifest
import com.example.openfy.features.themes.model.parseHexColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentThemeStyle by settingsRepository.themeStyle.collectAsState()
    val customThemeId by settingsRepository.customThemeId.collectAsState()

    var installedThemes by remember { mutableStateOf(ThemeEngine.getInstalledThemes(context)) }
    var themeToDelete by remember { mutableStateOf<ThemeManifest?>(null) }

    var catalogThemes by remember { mutableStateOf(ThemeCatalogRepository.BUILT_IN_CATALOG) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var inputUrl by remember { mutableStateOf("") }
    var isDownloadingUrl by remember { mutableStateOf(false) }
    var installingThemeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            catalogThemes = ThemeCatalogRepository.getCatalogThemes()
        }
    }

    fun refreshInstalledThemes() {
        installedThemes = ThemeEngine.getInstalledThemes(context)
    }

    // File picker launcher for .thm ZIP packages
    val themePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = ThemeEngine.importThemeFromUri(context, uri)
                result.onSuccess { manifest ->
                    refreshInstalledThemes()
                    settingsRepository.setCustomThemeId(manifest.id)
                    Toast.makeText(context, "Тема «${manifest.name}» успешно установлена и применена!", Toast.LENGTH_LONG).show()
                }.onFailure { error ->
                    Toast.makeText(context, "Ошибка импорта темы: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBg = if (currentThemeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface

    DynamicBackground(themeStyle = currentThemeStyle) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Темы оформления",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Кастомизация, цветовые схемы и импорт .thm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Import & Online Catalog Actions
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = cardBg,
                        hasGlowBorder = currentThemeStyle.hasNeonGlow,
                        glowColor = primaryAccent
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(primaryAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Импортировать тему (.thm)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Установка сторонних ZIP/THM тем с палитрами",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        themePickerLauncher.launch(arrayOf("*/*", "application/zip", "application/json", "application/octet-stream"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentThemeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else primaryAccent,
                                        contentColor = if (currentThemeStyle == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Выбрать файл",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showUrlDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "По ссылке (URL)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Online Catalog Header
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "КАТАЛОГ ТЕМ OPENFY (GITHUB)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccent,
                            letterSpacing = 1.sp
                        )

                        TextButton(
                            onClick = {
                                val syncUrl = ThemeCatalogRepository.getWebShowcaseSyncUrl(context)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(syncUrl))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Ссылка: $syncUrl", Toast.LENGTH_LONG).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Веб-витрина (GitHub Pages)", fontSize = 12.sp, color = primaryAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = primaryAccent
                            )
                        }
                    }
                }

                // Catalog themes list
                items(catalogThemes, key = { "catalog_${it.id}" }) { item ->
                    val isInstalled = installedThemes.any { it.id == item.id }
                    val isActive = customThemeId == item.id
                    val isInstalling = installingThemeId == item.id

                    CatalogThemeCard(
                        item = item,
                        isInstalled = isInstalled,
                        isActive = isActive,
                        isInstalling = isInstalling,
                        cardBg = cardBg,
                        onInstallAndApply = {
                            installingThemeId = item.id
                            scope.launch {
                                val result = ThemeCatalogRepository.installCatalogTheme(context, settingsRepository, item)
                                installingThemeId = null
                                result.onSuccess {
                                    refreshInstalledThemes()
                                    Toast.makeText(context, "Тема «${item.name}» успешно установлена и применена!", Toast.LENGTH_SHORT).show()
                                }.onFailure { err ->
                                    Toast.makeText(context, "Ошибка установки темы: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onApply = {
                            settingsRepository.setCustomThemeId(item.id)
                            Toast.makeText(context, "Применена тема «${item.name}»", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // App Launcher Icon Switcher
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    AppIconSelectorSection(settingsRepository = settingsRepository)
                }

                // Section 1: Built-in Themes
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ВСТРОЕННЫЕ ТЕМЫ OPENFY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                }

                items(AppThemeStyle.values()) { style ->
                    val isActive = customThemeId == null && currentThemeStyle == style
                    BuiltInThemeCard(
                        style = style,
                        isActive = isActive,
                        onClick = {
                            settingsRepository.setThemeStyle(style)
                            Toast.makeText(context, "Применена тема «${style.displayName}»", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Section 2: Installed Custom .thm Themes
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "УСТАНОВЛЕННЫЕ ПОЛЬЗОВАТЕЛЬСКИЕ ТЕМЫ (.THM)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                }

                if (installedThemes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Сторонние темы пока не установлены.\nНажмите кнопку «Импортировать тему (.thm)» выше!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(installedThemes, key = { it.id }) { theme ->
                        val isActive = customThemeId == theme.id
                        val rawColors = remember(theme.id) { ThemeEngine.loadThemeColorsRaw(context, theme.id) }

                        CustomThemeCard(
                            theme = theme,
                            primaryColor = parseHexColor(rawColors?.primary, fallback = Color(0xFF00E5FF)),
                            backgroundColor = parseHexColor(rawColors?.background, fallback = Color(0xFF0A0A0E)),
                            surfaceColor = parseHexColor(rawColors?.surface, fallback = Color(0xFF141418)),
                            accentColor = parseHexColor(rawColors?.secondary ?: rawColors?.tertiary, fallback = Color(0xFFBD00FF)),
                            isActive = isActive,
                            onClick = {
                                settingsRepository.setCustomThemeId(theme.id)
                                Toast.makeText(context, "Применена тема «${theme.name}»", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                themeToDelete = theme
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Delete Theme Dialog
    themeToDelete?.let { theme ->
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = { Text("Удалить тему?") },
            text = { Text("Вы действительно хотите удалить тему «${theme.name}» (версия ${theme.version})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customThemeId == theme.id) {
                            settingsRepository.setCustomThemeId(null)
                        }
                        ThemeEngine.deleteTheme(context, theme.id)
                        refreshInstalledThemes()
                        themeToDelete = null
                        Toast.makeText(context, "Тема «${theme.name}» удалена", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDownloadingUrl) showUrlDialog = false },
            title = { Text("Скачать тему по ссылке", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Вставьте прямую ссылку на .thm архив или theme.json (например, из GitHub):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text("https://.../theme.json") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            isDownloadingUrl = true
                            scope.launch {
                                val res = ThemeCatalogRepository.downloadAndInstallThemeFromUrl(context, settingsRepository, inputUrl)
                                isDownloadingUrl = false
                                showUrlDialog = false
                                res.onSuccess {
                                    refreshInstalledThemes()
                                    Toast.makeText(context, "Тема «${it.name}» успешно скачана и установлена!", Toast.LENGTH_SHORT).show()
                                }.onFailure { err ->
                                    Toast.makeText(context, "Ошибка загрузки темы: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !isDownloadingUrl && inputUrl.isNotBlank()
                ) {
                    if (isDownloadingUrl) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Скачать и применить")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUrlDialog = false },
                    enabled = !isDownloadingUrl
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun BuiltInThemeCard(
    style: AppThemeStyle,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val primaryAccent = MaterialTheme.colorScheme.primary

    val (c1, c2, c3, c4) = when (style) {
        AppThemeStyle.SERIOUS_DARK -> listOf(Color.White, Color.Black, Color(0xFF141418), Color(0xFF8E8E98))
        AppThemeStyle.MIDNIGHT_NEON -> listOf(NeonCyan, DarkBackground, GlassDarkSurface, ElectricPurple)
        AppThemeStyle.CYBERPUNK_BLOOD -> listOf(CyberpunkRubyRed, CyberpunkDarkBg, Color(0xFF1F040A), CyberpunkGold)
        AppThemeStyle.RETRO_PIXEL -> listOf(RetroPhosphorGreen, RetroDarkBg, Color(0xFF0F331D), Color(0xFF66FF99))
        AppThemeStyle.MATERIAL_YOU -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.secondary)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isActive) MaterialTheme.colorScheme.surface else Color.Black.copy(alpha = 0.35f),
        hasGlowBorder = isActive,
        glowColor = primaryAccent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Circles Preview
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(c1).border(1.5.dp, Color.Black, CircleShape))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(c2).border(1.5.dp, Color.Black, CircleShape))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(c3).border(1.5.dp, Color.Black, CircleShape))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(c4).border(1.5.dp, Color.Black, CircleShape))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = style.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryAccent.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "АКТИВНА",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomThemeCard(
    theme: ThemeManifest,
    primaryColor: Color,
    backgroundColor: Color,
    surfaceColor: Color,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryAccent = MaterialTheme.colorScheme.primary

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isActive) MaterialTheme.colorScheme.surface else Color.Black.copy(alpha = 0.35f),
        hasGlowBorder = isActive,
        glowColor = primaryAccent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Palette Circles Preview
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(primaryColor).border(1.5.dp, Color.Black, CircleShape))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(backgroundColor).border(1.5.dp, Color.Black, CircleShape))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(surfaceColor).border(1.5.dp, Color.Black, CircleShape))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(accentColor).border(1.5.dp, Color.Black, CircleShape))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v${theme.version} • ${theme.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (theme.description.isNotBlank()) {
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryAccent.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "АКТИВНА",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccent
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Удалить тему",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun CatalogThemeCard(
    item: CatalogThemeItem,
    isInstalled: Boolean,
    isActive: Boolean,
    isInstalling: Boolean,
    cardBg: Color,
    onInstallAndApply: () -> Unit,
    onApply: () -> Unit
) {
    val accentColor = parseHexColor(item.previewAccentHex, fallback = MaterialTheme.colorScheme.primary)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isActive) MaterialTheme.colorScheme.surface else cardBg,
        hasGlowBorder = isActive,
        glowColor = accentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "v${item.version} • ${item.author}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "АКТИВНА",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Palette color circles & Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Color dots preview
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    item.previewColors.take(4).forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(1.5.dp, Color.Black, CircleShape)
                        )
                    }
                }

                // Action button
                if (isActive) {
                    // Already active
                } else if (isInstalling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = accentColor
                    )
                } else if (isInstalled) {
                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text("Применить", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onInstallAndApply,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Установить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
