package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User

data class UserProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val blockMessage: String? = null,
    val isBlocked: Boolean = false
)