package com.example.safeaid.screens.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.request.ChatRequest
import com.example.safeaid.core.response.Session
import com.example.safeaid.core.response.SocketResponse
import com.example.safeaid.core.response.Source
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.service.ChatStreamService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.removeVietnameseAccents
import com.example.safeaid.pref.AppPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val apiService: ApiService,
    private val chatStreamService: ChatStreamService,
    private val appPreference: AppPreference
) : BaseViewModel<ChatState, ChatEvent>() {

    private val _conversations = MutableStateFlow<List<Session>>(emptyList())
    val conversations: StateFlow<List<Session>> = _conversations

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus

    private var allSessions = listOf<Session>()
    private var currentStreamingMessageId: String? = null
    private val streamingContent = StringBuilder()

    init {
        // Tự động connect WebSocket và load conversations khi khởi tạo
        connectWebSocket()
        loadConversations()
    }

    private fun connectWebSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = appPreference.getToken().first()
            chatStreamService.connect(token)
                .catch { e ->
                    _isConnected.value = false
                    addErrorMessage("Lỗi kết nối WebSocket: ${e.message}")
                }
                .collect { response ->
                    when (response) {
                        is SocketResponse.Status -> {
                            when (response.status) {
                                "connected" -> {
                                    _isConnected.value = true
                                    _connectionStatus.value = null
                                }
                                "disconnected" -> {
                                    _isConnected.value = false
                                    _connectionStatus.value = "Mất kết nối..."
                                }
                                "reconnecting" -> {
                                    _isConnected.value = false
                                    _connectionStatus.value = "Đang kết nối lại..."
                                }
                            }
                        }
                        else -> handleSocketResponse(response)
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatStreamService.disconnect()
    }

    fun loadConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getListConversation()
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        val sessions = response.sessions
                        allSessions = sessions
                        _conversations.value = sessions
                    }
                    result.doIfFailure {
                        val mockSessions = listOf(
                            Session(
                                id = "1",
                                title = "Thuốc đau bụng phù hợp cho người...",
                                lastMessage = null,
                                messageCount = "5"
                            ),
                            Session(
                                id = "2",
                                title = "Chuẩn đoán bệnh đau lưng",
                                lastMessage = null,
                                messageCount = "3"
                            ),
                            Session(
                                id = "3",
                                title = "Chuỗi nhà thuốc uy tín",
                                lastMessage = null,
                                messageCount = "8"
                            ),
                            Session(
                                id = "4",
                                title = "Thuốc tránh thai an toàn",
                                lastMessage = null,
                                messageCount = "2"
                            )
                        )
                        allSessions = mockSessions
                        _conversations.value = mockSessions
                    }
                }
            )
        }
    }

    fun searchConversations(query: String) {
        if (query.isEmpty()) {
            _conversations.value = allSessions
        } else {
            val normalizedQuery = query.removeVietnameseAccents()

            val filtered = allSessions.filter { session ->
                val title = session.title ?: ""
                title.removeVietnameseAccents()
                    .contains(normalizedQuery, ignoreCase = true)
            }

            _conversations.value = filtered
        }
    }

    fun selectConversation(session: Session) {
        _currentSession.value = session
        _messages.value = emptyList()
        loadSessionMessages(session.id ?: "")
    }

    private fun loadSessionMessages(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            ApiCaller.safeApiCall(
                apiCall = {
                    apiService.getDetailChatBot(sessionId)
                },
                callback = { result ->
                    result.doIfSuccess { response ->
                        val chatMessages = response.messages.map { msg ->
                            ChatMessage(
                                id = msg.id ?: UUID.randomUUID().toString(),
                                content = msg.content ?: "",
                                isSender = msg.role == "user",
                                timestamp = System.currentTimeMillis(),
                                sources = if (msg.role == "assistant") msg.sources else listOf()
                            )
                        }

                        _messages.value = chatMessages
                    }
                    result.doIfFailure {
                        // Handle error
                    }
                }
            )
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || !_isConnected.value) return

        // Add user message
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            isSender = true
        )
        _messages.value = _messages.value + userMessage

        // Prepare for streaming response
        _isLoading.value = true
        streamingContent.clear()
        currentStreamingMessageId = UUID.randomUUID().toString()

        // Send message through WebSocket
        val request = ChatRequest(
            message = text,
            sessionId = _currentSession.value?.id
        )
        chatStreamService.sendMessage(request)
    }

    private fun handleSocketResponse(response: SocketResponse) {
        when (response) {
            is SocketResponse.Status -> {
            }

            is SocketResponse.Start -> {
                if (_currentSession.value == null) {
                    _currentSession.value = Session(
                        id = response.sessionId,
                        title = "",
                        lastMessage = null,
                        messageCount = "0"
                    )
                }
            }

            is SocketResponse.Chunk -> {
                streamingContent.append(response.content)
                updateStreamingMessage(streamingContent.toString(), isStreaming = true)
            }

            is SocketResponse.End -> {
                // Finalize message with sources
                val finalContent = streamingContent.toString()
                updateStreamingMessage(
                    content = finalContent,
                    isStreaming = false,
                    sources = response.sources
                )
                streamingContent.clear()
                currentStreamingMessageId = null
                _isLoading.value = false

                // Reload conversations to update list
                loadConversations()
            }

            is SocketResponse.Error -> {
                _isLoading.value = false
                addErrorMessage("Lỗi: ${response.error}")
                currentStreamingMessageId = null
            }
        }
    }

    private fun updateStreamingMessage(
        content: String,
        isStreaming: Boolean,
        sources: List<Source> = listOf()
    ) {
        val messageId = currentStreamingMessageId ?: return
        val currentMessages = _messages.value.toMutableList()

        val existingIndex = currentMessages.indexOfFirst { it.id == messageId }
        val message = ChatMessage(
            id = messageId,
            content = content,
            isSender = false,
            isStreaming = isStreaming,
            sources = sources
        )

        if (existingIndex >= 0) {
            currentMessages[existingIndex] = message
        } else {
            currentMessages.add(message)
        }

        _messages.value = currentMessages
    }

    private fun addErrorMessage(error: String) {
        val errorMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = error,
            isSender = false
        )
        _messages.value = _messages.value + errorMessage
    }

    fun createNewConversation() {
        _currentSession.value = null
        _messages.value = emptyList()
        streamingContent.clear()
        currentStreamingMessageId = null
    }

    override fun onTriggerEvent(event: ChatEvent) {
    }
}

sealed class ChatState {}
sealed class ChatEvent {}