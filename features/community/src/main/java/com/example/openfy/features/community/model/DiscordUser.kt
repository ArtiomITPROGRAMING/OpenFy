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
