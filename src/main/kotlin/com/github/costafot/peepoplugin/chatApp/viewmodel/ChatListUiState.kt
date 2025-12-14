package com.github.costafot.peepoplugin.chatApp.viewmodel

import com.github.costafot.peepoplugin.chatApp.model.ChatMessage

data class ChatListUiState(
    val messages: List<ChatMessage> = emptyList(),
) {
    companion object Companion {
        val EMPTY = ChatListUiState()
    }
}