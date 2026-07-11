package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User

data class ProfileUiState(
    val user: User? = null,
    val postCount: Int = 0,
    val userPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)