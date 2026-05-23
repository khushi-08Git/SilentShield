package com.example.silentshield.presentation.home

enum class ScanStatus { IDLE, SCANNING, SAFE, SUSPICIOUS, DANGEROUS }

data class ScanLinkUiState(
    val url: String = "",
    val status: ScanStatus = ScanStatus.IDLE,
    val resultSummary: String = "",
    val resultDetails: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)