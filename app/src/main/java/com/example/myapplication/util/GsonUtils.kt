package com.example.myapplication.util

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeTypeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun serialize(src: LocalTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalTime {
        return LocalTime.parse(json?.asString, formatter)
    }
}

class LocalDateTypeAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate {
        return LocalDate.parse(json?.asString, formatter)
    }
}

fun createGson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
        .create()
} 