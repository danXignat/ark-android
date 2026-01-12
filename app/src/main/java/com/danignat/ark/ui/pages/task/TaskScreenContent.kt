package com.danignat.ark.ui.pages.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.danignat.ark.model.TaskModel
import com.danignat.ark.viewmodel.TaskViewModel
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreenContent(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel,
    onMenuClick: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val title by viewModel.title.collectAsState()

    Scaffold() { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn {
                items(tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { viewModel.toggleCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                    HorizontalDivider(
                        modifier = Modifier,
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                }
            }
            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.Bottom) {
                OutlinedTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("New task") }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    Log.d("TaskScreenContent", "Add button clicked")
                    viewModel.addTask()
                }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: TaskModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.done,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = task.title,
            modifier = Modifier.weight(1f),
            textDecoration = if (task.done) TextDecoration.LineThrough else null
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete"
            )
        }
    }
}
