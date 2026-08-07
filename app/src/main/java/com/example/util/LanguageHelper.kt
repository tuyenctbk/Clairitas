package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.app.LocaleManager
import android.os.LocaleList
import java.util.Locale

object LanguageHelper {
    fun setAppLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales = LocaleList(locale)
        } else {
            val resources = context.resources
            val configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
}
