package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Handles all post-related Firestore operations.
 *
 * Responsibilities:
 * - Creating and deleting posts
 * - Listening to the real-time post feed
 * - Toggling likes via atomic Firestore transactions
 * - Triggering LIKE notifications
 * - Counting posts for the current user's profile
 */
class FeedRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()

    /**
     * Creates a new post document in Firestore.
     * Fetches the current user's profile first to attach
     * username and profileImageUrl to the post.
     */
    fun createPost(
        content: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            onResult(false, "User not logged in.")
            return
        }

        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user == null) {
                    onResult(false, "User data not found.")
                    return@addOnSuccessListener
                }

                val postId = firestore
                    .collection(Constants.POSTS_COLLECTION)
                    .document()
                    .id

                val post = Post(
                    postId = postId,
                    userId = firebaseUser.uid,
                    username = user.username,
                    content = content,
                    profileImageUrl = user.profileImageUrl
                )

                firestore
                    .collection(Constants.POSTS_COLLECTION)
                    .document(postId)
                    .set(post)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { exception -> onResult(false, exception.message) }
            }
            .addOnFailureListener { exception -> onResult(false, exception.message) }
    }

    /**
     * Attaches a real-time Firestore snapshot listener to the posts collection.
     * Returns the ListenerRegistration so it can be removed in ViewModel.onCleared()
     * to prevent memory leaks.
     *
     * Posts are ordered newest first.
     */
    fun listenForPosts(
        onPostsChanged: (List<Post>) -> Unit,
        onError: (String?) -> Unit
    ): ListenerRegistration {
        return firestore
            .collection(Constants.POSTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message)
                    return@addSnapshotListener
                }

                val posts = mutableListOf<Post>()
                snapshot?.documents?.forEach { document ->
                    document.toObject(Post::class.java)?.let { posts.add(it) }
                }
                onPostsChanged(posts)
            }
    }

    /**
     * Toggles a like on a post using a Firestore transaction.
     * Using a transaction prevents race conditions when multiple users
     * like the same post simultaneously.
     *
     * Only sends a LIKE notification when actually liking — not when un-liking.
     * Never sends a notification if the user likes their own post
     * (handled inside NotificationRepository).
     */
    fun toggleLike(
        post: Post,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User not logged in.")
            return
        }

        val postRef = firestore
            .collection(Constants.POSTS_COLLECTION)
            .document(post.postId)

        firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val currentUsername = userDoc.toObject(User::class.java)?.username ?: ""

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentPost = snapshot.toObject(Post::class.java)
                        ?: return@runTransaction

                    val likedUsers = currentPost.likedBy.toMutableList()

                    if (likedUsers.contains(currentUser.uid)) {
                        likedUsers.remove(currentUser.uid)
                    } else {
                        likedUsers.add(currentUser.uid)
                        notificationRepository.createNotification(
                            receiverId = currentPost.userId,
                            senderId = currentUser.uid,
                            senderUsername = currentUsername,
                            type = "LIKE",
                            postId = post.postId
                        )
                    }

                    transaction.update(
                        postRef,
                        mapOf(
                            "likedBy" to likedUsers,
                            "likeCount" to likedUsers.size
                        )
                    )
                }
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { exception -> onResult(false, exception.message) }
            }
            .addOnFailureListener { exception -> onResult(false, exception.message) }
    }

    fun deletePost(
        postId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firestore
            .collection(Constants.POSTS_COLLECTION)
            .document(postId)
            .delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { exception -> onResult(false, exception.message) }
    }

    /**
     * Returns the number of posts created by the current user.
     * Used to display the post count on the profile screen.
     * Firestore does the filtering server-side via whereEqualTo.
     */
    fun getUserPostCount(onResult: (Int) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(0)
            return
        }

        firestore
            .collection(Constants.POSTS_COLLECTION)
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents -> onResult(documents.size()) }
            .addOnFailureListener { onResult(0) }
    }
}