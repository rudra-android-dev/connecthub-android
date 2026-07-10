package com.example.connecthub.data.repository

import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manages the blocked users list stored on the current user's Firestore document.
 *
 * Blocked UIDs are stored as an array field on the user document.
 * FieldValue.arrayUnion and arrayRemove are used to safely add/remove
 * values without downloading and re-uploading the entire array.
 *
 * Blocking only affects the blocking user's feed — the blocked user
 * is unaware and their content is still visible to everyone else.
 */
class BlockRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun blockUser(
        blockedUid: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            onResult(false, "User not logged in.")
            return
        }

        // arrayUnion ensures no duplicate entries even if called multiple times
        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(currentUid)
            .update("blockedUsers", FieldValue.arrayUnion(blockedUid))
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun unblockUser(
        blockedUid: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            onResult(false, "User not logged in.")
            return
        }

        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(currentUid)
            .update("blockedUsers", FieldValue.arrayRemove(blockedUid))
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    /**
     * Returns the list of UIDs blocked by the current user.
     * Called before the feed listener starts so blocked users'
     * posts can be filtered out client-side.
     */
    fun getBlockedUsers(
        onResult: (List<String>) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            onResult(emptyList())
            return
        }

        firestore
            .collection(Constants.USERS_COLLECTION)
            .document(currentUid)
            .get()
            .addOnSuccessListener { doc ->
                val blocked = doc.get("blockedUsers") as? List<*>
                val blockedIds = blocked?.filterIsInstance<String>() ?: emptyList()
                onResult(blockedIds)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}