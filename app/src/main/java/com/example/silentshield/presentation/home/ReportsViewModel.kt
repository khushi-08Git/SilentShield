package com.example.displace.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.silentshield.data.firebase.ReportsRepository
import com.example.silentshield.presentation.home.AlertType
import com.example.silentshield.presentation.home.ReportsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class SubmitState {
    object Idle    : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}

class ReportsViewModel : ViewModel() {

    private val repository = ReportsRepository()

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState

    init {
        observeReports()
    }

    fun observeReports() {
        viewModelScope.launch {
            repository.getReportsStream()
                .catch { _uiState.value = ReportsUiState.Error("Failed to load reports") }
                .collect { reports ->
                    _uiState.value = ReportsUiState.Success(reports)
                }
        }
    }

    fun upvoteReport(reportId: String) {
        // Optimistic update first for instant UI feel
        val current = _uiState.value as? ReportsUiState.Success ?: return
        _uiState.value = current.copy(
            reports = current.reports.map {
                if (it.id == reportId) it.copy(upvotes = it.upvotes + 1) else it
            }
        )
        // Then write to Firebase
        viewModelScope.launch {
            repository.upvoteReport(reportId)
        }
    }

    fun submitReport(type: AlertType, source: String, description: String) {
        if (source.isBlank() || description.isBlank()) {
            _submitState.value = SubmitState.Error("Please fill all fields")
            return
        }
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            val result = repository.submitReport(type, source, description)
            _submitState.value = if (result.isSuccess) {
                SubmitState.Success
            } else {
                SubmitState.Error("Failed to submit. Try again.")
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }
}