package com.example.connecthub.data.model

data class Notification(

    val notificationId: String = "",

    val receiverId: String = "",

    val senderId: String = "",

    val senderUsername: String = "",

    val type: String = "",

    val postId: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    val isRead: Boolean = false

)