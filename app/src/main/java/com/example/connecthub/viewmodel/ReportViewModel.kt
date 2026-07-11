package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.model.Post
import com.example.connecthub.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportViewModel : ViewModel() {

    private val repository = ReportRepository()

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    fun submitReport(post: Post, reason: String) {
        if (reason.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please select a reason.")
            return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

        repository.submitReport(post, reason) { success, message ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    success = true,
                    error = null
                )
            } else {
                val alreadyReported = message?.contains("already reported") == true
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    success = false,
                    error = message,
                    alreadyReported = alreadyReported
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState()
    }
}