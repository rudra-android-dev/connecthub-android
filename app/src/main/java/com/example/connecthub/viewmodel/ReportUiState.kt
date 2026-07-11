package com.example.connecthub.viewmodel

data class ReportUiState(
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val alreadyReported: Boolean = false
)