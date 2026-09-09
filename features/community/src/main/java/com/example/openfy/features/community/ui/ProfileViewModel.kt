package com.example.openfy.features.community.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openfy.features.community.auth.DiscordAuthManager
import com.example.openfy.features.community.auth.GitHubAuthManager
import com.example.openfy.features.community.model.UserProfile
import com.example.openfy.features.community.storage.AuthStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Unauthenticated : ProfileUiState
    data class Authorized(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    val authStorage: AuthStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val isAuthenticated: Boolean
        get() = authStorage.isAuthorized()

    val currentUserProfile: UserProfile?
        get() = authStorage.getProfile()

    private var pendingAuthProvider: String? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (!authStorage.isAuthorized()) {
            _uiState.value = ProfileUiState.Unauthenticated
            return
        }
        val profile = authStorage.getProfile()
        if (profile != null && profile !is UserProfile.Guest) {
            _uiState.value = ProfileUiState.Authorized(profile)
        } else {
            _uiState.value = ProfileUiState.Unauthenticated
        }
    }

    fun loginWithGitHub(
        context: Context,
        clientId: String = GITHUB_CLIENT_ID
    ) {
        pendingAuthProvider = "github"
        val authUri = GitHubAuthManager.getOAuthUrl(clientId)
        launchCustomTab(context, authUri)
    }

    fun loginWithDiscord(
        context: Context,
        clientId: String = DISCORD_CLIENT_ID
    ) {
        pendingAuthProvider = "discord"
        val authUri = DiscordAuthManager.getOAuthUrl(clientId)
        launchCustomTab(context, authUri)
    }

    private fun launchCustomTab(context: Context, uri: Uri) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, uri)
        } catch (_: Exception) {
            // Fallback to standard web browser intent
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    fun continueAsGuest(nickname: String) {
        val guestProfile = UserProfile.Guest(
            guestId = "guest_${System.currentTimeMillis()}",
            nickname = nickname.ifBlank { "Гость OpenFy" }
        )
        // Temporary session only - not saved to permanent auth storage
        _uiState.value = ProfileUiState.Authorized(guestProfile)
    }

    fun loginWithGitHubPersonalToken(token: String) {
        if (token.isBlank()) {
            _uiState.value = ProfileUiState.Error("Токен не может быть пустым")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val userResult = GitHubAuthManager.fetchUserProfile(token)
            userResult.onSuccess { githubUser ->
                val profile = UserProfile.GitHub(githubUser)
                authStorage.saveToken(token, provider = "github")
                authStorage.saveProfile(profile)
                _uiState.value = ProfileUiState.Authorized(profile)
            }.onFailure { err ->
                _uiState.value = ProfileUiState.Error("Ошибка получения профиля GitHub: ${err.localizedMessage}")
            }
        }
    }

    fun loginWithGitHubPAT(token: String) = loginWithGitHubPersonalToken(token)

    fun sendEmailOtp(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = com.example.openfy.features.community.auth.EmailAuthManager.sendOtp(email)
        result.onSuccess {
            onSuccess()
        }.onFailure { err ->
            onError(err.localizedMessage ?: "Ошибка отправки кода")
        }
    }

    fun verifyEmailOtp(
        email: String,
        code: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = com.example.openfy.features.community.auth.EmailAuthManager.verifyOtp(email, code)
            result.onSuccess { profile ->
                authStorage.saveProfile(profile)
                _uiState.value = ProfileUiState.Authorized(profile)
                onSuccess()
            }.onFailure { err ->
                _uiState.value = ProfileUiState.Unauthenticated
                onError(err.localizedMessage ?: "Неверный код подтверждения")
            }
        }
    }

    fun resendEmailOtp(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = com.example.openfy.features.community.auth.EmailAuthManager.sendOtp(email)
        result.onSuccess {
            onSuccess()
        }.onFailure { err ->
            onError(err.localizedMessage ?: "Ошибка повторной отправки кода")
        }
    }

    fun handleOAuthRedirect(
        uri: Uri,
        githubClientSecret: String = GITHUB_CLIENT_SECRET,
        discordClientSecret: String = DISCORD_CLIENT_SECRET
    ) {
        val error = uri.getQueryParameter("error")
        val errorDescription = uri.getQueryParameter("error_description")
        if (!error.isNullOrBlank()) {
            _uiState.value = ProfileUiState.Error(errorDescription ?: error)
            return
        }

        val code = uri.getQueryParameter("code")
        if (code.isNullOrBlank()) {
            _uiState.value = ProfileUiState.Error("Код авторизации не найден в ответе")
            return
        }

        val host = uri.host ?: ""
        val state = uri.getQueryParameter("state") ?: ""
        val provider = when {
            host == "discord-callback" || state.contains("discord") || pendingAuthProvider == "discord" -> "discord"
            else -> "github"
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            if (provider == "discord") {
                val tokenResult = DiscordAuthManager.exchangeCodeForToken(
                    clientId = DISCORD_CLIENT_ID,
                    clientSecret = discordClientSecret,
                    code = code,
                    redirectUri = DiscordAuthManager.DEFAULT_REDIRECT_URI
                )

                tokenResult.onSuccess { token ->
                    val userResult = DiscordAuthManager.fetchUserProfile(token)
                    userResult.onSuccess { discordUser ->
                        val profile = UserProfile.Discord(discordUser)
                        authStorage.saveToken(token, provider = "discord")
                        authStorage.saveProfile(profile)
                        _uiState.value = ProfileUiState.Authorized(profile)
                    }.onFailure { err ->
                        _uiState.value = ProfileUiState.Error("Ошибка получения профиля Discord: ${err.localizedMessage}")
                    }
                }.onFailure { err ->
                    _uiState.value = ProfileUiState.Error("Ошибка авторизации Discord: ${err.localizedMessage}")
                }
            } else {
                val tokenResult = GitHubAuthManager.exchangeCodeForToken(
                    clientId = GITHUB_CLIENT_ID,
                    clientSecret = githubClientSecret,
                    code = code
                )

                tokenResult.onSuccess { token ->
                    val userResult = GitHubAuthManager.fetchUserProfile(token)
                    userResult.onSuccess { githubUser ->
                        val profile = UserProfile.GitHub(githubUser)
                        authStorage.saveToken(token, provider = "github")
                        authStorage.saveProfile(profile)
                        _uiState.value = ProfileUiState.Authorized(profile)
                    }.onFailure { err ->
                        _uiState.value = ProfileUiState.Error("Ошибка получения профиля GitHub: ${err.localizedMessage}")
                    }
                }.onFailure { err ->
                    _uiState.value = ProfileUiState.Error("Ошибка авторизации GitHub: ${err.localizedMessage}")
                }
            }
        }
    }

    fun autoRestoreCredentials(context: Context, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = com.example.openfy.features.community.auth.OpenFyCredentialManager.restoreGitHubCredential(context)
            result.onSuccess { pair ->
                if (pair != null) {
                    val (_, token) = pair
                    val userResult = GitHubAuthManager.fetchUserProfile(token)
                    userResult.onSuccess { githubUser ->
                        val profile = UserProfile.GitHub(githubUser)
                        authStorage.saveToken(token, provider = "github")
                        authStorage.saveProfile(profile)
                        _uiState.value = ProfileUiState.Authorized(profile)
                        onComplete(true)
                    }.onFailure {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }.onFailure {
                onComplete(false)
            }
        }
    }

    fun saveGitHubCredential(context: Context, username: String, token: String) {
        viewModelScope.launch {
            com.example.openfy.features.community.auth.OpenFyCredentialManager.saveGitHubCredential(context, username, token)
        }
    }

    fun logout() {
        authStorage.clearAuth()
        _uiState.value = ProfileUiState.Unauthenticated
    }

    companion object {
        // Community GitHub OAuth Credentials
        const val GITHUB_CLIENT_ID = "Ov23lif8OuBmLgF8UyFb"
        const val GITHUB_CLIENT_SECRET = "ddc408461e3a5e37d03f076b85adb06ae893069f"

        // Community Discord OAuth Credentials
        const val DISCORD_CLIENT_ID = "1543312561355620352"
        const val DISCORD_CLIENT_SECRET = "6250b76a96eb072ea226ebe822c144459184d380a538b7b1806f03d6a6e09848"
    }
}
