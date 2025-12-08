package com.datavite.distrivite.data.local.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromEmbeddingsList(value: List<List<Float>>): String {
        return Json.Default.encodeToString(value)
    }

    @TypeConverter
    fun toEmbeddingsList(value: String): List<List<Float>> {
        return Json.Default.decodeFromString(value)
    }

    @TypeConverter
    fun fromWorkingDaysList(value: List<String>): String {
        return Json.Default.encodeToString(value)
    }

    @TypeConverter
    fun toWorkingDaysList(value: String): List<String> {
        return Json.Default.decodeFromString(value)
    }
}