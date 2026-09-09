package com.example.openfy.features.community.auth

import android.net.Uri
import com.example.openfy.features.community.model.DiscordUser
import com.example.openfy.features.community.model.OAuthTokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object DiscordAuthManager {

    private const val DISCORD_AUTH_URL = "https://discord.com/api/oauth2/authorize"
    private const val DISCORD_TOKEN_URL = "https://discord.com/api/oauth2/token"
    private const val DISCORD_USER_URL = "https://discord.com/api/users/@me"

    const val DEFAULT_REDIRECT_URI = "openfy://discord-callback"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Builds the authorization URL string for Discord OAuth2 grant in browser.
     */
    fun getOAuthUrlString(
        clientId: String,
        redirectUri: String = DEFAULT_REDIRECT_URI,
        scopes: List<String> = listOf("identify", "email", "guilds"),
        state: String = "openfy_discord_${System.currentTimeMillis()}"
    ): String {
        val encodedScope = scopes.joinToString("%20")
        return "$DISCORD_AUTH_URL?client_id=$clientId&redirect_uri=$redirectUri&response_type=code&scope=$encodedScope&state=$state"
    }

    /**
     * Builds the authorization Uri for Discord OAuth2 grant in browser.
     */
    fun getOAuthUrl(
        clientId: String,
        redirectUri: String = DEFAULT_REDIRECT_URI,
        scopes: List<String> = listOf("identify", "email", "guilds")
    ): Uri {
        return Uri.parse(getOAuthUrlString(clientId, redirectUri, scopes))
    }

    /**
     * Exchanges temporary authorization [code] for a Discord OAuth access token.
     */
    suspend fun exchangeCodeForToken(
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: String = DEFAULT_REDIRECT_URI
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build()

            val request = Request.Builder()
                .url(DISCORD_TOKEN_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Ошибка запроса токена Discord (HTTP ${response.code}): $responseBody"))
                }

                try {
                    val tokenResponse = json.decodeFromString<OAuthTokenResponse>(responseBody)
                    if (tokenResponse.accessToken.isNotBlank()) {
                        Result.success(tokenResponse.accessToken)
                    } else {
                        Result.failure(IllegalStateException("Пустой access_token в ответе Discord: $responseBody"))
                    }
                } catch (e: Exception) {
                    Result.failure(IllegalArgumentException("Не удалось распарсить ответ токена Discord: ${e.localizedMessage}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches authenticated user's Discord profile.
     */
    suspend fun fetchUserProfile(accessToken: String): Result<DiscordUser> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(DISCORD_USER_URL)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("User-Agent", "OpenFy-MusicPlayer-FOSS")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Ошибка профиля Discord (HTTP ${response.code}): $body"))
                }

                try {
                    val user = json.decodeFromString<DiscordUser>(body)
                    Result.success(user)
                } catch (e: Exception) {
                    Result.failure(IllegalArgumentException("Не удалось распарсить профиль Discord: ${e.localizedMessage}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
