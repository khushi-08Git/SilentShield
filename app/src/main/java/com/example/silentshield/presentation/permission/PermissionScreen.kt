package com.example.silentshield.presentation.permission

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel = viewModel(),
    onContinue: () -> Unit
) {
    val state = viewModel.state

    // --- Phone permission ---
    val phonePermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
    )

    // --- SMS permission ---
    val smsPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
    )

    // --- Notification permission (Android 13+) ---
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    // Sync real permission status → ViewModel whenever it changes
    LaunchedEffect(phonePermissionState.allPermissionsGranted) {
        viewModel.updatePhone(phonePermissionState.allPermissionsGranted)
    }
    LaunchedEffect(smsPermissionState.allPermissionsGranted) {
        viewModel.updateSms(smsPermissionState.allPermissionsGranted)
    }
    LaunchedEffect(
        notificationPermissionState?.status,
        notificationPermissionState?.status?.isGranted
    ) {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionState?.status?.isGranted == true
        } else true // Auto-granted below Android 13
        viewModel.updateNotification(granted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        Text(
            "Required Permissions",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Text("We only use permissions to detect scam calls and fraud SMS.")

        Spacer(Modifier.height(32.dp))

        // Phone Calls card
        PermissionItem(
            title = "Phone Calls",
            description = "Detect incoming scam calls",
            granted = state.phoneGranted,
            onGrant = {
                phonePermissionState.launchMultiplePermissionRequest()
            }
        )

        // SMS Access card
        PermissionItem(
            title = "SMS Access",
            description = "Scan messages for phishing",
            granted = state.smsGranted,
            onGrant = {
                smsPermissionState.launchMultiplePermissionRequest()
            }
        )

        // Notifications card
        PermissionItem(
            title = "Notifications",
            description = "Get real-time fraud alerts",
            granted = state.notificationGranted,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionState?.launchPermissionRequest()
                }
                // Below Android 13: no runtime permission needed, mark granted
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    viewModel.updateNotification(true)
                }
            }
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.allGranted
        ) {
            Text("Continue")
        }

        TextButton(
            onClick = onContinue, // or navigate separately if skip has different flow
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip for now")
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    if (granted) "Granted ✓" else "Required",
                    color = if (granted) Color(0xFF00C853) else Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onGrant,
                enabled = !granted
            ) {
                Text(if (granted) "Done" else "Grant")
            }
        }
    }
}