package com.example.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun toDoubleList(value: String?): List<Double>? {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.toDoubleOrNull() }
    }
}
