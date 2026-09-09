package com.example.openfy.features.community

import com.example.openfy.features.community.auth.GitHubAuthManager
import com.example.openfy.features.community.model.GitHubUser
import com.example.openfy.features.community.model.OAuthTokenResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubAuthManagerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `getOAuthUrlString generates correct GitHub OAuth URL parameters`() {
        val clientId = "test_client_12345"
        val redirectUri = "openfy://oauth-callback"
        val scopes = listOf("read:user", "gist")

        val url = GitHubAuthManager.getOAuthUrlString(clientId, redirectUri, scopes, state = "openfy_oauth_test_123")

        assertTrue(url.startsWith("https://github.com/login/oauth/authorize"))
        assertTrue(url.contains("client_id=test_client_12345"))
        assertTrue(url.contains("redirect_uri=openfy://oauth-callback"))
        assertTrue(url.contains("scope=read:user%20gist"))
        assertTrue(url.contains("state=openfy_oauth_test_123"))
    }

    @Test
    fun `OAuthTokenResponse deserializes correctly from GitHub API response`() {
        val tokenJsonResponse = """
            {
                "access_token": "gho_16C7e42F292c6912E7710c838347Ae178B4a",
                "token_type": "bearer",
                "scope": "read:user,gist"
            }
        """.trimIndent()

        val response = json.decodeFromString<OAuthTokenResponse>(tokenJsonResponse)

        assertEquals("gho_16C7e42F292c6912E7710c838347Ae178B4a", response.accessToken)
        assertEquals("bearer", response.tokenType)
        assertEquals("read:user,gist", response.scope)
    }

    @Test
    fun `GitHubUser deserializes correctly from user API JSON`() {
        val userJson = """
            {
                "login": "octocat",
                "id": 583231,
                "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                "name": "The Octocat",
                "bio": "Open source lover",
                "email": "octocat@github.com",
                "html_url": "https://github.com/octocat"
            }
        """.trimIndent()

        val user = json.decodeFromString<GitHubUser>(userJson)

        assertEquals("octocat", user.login)
        assertEquals(583231L, user.id)
        assertEquals("The Octocat", user.name)
        assertEquals("Open source lover", user.bio)
        assertEquals("octocat@github.com", user.email)
        assertEquals("https://avatars.githubusercontent.com/u/583231?v=4", user.avatarUrl)
    }

    @Test
    fun `OAuthTokenResponse correctly parses GitHub 200 OK error payloads`() {
        val errorJson = """
            {
                "error": "bad_verification_code",
                "error_description": "The code passed is incorrect or has expired.",
                "error_uri": "https://docs.github.com/apps/managing-oauth-apps/troubleshooting-oauth-app-access-token-request-errors/#bad-verification-code"
            }
        """.trimIndent()

        val response = json.decodeFromString<OAuthTokenResponse>(errorJson)

        assertEquals("bad_verification_code", response.error)
        assertEquals("The code passed is incorrect or has expired.", response.errorDescription)
        assertTrue(response.accessToken.isEmpty())
    }
}
