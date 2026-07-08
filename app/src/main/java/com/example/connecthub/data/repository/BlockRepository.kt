package com.example.connecthub.data.repository

import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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