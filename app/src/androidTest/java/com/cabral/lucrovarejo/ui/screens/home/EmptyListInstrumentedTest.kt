package com.cabral.lucrovarejo.ui.screens.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmptyListInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysAddPurchaseActionAndForwardsTap() {
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
        composeRule.onNodeWithText("Adicionar venda").assertIsDisplayed().performClick()
        composeRule.onNodeWithContentDescription("Adicionar venda").assertIsDisplayed()

        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }
}
