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
