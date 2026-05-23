package com.example.silentshield.presentation.home

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silentshield.R
import com.example.silentshield.ui.theme.AppColors

// ── ViewModel ──────────────────────────────────────────────────────────────

data class SettingsState(
    val callScanEnabled: Boolean      = true,
    val smsScanEnabled: Boolean       = true,
    val notificationsEnabled: Boolean = true,
    val autoBlockHighRisk: Boolean    = false,
    val communityReports: Boolean     = true
)

class SettingsViewModel : ViewModel() {
    var state by mutableStateOf(SettingsState())
        private set

    fun toggleCallScan()        { state = state.copy(callScanEnabled      = !state.callScanEnabled) }
    fun toggleSmsScan()         { state = state.copy(smsScanEnabled       = !state.smsScanEnabled) }
    fun toggleNotifications()   { state = state.copy(notificationsEnabled = !state.notificationsEnabled) }
    fun toggleAutoBlock()       { state = state.copy(autoBlockHighRisk    = !state.autoBlockHighRisk) }
    fun toggleCommunityReports(){ state = state.copy(communityReports     = !state.communityReports) }
}

// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Settings", color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Customize your protection", color = AppColors.TextMuted, fontSize = 12.sp)
                }
            }

            // ── Protection section
            SettingsSection(title = "Protection") {
                ToggleRow(
                    icon        = painterResource(R.drawable.ic_phone),
                    iconColor   = AppColors.AccentGreen,
                    title       = "Call Scanning",
                    subtitle    = "Detect scam & spam calls",
                    checked     = state.callScanEnabled,
                    onToggle    = viewModel::toggleCallScan
                )
                SettingsDivider()
                ToggleRow(
                    icon        = painterResource(R.drawable.ic_sms),
                    iconColor   = AppColors.AccentBlue,
                    title       = "SMS Scanning",
                    subtitle    = "Detect phishing messages",
                    checked     = state.smsScanEnabled,
                    onToggle    = viewModel::toggleSmsScan
                )
                SettingsDivider()
                ToggleRow(
                    icon        = painterResource(R.drawable.ic_warning),
                    iconColor   = AppColors.AccentRed,
                    title       = "Auto-block High Risk",
                    subtitle    = "Silently block flagged callers",
                    checked     = state.autoBlockHighRisk,
                    onToggle    = viewModel::toggleAutoBlock
                )
            }

            // ── Alerts section
            SettingsSection(title = "Alerts") {
                ToggleRow(
                    icon        = painterResource(R.drawable.ic_notification),
                    iconColor   = AppColors.AccentAmber,
                    title       = "Notifications",
                    subtitle    = "Real-time fraud alerts",
                    checked     = state.notificationsEnabled,
                    onToggle    = viewModel::toggleNotifications
                )
                SettingsDivider()
                ToggleRow(
                    icon        = painterResource(R.drawable.ic_people),
                    iconColor   = AppColors.AccentBlue,
                    title       = "Community Reports",
                    subtitle    = "Receive crowdsourced alerts",
                    checked     = state.communityReports,
                    onToggle    = viewModel::toggleCommunityReports
                )
            }

            // ── Permissions section
            SettingsSection(title = "Permissions") {
                PermissionStatusRow(painterResource(R.drawable.ic_phone),        "Phone",        AppColors.AccentGreen, true)
                SettingsDivider()
                PermissionStatusRow(painterResource(R.drawable.ic_sms),      "SMS",          AppColors.AccentBlue,  true)
                SettingsDivider()
                PermissionStatusRow(painterResource(R.drawable.ic_notification),"Notifications",AppColors.AccentAmber, Build.VERSION.SDK_INT >= 33)
            }

            // ── About section
            SettingsSection(title = "About") {
                InfoRow(painterResource(R.drawable.ic_info),     "App Version",   "1.0.0-MVP")
                SettingsDivider()
                InfoRow(painterResource(R.drawable.ic_shield),   "Protection DB", "Updated today")
                SettingsDivider()
                ActionRow(
                    icon      = Icons.Default.Star,
                    iconColor = AppColors.AccentAmber,
                    title     = "Rate SilentShield",
                    onClick   = { /* Open Play Store */ }
                )
                SettingsDivider()
                ActionRow(
                    icon      = Icons.Default.Share,
                    iconColor = AppColors.AccentBlue,
                    title     = "Share with Friends",
                    onClick   = { /* Share intent */ }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            color = AppColors.TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.BgCard)
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    Divider(color = AppColors.Divider, thickness = 0.5.dp)
}

@Composable
private fun ToggleRow(
    icon: Painter,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = AppColors.TextMuted, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor  = AppColors.BgDeep,
                checkedTrackColor  = AppColors.AccentGreen,
                uncheckedThumbColor = AppColors.BgCard,
                uncheckedTrackColor = AppColors.Divider
            )
        )
    }
}

@Composable
private fun PermissionStatusRow(icon: Painter, title: String, color: Color, granted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(title, color = AppColors.TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (granted) AppColors.AccentGreen.copy(alpha = 0.13f) else AppColors.AccentRed.copy(alpha = 0.13f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (granted) "Granted" else "Denied",
                color = if (granted) AppColors.AccentGreen else AppColors.AccentRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoRow(icon: Painter, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = AppColors.TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = AppColors.TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun ActionRow(icon: ImageVector, iconColor: Color, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = AppColors.TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
    }
}