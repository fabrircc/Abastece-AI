package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.FinanceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_entries ORDER BY dateMillis DESC")
    fun getAllFinances(): Flow<List<FinanceEntry>>

    @Query("SELECT * FROM finance_entries WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getFinancesBetween(startMillis: Long, endMillis: Long): Flow<List<FinanceEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(entry: FinanceEntry): Long

    @Update
    suspend fun updateFinance(entry: FinanceEntry)

    @Delete
    suspend fun deleteFinance(entry: FinanceEntry)

    @Query("DELETE FROM finance_entries WHERE id = :id")
    suspend fun deleteFinanceById(id: Long)
}
