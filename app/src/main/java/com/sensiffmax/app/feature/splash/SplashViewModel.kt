package com.sensiffmax.app.feature.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sensiffmax.app.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * SplashViewModel — Determines navigation target on app launch.
 *
 * Reads onboarding completion status from DataStore.
 * First launch: Splash → Onboarding
 * Returning user: Splash → Home
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    /**
     * Splash navigation decision.
     */
    sealed class SplashDestination {
        data object Loading : SplashDestination()
        data object Onboarding : SplashDestination()
        data object Home : SplashDestination()
    }

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            val completed = repository.isOnboardingCompleted.first()
            _destination.value = if (completed) {
                SplashDestination.Home
            } else {
                SplashDestination.Onboarding
            }
        }
    }
}
