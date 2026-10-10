/*
 * Copyright (C) 2026 ArtiomITPROGRAMING
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.openfy.features.community.auth

import android.net.Uri
import com.example.openfy.features.community.model.GitHubDeviceCodeResponse
import com.example.openfy.features.community.model.GitHubUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object GitHubAuthManager {

    private const val GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize"

    const val DEFAULT_REDIRECT_URI = "openfy://oauth-callback"
    const val DEFAULT_CLIENT_ID = ""
    const val DEFAULT_CLIENT_SECRET = ""

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

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
     * In offline F-Droid build, network authentication is not supported.
     */
    suspend fun exchangeCodeForToken(
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: String = DEFAULT_REDIRECT_URI
    ): Result<String> = withContext(Dispatchers.IO) {
        Result.failure(UnsupportedOperationException("Online authentication is disabled in offline build."))
    }

    /**
     * In offline F-Droid build, device code flow is not supported.
     */
    suspend fun requestDeviceCode(
        clientId: String,
        scopes: List<String> = listOf("read:user", "user:email", "gist")
    ): Result<GitHubDeviceCodeResponse> = withContext(Dispatchers.IO) {
        Result.failure(UnsupportedOperationException("Online authentication is disabled in offline build."))
    }

    /**
     * In offline F-Droid build, device token polling is not supported.
     */
    suspend fun pollDeviceToken(
        clientId: String,
        deviceCode: String
    ): Result<String> = withContext(Dispatchers.IO) {
        Result.failure(UnsupportedOperationException("Online authentication is disabled in offline build."))
    }

    /**
     * In offline F-Droid build, network profile fetching is not supported.
     */
    suspend fun fetchUserProfile(token: String): Result<GitHubUser> = withContext(Dispatchers.IO) {
        Result.failure(UnsupportedOperationException("Online authentication is disabled in offline build."))
    }

    /**
     * Direct Personal Access Token (PAT) authentication.
     */
    suspend fun loginWithPersonalAccessToken(token: String): Result<GitHubUser> = withContext(Dispatchers.IO) {
        Result.failure(UnsupportedOperationException("Online authentication is disabled in offline build."))
    }
}
