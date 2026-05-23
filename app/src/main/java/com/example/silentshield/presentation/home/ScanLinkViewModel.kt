package com.example.displace.presentation.scanclink

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silentshield.data.local.StatsDataStore
import com.example.silentshield.data.remote.ApiKeys
import com.example.silentshield.data.remote.ClientInfo
import com.example.silentshield.data.remote.RetrofitInstance
import com.example.silentshield.data.remote.SafeBrowsingRequest
import com.example.silentshield.data.remote.ThreatEntry
import com.example.silentshield.data.remote.ThreatInfo
import com.example.silentshield.presentation.home.ScanLinkUiState
import com.example.silentshield.presentation.home.ScanStatus
import kotlinx.coroutines.launch


class ScanLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val statsDataStore = StatsDataStore(application)

    var state by mutableStateOf(ScanLinkUiState())
        private set

    fun onUrlChange(value: String) {
        state = state.copy(url = value, status = ScanStatus.IDLE, errorMessage = null.toString())
    }

    fun clearUrl() {
        state = ScanLinkUiState()
    }

    fun scanUrl() {
        val rawUrl = state.url.trim()
        if (rawUrl.isBlank()) return

        val url = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else rawUrl

        state = state.copy(isLoading = true, status = ScanStatus.SCANNING, errorMessage = null.toString())

        viewModelScope.launch {
            try {
                // ── Real API call ──────────────────────────────────────
                val response = RetrofitInstance.safeBrowsingApi.checkUrl(
                    apiKey  = ApiKeys.SAFE_BROWSING,
                    request = SafeBrowsingRequest(
                        client     = ClientInfo(),
                        threatInfo = ThreatInfo(
                            threatEntries = listOf(ThreatEntry(url))
                        )
                    )
                )

                val googleThreats = response.matches ?: emptyList()
                val localFlags    = runLocalHeuristics(url)

                val (status, summary, details) = when {
                    googleThreats.isNotEmpty() -> {
                        val types = googleThreats
                            .map { it.threatType }
                            .distinct()
                            .joinToString(", ")
                        Triple(
                            ScanStatus.DANGEROUS,
                            "Google flagged this URL as dangerous.",
                            listOf("Threat: $types") + localFlags
                        )
                    }
                    localFlags.size >= 2 -> Triple(
                        ScanStatus.SUSPICIOUS,
                        "Suspicious patterns detected.",
                        localFlags
                    )
                    localFlags.size == 1 -> Triple(
                        ScanStatus.SUSPICIOUS,
                        "Minor suspicious signal found.",
                        localFlags
                    )
                    else -> Triple(
                        ScanStatus.SAFE,
                        "No threats detected.",
                        listOf(
                            "Not on Google Safe Browsing blocklist",
                            "No suspicious patterns found",
                            "URL appears legitimate"
                        )
                    )
                }

                // ── Increment counter for every scan ───────────────────
                statsDataStore.incrementLinksChecked()

                state = state.copy(
                    isLoading     = false,
                    status        = status,
                    resultSummary = summary,
                    resultDetails = details,
                    url           = url
                )

            } catch (e: Exception) {
                // ── Offline fallback ───────────────────────────────────
                val localFlags = runLocalHeuristics(url)
                val (status, summary) = when {
                    localFlags.size >= 2 ->
                        ScanStatus.DANGEROUS to "Multiple suspicious signals (offline check)."
                    localFlags.size == 1 ->
                        ScanStatus.SUSPICIOUS to "One suspicious signal found (offline check)."
                    else ->
                        ScanStatus.SAFE to "No issues found (offline check)."
                }

                statsDataStore.incrementLinksChecked()

                state = state.copy(
                    isLoading     = false,
                    status        = status,
                    resultSummary = summary,
                    resultDetails = localFlags.ifEmpty {
                        listOf("API unavailable — local check only")
                    },
                    errorMessage  = "Safe Browsing API unavailable — using local checks only.",
                    url           = url
                )
            }
        }
    }

    private fun runLocalHeuristics(url: String): List<String> {
        val flags = mutableListOf<String>()
        val lower = url.lowercase()

        if (lower.contains("bit.ly") || lower.contains("tinyurl") ||
            lower.contains("t.co")   || lower.contains("goo.gl")) {
            flags.add("URL shortener detected — hides real destination")
        }
        if (lower.contains("free") && (lower.contains("reward") ||
                    lower.contains("win")  || lower.contains("gift"))) {
            flags.add("'Free reward/win' pattern — classic phishing bait")
        }
        if (lower.contains("kyc") || lower.contains("aadhaar") ||
            lower.contains("pan-verify")) {
            flags.add("KYC/document keyword — common in Indian phishing scams")
        }
        if (lower.contains("login") && (lower.contains("bank") ||
                    lower.contains("sbi")  || lower.contains("hdfc")  ||
                    lower.contains("icici")|| lower.contains("paytm"))) {
            flags.add("Fake bank login page pattern detected")
        }
        if (Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""")
                .containsMatchIn(lower)) {
            flags.add("IP address used instead of domain — suspicious")
        }
        if (lower.count { it == '-' } >= 3) {
            flags.add("Excessive hyphens in domain — common in spoofed URLs")
        }
        val domain = lower
            .removePrefix("https://")
            .removePrefix("http://")
            .split("/").first()
        if (domain.length > 40) {
            flags.add("Unusually long domain name")
        }

        return flags
    }
}