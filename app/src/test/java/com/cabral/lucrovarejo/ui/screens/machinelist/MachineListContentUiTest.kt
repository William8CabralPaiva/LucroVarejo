package com.cabral.lucrovarejo.ui.screens.machinelist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MachineListContentUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysEmptyStateAndForwardsAddAction() {
        var added = false
        composeRule.setContent {
            LucroVarejoTheme {
                MachineListContent(
                    state = MachineListUiState(isLoading = false),
                    onAddTerminal = { added = true },
                    onArchive = {}
                )
            }
        }

        composeRule.onNodeWithText("Nenhuma maquininha cadastrada").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Cadastre suas maquininhas para registrar a forma de pagamento e a tarifa nas vendas"
        ).assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Adicionar maquininha")[1].performClick()
        composeRule.runOnIdle { assertTrue(added) }
    }

    @Test
    fun displaysTerminalsInsteadOfEmptyState() {
        composeRule.setContent {
            LucroVarejoTheme {
                MachineListContent(
                    state = MachineListUiState(
                        isLoading = false,
                        items = listOf(PaymentTerminalModel(key = "1", name = "Stone"))
                    ),
                    onAddTerminal = {},
                    onArchive = {}
                )
            }
        }

        composeRule.onNodeWithText("Stone").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma maquininha cadastrada").assertDoesNotExist()
    }
}
