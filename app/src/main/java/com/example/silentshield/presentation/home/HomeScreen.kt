package com.example.silentshield.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silentshield.R

// ── Design tokens ──────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF0D1117)
private val BgCard       = Color(0xFF161B22)
private val BgCardAlt    = Color(0xFF1C2230)
private val AccentGreen  = Color(0xFF00E676)
private val AccentRed    = Color(0xFFFF3D57)
private val AccentAmber  = Color(0xFFFFAB00)
private val AccentBlue   = Color(0xFF448AFF)
private val TextPrimary  = Color(0xFFE6EDF3)
private val TextMuted    = Color(0xFF8B949E)
private val DividerColor = Color(0xFF21262D)

// ── Entry point ────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onAlertClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isActive by viewModel.isProtectionActive.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        when (uiState) {
            is HomeUiState.Loading -> LoadingContent()
            is HomeUiState.Error   -> ErrorContent(
                message = (uiState as HomeUiState.Error).message,
                onRetry = viewModel::loadDashboard
            )
            is HomeUiState.Success -> SuccessContent(
                state    = uiState as HomeUiState.Success,
                isActive = isActive,
                onToggle = viewModel::toggleProtection,
                onRefresh = viewModel::loadDashboard,
                onAlertClick  = onAlertClick
            )
        }
    }
}

// ── Loading ────────────────────────────────────────────────────────────────
@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AccentGreen)
            Spacer(Modifier.height(16.dp))
            Text("Loading shield…", color = TextMuted, fontSize = 14.sp)
        }
    }
}

// ── Error ──────────────────────────────────────────────────────────────────
@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRed, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text("Retry", color = BgDeep, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Main dashboard ─────────────────────────────────────────────────────────
@Composable
private fun SuccessContent(
    state: HomeUiState.Success,
    isActive: Boolean,
    onToggle: () -> Unit,
    onRefresh: () -> Unit,
    onAlertClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top bar
        item { TopBar(onRefresh = onRefresh) }

        // Shield status hero
        item { ShieldStatusCard(isActive = isActive, onToggle = onToggle) }

        // Stats row
        item {
            StatsRow(stats = state.stats)
        }

        // Section header
        item {
            SectionHeader(
                title = "Recent Alerts",
                count = state.recentAlerts.size
            )
        }

        if (state.recentAlerts.isEmpty()) {
            item { EmptyAlerts() }
        } else {
            items(state.recentAlerts) { alert ->
                AlertCard(
                    alert = alert,
                    onAlertClick = onAlertClick
                )
            }
        }
    }
}

// ── Top bar ────────────────────────────────────────────────────────────────
@Composable
private fun TopBar(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SilentShield",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Your digital guardian",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .clip(CircleShape)
                .background(BgCard)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextMuted)
        }
    }
}

// ── Shield hero card ───────────────────────────────────────────────────────
@Composable
private fun ShieldStatusCard(isActive: Boolean, onToggle: () -> Unit) {
    val shieldColor by animateColorAsState(
        targetValue = if (isActive) AccentGreen else AccentRed,
        animationSpec = tween(500),
        label = "shieldColor"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.6f,
        animationSpec = tween(500),
        label = "bgAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isActive)
                        listOf(Color(0xFF0A2A1A), Color(0xFF0D1F2D))
                    else
                        listOf(Color(0xFF2A0A0A), Color(0xFF1F0D0D))
                )
            )
    ) {
        // Subtle border glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            shieldColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shield),
                        contentDescription = null,
                        tint = shieldColor,
                        modifier = Modifier
                            .size(28.dp)
                            .alpha(bgAlpha)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isActive) "Protected" else "Paused",
                        color = shieldColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isActive)
                        "SilentShield is actively monitoring calls, SMS, and links."
                    else
                        "Protection is paused. Tap to re-enable.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BgDeep,
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = BgCard,
                    uncheckedTrackColor = AccentRed.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// ── Stats row ──────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(stats: ShieldStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatChip(
            painter = painterResource(R.drawable.ic_phone),
            label = "Calls\nBlocked",
            value = stats.callsBlocked.toString(),
            color = AccentRed,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            painter = painterResource(R.drawable.ic_sms),
            label = "SMS\nScanned",
            value = stats.smsScanned.toString(),
            color = AccentAmber,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            painter = painterResource(R.drawable.ic_link),
            label = "Links\nChecked",
            value = stats.linksChecked.toString(),
            color = AccentBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    painter: Painter,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

// ── Section header ─────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AccentRed.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text("$count threats", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Empty state ────────────────────────────────────────────────────────────
@Composable
private fun EmptyAlerts() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(painter = painterResource(R.drawable.ic_shield), contentDescription = null, tint = AccentGreen, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(12.dp))
        Text("All clear!", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("No threats detected recently.", color = TextMuted, fontSize = 13.sp)
    }
}

// ── Alert card ─────────────────────────────────────────────────────────────
@Composable
private fun AlertCard(
    alert: RecentAlert,
    onAlertClick: (String) -> Unit
) {
    val (accentColor, bgColor) = when (alert.riskLevel) {
        RiskLevel.HIGH   -> AccentRed   to Color(0xFF2A0A0A)
        RiskLevel.MEDIUM -> AccentAmber to Color(0xFF2A1F00)
        RiskLevel.LOW    -> AccentBlue  to Color(0xFF0A1A2A)
    }

    val typeIcon = when (alert.type) {
        AlertType.CALL -> painterResource(R.drawable.ic_phone)
        AlertType.SMS  -> painterResource(R.drawable.ic_sms)
        AlertType.LINK -> painterResource(R.drawable.ic_link)
    }

    val riskLabel = when (alert.riskLevel) {
        RiskLevel.HIGH   -> "HIGH RISK"
        RiskLevel.MEDIUM -> "MEDIUM"
        RiskLevel.LOW    -> "LOW"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable { onAlertClick(alert.id) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon bubble
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(14.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.source,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = alert.summary,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            // Right side: risk badge + time
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(riskLabel, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(alert.timeAgo, color = TextMuted, fontSize = 11.sp)
            }
        }

        // Bottom accent line for HIGH risk
        if (alert.riskLevel == RiskLevel.HIGH) {
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.5.dp,
                color = accentColor.copy(alpha = 0.3f)
            )
        }
    }
}