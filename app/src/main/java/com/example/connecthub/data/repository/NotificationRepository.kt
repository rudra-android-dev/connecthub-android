package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Notification
import com.example.connecthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Handles all notification-related Firestore operations.
 */
class NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Creates a notification document.
     * Self notification guard: users are never notified about their own actions.
     * Duplicate LIKE guard: if a LIKE notification already exists from this
     * sender on this post, a second one is not created (handles like→unlike→like).
     */
    fun createNotification(
        receiverId: String,
        senderId: String,
        senderUsername: String,
        type: String,
        postId: String
    ) {
        if (receiverId == senderId) return

        if (type == "LIKE") {
            // Check for existing LIKE notification before creating a duplicate
            firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("postId", postId)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("type", "LIKE")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) return@addOnSuccessListener
                    saveNotification(receiverId, senderId, senderUsername, type, postId)
                }
        } else {
            saveNotification(receiverId, senderId, senderUsername, type, postId)
        }
    }

    private fun saveNotification(
        receiverId: String,
        senderId: String,
        senderUsername: String,
        type: String,
        postId: String
    ) {
        val notificationId = firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .document()
            .id

        val notification = Notification(
            notificationId = notificationId,
            receiverId = receiverId,
            senderId = senderId,
            senderUsername = senderUsername,
            type = type,
            postId = postId
        )

        firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .set(notification)
    }

    fun listenForNotifications(
        receiverId: String,
        onResult: (List<Notification>) -> Unit,
        onError: (String?) -> Unit
    ): ListenerRegistration {
        return firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .whereEqualTo("receiverId", receiverId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull {
                    it.toObject(Notification::class.java)
                } ?: emptyList()

                onResult(notifications)
            }
    }

    fun markAllAsRead(receiverId: String) {
        firestore
            .collection(Constants.NOTIFICATIONS_COLLECTION)
            .whereEqualTo("receiverId", receiverId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit()
            }
    }
}