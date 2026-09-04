package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isUser: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null, // "FUEL", "MAINTENANCE", "FINANCE", null
    val actionDataJson: String? = null,
    val actionConfirmed: Boolean = false
)
