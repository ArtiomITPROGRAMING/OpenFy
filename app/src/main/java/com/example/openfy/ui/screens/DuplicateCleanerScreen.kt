package com.example.openfy.ui.screens

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.DuplicateGroup
import com.example.openfy.core.audio.data.DuplicateScanner
import com.example.openfy.core.audio.model.Song
import com.example.openfy.core.audio.service.PlaybackManager
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.AppIcons
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.core.ui.theme.NeonPink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DuplicateCleanerScreen(
    playbackManager: PlaybackManager,
    allSongs: List<Song>,
    onBack: () -> Unit,
    onRefreshAudio: () -> Unit
) {
    val context = LocalContext.current
    val duplicateScanner = remember { DuplicateScanner(context) }
    val themeStyle by playbackManager.settingsRepository.themeStyle.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()

    var duplicateGroups by remember { mutableStateOf<List<DuplicateGroup>>(emptyList()) }
    var isScanning by remember { mutableStateOf(true) }
    val selectedSongIdsToDelete = remember { mutableStateListOf<Long>() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    fun runScan() {
        isScanning = true
        selectedSongIdsToDelete.clear()
        val groups = duplicateScanner.findDuplicates(allSongs)
        duplicateGroups = groups

        // Auto-select all duplicate copies (except the original 1st item)
        groups.forEach { group ->
            group.songs.drop(1).forEach { song ->
                selectedSongIdsToDelete.add(song.id)
            }
        }
        isScanning = false
    }

    // Android 11+ System Delete Request Intent Launcher
    val deleteRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Файлы успешно удалены", Toast.LENGTH_SHORT).show()
            onRefreshAudio()
            runScan()
        } else {
            Toast.makeText(context, "Удаление отменено пользователем", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(allSongs) {
        withContext(Dispatchers.Default) {
            runScan()
        }
    }

    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBg = if (themeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface

    val totalWastedBytes = remember(duplicateGroups) {
        duplicateGroups.sumOf { it.totalWastedBytes }
    }
    val totalWastedMb = (totalWastedBytes / (1024 * 1024.0)).let { "%.1f".format(it) }

    fun executeDeletion() {
        val songsToDelete = allSongs.filter { selectedSongIdsToDelete.contains(it.id) }
        if (songsToDelete.isEmpty()) return

        // 1. Android 11+ (API 30+) Native MediaStore Delete Request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val uris = songsToDelete.map { it.contentUri }
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                deleteRequestLauncher.launch(intentSenderRequest)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Android 10 / Legacy / Direct File Deletion fallback
        var deletedCount = 0
        for (song in songsToDelete) {
            try {
                val rows = context.contentResolver.delete(song.contentUri, null, null)
                if (rows > 0) {
                    deletedCount++
                } else if (song.path.isNotBlank()) {
                    val file = File(song.path)
                    if (file.exists() && file.delete()) {
                        deletedCount++
                    }
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    val intentSenderRequest = IntentSenderRequest.Builder(e.userAction.actionIntent.intentSender).build()
                    deleteRequestLauncher.launch(intentSenderRequest)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (deletedCount > 0) {
            Toast.makeText(context, "Успешно удалено $deletedCount файлов", Toast.LENGTH_SHORT).show()
            onRefreshAudio()
            runScan()
        } else {
            // Suggest All Files Access on Android 11+ if system restricted direct deletion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !android.os.Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                    Toast.makeText(context, "Предоставьте доступ к файлам для прямого удаления", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Не удалось удалить файлы. Проверьте разрешения в настройках Android", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Не удалось удалить файлы. Проверьте разрешения Android", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DynamicBackground(isPlaying = isPlaying, themeStyle = themeStyle) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Очистка клонов и дубликатов",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Умный поиск похожих и одинаковых треков",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            if (isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сканирование медиатеки на дубликаты...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryAccent
                    )
                }
            } else if (duplicateGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = cardBg
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = AppIcons.check,
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Дубликатов не найдено!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ваша медиатека идеальна: все треки уникальны и не занимают лишней памяти.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary Banner
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = cardBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Найдено групп дубликатов: ${duplicateGroups.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Занимают лишней памяти: ~$totalWastedMb МБ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = primaryAccent
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedSongIdsToDelete.clear()
                                        duplicateGroups.forEach { group ->
                                            group.songs.drop(1).forEach { s ->
                                                selectedSongIdsToDelete.add(s.id)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Выбрать лишние", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Duplicate Groups
                    items(duplicateGroups, key = { it.id }) { group ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = cardBg
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = group.baseTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = group.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = primaryAccent,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                group.songs.forEachIndexed { index, song ->
                                    val isSelected = selectedSongIdsToDelete.contains(song.id)
                                    val fileSizeMb = try {
                                        val length = File(song.path).length()
                                        "%.1f MB".format(length / (1024 * 1024.0))
                                    } catch (e: Exception) {
                                        ""
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) Color(0x33FF0055) else Color.Black.copy(alpha = 0.3f)
                                            )
                                            .clickable {
                                                if (isSelected) selectedSongIdsToDelete.remove(song.id)
                                                else selectedSongIdsToDelete.add(song.id)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedSongIdsToDelete.add(song.id)
                                                else selectedSongIdsToDelete.remove(song.id)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else NeonPink
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (index == 0) "★ Оригинал: ${song.title}" else "Клон: ${song.title}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                                color = if (index == 0) Color.White else Color.White.copy(alpha = 0.85f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${song.formattedDuration} • $fileSizeMb • ${song.path.substringAfterLast("/")}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.6f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }

                // Bottom Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedSongIdsToDelete.isNotEmpty()) {
                                showConfirmDialog = true
                            } else {
                                Toast.makeText(context, "Выберите хотя бы один дубликат для удаления", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.White else NeonPink,
                            contentColor = if (themeStyle == AppThemeStyle.SERIOUS_DARK) Color.Black else Color.White
                        )
                    ) {
                        Text(
                            text = "Удалить выбранные дубликаты (${selectedSongIdsToDelete.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Удаление дубликатов") },
                text = {
                    Text("Удалить ${selectedSongIdsToDelete.size} выбранных копий файлов с устройства?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            executeDeletion()
                        }
                    ) {
                        Text("Удалить", color = NeonPink, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
