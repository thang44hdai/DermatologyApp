package com.example.safeaid.core.service

import com.example.safeaid.core.request.ChatRequest
import com.example.safeaid.core.response.SocketResponse
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatStreamService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val WS_URL = "ws://192.168.100.178:8000/chat/ws"
    }

    private var webSocket: WebSocket? = null
    private val _messageFlow = MutableSharedFlow<SocketResponse>()
    val messageFlow: Flow<SocketResponse> = _messageFlow.asSharedFlow()

    fun connect(token: String): Flow<SocketResponse> = callbackFlow {
        val wsRequest = Request.Builder()
            .url("$WS_URL?token=$token")
            .build()

        webSocket = okHttpClient.newWebSocket(wsRequest, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // WebSocket đã mở, sẵn sàng nhận tin nhắn
                trySend(
                    SocketResponse.Status(
                        type = "status",
                        status = "connected"
                    )
                )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val socketResponse = parseSocketResponse(text)
                    if (socketResponse != null)
                        trySend(socketResponse)
                } catch (e: Exception) {
                    trySend(
                        SocketResponse.Error(
                            type = "error",
                            error = "Parse error",
                            detail = e.message
                        )
                    )
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                trySend(
                    SocketResponse.Error(
                        type = "error",
                        error = "WebSocket failure",
                        detail = t.message
                    )
                )
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
        })

        awaitClose {
            disconnect()
        }
    }

    fun sendMessage(request: ChatRequest) {
        val jsonMessage = gson.toJson(request)
        webSocket?.send(jsonMessage)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closed")
        webSocket = null
    }

    private fun parseSocketResponse(json: String): SocketResponse? {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        val type = jsonObject.get("type")?.asString ?: ""

        if (type == "ping" || type == "pong")
            return null
        return when (type) {
            "status" -> gson.fromJson(json, SocketResponse.Status::class.java)
            "start" -> gson.fromJson(json, SocketResponse.Start::class.java)
            "chunk" -> gson.fromJson(json, SocketResponse.Chunk::class.java)
            "end" -> gson.fromJson(json, SocketResponse.End::class.java)
            "error" -> gson.fromJson(json, SocketResponse.Error::class.java)
            else -> SocketResponse.Error(
                type = "error",
                error = "Unknown type",
                detail = "Received unknown response type: $type"
            )
        }
    }
}
