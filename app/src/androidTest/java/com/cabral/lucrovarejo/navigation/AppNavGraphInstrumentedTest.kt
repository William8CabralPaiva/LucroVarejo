package com.cabral.lucrovarejo.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavGraphInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomNavigationMovesBetweenLoggedScreens() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppNavGraph(startDestination = LoggedRoutes.HOME)
            }
        }

        composeRule.onNodeWithText("Bem-vindo ao Lucro Varejo!").assertIsDisplayed()
        composeRule.onNodeWithText(label(R.string.bottom_nav_sales)).performClick()
        composeRule.onNodeWithText("Suas vendas aparecerão aqui.").assertIsDisplayed()
        composeRule.onNodeWithText(label(R.string.bottom_nav_config)).performClick()
        composeRule.onNodeWithText("Aparência").assertIsDisplayed()
        composeRule.onNodeWithText(label(R.string.bottom_nav_home)).performClick()
        composeRule.onNodeWithText("Bem-vindo ao Lucro Varejo!").assertIsDisplayed()
    }

    private fun label(resourceId: Int) = composeRule.activity.getString(resourceId)
}
