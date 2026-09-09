package com.example.openfy.features.community.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.openfy.features.community.model.DiscordUser
import com.example.openfy.features.community.model.GitHubUser
import com.example.openfy.features.community.model.UserProfile
import kotlinx.serialization.json.Json

class AuthStorage(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        // Fallback to private prefs if KeyStore is unavailable on custom ROMs
        context.getSharedPreferences(PREFS_FILE_NAME + "_fallback", Context.MODE_PRIVATE)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun saveToken(token: String, provider: String = "github") {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_AUTH_PROVIDER, provider)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getProvider(): String? {
        return prefs.getString(KEY_AUTH_PROVIDER, null)
    }

    fun clearToken() {
        clearAuth()
    }

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_AUTH_PROVIDER)
            .remove(KEY_SAVED_USER)
            .remove(KEY_SAVED_DISCORD_USER)
            .remove(KEY_SAVED_PROFILE)
            .apply()
    }

    fun isAuthorized(): Boolean {
        val profile = getProfile()
        if (profile is UserProfile.Guest) return false
        val token = getToken()
        return !token.isNullOrBlank() || (profile != null && profile !is UserProfile.Guest)
    }

    // --- Profile Handling ---

    fun saveProfile(profile: UserProfile) {
        when (profile) {
            is UserProfile.GitHub -> {
                saveUser(profile.user)
                saveToken(getToken() ?: "", provider = "github")
            }
            is UserProfile.Discord -> {
                saveDiscordUser(profile.user)
                saveToken(getToken() ?: "", provider = "discord")
            }
            is UserProfile.Email -> {
                saveToken(getToken() ?: "email_token_${System.currentTimeMillis()}", provider = "email")
            }
            is UserProfile.Guest -> {
                prefs.edit().putString(KEY_AUTH_PROVIDER, "guest").apply()
            }
        }
        val profileJson = json.encodeToString(UserProfile.serializer(), profile)
        prefs.edit().putString(KEY_SAVED_PROFILE, profileJson).apply()
    }

    fun getProfile(): UserProfile? {
        val profileJson = prefs.getString(KEY_SAVED_PROFILE, null)
        if (!profileJson.isNullOrBlank()) {
            try {
                return json.decodeFromString<UserProfile>(profileJson)
            } catch (_: Exception) {}
        }

        // Fallback checks from individual user keys
        val githubUser = getUser()
        if (githubUser != null) {
            return UserProfile.GitHub(githubUser)
        }

        val discordUser = getDiscordUser()
        if (discordUser != null) {
            return UserProfile.Discord(discordUser)
        }

        return null
    }

    // --- GitHub Specific ---

    fun saveUser(user: GitHubUser) {
        val userJson = json.encodeToString(GitHubUser.serializer(), user)
        prefs.edit()
            .putString(KEY_SAVED_USER, userJson)
            .putString(KEY_AUTH_PROVIDER, "github")
            .apply()
    }

    fun getUser(): GitHubUser? {
        val userJson = prefs.getString(KEY_SAVED_USER, null) ?: return null
        return try {
            json.decodeFromString<GitHubUser>(userJson)
        } catch (_: Exception) {
            null
        }
    }

    // --- Discord Specific ---

    fun saveDiscordUser(user: DiscordUser) {
        val userJson = json.encodeToString(DiscordUser.serializer(), user)
        prefs.edit()
            .putString(KEY_SAVED_DISCORD_USER, userJson)
            .putString(KEY_AUTH_PROVIDER, "discord")
            .apply()
    }

    fun getDiscordUser(): DiscordUser? {
        val userJson = prefs.getString(KEY_SAVED_DISCORD_USER, null) ?: return null
        return try {
            json.decodeFromString<DiscordUser>(userJson)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_FILE_NAME = "openfy_community_auth_secure"
        private const val KEY_ACCESS_TOKEN = "community_access_token"
        private const val KEY_AUTH_PROVIDER = "community_auth_provider"
        private const val KEY_SAVED_USER = "github_saved_user"
        private const val KEY_SAVED_DISCORD_USER = "discord_saved_user"
        private const val KEY_SAVED_PROFILE = "community_saved_profile"
    }
}
