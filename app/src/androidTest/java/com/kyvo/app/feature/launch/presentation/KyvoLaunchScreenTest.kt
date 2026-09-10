package com.kyvo.app.feature.launch.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.kyvo.app.core.designsystem.KyvoTheme
import org.junit.Rule
import org.junit.Test

class KyvoLaunchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun launchCoverRendersItsVisualAsset() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                KyvoLaunchScreen()
            }
        }

        composeRule.onNodeWithTag(KYVO_LAUNCH_COVER_TAG).assertIsDisplayed()
    }
}
