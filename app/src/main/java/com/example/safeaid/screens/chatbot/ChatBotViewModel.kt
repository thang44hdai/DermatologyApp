package com.example.safeaid.screens.chatbot

import androidx.lifecycle.ViewModel
import com.example.safeaid.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ChatBotViewModel @Inject constructor() : BaseViewModel<ChatState, ChatEvent>() {
    override fun onTriggerEvent(event: ChatEvent) {
    }
}

sealed class ChatState {}
sealed class ChatEvent {}