package com.example.connecthub.data.model

data class Report(

    val reportId: String = "",

    val postId: String = "",

    val reportedUserId: String = "",

    val reportedByUserId: String = "",

    val reason: String = "",

    val createdAt: Long = System.currentTimeMillis()

)