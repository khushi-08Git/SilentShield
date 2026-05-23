package com.example.silentshield.presentation.home

data class RecentAlert(
    val id: String,
    val type: AlertType,
    val source: String,       // phone number or URL
    val summary: String,
    val timeAgo: String,
    val riskLevel: RiskLevel
)

enum class AlertType { CALL, SMS, LINK }

enum class RiskLevel { HIGH, MEDIUM, LOW }

data class ShieldStats(
    val callsBlocked: Int,
    val smsScanned: Int,
    val linksChecked: Int
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val isProtectionActive: Boolean,
        val stats: ShieldStats,
        val recentAlerts: List<RecentAlert>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}