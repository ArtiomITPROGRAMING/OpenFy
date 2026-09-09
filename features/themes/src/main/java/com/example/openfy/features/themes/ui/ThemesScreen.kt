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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

                            Button(
                                onClick = {
                                    themePickerLauncher.launch(arrayOf("*/*", "application/zip", "application/octet-stream"))
                                },
                                modifier = Modifier.fillMaxWidth(),
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
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Выбрать .thm файл",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Online Catalog
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ArtiomCrudu2010/OpenFy-Themes"))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Ссылка: https://github.com/ArtiomCrudu2010/OpenFy-Themes", Toast.LENGTH_LONG).show()
                                }
                            },
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = cardBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFBD00FF).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color(0xFFBD00FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Онлайн-каталог тем OpenFy",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Скачивайте открытые темы оформления от сообщества",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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
