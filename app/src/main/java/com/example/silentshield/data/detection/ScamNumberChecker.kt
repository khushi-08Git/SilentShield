package com.example.silentshield.data.detection

import android.util.Log
import com.example.silentshield.presentation.home.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

data class CallCheckResult(
    val isScam: Boolean,
    val riskLevel: RiskLevel,
    val reason: String
)

object ScamNumberChecker {

    // Known Indian scam prefixes and patterns
    private val highRiskPrefixes = listOf(
        "1800116", "18001",         // fake toll-free
        "+1809", "+1876", "+1473"   // Caribbean premium numbers
    )

    private val scamKeywordPatterns = listOf(
        "00911", "0091"             // international India spoofs
    )

    suspend fun check(phoneNumber: String): CallCheckResult {
        val cleaned = phoneNumber.replace(Regex("[\\s\\-()]"), "")

        // ── Layer 1: Local pattern checks (instant, no network) ────────
        val localResult = runLocalChecks(cleaned)
        if (localResult.riskLevel == RiskLevel.HIGH) return localResult

        // ── Layer 2: NumLookup free API (network) ──────────────────────
        return try {
            val apiResult = checkWithNumLookup(cleaned)
            // Return whichever is more severe
            if (apiResult.isScam) apiResult else localResult
        } catch (e: Exception) {
            Log.e("ScamNumberChecker", "API check failed: ${e.message}")
            localResult // fall back to local if API fails
        }
    }

    private fun runLocalChecks(number: String): CallCheckResult {
        // Private / hidden number
        if (number.isBlank() || number == "-1" || number == "unknown") {
            return CallCheckResult(
                isScam    = true,
                riskLevel = RiskLevel.MEDIUM,
                reason    = "Hidden or private number"
            )
        }

        // High risk prefix match
        highRiskPrefixes.forEach { prefix ->
            if (number.startsWith(prefix)) {
                return CallCheckResult(
                    isScam    = true,
                    riskLevel = RiskLevel.HIGH,
                    reason    = "Matches known scam number prefix"
                )
            }
        }

        // Spoof patterns
        scamKeywordPatterns.forEach { pattern ->
            if (number.contains(pattern)) {
                return CallCheckResult(
                    isScam    = true,
                    riskLevel = RiskLevel.HIGH,
                    reason    = "International spoof pattern detected"
                )
            }
        }

        // Repeated digit patterns (e.g. 9999999999) — often robocalls
        if (number.length >= 10) {
            val digits = number.takeLast(10)
            if (digits.all { it == digits[0] }) {
                return CallCheckResult(
                    isScam    = true,
                    riskLevel = RiskLevel.MEDIUM,
                    reason    = "Repeated digit pattern — likely robocall"
                )
            }
        }

        return CallCheckResult(
            isScam    = false,
            riskLevel = RiskLevel.LOW,
            reason    = "No local flags found"
        )
    }

    private suspend fun checkWithNumLookup(number: String): CallCheckResult =
        withContext(Dispatchers.IO) {
            // NumLookup free tier — no API key needed
            val cleanNumber = number.removePrefix("+").removePrefix("91")
            val url = "https://api.numlookupapi.com/v1/info/$cleanNumber"
            val response = URL(url).readText()

            // Basic check — if response contains "VOIP" or "toll_free" flag it
            val isVoip     = response.contains("\"line_type\":\"voip\"", ignoreCase = true)
            val isTollFree = response.contains("\"line_type\":\"toll_free\"", ignoreCase = true)

            when {
                isVoip -> CallCheckResult(
                    isScam    = true,
                    riskLevel = RiskLevel.MEDIUM,
                    reason    = "VOIP number — commonly used in scam calls"
                )
                isTollFree -> CallCheckResult(
                    isScam    = false,
                    riskLevel = RiskLevel.LOW,
                    reason    = "Toll-free number"
                )
                else -> CallCheckResult(
                    isScam    = false,
                    riskLevel = RiskLevel.LOW,
                    reason    = "Number appears legitimate"
                )
            }
        }
}