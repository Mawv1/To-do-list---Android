package com.example.todolist.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "tasks")
@TypeConverters(AttachmentsConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val createdAt: Long,
    val dueAt: Long?,
    val isCompleted: Boolean = false,
    val notificationEnabled: Boolean = false,
    val notificationMinutesInAdvance: Int = 0,
    val category: String? = null,
    val attachments: List<AttachmentItem> = emptyList()
    )
