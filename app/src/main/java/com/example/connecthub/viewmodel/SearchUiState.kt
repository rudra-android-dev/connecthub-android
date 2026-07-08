package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User

data class SearchUiState(
    val searchQuery: String = "",
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class UserProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val blockMessage: String? = null
)