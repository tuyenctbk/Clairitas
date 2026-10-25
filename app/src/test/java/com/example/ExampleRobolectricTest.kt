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
    assertEquals(8, BuildConfig.VERSION_CODE)
    assertEquals("1.7", BuildConfig.VERSION_NAME)
  }

  @Test
  fun `verify wikipedia entity extraction from news headlines`() {
    val entity1 = com.example.data.remote.WikipediaContextService.extractSearchEntity("Breaking: James Webb Space Telescope spots oldest galaxy")
    assertEquals("James Webb Space Telescope", entity1)

    val entity2 = com.example.data.remote.WikipediaContextService.extractSearchEntity("Exclusive: Nvidia announces next-generation Blackwell architecture")
    assertEquals("Nvidia announces next-generation Blackwell", entity2)

    val entity3 = com.example.data.remote.WikipediaContextService.extractSearchEntity("Quantum supremacy achieved in novel neutral-atom simulator")
    assertEquals("Quantum supremacy achieved novel", entity3)
  }

  @Test
  fun `verify context card localization strings`() {
    val enTitle = com.example.util.AppStrings.getContextCardTitle("en")
    assertEquals("Encyclopedic Context", enTitle)

    val viTitle = com.example.util.AppStrings.getContextCardTitle("vi")
    assertEquals("Bối cảnh Bách khoa toàn thư", viTitle)

    val esButton = com.example.util.AppStrings.getContextCardButton("es")
    assertEquals("Contexto", esButton)

    val jaButton = com.example.util.AppStrings.getContextCardButton("ja")
    assertEquals("コンテキスト", jaButton)
  }

  @Test
  fun `verify suggestion policy anti-spam rules`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("test_sift_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()

    assertEquals(false, com.example.util.SuggestionPolicy.canShowRatingPrompt(prefs, hasShownThisSession = false))

    prefs.edit()
      .putInt(com.example.util.SuggestionPolicy.PREF_APP_LAUNCH_COUNT, 3)
      .putInt(com.example.util.SuggestionPolicy.PREF_TOTAL_READ_COUNT, 5)
      .apply()

    assertEquals(true, com.example.util.SuggestionPolicy.canShowRatingPrompt(prefs, hasShownThisSession = false))
    assertEquals(false, com.example.util.SuggestionPolicy.canShowRatingPrompt(prefs, hasShownThisSession = true))

    com.example.util.SuggestionPolicy.recordDismissal(prefs, com.example.util.SuggestionType.RATING)
    assertEquals(false, com.example.util.SuggestionPolicy.canShowRatingPrompt(prefs, hasShownThisSession = false))

    com.example.util.SuggestionPolicy.recordDismissal(prefs, com.example.util.SuggestionType.RATING)
    assertEquals(true, prefs.getBoolean(com.example.util.SuggestionPolicy.PREF_RATING_COMPLETED, false))
  }
}
