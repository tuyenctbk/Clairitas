package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.app.LocaleManager
import android.os.LocaleList
import java.util.Locale

object LanguageHelper {
    fun getLocale(languageCode: String): Locale {
        return try {
            if (languageCode.contains("-")) {
                val parts = languageCode.split("-")
                if (parts.size == 2 && parts[1].length == 2) {
                    Locale(parts[0], parts[1])
                } else {
                    Locale.forLanguageTag(languageCode)
                }
            } else {
                Locale.forLanguageTag(languageCode)
            }
        } catch (e: Exception) {
            Locale.ENGLISH
        }
    }

    fun wrapContext(context: Context, languageCode: String): Context {
        return try {
            val locale = getLocale(languageCode)
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } catch (e: Exception) {
            context
        }
    }

    fun setAppLanguage(context: Context, languageCode: String) {
        try {
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
                    val currentLocales = localeManager?.applicationLocales
                    if (currentLocales == null || currentLocales.isEmpty || currentLocales.get(0) != locale) {
                        localeManager?.applicationLocales = LocaleList(locale)
                    }
                } catch (e: Exception) {
                    // Ignore fallback
                }
            }
        } catch (e: Exception) {
            // Ignore fallback
        }
    }
}
