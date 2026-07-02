package com.example.connecthub.viewmodel

import androidx.lifecycle.ViewModel
import com.example.connecthub.data.repository.ProfileRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private var searchListener: ListenerRegistration? = null

    fun searchUsers(query: String) {
        searchListener?.remove()
        searchListener = null

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                users = emptyList(),
                isLoading = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isLoading = true
        )

        searchListener = repository.searchUsers(
            query = query,
            onResult = { users ->
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false
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

    override fun onCleared() {
        super.onCleared()
        searchListener?.remove()
        searchListener = null
    }
}