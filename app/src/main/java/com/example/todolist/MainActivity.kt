package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.data.TaskDatabase
import com.example.todolist.repo.TaskRepository
import com.example.todolist.ui.TaskDetailScreen
import com.example.todolist.ui.TaskListScreen
import com.example.todolist.ui.theme.ToDoListTheme
import com.example.todolist.viewmodel.TaskViewModel
import com.example.todolist.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(db.taskDao())
        val factory = TaskViewModelFactory(repository)
        setContent {
            ToDoListTheme {
                val navController = rememberNavController()
                val viewModel: TaskViewModel = viewModel(factory = factory)
                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        TaskListScreen(
                            viewModel = viewModel,
                            onAddTask = { navController.navigate("detail") },
                            onTaskClick = { task -> navController.navigate("detail/${task.id}") }
                        )
                    }
                    composable(
                        "detail/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = 0L })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                        val task by viewModel.tasks.collectAsState()
                            .let { state ->
                                derivedStateOf { state.value.find { it.id == taskId } }
                            }
                        TaskDetailScreen(
                            task = if (taskId == 0L) null else task,
                            onSave = {
                                if (it.id == 0L) viewModel.insertTask(it) else viewModel.updateTask(it)
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                    composable("detail") {
                        TaskDetailScreen(
                            task = null,
                            onSave = {
                                viewModel.insertTask(it)
                                navController.popBackStack()
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ToDoListTheme {
        Greeting("Android")
    }
}

