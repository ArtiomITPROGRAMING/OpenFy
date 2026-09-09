package com.example.openfy.features.community.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubUser(
    val login: String,
    val id: Long,
    @SerialName("avatar_url")
    val avatarUrl: String = "",
    val name: String? = null,
    val bio: String? = null,
    val email: String? = null,
    @SerialName("html_url")
    val htmlUrl: String? = null
)
