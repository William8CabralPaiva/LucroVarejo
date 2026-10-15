package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BottomNavBarUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysNavigationDestinationsAndMarksCurrentRouteSelected() {
        composeRule.setContent {
            LucroVarejoTheme {
                BottomNavBar(
                    currentRoute = LoggedRoutes.SALES,
                    onNavigate = {}
                )
            }
        }

        composeRule.onNodeWithText("Início").assertIsDisplayed()
        composeRule.onNodeWithText("Vendas").assertIsSelected()
        composeRule.onNodeWithText("Resumo").assertIsDisplayed()
        composeRule.onNodeWithText("Configurações").assertIsDisplayed()
    }

    @Test
    fun clickingDestinationForwardsItsRoute() {
        var selectedRoute: String? = null
        composeRule.setContent {
            LucroVarejoTheme {
                BottomNavBar(
                    currentRoute = LoggedRoutes.HOME,
                    onNavigate = { selectedRoute = it }
                )
            }
        }

        composeRule.onNodeWithText("Resumo").performClick()

        composeRule.runOnIdle {
            assertEquals(LoggedRoutes.SUMMARY, selectedRoute)
        }
    }

    @Test
    fun hidesNavigationForRoutesOutsideBottomBar() {
        composeRule.setContent {
            LucroVarejoTheme {
                BottomNavBar(
                    currentRoute = LoggedRoutes.ADD_PURCHASE,
                    onNavigate = {}
                )
            }
        }

        composeRule.onAllNodesWithText("Início").assertCountEquals(0)
    }
}
