package com.example.silentshield.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.displace.presentation.reports.ReportsScreen
import com.example.silentshield.presentation.home.AlertDetailScreen
import com.example.silentshield.presentation.home.HomeScreen
import com.example.silentshield.presentation.home.ScanLinkScreen
import com.example.silentshield.presentation.home.SettingsScreen
import com.example.silentshield.presentation.onboarding.OnboardingScreen
import com.example.silentshield.presentation.permission.PermissionScreen
import com.example.silentshield.presentation.splash.SplashScreen
import com.example.silentshield.ui.theme.AppColors

@Composable
fun AppNavGraph() {
    val backStack = rememberNavBackStack(SplashKey)

    // Current top of the back stack — drives bottom bar highlight & visibility
    val currentKey by remember { derivedStateOf { backStack.lastOrNull() } }

    // Bottom bar is only shown for the 4 main tabs
    val showBottomBar by remember {
        derivedStateOf {
            currentKey is HomeKey ||
                    currentKey is ScanLinkKey ||
                    currentKey is ReportsKey ||
                    currentKey is SettingsKey
        }
    }

    Scaffold(
        containerColor = AppColors.BgDeep,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentKey = currentKey ?: HomeKey,
                    onNavigate = { key ->
                        // Avoid duplicate entries — clear tabs above Home then push
                        backStack.removeAll { it is HomeKey || it is ScanLinkKey || it is ReportsKey || it is SettingsKey }
                        backStack.add(key as NavKey)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BgDeep)
                .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = backStack,
                onBack    = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {

                    entry<SplashKey> {
                        SplashScreen(
                            onNavigate = { backStack.add(PermissionKey) }
                        )
                    }

                    entry<PermissionKey> {
                        PermissionScreen(
                            onContinue = { backStack.add(OnboardingKey) }
                        )
                    }

                    entry<OnboardingKey> {
                        OnboardingScreen(
                            onFinish = {
                                backStack.clear()
                                backStack.add(HomeKey)
                            }
                        )
                    }

                    entry<HomeKey> {
                        HomeScreen(
                            onAlertClick = { alertId ->
                                backStack.add(AlertDetailKey(alertId))
                            }
                        )
                    }

                    entry<ScanLinkKey> {
                        ScanLinkScreen()
                    }

                    entry<ReportsKey> {
                        ReportsScreen()
                    }

                    entry<SettingsKey> {
                        SettingsScreen()
                    }

                    entry<AlertDetailKey> { key ->
                        AlertDetailScreen(
                            alertId = key.alertId,
                            onBack  = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}