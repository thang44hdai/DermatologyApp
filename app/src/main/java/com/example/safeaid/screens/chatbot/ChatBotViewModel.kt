package com.example.safeaid.screens.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeaid.core.base.BaseViewModel
import com.example.safeaid.core.response.Session
import com.example.safeaid.core.service.ApiService
import com.example.safeaid.core.utils.ApiCaller
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.removeVietnameseAccents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel<ChatState, ChatEvent>() {

    private val _conversations = MutableStateFlow<List<Session>>(emptyList())
    val conversations: StateFlow<List<Session>> = _conversations

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession

    private var allSessions = listOf<Session>()

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
                        // Handle messages - TODO: Update chat UI
                    }
                    result.doIfFailure {
                        // Handle error
                    }
                }
            )
        }
    }

    fun createNewConversation() {
        _currentSession.value = null
    }

    override fun onTriggerEvent(event: ChatEvent) {
    }
}

sealed class ChatState {}
sealed class ChatEvent {}