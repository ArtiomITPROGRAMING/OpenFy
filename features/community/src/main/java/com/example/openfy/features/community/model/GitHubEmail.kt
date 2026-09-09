package com.example.openfy.features.community.model

import kotlinx.serialization.Serializable

@Serializable
data class GitHubEmail(
    val email: String,
    val primary: Boolean = false,
    val verified: Boolean = false,
    val visibility: String? = null
)
