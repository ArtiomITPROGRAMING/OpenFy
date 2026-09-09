package com.example.openfy.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.Coil
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.IconPackStyle
import com.example.openfy.core.audio.model.Quadruple
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AmoledPureBlack
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.DarkBackground
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import com.example.openfy.core.ui.theme.RetroPhosphorGreen

enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val badge: String
) {
    APPEARANCE(
        "Внешний вид и интерфейс",
        "Темы оформления (5 стилей), наборы векторных SVG-иконок, компактность",
        "Темы и иконки"
    ),
    PLAYBACK(
        "Воспроизведение и звук",
        "Скорость, таймер сна, пауза при отключении, Gapless, затухание, перемотка",
        "Аудио и таймер"
    ),
    AUDIO_ENGINE(
        "Звуковой движок и система",
        "10-полосный DSP эквалайзер, фильтр коротких файлов, очистка клонов и кэша",
        "DSP и система"
    ),
    ABOUT_AUTHOR(
        "Об авторе и поддержка",
        "Разработчик: Артём (Artiom Crudu), Roadmap, поддержка через PayPal",
        "PayPal и автор"
    )
}

@Composable
fun SettingsScreen(
    playbackManager: PlaybackManager,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToDuplicateCleaner: () -> Unit = {},
    onNavigateToThemes: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToQrScanner: () -> Unit = {},
    onNavigateToCarMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsRepo = playbackManager.settingsRepository
    val currentTheme by settingsRepo.themeStyle.collectAsState()
    val currentIconPack by settingsRepo.iconPackStyle.collectAsState()

    val pauseOnUnplug by settingsRepo.pauseOnUnplug.collectAsState()
    val gaplessPlayback by settingsRepo.gaplessPlayback.collectAsState()
    val fadeOnPlayPause by settingsRepo.fadeOnPlayPause.collectAsState()
    val ignoreShortTracks by settingsRepo.ignoreShortTracks.collectAsState()
    val compactListMode by settingsRepo.compactListMode.collectAsState()
    val seekInterval by settingsRepo.seekIntervalSeconds.collectAsState()
    val carModeAutoLaunch by settingsRepo.carModeAutoLaunch.collectAsState()
    val carModeKeepScreenOn by settingsRepo.carModeKeepScreenOn.collectAsState()

    val currentSpeed by playbackManager.speed.collectAsState()
    val sleepTimerSeconds by playbackManager.sleepTimerSecondsLeft.collectAsState()

    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBg = if (currentTheme == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface

    var activeSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    // Intercept back button when inside a section
    BackHandler(enabled = activeSection != null) {
        activeSection = null
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Скопировано: $text", Toast.LENGTH_SHORT).show()
    }

    val sectionGlow = currentTheme.hasNeonGlow

    // Thematic Card Colors based on Active Theme Style
    val (appGrad, playGrad, dspGrad, authorGrad) = when (currentTheme) {
        AppThemeStyle.SERIOUS_DARK -> Quadruple(
            listOf(Color(0xFF383944), Color(0xFF1E1F28)),
            listOf(Color(0xFF404252), Color(0xFF222430)),
            listOf(Color(0xFF30323E), Color(0xFF181920)),
            listOf(Color(0xFF363846), Color(0xFF1C1D24))
        )
        AppThemeStyle.CYBERPUNK_BLOOD -> Quadruple(
            listOf(Color(0xFF5A0D1E), Color(0xFF26040C)),
            listOf(Color(0xFFFF0055), Color(0xFF6B0024)),
            listOf(Color(0xFFFF9100), Color(0xFF663800)),
            listOf(Color(0xFF8B1E3F), Color(0xFF3A0014))
        )
        AppThemeStyle.RETRO_PIXEL -> Quadruple(
            listOf(Color(0xFF0F3820), Color(0xFF061A0E)),
            listOf(Color(0xFF175932), Color(0xFF0A2B18)),
            listOf(Color(0xFF1E6B3E), Color(0xFF0D331E)),
            listOf(Color(0xFF0A4022), Color(0xFF041C0E))
        )
        AppThemeStyle.MATERIAL_YOU -> Quadruple(
            listOf(Color(0xFF4A4458), Color(0xFF2D2938)),
            listOf(Color(0xFF6750A4), Color(0xFF3E2D69)),
            listOf(Color(0xFF534341), Color(0xFF322725)),
            listOf(Color(0xFF384956), Color(0xFF1D2830))
        )
        else -> Quadruple(
            listOf(NeonCyan, Color(0xFF0070F3)),
            listOf(Color(0xFFFF007F), Color(0xFFBD00FF)),
            listOf(Color(0xFFFF9100), Color(0xFFFF3D00)),
            listOf(Color(0xFF0079C1), Color(0xFF00457C))
        )
    }

    AnimatedContent(
        targetState = activeSection,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "SettingsSectionTransition"
    ) { section ->
        if (section == null) {
            // ==========================================
            // ГЛАВНЫЙ ЭКРАН НАСТРОЕК (HUB РАЗДЕЛОВ)
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Настройки",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Персонализация и управление OpenFy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 0. Профиль и Сообщество Card
                item {
                    val commGrad = if (currentTheme == AppThemeStyle.SERIOUS_DARK) {
                        listOf(Color(0xFF383944), Color(0xFF1E1F28))
                    } else {
                        listOf(Color(0xFF5865F2), Color(0xFF24292F))
                    }
                    SettingsSectionHubCard(
                        title = "Профиль & Сообщество",
                        subtitle = "GitHub / Discord авторизация, P2P обмен темами и плейлистами",
                        badge = "OAuth 2.0 • P2P",
                        icon = AppIcons.author,
                        gradientColors = commGrad,
                        cardBg = cardBg,
                        hasGlow = sectionGlow,
                        glowColor = Color(0xFF5865F2),
                        onClick = onNavigateToProfile
                    )
                }

                // 0.1 Сканер QR-кода Card
                item {
                    val qrGrad = if (currentTheme == AppThemeStyle.SERIOUS_DARK) {
                        listOf(Color(0xFF2A2B35), Color(0xFF14141A))
                    } else {
                        listOf(Color(0xFF00E5FF), Color(0xFF005A64))
                    }
                    SettingsSectionHubCard(
                        title = "Сканер QR-кода",
                        subtitle = "Импорт плейлистов и тем с другого устройства",
                        badge = "CameraX • P2P Sync",
                        icon = Icons.Default.QrCodeScanner,
                        gradientColors = qrGrad,
                        cardBg = cardBg,
                        hasGlow = sectionGlow,
                        glowColor = Color(0xFF00E5FF),
                        onClick = onNavigateToQrScanner
                    )
                }

                // 1. Внешний вид и интерфейс Card
                item {
                    SettingsSectionHubCard(
                        title = SettingsSection.APPEARANCE.title,
                        subtitle = SettingsSection.APPEARANCE.subtitle,
                        badge = currentTheme.displayName,
                        icon = AppIcons.album(currentIconPack),
                        gradientColors = appGrad,
                        cardBg = cardBg,
                        hasGlow = sectionGlow,
                        glowColor = primaryAccent,
                        onClick = { activeSection = SettingsSection.APPEARANCE }
                    )
                }

                // 2. Воспроизведение и звук Card
                item {
                    SettingsSectionHubCard(
                        title = SettingsSection.PLAYBACK.title,
                        subtitle = SettingsSection.PLAYBACK.subtitle,
                        badge = "${"%.2f".format(currentSpeed)}x" + if (sleepTimerSeconds != null) " • Таймер активен" else "",
                        icon = AppIcons.play(currentIconPack),
                        gradientColors = playGrad,
                        cardBg = cardBg,
                        hasGlow = sectionGlow,
                        glowColor = primaryAccent,
                        onClick = { activeSection = SettingsSection.PLAYBACK }
                    )
                }

                // 3. Звуковой движок и система Card
                item {
                    SettingsSectionHubCard(
                        title = SettingsSection.AUDIO_ENGINE.title,
                        subtitle = SettingsSection.AUDIO_ENGINE.subtitle,
                        badge = "10-полосный DSP • Клоны",
                        icon = AppIcons.equalizer(currentIconPack),
                        gradientColors = dspGrad,
                        cardBg = cardBg,
                        hasGlow = sectionGlow,
                        glowColor = primaryAccent,
                        onClick = { activeSection = SettingsSection.AUDIO_ENGINE }
                    )
                }

                // 3.1 Фоновая работа & Батарея (Xiaomi / Samsung / Huawei OEM)
                item {
                    val batteryGrad = if (currentTheme == AppThemeStyle.SERIOUS_DARK) {
                        listOf(Color(0xFF2E2F3B), Color(0xFF16171E))
                    } else {
                        listOf(Color(0xFF00E676), Color(0xFF005A32))
                    }
                    val brand = com.example.openfy.core.ui.components.BatteryOptimizationHelper.getDeviceBrand()
                    val isIgnored = com.example.openfy.core.ui.components.BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)

                    SettingsSectionHubCard(
                        title = "Фоновая работа и батарея",
                        subtitle = "Настройка автозапуска и снятие ограничений питания (${brand.displayName})",
                        badge = if (isIgnored) "Без ограничений ✓" else "Требует настройки ⚠️",
                        icon = androidx.compose.material.icons.Icons.Default.VerifiedUser,
                        gradientColors = batteryGrad,
                        cardBg = cardBg,
                        hasGlow = sectionGlow,
                        glowColor = Color(0xFF00E676),
                        onClick = { showBatteryDialog = true }
                    )
                }

                // 4. Об авторе и поддержка Card
                item {
                    SettingsSectionHubCard(
                        title = SettingsSection.ABOUT_AUTHOR.title,
                        subtitle = SettingsSection.ABOUT_AUTHOR.subtitle,
                        badge = "Артём • paypal.me",
                        icon = AppIcons.author,
                        gradientColors = authorGrad,
                        cardBg = cardBg,
                        hasGlow = sectionGlow,
                        glowColor = primaryAccent,
                        onClick = { activeSection = SettingsSection.ABOUT_AUTHOR }
                    )
                }

                // ==========================================
                // СТЕНДАЛОН КАРТОЧКА: О ПРИЛОЖЕНИИ (F-DROID FOSS)
                // ==========================================
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "О ПРИЛОЖЕНИИ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = cardBg,
                        hasGlowBorder = currentTheme.hasNeonGlow,
                        glowColor = primaryAccent.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    if (currentTheme == AppThemeStyle.SERIOUS_DARK) listOf(Color(0xFF383944), Color(0xFF1E1F28))
                                                    else listOf(primaryAccent.copy(alpha = 0.8f), ElectricPurple.copy(alpha = 0.6f))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = AppIcons.music(currentIconPack),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = "OpenFy Music Player",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Версия 1.0.0 (F-Droid FOSS Edition)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = primaryAccent
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "FOSS",
                                    tint = primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Specs Grid
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SpecInfoRow("Архитектура", "100% Offline & Open-Source (Без облаков)")
                                SpecInfoRow("Лицензия", "GNU General Public License v3.0 (FOSS)")
                                SpecInfoRow("Приватность", "0 Трекеров • 0 Рекламы • 0 Телеметрии")
                                SpecInfoRow("Аудиоядро", "AndroidX Media3 (ExoPlayer) + 10-Band DSP")
                                SpecInfoRow("Совместимость", "Android 8.0 - 15 (Target SDK 35)")
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Copy System Info Button
                            OutlinedButton(
                                onClick = {
                                    val buildInfo = """
                                        OpenFy Music Player v1.0.0 (F-Droid FOSS)
                                        OS: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                                        Device: ${Build.MANUFACTURER} ${Build.MODEL}
                                        Audio Engine: Media3 ExoPlayer 1.3.1 + DSP
                                        Theme: ${currentTheme.name}
                                        Icon Pack: ${currentIconPack.name}
                                        Privacy: 100% Offline / Zero Trackers
                                    """.trimIndent()
                                    copyToClipboard("Сведения OpenFy", buildInfo)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.copy,
                                    contentDescription = null,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Скопировать сведения о сборке",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        } else {
            // ==========================================
            // ЭКРАН ВЫБРАННОГО РАЗДЕЛА (SUB-SCREEN)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Section Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeSection = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = primaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Настройки OpenFy",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryAccent
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (section) {
                        SettingsSection.APPEARANCE -> {
                            // 0. Менеджер тем и импорт .thm
                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToThemes() },
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = primaryAccent.copy(alpha = 0.15f),
                                    hasGlowBorder = currentTheme.hasNeonGlow,
                                    glowColor = primaryAccent
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
                                                .background(primaryAccent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = AppIcons.palette(currentIconPack),
                                                contentDescription = null,
                                                tint = primaryAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Менеджер и импорт тем (.thm)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Установка сторонних тем, предпросмотр палитр и онлайн-каталог",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = primaryAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // 0.1 Селектор иконки лаунчера
                            item {
                                Spacer(modifier = Modifier.height(2.dp))
                                com.example.openfy.features.themes.ui.AppIconSelectorSection(settingsRepository = settingsRepo)
                            }

                            // 1. Темы оформления
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ВСТРОЕННЫЕ ПРЕСЕТЫ",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AppThemeStyle.values().forEach { style ->
                                        val isSelected = currentTheme == style
                                        val previewBg = when (style) {
                                            AppThemeStyle.SERIOUS_DARK -> AmoledPureBlack
                                            AppThemeStyle.MIDNIGHT_NEON -> DarkBackground
                                            AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
                                            AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
                                            AppThemeStyle.MATERIAL_YOU -> Color(0xFF1B1B22)
                                        }
                                        val previewAccent = when (style) {
                                            AppThemeStyle.SERIOUS_DARK -> Color.White
                                            AppThemeStyle.MIDNIGHT_NEON -> NeonCyan
                                            AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkRubyRed
                                            AppThemeStyle.RETRO_PIXEL -> RetroPhosphorGreen
                                            AppThemeStyle.MATERIAL_YOU -> Color(0xFF6750A4)
                                        }

                                        GlassCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { settingsRepo.setThemeStyle(style) },
                                            shape = RoundedCornerShape(18.dp),
                                            backgroundColor = if (isSelected) cardBg else Color.Black.copy(alpha = 0.5f),
                                            hasGlowBorder = isSelected && style.hasNeonGlow,
                                            glowColor = previewAccent
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
                                                        .background(previewBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .clip(CircleShape)
                                                            .background(previewAccent)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(14.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = style.displayName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = style.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.75f)
                                                    )
                                                }

                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { settingsRepo.setThemeStyle(style) },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = previewAccent,
                                                        unselectedColor = Color.White.copy(alpha = 0.4f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Дополнительные настройки интерфейса
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ПЕРСОНАЛИЗАЦИЯ ИНТЕРФЕЙСА",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Компактный вид списков",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Уменьшает отступы для отображения большего числа треков на экране",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = compactListMode,
                                                onCheckedChange = { settingsRepo.setCompactListMode(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                                    checkedTrackColor = primaryAccent
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsSection.PLAYBACK -> {
                            // 1. Скорость
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(AppIcons.speed, contentDescription = null, tint = primaryAccent, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Скорость воспроизведения",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = "%.2fx".format(currentSpeed),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryAccent
                                            )
                                        }

                                        Slider(
                                            value = currentSpeed,
                                            onValueChange = { playbackManager.setPlaybackSpeed(it) },
                                            valueRange = 0.5f..2.0f,
                                            steps = 14,
                                            colors = SliderDefaults.colors(
                                                thumbColor = primaryAccent,
                                                activeTrackColor = primaryAccent,
                                                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            )
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speedVal ->
                                                FilterChip(
                                                    selected = (currentSpeed - speedVal).let { it > -0.05f && it < 0.05f },
                                                    onClick = { playbackManager.setPlaybackSpeed(speedVal) },
                                                    label = { Text("${speedVal}x") }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Таймер сна
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Таймер сна",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (sleepTimerSeconds != null) {
                                                        if (sleepTimerSeconds == -1) "Остановится после текущего трека"
                                                        else "Осталось: ${sleepTimerSeconds!! / 60} мин ${sleepTimerSeconds!! % 60} сек"
                                                    } else "Выключен",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (sleepTimerSeconds != null) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            if (sleepTimerSeconds != null) {
                                                OutlinedButton(
                                                    onClick = { playbackManager.cancelSleepTimer() },
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Отменить", fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            val timerPresets = listOf(5, 10, 15, 30, 45, 60, 90, 120)
                                            items(timerPresets) { min ->
                                                FilterChip(
                                                    selected = false,
                                                    onClick = { playbackManager.startSleepTimer(min) },
                                                    label = { Text("$min мин") },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                )
                                            }
                                            item {
                                                FilterChip(
                                                    selected = false,
                                                    onClick = { playbackManager.setSleepTimerAfterCurrentTrack() },
                                                    label = { Text("Конец трека") },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Поведение звука
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Pause on unplug
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Пауза при отключении наушников",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Автоматически ставить на паузу при отсоединении гарнитуры или Bluetooth",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = pauseOnUnplug,
                                                onCheckedChange = { settingsRepo.setPauseOnUnplug(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                                    checkedTrackColor = primaryAccent
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Gapless Playback
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Воспроизведение без пауз (Gapless)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Бесшовный переход между композициями для альбомов и живых записей",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = gaplessPlayback,
                                                onCheckedChange = { settingsRepo.setGaplessPlayback(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                                    checkedTrackColor = primaryAccent
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Fade on Play/Pause
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Плавное затухание (Fade In/Out)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Мягкое уменьшение и нарастание громкости при паузе и старте",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = fadeOnPlayPause,
                                                onCheckedChange = { settingsRepo.setFadeOnPlayPause(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                                    checkedTrackColor = primaryAccent
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Double Tap Seek Interval
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Интервал быстрой перемотки",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Шаг перемотки вперед и назад при двойном касании",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(5, 10, 15, 30).forEach { sec ->
                                                FilterChip(
                                                    selected = seekInterval == sec,
                                                    onClick = { settingsRepo.setSeekIntervalSeconds(sec) },
                                                    label = { Text("$sec сек") }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Car Mode & Android Auto Hub
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = AppIcons.car,
                                                    contentDescription = null,
                                                    tint = primaryAccent,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Режим «В автомобиле» & Auto",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Drive-Safe интерфейс с крупными элементами, жестами свайпа и удержанием экрана для безопасного вождения.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = onNavigateToCarMode,
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = AppIcons.car,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Запустить автомобильный экран (Car Mode)", fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Auto-launch switch
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Автозапуск при подключении авто",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Автоматически открывать Car Mode при соединении с Bluetooth автомобиля",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = carModeAutoLaunch,
                                                onCheckedChange = { settingsRepo.setCarModeAutoLaunch(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                                    checkedTrackColor = primaryAccent
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Keep screen on switch
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Не выключать экран в Car Mode",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Удерживает экран активным во время поездки без перехода в спящий режим",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = carModeKeepScreenOn,
                                                onCheckedChange = { settingsRepo.setCarModeKeepScreenOn(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                                    checkedTrackColor = primaryAccent
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsSection.AUDIO_ENGINE -> {
                            // 1. Equalizer Button
                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onNavigateToEqualizer),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Brush.linearGradient(dspGrad)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = AppIcons.equalizer(currentIconPack),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "DSP Эквалайзер и Звуковые эффекты",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "10-полосный эквалайзер, Virtualizer, Bass Boost",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // 2. Duplicate Cleaner Card
                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onNavigateToDuplicateCleaner),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Brush.linearGradient(dspGrad)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = AppIcons.clear,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Очистка клонов и дубликатов",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Поиск похожих треков (Slowed, Sped Up, (1).mp3) и освобождение места",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // 3. Battery Optimization Button
                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                                    context.startActivity(intent)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Brush.linearGradient(dspGrad)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = AppIcons.battery,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Фоновое воспроизведение без выгрузок",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Отключение оптимизации батареи Android для непрерывной музыки",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // 4. File Filter & Cache Maintenance
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Игнорировать короткие аудио (<30 сек)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Скрывает из медиатеки рингтоны, звуки уведомлений и голосовые сообщения",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = ignoreShortTracks,
                                                onCheckedChange = { settingsRepo.setIgnoreShortTracks(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                                    checkedTrackColor = primaryAccent
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Clear Image Cache Button
                                        Button(
                                            onClick = {
                                                try {
                                                    @OptIn(coil.annotation.ExperimentalCoilApi::class)
                                                    run {
                                                        Coil.imageLoader(context).memoryCache?.clear()
                                                        Coil.imageLoader(context).diskCache?.clear()
                                                    }
                                                    Toast.makeText(context, "Кэш обложек успешно очищен", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Кэш пуст", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White.copy(alpha = 0.12f),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CleaningServices,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Очистить кэш обложек альбомов",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        SettingsSection.ABOUT_AUTHOR -> {
                            // 1. Author Info Card
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    backgroundColor = cardBg,
                                    hasGlowBorder = currentTheme.hasNeonGlow,
                                    glowColor = primaryAccent.copy(alpha = 0.4f)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(authorGrad)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = AppIcons.author,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Text(
                                                    text = "Артём (Artiom Crudu)",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Создатель и ведущий разработчик OpenFy",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color(0xFFAAAAAF) else Color(0xFF00E5FF)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "«Привет! Я разрабатываю OpenFy как полностью независимый, бескомпромиссно быстрый и эстетичный плеер без рекламы, платных подписок и слежки. Ваша поддержка мотивирует меня уделять проекту максимум времени и реализовывать самые амбициозные идеи!»",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 19.sp
                                        )
                                    }
                                }
                            }

                            // 2. Future Plans & Roadmap
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    backgroundColor = cardBg
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = AppIcons.speed,
                                                contentDescription = null,
                                                tint = primaryAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Планы на будущее (Roadmap)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Что появится в следующих обновлениях OpenFy:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        val roadmapItems = listOf(
                                            Triple(AppIcons.music(currentIconPack), "Bit-Perfect Hi-Res Audio Engine", "Прямой вывод звука в обход системного микшера (24-bit/192kHz, DSD, FLAC, ALAC)"),
                                            Triple(AppIcons.album(currentIconPack), "Локальный редактор ID3-тегов", "Редактирование метаданных и встраивание обложек прямо в аудиофайлы"),
                                            Triple(AppIcons.equalizer(currentIconPack), "32-полосный параметрический DSP", "Студийный эквалайзер с локальными пресетами калибровки под наушники"),
                                            Triple(AppIcons.home(currentIconPack), "Интерактивные виджеты", "Стильные виджеты на рабочий стол с визуализацией волн в стиле Material You"),
                                            Triple(AppIcons.play(currentIconPack), "Умный локальный кроссфейд", "Плавное бесшовное сведение треков с аппаратным анализом BPM"),
                                            Triple(AppIcons.security, "100% Офлайн и приватность", "Никаких облаков, трекеров и фоновых утечек данных — всё только на устройстве")
                                        )

                                        roadmapItems.forEach { (icon, title, desc) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 5.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = primaryAccent,
                                                    modifier = Modifier
                                                        .padding(top = 2.dp, end = 10.dp)
                                                        .size(16.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = desc,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.65f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. PayPal Hero Showcase Card
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    backgroundColor = cardBg,
                                    hasGlowBorder = currentTheme.hasNeonGlow,
                                    glowColor = primaryAccent.copy(alpha = 0.6f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(authorGrad)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = AppIcons.paypal,
                                                contentDescription = "PayPal",
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Поддержать через PayPal",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Text(
                                            text = "paypal.me/ArtiomCrudu2010",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color(0xFFAAAAAF) else Color(0xFF00E5FF)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Primary Big Action: Open PayPal
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/ArtiomCrudu2010"))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    copyToClipboard("PayPal", "https://paypal.me/ArtiomCrudu2010")
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.White else Color(0xFF0079C1),
                                                contentColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White
                                            )
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = AppIcons.openInNew,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Открыть перевод в PayPal",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Secondary Action: Copy Link
                                        OutlinedButton(
                                            onClick = { copyToClipboard("PayPal", "https://paypal.me/ArtiomCrudu2010") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(42.dp),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = AppIcons.copy,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.9f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Скопировать ссылку paypal.me/ArtiomCrudu2010",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }

    if (showBatteryDialog) {
        com.example.openfy.core.ui.components.BatteryOptimizationDialog(
            onDismissRequest = { showBatteryDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHubCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    cardBg: Color,
    hasGlow: Boolean,
    glowColor: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = cardBg,
        hasGlowBorder = hasGlow,
        glowColor = glowColor.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SpecInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
