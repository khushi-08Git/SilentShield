package com.example.silentshield.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    // Live stream — HomeViewModel collects this
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT 30")
    fun getAlertsStream(): Flow<List<AlertEntity>>

    @Query("DELETE FROM alerts WHERE timestamp < :cutoff")
    suspend fun deleteOldAlerts(cutoff: Long)
}