package com.example.incometracker.data.local

import androidx.room.TypeConverter
import com.example.incometracker.data.model.Product
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromProduct(product: Product?): String? {
        return product?.name // Store enum name as String
    }

    @TypeConverter
    fun toProduct(name: String?): Product? {
        return name?.let { Product.valueOf(it) }
    }
} 