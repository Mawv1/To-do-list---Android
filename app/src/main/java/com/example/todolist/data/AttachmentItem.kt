package com.example.todolist.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AttachmentItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long = 0L,
    val fileUri: String, // URI pliku z pickera jako String
    val createdAt: Long = System.currentTimeMillis()
)

