package com.danignat.ark.service.ai

data class ChatMessage(
    val role: MessageRole,
    val content: String
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

sealed class AgentAction {
    data class ToolCall(
        val toolName: String,
        val arguments: Map<String, String>,
        val callId: String = ""
    ) : AgentAction()

    data class FinalResponse(val message: String) : AgentAction()
}
