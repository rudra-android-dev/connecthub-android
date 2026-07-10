package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Comment
import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Handles all comment-related Firestore operations.
 *
 * Responsibilities:
 * - Adding comments to posts
 * - Incrementing the post's commentCount atomically
 * - Triggering COMMENT notifications to the post owner
 * - Listening to real-time comment updates for a given post
 */
class CommentRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()

    /**
     * Adds a new comment to a post.
     *
     * Flow:
     * 1. Fetch current user profile (for username and avatar)
     * 2. Save comment document
     * 3. Increment post's commentCount with FieldValue.increment (atomic)
     * 4. Notify the post owner via NotificationRepository
     */
    fun addComment(
        postId: String,
        content: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            onResult(false, "User not logged in.")
            return
        }

        firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUserId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                val username = user?.username ?: "Anonymous"

                val commentRef = firestore
                    .collection(Constants.COMMENTS_COLLECTION)
                    .document()
                val commentId = commentRef.id

                val newComment = Comment(
                    commentId = commentId,
                    postId = postId,
                    userId = currentUserId,
                    username = username,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    profileImageUrl = user?.profileImageUrl ?: ""
                )

                commentRef.set(newComment)
                    .addOnSuccessListener {
                        // Atomic increment no race condition risk
                        firestore.collection(Constants.POSTS_COLLECTION)
                            .document(postId)
                            .update("commentCount", FieldValue.increment(1))

                        firestore.collection(Constants.POSTS_COLLECTION)
                            .document(postId)
                            .get()
                            .addOnSuccessListener { postDoc ->
                                val post = postDoc.toObject(Post::class.java)
                                if (post != null) {
                                    notificationRepository.createNotification(
                                        receiverId = post.userId,
                                        senderId = currentUserId,
                                        senderUsername = username,
                                        type = "COMMENT",
                                        postId = postId
                                    )
                                }
                            }

                        onResult(true, null)
                    }
                    .addOnFailureListener { exception ->
                        onResult(false, exception.localizedMessage)
                    }
            }
            .addOnFailureListener { exception ->
                onResult(false, "Failed to fetch user profile: ${exception.localizedMessage}")
            }
    }

    /**
     * Attaches a real-time listener to comments for a specific post.
     * Returns ListenerRegistration so it can be removed in ViewModel.onCleared()
     * to prevent memory leaks.
     *
     * Comments are ordered oldest first (chronological conversation order).
     */
    fun listenForComments(
        postId: String,
        onCommentsChanged: (List<Comment>) -> Unit,
        onError: (String?) -> Unit
    ): ListenerRegistration {
        return firestore.collection(Constants.COMMENTS_COLLECTION)
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val commentsList = snapshot.documents.mapNotNull { document ->
                        document.toObject(Comment::class.java)
                    }
                    onCommentsChanged(commentsList)
                }
            }
    }
}