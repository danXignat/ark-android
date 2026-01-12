package com.danignat.ark.service.ai

import android.util.Log
import com.danignat.ark.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmTaskAgent @Inject constructor(
    private val taskAgentTools: TaskAgentTools,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val TAG = "LlmTaskAgent"
    }
    private val tools: List<AgentTool> by lazy { taskAgentTools.getTools() }

    private val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta/interactions"
    private val model: String = "gemini-2.5-flash-lite"

    private var lastInteractionId: String = ""

    suspend fun processMessage(userMessage: String): String {
        val apiKey = settingsRepository.aiApiKey.first()

        if (apiKey.isBlank()) {
            return processWithRules(userMessage)
        }

        return try {
            processWithLlm(userMessage, apiKey)
        } catch (e: Exception) {
            "Sorry, I had trouble processing that. Error: ${e.message}"
        }
    }

    private suspend fun processWithLlm(userMessage: String, apiKey: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "=== START processWithLlm ===")
        Log.d(TAG, "User message: $userMessage")

        val systemPrompt = buildSystemPrompt()
        val response = callLlmApi(systemPrompt, userMessage, apiKey)
        Log.d(TAG, "Raw LLM response: $response")

        val action = parseLlmResponse(response)
        Log.d(TAG, "Parsed action: $action")

        when (action) {
            is AgentAction.ToolCall -> {
                Log.d(TAG, "Tool call detected: ${action.toolName}")
                Log.d(TAG, "Tool arguments: ${action.arguments}")
                Log.d(TAG, "Call ID: ${action.callId}")

                val tool = tools.find { it.name == action.toolName }
                if (tool != null) {
                    Log.d(TAG, "Executing tool: ${tool.name}")
                    val toolResult = tool.execute(action.arguments)
                    val resultMessage = when (toolResult) {
                        is ToolResult.Success -> toolResult.message
                        is ToolResult.Error -> "Error: ${toolResult.message}"
                    }
                    Log.d(TAG, "Tool result: $resultMessage")

                    val followUpResponse = callLlmWithToolResult(
                        toolName = action.toolName,
                        callId = action.callId,
                        toolResult = resultMessage,
                        apiKey = apiKey
                    )
                    Log.d(TAG, "Follow-up response: $followUpResponse")
                    parseNaturalResponse(followUpResponse)
                } else {
                    Log.e(TAG, "Tool not found: ${action.toolName}")
                    "I couldn't find the right action to perform."
                }
            }
            is AgentAction.FinalResponse -> {
                Log.d(TAG, "Final response (no tool call): ${action.message}")
                action.message
            }
        }
    }

    private suspend fun callLlmWithToolResult(
        toolName: String,
        callId: String,
        toolResult: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = JSONObject().apply {
                put("model", model)
                put("previous_interaction_id", lastInteractionId)
                put("input", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "function_result")
                        put("name", toolName)
                        put("call_id", callId)
                        put("result", toolResult)
                    })
                })
            }

            connection.outputStream.bufferedWriter().use { it.write(requestBody.toString()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)

                val outputs = responseJson.optJSONArray("outputs")
                if (outputs != null && outputs.length() > 0) {
                    for (i in 0 until outputs.length()) {
                        val item = outputs.getJSONObject(i)
                        if (item.optString("type", "") == "text" || item.has("text")) {
                            return@withContext item.optString("text", "")
                        }
                    }
                }
                throw Exception("No content found in response: $responseText")
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                throw Exception("API error ($responseCode): $errorText")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseNaturalResponse(response: String): String {
        return try {
            val json = JSONObject(response.trim())
            json.optString("response", response)
        } catch (e: Exception) {
            response.trim()
        }
    }

    private fun buildSystemPrompt(): String {
        return """
            |You are a friendly and helpful task management assistant named Ark. You help users manage their tasks in a conversational way.
            |
            |You have access to tools to manage tasks. Use them when the user asks to add, list, complete, or remove tasks.
            |
            |Be helpful, concise, and friendly in your responses.
        """.trimMargin()
    }

    private suspend fun callLlmApi(systemPrompt: String, userMessage: String, apiKey: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "=== callLlmApi ===")
        val url = URL("$baseUrl?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val toolsJson = JSONArray()
            tools.forEach { tool ->
                val toolJson = JSONObject()
                toolJson.put("type", "function")
                toolJson.put("name", tool.name)
                toolJson.put("description", tool.description)
                val parametersJson = JSONObject()
                parametersJson.put("type", "object")
                val propertiesJson = JSONObject()
                val requiredJson = JSONArray()
                tool.parameters.forEach { param ->
                    val paramJson = JSONObject()
                    paramJson.put("type", param.type)
                    paramJson.put("description", param.description)
                    propertiesJson.put(param.name, paramJson)
                    if (param.required) {
                        requiredJson.put(param.name)
                    }
                }
                parametersJson.put("properties", propertiesJson)
                if (requiredJson.length() > 0) {
                    parametersJson.put("required", requiredJson)
                }
                toolJson.put("parameters", parametersJson)
                toolsJson.put(toolJson)
            }

            val requestBody = JSONObject().apply {
                put("model", model)
                put("input", "$systemPrompt\n\nUser: $userMessage")
                put("tools", toolsJson)
            }

            Log.d(TAG, "Request URL: $baseUrl")
            Log.d(TAG, "Request body: ${requestBody.toString(2)}")

            connection.outputStream.bufferedWriter().use { it.write(requestBody.toString()) }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw API response: $responseText")
                parseInteractionsResponse(responseText)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                Log.e(TAG, "API error ($responseCode): $errorText")
                throw Exception("API error ($responseCode): $errorText")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseInteractionsResponse(responseText: String): String {
        Log.d(TAG, "=== parseInteractionsResponse ===")
        val responseJson = JSONObject(responseText)

        // Store interaction id for potential follow-up calls (API uses "id" not "interaction_id")
        lastInteractionId = responseJson.optString("id", "")
        Log.d(TAG, "Interaction ID: $lastInteractionId")

        // Check if there's a function call in the outputs (API uses "outputs" not "output")
        val outputs = responseJson.optJSONArray("outputs")
        Log.d(TAG, "Outputs array: $outputs")

        if (outputs != null && outputs.length() > 0) {
            for (i in 0 until outputs.length()) {
                val item = outputs.getJSONObject(i)
                val type = item.optString("type", "")
                Log.d(TAG, "Output item $i type: '$type', item: $item")

                if (type == "function_call") {
                    Log.d(TAG, "Found function_call!")
                    val result = JSONObject()
                    result.put("action", "tool_call")
                    result.put("tool_name", item.getString("name"))
                    // API uses "id" for the call id, not "call_id"
                    result.put("call_id", item.optString("id", ""))

                    // Parse arguments - could be a JSON object or string
                    val args = item.opt("arguments")
                    Log.d(TAG, "Function args: $args (type: ${args?.javaClass?.simpleName})")
                    if (args is JSONObject) {
                        result.put("arguments", args)
                    } else if (args is String) {
                        result.put("arguments", JSONObject(args))
                    } else {
                        result.put("arguments", JSONObject())
                    }
                    Log.d(TAG, "Returning tool_call: $result")
                    return result.toString()
                } else if (type == "text" || item.has("text")) {
                    val text = item.optString("text", "")
                    Log.d(TAG, "Found text response: $text")
                    val result = JSONObject()
                    result.put("action", "respond")
                    result.put("response", text)
                    return result.toString()
                }
            }
        }

        // Fallback: check for text field directly
        val text = responseJson.optString("text", "")
        if (text.isNotEmpty()) {
            Log.d(TAG, "Fallback text: $text")
            val result = JSONObject()
            result.put("action", "respond")
            result.put("response", text)
            return result.toString()
        }

        Log.e(TAG, "No content found in response!")
        throw Exception("No content found in response: $responseText")
    }

    private fun parseLlmResponse(response: String): AgentAction {
        return try {
            val json = JSONObject(response.trim())
            val action = json.optString("action", "respond")

            if (action == "tool_call") {
                val toolName = json.getString("tool_name")
                val callId = json.optString("call_id", "")
                val arguments = mutableMapOf<String, String>()
                json.optJSONObject("arguments")?.let { args ->
                    args.keys().forEach { key ->
                        arguments[key] = args.getString(key)
                    }
                }
                AgentAction.ToolCall(toolName, arguments, callId)
            } else {
                AgentAction.FinalResponse(json.optString("response", "I'm not sure how to help with that."))
            }
        } catch (e: Exception) {
            AgentAction.FinalResponse(response)
        }
    }

    private suspend fun processWithRules(userMessage: String): String {
        val message = userMessage.lowercase().trim()

        return when {
            message.contains("list") || message.contains("show") || message.contains("view") -> {
                val tool = tools.find { it.name == "list_tasks" }
                when (val result = tool?.execute(emptyMap())) {
                    is ToolResult.Success -> result.message
                    is ToolResult.Error -> result.message
                    null -> "Could not list tasks"
                }
            }
            message.contains("add") || message.contains("create") -> {
                val title = message
                    .replace(Regex("(?:add|create)\\s+(?:a\\s+)?(?:task|todo)?[:\\s]*", RegexOption.IGNORE_CASE), "")
                    .trim()
                if (title.isNotBlank()) {
                    val tool = tools.find { it.name == "add_task" }
                    when (val result = tool?.execute(mapOf("title" to title))) {
                        is ToolResult.Success -> result.message
                        is ToolResult.Error -> result.message
                        null -> "Could not add task"
                    }
                } else {
                    "Please specify a task title. Example: 'Add task: Buy groceries'"
                }
            }
            else -> {
                "I can help you manage tasks. Try 'show my tasks', 'add task: [title]', 'complete task [number]', or 'remove task [number]'"
            }
        }
    }
}
