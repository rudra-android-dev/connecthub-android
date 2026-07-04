package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    private var notificationsListener: ListenerRegistration? = null

    fun loadNotifications() {
        if (notificationsListener != null) return

        val currentUserId = auth.currentUser?.uid ?: return

        _uiState.value = _uiState.value.copy(isLoading = true)

        notificationsListener = repository.listenForNotifications(
            receiverId = currentUserId,
            onResult = { notifications ->
                _uiState.value = _uiState.value.copy(
                    notifications = notifications,
                    isLoading = false
                )
            },
            onError = { message ->
                _uiState.value = _uiState.value.copy(
                    error = message,
                    isLoading = false
                )
            }
        )
    }

    fun markAllAsRead() {
        val currentUserId = auth.currentUser?.uid ?: return
        repository.markAllAsRead(currentUserId)
    }

    override fun onCleared() {
        super.onCleared()
        notificationsListener?.remove()
        notificationsListener = null
    }
}