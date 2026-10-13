package com.cabral.lucrovarejo.ui.screens.product

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.navigation.LoggedRoutes
import com.cabral.lucrovarejo.navigation.LoggedRouteTopBar
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
    fun productRouteHeaderShowsTitleAndInvokesBackAction() {
        var backPressCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                LoggedRouteTopBar(
                    route = LoggedRoutes.PRODUCT,
                    onBackPress = { backPressCount++ }
                )
            }
        }

        composeRule.onNodeWithText("Novo Produto").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Voltar").performClick()

        composeRule.runOnIdle {
            org.junit.Assert.assertEquals(1, backPressCount)
        }
    }
}
