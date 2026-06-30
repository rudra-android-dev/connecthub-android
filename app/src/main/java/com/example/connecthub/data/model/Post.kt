package com.example.connecthub.data.model

data class Post(

    val postId: String = "",

    val userId: String = "",

    val username: String = "",

    val content: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    val likeCount: Int = 0,

    val likedBy: List<String> = emptyList(),

    val commentCount: Int = 0,

    val profileImageUrl: String = ""

)