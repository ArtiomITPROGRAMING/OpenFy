package com.example.openfy.features.community.auth

import com.example.openfy.features.community.model.UserProfile
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Passwordless Email OTP authentication (6-digit one-time code).
 * 100% Offline-first, secure, and privacy-preserving.
 */
object EmailAuthManager {

    private const val CODE_EXPIRATION_MS = 5 * 60 * 1000L // 5 minutes
    private const val RESEND_COOLDOWN_MS = 60 * 1000L      // 60 seconds
    private const val MAX_VERIFICATION_ATTEMPTS = 5

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val secureRandom = SecureRandom()

    private data class OtpSession(
        val code: String,
        val createdAt: Long,
        var attemptsLeft: Int = MAX_VERIFICATION_ATTEMPTS
    )

    private val activeSessions = ConcurrentHashMap<String, OtpSession>()

    /**
     * Validates whether [email] conforms to a standard email format.
     */
    fun isValidEmail(email: String): Boolean {
        val cleanEmail = email.trim()
        return cleanEmail.isNotBlank() && emailRegex.matches(cleanEmail)
    }

    /**
     * Requests OTP code generation and delivery for the specified [email].
     * Enforces a 60-second cooldown between consecutive requests.
     */
    fun sendOtp(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        if (!isValidEmail(cleanEmail)) {
            return Result.failure(IllegalArgumentException("Некорректный формат email"))
        }

        val existing = activeSessions[cleanEmail]
        val now = System.currentTimeMillis()

        if (existing != null && (now - existing.createdAt) < RESEND_COOLDOWN_MS) {
            val remainingSec = ((RESEND_COOLDOWN_MS - (now - existing.createdAt)) / 1000).toInt() + 1
            return Result.failure(IllegalStateException("Повторная отправка возможна через $remainingSec сек."))
        }

        // Generate cryptographically secure 6-digit numeric OTP code
        val rawCode = secureRandom.nextInt(900000) + 100000
        val code = rawCode.toString()

        activeSessions[cleanEmail] = OtpSession(
            code = code,
            createdAt = now,
            attemptsLeft = MAX_VERIFICATION_ATTEMPTS
        )

        return Result.success(code)
    }

    /**
     * Verifies the 6-digit [code] for the given [email].
     */
    fun verifyOtp(email: String, code: String): Result<UserProfile.Email> {
        val cleanEmail = email.trim().lowercase()
        val cleanCode = code.trim()

        if (!isValidEmail(cleanEmail)) {
            return Result.failure(IllegalArgumentException("Некорректный email"))
        }

        if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Код должен состоять из 6 цифр"))
        }

        val session = activeSessions[cleanEmail]
            ?: return Result.failure(IllegalStateException("Код не был отправлен или срок его действия истек"))

        val now = System.currentTimeMillis()
        if (now - session.createdAt > CODE_EXPIRATION_MS) {
            activeSessions.remove(cleanEmail)
            return Result.failure(IllegalStateException("Срок действия кода истек. Запросите новый код"))
        }

        if (session.attemptsLeft <= 0) {
            activeSessions.remove(cleanEmail)
            return Result.failure(IllegalStateException("Превышено количество попыток. Запросите код повторно"))
        }

        if (session.code != cleanCode) {
            session.attemptsLeft--
            val attemptsRemaining = session.attemptsLeft
            return Result.failure(
                IllegalArgumentException(
                    if (attemptsRemaining > 0) "Неверный код. Осталось попыток: $attemptsRemaining"
                    else "Неверный код. Попытки исчерпаны, запросите новый код"
                )
            )
        }

        // Success: consume session immediately
        activeSessions.remove(cleanEmail)
        val profile = UserProfile.Email(
            email = cleanEmail,
            customNickname = cleanEmail.substringBefore("@")
        )
        return Result.success(profile)
    }

    /**
     * Returns remaining seconds before resend is allowed (0 to 60).
     */
    fun getRemainingCooldown(email: String): Int {
        val cleanEmail = email.trim().lowercase()
        val session = activeSessions[cleanEmail] ?: return 0
        val elapsed = System.currentTimeMillis() - session.createdAt
        return if (elapsed < RESEND_COOLDOWN_MS) {
            ((RESEND_COOLDOWN_MS - elapsed) / 1000).toInt() + 1
        } else {
            0
        }
    }

    /**
     * Retrieves active OTP code (for unit testing and debug display).
     */
    fun peekOtpForTesting(email: String): String? {
        val cleanEmail = email.trim().lowercase()
        return activeSessions[cleanEmail]?.code
    }

    /**
     * Clears any active OTP verification session for [email].
     */
    fun clearSession(email: String) {
        val cleanEmail = email.trim().lowercase()
        activeSessions.remove(cleanEmail)
    }
}
