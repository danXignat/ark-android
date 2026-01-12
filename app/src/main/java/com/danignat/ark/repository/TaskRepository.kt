package com.danignat.ark.repository

import com.danignat.ark.data.TaskDao
import com.danignat.ark.model.TaskModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(private val taskDao: TaskDao) : ITaskRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val tasks: StateFlow<List<TaskModel>> = taskDao.getAllTasks()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    override suspend fun addTask(title: String) {
        val newTask = TaskModel(
            moduleId = "default",
            title = title.trim(),
            done = false
        )
        taskDao.insertTask(newTask)
    }

    override suspend fun deleteTask(task: TaskModel) {
        taskDao.deleteTask(task)
    }

    override suspend fun toggleCompleted(task: TaskModel) {
        taskDao.updateTask(task.copy(done = !task.done))
    }

    override suspend fun deleteAllTasks() {
        tasks.value.forEach { task ->
            taskDao.deleteTask(task)
        }
    }

    override suspend fun completeAllTasks() {
        tasks.value.filter { !it.done }.forEach { task ->
            taskDao.updateTask(task.copy(done = true))
        }
    }

    override suspend fun uncompleteAllTasks() {
        tasks.value.filter { it.done }.forEach { task ->
            taskDao.updateTask(task.copy(done = false))
        }
    }
}