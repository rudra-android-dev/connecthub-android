package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeedViewModel : ViewModel() {

    private val repository = FeedRepository()

    private val _uiState = MutableStateFlow(FeedUiState())

    val uiState = _uiState.asStateFlow()

    fun createPost(content: String) {

        val text = content.trim()

        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Post cannot be empty.",
                success = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            success = false
        )

        repository.createPost(text) { success, message ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    success = true,
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = message,
                    isLoading = false,
                    success = false
                )
            }
        }
    }


    fun startListeningToPosts() {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        repository.listenForPosts(
            onPostsChanged = { posts ->
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isLoading = false,
                    error = null
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
}
