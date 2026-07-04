package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Notification
import com.example.connecthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()

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