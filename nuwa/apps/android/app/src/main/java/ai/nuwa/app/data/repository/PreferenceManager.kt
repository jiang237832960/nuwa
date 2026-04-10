package ai.nuwa.app.data.repository

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "nuwa_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_FIRST_LAUNCH_TIME = "first_launch_time"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
    
    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    
    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
    
    var firstLaunchTime: Long
        get() = prefs.getLong(KEY_FIRST_LAUNCH_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_FIRST_LAUNCH_TIME, value).apply()
    
    fun isFirstLaunch(): Boolean {
        if (firstLaunchTime == 0L) {
            firstLaunchTime = System.currentTimeMillis()
            return true
        }
        return false
    }
}
