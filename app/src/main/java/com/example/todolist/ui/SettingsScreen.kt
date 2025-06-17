package com.example.todolist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.todolist.data.AppSettingsManager
import com.example.todolist.data.dataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { AppSettingsManager(context) }
    val scope = rememberCoroutineScope()

    // Stan dla ustawień
    val showCompletedTasks = settingsManager.showCompletedTasks.collectAsState(initial = true)
    val selectedCategories = settingsManager.selectedCategories.collectAsState(initial = setOf())
    val defaultNotificationMinutes = settingsManager.defaultNotificationMinutes.collectAsState(initial = 15)

    // Potencjalne opcje dla czasu powiadomień
    val notificationOptions = listOf(0, 5, 10, 15, 30, 60)
    var expandedNotificationDropdown by remember { mutableStateOf(false) }

    // Stan lokalny dla kategorii, aby móc je modyfikować w UI
    val allCategories = AppSettingsManager.ALL_CATEGORIES
    val selectedCategoriesState = remember {
        mutableStateOf(selectedCategories.value.toMutableSet())
    }

    // Aktualizacja lokalnego stanu gdy zmienią się dane z DataStore
    LaunchedEffect(selectedCategories.value) {
        selectedCategoriesState.value = selectedCategories.value.toMutableSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Powrót")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Sekcja - Pokazywanie zakończonych zadań
            Box(
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Pokazuj zakończone zadania",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Switch(
                        checked = showCompletedTasks.value,
                        onCheckedChange = { isChecked ->
                            scope.launch {
                                settingsManager.updateShowCompletedTasks(isChecked)
                            }
                        }
                    )
                }
            }

            // Sekcja - Domyślny czas powiadomień
            Box(
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Domyślny czas powiadomień",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedNotificationDropdown,
                        onExpandedChange = { expandedNotificationDropdown = !expandedNotificationDropdown }
                    ) {
                        OutlinedTextField(
                            value = if (defaultNotificationMinutes.value == 0)
                                      "Brak wyprzedzenia"
                                    else
                                      "${defaultNotificationMinutes.value} minut",
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNotificationDropdown) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedNotificationDropdown,
                            onDismissRequest = { expandedNotificationDropdown = false }
                        ) {
                            notificationOptions.forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(if (time == 0) "Brak wyprzedzenia" else "$time minut") },
                                    onClick = {
                                        scope.launch {
                                            settingsManager.updateDefaultNotificationMinutes(time)
                                        }
                                        expandedNotificationDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sekcja - Wybór kategorii
            Box(
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Widoczne kategorie zadań",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    allCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = category in selectedCategoriesState.value,
                                onCheckedChange = { isChecked ->
                                    val updatedCategories = selectedCategoriesState.value.toMutableSet()

                                    if (isChecked) {
                                        updatedCategories.add(category)
                                    } else {
                                        updatedCategories.remove(category)
                                    }

                                    // Nie pozwalamy na pusty zbiór kategorii
                                    if (updatedCategories.isNotEmpty()) {
                                        selectedCategoriesState.value = updatedCategories

                                        scope.launch {
                                            settingsManager.updateSelectedCategories(updatedCategories)
                                        }
                                    }
                                }
                            )

                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
