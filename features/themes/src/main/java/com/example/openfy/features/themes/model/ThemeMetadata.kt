package com.example.openfy.features.themes.model

import kotlinx.serialization.Serializable

@Serializable
data class ThemeMetadata(
    val id: String,
    val name: String,
    val author: String = "OpenFy Creator",
    val version: String = "1.0",
    val minAppVersion: Int = 1,
    val isDark: Boolean = true,
    val description: String = "",
    val previewAccentHex: String? = null
)
