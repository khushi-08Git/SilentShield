package com.example.silentshield.presentation.permission

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class PermissionUiState(
    val phoneGranted: Boolean = false,
    val smsGranted: Boolean = false,
    val notificationGranted: Boolean = false
) {
    val allGranted: Boolean
        get() = phoneGranted && smsGranted && notificationGranted
}

class PermissionViewModel : ViewModel() {

    var state by mutableStateOf(PermissionUiState())
        private set

    fun updatePhone(value: Boolean) {
        state = state.copy(phoneGranted = value)
    }

    fun updateSms(value: Boolean) {
        state = state.copy(smsGranted = value)
    }

    fun updateNotification(value: Boolean) {
        state = state.copy(notificationGranted = value)
    }
}