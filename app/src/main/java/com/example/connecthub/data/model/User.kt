package com.example.connecthub.data.model

data class User(

    val uid: String = "",

    val username: String = "",

    val usernameLower: String = "",

    val email: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    val bio: String = "",

    val profileImageUrl: String = "",

    val blockedUsers: List<String> = emptyList()

)