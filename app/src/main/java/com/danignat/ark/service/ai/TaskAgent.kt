package com.danignat.ark.service.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAgent @Inject constructor(
    private val taskAgentTools: TaskAgentTools
) {
    private val tools: List<AgentTool> by lazy { taskAgentTools.getTools() }

    suspend fun processMessage(userMessage: String): String {
        val action = decideAction(userMessage)

        return when (action) {
            is AgentAction.ToolCall -> {
                val tool = tools.find { it.name == action.toolName }
                if (tool != null) {
                    when (val result = tool.execute(action.arguments)) {
                        is ToolResult.Success -> result.message
                        is ToolResult.Error -> "Sorry, there was an issue: ${result.message}"
                    }
                } else {
                    "I couldn't find the right action to perform."
                }
            }
            is AgentAction.FinalResponse -> action.message
        }
    }

    private fun decideAction(userMessage: String): AgentAction {
        val message = userMessage.lowercase().trim()

        if (matchesListIntent(message)) {
            return AgentAction.ToolCall("list_tasks", emptyMap())
        }

        val addTaskTitle = extractAddTaskTitle(message)
        if (addTaskTitle != null) {
            return AgentAction.ToolCall("add_task", mapOf("title" to addTaskTitle))
        }

        val completeTaskId = extractCompleteTaskIdentifier(message)
        if (completeTaskId != null) {
            return AgentAction.ToolCall("complete_task", mapOf("task_identifier" to completeTaskId))
        }

        val removeTaskId = extractRemoveTaskIdentifier(message)
        if (removeTaskId != null) {
            return AgentAction.ToolCall("remove_task", mapOf("task_identifier" to removeTaskId))
        }

        if (matchesHelpIntent(message)) {
            return AgentAction.FinalResponse(getHelpMessage())
        }

        return AgentAction.FinalResponse(
            "I can help you manage your tasks! Try:\n" +
            "• \"Add task: Buy groceries\"\n" +
            "• \"Show my tasks\"\n" +
            "• \"Complete task 1\" or \"Check off groceries\"\n" +
            "• \"Remove task 2\" or \"Delete groceries\""
        )
    }

    private fun matchesListIntent(message: String): Boolean {
        val listPatterns = listOf(
            "show", "list", "view", "see", "what are", "my tasks",
            "all tasks", "get tasks", "tasks", "check tasks", "display"
        )
        return listPatterns.any { message.contains(it) } &&
               !message.contains("add") &&
               !message.contains("remove") &&
               !message.contains("delete") &&
               !message.contains("complete") &&
               !message.contains("done") &&
               !message.contains("check off")
    }

    private fun extractAddTaskTitle(message: String): String? {
        val addPatterns = listOf(
            Regex("(?:add|create|new|make|schedule)\\s+(?:a\\s+)?(?:task|todo|item)?[:\\s]+(.+)", RegexOption.IGNORE_CASE),
            Regex("(?:add|create|new|make)\\s+(?:a\\s+)?(?:task|todo|item)\\s+(?:called|named|titled)?\\s*[\"']?(.+?)[\"']?$", RegexOption.IGNORE_CASE),
            Regex("(?:remind me to|i need to|i have to|don't forget to)\\s+(.+)", RegexOption.IGNORE_CASE),
            Regex("(?:add|create)\\s+[\"'](.+?)[\"']", RegexOption.IGNORE_CASE)
        )

        for (pattern in addPatterns) {
            pattern.find(message)?.groupValues?.getOrNull(1)?.let {
                val title = it.trim()
                if (title.isNotBlank()) return title
            }
        }

        if (message.startsWith("add ")) {
            val afterAdd = message.removePrefix("add ").trim()
            if (afterAdd.isNotBlank() &&
                !afterAdd.startsWith("task") &&
                afterAdd.length > 2) {
                return afterAdd
            }
        }

        return null
    }

    private fun extractCompleteTaskIdentifier(message: String): String? {
        val completePatterns = listOf(
            Regex("(?:complete|finish|done|check off|check|mark done|mark complete|mark as done|mark as complete)\\s+(?:task\\s+)?(.+)", RegexOption.IGNORE_CASE),
            Regex("(?:i'?ve?|i\\s+have)\\s+(?:completed|finished|done)\\s+(.+)", RegexOption.IGNORE_CASE),
            Regex("mark\\s+(.+?)\\s+(?:as\\s+)?(?:done|complete|finished)", RegexOption.IGNORE_CASE)
        )

        for (pattern in completePatterns) {
            pattern.find(message)?.groupValues?.getOrNull(1)?.let {
                val id = it.trim().removeSuffix("task").trim()
                if (id.isNotBlank()) return id
            }
        }

        return null
    }

    private fun extractRemoveTaskIdentifier(message: String): String? {
        val removePatterns = listOf(
            Regex("(?:remove|delete|clear|cancel)\\s+(?:task\\s+)?(.+)", RegexOption.IGNORE_CASE),
            Regex("(?:get rid of|throw away)\\s+(?:task\\s+)?(.+)", RegexOption.IGNORE_CASE)
        )

        for (pattern in removePatterns) {
            pattern.find(message)?.groupValues?.getOrNull(1)?.let {
                val id = it.trim().removeSuffix("task").trim()
                if (id.isNotBlank()) return id
            }
        }

        return null
    }

    private fun matchesHelpIntent(message: String): Boolean {
        val helpPatterns = listOf("help", "what can you do", "how do i", "how to", "commands", "options")
        return helpPatterns.any { message.contains(it) }
    }

    private fun getHelpMessage(): String {
        return """
            |I'm your AI task assistant! Here's what I can do:
            |
            |📝 **Add Tasks**
            |• "Add task: Buy groceries"
            |• "Create a task called Review report"
            |• "Remind me to call mom"
            |
            |📋 **View Tasks**
            |• "Show my tasks"
            |• "List all tasks"
            |• "What are my tasks?"
            |
            |✅ **Complete Tasks**
            |• "Complete task 1"
            |• "Mark groceries as done"
            |• "Check off task 2"
            |
            |🗑️ **Remove Tasks**
            |• "Remove task 1"
            |• "Delete groceries"
            |• "Clear task 3"
        """.trimMargin()
    }
}
