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
            _uiState.value = FeedUiState(
                error = "Post cannot be empty."
            )
            return
        }


        _uiState.value = FeedUiState(
            isLoading = true
        )


        repository.createPost(text) { success, message ->
            if (success) {
                _uiState.value = FeedUiState(success = true)
            } else {
                _uiState.value = FeedUiState(error = message)
            }
        }
    }
}
