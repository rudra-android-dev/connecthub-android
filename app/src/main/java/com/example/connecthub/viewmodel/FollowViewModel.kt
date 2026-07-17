package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.FollowRepository
import com.example.connecthub.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FollowViewModel : ViewModel() {

    private val repository = FollowRepository()
    private val profileRepository = ProfileRepository()

    private val _uiState = MutableStateFlow(FollowUiState())
    val uiState = _uiState.asStateFlow()

    private var targetUid: String = ""

    fun init(uid: String) {
        if (targetUid == uid) return
        targetUid = uid
        refreshCountsFromFirestore()
        checkFollowing()
    }

    /**
     * Always fetches fresh counts from Firestore.
     * Called on init and after every follow/unfollow so the displayed
     * number always reflects the real Firestore value, not a local guess.
     */
    private fun refreshCountsFromFirestore() {
        profileRepository.getUserByUid(targetUid) { user ->
            _uiState.value = _uiState.value.copy(
                followers = user?.followersCount ?: 0,
                following = user?.followingCount ?: 0
            )
        }
    }

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
                refreshCountsFromFirestore()
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
                refreshCountsFromFirestore()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = message,
                    isLoading = false
                )
            }
        }
    }
}