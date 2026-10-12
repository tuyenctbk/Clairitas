package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sift", appName)
  }

  @Test
  fun `verify category strings localization`() {
    val enTech = com.example.util.AppStrings.getCategoryTitle("Tech", "en")
    assertEquals("Technology", enTech)

    val viTech = com.example.util.AppStrings.getCategoryTitle("Tech", "vi")
    assertEquals("Công nghệ", viTech)

    val esTech = com.example.util.AppStrings.getCategoryTitle("Technology", "es")
    assertEquals("Tecnología", esTech)

    val deMarkets = com.example.util.AppStrings.getCategoryTitle("Markets", "de")
    assertEquals("Märkte", deMarkets)

    val frAi = com.example.util.AppStrings.getCategoryTitle("AI", "fr")
    assertEquals("Intelligence Artificielle (IA)", frAi)
  }

  @Test
  fun `verify category normalization and strings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("All", context.getString(R.string.category_all))
    assertEquals("Technology", context.getString(R.string.category_tech))
    assertEquals("World", context.getString(R.string.category_global))

    assertEquals("Tech", com.example.util.AppStrings.normalizeCategory("Technology"))
    assertEquals("Tech", com.example.util.AppStrings.normalizeCategory("tech"))
    assertEquals("Tech", com.example.util.AppStrings.normalizeCategory("Công nghệ"))
    assertEquals("ALL", com.example.util.AppStrings.normalizeCategory("All"))
    assertEquals("ALL", com.example.util.AppStrings.normalizeCategory("All Topics"))
    assertEquals("ALL", com.example.util.AppStrings.normalizeCategory("Tất cả"))
    assertEquals("World", com.example.util.AppStrings.normalizeCategory("World"))
    assertEquals("World", com.example.util.AppStrings.normalizeCategory("global"))
    assertEquals("RealEstate", com.example.util.AppStrings.normalizeCategory("Real Estate"))
    assertEquals("AI", com.example.util.AppStrings.normalizeCategory("Artificial Intelligence"))
  }

  @Test
  fun `verify app versioning`() {
    assertEquals(7, BuildConfig.VERSION_CODE)
    assertEquals("1.6", BuildConfig.VERSION_NAME)
  }
}
