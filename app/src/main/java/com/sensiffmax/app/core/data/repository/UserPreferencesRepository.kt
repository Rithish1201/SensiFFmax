package com.sensiffmax.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sensiffmax.app.core.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * UserPreferencesRepository — Persistence layer for user profile data.
 *
 * Uses Jetpack DataStore Preferences to persist onboarding selections.
 * All reads are exposed as Flows for reactive UI updates.
 * All writes are suspend functions called from ViewModels.
 *
 * No permissions required.
 */

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sensiffmax_user_preferences"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val GAME = stringPreferencesKey("target_game")
        val PLAY_STYLE = stringPreferencesKey("play_style")
        val FINGER_SETUP = stringPreferencesKey("finger_setup")
        val SCREEN_WIDTH_PX = intPreferencesKey("screen_width_px")
        val SCREEN_HEIGHT_PX = intPreferencesKey("screen_height_px")
        val SCREEN_DENSITY_DPI = intPreferencesKey("screen_density_dpi")
        val REFRESH_RATE = floatPreferencesKey("refresh_rate")
    }

    /**
     * Whether the user has completed onboarding.
     * Used by SplashScreen to decide navigation target.
     */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    /**
     * Full user profile as a reactive Flow.
     */
    val userProfile: Flow<UserProfile> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            UserProfile(
                game = prefs[Keys.GAME]?.let { name ->
                    TargetGame.entries.find { it.name == name }
                } ?: TargetGame.FREE_FIRE,
                playStyle = prefs[Keys.PLAY_STYLE]?.let { name ->
                    PlayStyle.entries.find { it.name == name }
                } ?: PlayStyle.BALANCED,
                fingerSetup = prefs[Keys.FINGER_SETUP]?.let { name ->
                    FingerSetup.entries.find { it.name == name }
                } ?: FingerSetup.TWO_FINGER,
                deviceProfile = DeviceProfile(
                    screenWidthPx = prefs[Keys.SCREEN_WIDTH_PX] ?: 0,
                    screenHeightPx = prefs[Keys.SCREEN_HEIGHT_PX] ?: 0,
                    screenDensityDpi = prefs[Keys.SCREEN_DENSITY_DPI] ?: 0,
                    refreshRate = prefs[Keys.REFRESH_RATE] ?: 60f
                ),
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
            )
        }

    /**
     * Save complete onboarding profile. Called once at end of onboarding flow.
     */
    suspend fun saveOnboardingProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
            prefs[Keys.GAME] = profile.game.name
            prefs[Keys.PLAY_STYLE] = profile.playStyle.name
            prefs[Keys.FINGER_SETUP] = profile.fingerSetup.name
            prefs[Keys.SCREEN_WIDTH_PX] = profile.deviceProfile.screenWidthPx
            prefs[Keys.SCREEN_HEIGHT_PX] = profile.deviceProfile.screenHeightPx
            prefs[Keys.SCREEN_DENSITY_DPI] = profile.deviceProfile.screenDensityDpi
            prefs[Keys.REFRESH_RATE] = profile.deviceProfile.refreshRate
        }
    }

    /**
     * Reset onboarding state. Useful for testing or re-onboarding.
     */
    suspend fun clearOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = false
        }
    }
}
