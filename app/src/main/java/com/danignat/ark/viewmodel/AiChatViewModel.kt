package com.danignat.ark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danignat.ark.service.ai.ChatMessage
import com.danignat.ark.service.ai.LlmTaskAgent
import com.danignat.ark.service.ai.MessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val taskAgent: LlmTaskAgent
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState(
        messages = listOf(
            ChatMessage(
                role = MessageRole.ASSISTANT,
                content = "Hi! I'm your AI task assistant. I can help you add, view, complete, and remove tasks. What would you like to do?"
            )
        )
    ))
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val currentInput = _uiState.value.inputText.trim()
        if (currentInput.isBlank()) return

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = currentInput
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            inputText = "",
            isLoading = true
        )

        viewModelScope.launch {
            try {
                val response = taskAgent.processMessage(currentInput)
                val assistantMessage = ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = response
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = "Sorry, something went wrong. Please try again."
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false
                )
            }
        }
    }

    fun clearChat() {
        _uiState.value = AiChatUiState(
            messages = listOf(
                ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = "Chat cleared. How can I help you with your tasks?"
                )
            )
        )
    }
}

