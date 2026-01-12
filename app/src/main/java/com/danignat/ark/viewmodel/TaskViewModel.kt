package com.danignat.ark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danignat.ark.model.TaskModel
import com.danignat.ark.repository.ITaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine

enum class TaskFilter {
    ALL,
    COMPLETED,
    UNCOMPLETED
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: ITaskRepository
) : ViewModel() {
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    val tasks: StateFlow<List<TaskModel>> = combine(
        repository.tasks,
        _filter
    ) { allTasks, currentFilter ->
        when (currentFilter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.COMPLETED -> allTasks.filter { it.done }
            TaskFilter.UNCOMPLETED -> allTasks.filter { !it.done }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun addTask() {
        val currentTitle = _title.value
        Log.d("TaskViewModel", "addTask called with title: $currentTitle")
        viewModelScope.launch {
            repository.addTask(currentTitle)
            _title.value = ""
            Log.d("TaskViewModel", "addTask completed")
        }
    }

    fun deleteTask(task: TaskModel) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleCompletion(task: TaskModel) {
        viewModelScope.launch {
            repository.toggleCompleted(task)
        }
    }
}