package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.FuelEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_entries ORDER BY odometerKm DESC, dateMillis DESC")
    fun getAllFuelEntries(): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries ORDER BY odometerKm ASC, dateMillis ASC")
    fun getAllFuelEntriesAsc(): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY odometerKm ASC, dateMillis ASC")
    fun getFuelEntriesBetween(startMillis: Long, endMillis: Long): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries ORDER BY odometerKm DESC, dateMillis DESC LIMIT 1")
    suspend fun getLatestFuelEntry(): FuelEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuel(entry: FuelEntry): Long

    @Update
    suspend fun updateFuel(entry: FuelEntry)

    @Delete
    suspend fun deleteFuel(entry: FuelEntry)

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteFuelById(id: Long)
}
