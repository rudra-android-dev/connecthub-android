package com.example.connecthub.data.repository

import com.example.connecthub.data.model.Post
import com.example.connecthub.data.model.Report
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun submitReport(
        post: Post,
        reason: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User not logged in.")
            return
        }

        // Duplicate check: has this user already reported this post?
        firestore
            .collection(Constants.REPORTS_COLLECTION)
            .whereEqualTo("postId", post.postId)
            .whereEqualTo("reportedByUserId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    onResult(false, "You have already reported this post.")
                    return@addOnSuccessListener
                }

                val reportId = firestore
                    .collection(Constants.REPORTS_COLLECTION)
                    .document()
                    .id

                val report = Report(
                    reportId = reportId,
                    postId = post.postId,
                    reportedUserId = post.userId,
                    reportedByUserId = currentUser.uid,
                    reason = reason
                )

                firestore
                    .collection(Constants.REPORTS_COLLECTION)
                    .document(reportId)
                    .set(report)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }
}