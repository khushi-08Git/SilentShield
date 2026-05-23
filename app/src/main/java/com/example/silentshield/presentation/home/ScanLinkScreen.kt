package com.example.silentshield.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.displace.presentation.scanclink.ScanLinkViewModel
import com.example.silentshield.R
import com.example.silentshield.ui.theme.AppColors

@Composable
fun ScanLinkScreen(viewModel: ScanLinkViewModel = viewModel()) {
    val state = viewModel.state
    val keyboard = LocalSoftwareKeyboardController.current

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.AccentBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_link), contentDescription = null, tint = AppColors.AccentBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Link Scanner", color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Check any URL for phishing", color = AppColors.TextMuted, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // URL Input
            OutlinedTextField(
                value = state.url,
                onValueChange = viewModel::onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Paste a link here…", color = AppColors.TextMuted)
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_link),
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (state.url.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearUrl) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AppColors.TextMuted)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboard?.hide()
                        viewModel.scanUrl()
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AppColors.AccentBlue,
                    unfocusedBorderColor = AppColors.Divider,
                    focusedTextColor     = AppColors.TextPrimary,
                    unfocusedTextColor   = AppColors.TextPrimary,
                    cursorColor          = AppColors.AccentBlue
                )
            )

            Spacer(Modifier.height(16.dp))

            // Scan button
            Button(
                onClick = {
                    keyboard?.hide()
                    viewModel.scanUrl()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = state.url.isNotBlank() && !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentBlue,
                    disabledContainerColor = AppColors.BgCard
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.BgDeep,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Scanning…", color = AppColors.BgDeep, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.BgDeep)
                    Spacer(Modifier.width(8.dp))
                    Text("Scan Link", color = AppColors.BgDeep, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Result
            AnimatedVisibility(
                visible = state.status != ScanStatus.IDLE && state.status != ScanStatus.SCANNING,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                ScanResultCard(state = state)
            }

            // Idle illustration
            if (state.status == ScanStatus.IDLE) {
                Spacer(Modifier.height(24.dp))
                IdlePlaceholder()
            }
        }
    }
}

@Composable
private fun ScanResultCard(state: ScanLinkUiState) {
    val (accentColor, bgColor, icon, label) = when (state.status) {
        ScanStatus.SAFE      -> Quad(AppColors.AccentGreen, Color(0xFF061A0D), Icons.Default.CheckCircle, "SAFE")
        ScanStatus.SUSPICIOUS-> Quad(AppColors.AccentAmber, Color(0xFF1A1200), Icons.Default.Warning,     "SUSPICIOUS")
        ScanStatus.DANGEROUS -> Quad(AppColors.AccentRed,   Color(0xFF1A0608), Icons.Default.Warning,     "DANGEROUS")
        else -> return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(listOf(bgColor, AppColors.BgCard))
            )
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(state.resultSummary, color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (state.resultDetails.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Divider(color = AppColors.Divider, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            state.resultDetails.forEach { detail ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(accentColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(detail, color = AppColors.TextMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun IdlePlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AppColors.AccentBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_link), contentDescription = null, tint = AppColors.AccentBlue.copy(alpha = 0.4f), modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Paste any suspicious link", color = AppColors.TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Text("We'll check it instantly", color = AppColors.TextMuted.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

// Helper data class for destructuring 4 values
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)