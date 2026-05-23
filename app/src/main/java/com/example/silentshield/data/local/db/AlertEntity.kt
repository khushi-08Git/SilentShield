package com.example.silentshield.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,           // "CALL", "SMS", "LINK"
    val source: String,         // phone number or sender
    val summary: String,
    val riskLevel: String,      // "HIGH", "MEDIUM", "LOW"
    val timestamp: Long = System.currentTimeMillis()
)