package com.example.openfy.core.audio.data

import android.content.Context
import com.example.openfy.core.audio.model.LyricLine
import com.example.openfy.core.audio.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.regex.Pattern

class LyricsRepository(private val context: Context) {

    // Supports: [mm:ss], [m:ss], [mm:ss.xx], [mm:ss.xxx], [mm:ss:xx], [hh:mm:ss.xx]
    private val timestampPattern = Pattern.compile("\\[(?:(\\d{1,2}):)?(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?]")

    suspend fun getLyricsForSong(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        // 1. Try local .lrc / .txt file located near the audio file
        val lrcFromDisk = findLocalLrcFile(song)
        if (!lrcFromDisk.isNullOrEmpty()) {
            return@withContext lrcFromDisk
        }

        // 2. Try streaming embedded lyrics from ID3v2 / FLAC / M4A metadata
        val embeddedLyrics = extractEmbeddedLyrics(song)
        if (!embeddedLyrics.isNullOrBlank()) {
            val parsed = parseLrc(embeddedLyrics)
            if (parsed.isNotEmpty()) {
                return@withContext parsed
            } else {
                return@withContext synthesizeTimestampsForPlainText(embeddedLyrics, song.durationMs)
            }
        }

        emptyList()
    }

    /**
     * Searches for a matching .lrc or .txt lyrics file in the directory containing the song.
     * Checks multiple name variants (filename, song title, artist - title, title - artist).
     */
    private fun findLocalLrcFile(song: Song): List<LyricLine>? {
        val audioPath = song.path
        if (audioPath.isBlank()) return null

        try {
            val audioFile = File(audioPath)
            val parentDir = audioFile.parentFile ?: return null
            if (!parentDir.exists() || !parentDir.canRead()) return null

            val baseName = audioFile.nameWithoutExtension.trim()
            val songTitle = song.title.trim()
            val songArtist = song.artist.trim()

            val dirFiles = parentDir.listFiles() ?: emptyArray()

            // 1. Check exact baseName matches (case-insensitive extension)
            val exactMatch = dirFiles.firstOrNull { file ->
                file.isFile && (file.extension.equals("lrc", ignoreCase = true) || file.extension.equals("txt", ignoreCase = true)) &&
                        file.nameWithoutExtension.equals(baseName, ignoreCase = true)
            }
            if (exactMatch != null) {
                return readAndParseFile(exactMatch, song.durationMs)
            }

            // 2. Check title-based matches (e.g. "SongTitle.lrc")
            if (songTitle.isNotBlank() && !songTitle.equals("Unknown Title", ignoreCase = true)) {
                val titleMatch = dirFiles.firstOrNull { file ->
                    file.isFile && (file.extension.equals("lrc", ignoreCase = true) || file.extension.equals("txt", ignoreCase = true)) &&
                            file.nameWithoutExtension.equals(songTitle, ignoreCase = true)
                }
                if (titleMatch != null) {
                    return readAndParseFile(titleMatch, song.durationMs)
                }

                // 3. Check "Artist - Title.lrc" or "Title - Artist.lrc"
                val artistTitleMatch = dirFiles.firstOrNull { file ->
                    if (!file.isFile) return@firstOrNull false
                    val ext = file.extension
                    if (!ext.equals("lrc", ignoreCase = true) && !ext.equals("txt", ignoreCase = true)) return@firstOrNull false
                    val name = file.nameWithoutExtension
                    name.equals("$songArtist - $songTitle", ignoreCase = true) ||
                            name.equals("$songTitle - $songArtist", ignoreCase = true)
                }
                if (artistTitleMatch != null) {
                    return readAndParseFile(artistTitleMatch, song.durationMs)
                }

                // 4. Fuzzy fallback: any .lrc file in the same folder that contains the title
                val fuzzyMatch = dirFiles.firstOrNull { file ->
                    file.isFile && file.extension.equals("lrc", ignoreCase = true) &&
                            (file.nameWithoutExtension.contains(songTitle, ignoreCase = true) ||
                                    songTitle.contains(file.nameWithoutExtension, ignoreCase = true))
                }
                if (fuzzyMatch != null) {
                    return readAndParseFile(fuzzyMatch, song.durationMs)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun readAndParseFile(file: File, durationMs: Long): List<LyricLine>? {
        return try {
            val bytes = file.readBytes()
            val text = decodeText(bytes)
            val parsed = parseLrc(text)
            if (parsed.isNotEmpty()) {
                parsed
            } else {
                synthesizeTimestampsForPlainText(text, durationMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes text bytes with automatic BOM and charset detection (UTF-8, UTF-16, Windows-1251).
     */
    private fun decodeText(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        // Check UTF-8 BOM
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        // Check UTF-16LE BOM
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        // Check UTF-16BE BOM
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

        // Attempt strict UTF-8 decoding
        val utf8Decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)

        return try {
            utf8Decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (e: Exception) {
            // Fallback to Windows-1251 (standard for Cyrillic files) or ISO-8859-1
            try {
                String(bytes, Charset.forName("windows-1251"))
            } catch (e2: Exception) {
                String(bytes, Charsets.ISO_8859_1)
            }
        }
    }

    /**
     * Parses standard and extended LRC format into timed lines.
     */
    fun parseLrc(lrcContent: String): List<LyricLine> {
        // Strip BOM if present in string
        val cleanContent = lrcContent.trimStart('\uFEFF')
        val lines = cleanContent.lines()
        val result = mutableListOf<LyricLine>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Skip metadata header tags like [ar:Artist], [ti:Title], [length:03:45], etc.
            if (trimmed.startsWith("[ar:", ignoreCase = true) ||
                trimmed.startsWith("[ti:", ignoreCase = true) ||
                trimmed.startsWith("[al:", ignoreCase = true) ||
                trimmed.startsWith("[by:", ignoreCase = true) ||
                trimmed.startsWith("[length:", ignoreCase = true) ||
                trimmed.startsWith("[offset:", ignoreCase = true) ||
                trimmed.startsWith("[re:", ignoreCase = true) ||
                trimmed.startsWith("[ve:", ignoreCase = true)
            ) {
                continue
            }

            val matcher = timestampPattern.matcher(trimmed)
            val timestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            while (matcher.find()) {
                val hourStr = matcher.group(1)
                val minStr = matcher.group(2) ?: "0"
                val secStr = matcher.group(3) ?: "0"
                val fracStr = matcher.group(4)

                val hours = hourStr?.toLongOrNull() ?: 0L
                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L

                val fractionMs = when {
                    fracStr.isNullOrEmpty() -> 0L
                    fracStr.length == 1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
                    fracStr.length == 2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
                    else -> (fracStr.take(3).toLongOrNull() ?: 0L)
                }

                val totalMs = (hours * 3600_000L) + (minutes * 60_000L) + (seconds * 1000L) + fractionMs
                timestamps.add(totalMs)
                lastMatchEnd = matcher.end()
            }

            if (timestamps.isNotEmpty()) {
                val lyricText = trimmed.substring(lastMatchEnd).trim()
                for (time in timestamps) {
                    result.add(LyricLine(timeMs = time, text = lyricText))
                }
            }
        }

        return result.sortedBy { it.timeMs }
    }

    private fun synthesizeTimestampsForPlainText(text: String, durationMs: Long): List<LyricLine> {
        val clean = text.trimStart('\uFEFF')
        val rawLines = clean.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (rawLines.isEmpty()) return emptyList()

        val interval = if (durationMs > 0) durationMs / rawLines.size else 4000L
        return rawLines.mapIndexed { index, line ->
            LyricLine(
                timeMs = index * interval,
                text = line
            )
        }
    }

    /**
     * Extracts embedded lyrics from an audio file by streaming through container frames.
     * Skips large frames (such as multi-megabyte APIC album art) efficiently.
     */
    private fun extractEmbeddedLyrics(song: Song): String? {
        try {
            val rawStream: InputStream? = if (song.path.isNotBlank() && File(song.path).exists()) {
                File(song.path).inputStream()
            } else {
                context.contentResolver.openInputStream(song.contentUri)
            }

            rawStream?.let { stream ->
                BufferedInputStream(stream, 65536).use { bufStream ->
                    bufStream.mark(16)
                    val magic = ByteArray(4)
                    val read = readFully(bufStream, magic, 4)
                    bufStream.reset()
                    if (read < 4) return null

                    // 1. MP3 ID3v2 tag
                    if (magic[0] == 0x49.toByte() && magic[1] == 0x44.toByte() && magic[2] == 0x33.toByte()) {
                        return parseId3v2Stream(bufStream)
                    }

                    // 2. FLAC Vorbis Comment
                    if (magic[0] == 0x66.toByte() && magic[1] == 0x4C.toByte() && magic[2] == 0x61.toByte() && magic[3] == 0x43.toByte()) {
                        return parseFlacStream(bufStream)
                    }

                    // 3. M4A / MP4 atom box
                    if ((magic[0] == 0x4D.toByte() && magic[1] == 0x34.toByte() && magic[2] == 0x41.toByte()) ||
                        (magic[4.coerceAtMost(magic.size - 1)] == 0x66.toByte())
                    ) {
                        return parseM4aStream(bufStream)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Streams through ID3v2 frames, skipping non-lyrics frames (e.g. APIC) using skipFully.
     */
    private fun parseId3v2Stream(stream: InputStream): String? {
        val header = ByteArray(10)
        if (readFully(stream, header, 10) < 10) return null
        if (header[0] != 0x49.toByte() || header[1] != 0x44.toByte() || header[2] != 0x33.toByte()) return null

        val majorVersion = header[3].toInt() and 0xFF
        val flags = header[5].toInt() and 0xFF
        val tagSize = ((header[6].toInt() and 0x7F) shl 21) or
                ((header[7].toInt() and 0x7F) shl 14) or
                ((header[8].toInt() and 0x7F) shl 7) or
                (header[9].toInt() and 0x7F)

        var remainingTagBytes = tagSize.toLong()

        // Skip extended header if present
        if ((flags and 0x40) != 0) {
            val extHeaderSizeBuf = ByteArray(4)
            if (readFully(stream, extHeaderSizeBuf, 4) == 4) {
                val extSize = if (majorVersion == 4) {
                    ((extHeaderSizeBuf[0].toInt() and 0x7F) shl 21) or
                            ((extHeaderSizeBuf[1].toInt() and 0x7F) shl 14) or
                            ((extHeaderSizeBuf[2].toInt() and 0x7F) shl 7) or
                            (extHeaderSizeBuf[3].toInt() and 0x7F)
                } else {
                    ((extHeaderSizeBuf[0].toInt() and 0xFF) shl 24) or
                            ((extHeaderSizeBuf[1].toInt() and 0xFF) shl 16) or
                            ((extHeaderSizeBuf[2].toInt() and 0xFF) shl 8) or
                            (extHeaderSizeBuf[3].toInt() and 0xFF)
                }
                val toSkip = (extSize - 4).coerceAtLeast(0).toLong()
                skipFully(stream, toSkip)
                remainingTagBytes -= (4 + toSkip)
            }
        }

        var candidateCommentLyrics: String? = null

        val frameHeaderSize = if (majorVersion == 2) 6 else 10
        val frameHeaderBuf = ByteArray(frameHeaderSize)

        while (remainingTagBytes > frameHeaderSize) {
            val bytesRead = readFully(stream, frameHeaderBuf, frameHeaderSize)
            if (bytesRead < frameHeaderSize) break
            remainingTagBytes -= frameHeaderSize

            // Null padding reached
            if (frameHeaderBuf[0] == 0.toByte()) break

            val frameId = if (majorVersion == 2) {
                String(frameHeaderBuf, 0, 3, Charsets.ISO_8859_1)
            } else {
                String(frameHeaderBuf, 0, 4, Charsets.ISO_8859_1)
            }

            val frameSize: Int = if (majorVersion == 2) {
                ((frameHeaderBuf[3].toInt() and 0xFF) shl 16) or
                        ((frameHeaderBuf[4].toInt() and 0xFF) shl 8) or
                        (frameHeaderBuf[5].toInt() and 0xFF)
            } else if (majorVersion == 4) {
                ((frameHeaderBuf[4].toInt() and 0x7F) shl 21) or
                        ((frameHeaderBuf[5].toInt() and 0x7F) shl 14) or
                        ((frameHeaderBuf[6].toInt() and 0x7F) shl 7) or
                        (frameHeaderBuf[7].toInt() and 0x7F)
            } else {
                ((frameHeaderBuf[4].toInt() and 0xFF) shl 24) or
                        ((frameHeaderBuf[5].toInt() and 0xFF) shl 16) or
                        ((frameHeaderBuf[6].toInt() and 0xFF) shl 8) or
                        (frameHeaderBuf[7].toInt() and 0xFF)
            }

            if (frameSize <= 0 || frameSize > remainingTagBytes) {
                break
            }

            if (frameId == "USLT" || frameId == "ULT") {
                val data = ByteArray(frameSize)
                readFully(stream, data, frameSize)
                remainingTagBytes -= frameSize

                val lyrics = parseUsltFrameData(data)
                if (!lyrics.isNullOrBlank()) {
                    return lyrics
                }
            } else if (frameId == "COMM") {
                val data = ByteArray(frameSize)
                readFully(stream, data, frameSize)
                remainingTagBytes -= frameSize

                val comment = parseUsltFrameData(data)
                if (!comment.isNullOrBlank() && (comment.contains("\n") || comment.length > 80)) {
                    candidateCommentLyrics = comment
                }
            } else if (frameId == "TXXX") {
                val data = ByteArray(frameSize)
                readFully(stream, data, frameSize)
                remainingTagBytes -= frameSize

                val text = decodeText(data)
                if (text.contains("LYRICS", ignoreCase = true)) {
                    val parts = text.split("\u0000", limit = 2)
                    if (parts.size > 1 && parts[1].isNotBlank()) {
                        return parts[1].trim()
                    }
                }
            } else {
                // Efficiently skip other large frames (e.g. APIC image of 2MB)
                skipFully(stream, frameSize.toLong())
                remainingTagBytes -= frameSize
            }
        }

        return candidateCommentLyrics
    }

    private fun parseUsltFrameData(data: ByteArray): String? {
        if (data.size < 5) return null
        val encodingByte = data[0].toInt() and 0xFF
        val charset = when (encodingByte) {
            1 -> Charsets.UTF_16
            2 -> Charsets.UTF_16BE
            3 -> Charsets.UTF_8
            else -> Charsets.ISO_8859_1
        }

        // Header: 1 byte encoding + 3 bytes language = 4 bytes
        var offset = 4
        // Skip description null-terminated string
        while (offset < data.size && data[offset] != 0.toByte()) {
            offset++
        }
        if (offset < data.size && data[offset] == 0.toByte()) {
            offset++
            if (encodingByte == 1 && offset < data.size && data[offset] == 0.toByte()) {
                offset++
            }
        }

        if (offset < data.size) {
            val text = String(data, offset, data.size - offset, charset).trim()
            if (text.isNotBlank()) return text
        }
        return null
    }

    /**
     * Streams through FLAC metadata blocks, skipping PICTURE blocks and reading VORBIS_COMMENT.
     */
    private fun parseFlacStream(stream: InputStream): String? {
        val magic = ByteArray(4)
        if (readFully(stream, magic, 4) < 4) return null
        if (magic[0] != 0x66.toByte() || magic[1] != 0x4C.toByte() || magic[2] != 0x61.toByte() || magic[3] != 0x43.toByte()) {
            return null
        }

        val headerBuf = ByteArray(4)
        while (true) {
            if (readFully(stream, headerBuf, 4) < 4) break
            val isLast = (headerBuf[0].toInt() and 0x80) != 0
            val blockType = headerBuf[0].toInt() and 0x7F
            val blockSize = ((headerBuf[1].toInt() and 0xFF) shl 16) or
                    ((headerBuf[2].toInt() and 0xFF) shl 8) or
                    (headerBuf[3].toInt() and 0xFF)

            if (blockType == 4) { // VORBIS_COMMENT
                val data = ByteArray(blockSize)
                if (readFully(stream, data, blockSize) < blockSize) break
                val lyrics = parseVorbisComment(data)
                if (!lyrics.isNullOrBlank()) return lyrics
            } else {
                skipFully(stream, blockSize.toLong())
            }

            if (isLast) break
        }
        return null
    }

    private fun parseVorbisComment(buffer: ByteArray): String? {
        if (buffer.size < 8) return null
        var offset = 0
        val vendorLen = (buffer[offset].toInt() and 0xFF) or
                ((buffer[offset + 1].toInt() and 0xFF) shl 8) or
                ((buffer[offset + 2].toInt() and 0xFF) shl 16) or
                ((buffer[offset + 3].toInt() and 0xFF) shl 24)
        offset += 4 + vendorLen
        if (offset + 4 > buffer.size) return null

        val commentCount = (buffer[offset].toInt() and 0xFF) or
                ((buffer[offset + 1].toInt() and 0xFF) shl 8) or
                ((buffer[offset + 2].toInt() and 0xFF) shl 16) or
                ((buffer[offset + 3].toInt() and 0xFF) shl 24)
        offset += 4

        for (i in 0 until commentCount) {
            if (offset + 4 > buffer.size) break
            val commentLen = (buffer[offset].toInt() and 0xFF) or
                    ((buffer[offset + 1].toInt() and 0xFF) shl 8) or
                    ((buffer[offset + 2].toInt() and 0xFF) shl 16) or
                    ((buffer[offset + 3].toInt() and 0xFF) shl 24)
            offset += 4

            if (offset + commentLen <= buffer.size) {
                val comment = String(buffer, offset, commentLen, Charsets.UTF_8)
                if (comment.startsWith("LYRICS=", ignoreCase = true)) {
                    return comment.substringAfter("=").trim()
                }
                if (comment.startsWith("UNSYNCEDLYRICS=", ignoreCase = true)) {
                    return comment.substringAfter("=").trim()
                }
                offset += commentLen
            }
        }
        return null
    }

    /**
     * Reads up to 2MB for M4A/MP4 metadata search.
     */
    private fun parseM4aStream(stream: InputStream): String? {
        val buffer = ByteArray(512 * 1024)
        val read = stream.read(buffer)
        if (read <= 0) return null

        val lyrSignature = byteArrayOf(0xA9.toByte(), 0x6C.toByte(), 0x79.toByte(), 0x72.toByte())
        for (i in 0 until read - lyrSignature.size - 16) {
            var match = true
            for (j in lyrSignature.indices) {
                if (buffer[i + j] != lyrSignature[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                val dataOffset = i + 8
                if (dataOffset + 8 < read) {
                    val dataSize = ((buffer[dataOffset].toInt() and 0xFF) shl 24) or
                            ((buffer[dataOffset + 1].toInt() and 0xFF) shl 16) or
                            ((buffer[dataOffset + 2].toInt() and 0xFF) shl 8) or
                            (buffer[dataOffset + 3].toInt() and 0xFF)

                    val textStart = dataOffset + 16
                    val textLen = (dataSize - 16).coerceIn(0, read - textStart)
                    if (textLen > 0) {
                        val lyrics = String(buffer, textStart, textLen, Charsets.UTF_8).trim()
                        if (lyrics.isNotBlank()) return lyrics
                    }
                }
            }
        }
        return null
    }

    private fun readFully(stream: InputStream, buffer: ByteArray, length: Int): Int {
        var totalRead = 0
        while (totalRead < length) {
            val read = stream.read(buffer, totalRead, length - totalRead)
            if (read == -1) break
            totalRead += read
        }
        return totalRead
    }

    private fun skipFully(stream: InputStream, n: Long) {
        var remaining = n
        val skipBuf = ByteArray(8192)
        while (remaining > 0) {
            val skipped = stream.skip(remaining)
            if (skipped > 0) {
                remaining -= skipped
            } else {
                val read = stream.read(skipBuf, 0, remaining.coerceAtMost(skipBuf.size.toLong()).toInt())
                if (read <= 0) break
                remaining -= read
            }
        }
    }
}
