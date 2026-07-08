package com.example.connecthub.data.model

data class Follow(

    val followId: String = "",

    val followerId: String = "",

    val followingId: String = "",

    val createdAt: Long = System.currentTimeMillis()

)