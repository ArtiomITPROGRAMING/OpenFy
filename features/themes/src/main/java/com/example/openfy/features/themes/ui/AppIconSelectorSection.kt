package com.example.openfy.features.themes.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.features.themes.engine.AppIconManager
import com.example.openfy.features.themes.engine.AppLauncherIcon

@Composable
fun AppIconSelectorSection(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val currentIconKey by settingsRepository.appLauncherIconKey.collectAsState()
    val activeIcon = AppLauncherIcon.fromKey(currentIconKey)
    val primaryAccent = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "ИКОНКА ПРИЛОЖЕНИЯ В ЛАУНЧЕРЕ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = primaryAccent,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(AppLauncherIcon.entries, key = { it.key }) { icon ->
                val isSelected = activeIcon == icon

                AppIconPreviewCard(
                    icon = icon,
                    isSelected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            AppIconManager.setAppIcon(context, icon)
                            settingsRepository.setAppLauncherIconKey(icon.key)
                            Toast.makeText(context, "Иконка «${icon.title}» применена", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppIconPreviewCard(
    icon: AppLauncherIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryAccent = MaterialTheme.colorScheme.primary

    val (cardBgGradient, innerIconColor, iconVector: ImageVector) = when (icon) {
        AppLauncherIcon.NEON_CORE -> Triple(
            listOf(Color(0xFF002D3A), Color(0xFF08080E)),
            Color(0xFF00E5FF),
            Icons.Default.MusicNote
        )
        AppLauncherIcon.TITANIUM_STUDIO -> Triple(
            listOf(Color(0xFF26262E), Color(0xFF0E0E12)),
            Color.White,
            Icons.Default.MusicNote
        )
        AppLauncherIcon.SPECTRUM_WAVE -> Triple(
            listOf(Color(0xFF26001E), Color(0xFF0A0410)),
            Color(0xFFFF0077),
            Icons.Default.GraphicEq
        )
        AppLauncherIcon.MIDNIGHT_PLAY -> Triple(
            listOf(Color(0xFF1C1A28), Color(0xFF0D0D11)),
            Color(0xFFC084FC),
            Icons.Default.PlayArrow
        )
    }

    GlassCard(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Black.copy(alpha = 0.4f),
        hasGlowBorder = isSelected,
        glowColor = innerIconColor
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon Circle / Squircle Preview
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.radialGradient(cardBgGradient))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) innerIconColor else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Ring
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, innerIconColor.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = innerIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = icon.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = icon.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = innerIconColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, innerIconColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = innerIconColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "АКТИВНА",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = innerIconColor,
                            fontSize = 9.sp
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }
}
