package com.example.todolist.repo

import com.example.todolist.data.Task
import com.example.todolist.data.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)

    fun getAllTasksSorted(): Flow<List<Task>> = taskDao.getAllTasksSorted()
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)
    fun getTasksFiltered(category: String?, showCompleted: Boolean): Flow<List<Task>> =
        taskDao.getTasksFiltered(category, showCompleted)
}
