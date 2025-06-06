package com.example.todolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY dueAt ASC")
    fun getAllTasksSorted(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueAt ASC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY dueAt ASC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY dueAt ASC")
    fun searchTasks(query: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE (:category IS NULL OR category = :category) AND (:showCompleted OR isCompleted = 0) ORDER BY dueAt ASC")
    fun getTasksFiltered(category: String?, showCompleted: Boolean): Flow<List<Task>>
}

