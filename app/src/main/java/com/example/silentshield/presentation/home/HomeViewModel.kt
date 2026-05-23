package com.example.silentshield.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silentshield.data.local.AlertRepository
import com.example.silentshield.data.local.StatsDataStore
import com.example.silentshield.presentation.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val alertRepository = AlertRepository(application)
    private val statsDataStore  = StatsDataStore(application)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _isProtectionActive = MutableStateFlow(true)
    val isProtectionActive: StateFlow<Boolean> = _isProtectionActive

    init {
        viewModelScope.launch {
            // Combine real alerts + real stats into one UI state update
            combine(
                alertRepository.alertsStream,
                statsDataStore.statsFlow
            ) { alerts, stats ->
                HomeUiState.Success(
                    isProtectionActive = _isProtectionActive.value,
                    stats              = stats,
                    recentAlerts       = alerts
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleProtection() {
        _isProtectionActive.value = !_isProtectionActive.value
        val current = _uiState.value
        if (current is HomeUiState.Success) {
            _uiState.value = current.copy(isProtectionActive = _isProtectionActive.value)
        }
    }

    fun loadDashboard() {
        // Already live via Flow — nothing to manually reload
    }
}