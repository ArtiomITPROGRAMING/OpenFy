package com.example.openfy.features.streamer.resolver

import android.net.Uri
import com.example.openfy.core.audio.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class StreamMetadata(
    val streamUrl: String,
    val title: String,
    val artist: String,
    val album: String = "Online Stream",
    val coverUrl: String? = null,
    val contentType: String? = null,
    val bitrateKbps: Int? = null,
    val durationMs: Long = 0L,
    val isLive: Boolean = true
) {
    fun toSong(): Song = Song.createStreamTrack(
        url = streamUrl,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        coverUrl = coverUrl
    )
}

object SafeStreamResolver {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val ogTitlePattern = Pattern.compile("<meta\\s+property=[\"']og:title[\"']\\s+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val ogImagePattern = Pattern.compile("<meta\\s+property=[\"']og:image[\"']\\s+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val titleTagPattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val audioSrcPattern = Pattern.compile("<audio[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val sourceSrcPattern = Pattern.compile("<source[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)

    /**
     * Validates and cleans user input URL.
     */
    fun sanitizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    /**
     * Checks if URL has a valid web audio/stream schema.
     */
    fun isValidStreamUrl(url: String): Boolean {
        val sanitized = sanitizeUrl(url)
        return try {
            val parsed = sanitized.toHttpUrlOrNull()
            parsed != null && (parsed.scheme == "http" || parsed.scheme == "https") && parsed.host.isNotBlank()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves audio stream metadata (title, artist, cover, bitrate, duration) from direct link or webpage.
     */
    suspend fun resolveStream(rawUrl: String): Result<StreamMetadata> = withContext(Dispatchers.IO) {
        val targetUrl = sanitizeUrl(rawUrl)
        if (!isValidStreamUrl(targetUrl)) {
            return@withContext Result.failure(IllegalArgumentException("Некорректная ссылка на аудиопоток"))
        }

        try {
            // 1. Try HEAD request first for fast header examination
            val headRequest = Request.Builder()
                .url(targetUrl)
                .head()
                .header("User-Agent", "OpenFy/1.0 (Android; Safe Streamer)")
                .header("Icy-MetaData", "1")
                .build()

            var finalUrl = targetUrl
            var contentType: String? = null
            var icyName: String? = null
            var icyGenre: String? = null
            var icyBr: String? = null
            var contentLength: Long = -1L

            try {
                httpClient.newCall(headRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        finalUrl = response.request.url.toString()
                        contentType = response.header("Content-Type")
                        icyName = response.header("icy-name")
                        icyGenre = response.header("icy-genre")
                        icyBr = response.header("icy-br")
                        contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
                    }
                }
            } catch (_: Exception) {
                // If HEAD fails or method not allowed, fallback to GET range
            }

            val isHtml = contentType?.contains("text/html", ignoreCase = true) == true

            if (isHtml) {
                // Fetch first 32KB of HTML to extract metadata & audio tags
                val getHtmlRequest = Request.Builder()
                    .url(finalUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) OpenFy/1.0")
                    .header("Range", "bytes=0-32768")
                    .build()

                var htmlBody = ""
                try {
                    httpClient.newCall(getHtmlRequest).execute().use { response ->
                        if (response.isSuccessful) {
                            htmlBody = response.body?.string() ?: ""
                        }
                    }
                } catch (_: Exception) {}

                val ogTitle = extractPattern(ogTitlePattern, htmlBody)
                val ogImage = extractPattern(ogImagePattern, htmlBody)
                val pageTitle = extractPattern(titleTagPattern, htmlBody)?.trim()
                val embeddedAudio = extractPattern(audioSrcPattern, htmlBody) ?: extractPattern(sourceSrcPattern, htmlBody)

                val effectiveStreamUrl = if (!embeddedAudio.isNullOrBlank()) {
                    resolveRelativeUrl(finalUrl, embeddedAudio)
                } else {
                    finalUrl
                }

                val title = ogTitle ?: pageTitle ?: extractNameFromUrl(finalUrl)
                val host = Uri.parse(finalUrl).host ?: "Web Stream"

                return@withContext Result.success(
                    StreamMetadata(
                        streamUrl = effectiveStreamUrl,
                        title = title,
                        artist = host,
                        album = "Web Stream",
                        coverUrl = ogImage,
                        contentType = contentType ?: "audio/mpeg",
                        isLive = contentLength <= 0
                    )
                )
            }

            // Direct Audio Stream Metadata Extraction
            val fallbackTitle = extractNameFromUrl(finalUrl)
            val title = icyName ?: fallbackTitle
            val artist = icyGenre ?: Uri.parse(finalUrl).host ?: "Radio Stream"
            val bitrate = icyBr?.toIntOrNull()

            val isLiveStream = contentLength <= 0L

            Result.success(
                StreamMetadata(
                    streamUrl = finalUrl,
                    title = title,
                    artist = artist,
                    album = "OpenFy Streamer",
                    contentType = contentType ?: "audio/mpeg",
                    bitrateKbps = bitrate,
                    durationMs = 0L,
                    isLive = isLiveStream
                )
            )
        } catch (e: Exception) {
            // Fallback for offline/unreachable preview: return valid basic metadata for ExoPlayer to attempt
            val fallbackTitle = extractNameFromUrl(targetUrl)
            val host = Uri.parse(targetUrl).host ?: "Audio Stream"
            Result.success(
                StreamMetadata(
                    streamUrl = targetUrl,
                    title = fallbackTitle,
                    artist = host,
                    album = "Web Stream",
                    isLive = true
                )
            )
        }
    }

    private fun extractPattern(pattern: Pattern, text: String): String? {
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun resolveRelativeUrl(baseUrl: String, relativeUrl: String): String {
        return try {
            if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
                relativeUrl
            } else {
                val baseUri = Uri.parse(baseUrl)
                if (relativeUrl.startsWith("//")) {
                    "${baseUri.scheme}:$relativeUrl"
                } else if (relativeUrl.startsWith("/")) {
                    "${baseUri.scheme}://${baseUri.host}$relativeUrl"
                } else {
                    baseUrl.substringBeforeLast('/') + "/" + relativeUrl
                }
            }
        } catch (_: Exception) {
            relativeUrl
        }
    }

    private fun extractNameFromUrl(url: String): String {
        return try {
            val parsed = url.toHttpUrlOrNull()
            val lastSegment = parsed?.pathSegments?.lastOrNull()
            if (!lastSegment.isNullOrBlank()) {
                val decoded = URLDecoder.decode(lastSegment, StandardCharsets.UTF_8.name())
                val clean = decoded.substringBeforeLast('.')
                if (clean.isNotBlank() && clean != "stream" && clean != "live" && clean != "audio") {
                    clean.replace('_', ' ').replace('-', ' ').replaceFirstChar { it.uppercase() }
                } else {
                    parsed.host
                }
            } else {
                parsed?.host ?: "Online Audio Stream"
            }
        } catch (_: Exception) {
            "Online Audio Stream"
        }
    }
}
