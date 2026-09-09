package com.example.openfy.features.community.auth

import android.net.Uri
import com.example.openfy.features.community.model.GitHubDeviceCodeResponse
import com.example.openfy.features.community.model.GitHubEmail
import com.example.openfy.features.community.model.GitHubUser
import com.example.openfy.features.community.model.OAuthTokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object GitHubAuthManager {

    private const val GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize"
    private const val GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token"
    private const val GITHUB_DEVICE_CODE_URL = "https://github.com/login/device/code"
    private const val GITHUB_API_USER_URL = "https://api.github.com/user"
    private const val GITHUB_API_EMAILS_URL = "https://api.github.com/user/emails"

    const val DEFAULT_REDIRECT_URI = "openfy://oauth-callback"
    const val DEFAULT_CLIENT_ID = "Ov23lif8OuBmLgF8UyFb"
    const val DEFAULT_CLIENT_SECRET = "ddc408461e3a5e37d03f076b85adb06ae893069f"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Builds the authorization URL string for OAuth grant in browser.
     */
    fun getOAuthUrlString(
        clientId: String,
        redirectUri: String = DEFAULT_REDIRECT_URI,
        scopes: List<String> = listOf("read:user", "user:email", "gist"),
        state: String = "openfy_oauth_${System.currentTimeMillis()}"
    ): String {
        val encodedScope = scopes.joinToString("%20")
        return "$GITHUB_AUTH_URL?client_id=$clientId&redirect_uri=$redirectUri&scope=$encodedScope&state=$state"
    }

    /**
     * Builds the authorization Uri for user to grant OAuth access in browser.
     */
    fun getOAuthUrl(
        clientId: String,
        redirectUri: String = DEFAULT_REDIRECT_URI,
        scopes: List<String> = listOf("read:user", "user:email", "gist")
    ): Uri {
        return Uri.parse(getOAuthUrlString(clientId, redirectUri, scopes))
    }

    /**
     * Exchanges temporary authorization [code] for a GitHub OAuth access token.
     * Handles GitHub 200 OK error payloads correctly.
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
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build()

            val request = Request.Builder()
                .url(GITHUB_TOKEN_URL)
                .addHeader("Accept", "application/json")
                .post(formBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Ошибка сервера GitHub (HTTP ${response.code}): $responseBody"))
                }

                try {
                    val tokenResponse = json.decodeFromString<OAuthTokenResponse>(responseBody)

                    if (!tokenResponse.error.isNullOrBlank()) {
                        val errorDetail = when (tokenResponse.error) {
                            "bad_verification_code" -> "Код авторизации устарел или неверен. Попробуйте войти заново."
                            "incorrect_client_credentials" -> "Неверный Client ID или Client Secret приложения."
                            "redirect_uri_mismatch" -> "Redirect URI ($redirectUri) не совпадает с настройками OAuth App в GitHub."
                            "application_suspended" -> "OAuth-приложение GitHub временно заблокировано."
                            else -> tokenResponse.errorDescription ?: tokenResponse.error
                        }
                        return@withContext Result.failure(IllegalStateException(errorDetail))
                    }

                    if (tokenResponse.accessToken.isNotBlank()) {
                        Result.success(tokenResponse.accessToken)
                    } else {
                        Result.failure(IllegalStateException("Пустой токен в ответе GitHub: $responseBody"))
                    }
                } catch (e: Exception) {
                    Result.failure(IllegalArgumentException("Не удалось обработать ответ токена: ${e.localizedMessage}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * GitHub Device Flow (RFC 8628): Requests user & device codes without needing client_secret.
     */
    suspend fun requestDeviceCode(
        clientId: String,
        scopes: List<String> = listOf("read:user", "user:email", "gist")
    ): Result<GitHubDeviceCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("client_id", clientId)
                .add("scope", scopes.joinToString(" "))
                .build()

            val request = Request.Builder()
                .url(GITHUB_DEVICE_CODE_URL)
                .addHeader("Accept", "application/json")
                .post(formBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Ошибка Device Code (HTTP ${response.code}): $responseBody"))
                }

                try {
                    val deviceResp = json.decodeFromString<GitHubDeviceCodeResponse>(responseBody)
                    if (deviceResp.deviceCode.isNotBlank()) {
                        Result.success(deviceResp)
                    } else {
                        Result.failure(IllegalStateException(deviceResp.errorDescription ?: deviceResp.error ?: "Не удалось получить Device Code"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Polls token for Device Authorization Flow.
     */
    suspend fun pollDeviceToken(
        clientId: String,
        deviceCode: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("client_id", clientId)
                .add("device_code", deviceCode)
                .add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                .build()

            val request = Request.Builder()
                .url(GITHUB_TOKEN_URL)
                .addHeader("Accept", "application/json")
                .post(formBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val tokenResponse = json.decodeFromString<OAuthTokenResponse>(responseBody)

                if (!tokenResponse.error.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalStateException(tokenResponse.error))
                }

                if (tokenResponse.accessToken.isNotBlank()) {
                    Result.success(tokenResponse.accessToken)
                } else {
                    Result.failure(IllegalStateException("Токен пока не готов"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches authenticated user's GitHub profile.
     * If user's public email is null, attempts to query private emails via `/user/emails`.
     */
    suspend fun fetchUserProfile(token: String): Result<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_API_USER_URL)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("User-Agent", "OpenFy-MusicPlayer-FOSS")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = if (response.code == 401) {
                        "Недействительный или отозванный токен GitHub (401 Unauthorized)"
                    } else {
                        "Ошибка профиля GitHub (HTTP ${response.code}): $body"
                    }
                    return@withContext Result.failure(IOException(errorMsg))
                }

                try {
                    val user = json.decodeFromString<GitHubUser>(body)

                    // If public email is null, fetch from /user/emails
                    val finalUser = if (user.email.isNullOrBlank()) {
                        val privateEmail = fetchPrimaryEmail(token)
                        if (privateEmail != null) user.copy(email = privateEmail) else user
                    } else {
                        user
                    }

                    Result.success(finalUser)
                } catch (e: Exception) {
                    Result.failure(IllegalArgumentException("Не удалось распарсить профиль пользователя: ${e.localizedMessage}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Direct Personal Access Token (PAT) authentication.
     */
    suspend fun loginWithPersonalAccessToken(token: String): Result<GitHubUser> {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Токен не может быть пустым"))
        }
        return fetchUserProfile(cleanToken)
    }

    private suspend fun fetchPrimaryEmail(token: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_API_EMAILS_URL)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("User-Agent", "OpenFy-MusicPlayer-FOSS")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val emails = json.decodeFromString<List<GitHubEmail>>(body)
                emails.firstOrNull { it.primary && it.verified }?.email ?: emails.firstOrNull()?.email
            }
        } catch (_: Exception) {
            null
        }
    }
}
