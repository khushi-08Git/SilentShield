package com.example.silentshield.data.local

import android.content.Context
import com.example.silentshield.data.local.db.AlertEntity
import com.example.silentshield.data.local.db.AlertDatabase
import com.example.silentshield.presentation.home.AlertType
import com.example.silentshield.presentation.home.RecentAlert
import com.example.silentshield.presentation.home.RiskLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlertRepository(context: Context) {

    private val dao          = AlertDatabase.getInstance(context).alertDao()
    private val statsStore   = StatsDataStore(context)

    // Live stream of alerts → HomeViewModel collects this
    val alertsStream: Flow<List<RecentAlert>> = dao.getAlertsStream().map { entities ->
        entities.map { it.toRecentAlert() }
    }

    suspend fun saveCallAlert(
        phoneNumber: String,
        summary: String,
        riskLevel: RiskLevel
    ) {
        dao.insertAlert(
            AlertEntity(
                type      = AlertType.CALL.name,
                source    = phoneNumber,
                summary   = summary,
                riskLevel = riskLevel.name
            )
        )
        statsStore.incrementCallsBlocked()
    }

    suspend fun saveSmsAlert(
        sender: String,
        summary: String,
        riskLevel: RiskLevel
    ) {
        dao.insertAlert(
            AlertEntity(
                type      = AlertType.SMS.name,
                source    = sender,
                summary   = summary,
                riskLevel = riskLevel.name
            )
        )
        statsStore.incrementSmsScanned()
    }

    // Cleanup alerts older than 7 days
    suspend fun pruneOldAlerts() {
        val cutoff = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        dao.deleteOldAlerts(cutoff)
    }

    private fun AlertEntity.toRecentAlert() = RecentAlert(
        id        = id.toString(),
        type      = AlertType.valueOf(type),
        source    = source,
        summary   = summary,
        timeAgo   = formatTimestamp(timestamp),
        riskLevel = RiskLevel.valueOf(riskLevel)
    )

    private fun formatTimestamp(ts: Long): String {
        val diff    = System.currentTimeMillis() - ts
        val minutes = diff / 60_000
        val hours   = diff / 3_600_000
        val days    = diff / 86_400_000
        return when {
            minutes < 1  -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours   < 24 -> "$hours hr ago"
            days    < 7  -> "$days days ago"
            else         -> "A while ago"
        }
    }
}