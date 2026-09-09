package com.example.openfy.features.community.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import com.example.openfy.features.community.sync.SharePayload
import com.example.openfy.features.community.sync.ShareType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.openfy.core.audio.data.AppThemeStyle
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.AmoledDarkSurface
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.features.community.model.UserProfile

val DiscordBlurple = Color(0xFF5865F2)
val GitHubDark = Color(0xFF24292F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityProfileScreen(
    viewModel: ProfileViewModel,
    settingsRepository: SettingsRepository,
    onBack: () -> Unit,
    onNavigateToQrScanner: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentThemeStyle by settingsRepository.themeStyle.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showP2pActionDialog by remember { mutableStateOf(false) }
    var showShareProfileSheet by remember { mutableStateOf(false) }
    var guestNickname by remember { mutableStateOf("") }
    var showGuestDialog by remember { mutableStateOf(false) }
    var showPatDialog by remember { mutableStateOf(false) }
    var patTokenInput by remember { mutableStateOf("") }

    val primaryAccent = MaterialTheme.colorScheme.primary
    val cardBg = if (currentThemeStyle == AppThemeStyle.SERIOUS_DARK) AmoledDarkSurface else GlassDarkSurface

    DynamicBackground(themeStyle = currentThemeStyle) {
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
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Профиль & Сообщество",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "P2P Синхронизация, обмен темами и плейлистами",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateToQrScanner) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Сканер QR-кода",
                        tint = primaryAccent
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = primaryAccent)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Авторизация и синхронизация...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    is ProfileUiState.Authorized -> {
                        val profile = state.profile

                        // User Profile Header Card
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = cardBg,
                                hasGlowBorder = currentThemeStyle.hasNeonGlow,
                                glowColor = primaryAccent
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Avatar
                                    if (profile.avatarUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = profile.avatarUrl,
                                            contentDescription = "Аватар пользователя",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, primaryAccent, CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(primaryAccent.copy(alpha = 0.2f))
                                                .border(2.dp, primaryAccent, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = primaryAccent,
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = profile.displayName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Provider Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (profile) {
                                            is UserProfile.GitHub -> GitHubDark
                                            is UserProfile.Discord -> DiscordBlurple
                                            is UserProfile.Email -> primaryAccent
                                            is UserProfile.Guest -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (profile) {
                                                    is UserProfile.GitHub -> Icons.Default.Code
                                                    is UserProfile.Discord -> Icons.Default.Group
                                                    is UserProfile.Email -> Icons.Default.Email
                                                    is UserProfile.Guest -> Icons.Default.AccountCircle
                                                },
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = profile.providerName.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ID: ${profile.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // Community Statistics Card
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = cardBg
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "СТАТИСТИКА СООБЩЕСТВА",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryAccent,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    ProfileStatRow(
                                        icon = Icons.Default.Palette,
                                        title = "Создано / Импортировано тем",
                                        value = "Локально"
                                    )
                                    ProfileStatRow(
                                        icon = Icons.Default.Share,
                                        title = "P2P Передача треков & плейлистов",
                                        value = "Открыть сканер / Обмен",
                                        onClick = { showP2pActionDialog = true }
                                    )
                                    ProfileStatRow(
                                        icon = Icons.Default.Lock,
                                        title = "Приватность аккаунта",
                                        value = "100% Offline Encrypted"
                                    )
                                }
                            }
                        }

                        // Logout Button
                        item {
                            OutlinedButton(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Выйти из аккаунта",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    is ProfileUiState.Unauthenticated, is ProfileUiState.Error -> {
                        if (state is ProfileUiState.Error) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }

                        // Welcome Hero Card
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = cardBg,
                                hasGlowBorder = currentThemeStyle.hasNeonGlow,
                                glowColor = primaryAccent
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Добро пожаловать в OpenFy Community!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Авторизуйтесь через GitHub или Discord, чтобы обмениваться темами оформления, делиться плейлистами без интернета и участвовать в развитии плеера.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // OAuth Buttons Section
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // GitHub Login Button
                                Button(
                                    onClick = { viewModel.loginWithGitHub(context) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GitHubDark,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Войти через GitHub",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Discord Login Button
                                Button(
                                    onClick = { viewModel.loginWithDiscord(context) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DiscordBlurple,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Войти через Discord",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // GitHub Personal Access Token (PAT) Login Button
                                TextButton(
                                    onClick = { showPatDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Войти по Personal Access Token (PAT)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = primaryAccent
                                    )
                                }

                                // Continue as Guest Button
                                OutlinedButton(
                                    onClick = { showGuestDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Продолжить как Гость (Офлайн)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // P2P Quick Actions Card
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = cardBg,
                                hasGlowBorder = currentThemeStyle.hasNeonGlow,
                                glowColor = primaryAccent
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "P2P ПЕРЕДАЧА ТРЕКОВ & ПЛЕЙЛИСТОВ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryAccent,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Мгновенный офлайн-обмен плейлистами, темами оформления (.thm) и треками через QR-код или локальный Wi-Fi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = onNavigateToQrScanner,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Сканер QR", fontSize = 13.sp, maxLines = 1)
                                        }

                                        OutlinedButton(
                                            onClick = { showShareProfileSheet = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Поделиться", fontSize = 13.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // GitHub PAT Dialog
    if (showPatDialog) {
        AlertDialog(
            onDismissRequest = { showPatDialog = false },
            title = { Text("Вход по токену GitHub") },
            text = {
                Column {
                    Text(
                        text = "Вставьте GitHub Personal Access Token (Classic или Fine-grained с правом read:user). 100% локально и безопасно.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = patTokenInput,
                        onValueChange = { patTokenInput = it },
                        placeholder = { Text("ghp_... или github_pat_...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loginWithGitHubPAT(patTokenInput)
                        showPatDialog = false
                    }
                ) {
                    Text("Войти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPatDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Guest Nickname Dialog
    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text("Имя гостя") },
            text = {
                Column {
                    Text(
                        text = "Введите имя пользователя для локального профиля и P2P-обмена:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = guestNickname,
                        onValueChange = { guestNickname = it },
                        placeholder = { Text("Например: Меломан") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.continueAsGuest(guestNickname)
                        showGuestDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // P2P Action Selection Dialog
    if (showP2pActionDialog) {
        AlertDialog(
            onDismissRequest = { showP2pActionDialog = false },
            title = { Text("P2P Синхронизация") },
            text = { Text("Выберите действие для обмена данными: запустить сканер QR-кода для импорта или поделиться своим профилем.") },
            confirmButton = {
                Button(
                    onClick = {
                        showP2pActionDialog = false
                        onNavigateToQrScanner()
                    }
                ) {
                    Text("Сканировать QR")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showP2pActionDialog = false
                        showShareProfileSheet = true
                    }
                ) {
                    Text("Поделиться профилем")
                }
            }
        )
    }

    // P2P Share Profile BottomSheet
    if (showShareProfileSheet) {
        val nickname = when (val state = uiState) {
            is ProfileUiState.Authorized -> state.profile.displayName
            else -> "OpenFy User"
        }
        val profilePayload = remember(nickname) {
            val jsonObject = buildJsonObject {
                put("nickname", JsonPrimitive(nickname))
                put("version", JsonPrimitive("OpenFy 1.0"))
            }
            SharePayload(
                type = ShareType.TRACK_META,
                title = "OpenFy Profile - $nickname",
                description = "Профиль OpenFy P2P Community",
                jsonData = jsonObject.toString(),
                author = nickname
            )
        }
        ShareBottomSheet(
            payload = profilePayload,
            onDismiss = { showShareProfileSheet = false }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Вы уверены, что хотите выйти? Локальные сохраненные ключи будут очищены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ProfileStatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                else Modifier.padding(vertical = 6.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
