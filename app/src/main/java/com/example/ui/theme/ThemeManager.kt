package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton / Manager handling theme mode selection and switching across the entire app.
 */
class ThemeManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sift_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private fun loadSavedThemeMode(): ThemeMode {
        val savedName = prefs.getString(PREF_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(savedName ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(PREF_THEME_MODE, mode.name).apply()
    }

    companion object {
        private const val PREF_THEME_MODE = "pref_theme_mode_key"
    }
}
