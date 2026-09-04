package com.example.data

import androidx.room.TypeConverter
import com.example.model.FinanceType
import com.example.model.FuelType

class Converters {
    @TypeConverter
    fun fromFuelType(value: FuelType?): String? = value?.name

    @TypeConverter
    fun toFuelType(value: String?): FuelType = value?.let {
        try {
            FuelType.valueOf(it)
        } catch (_: Exception) {
            FuelType.fromString(it)
        }
    } ?: FuelType.GASOLINA_COMUM

    @TypeConverter
    fun fromFinanceType(value: FinanceType?): String? = value?.name

    @TypeConverter
    fun toFinanceType(value: String?): FinanceType = value?.let {
        try {
            FinanceType.valueOf(it)
        } catch (_: Exception) {
            FinanceType.OUTRO_GASTO
        }
    } ?: FinanceType.OUTRO_GASTO
}
