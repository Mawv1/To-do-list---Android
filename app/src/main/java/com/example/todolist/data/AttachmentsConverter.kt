package com.example.todolist.data

import androidx.room.TypeConverter

class AttachmentsConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(separator = ";;;")
    }

    @TypeConverter
    fun toList(data: String): List<String> {
        return if (data.isBlank()) emptyList() else data.split(";;;")
    }
}

