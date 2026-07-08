package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.BlockRepository
import com.example.connecthub.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()
    private val blockRepository = BlockRepository()

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun loadUserProfile(uid: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        repository.getUserByUid(uid) { user ->
            if (user == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not found."
                )
                return@getUserByUid
            }

            repository.getUserPosts(uid) { posts ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    posts = posts,
                    isLoading = false
                )
            }
        }
    }

    fun blockUser(uid: String) {
        blockRepository.blockUser(uid) { success, message ->
            _uiState.value = _uiState.value.copy(
                blockMessage = if (success) "User blocked." else message
            )
        }
    }
}