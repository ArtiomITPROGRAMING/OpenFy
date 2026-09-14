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

package com.example.openfy.features.community.auth

import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException

object OpenFyCredentialManager {

    /**
     * Saves GitHub credentials (username and access token / personal token) into Android Credential Manager.
     */
    suspend fun saveGitHubCredential(
        context: Context,
        username: String,
        token: String
    ): Result<Unit> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val createPasswordRequest = CreatePasswordRequest(
                id = username,
                password = token
            )
            credentialManager.createCredential(context, createPasswordRequest)
            Result.success(Unit)
        } catch (e: CreateCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves saved credentials from Android Credential Manager.
     * Returns Pair(username, token) if found, or null if cancelled / not present.
     */
    suspend fun restoreGitHubCredential(
        context: Context
    ): Result<Pair<String, String>?> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val getPasswordOption = GetPasswordOption()
            val getCredentialRequest = GetCredentialRequest(
                listOf(getPasswordOption)
            )

            val response = credentialManager.getCredential(context, getCredentialRequest)
            val credential = response.credential

            if (credential is PasswordCredential) {
                Result.success(Pair(credential.id, credential.password))
            } else {
                Result.success(null)
            }
        } catch (e: GetCredentialCancellationException) {
            // User dismissed or skipped prompt
            Result.success(null)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
