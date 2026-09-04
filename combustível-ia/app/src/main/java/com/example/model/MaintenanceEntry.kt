package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_entries")
data class MaintenanceEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val odometerKm: Double = 0.0,
    val serviceType: String,
    val workshopName: String = "",
    val cost: Double,
    val notes: String = ""
)
