package com.example.openfy.features.community.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubDeviceCodeResponse(
    @SerialName("device_code")
    val deviceCode: String = "",
    @SerialName("user_code")
    val userCode: String = "",
    @SerialName("verification_uri")
    val verificationUri: String = "https://github.com/login/device",
    @SerialName("expires_in")
    val expiresIn: Int = 900,
    val interval: Int = 5,
    val error: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null
)
