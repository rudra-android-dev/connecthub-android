package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Handles user profile lookups and user search.
 *
 * Search uses a usernameLower field (stored on registration)
 * with a Firestore range query for prefix matching.
 * This makes search case-insensitive without loading all users.
 *
 * Requires a Firestore index on usernameLower (ASC).
 */
class ProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Performs a real-time prefix search on usernameLower.
     *
     * Example: query "ru" matches "rudra", "ruby", "russell"
     * The \uf8ff character is the highest Unicode code point,
     * so quereLower + "\uf8ff" acts as an upper bound for the prefix.
     *
     * Skips documents where usernameLower is blank (pre-migration accounts).
     */
    fun searchUsers(
        query: String,
        onResult: (List<User>) -> Unit,
        onError: (String?) -> Unit
    ): ListenerRegistration {
        val queryLower = query.lowercase()
        return firestore
            .collection(Constants.USERS_COLLECTION)
            .whereGreaterThanOrEqualTo("usernameLower", queryLower)
            .whereLessThanOrEqualTo("usernameLower", queryLower + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message)
                    return@addSnapshotListener
                }

                val users = mutableListOf<User>()
                snapshot?.documents?.forEach {
                    it.toObject(User::class.java)?.let { user ->
                        if (user.usernameLower.isNotBlank()) {
                            users.add(user)
                        }
                    }
                }
                onResult(users)
            }
    }

    fun getUserByUid(
        uid: String,
        onResult: (User?) -> Unit
    ) {
        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener {
                onResult(it.toObject(User::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    /**
     * Returns all posts by a given user, sorted client-side by createdAt DESC.
     * No orderBy() is used here to avoid requiring a composite Firestore index
     * on userId + createdAt.
     */
    fun getUserPosts(
        uid: String,
        onResult: (List<Post>) -> Unit
    ) {
        firestore
            .collection(Constants.POSTS_COLLECTION)
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents
                    .mapNotNull { it.toObject(Post::class.java) }
                    .sortedByDescending { it.createdAt }
                onResult(posts)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}