package com.example.connecthub.utils

/**
 * Firestore collection name constants.
 *
 * Centralising these prevents typos when referencing collections
 * across multiple repositories.
 *
 * Current Firestore structure:
 *   users / posts / comments / notifications /
 *   bookmarks / reports / follows
 */
object Constants {

    const val USERS_COLLECTION = "users"

    const val POSTS_COLLECTION = "posts"

    const val COMMENTS_COLLECTION = "comments"

    const val NOTIFICATIONS_COLLECTION = "notifications"

    const val BOOKMARKS_COLLECTION = "bookmarks"

    const val REPORTS_COLLECTION = "reports"

    const val FOLLOWS_COLLECTION = "follows"

}