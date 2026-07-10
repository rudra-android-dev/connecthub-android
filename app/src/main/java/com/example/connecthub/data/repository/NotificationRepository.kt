package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Notification
import com.example.connecthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Handles all notification-related Firestore operations.
 *
 * Notifications are created by FeedRepository (likes) and
 * CommentRepository (comments). This repository is NOT
 * responsible for deciding when to notify — it only creates,
 * reads, and marks notifications.
 */
class NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Creates a notification document in Firestore.
     *
     * The self-notification guard (receiverId == senderId) ensures
     * users are never notified about their own actions.
     */
    fun createNotification(
        receiverId: String,
        senderId: String,
        senderUsername: String,
        type: String,
        postId: String
    ) {
        // Never notify someone about their own action
        if (receiverId == senderId) return

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

    /**
     * Listens for all notifications for the current user in real time.
     * Ordered newest first.
     *
     * Requires a Firestore composite index on:
     * receiverId (ASC) + createdAt (DESC)
     */
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

    /**
     * Marks all unread notifications for a user as read in a single batch write.
     * Called when the user opens the notifications screen.
     */
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