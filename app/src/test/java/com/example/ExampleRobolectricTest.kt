package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_displaysGreetingMicAndShortcuts_andNavigatesTabs() {
        // Verify Home Screen Greeting & Central Mic
        composeTestRule.onNodeWithText("नमस्ते भाई! मैं मुन्ना हूँ। बताओ, क्या करना है?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("central_mic_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("shortcut_card_youtube").assertIsDisplayed()
        composeTestRule.onNodeWithTag("shortcut_card_whatsapp").assertIsDisplayed()

        // Navigate to History Tab
        composeTestRule.onNodeWithTag("nav_tab_history").performClick()
        composeTestRule.onNodeWithTag("history_screen").assertIsDisplayed()

        // Navigate to Settings Tab
        composeTestRule.onNodeWithTag("nav_tab_settings").performClick()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    }
}
