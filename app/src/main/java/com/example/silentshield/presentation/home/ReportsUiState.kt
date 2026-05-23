package com.example.silentshield.presentation.home

data class CommunityReport(
    val id: String,
    val type: AlertType,
    val source: String,
    val description: String,
    val reportedBy: String,
    val timeAgo: String,
    val upvotes: Int,
    val riskLevel: RiskLevel
)

sealed class ReportsUiState {
    object Loading : ReportsUiState()
    data class Success(val reports: List<CommunityReport>) : ReportsUiState()
    data class Error(val message: String) : ReportsUiState()
}
