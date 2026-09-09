package com.example.openfy.features.community.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordUser(
    val id: String,
    val username: String,
    @SerialName("global_name")
    val globalName: String? = null,
    val avatar: String? = null,
    val discriminator: String? = null,
    val email: String? = null
) {
    val avatarUrl: String
        get() = if (!avatar.isNullOrBlank()) {
            "https://cdn.discordapp.com/avatars/$id/$avatar.png"
        } else {
            "https://cdn.discordapp.com/embed/avatars/0.png"
        }

    val displayName: String
        get() = globalName ?: username
}
