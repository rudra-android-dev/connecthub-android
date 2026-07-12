package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.User

data class BlockedUsersUiState(
    val blockedUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)