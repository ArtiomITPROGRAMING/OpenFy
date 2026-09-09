package com.example.openfy.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.IconPackStyle
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.RetroDarkBg
import com.example.openfy.core.ui.theme.RetroPhosphorGreen
import kotlin.math.abs

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    themeStyle: AppThemeStyle = AppThemeStyle.SERIOUS_DARK,
    iconPackStyle: IconPackStyle = IconPackStyle.MINIMAL_THIN,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit = {},
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = song != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (song == null) return@AnimatedVisibility

        val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

        val accentColor = when (themeStyle) {
            AppThemeStyle.SERIOUS_DARK -> Color.White
            AppThemeStyle.MIDNIGHT_NEON -> NeonCyan
            AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkRubyRed
            AppThemeStyle.RETRO_PIXEL -> RetroPhosphorGreen
            AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.primary
        }

        val cardBg = when (themeStyle) {
            AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface
            AppThemeStyle.MIDNIGHT_NEON -> GlassDarkSurface
            AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
            AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
            AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.surface
        }

        val placeholderBrush = when (themeStyle) {
            AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(listOf(Color(0xFF282932), Color(0xFF121318)))
            AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(listOf(CyberpunkRubyRed.copy(alpha = 0.4f), Color(0xFF2E0914)))
            AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(listOf(Color(0xFF0F331D), Color(0xFF06150C)))
            else -> Brush.linearGradient(listOf(accentColor.copy(alpha = 0.3f), ElectricPurple.copy(alpha = 0.3f)))
        }

        // Gesture state tracking for Swipe Up, Swipe Left, Swipe Right
        var totalDragX by remember { mutableFloatStateOf(0f) }
        var totalDragY by remember { mutableFloatStateOf(0f) }

        // Spring scale animation for Play/Pause button
        val playButtonScale by animateFloatAsState(
            targetValue = if (isPlaying) 1.0f else 0.95f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "playScale"
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .pointerInput(song.id) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        },
                        onDragEnd = {
                            if (totalDragY < -100f && abs(totalDragY) > abs(totalDragX)) {
                                // Swipe Up -> Open NowPlaying
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClick()
                            } else if (totalDragX < -120f) {
                                // Swipe Left -> Next Track
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSkipNext()
                            } else if (totalDragX > 120f) {
                                // Swipe Right -> Prev Track
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSkipPrev()
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            backgroundColor = cardBg,
            hasGlowBorder = themeStyle.hasNeonGlow,
            glowColor = accentColor.copy(alpha = 0.5f),
            onClick = onClick,
            onLongClick = onLongClick
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Ultra-thin top progress line (1.5dp)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp),
                    color = accentColor,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Art
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(placeholderBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        if (song.albumArtUriString != null) {
                            AsyncImage(
                                model = song.albumArtUri,
                                contentDescription = song.album,
                                modifier = Modifier.size(46.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = AppIcons.music(iconPackStyle),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist with Marquee
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (song.isStream) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF00E5FF).copy(alpha = 0.22f),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "STREAM",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF),
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Real-time Mini Audio Visualizer Wave
                    if (isPlaying) {
                        VisualizerCanvas(
                            modifier = Modifier
                                .width(22.dp)
                                .height(16.dp),
                            isPlaying = true,
                            barCount = 4,
                            primaryColor = accentColor,
                            secondaryColor = accentColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Play/Pause Button with Spring Scale
                    FilledIconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onPlayPause()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .scale(playButtonScale),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = accentColor,
                            contentColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK || themeStyle == AppThemeStyle.RETRO_PIXEL) Color.Black else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) AppIcons.pause(iconPackStyle) else AppIcons.play(iconPackStyle),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Next Track Button
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSkipNext()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.next(iconPackStyle),
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
