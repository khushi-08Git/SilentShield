package com.example.silentshield.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.silentshield.data.repository.ScamDetectionRepository
import kotlinx.coroutines.launch

class AlertDetailViewModel(
    private val alertId: String,
    private val repository: ScamDetectionRepository = ScamDetectionRepository()
) : ViewModel() {

    var alert: RecentAlert? by mutableStateOf(null)
        private set

    init {
        viewModelScope.launch {
            alert = repository.getAlertById(alertId)
        }
    }
}

class AlertDetailViewModelFactory(private val alertId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlertDetailViewModel(alertId) as T
    }
}