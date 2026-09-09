package com.example.openfy.features.community.sync

import android.annotation.SuppressLint
import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Serializable
enum class ShareType {
    PLAYLIST,
    THEME,
    TRACK_META
}

@Serializable
data class SharePayload(
    val version: Int = 1,
    val type: ShareType,
    val title: String,
    val description: String? = null,
    val jsonData: String,
    val createdAt: Long = System.currentTimeMillis(),
    val author: String = "OpenFy User"
) {

    fun toCompressedString(): String {
        val jsonStr = json.encodeToString(serializer(), this)
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzip ->
            gzip.write(jsonStr.toByteArray(Charsets.UTF_8))
        }
        val bytes = baos.toByteArray()
        val base64 = safeBase64Encode(bytes)
        return "openfy://share?data=$base64"
    }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        @SuppressLint("NewApi")
        private fun safeBase64Encode(bytes: ByteArray): String {
            return try {
                android.util.Base64.encodeToString(
                    bytes,
                    android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
                )
            } catch (_: RuntimeException) {
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
            }
        }

        @SuppressLint("NewApi")
        private fun safeBase64Decode(data: String): ByteArray {
            return try {
                android.util.Base64.decode(
                    data,
                    android.util.Base64.DEFAULT or android.util.Base64.URL_SAFE
                )
            } catch (_: RuntimeException) {
                try {
                    java.util.Base64.getUrlDecoder().decode(data)
                } catch (_: Exception) {
                    java.util.Base64.getDecoder().decode(data)
                }
            }
        }

        fun fromCompressedString(raw: String): Result<SharePayload> {
            return try {
                val cleaned = raw.trim()
                val base64Data = when {
                    cleaned.startsWith("openfy://share?data=") -> cleaned.removePrefix("openfy://share?data=")
                    cleaned.startsWith("openfy://share/") -> cleaned.removePrefix("openfy://share/")
                    else -> cleaned
                }

                // First try uncompressing as Base64 GZIP
                try {
                    val bytes = safeBase64Decode(base64Data)
                    val bais = ByteArrayInputStream(bytes)
                    val uncompressedBytes = GZIPInputStream(bais).use { it.readBytes() }
                    val jsonStr = uncompressedBytes.decodeToString()
                    Result.success(json.decodeFromString(serializer(), jsonStr))
                } catch (_: Exception) {
                    // Fallback to direct raw JSON
                    Result.success(json.decodeFromString(serializer(), cleaned))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
