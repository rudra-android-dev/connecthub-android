package com.example.connecthub.viewmodel

data class FollowUiState(
    val isFollowing: Boolean = false,
    val followers: Int = 0,
    val following: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)