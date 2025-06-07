package com.example.todolist

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
    private var navControllerRef: androidx.navigation.NavHostController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Prośba o uprawnienie do powiadomień na Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean -> }
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val db = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(db.taskDao())
        val factory = TaskViewModelFactory(repository)
        setContent {
            ToDoListTheme {
                val navController = rememberNavController()
                navControllerRef = navController
                val viewModel: TaskViewModel = viewModel(factory = factory)
                // Obsługa taskId z powiadomienia
                val startTaskId = intent?.getLongExtra("taskId", 0L) ?: 0L
                val startDestination = if (startTaskId != 0L) "detail/$startTaskId" else "list"
                // Po usunięciu taska wracaj na listę
                NavHost(navController = navController, startDestination = startDestination) {
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
                                derivedStateOf { state.value.find { it.id == taskId && taskId > 0L } }
                            }
                        TaskDetailScreen(
                            task = if (taskId > 0L) task else null,
                            onSave = {
                                if (it.id == 0L) viewModel.insertTask(it) else viewModel.updateTask(it)
                                navController.navigate("list") {
                                    popUpTo("list") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onCancel = {
                                navController.navigate("list") {
                                    popUpTo("list") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onDelete = {
                                viewModel.deleteTask(it)
                                navController.navigate("list") {
                                    popUpTo("list") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable("detail") {
                        TaskDetailScreen(
                            task = null,
                            onSave = {
                                viewModel.insertTask(it)
                                navController.navigate("list") {
                                    popUpTo("list") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onCancel = {
                                navController.navigate("list") {
                                    popUpTo("list") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        val taskId = intent?.getLongExtra("taskId", 0L) ?: 0L
        if (taskId != 0L) {
            navControllerRef?.navigate("detail/$taskId") {
                popUpTo("list") { inclusive = false }
                launchSingleTop = true
                restoreState = true
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

