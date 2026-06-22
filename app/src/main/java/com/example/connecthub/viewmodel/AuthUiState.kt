package com.example.connecthub.viewmodel

data class AuthUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)