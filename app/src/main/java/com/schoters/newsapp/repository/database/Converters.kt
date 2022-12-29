package com.schoters.newsapp.repository.database

import androidx.room.TypeConverter
import com.schoters.newsapp.model.Source

class Converters {
    @TypeConverter
    fun fromSource(source: Source): String? = source.name

    @TypeConverter
    fun toSource(name: String): Source = Source(name, name)
}