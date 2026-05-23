package com.example.displace.presentation.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silentshield.R
import com.example.silentshield.presentation.home.AlertType
import com.example.silentshield.presentation.home.CommunityReport
import com.example.silentshield.presentation.home.ReportsUiState
import com.example.silentshield.presentation.home.RiskLevel
import com.example.silentshield.ui.theme.AppColors


// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = viewModel()) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    var showDialog  by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Community Reports",
                        color      = AppColors.TextPrimary,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Crowdsourced scam intelligence",
                        color    = AppColors.TextMuted,
                        fontSize = 12.sp
                    )
                }
                FloatingActionButton(
                    onClick          = { showDialog = true },
                    modifier         = Modifier.size(44.dp),
                    containerColor   = AppColors.AccentGreen,
                    contentColor     = AppColors.BgDeep,
                    shape            = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Report")
                }
            }

            // ── Body ──────────────────────────────────────────────────
            when (uiState) {
                is ReportsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AppColors.AccentGreen)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading reports…", color = AppColors.TextMuted, fontSize = 13.sp)
                        }
                    }
                }

                is ReportsUiState.Success -> {
                    val reports = (uiState as ReportsUiState.Success).reports
                    if (reports.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No reports yet. Be the first to report a scam!",
                                color    = AppColors.TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier            = Modifier.padding(horizontal = 20.dp)
                        ) {
                            items(
                                items = reports,
                                key   = { it.id }
                            ) { report ->
                                CommunityReportCard(
                                    report   = report,
                                    onUpvote = { viewModel.upvoteReport(report.id) }
                                )
                            }
                        }
                    }
                }

                is ReportsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                (uiState as ReportsUiState.Error).message,
                                color    = AppColors.AccentRed,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.observeReports() },
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.AccentGreen
                                )
                            ) {
                                Text("Retry", color = AppColors.BgDeep)
                            }
                        }
                    }
                }
            }
        }

        // ── Submit dialog ──────────────────────────────────────────────
        if (showDialog) {
            SubmitReportDialog(
                submitState = submitState,
                onSubmit    = { type, source, desc ->
                    viewModel.submitReport(type, source, desc)
                },
                onDismiss   = {
                    showDialog = false
                    viewModel.resetSubmitState()
                }
            )
        }
    }
}

// ── Report card ────────────────────────────────────────────────────────────

@Composable
private fun CommunityReportCard(
    report: CommunityReport,
    onUpvote: () -> Unit
) {
    val accentColor = when (report.riskLevel) {
        RiskLevel.HIGH   -> AppColors.AccentRed
        RiskLevel.MEDIUM -> AppColors.AccentAmber
        RiskLevel.LOW    -> AppColors.AccentBlue
    }
    val typeIcon: Painter = when (report.type) {
        AlertType.CALL -> painterResource(R.drawable.ic_phone)
        AlertType.SMS  -> painterResource(R.drawable.ic_sms)
        AlertType.LINK -> painterResource(R.drawable.ic_link)
    }
    Card(
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(accentColor.copy(alpha = 0.13f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            typeIcon,
                            contentDescription = null,
                            tint     = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        report.source,
                        color      = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.13f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        report.riskLevel.name,
                        color      = accentColor,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                report.description,
                color      = AppColors.TextMuted,
                fontSize   = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(12.dp))

            // Bottom row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "${report.reportedBy}  ·  ${report.timeAgo}",
                    color    = AppColors.TextMuted.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
                OutlinedButton(
                    onClick        = onUpvote,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape          = RoundedCornerShape(8.dp),
                    border         = BorderStroke(1.dp, AppColors.Divider),
                    modifier       = Modifier.height(30.dp)
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint     = AppColors.TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("${report.upvotes}", color = AppColors.TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

// ── Submit dialog ──────────────────────────────────────────────────────────

@Composable
private fun SubmitReportDialog(
    submitState: SubmitState,
    onSubmit: (AlertType, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var source      by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AlertType.CALL) }

    // Auto-dismiss after success
    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            kotlinx.coroutines.delay(800)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = AppColors.BgCard,
        title = {
            Text(
                "Submit a Report",
                color      = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Type chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertType.entries.forEach { type ->
                        val selected = selectedType == type
                        OutlinedButton(
                            onClick        = { selectedType = type },
                            colors         = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected)
                                    AppColors.AccentGreen.copy(alpha = 0.15f)
                                else Color.Transparent,
                                contentColor   = if (selected)
                                    AppColors.AccentGreen
                                else AppColors.TextMuted
                            ),
                            border         = BorderStroke(
                                1.dp,
                                if (selected) AppColors.AccentGreen else AppColors.Divider
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape          = RoundedCornerShape(8.dp)
                        ) {
                            Text(type.name, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value         = source,
                    onValueChange = { source = it },
                    label         = {
                        Text("Number / URL / Sender ID", color = AppColors.TextMuted)
                    },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = AppColors.AccentGreen,
                        unfocusedBorderColor = AppColors.Divider,
                        focusedTextColor     = AppColors.TextPrimary,
                        unfocusedTextColor   = AppColors.TextPrimary
                    )
                )

                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("What happened?", color = AppColors.TextMuted) },
                    maxLines      = 4,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = AppColors.AccentGreen,
                        unfocusedBorderColor = AppColors.Divider,
                        focusedTextColor     = AppColors.TextPrimary,
                        unfocusedTextColor   = AppColors.TextPrimary
                    )
                )

                when (submitState) {
                    is SubmitState.Error ->
                        Text(submitState.message, color = AppColors.AccentRed, fontSize = 12.sp)
                    is SubmitState.Success ->
                        Text("✓ Report submitted!", color = AppColors.AccentGreen, fontSize = 12.sp)
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onSubmit(selectedType, source, description) },
                enabled  = submitState !is SubmitState.Loading &&
                        submitState !is SubmitState.Success,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentGreen
                )
            ) {
                if (submitState is SubmitState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        color       = AppColors.BgDeep,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit", color = AppColors.BgDeep, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextMuted)
            }
        }
    )
}