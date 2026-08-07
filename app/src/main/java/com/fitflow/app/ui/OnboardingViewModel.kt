package com.fitflow.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.util.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _isOnboardingComplete = MutableStateFlow<Boolean?>(null)
    val isOnboardingComplete: StateFlow<Boolean?> = _isOnboardingComplete.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.isOnboardingComplete.collect {
                _isOnboardingComplete.value = it
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            userPreferences.getOrCreateGuestId()
            userPreferences.setDisplayName("Guest")
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.completeOnboarding()
        }
    }
}
