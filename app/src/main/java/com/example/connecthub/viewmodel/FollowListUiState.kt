package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.User

data class FollowListUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)