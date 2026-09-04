package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.MaintenanceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_entries ORDER BY dateMillis DESC, odometerKm DESC")
    fun getAllMaintenance(): Flow<List<MaintenanceEntry>>

    @Query("SELECT * FROM maintenance_entries WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getMaintenanceBetween(startMillis: Long, endMillis: Long): Flow<List<MaintenanceEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(entry: MaintenanceEntry): Long

    @Update
    suspend fun updateMaintenance(entry: MaintenanceEntry)

    @Delete
    suspend fun deleteMaintenance(entry: MaintenanceEntry)

    @Query("DELETE FROM maintenance_entries WHERE id = :id")
    suspend fun deleteMaintenanceById(id: Long)
}
