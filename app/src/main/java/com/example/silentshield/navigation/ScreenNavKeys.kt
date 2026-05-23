package com.example.silentshield.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashKey : NavKey

@Serializable
data object PermissionKey : NavKey

@Serializable
data object OnboardingKey : NavKey

@Serializable
data object HomeKey : NavKey

@Serializable
data object ScanLinkKey : NavKey

@Serializable
data object ReportsKey : NavKey

@Serializable
data object SettingsKey : NavKey

@Serializable
data class AlertDetailKey(val alertId: String) : NavKey