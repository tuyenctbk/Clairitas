package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.PermissionHelper
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PermissionHelperTest {

    @Test
    fun testHasNotificationPermissionCheckDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = PermissionHelper.hasNotificationPermission(context)
        // In Robolectric test context notifications enabled flag can be queried safely without crashing
        assertNotNull(result)
    }

    @Test
    fun testNotificationStringsExist() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val title = context.getString(R.string.permission_guide_title)
        val subtitle = context.getString(R.string.permission_guide_subtitle)
        val button = context.getString(R.string.permission_approve_button)

        assertNotNull(title)
        assertNotNull(subtitle)
        assertNotNull(button)
    }
}
