package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.model.User
import com.example.connecthub.data.repository.BlockRepository
import com.example.connecthub.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockedUsersViewModel : ViewModel() {

    private val blockRepository = BlockRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(BlockedUsersUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBlockedUsers() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        blockRepository.getBlockedUsers { blockedIds ->
            if (blockedIds.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    blockedUsers = emptyList(),
                    isLoading = false
                )
                return@getBlockedUsers
            }

            val users = mutableListOf<User>()
            var fetched = 0

            blockedIds.forEach { uid ->
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        doc.toObject(User::class.java)?.let { users.add(it) }
                        fetched++
                        if (fetched == blockedIds.size) {
                            _uiState.value = _uiState.value.copy(
                                blockedUsers = users.sortedBy { it.username },
                                isLoading = false
                            )
                        }
                    }
                    .addOnFailureListener {
                        fetched++
                        if (fetched == blockedIds.size) {
                            _uiState.value = _uiState.value.copy(
                                blockedUsers = users.sortedBy { it.username },
                                isLoading = false
                            )
                        }
                    }
            }
        }
    }

    fun unblockUser(uid: String) {
        blockRepository.unblockUser(uid) { success, _ ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    blockedUsers = _uiState.value.blockedUsers.filter { it.uid != uid }
                )
            }
        }
    }
}