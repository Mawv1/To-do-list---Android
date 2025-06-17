package com.example.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.AppSettingsManager
import com.example.todolist.data.Task
import com.example.todolist.repo.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val settingsManager: AppSettingsManager? = null
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedCategory: StateFlow<String?> = _selectedCategory

    val showCompleted: Flow<Boolean> = settingsManager?.showCompletedTasks ?: flow { emit(true) }
    val selectedCategories: Flow<Set<String>> = settingsManager?.selectedCategories ?: flow { emit(setOf()) }
    val defaultNotificationMinutes: Flow<Int> = settingsManager?.defaultNotificationMinutes ?: flow { emit(15) }

    val tasks: StateFlow<List<Task>> = combine(
        _searchQuery,
        _selectedCategory,
        showCompleted,
        selectedCategories
    ) { query, categoryFilter, showCompletedTasks, selectedCats ->
        QueryParams(query, categoryFilter, showCompletedTasks, selectedCats)
    }.flatMapLatest { params ->
        repository.getAllTasksSorted().map { allTasks ->
            allTasks
                // Filtrujemy wg wyszukiwania
                .filter { task ->
                    params.query.isEmpty() ||
                    task.title.contains(params.query, ignoreCase = true) ||
                    task.description.contains(params.query, ignoreCase = true)
                }
                // Filtrujemy wg kategorii wybranej jako filtr (jeśli ustawiona)
                .filter { task ->
                    params.categoryFilter == null || task.category == params.categoryFilter
                }
                // Filtrujemy wg wybranych kategorii w ustawieniach (jeśli niepuste)
                .filter { task ->
                    params.selectedCategories.isEmpty() || task.category in params.selectedCategories
                }
                // Ukrywamy zakończone, jeśli tak ustawiono
                .filter { task ->
                    params.showCompletedTasks || !task.isCompleted
                }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    // Klasa pomocnicza dla parametrów zapytania
    private data class QueryParams(
        val query: String,
        val categoryFilter: String?,
        val showCompletedTasks: Boolean,
        val selectedCategories: Set<String>
    )
}
