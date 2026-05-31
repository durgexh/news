package com.newsapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newsapp.data.SourceInfo

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSourceInfoList(value: List<SourceInfo>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSourceInfoList(value: String): List<SourceInfo> {
        val listType = object : TypeToken<List<SourceInfo>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
