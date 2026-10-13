package com.cabral.lucrovarejo.ui.screens.product

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.navigation.AppNavGraph
import com.cabral.lucrovarejo.navigation.LoggedRoutes
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun routeShowsItsHeaderAndBackNavigatesToHome() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppNavGraph(startDestination = LoggedRoutes.HOME)
            }
        }

        composeRule.onNodeWithText("Testando navegação").performClick()
        composeRule.onNodeWithText("Novo Produto").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Voltar").performClick()
        composeRule.onNodeWithText("Lucro Varejo - Home").assertIsDisplayed()
    }
}
