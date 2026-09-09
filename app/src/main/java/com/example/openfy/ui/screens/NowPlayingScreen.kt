package com.example.openfy.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.openfy.features.community.sync.SharePayload
import com.example.openfy.features.community.sync.ShareType
import com.example.openfy.features.community.ui.ShareBottomSheet
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.palette.graphics.Palette
import com.example.openfy.core.ui.components.VisualizerCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.PlayerCoverStyle
import com.example.openfy.core.audio.data.ProgressBarStyle
import com.example.openfy.core.audio.model.RepeatMode as AppRepeatMode
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.AddToPlaylistBottomSheet
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.components.PlayerCustomizationBottomSheet
import com.example.openfy.core.ui.components.SongListItem
import com.example.openfy.core.ui.components.WaveformSlider
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.CyberpunkDarkBg
import com.example.openfy.core.ui.theme.CyberpunkRubyRed
import com.example.openfy.core.ui.theme.ElectricPurple
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonCyan
import com.example.openfy.core.ui.theme.NeonPink
import com.example.openfy.core.ui.theme.RetroDarkBg
import com.example.openfy.core.ui.theme.RetroPhosphorGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playbackManager: PlaybackManager,
    onDismiss: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    onNavigateToCarMode: () -> Unit = {}
) {
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val isShuffle by playbackManager.isShuffle.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()
    val queue by playbackManager.queue.collectAsState()
    val currentIndex by playbackManager.currentIndex.collectAsState()
    val favorites by playbackManager.playlistRepository.favorites.collectAsState()
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val iconPackStyle by playbackManager.settingsRepository.iconPackStyle.collectAsState()
    val sleepTimerSeconds by playbackManager.sleepTimerSecondsLeft.collectAsState()

    val progressBarStyle by playbackManager.settingsRepository.progressBarStyle.collectAsState()
    val playerCoverStyle by playbackManager.settingsRepository.playerCoverStyle.collectAsState()

    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showCustomizationSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }

    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sleepTimerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val customizationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (currentSong == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Ничего не воспроизводится", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val song = currentSong!!
    val isFavorite = favorites.contains(song.id)
    val hapticFeedback = LocalHapticFeedback.current

    var dragOffsetAccumulator by remember { mutableFloatStateOf(0f) }

    val primaryAccent = MaterialTheme.colorScheme.primary
    val dominantColor = rememberDominantColor(song.albumArtUriString, primaryAccent)

    // Smooth Album Art Scaling when playing vs paused (1.0f vs 0.88f)
    val albumScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.88f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "AlbumScale"
    )

    // Spring Bouncy Micro-animations
    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "PlayScale"
    )
    val favScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "FavScale"
    )
    val shuffleScale by animateFloatAsState(
        targetValue = if (isShuffle) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ShuffleScale"
    )
    val repeatScale by animateFloatAsState(
        targetValue = if (repeatMode != AppRepeatMode.OFF) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "RepeatScale"
    )

    // Pulsing breathing animation for album art glow
    val infiniteTransition = rememberInfiniteTransition(label = "NowPlayingGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    // Vinyl Rotation Animation
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VinylRotation"
    )

    val artworkCardBg = when (themeStyle) {
        AppThemeStyle.SERIOUS_DARK -> AmoledDarkSurface
        AppThemeStyle.MIDNIGHT_NEON -> GlassDarkSurface
        AppThemeStyle.CYBERPUNK_BLOOD -> CyberpunkDarkBg
        AppThemeStyle.RETRO_PIXEL -> RetroDarkBg
        AppThemeStyle.MATERIAL_YOU -> MaterialTheme.colorScheme.surface
    }

    DynamicBackground(isPlaying = isPlaying, themeStyle = themeStyle, accentColor = dominantColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // TOP BAR: Collapse Button, Title, and Player Customization (Settings)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "СЕЙЧАС ИГРАЕТ",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = song.album.ifBlank { "Локальный трек" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Top Right: Player Customization Button (Replaced EQ here)
                IconButton(onClick = { showCustomizationSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Персонализация плеера",
                        tint = primaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // HERO ALBUM ARTWORK (CUSTOMIZABLE STYLES WITH HORIZONTAL SWIPE GESTURES)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(albumScale)
                    .padding(8.dp)
                    .pointerInput(song.id) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragOffsetAccumulator > 75f) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    playbackManager.skipPrev()
                                } else if (dragOffsetAccumulator < -75f) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    playbackManager.skipNext()
                                }
                                dragOffsetAccumulator = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffsetAccumulator += dragAmount
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                playbackManager.playPause()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                when (playerCoverStyle) {
                    PlayerCoverStyle.SPINNING_VINYL -> {
                        // Spinning Vinyl Disc
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(if (isPlaying) vinylRotation else 0f)
                                .clip(CircleShape)
                                .background(Color(0xFF0F1014))
                                .border(6.dp, Color(0xFF20222A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Grooves
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val r = size.minDimension / 2f
                                drawCircle(Color(0xFF282A34), radius = r * 0.85f, style = Stroke(1.5f))
                                drawCircle(Color(0xFF1E2028), radius = r * 0.70f, style = Stroke(1.5f))
                                drawCircle(Color(0xFF282A34), radius = r * 0.55f, style = Stroke(1.5f))
                            }

                            // Center Label Thumbnail
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.48f)
                                    .clip(CircleShape)
                                    .border(3.dp, primaryAccent, CircleShape)
                            ) {
                                if (song.albumArtUriString != null) {
                                    AsyncImage(
                                        model = song.albumArtUri,
                                        contentDescription = song.album,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(primaryAccent.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(AppIcons.music(iconPackStyle), contentDescription = null, tint = primaryAccent, modifier = Modifier.size(36.dp))
                                    }
                                }
                            }

                            // Spindle Hole
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                    }

                    PlayerCoverStyle.CYBER_CASSETTE -> {
                        // Retro Cyber Cassette Tape
                        GlassCard(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = Color(0xFF14151C),
                            hasGlowBorder = themeStyle.hasNeonGlow,
                            glowColor = primaryAccent
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "OPENFY HI-FI CASSETTE 90",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent,
                                    letterSpacing = 1.sp
                                )

                                // Center Spools Window
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left Reel
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .rotate(if (isPlaying) vinylRotation else 0f)
                                                .clip(CircleShape)
                                                .background(Color(0xFF282A36))
                                                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.Black))
                                        }

                                        // Tape Strip
                                        Box(modifier = Modifier.weight(1f).height(12.dp).background(Color(0xFF3B2818)))

                                        // Right Reel
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .rotate(if (isPlaying) vinylRotation else 0f)
                                                .clip(CircleShape)
                                                .background(Color(0xFF282A36))
                                                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.Black))
                                        }
                                    }
                                }

                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    else -> {
                        // ROUNDED_CARD & DYNAMIC_GLOW
                        if (themeStyle.hasNeonGlow || playerCoverStyle == PlayerCoverStyle.DYNAMIC_GLOW) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.92f)
                                    .scale(glowScale)
                                    .blur(40.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                primaryAccent.copy(alpha = 0.55f),
                                                ElectricPurple.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(30.dp),
                            backgroundColor = artworkCardBg,
                            hasGlowBorder = themeStyle.hasNeonGlow,
                            glowColor = primaryAccent
                        ) {
                            if (song.albumArtUriString != null) {
                                AsyncImage(
                                    model = song.albumArtUri,
                                    contentDescription = song.album,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            when (themeStyle) {
                                                AppThemeStyle.SERIOUS_DARK -> Brush.linearGradient(listOf(Color(0xFF282932), Color(0xFF101115)))
                                                AppThemeStyle.CYBERPUNK_BLOOD -> Brush.linearGradient(listOf(CyberpunkRubyRed.copy(alpha = 0.3f), Color(0xFF2E0914)))
                                                AppThemeStyle.RETRO_PIXEL -> Brush.linearGradient(listOf(Color(0xFF0F331D), Color(0xFF06150C)))
                                                else -> Brush.linearGradient(listOf(primaryAccent.copy(alpha = 0.25f), ElectricPurple.copy(alpha = 0.25f)))
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = AppIcons.music(iconPackStyle),
                                        contentDescription = "Placeholder",
                                        tint = primaryAccent,
                                        modifier = Modifier.size(96.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Track Info, Add to Playlist & Favorite Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .basicMarquee(iterations = Int.MAX_VALUE)
                        )
                        if (isPlaying) {
                            Spacer(modifier = Modifier.width(8.dp))
                            VisualizerCanvas(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(16.dp),
                                isPlaying = true,
                                barCount = 5,
                                primaryColor = primaryAccent,
                                secondaryColor = dominantColor
                            )
                        }
                    }
                    if (song.isStream) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ONLINE STREAM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF),
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Add To Playlist Button
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            songForAddToPlaylist = song
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = "Добавить в плейлист",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Favorite Button with Spring Scale
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            playbackManager.playlistRepository.toggleFavorite(song.id)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(favScale)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) AppIcons.favoriteFilled(iconPackStyle) else AppIcons.favoriteOutline(iconPackStyle),
                            contentDescription = "Favorite",
                            tint = if (isFavorite) (if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else NeonPink) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // PROGRESS BAR (CUSTOMIZABLE STYLES)
            // ==========================================
            when (progressBarStyle) {
                ProgressBarStyle.CLASSIC_LINE -> {
                    ClassicLineSlider(
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onSeek = { playbackManager.seekTo(it) },
                        activeColor = primaryAccent
                    )
                }
                ProgressBarStyle.LASER_GLOW -> {
                    LaserGlowSlider(
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onSeek = { playbackManager.seekTo(it) },
                        activeColor = primaryAccent
                    )
                }
                ProgressBarStyle.CHUNKY_PILL -> {
                    ChunkyPillSlider(
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onSeek = { playbackManager.seekTo(it) },
                        activeColor = primaryAccent
                    )
                }
                else -> {
                    // WAVEFORM_BARS (Default)
                    WaveformSlider(
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onSeek = { playbackManager.seekTo(it) },
                        activeColor = primaryAccent,
                        inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        playbackManager.toggleShuffle()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .scale(shuffleScale)
                ) {
                    Icon(
                        imageVector = AppIcons.shuffle(iconPackStyle),
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous track
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        playbackManager.skipPrev()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.previous(iconPackStyle),
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play / Pause FAB with Spring Scale & Ambient Glow
                FilledIconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        playbackManager.playPause()
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .scale(playScale)
                        .shadow(16.dp, CircleShape, spotColor = primaryAccent.copy(alpha = 0.5f)),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = primaryAccent,
                        contentColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK || themeStyle == AppThemeStyle.RETRO_PIXEL) Color.Black else Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) AppIcons.pause(iconPackStyle) else AppIcons.play(iconPackStyle),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next track
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        playbackManager.skipNext()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.next(iconPackStyle),
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        playbackManager.toggleRepeatMode()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .scale(repeatScale)
                ) {
                    val repeatIcon = when (repeatMode) {
                        AppRepeatMode.ONE -> AppIcons.repeatOne(iconPackStyle)
                        else -> AppIcons.repeat(iconPackStyle)
                    }
                    val isRepeatActive = repeatMode != AppRepeatMode.OFF
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = if (isRepeatActive) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // MODERN FLOATING BOTTOM ACTION DOCK
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerDockItem(
                        icon = AppIcons.lyrics(iconPackStyle),
                        label = "Текст",
                        isActive = false,
                        accentColor = primaryAccent,
                        onClick = onNavigateToLyrics
                    )

                    PlayerDockItem(
                        icon = Icons.Default.GraphicEq,
                        label = "DSP",
                        isActive = false,
                        accentColor = primaryAccent,
                        onClick = onNavigateToEqualizer
                    )

                    PlayerDockItem(
                        icon = AppIcons.queue(iconPackStyle),
                        label = "${queue.size}",
                        isActive = false,
                        accentColor = primaryAccent,
                        onClick = { showQueueSheet = true }
                    )

                    PlayerDockItem(
                        icon = Icons.Default.Timer,
                        label = if (sleepTimerSeconds != null) "${sleepTimerSeconds!! / 60}м" else "Сон",
                        isActive = sleepTimerSeconds != null,
                        accentColor = primaryAccent,
                        onClick = { showSleepTimerSheet = true }
                    )

                    PlayerDockItem(
                        icon = Icons.Default.DirectionsCar,
                        label = "Авто",
                        isActive = false,
                        accentColor = primaryAccent,
                        onClick = onNavigateToCarMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Spacer(modifier = Modifier.height(10.dp))
        }

        // ==========================================
        // PLAYER CUSTOMIZATION BOTTOM SHEET
        // ==========================================
        if (showCustomizationSheet) {
            PlayerCustomizationBottomSheet(
                playbackManager = playbackManager,
                sheetState = customizationSheetState,
                onDismiss = { showCustomizationSheet = false }
            )
        }

        // ==========================================
        // ADD TO PLAYLIST BOTTOM SHEET
        // ==========================================
        if (songForAddToPlaylist != null) {
            AddToPlaylistBottomSheet(
                song = songForAddToPlaylist!!,
                playbackManager = playbackManager,
                onDismiss = { songForAddToPlaylist = null }
            )
        }

        // ==========================================
        // SLEEP TIMER BOTTOM SHEET
        // ==========================================
        if (showSleepTimerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSleepTimerSheet = false },
                sheetState = sleepTimerSheetState,
                containerColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color(0xFF121218) else Color(0xFF181A26),
                dragHandle = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Таймер сна",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showSleepTimerSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (sleepTimerSeconds != null) {
                            if (sleepTimerSeconds == -1) "Музыка выключится после окончания текущего трека"
                            else "Осталось времени: ${sleepTimerSeconds!! / 60} мин ${sleepTimerSeconds!! % 60} сек"
                        } else "Таймер сна выключен",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (sleepTimerSeconds != null) primaryAccent else Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val presets = listOf(5, 10, 15, 30, 45, 60, 90, 120)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presets) { min ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    playbackManager.startSleepTimer(min)
                                    showSleepTimerSheet = false
                                },
                                label = { Text("$min мин") },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stop after current song
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                playbackManager.setSleepTimerAfterCurrentTrack()
                                showSleepTimerSheet = false
                            },
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color.White.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "В конце этого трека",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "С плавным затуханием в конце композиции",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Icon(
                                imageVector = AppIcons.play(iconPackStyle),
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (sleepTimerSeconds != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                playbackManager.cancelSleepTimer()
                                showSleepTimerSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Отключить таймер", color = Color(0xFFFF5555))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // ==========================================
        // QUEUE BOTTOM SHEET
        // ==========================================
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = queueSheetState,
                containerColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color(0xFF101015) else Color(0xFF141622),
                dragHandle = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Очередь воспроизведения",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${queue.size} треков • Сейчас играет ${currentIndex + 1}-й",
                                style = MaterialTheme.typography.bodySmall,
                                color = primaryAccent
                            )
                        }

                        IconButton(onClick = { showQueueSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(queue) { idx, itemSong ->
                            val isItemPlaying = idx == currentIndex
                            SongListItem(
                                song = itemSong,
                                isPlaying = isItemPlaying,
                                isFavorite = favorites.contains(itemSong.id),
                                iconPackStyle = iconPackStyle,
                                onClick = {
                                    playbackManager.playSongFromList(queue, itemSong)
                                },
                                onFavoriteToggle = {
                                    playbackManager.playlistRepository.toggleFavorite(itemSong.id)
                                },
                                onAddToPlaylist = {
                                    songForAddToPlaylist = itemSong
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // SHARE TRACK BOTTOM SHEET
        // ==========================================
        if (showShareSheet) {
            val trackPayload = remember(song) {
                val jsonObject = buildJsonObject {
                    put("id", JsonPrimitive(song.id))
                    put("title", JsonPrimitive(song.title))
                    put("artist", JsonPrimitive(song.artist))
                    put("album", JsonPrimitive(song.album))
                    put("durationMs", JsonPrimitive(song.durationMs))
                }
                SharePayload(
                    type = ShareType.TRACK_META,
                    title = "${song.artist} - ${song.title}",
                    description = "Трек OpenFy • ${song.album.ifBlank { "Сингл" }}",
                    jsonData = jsonObject.toString()
                )
            }
            ShareBottomSheet(
                payload = trackPayload,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

@Composable
private fun rememberDominantColor(uriString: String?, defaultColor: Color): Color {
    val context = LocalContext.current
    var dominantColor by remember(uriString) { mutableStateOf(defaultColor) }

    LaunchedEffect(uriString) {
        if (uriString != null) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                        val bitmap = BitmapFactory.decodeStream(stream, null, options)
                        if (bitmap != null) {
                            val palette = Palette.from(bitmap).generate()
                            val colorInt = palette.getVibrantColor(
                                palette.getDominantColor(
                                    palette.getMutedColor(defaultColor.toArgb())
                                )
                            )
                            withContext(Dispatchers.Main) {
                                dominantColor = Color(colorInt)
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        } else {
            dominantColor = defaultColor
        }
    }
    return dominantColor
}

@Composable
private fun ClassicLineSlider(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    activeColor: Color
) {
    val hapticFeedback = LocalHapticFeedback.current
    val totalMs = durationMs.coerceAtLeast(1L)
    val progress = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val effectiveProgress = if (isDragging) dragProgress else progress
    val displayedMs = if (isDragging) (dragProgress * totalMs).toLong() else currentPositionMs

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = effectiveProgress,
            onValueChange = {
                isDragging = true
                dragProgress = it
            },
            onValueChangeFinished = {
                isDragging = false
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onSeek((dragProgress * totalMs).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(displayedMs),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDragging) activeColor else Color.White.copy(alpha = 0.7f),
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal
            )
            Text(formatTime(durationMs), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun LaserGlowSlider(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    activeColor: Color
) {
    val hapticFeedback = LocalHapticFeedback.current
    val totalMs = durationMs.coerceAtLeast(1L)
    val progress = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val effectiveProgress = if (isDragging) dragProgress else progress
    val displayedMs = if (isDragging) (dragProgress * totalMs).toLong() else currentPositionMs

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeek((ratio * totalMs).toLong())
                    }
                }
                .pointerInput(totalMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = {
                            isDragging = false
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSeek((dragProgress * totalMs).toLong())
                        },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                val w = size.width
                val midY = size.height / 2f
                val activeW = w * effectiveProgress

                // Inactive track
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Laser glow layer
                drawLine(
                    color = activeColor.copy(alpha = 0.35f),
                    start = Offset(0f, midY),
                    end = Offset(activeW, midY),
                    strokeWidth = if (isDragging) 12.dp.toPx() else 8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Laser core line
                drawLine(
                    color = activeColor,
                    start = Offset(0f, midY),
                    end = Offset(activeW, midY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Glowing Laser Head
                drawCircle(color = activeColor.copy(alpha = 0.4f), radius = if (isDragging) 12.dp.toPx() else 9.dp.toPx(), center = Offset(activeW, midY))
                drawCircle(color = Color.White, radius = if (isDragging) 6.dp.toPx() else 4.5.dp.toPx(), center = Offset(activeW, midY))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(displayedMs),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDragging) activeColor else Color.White.copy(alpha = 0.7f),
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal
            )
            Text(formatTime(durationMs), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun ChunkyPillSlider(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    activeColor: Color
) {
    val hapticFeedback = LocalHapticFeedback.current
    val totalMs = durationMs.coerceAtLeast(1L)
    val progress = (currentPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val effectiveProgress = if (isDragging) dragProgress else progress
    val displayedMs = if (isDragging) (dragProgress * totalMs).toLong() else currentPositionMs

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeek((ratio * totalMs).toLong())
                    }
                }
                .pointerInput(totalMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = {
                            isDragging = false
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSeek((dragProgress * totalMs).toLong())
                        },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(effectiveProgress)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(activeColor.copy(alpha = 0.7f), activeColor)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(displayedMs),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDragging) activeColor else Color.White.copy(alpha = 0.7f),
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal
            )
            Text(formatTime(durationMs), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun PlayerDockItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(if (isActive) accentColor.copy(alpha = 0.22f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) accentColor else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) accentColor else Color.White.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

