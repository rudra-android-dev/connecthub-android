package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommentViewModel : ViewModel() {

    private val repository = CommentRepository()

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()


    fun addComment(postId: String, content: String) {
        val text = content.trim()
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Comment cannot be empty."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        repository.addComment(postId, text) { success, errorMessage ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage ?: "Failed to post comment."
                )
            }
        }
    }
}
