package com.example.openfy.core.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.PlayerCoverStyle
import com.example.openfy.core.audio.data.ProgressBarStyle
import com.example.openfy.core.audio.data.ScreenOffSkipMode
import com.example.openfy.core.audio.service.NatureSoundType
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.GlassDarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCustomizationBottomSheet(
    playbackManager: PlaybackManager,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settingsRepo = playbackManager.settingsRepository
    val currentTheme by settingsRepo.themeStyle.collectAsState()

    val currentProgressBar by settingsRepo.progressBarStyle.collectAsState()
    val currentCoverStyle by settingsRepo.playerCoverStyle.collectAsState()
    val currentSkipMode by settingsRepo.screenOffSkipMode.collectAsState()

    val smartSleepEnabled by settingsRepo.smartSleepGuardEnabled.collectAsState()
    val smartSleepSound by settingsRepo.smartSleepNatureSound.collectAsState()
    val smartSleepInactivity by settingsRepo.smartSleepInactivityMinutes.collectAsState()

    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBg = if (currentTheme == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface

    var isTestingNatureSound by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            if (isTestingNatureSound) {
                playbackManager.zenNatureAudioEngine.stop()
            }
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color(0xFF101014) else Color(0xFF161822),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Персонализация плеера",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Стили интерфейса и умные алгоритмы",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(onClick = {
                    if (isTestingNatureSound) {
                        playbackManager.zenNatureAudioEngine.stop()
                    }
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "СТИЛЬ ПРОГРЕСС-БАРА",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProgressBarStyle.values().forEach { style ->
                            val isSelected = currentProgressBar == style
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsRepo.setProgressBarStyle(style) },
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = if (isSelected) cardBg else Color.Black.copy(alpha = 0.35f),
                                hasGlowBorder = isSelected && currentTheme.hasNeonGlow,
                                glowColor = primaryAccent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { settingsRepo.setProgressBarStyle(style) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = primaryAccent,
                                            unselectedColor = Color.White.copy(alpha = 0.4f)
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = style.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = style.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "СТИЛЬ ОБЛОЖКИ ТРЕКА",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlayerCoverStyle.values().forEach { style ->
                            val isSelected = currentCoverStyle == style
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsRepo.setPlayerCoverStyle(style) },
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = if (isSelected) cardBg else Color.Black.copy(alpha = 0.35f),
                                hasGlowBorder = isSelected && currentTheme.hasNeonGlow,
                                glowColor = primaryAccent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { settingsRepo.setPlayerCoverStyle(style) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = primaryAccent,
                                            unselectedColor = Color.White.copy(alpha = 0.4f)
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = style.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = style.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "УПРАВЛЕНИЕ С ВЫКЛЮЧЕННЫМ ЭКРАНОМ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScreenOffSkipMode.values().forEach { mode ->
                            val isSelected = currentSkipMode == mode
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsRepo.setScreenOffSkipMode(mode) },
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = if (isSelected) cardBg else Color.Black.copy(alpha = 0.35f),
                                hasGlowBorder = isSelected && currentTheme.hasNeonGlow,
                                glowColor = primaryAccent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { settingsRepo.setScreenOffSkipMode(mode) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = primaryAccent,
                                            unselectedColor = Color.White.copy(alpha = 0.4f)
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = mode.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "УМНЫЙ СОН И ЗАЩИТА СЛУХА (SOMNOGUARD)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = cardBg,
                        hasGlowBorder = smartSleepEnabled && currentTheme.hasNeonGlow,
                        glowColor = primaryAccent
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Детектор засыпания в наушниках",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Определяет неподвижность и мягко гасит музыку, включая звуки природы",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }

                                Switch(
                                    checked = smartSleepEnabled,
                                    onCheckedChange = { settingsRepo.setSmartSleepGuardEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White,
                                        checkedTrackColor = primaryAccent
                                    )
                                )
                            }

                            AnimatedVisibility(visible = smartSleepEnabled) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    Text(
                                        text = "Звук природы при засыпании (100% Офлайн):",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryAccent
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val chipTextColor = if (currentTheme == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White
                                    val natureChipColors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryAccent,
                                        selectedLabelColor = chipTextColor
                                    )

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(NatureSoundType.values()) { nature ->
                                            val isSelected = smartSleepSound == nature
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { settingsRepo.setSmartSleepNatureSound(nature) },
                                                label = { Text(nature.displayName) },
                                                colors = natureChipColors
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Интервал неподвижности до срабатывания:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryAccent
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val minutesList = listOf(10, 15, 20, 30, 45)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(minutesList) { min ->
                                            val isSelected = smartSleepInactivity == min
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { settingsRepo.setSmartSleepInactivityMinutes(min) },
                                                label = { Text("$min мин") },
                                                colors = natureChipColors
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (isTestingNatureSound) {
                                                playbackManager.zenNatureAudioEngine.stop()
                                                isTestingNatureSound = false
                                            } else {
                                                playbackManager.zenNatureAudioEngine.startNatureSound(smartSleepSound, volume = 0.35f)
                                                isTestingNatureSound = true
                                                Toast.makeText(context, "Воспроизведение: ${smartSleepSound.displayName}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isTestingNatureSound) Color(0xFFFF5555) else Color.White.copy(alpha = 0.12f),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (isTestingNatureSound) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isTestingNatureSound) "Остановить тест звука природы" else "Послушать ${smartSleepSound.displayName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
