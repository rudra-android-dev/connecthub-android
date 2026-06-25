package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
}
