package com.example.todolist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.example.todolist.data.Task
import com.example.todolist.viewmodel.TaskViewModel

@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTask: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onSettingsClick: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Sortowanie zadań: niezakończone na górze, zakończone na dole
    val sortedTasks = tasks.sortedBy { it.isCompleted }

    Column(modifier = Modifier.fillMaxSize().padding(top=16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Szukaj zadań") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Przycisk ustawień
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, shape = MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Ustawienia",
                    tint = Color.White
                )
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sortedTasks) { task ->
                TaskItem(
                    task = task,
                    onClick = { onTaskClick(task) },
                    onToggleCompleted = { toggledTask ->
                        viewModel.updateTask(toggledTask.copy(isCompleted = !toggledTask.isCompleted))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(2.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
        FloatingActionButton(
            onClick = onAddTask,
            modifier = Modifier
                .padding(bottom = 64.dp, end = 32.dp)
                .align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj zadanie")
        }
    }
}
