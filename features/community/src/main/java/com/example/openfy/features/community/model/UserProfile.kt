package com.example.openfy.features.community.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface UserProfile {

    val id: String
    val displayName: String
    val avatarUrl: String
    val providerName: String

    @Serializable
    data class GitHub(val user: GitHubUser) : UserProfile {
        override val id: String get() = user.id.toString()
        override val displayName: String get() = user.name ?: user.login
        override val avatarUrl: String get() = user.avatarUrl
        override val providerName: String get() = "GitHub"
    }

    @Serializable
    data class Discord(val user: DiscordUser) : UserProfile {
        override val id: String get() = user.id
        override val displayName: String get() = user.displayName
        override val avatarUrl: String get() = user.avatarUrl
        override val providerName: String get() = "Discord"
    }

    @Serializable
    data class Email(
        val email: String,
        val customNickname: String? = null,
        val emailAvatarUrl: String = ""
    ) : UserProfile {
        override val id: String get() = email
        override val displayName: String get() = customNickname?.ifBlank { null } ?: email.substringBefore("@")
        override val avatarUrl: String get() = emailAvatarUrl
        override val providerName: String get() = "Email"
    }

    @Serializable
    data class Guest(
        val guestId: String,
        val nickname: String = "Гость OpenFy"
    ) : UserProfile {
        override val id: String get() = guestId
        override val displayName: String get() = nickname
        override val avatarUrl: String get() = ""
        override val providerName: String get() = "Guest"
    }
}
