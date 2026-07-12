package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.model.User
import com.example.connecthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FollowListViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Loads all followers of [uid] — users who follow that person.
     * Queries the follows collection for documents where followingId == uid,
     * then fetches each follower's User document.
     */
    fun loadFollowers(uid: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        firestore.collection(Constants.FOLLOWS_COLLECTION)
            .whereEqualTo("followingId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val followerIds = snapshot.documents.mapNotNull { it.getString("followerId") }
                if (followerIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(users = emptyList(), isLoading = false)
                    return@addOnSuccessListener
                }
                fetchUsers(followerIds)
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
    }

    /**
     * Loads all users that [uid] is following.
     * Queries the follows collection for documents where followerId == uid,
     * then fetches each followed user's User document.
     */
    fun loadFollowing(uid: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        firestore.collection(Constants.FOLLOWS_COLLECTION)
            .whereEqualTo("followerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val followingIds = snapshot.documents.mapNotNull { it.getString("followingId") }
                if (followingIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(users = emptyList(), isLoading = false)
                    return@addOnSuccessListener
                }
                fetchUsers(followingIds)
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
    }

    private fun fetchUsers(uids: List<String>) {
        val users = mutableListOf<User>()
        var fetched = 0

        uids.forEach { uid ->
            firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    doc.toObject(User::class.java)?.let { users.add(it) }
                    fetched++
                    if (fetched == uids.size) {
                        _uiState.value = _uiState.value.copy(
                            users = users.sortedBy { it.username },
                            isLoading = false
                        )
                    }
                }
                .addOnFailureListener {
                    fetched++
                    if (fetched == uids.size) {
                        _uiState.value = _uiState.value.copy(
                            users = users.sortedBy { it.username },
                            isLoading = false
                        )
                    }
                }
        }
    }
}