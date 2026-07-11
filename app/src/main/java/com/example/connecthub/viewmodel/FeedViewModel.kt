package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.model.Post
import com.example.connecthub.data.repository.BlockRepository
import com.example.connecthub.data.repository.FeedRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeedViewModel : ViewModel() {

    private val repository = FeedRepository()
    private val blockRepository = BlockRepository()

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    private var postsListener: ListenerRegistration? = null

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

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    fun startListeningToPosts() {
        if (postsListener != null) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        blockRepository.getBlockedUsers { blockedIds ->
            _uiState.value = _uiState.value.copy(
                blockedUserIds = blockedIds.toSet()
            )

            postsListener = repository.listenForPosts(
                onPostsChanged = { posts ->
                    val filtered = posts.filter { post ->
                        !_uiState.value.blockedUserIds.contains(post.userId)
                    }
                    _uiState.value = _uiState.value.copy(
                        posts = filtered,
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

    /**
     * Called after the user blocks someone from UserProfileScreen.
     * Refreshes the blocked list and re-filters the current posts immediately
     * so blocked posts disappear without requiring an app restart.
     */
    fun refreshBlockedUsers() {
        blockRepository.getBlockedUsers { blockedIds ->
            val newBlockedSet = blockedIds.toSet()
            val filtered = _uiState.value.posts.filter { post ->
                !newBlockedSet.contains(post.userId)
            }
            _uiState.value = _uiState.value.copy(
                blockedUserIds = newBlockedSet,
                posts = filtered
            )
        }
    }

    fun toggleLike(post: Post) {
        repository.toggleLike(post) { success, errorMessage ->
            if (!success) {
                _uiState.value = _uiState.value.copy(error = errorMessage)
            }
        }
    }

    fun deletePost(postId: String) {
        repository.deletePost(postId) { success, errorMessage ->
            if (!success) {
                _uiState.value = _uiState.value.copy(error = errorMessage)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        postsListener?.remove()
        postsListener = null
    }
}