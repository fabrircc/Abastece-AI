package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.model.ChatMessage
import com.example.model.FinanceEntry
import com.example.model.FuelEntry
import com.example.model.MaintenanceEntry
import com.example.model.MaintenanceTrackerItem

@Database(
    entities = [
        FuelEntry::class,
        MaintenanceEntry::class,
        FinanceEntry::class,
        ChatMessage::class,
        MaintenanceTrackerItem::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fuelDao(): FuelDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun financeDao(): FinanceDao
    abstract fun chatDao(): ChatDao
    abstract fun maintenanceTrackerDao(): MaintenanceTrackerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "combustivel_ia.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
