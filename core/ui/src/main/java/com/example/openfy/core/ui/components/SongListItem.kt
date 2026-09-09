package com.example.openfy.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.openfy.core.audio.data.IconPackStyle
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.ui.theme.AppIcons

@Composable
fun SongListItem(
    modifier: Modifier = Modifier,
    song: Song,
    isPlaying: Boolean = false,
    isFavorite: Boolean = false,
    iconPackStyle: IconPackStyle = IconPackStyle.MINIMAL_THIN,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onDeleteFromDevice: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val primaryAccent = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(320),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(480),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val itemShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(itemShape)
            .background(
                if (isPlaying) primaryAccent.copy(alpha = 0.12f) else Color.Transparent
            )
            .then(
                if (isPlaying) Modifier.border(1.5.dp, primaryAccent, itemShape) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUriString != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.album,
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = AppIcons.music(iconPackStyle),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(bar1Height.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryAccent)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(bar2Height.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryAccent)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(bar3Height.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryAccent)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    Text(
                        text = "▶ ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent
                    )
                }
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                    color = if (isPlaying) primaryAccent else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${song.artist} • ${song.album}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPlaying) primaryAccent.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = song.formattedDuration,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
            color = if (isPlaying) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) AppIcons.favoriteFilled(iconPackStyle) else AppIcons.favoriteOutline(iconPackStyle),
                contentDescription = "Favorite",
                tint = if (isFavorite) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = AppIcons.more,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("В плейлист") },
                    onClick = {
                        showMenu = false
                        onAddToPlaylist()
                    },
                    leadingIcon = {
                        Icon(AppIcons.playlist(iconPackStyle), contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Удалить из любимых" else "В любимые") },
                    onClick = {
                        showMenu = false
                        onFavoriteToggle()
                    },
                    leadingIcon = {
                        Icon(if (isFavorite) AppIcons.favoriteFilled(iconPackStyle) else AppIcons.favoriteOutline(iconPackStyle), contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Удалить с устройства", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        onDeleteFromDevice()
                    },
                    leadingIcon = {
                        Icon(AppIcons.clear, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                )
            }
        }
    }
}
