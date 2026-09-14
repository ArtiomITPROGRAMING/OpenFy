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

package com.example.openfy.features.community

import com.example.openfy.features.community.auth.EmailAuthManager
import com.example.openfy.features.community.model.UserProfile
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailAuthManagerTest {

    private val testEmail = "tester@openfy.org"

    @After
    fun tearDown() {
        EmailAuthManager.clearSession(testEmail)
    }

    @Test
    fun `isValidEmail validates emails correctly`() {
        assertTrue(EmailAuthManager.isValidEmail("user@domain.com"))
        assertTrue(EmailAuthManager.isValidEmail("user.name+tag@sub.domain.org"))
        assertTrue(EmailAuthManager.isValidEmail("artiom@openfy.app"))

        assertFalse(EmailAuthManager.isValidEmail(""))
        assertFalse(EmailAuthManager.isValidEmail("plainaddress"))
        assertFalse(EmailAuthManager.isValidEmail("@missingusername.com"))
        assertFalse(EmailAuthManager.isValidEmail("user@.com"))
        assertFalse(EmailAuthManager.isValidEmail("user@domain"))
    }

    @Test
    fun `sendOtp generates 6 digit numeric code and enforces cooldown`() {
        val result = EmailAuthManager.sendOtp(testEmail)
        assertTrue(result.isSuccess)

        val code = result.getOrNull()
        assertNotNull(code)
        assertEquals(6, code?.length)
        assertTrue(code?.all { it.isDigit() } == true)

        // Second immediate call should fail due to 60s cooldown
        val secondResult = EmailAuthManager.sendOtp(testEmail)
        assertTrue(secondResult.isFailure)
        assertTrue(EmailAuthManager.getRemainingCooldown(testEmail) > 0)
    }

    @Test
    fun `verifyOtp verifies valid code and returns UserProfile Email`() {
        EmailAuthManager.sendOtp(testEmail)
        val code = EmailAuthManager.peekOtpForTesting(testEmail)
        assertNotNull(code)

        val verifyResult = EmailAuthManager.verifyOtp(testEmail, code!!)
        assertTrue(verifyResult.isSuccess)

        val profile = verifyResult.getOrNull()
        assertNotNull(profile)
        assertTrue(profile is UserProfile.Email)
        assertEquals(testEmail, profile?.id)
        assertEquals("tester", profile?.displayName)
        assertEquals("Email", profile?.providerName)

        // Session should now be consumed
        val secondVerify = EmailAuthManager.verifyOtp(testEmail, code)
        assertTrue(secondVerify.isFailure)
    }

    @Test
    fun `verifyOtp rejects incorrect code`() {
        EmailAuthManager.sendOtp(testEmail)

        val verifyResult = EmailAuthManager.verifyOtp(testEmail, "000000")
        assertTrue(verifyResult.isFailure)
    }
}
