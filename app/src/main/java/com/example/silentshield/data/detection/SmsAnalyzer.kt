package com.example.silentshield.data.detection

import com.example.silentshield.presentation.home.RiskLevel


data class SmsCheckResult(
    val isPhishing: Boolean,
    val riskLevel: RiskLevel,
    val reason: String,
    val flaggedKeywords: List<String>
)

object SmsAnalyzer {

    // Each entry: keywords that must ALL appear in the message
    private val highRiskPatterns = listOf(
        listOf("otp", "share"),
        listOf("otp", "expires"),
        listOf("otp", "do not share"),
        listOf("verify", "account", "click"),
        listOf("kyc", "update", "link"),
        listOf("kyc", "suspended", "click"),
        listOf("debit card", "blocked", "click"),
        listOf("credit card", "suspended", "verify"),
        listOf("prize", "won", "claim"),
        listOf("winner", "lottery", "click"),
        listOf("loan", "approved", "click"),
        listOf("loan", "disburse", "link"),
        listOf("reward", "redeem", "link"),
        listOf("account", "suspended", "verify"),
        listOf("bank", "blocked", "update"),
        listOf("aadhaar", "link", "click"),
        listOf("pan", "verify", "link"),
        listOf("income tax", "refund", "click"),
        listOf("refund", "click", "link"),
        listOf("upi", "blocked", "verify")
    )

    private val suspiciousPatterns = listOf(
        listOf("free", "click"),
        listOf("offer", "expires", "today"),
        listOf("congratulations", "selected"),
        listOf("job offer", "apply"),
        listOf("work from home", "earn"),
        listOf("investment", "guaranteed", "return"),
        listOf("double", "money", "invest")
    )

    // URL shorteners in SMS are almost always suspicious
    private val suspiciousUrlPatterns = listOf(
        "bit.ly", "tinyurl", "t.co", "goo.gl",
        "ow.ly", "is.gd", "buff.ly", "rebrand.ly"
    )

    fun analyze(sender: String, message: String): SmsCheckResult {
        val lower = message.lowercase()
        val flagged = mutableListOf<String>()

        // ── Check high risk patterns ───────────────────────────────────
        for (pattern in highRiskPatterns) {
            if (pattern.all { keyword -> lower.contains(keyword) }) {
                flagged.addAll(pattern)
                return SmsCheckResult(
                    isPhishing      = true,
                    riskLevel       = RiskLevel.HIGH,
                    reason          = "High-risk phishing pattern detected",
                    flaggedKeywords = flagged.distinct()
                )
            }
        }

        // ── Check suspicious URL shorteners ───────────────────────────
        val hasShortUrl = suspiciousUrlPatterns.any { lower.contains(it) }
        if (hasShortUrl) {
            flagged.add("suspicious URL shortener")
        }

        // ── Check medium risk patterns ─────────────────────────────────
        for (pattern in suspiciousPatterns) {
            if (pattern.all { keyword -> lower.contains(keyword) }) {
                flagged.addAll(pattern)
            }
        }

        // ── Check sender ID spoofing ───────────────────────────────────
        // Legitimate Indian bank SMS IDs follow VM-BANKNAME or BZ-BANKNAME format
        val isSpoofedSender = isSenderSpoofed(sender, lower)
        if (isSpoofedSender) {
            flagged.add("sender ID spoofed to look like a bank")
        }

        return when {
            flagged.size >= 3 || (hasShortUrl && flagged.size >= 2) -> SmsCheckResult(
                isPhishing      = true,
                riskLevel       = RiskLevel.HIGH,
                reason          = "Multiple phishing signals detected",
                flaggedKeywords = flagged.distinct()
            )
            flagged.isNotEmpty() -> SmsCheckResult(
                isPhishing      = true,
                riskLevel       = RiskLevel.MEDIUM,
                reason          = "Suspicious SMS pattern",
                flaggedKeywords = flagged.distinct()
            )
            else -> SmsCheckResult(
                isPhishing      = false,
                riskLevel       = RiskLevel.LOW,
                reason          = "No phishing patterns found",
                flaggedKeywords = emptyList()
            )
        }
    }

    private fun isSenderSpoofed(sender: String, messageContent: String): Boolean {
        val lowerSender = sender.lowercase()
        // If sender looks like a bank but message has suspicious patterns
        val bankNames = listOf("sbi", "hdfc", "icici", "axis", "kotak", "paytm", "phonepe")
        val senderClaimsToBeBank = bankNames.any { lowerSender.contains(it) }
        val messageHasSuspiciousLink = suspiciousUrlPatterns.any { messageContent.contains(it) }
        return senderClaimsToBeBank && messageHasSuspiciousLink
    }
}