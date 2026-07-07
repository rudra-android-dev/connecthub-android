package com.example.connecthub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.connecthub.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = SettingsPreferences(application)

    private val _darkMode = MutableStateFlow(false)
    val darkMode = _darkMode.asStateFlow()

    init {
        // Restore saved preference on startup
        viewModelScope.launch {
            preferences.darkModeFlow.collect { saved ->
                _darkMode.value = saved
            }
        }
    }

    fun saveDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.saveDarkMode(enabled)
            _darkMode.value = enabled
        }
    }
}