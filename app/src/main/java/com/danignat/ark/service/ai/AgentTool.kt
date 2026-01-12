package com.danignat.ark.service.ai

data class AgentTool(
    val name: String,
    val description: String,
    val parameters: List<ToolParameter>,
    val execute: suspend (Map<String, String>) -> ToolResult
)

data class ToolParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true
)

sealed class ToolResult {
    data class Success(val message: String) : ToolResult()
    data class Error(val message: String) : ToolResult()
}
