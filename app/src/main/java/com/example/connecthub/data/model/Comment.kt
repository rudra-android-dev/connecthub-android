package com.example.connecthub.data.model

data class Comment(

    val commentId: String = "",

    val postId: String = "",

    val userId: String = "",

    val username: String = "",

    val content: String = "",

    val createdAt: Long = System.currentTimeMillis()

)