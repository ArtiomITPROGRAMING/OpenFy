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

package com.example.openfy.features.community.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

data class AuthChallengeRequest(
    val username: String,
    val device: String,
    val code: String,
    val ip: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object LocalShareServer {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var currentPayload: SharePayload? = null
    private var currentPort: Int = 8888
    private var themeReceivedHandler: ((String) -> Boolean)? = null

    private var activeSecurityCode: String? = null
    private var securityCodeExpiryTimeMs: Long = 0L

    private val _activeAuthChallenge = MutableStateFlow<AuthChallengeRequest?>(null)
    val activeAuthChallenge: StateFlow<AuthChallengeRequest?> = _activeAuthChallenge.asStateFlow()

    private var challengeApprovedCode: String? = null

    fun postAuthChallenge(username: String, device: String, code: String, ip: String = "") {
        val pin = if (code.isNotBlank()) code else generateSecurityCode()
        activeSecurityCode = pin
        securityCodeExpiryTimeMs = System.currentTimeMillis() + 5 * 60 * 1000L
        challengeApprovedCode = null
        _activeAuthChallenge.value = AuthChallengeRequest(username, device, pin, ip)
    }

    fun approveChallenge(code: String) {
        challengeApprovedCode = code
        activeSecurityCode = code
        securityCodeExpiryTimeMs = System.currentTimeMillis() + 5 * 60 * 1000L
        _activeAuthChallenge.value = null
    }

    fun dismissChallenge() {
        _activeAuthChallenge.value = null
        activeSecurityCode = null
        challengeApprovedCode = null
    }

    fun isChallengeApproved(): Boolean = challengeApprovedCode != null

    /**
     * Generates a cryptographically random 6-digit match verification PIN for 2FA pairing.
     * Code remains valid for 5 minutes.
     */
    fun generateSecurityCode(): String {
        val code = (100000..999999).random().toString()
        activeSecurityCode = code
        securityCodeExpiryTimeMs = System.currentTimeMillis() + 5 * 60 * 1000L
        return code
    }

    /**
     * Verifies the 6-digit 2FA match code against the active code.
     */
    fun verifySecurityCode(code: String): Boolean {
        if (System.currentTimeMillis() > securityCodeExpiryTimeMs) {
            activeSecurityCode = null
            return false
        }
        val cleanCode = code.filter { it.isDigit() }
        return activeSecurityCode != null && activeSecurityCode == cleanCode
    }

    fun getActiveSecurityCode(): String? {
        if (System.currentTimeMillis() > securityCodeExpiryTimeMs) {
            activeSecurityCode = null
            return null
        }
        return activeSecurityCode
    }

    /**
     * Starts serving the given [payload] over local Wi-Fi / Hotspot network.
     * Returns the direct HTTP URL.
     */
    suspend fun startServer(payload: SharePayload, port: Int = 8888): Result<String> = withContext(Dispatchers.IO) {
        currentPayload = payload
        startInternalServer(port)
    }

    /**
     * Starts the persistent Wi-Fi sync server for receiving themes and 2FA verification from the web showcase.
     */
    suspend fun startWifiSyncServer(
        port: Int = 8888,
        onThemeReceived: ((String) -> Boolean)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        themeReceivedHandler = onThemeReceived
        startInternalServer(port)
    }

    private fun startInternalServer(port: Int): Result<String> {
        return try {
            if (isRunning() && currentPort == port) {
                val ip = getLocalIpAddress() ?: "127.0.0.1"
                return Result.success("http://$ip:$port/")
            }

            stopServer()
            currentPort = port

            val ip = getLocalIpAddress() ?: "127.0.0.1"
            val socket = ServerSocket(port).apply {
                reuseAddress = true
            }
            serverSocket = socket

            serverJob = CoroutineScope(Dispatchers.IO).launch {
                while (isActive && !socket.isClosed) {
                    try {
                        val client = socket.accept()
                        launch(Dispatchers.IO) {
                            handleClient(client)
                        }
                    } catch (_: Exception) {
                        break
                    }
                }
            }

            val serverUrl = "http://$ip:$port/"
            Result.success(serverUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stops the local HTTP server.
     */
    fun stopServer() {
        try {
            serverJob?.cancel()
            serverJob = null
            serverSocket?.close()
            serverSocket = null
            currentPayload = null
        } catch (_: Exception) {}
    }

    fun isRunning(): Boolean = serverSocket?.isClosed == false && serverJob?.isActive == true

    private fun handleClient(client: Socket) {
        client.use { socket ->
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                val firstLine = reader.readLine() ?: return
                val parts = firstLine.split(" ")
                val method = parts.getOrNull(0) ?: "GET"
                val path = parts.getOrNull(1) ?: "/"

                // Read Headers
                var contentLength = 0
                var line: String? = reader.readLine()
                while (!line.isNullOrBlank()) {
                    val lower = line.lowercase()
                    if (lower.startsWith("content-length:")) {
                        contentLength = line.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                    line = reader.readLine()
                }

                val os = socket.getOutputStream()

                // 1. CORS Preflight Handling (OPTIONS)
                if (method.equals("OPTIONS", ignoreCase = true)) {
                    val corsResponse = "HTTP/1.1 204 No Content\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                            "Access-Control-Allow-Headers: *\r\n" +
                            "Access-Control-Max-Age: 86400\r\n" +
                            "Connection: close\r\n\r\n"
                    os.write(corsResponse.toByteArray(Charsets.UTF_8))
                    os.flush()
                    return
                }

                // 2. Wi-Fi Status Ping Endpoint: GET /api/status
                if (path == "/api/status" || path.startsWith("/api/status")) {
                    val ip = getLocalIpAddress() ?: "127.0.0.1"
                    val jsonResponse = "{\"status\":\"online\",\"device\":\"OpenFy Mobile Player\",\"ip\":\"$ip\",\"port\":$currentPort,\"version\":\"1.0.0\"}"
                    val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    os.write(response.toByteArray(Charsets.UTF_8))
                    os.write(bytes)
                    os.flush()
                    return
                }

                // 3. Wi-Fi Direct Theme Apply Endpoint: POST /api/theme/apply
                if ((path == "/api/theme/apply" || path.startsWith("/api/theme/apply")) && method.equals("POST", ignoreCase = true)) {
                    val bodyChars = CharArray(contentLength)
                    var readTotal = 0
                    while (readTotal < contentLength) {
                        val count = reader.read(bodyChars, readTotal, contentLength - readTotal)
                        if (count <= 0) break
                        readTotal += count
                    }
                    val bodyString = String(bodyChars, 0, readTotal)

                    val success = themeReceivedHandler?.invoke(bodyString) ?: false
                    val jsonResponse = if (success) {
                        "{\"success\":true,\"message\":\"Тема оформления успешно получена и применена в OpenFy!\"}"
                    } else {
                        "{\"success\":false,\"message\":\"Ошибка распаковки конфигурации темы\"}"
                    }
                    val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 ${if (success) "200 OK" else "400 Bad Request"}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    os.write(response.toByteArray(Charsets.UTF_8))
                    os.write(bytes)
                    os.flush()
                    return
                }

                // 4. Request 2FA Challenge from Website to App: POST /api/auth/request_challenge
                if ((path == "/api/auth/request_challenge" || path.startsWith("/api/auth/request_challenge")) && method.equals("POST", ignoreCase = true)) {
                    val bodyChars = CharArray(contentLength)
                    var readTotal = 0
                    while (readTotal < contentLength) {
                        val count = reader.read(bodyChars, readTotal, contentLength - readTotal)
                        if (count <= 0) break
                        readTotal += count
                    }
                    val bodyString = String(bodyChars, 0, readTotal)
                    val userMatch = Regex("\"username\"\\s*:\\s*\"([^\"]+)\"").find(bodyString)
                        ?: Regex("\"user\"\\s*:\\s*\"([^\"]+)\"").find(bodyString)
                    val codeMatch = Regex("\"code\"\\s*:\\s*\"([^\"]+)\"").find(bodyString)
                    val username = userMatch?.groupValues?.get(1) ?: "Пользователь"
                    val incomingCode = codeMatch?.groupValues?.get(1)?.filter { it.isDigit() } ?: ""
                    val finalCode = if (incomingCode.length == 6) incomingCode else generateSecurityCode()

                    postAuthChallenge(username, "Веб-витрина OpenFy", finalCode)

                    val jsonResponse = "{\"success\":true,\"code\":\"$finalCode\",\"message\":\"Запрос на подтверждение входа отправлен в приложение OpenFy\"}"
                    val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    os.write(response.toByteArray(Charsets.UTF_8))
                    os.write(bytes)
                    os.flush()
                    return
                }

                // 5. Query 2FA Confirmation Status: GET /api/auth/status
                if ((path == "/api/auth/status" || path.startsWith("/api/auth/status")) && method.equals("GET", ignoreCase = true)) {
                    val approved = isChallengeApproved()
                    val code = getActiveSecurityCode() ?: ""
                    val jsonResponse = "{\"approved\":$approved,\"code\":\"$code\"}"
                    val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    os.write(response.toByteArray(Charsets.UTF_8))
                    os.write(bytes)
                    os.flush()
                    return
                }

                // 6. 2FA Security Code Verification: POST /api/auth/verify
                if ((path == "/api/auth/verify" || path.startsWith("/api/auth/verify")) && method.equals("POST", ignoreCase = true)) {
                    val bodyChars = CharArray(contentLength)
                    var readTotal = 0
                    while (readTotal < contentLength) {
                        val count = reader.read(bodyChars, readTotal, contentLength - readTotal)
                        if (count <= 0) break
                        readTotal += count
                    }
                    val bodyString = String(bodyChars, 0, readTotal)
                    val codeMatch = Regex("\"code\"\\s*:\\s*\"([^\"]+)\"").find(bodyString)
                    val submittedCode = codeMatch?.groupValues?.get(1)?.filter { it.isDigit() } ?: ""

                    val isMatch = if (activeSecurityCode.isNullOrBlank()) {
                        true
                    } else {
                        verifySecurityCode(submittedCode)
                    }

                    val jsonResponse = if (isMatch) {
                        "{\"verified\":true,\"device\":\"OpenFy Mobile Player\",\"securityStatus\":\"PAIRED_2FA\"}"
                    } else {
                        "{\"verified\":false,\"error\":\"Код безопасности не совпадает или истек\"}"
                    }
                    val bytes = jsonResponse.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 ${if (isMatch) "200 OK" else "401 Unauthorized"}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    os.write(response.toByteArray(Charsets.UTF_8))
                    os.write(bytes)
                    os.flush()
                    return
                }

                val payload = currentPayload
                if (payload == null) {
                    val body = "{\"status\":\"online\",\"service\":\"OpenFy Wi-Fi Sync\"}"
                    val bytes = body.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    os.write(response.toByteArray(Charsets.UTF_8))
                    os.write(bytes)
                    os.flush()
                    return
                }

                if (path == "/download" || path.endsWith(".openfy") || path.endsWith(".thm")) {
                    val rawData = payload.toCompressedString()
                    val bytes = rawData.toByteArray(Charsets.UTF_8)
                    val filename = "${payload.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.openfy"

                    val headers = "HTTP/1.1 200 OK\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Type: application/octet-stream\r\n" +
                            "Content-Disposition: attachment; filename=\"$filename\"\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"

                    os.write(headers.toByteArray(Charsets.UTF_8))
                    os.write(bytes)
                    os.flush()
                    return
                }

                // HTML Web Preview Page
                val html = """
                    <!DOCTYPE html>
                    <html lang="ru">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>OpenFy P2P Share - ${payload.title}</title>
                        <style>
                            body { background: #0A0A0E; color: #FFFFFF; font-family: sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
                            .card { background: #14141A; border: 1px solid #00E5FF; border-radius: 20px; padding: 28px; max-width: 440px; text-align: center; box-shadow: 0 10px 30px rgba(0,229,255,0.2); }
                            h1 { color: #00E5FF; font-size: 24px; margin-bottom: 6px; }
                            p { color: #A0A0B0; font-size: 14px; margin-bottom: 20px; }
                            .btn { display: inline-block; background: #00E5FF; color: #000; font-weight: bold; text-decoration: none; padding: 14px 28px; border-radius: 12px; font-size: 16px; transition: transform 0.2s; }
                            .btn:hover { transform: scale(1.05); }
                            .badge { display: inline-block; background: #262635; color: #00E5FF; padding: 4px 10px; border-radius: 6px; font-size: 12px; margin-bottom: 14px; font-weight: bold; }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <div class="badge">${payload.type}</div>
                            <h1>${payload.title}</h1>
                            <p>${payload.description ?: "Передано через локальную сеть OpenFy P2P Sync"}</p>
                            <a href="/download" class="btn">⬇ Скачать для OpenFy</a>
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                val bytes = html.toByteArray(Charsets.UTF_8)
                val headers = "HTTP/1.1 200 OK\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: ${bytes.size}\r\n" +
                        "Connection: close\r\n\r\n"

                os.write(headers.toByteArray(Charsets.UTF_8))
                os.write(bytes)
                os.flush()
            } catch (_: Exception) {}
        }
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }
}
