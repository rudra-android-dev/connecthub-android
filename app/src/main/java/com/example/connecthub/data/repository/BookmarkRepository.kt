package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Bookmark
import com.example.connecthub.data.model.Post
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookmarkRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun addBookmark(
        postId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User not logged in.")
            return
        }

        val bookmarkId = firestore
            .collection(Constants.BOOKMARKS_COLLECTION)
            .document()
            .id

        val bookmark = Bookmark(
            bookmarkId = bookmarkId,
            userId = currentUser.uid,
            postId = postId
        )

        firestore
            .collection(Constants.BOOKMARKS_COLLECTION)
            .document(bookmarkId)
            .set(bookmark)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun removeBookmark(
        postId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User not logged in.")
            return
        }

        firestore
            .collection(Constants.BOOKMARKS_COLLECTION)
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(false, "Bookmark not found.")
                    return@addOnSuccessListener
                }
                val docId = snapshot.documents[0].id
                firestore
                    .collection(Constants.BOOKMARKS_COLLECTION)
                    .document(docId)
                    .delete()
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun getBookmarkedPostIds(
        onResult: (List<String>) -> Unit,
        onError: (String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User not logged in.")
            return
        }

        firestore
            .collection(Constants.BOOKMARKS_COLLECTION)
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val postIds = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Bookmark::class.java)?.postId
                }
                onResult(postIds)
            }
            .addOnFailureListener { onError(it.message) }
    }

    fun getBookmarks(
        onResult: (List<Post>) -> Unit,
        onError: (String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User not logged in.")
            return
        }

        firestore
            .collection(Constants.BOOKMARKS_COLLECTION)
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val postIds = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Bookmark::class.java)?.postId
                }

                if (postIds.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val posts = mutableListOf<Post>()
                var fetched = 0

                postIds.forEach { postId ->
                    firestore
                        .collection(Constants.POSTS_COLLECTION)
                        .document(postId)
                        .get()
                        .addOnSuccessListener { postDoc ->
                            postDoc.toObject(Post::class.java)?.let { posts.add(it) }
                            fetched++
                            if (fetched == postIds.size) {
                                onResult(posts.sortedByDescending { it.createdAt })
                            }
                        }
                        .addOnFailureListener {
                            fetched++
                            if (fetched == postIds.size) {
                                onResult(posts.sortedByDescending { it.createdAt })
                            }
                        }
                }
            }
            .addOnFailureListener { onError(it.message) }
    }

    fun isBookmarked(
        postId: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onResult(false)

        firestore
            .collection(Constants.BOOKMARKS_COLLECTION)
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(!snapshot.isEmpty)
            }
            .addOnFailureListener { onResult(false) }
    }
}