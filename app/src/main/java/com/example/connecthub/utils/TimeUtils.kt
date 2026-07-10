package com.example.connecthub.utils

/**
 * Utility for converting timestamps into user-friendly relative time strings.
 *
 * Examples:
 *   < 1 minute  → "Just now"
 *   5 minutes   → "5 min ago"
 *   3 hours     → "3 hr ago"
 *   1 day       → "Yesterday"
 *   5 days      → "5 days ago"
 *
 * Used in PostItem, CommentItem, and NotificationItem.
 */
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