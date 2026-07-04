package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.Notification

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)