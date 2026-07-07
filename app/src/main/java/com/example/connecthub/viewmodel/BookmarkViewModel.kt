package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookmarkViewModel : ViewModel() {

    private val repository = BookmarkRepository()

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBookmarks() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        repository.getBookmarks(
            onResult = { posts ->
                val postIds = posts.map { it.postId }.toSet()
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    bookmarkedPostIds = postIds,
                    isLoading = false
                )
            },
            onError = { message ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = message
                )
            }
        )
    }

    fun loadBookmarkedPostIds() {
        repository.getBookmarkedPostIds(
            onResult = { postIds ->
                _uiState.value = _uiState.value.copy(
                    bookmarkedPostIds = postIds.toSet()
                )
            },
            onError = { }
        )
    }

    fun addBookmark(postId: String) {
        repository.addBookmark(postId) { success, _ ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    bookmarkedPostIds = _uiState.value.bookmarkedPostIds + postId
                )
                loadBookmarks()
            }
        }
    }

    fun removeBookmark(postId: String) {
        repository.removeBookmark(postId) { success, _ ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    bookmarkedPostIds = _uiState.value.bookmarkedPostIds - postId,
                    posts = _uiState.value.posts.filter { it.postId != postId }
                )
            }
        }
    }

    fun toggleBookmark(postId: String) {
        if (_uiState.value.bookmarkedPostIds.contains(postId)) {
            removeBookmark(postId)
        } else {
            addBookmark(postId)
        }
    }
}