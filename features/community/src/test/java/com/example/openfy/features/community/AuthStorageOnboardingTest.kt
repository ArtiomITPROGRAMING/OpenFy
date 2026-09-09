package com.example.openfy.features.community

import com.example.openfy.features.community.model.DiscordUser
import com.example.openfy.features.community.model.GitHubUser
import com.example.openfy.features.community.model.UserProfile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthStorageOnboardingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `Guest user profile serializes correctly`() {
        val guest = UserProfile.Guest(guestId = "guest_123", nickname = "Артём")
        val serialized = json.encodeToString(UserProfile.serializer(), guest)
        val deserialized = json.decodeFromString<UserProfile>(serialized)

        assertTrue(deserialized is UserProfile.Guest)
        assertEquals("guest_123", deserialized.id)
        assertEquals("Артём", deserialized.displayName)
        assertEquals("Guest", deserialized.providerName)
    }

    @Test
    fun `GitHub user profile serializes correctly`() {
        val github = UserProfile.GitHub(
            GitHubUser(id = 12345, login = "artiom", name = "Artiom Crudu", avatarUrl = "https://github.com/avatar.png")
        )
        val serialized = json.encodeToString(UserProfile.serializer(), github)
        val deserialized = json.decodeFromString<UserProfile>(serialized)

        assertTrue(deserialized is UserProfile.GitHub)
        assertEquals("12345", deserialized.id)
        assertEquals("Artiom Crudu", deserialized.displayName)
        assertEquals("GitHub", deserialized.providerName)
    }

    @Test
    fun `Discord user profile serializes correctly`() {
        val discord = UserProfile.Discord(
            DiscordUser(id = "1543312561355620352", username = "artiom_c", globalName = "Артём", avatar = "abc12345")
        )
        val serialized = json.encodeToString(UserProfile.serializer(), discord)
        val deserialized = json.decodeFromString<UserProfile>(serialized)

        assertTrue(deserialized is UserProfile.Discord)
        assertEquals("1543312561355620352", deserialized.id)
        assertEquals("Артём", deserialized.displayName)
        assertEquals("Discord", deserialized.providerName)
    }

    @Test
    fun `Email user profile serializes correctly`() {
        val emailProfile = UserProfile.Email(
            email = "artiom@openfy.org",
            customNickname = "Артём"
        )
        val serialized = json.encodeToString(UserProfile.serializer(), emailProfile)
        val deserialized = json.decodeFromString<UserProfile>(serialized)

        assertTrue(deserialized is UserProfile.Email)
        assertEquals("artiom@openfy.org", deserialized.id)
        assertEquals("Артём", deserialized.displayName)
        assertEquals("Email", deserialized.providerName)
    }
}
