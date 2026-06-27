package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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
                    content = content
                )

                firestore
                    .collection(Constants.POSTS_COLLECTION)
                    .document(postId)
                    .set(post)
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { exception ->
                        onResult(false, exception.message)
                    }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }

    fun listenForPosts(
        onPostsChanged: (List<Post>) -> Unit,
        onError: (String?) -> Unit
    ) {
        firestore
            .collection(Constants.POSTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error.message)
                    return@addSnapshotListener
                }

                val posts = mutableListOf<Post>()

                snapshot?.documents?.forEach { document ->
                    document.toObject(Post::class.java)?.let {
                        posts.add(it)
                    }
                }

                onPostsChanged(posts)
            }
    }


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


        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(postRef)
            val currentPost = snapshot.toObject(Post::class.java)
                ?: return@runTransaction


            val likedUsers = currentPost.likedBy.toMutableList()


            if (likedUsers.contains(currentUser.uid)) {
                likedUsers.remove(currentUser.uid)
            } else {
                likedUsers.add(currentUser.uid)
            }


            transaction.update(
                postRef,
                mapOf(
                    "likedBy" to likedUsers,
                    "likeCount" to likedUsers.size
                )
            )
        }

            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message)
            }
    }
}
