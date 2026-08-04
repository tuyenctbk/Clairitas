package com.example.ui.theme

/**
 * Supported UI theme modes for Claritas News application.
 */
enum class ThemeMode(val displayName: String, val description: String) {
    SYSTEM("Hệ thống (Tự động)", "Tự động đổi giao diện theo cài đặt thiết bị"),
    LIGHT("Giao diện Sáng (Light)", "Tone sáng tinh khôi, tương phản cao ban ngày"),
    DARK("Giao diện Tối (Dark)", "Tone màu tối dịu mắt, tiết kiệm pin & bảo vệ mắt")
}
