package com.example.ui.theme

/**
 * Supported UI theme modes for Sift News application.
 */
enum class ThemeMode(val displayName: String, val description: String) {
    SYSTEM("System (Automatic)", "Follow device system theme settings"),
    LIGHT("Light Theme", "Clean light tones with high contrast for daytime"),
    DARK("Dark Theme", "Eye-friendly dark tones, saves battery in dark environments")
}
