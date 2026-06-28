package com.example.connecthub.viewmodel

import com.example.connecthub.data.model.Comment

data class CommentUiState(

    val comments: List<Comment> = emptyList(),

    val isLoading: Boolean = false,

    val error: String? = null

)