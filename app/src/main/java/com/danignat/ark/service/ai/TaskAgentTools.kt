package com.danignat.ark.service.ai

import com.danignat.ark.model.TaskModel
import com.danignat.ark.repository.ITaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAgentTools @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    fun getTools(): List<AgentTool> = listOf(
        createAddTaskTool(),
        createListTasksTool(),
        createCompleteTaskTool(),
        createRemoveTaskTool(),
        createCompleteAllTasksTool(),
        createUncompleteAllTasksTool(),
        createRemoveAllTasksTool()
    )

    private fun createAddTaskTool() = AgentTool(
        name = "add_task",
        description = "Add a new task to the task list. Use this when the user wants to create, add, or schedule a new task.",
        parameters = listOf(
            ToolParameter(
                name = "title",
                type = "string",
                description = "The title/description of the task to add"
            )
        ),
        execute = { params ->
            val title = params["title"]
            if (title.isNullOrBlank()) {
                ToolResult.Error("Task title is required")
            } else {
                taskRepository.addTask(title)
                ToolResult.Success("Task '$title' has been added successfully.")
            }
        }
    )

    private fun createListTasksTool() = AgentTool(
        name = "list_tasks",
        description = "List all tasks. Use this when the user wants to see, check, view, or list their tasks.",
        parameters = emptyList(),
        execute = {
            val tasks = taskRepository.tasks.first()
            if (tasks.isEmpty()) {
                ToolResult.Success("You don't have any tasks yet.")
            } else {
                val taskList = tasks.mapIndexed { index, task ->
                    val status = if (task.done) "✓" else "○"
                    "${index + 1}. [$status] ${task.title}"
                }.joinToString("\n")
                ToolResult.Success("Here are your tasks:\n$taskList")
            }
        }
    )

    private fun createCompleteTaskTool() = AgentTool(
        name = "complete_task",
        description = "Mark a task as complete or toggle its completion status. Use this when the user wants to check off, complete, mark done, or uncheck a task.",
        parameters = listOf(
            ToolParameter(
                name = "task_identifier",
                type = "string",
                description = "The task number (1-based) or part of the task title to identify which task to complete"
            )
        ),
        execute = { params ->
            val identifier = params["task_identifier"]
            if (identifier.isNullOrBlank()) {
                ToolResult.Error("Please specify which task to complete")
            } else {
                val task = findTask(identifier)
                if (task != null) {
                    taskRepository.toggleCompleted(task)
                    val newStatus = if (task.done) "incomplete" else "complete"
                    ToolResult.Success("Task '${task.title}' has been marked as $newStatus.")
                } else {
                    ToolResult.Error("Could not find a task matching '$identifier'. Try listing your tasks first.")
                }
            }
        }
    )

    private fun createRemoveTaskTool() = AgentTool(
        name = "remove_task",
        description = "Remove/delete a task from the list. Use this when the user wants to remove, delete, or clear a task.",
        parameters = listOf(
            ToolParameter(
                name = "task_identifier",
                type = "string",
                description = "The task number (1-based) or part of the task title to identify which task to remove"
            )
        ),
        execute = { params ->
            val identifier = params["task_identifier"]
            if (identifier.isNullOrBlank()) {
                ToolResult.Error("Please specify which task to remove")
            } else {
                val task = findTask(identifier)
                if (task != null) {
                    taskRepository.deleteTask(task)
                    ToolResult.Success("Task '${task.title}' has been removed.")
                } else {
                    ToolResult.Error("Could not find a task matching '$identifier'. Try listing your tasks first.")
                }
            }
        }
    )

    private fun createCompleteAllTasksTool() = AgentTool(
        name = "complete_all_tasks",
        description = "Mark all tasks as complete. Use this when the user wants to check off all tasks, complete everything, mark all done, or finish all tasks.",
        parameters = emptyList(),
        execute = {
            val tasks = taskRepository.tasks.first()
            val incompleteTasks = tasks.filter { !it.done }
            if (tasks.isEmpty()) {
                ToolResult.Success("You don't have any tasks to complete.")
            } else if (incompleteTasks.isEmpty()) {
                ToolResult.Success("All tasks are already completed.")
            } else {
                taskRepository.completeAllTasks()
                ToolResult.Success("Marked ${incompleteTasks.size} task(s) as complete.")
            }
        }
    )

    private fun createUncompleteAllTasksTool() = AgentTool(
        name = "uncomplete_all_tasks",
        description = "Mark all tasks as incomplete. Use this when the user wants to uncheck all tasks, reset all tasks, or mark all as not done.",
        parameters = emptyList(),
        execute = {
            val tasks = taskRepository.tasks.first()
            val completedTasks = tasks.filter { it.done }
            if (tasks.isEmpty()) {
                ToolResult.Success("You don't have any tasks.")
            } else if (completedTasks.isEmpty()) {
                ToolResult.Success("No tasks are currently completed.")
            } else {
                taskRepository.uncompleteAllTasks()
                ToolResult.Success("Marked ${completedTasks.size} task(s) as incomplete.")
            }
        }
    )

    private fun createRemoveAllTasksTool() = AgentTool(
        name = "remove_all_tasks",
        description = "Remove/delete all tasks from the list. Use this when the user wants to clear all tasks, delete everything, remove all, or start fresh.",
        parameters = emptyList(),
        execute = {
            val tasks = taskRepository.tasks.first()
            if (tasks.isEmpty()) {
                ToolResult.Success("You don't have any tasks to remove.")
            } else {
                val count = tasks.size
                taskRepository.deleteAllTasks()
                ToolResult.Success("Removed all $count task(s).")
            }
        }
    )

    private suspend fun findTask(identifier: String): TaskModel? {
        val tasks = taskRepository.tasks.first()

        val index = identifier.toIntOrNull()
        if (index != null && index in 1..tasks.size) {
            return tasks[index - 1]
        }

        tasks.find { it.title.equals(identifier, ignoreCase = true) }?.let { return it }

        tasks.find { it.title.contains(identifier, ignoreCase = true) }?.let { return it }

        return null
    }
}
