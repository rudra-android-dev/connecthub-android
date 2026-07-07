package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.Post

data class BookmarkUiState(
    val posts: List<Post> = emptyList(),
    val bookmarkedPostIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)