package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entries")
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val odometerKm: Double,
    val liters: Double,
    val pricePerLiter: Double,
    val totalCost: Double,
    val fuelType: FuelType = FuelType.GASOLINA_COMUM,
    val stationName: String = "",
    val isFullTank: Boolean = true,
    val notes: String = ""
)
