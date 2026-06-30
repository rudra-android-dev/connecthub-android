package com.example.connecthub.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.connecthub.data.model.User
import com.example.connecthub.data.repository.FeedRepository
import com.example.connecthub.data.repository.ImageRepository
import com.example.connecthub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val postCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = FeedRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun loadProfileData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = ProfileUiState(error = "User not logged in.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val userData = document.toObject(User::class.java)

                repository.getUserPostCount { count ->
                    _uiState.value = ProfileUiState(
                        user = userData,
                        postCount = count,
                        isLoading = false,
                        error = if (userData == null) "User data not found." else null
                    )
                }
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
    }

    fun updateProfile(
        newUsername: String,
        newBio: String,
        selectedImageUri: Uri?,
        context: Context,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not logged in.")
            onComplete(false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            var finalImageUrl = _uiState.value.user?.profileImageUrl ?: ""

            if (selectedImageUri != null) {
                val imageRepo = ImageRepository(context)
                val uploadedUrl = imageRepo.uploadProfileImage(selectedImageUri)
                if (uploadedUrl != null) {
                    finalImageUrl = uploadedUrl
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to upload image.")
                    onComplete(false)
                    return@launch
                }
            }

            val updates = mapOf(
                "username" to newUsername,
                "bio" to newBio,
                "profileImageUrl" to finalImageUrl
            )

            firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUser.uid)
                .update(updates)
                .addOnSuccessListener {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadProfileData()
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = exception.message)
                    onComplete(false)
                }
        }
    }
}