package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.app.LocaleManager
import android.os.LocaleList
import java.util.Locale

object LanguageHelper {
    fun getLocale(languageCode: String): Locale {
        return if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
        } else {
            Locale.forLanguageTag(languageCode)
        }
    }

    fun wrapContext(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun setAppLanguage(context: Context, languageCode: String) {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            try {
                localeManager?.applicationLocales = LocaleList(locale)
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
    }
}
