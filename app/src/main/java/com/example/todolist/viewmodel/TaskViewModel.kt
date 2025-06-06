package com.example.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Task
import com.example.todolist.repo.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _showCompleted = MutableStateFlow(true)

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedCategory: StateFlow<String?> = _selectedCategory
    val showCompleted: StateFlow<Boolean> = _showCompleted

    val tasks: StateFlow<List<Task>> = combine(
        _searchQuery, _selectedCategory, _showCompleted
    ) { query, category, showCompleted ->
        Triple(query, category, showCompleted)
    }.flatMapLatest { (query, category, showCompleted) ->
        when {
            query.isNotBlank() -> repository.searchTasks(query)
            category != null || !showCompleted -> repository.getTasksFiltered(category, showCompleted)
            else -> repository.getAllTasksSorted()
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

    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
    }
}

