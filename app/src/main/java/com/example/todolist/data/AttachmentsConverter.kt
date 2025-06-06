package com.example.todolist.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AttachmentsConverter {
    @TypeConverter
    fun fromAttachmentsList(list: List<AttachmentItem>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toAttachmentsList(data: String?): List<AttachmentItem> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<AttachmentItem>>() {}.type
        return Gson().fromJson(data, type)
    }
}

