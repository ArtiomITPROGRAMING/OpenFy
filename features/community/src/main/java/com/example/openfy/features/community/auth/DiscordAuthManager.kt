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
import com.example.openfy.features.community.model.DiscordUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DiscordAuthManager {

    private const val DISCORD_AUTH_URL = "https://discord.com/api/oauth2/authorize"
    const val DEFAULT_REDIRECT_URI = "openfy://discord-callback"

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
     * In offline F-Droid build, network profile fetching is not supported.
     */
    suspend fun fetchUserProfile(accessToken: String): Result<DiscordUser> = withContext(Dispatchers.IO) {
        Result.failure(UnsupportedOperationException("Online authentication is disabled in offline build."))
    }
}
