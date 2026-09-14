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
