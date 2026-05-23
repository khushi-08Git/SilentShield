package com.example.silentshield.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentshield.ui.theme.AppColors

data class BottomNavItem(
    val label: String,
    val key: Any,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home",     HomeKey,     Icons.Default.Home),
    BottomNavItem("Scan",     ScanLinkKey, Icons.Default.Build),
    BottomNavItem("Reports",  ReportsKey,  Icons.Default.Warning),
    BottomNavItem("Settings", SettingsKey, Icons.Default.Settings),
)

@Composable
fun BottomNavBar(
    currentKey: Any,
    onNavigate: (Any) -> Unit
) {
    NavigationBar(
        containerColor = AppColors.BgCard,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentKey::class == item.key::class

            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.key) },
                icon     = { Icon(item.icon, contentDescription = item.label) },
                label    = { Text(item.label, fontSize = 11.sp) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = AppColors.AccentGreen,
                    selectedTextColor   = AppColors.AccentGreen,
                    unselectedIconColor = AppColors.TextMuted,
                    unselectedTextColor = AppColors.TextMuted,
                    indicatorColor      = AppColors.AccentGreen.copy(alpha = 0.12f)
                )
            )
        }
    }
}