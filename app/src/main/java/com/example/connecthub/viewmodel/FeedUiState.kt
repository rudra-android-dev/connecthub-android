package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.Post

data class FeedUiState(

    val posts: List<Post> = emptyList(),

    val blockedUserIds: Set<String> = emptySet(),

    val isLoading: Boolean = false,

    val success: Boolean = false,

    val error: String? = null

)