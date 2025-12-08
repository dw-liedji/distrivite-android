package com.datavite.distrivite.utils

import kotlinx.serialization.json.Json

object JsonConverter {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    inline fun <reified T> toJson(obj: T): String {
        return json.encodeToString(obj)
    }

    inline fun <reified T> fromJson(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }
}