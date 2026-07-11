package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.FollowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FollowViewModel : ViewModel() {

    private val repository = FollowRepository()

    private val _uiState = MutableStateFlow(FollowUiState())
    val uiState = _uiState.asStateFlow()

    private var targetUid: String = ""

    fun init(uid: String, followersCount: Int, followingCount: Int) {
        targetUid = uid
        _uiState.value = _uiState.value.copy(
            followers = followersCount,
            following = followingCount
        )
        checkFollowing()
    }

    // Private, only called internally from init()
    private fun checkFollowing() {
        repository.isFollowing(targetUid) { following ->
            _uiState.value = _uiState.value.copy(isFollowing = following)
        }
    }

    fun followUser() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        repository.followUser(targetUid) { success, message ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isFollowing = true,
                    followers = _uiState.value.followers + 1,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = message,
                    isLoading = false
                )
            }
        }
    }

    fun unfollowUser() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        repository.unfollowUser(targetUid) { success, message ->
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isFollowing = false,
                    followers = (_uiState.value.followers - 1).coerceAtLeast(0),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = message,
                    isLoading = false
                )
            }
        }
    }
}