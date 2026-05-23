package com.example.silentshield.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silentshield.R
import com.example.silentshield.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailScreen(
    alertId: String,
    onBack: () -> Unit,
    viewModel: AlertDetailViewModel = viewModel(
        factory = AlertDetailViewModelFactory(alertId)
    )
) {
    val alert = viewModel.alert

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgDeep)
    ) {
        if (alert == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.AccentGreen)
            }
            return@Box
        }

        val (accentColor, gradStart) = when (alert.riskLevel) {
            RiskLevel.HIGH   -> AppColors.AccentRed   to Color(0xFF1A0608)
            RiskLevel.MEDIUM -> AppColors.AccentAmber to Color(0xFF1A1200)
            RiskLevel.LOW    -> AppColors.AccentBlue  to Color(0xFF061018)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero banner ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(gradStart, AppColors.BgDeep)
                        )
                    )
                    .statusBarsPadding()
                    .padding(bottom = 28.dp)
            ) {
                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.TextPrimary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = when (alert.type) {
                                AlertType.CALL -> painterResource(R.drawable.ic_phone)
                                AlertType.SMS  -> painterResource(R.drawable.ic_sms)
                                AlertType.LINK -> painterResource(R.drawable.ic_link)
                            },
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Risk badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = when (alert.riskLevel) {
                                RiskLevel.HIGH   -> "⚠ HIGH RISK"
                                RiskLevel.MEDIUM -> "◉ MEDIUM RISK"
                                RiskLevel.LOW    -> "● LOW RISK"
                            },
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = alert.source,
                        color = AppColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = alert.summary,
                        color = AppColors.TextMuted,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = alert.timeAgo,
                        color = AppColors.TextMuted.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            // ── Detail cards ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                DetailSection(title = "What was detected") {
                    DetailRow("Type",   alert.type.name.lowercase().replaceFirstChar { it.uppercase() })
                    DetailRow("Source", alert.source)
                    DetailRow("Risk",   alert.riskLevel.name)
                    DetailRow("Time",   alert.timeAgo)
                }

                DetailSection(title = "Why it was flagged") {
                    ThreatReasonList(alert = alert)
                }

                DetailSection(title = "Recommended action") {
                    RecommendationBox(alert = alert, accentColor = accentColor)
                }

                // Action buttons
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { /* Report to community — wire later */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AppColors.BgDeep)
                    Spacer(Modifier.width(8.dp))
                    Text("Report to Community", color = AppColors.BgDeep, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { /* Block action — wire later */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Divider)
                ) {
                    Text("Block ${alert.type.name.lowercase().replaceFirstChar { it.uppercase() }}")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.BgCard)
            .padding(16.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = AppColors.TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppColors.TextMuted, fontSize = 13.sp)
        Text(value, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    Divider(color = AppColors.Divider, thickness = 0.5.dp)
}

@Composable
private fun ThreatReasonList(alert: RecentAlert) {
    val reasons = when (alert.type) {
        AlertType.CALL -> listOf(
            "Number not in your contacts",
            "Matches known scam number database",
            "Called multiple users in short time",
            "Associated with KYC/OTP fraud patterns"
        )
        AlertType.SMS -> listOf(
            "Contains suspicious link or shortener",
            "Sender ID spoofed to look like a bank",
            "Requests OTP or personal information",
            "Grammar patterns match phishing templates"
        )
        AlertType.LINK -> listOf(
            "Domain registered recently (< 30 days)",
            "Redirects to a known phishing domain",
            "URL shortener masking destination",
            "SSL certificate mismatch"
        )
    }

    reasons.forEach { reason ->
        Row(
            modifier = Modifier.padding(vertical = 5.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("•", color = AppColors.AccentRed, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp, top = 1.dp))
            Text(reason, color = AppColors.TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun RecommendationBox(alert: RecentAlert, accentColor: Color) {
    val (icon, text) = when (alert.type) {
        AlertType.CALL -> painterResource(R.drawable.ic_phone) to "Do not call back. Block this number immediately. Never share OTPs or account details over a call."
        AlertType.SMS  -> painterResource(R.drawable.ic_sms) to "Do not click any links in the message. Delete the SMS. Report the sender to your carrier."
        AlertType.LINK -> painterResource(R.drawable.ic_link) to "Do not visit this URL. If you already opened it, clear your browser data and change passwords immediately."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accentColor.copy(alpha = 0.07f))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp).padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = AppColors.TextPrimary, fontSize = 13.sp, lineHeight = 20.sp)
    }
}
