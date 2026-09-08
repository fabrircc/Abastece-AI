package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.MaintenanceTrackerItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceTrackerDao {
    @Query("SELECT * FROM maintenance_tracker_items")
    fun getAllItems(): Flow<List<MaintenanceTrackerItem>>

    @Query("SELECT * FROM maintenance_tracker_items WHERE idKey = :idKey LIMIT 1")
    suspend fun getItemById(idKey: String): MaintenanceTrackerItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MaintenanceTrackerItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: MaintenanceTrackerItem)

    @Update
    suspend fun updateItem(item: MaintenanceTrackerItem)

    @Query("UPDATE maintenance_tracker_items SET lastReplacedKm = :km, lastReplacedDateMillis = :dateMillis WHERE idKey = :idKey")
    suspend fun updateReplacement(idKey: String, km: Double, dateMillis: Long)

    @Query("UPDATE maintenance_tracker_items SET recommendedIntervalKm = :intervalKm WHERE idKey = :idKey")
    suspend fun updateInterval(idKey: String, intervalKm: Double)

    @Query("DELETE FROM maintenance_tracker_items")
    suspend fun deleteAll()
}
