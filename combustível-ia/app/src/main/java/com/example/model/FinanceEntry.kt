package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FinanceType(val label: String) {
    RECEITA("Receita"),
    OUTRO_GASTO("Outro Gasto")
}

@Entity(tableName = "finance_entries")
data class FinanceEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val type: FinanceType,
    val category: String,
    val amount: Double,
    val notes: String = ""
)
