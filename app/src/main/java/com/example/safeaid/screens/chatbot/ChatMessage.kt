package com.example.safeaid.screens.chatbot

import com.example.safeaid.core.response.Source

data class ChatMessage(
    val id: String,
    val content: String,
    val isSender: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<Source> = listOf(),
    val isStreaming: Boolean = false
)
