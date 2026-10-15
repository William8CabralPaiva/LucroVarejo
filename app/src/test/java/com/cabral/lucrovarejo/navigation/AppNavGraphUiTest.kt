package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavGraphUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNavigationOpensEachLoggedDestination() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppNavGraph(startDestination = LoggedRoutes.HOME)
            }
        }

        composeRule.onNodeWithText("Bem-vindo ao Lucro Varejo!").assertIsDisplayed()

        composeRule.onNodeWithText("Vendas").performClick()
        composeRule.onNodeWithText("Suas vendas aparecerão aqui.").assertIsDisplayed()

        composeRule.onNodeWithText("Resumo").performClick()
        composeRule.onNodeWithText("O resumo das vendas aparecerá aqui.").assertIsDisplayed()

        composeRule.onNodeWithText("Configurações").performClick()
        composeRule.onNodeWithText("Aparência").assertIsDisplayed()

        composeRule.onNodeWithText("Início").performClick()
        composeRule.onNodeWithText("Bem-vindo ao Lucro Varejo!").assertIsDisplayed()
    }
}
