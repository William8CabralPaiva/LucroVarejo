package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LoggedRouteTopBarUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysTitlesForEveryKnownRoute() {
        val currentRoute = mutableStateOf(LoggedRoutes.HOME)
        val routesAndTitles = listOf(
            LoggedRoutes.HOME to "Controle de Vendas",
            LoggedRoutes.PRODUCTS to "Produtos",
            LoggedRoutes.ADD_PRODUCT to "Novo Produto",
            LoggedRoutes.EDIT_PRODUCT to "Editar Produto",
            LoggedRoutes.SALES to "Vendas",
            LoggedRoutes.SUMMARY to "Resumo",
            LoggedRoutes.CONFIG to "Configurações",
            LoggedRoutes.ADD_PURCHASE to "Nova Venda",
            LoggedRoutes.EDIT_SALES to "Editar Venda"
        )

        composeRule.setContent {
            LucroVarejoTheme {
                LoggedRouteTopBar(route = currentRoute.value, onBackPress = {})
            }
        }

        routesAndTitles.forEach { (route, title) ->
            composeRule.runOnIdle { currentRoute.value = route }
            composeRule.onNodeWithText(title).assertIsDisplayed()
        }
    }

    @Test
    fun showsBackButtonOnlyForNestedRoutesAndForwardsItsClick() {
        val route = mutableStateOf(LoggedRoutes.ADD_PRODUCT)
        var backPressed = false
        composeRule.setContent {
            LucroVarejoTheme {
                LoggedRouteTopBar(
                    route = route.value,
                    onBackPress = { backPressed = true }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Voltar").performClick()
        composeRule.runOnIdle { assertTrue(backPressed) }

        composeRule.runOnIdle { route.value = LoggedRoutes.HOME }
        composeRule.onAllNodesWithContentDescription("Voltar").assertCountEquals(0)
    }

    @Test
    fun doesNotRenderForAnUnknownRoute() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoggedRouteTopBar(route = "unknown", onBackPress = {})
            }
        }

        composeRule.onAllNodesWithText("Controle de Vendas").assertCountEquals(0)
    }
}
