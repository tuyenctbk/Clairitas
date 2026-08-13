package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.app.LocaleManager
import android.os.LocaleList
import java.util.Locale

object LanguageHelper {
    fun setAppLanguage(context: Context, languageCode: String) {
        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
        } else {
            Locale.forLanguageTag(languageCode)
        }
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales = LocaleList(locale)
        } else {
            val resources = context.resources
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
}
