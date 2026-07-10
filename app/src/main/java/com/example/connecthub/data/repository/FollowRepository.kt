package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Follow
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Handles the follow/unfollow relationship between users.
 *
 * Each follow is stored as its own document in the follows collection.
 * On follow/unfollow, followerCount and followingCount are updated
 * atomically on both user documents using FieldValue.increment(),
 * which avoids race conditions.
 */
class FollowRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Creates a follow document and increments counts on both users.
     * Prevents self-following before touching Firestore.
     */
    fun followUser(
        followingUid: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val followerUid = auth.currentUser?.uid
        if (followerUid == null) {
            onResult(false, "User not logged in.")
            return
        }

        // Users cannot follow themselves
        if (followerUid == followingUid) {
            onResult(false, "You can't follow yourself.")
            return
        }

        val followId = firestore
            .collection(Constants.FOLLOWS_COLLECTION)
            .document()
            .id

        val follow = Follow(
            followId = followId,
            followerId = followerUid,
            followingId = followingUid
        )

        firestore
            .collection(Constants.FOLLOWS_COLLECTION)
            .document(followId)
            .set(follow)
            .addOnSuccessListener {
                // Atomic increments no read-modify-write needed
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(followingUid)
                    .update("followersCount", FieldValue.increment(1))

                firestore.collection(Constants.USERS_COLLECTION)
                    .document(followerUid)
                    .update("followingCount", FieldValue.increment(1))

                onResult(true, null)
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    /**
     * Finds and deletes the follow document, then decrements counts on both users.
     */
    fun unfollowUser(
        followingUid: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val followerUid = auth.currentUser?.uid
        if (followerUid == null) {
            onResult(false, "User not logged in.")
            return
        }

        firestore
            .collection(Constants.FOLLOWS_COLLECTION)
            .whereEqualTo("followerId", followerUid)
            .whereEqualTo("followingId", followingUid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(false, "Follow relationship not found.")
                    return@addOnSuccessListener
                }

                val docId = snapshot.documents[0].id

                firestore
                    .collection(Constants.FOLLOWS_COLLECTION)
                    .document(docId)
                    .delete()
                    .addOnSuccessListener {
                        firestore.collection(Constants.USERS_COLLECTION)
                            .document(followingUid)
                            .update("followersCount", FieldValue.increment(-1))

                        firestore.collection(Constants.USERS_COLLECTION)
                            .document(followerUid)
                            .update("followingCount", FieldValue.increment(-1))

                        onResult(true, null)
                    }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    /**
     * Checks whether the current user is already following the given user.
     * Used to set the initial state of the Follow/Following button.
     */
    fun isFollowing(
        followingUid: String,
        onResult: (Boolean) -> Unit
    ) {
        val followerUid = auth.currentUser?.uid
        if (followerUid == null) {
            onResult(false)
            return
        }

        firestore
            .collection(Constants.FOLLOWS_COLLECTION)
            .whereEqualTo("followerId", followerUid)
            .whereEqualTo("followingId", followingUid)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(!snapshot.isEmpty)
            }
            .addOnFailureListener { onResult(false) }
    }
}