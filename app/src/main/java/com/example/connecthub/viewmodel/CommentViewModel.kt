package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.CommentRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommentViewModel : ViewModel() {

    private val repository = CommentRepository()

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()

    private var commentsListener: ListenerRegistration? = null

    fun addComment(
        postId: String,
        content: String,
        onSuccess: () -> Unit = {}
    ) {
        val text = content.trim()
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Comment cannot be empty.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        repository.addComment(postId, text) { success, errorMessage ->
            if (success) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage ?: "Failed to post comment."
                )
            }
        }
    }

    fun deleteComment(commentId: String, postId: String) {
        repository.deleteComment(commentId, postId) { success, errorMessage ->
            if (!success) {
                _uiState.value = _uiState.value.copy(
                    error = errorMessage ?: "Failed to delete comment."
                )
            }
        }
    }

    fun startListening(postId: String) {
        if (commentsListener != null) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        commentsListener = repository.listenForComments(
            postId = postId,
            onCommentsChanged = { updatedComments ->
                _uiState.value = _uiState.value.copy(
                    comments = updatedComments,
                    isLoading = false,
                    error = null
                )
            },
            onError = { errorMessage ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage ?: "Failed to load comments."
                )
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        commentsListener?.remove()
        commentsListener = null
    }
}