package com.example.silentshield.data.repository

import com.example.silentshield.presentation.home.AlertType
import com.example.silentshield.presentation.home.RecentAlert
import com.example.silentshield.presentation.home.RiskLevel
import com.example.silentshield.presentation.home.ShieldStats
import kotlinx.coroutines.delay

/**
 * Repository layer for scam detection data.
 *
 * Currently returns mock data. To wire up a real API later:
 * 1. Inject a Retrofit/Ktor service here
 * 2. Replace the mock functions with actual network calls
 * 3. Map API response DTOs → domain models (RecentAlert, ShieldStats)
 */
class ScamDetectionRepository {

    // --- When you add a real API, replace this with: ---
    // private val api: ScamDetectionApiService  (Retrofit interface)
    // and call: api.getStats(), api.getRecentAlerts()

    suspend fun getStats(): ShieldStats {
        delay(600) // simulate network
        return ShieldStats(
            callsBlocked = 14,
            smsScanned = 38,
            linksChecked = 7
        )
    }

    suspend fun getRecentAlerts(): List<RecentAlert> {
        delay(800)
        return listOf(
            RecentAlert(
                id = "1",
                type = AlertType.CALL,
                source = "+91-98XXXXXX12",
                summary = "Fake KYC verification call",
                timeAgo = "2 min ago",
                riskLevel = RiskLevel.HIGH
            ),
            RecentAlert(
                id = "2",
                type = AlertType.SMS,
                source = "VM-HDFCBK",
                summary = "OTP phishing attempt detected",
                timeAgo = "18 min ago",
                riskLevel = RiskLevel.HIGH
            ),
            RecentAlert(
                id = "3",
                type = AlertType.LINK,
                source = "bit.ly/free-reward-now",
                summary = "Redirects to known phishing domain",
                timeAgo = "1 hr ago",
                riskLevel = RiskLevel.MEDIUM
            ),
            RecentAlert(
                id = "4",
                type = AlertType.CALL,
                source = "+91-70XXXXXX88",
                summary = "Repeated spam caller",
                timeAgo = "3 hr ago",
                riskLevel = RiskLevel.MEDIUM
            ),
            RecentAlert(
                id = "5",
                type = AlertType.SMS,
                source = "TX-LOANAPP",
                summary = "Fake loan approval message",
                timeAgo = "Yesterday",
                riskLevel = RiskLevel.LOW
            )
        )
    }

    suspend fun toggleProtection(current: Boolean): Boolean {
        delay(300)
        return !current
    }

    suspend fun getAlertById(id: String): RecentAlert? {
        return getRecentAlerts().find { it.id == id }
    }
}