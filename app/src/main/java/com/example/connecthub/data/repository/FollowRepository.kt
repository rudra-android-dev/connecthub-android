package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Follow
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FollowRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun followUser(
        followingUid: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val followerUid = auth.currentUser?.uid
        if (followerUid == null) {
            onResult(false, "User not logged in.")
            return
        }

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