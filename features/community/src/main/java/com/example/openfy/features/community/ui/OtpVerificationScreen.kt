package com.example.openfy.features.community.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openfy.core.audio.data.SettingsRepository
import com.example.openfy.core.ui.components.DynamicBackground
import com.example.openfy.core.ui.components.GlassCard
import com.example.openfy.core.ui.theme.GlassDarkSurface
import com.example.openfy.features.community.auth.EmailAuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OtpVerificationScreen(
    email: String,
    viewModel: ProfileViewModel,
    settingsRepository: SettingsRepository,
    onVerificationSuccess: () -> Unit,
    onChangeEmail: () -> Unit
) {
    val themeStyle by settingsRepository.themeStyle.collectAsState()
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendFeedback by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 60-second countdown timer for resending OTP
    var cooldownSeconds by remember {
        val initial = EmailAuthManager.getRemainingCooldown(email)
        mutableIntStateOf(if (initial > 0) initial else 60)
    }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds--
        }
    }

    // Auto-focus the OTP input field on entry
    LaunchedEffect(Unit) {
        delay(300L)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    fun submitVerification(code: String) {
        if (code.length == 6 && !isVerifying) {
            isVerifying = true
            errorMessage = null
            focusManager.clearFocus()
            viewModel.verifyEmailOtp(
                email = email,
                code = code,
                onSuccess = {
                    isVerifying = false
                    onVerificationSuccess()
                },
                onError = { err ->
                    isVerifying = false
                    errorMessage = err
                }
            )
        }
    }

    val primaryAccent = MaterialTheme.colorScheme.primary
    val neonGlow = Color(0xFF00E5FF)
    val neonPurple = Color(0xFFB388FF)

    DynamicBackground(themeStyle = themeStyle) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar with Back / Change Email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onChangeEmail) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад к вводу Email",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(onClick = onChangeEmail) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = primaryAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Изменить Email",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryAccent
                        )
                    }
                }

                // Middle Card with Code Inputs
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(20.dp, shape = CircleShape, spotColor = neonGlow, ambientColor = neonPurple)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(neonGlow, primaryAccent)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = neonGlow,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Код подтверждения",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Мы отправили 6-значный код на адрес:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Glass Card with 6-digit OTP fields
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = GlassDarkSurface.copy(alpha = 0.85f),
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Hidden TextField that captures key events
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BasicTextField(
                                    value = otpCode,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }.take(6)
                                        otpCode = filtered
                                        errorMessage = null
                                        if (filtered.length == 6) {
                                            submitVerification(filtered)
                                        }
                                    },
                                    modifier = Modifier
                                        .focusRequester(focusRequester)
                                        .size(1.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (otpCode.length == 6) submitVerification(otpCode)
                                        }
                                    )
                                )

                                // Visual 6 Digit Cells
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            focusRequester.requestFocus()
                                        }
                                ) {
                                    for (i in 0 until 6) {
                                        val char = otpCode.getOrNull(i)?.toString() ?: ""
                                        val isFocused = otpCode.length == i || (i == 5 && otpCode.length == 6)

                                        OtpDigitBox(
                                            digit = char,
                                            isFocused = isFocused,
                                            isError = errorMessage != null,
                                            primaryAccent = primaryAccent
                                        )
                                    }
                                }
                            }

                            // Error Message
                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Resend Feedback Text
                            if (resendFeedback != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = resendFeedback ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Submit Verification Button
                            Button(
                                onClick = { submitVerification(otpCode) },
                                enabled = otpCode.length == 6 && !isVerifying,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryAccent,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Проверка...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Подтвердить и войти",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Resend Timer & Action
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    if (cooldownSeconds > 0) {
                        Text(
                            text = "Повторно отправить код через ${String.format("%02d:%02d", cooldownSeconds / 60, cooldownSeconds % 60)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    } else {
                        TextButton(
                            onClick = {
                                isResending = true
                                resendFeedback = null
                                errorMessage = null
                                viewModel.resendEmailOtp(
                                    email = email,
                                    onSuccess = {
                                        isResending = false
                                        cooldownSeconds = 60
                                        resendFeedback = "Новый код отправлен!"
                                    },
                                    onError = { err ->
                                        isResending = false
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isResending
                        ) {
                            if (isResending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = primaryAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = primaryAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "Отправить код повторно",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    isError: Boolean,
    primaryAccent: Color
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> primaryAccent
        digit.isNotEmpty() -> primaryAccent.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val bgColor = when {
        isFocused -> primaryAccent.copy(alpha = 0.12f)
        digit.isNotEmpty() -> primaryAccent.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )
    }
}
