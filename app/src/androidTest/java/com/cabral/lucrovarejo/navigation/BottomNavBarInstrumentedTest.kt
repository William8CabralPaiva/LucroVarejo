package com.cabral.lucrovarejo.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomNavBarInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysSelectionAndDispatchesClickedDestination() {
        var selectedRoute: String? = null
        composeRule.setContent {
            LucroVarejoTheme {
                BottomNavBar(
                    currentRoute = LoggedRoutes.HOME,
                    onNavigate = { selectedRoute = it }
                )
            }
        }

        composeRule.onNodeWithText(label(R.string.bottom_nav_home)).assertIsSelected()
        composeRule.onNodeWithText(label(R.string.bottom_nav_sales)).assertIsDisplayed()
        composeRule.onNodeWithText(label(R.string.bottom_nav_sales)).performClick()

        composeRule.runOnIdle {
            assertEquals(LoggedRoutes.SALES, selectedRoute)
        }
    }

    private fun label(resourceId: Int) = composeRule.activity.getString(resourceId)
}
