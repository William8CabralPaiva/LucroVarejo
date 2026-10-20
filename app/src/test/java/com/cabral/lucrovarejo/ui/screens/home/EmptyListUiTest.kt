package com.cabral.lucrovarejo.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.ui.components.EmptyList
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EmptyListUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysEmptyStateAndForwardsAddPurchaseTap() {
        var clickCount = 0

        composeRule.setContent {
            LucroVarejoTheme {
                EmptyList(onAddPurchase = { clickCount++ })
            }
        }

        composeRule.onNodeWithText("Nenhuma venda registrada").assertIsDisplayed()
        composeRule.onNodeWithText(
            "As vendas que você fizer aparecerão aqui para acompanhar seu desempenho"
        ).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Adicionar venda")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }
}
