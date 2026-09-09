package com.example.openfy.features.community.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

object LocalShareServer {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var currentPayload: SharePayload? = null
    private var currentPort: Int = 8888

    /**
     * Starts serving the given [payload] over local Wi-Fi / Hotspot network.
     * Returns the direct HTTP URL.
     */
    suspend fun startServer(payload: SharePayload, port: Int = 8888): Result<String> = withContext(Dispatchers.IO) {
        try {
            stopServer()
            currentPayload = payload
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
                val path = if (parts.size > 1) parts[1] else "/"

                val payload = currentPayload
                if (payload == null) {
                    val body = "No active share payload."
                    val bytes = body.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    val os = socket.getOutputStream()
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
                            "Content-Type: application/octet-stream\r\n" +
                            "Content-Disposition: attachment; filename=\"$filename\"\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"

                    val os = socket.getOutputStream()
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
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: ${bytes.size}\r\n" +
                        "Connection: close\r\n\r\n"

                val os = socket.getOutputStream()
                os.write(headers.toByteArray(Charsets.UTF_8))
                os.write(bytes)
                os.flush()
            } catch (_: Exception) {}
        }
    }

    private fun getLocalIpAddress(): String? {
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
