package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages authentication state for Login and Register screens.
 *
 * Delegates all Firebase Auth operations to AuthRepository.
 * Exposes AuthUiState via StateFlow for the UI to observe.
 *
 * Local validation (empty fields, password length) is handled
 * here before any network call is made.
 */
class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow(AuthUiState())
    val authState = _authState.asStateFlow()

    fun register(username: String, email: String, password: String) {
        if (username.isBlank()) {
            _authState.value = AuthUiState(error = "Username cannot be empty")
            return
        }

        if (email.isBlank()) {
            _authState.value = AuthUiState(error = "Email cannot be empty")
            return
        }

        if (password.isBlank()) {
            _authState.value = AuthUiState(error = "Password cannot be empty")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthUiState(error = "Password must be at least 6 characters")
            return
        }

        _authState.value = AuthUiState(isLoading = true)

        repository.registerUser(username, email, password) { success, message ->
            _authState.value = if (success) {
                AuthUiState(success = true)
            } else {
                AuthUiState(error = message)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _authState.value = AuthUiState(error = "Email cannot be empty")
            return
        }

        if (password.isBlank()) {
            _authState.value = AuthUiState(error = "Password cannot be empty")
            return
        }

        _authState.value = AuthUiState(isLoading = true)

        repository.loginUser(email, password) { success, message ->
            _authState.value = if (success) {
                AuthUiState(success = true)
            } else {
                AuthUiState(error = message)
            }
        }
    }

    fun logout() {
        repository.logout()
    }

    fun isUserLoggedIn(): Boolean {
        return repository.currentUser() != null
    }

    fun resetState() {
        _authState.value = AuthUiState()
    }
}