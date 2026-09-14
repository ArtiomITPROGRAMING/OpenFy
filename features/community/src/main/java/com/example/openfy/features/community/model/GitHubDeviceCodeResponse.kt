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
