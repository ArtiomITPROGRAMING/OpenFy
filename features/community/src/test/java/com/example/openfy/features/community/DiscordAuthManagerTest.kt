package com.example.openfy.features.community

import com.example.openfy.features.community.auth.DiscordAuthManager
import com.example.openfy.features.community.model.DiscordUser
import com.example.openfy.features.community.model.GitHubUser
import com.example.openfy.features.community.model.UserProfile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscordAuthManagerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `getOAuthUrlString generates valid Discord authorization URL`() {
        val clientId = "123456789012345678"
        val redirectUri = "openfy://oauth-callback"
        val scopes = listOf("identify", "email", "guilds")

        val url = DiscordAuthManager.getOAuthUrlString(
            clientId = clientId,
            redirectUri = redirectUri,
            scopes = scopes,
            state = "openfy_discord_test_777"
        )

        assertTrue(url.startsWith("https://discord.com/api/oauth2/authorize"))
        assertTrue(url.contains("client_id=123456789012345678"))
        assertTrue(url.contains("redirect_uri=openfy://oauth-callback"))
        assertTrue(url.contains("response_type=code"))
        assertTrue(url.contains("scope=identify%20email%20guilds"))
        assertTrue(url.contains("state=openfy_discord_test_777"))

        val defaultUrl = DiscordAuthManager.getOAuthUrlString(clientId = "1543312561355620352")
        assertTrue(defaultUrl.contains("redirect_uri=openfy://discord-callback"))
        assertTrue(defaultUrl.contains("client_id=1543312561355620352"))
    }

    @Test
    fun `DiscordUser deserializes properly and formats avatar URLs correctly`() {
        val jsonWithAvatar = """
            {
                "id": "80351110224678912",
                "username": "nelly",
                "discriminator": "1337",
                "avatar": "8342729096ea3686f60800713ff0efee",
                "global_name": "Nelly the Hamster",
                "email": "nelly@discord.com"
            }
        """.trimIndent()

        val userWithAvatar = json.decodeFromString<DiscordUser>(jsonWithAvatar)
        assertEquals("80351110224678912", userWithAvatar.id)
        assertEquals("nelly", userWithAvatar.username)
        assertEquals("Nelly the Hamster", userWithAvatar.displayName)
        assertEquals("https://cdn.discordapp.com/avatars/80351110224678912/8342729096ea3686f60800713ff0efee.png", userWithAvatar.avatarUrl)

        val jsonNoAvatar = """
            {
                "id": "999999999",
                "username": "default_user"
            }
        """.trimIndent()

        val userNoAvatar = json.decodeFromString<DiscordUser>(jsonNoAvatar)
        assertEquals("default_user", userNoAvatar.displayName)
        assertEquals("https://cdn.discordapp.com/embed/avatars/0.png", userNoAvatar.avatarUrl)
    }

    @Test
    fun `UserProfile polymorphic serialization works for GitHub, Discord and Guest`() {
        val discordProfile: UserProfile = UserProfile.Discord(
            DiscordUser(
                id = "555123",
                username = "artiom_crudu",
                globalName = "Artiom"
            )
        )
        val encodedDiscord = json.encodeToString(UserProfile.serializer(), discordProfile)
        val decodedDiscord = json.decodeFromString<UserProfile>(encodedDiscord)
        assertTrue(decodedDiscord is UserProfile.Discord)
        assertEquals("Artiom", decodedDiscord.displayName)
        assertEquals("Discord", decodedDiscord.providerName)

        val guestProfile: UserProfile = UserProfile.Guest(
            guestId = "guest_001",
            nickname = "Анонимный Слушатель"
        )
        val encodedGuest = json.encodeToString(UserProfile.serializer(), guestProfile)
        val decodedGuest = json.decodeFromString<UserProfile>(encodedGuest)
        assertTrue(decodedGuest is UserProfile.Guest)
        assertEquals("Анонимный Слушатель", decodedGuest.displayName)
        assertEquals("Guest", decodedGuest.providerName)
    }
}
