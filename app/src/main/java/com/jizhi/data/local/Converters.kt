package com.jizhi.data.local

import androidx.room.TypeConverter

/**
 * Room 类型转换器
 * 用于 List<String> 与 String 之间的转换
 */
class Converters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString("|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split("|||")
    }
}
