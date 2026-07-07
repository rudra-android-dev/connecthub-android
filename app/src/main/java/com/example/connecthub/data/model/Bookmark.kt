package com.example.connecthub.data.model

data class Bookmark(

    val bookmarkId: String = "",

    val userId: String = "",

    val postId: String = "",

    val createdAt: Long = System.currentTimeMillis()

)