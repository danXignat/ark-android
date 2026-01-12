package com.danignat.ark.repository

import com.danignat.ark.model.TaskModel
import kotlinx.coroutines.flow.StateFlow

interface ITaskRepository {
    val tasks: StateFlow<List<TaskModel>>

    suspend fun addTask(title: String)
    suspend fun deleteTask(task: TaskModel)
    suspend fun toggleCompleted(task: TaskModel)
    suspend fun deleteAllTasks()
    suspend fun completeAllTasks()
    suspend fun uncompleteAllTasks()
}