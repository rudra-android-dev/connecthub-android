package com.example.connecthub.utils

object TimeUtils {

    fun formatRelativeTime(time: Long): String {
        val diff = System.currentTimeMillis() - time
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days == 1L -> "Yesterday"
            else -> "$days days ago"
        }
    }
}