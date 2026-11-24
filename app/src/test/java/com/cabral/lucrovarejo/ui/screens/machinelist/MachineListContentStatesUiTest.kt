package com.cabral.lucrovarejo.ui.screens.machinelist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MachineListContentStatesUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val archived = mutableListOf<String>()
    private val edited = mutableListOf<String>()

    private fun render(state: MachineListUiState) {
        composeRule.setContent {
            LucroVarejoTheme {
                MachineListContent(
                    state = state,
                    onAddTerminal = {},
                    onArchive = { archived += it },
                    onEditTerminal = { edited += it.name }
                )
            }
        }
    }

    private val items = listOf(
        PaymentTerminalModel(key = "1", name = "Stone"),
        PaymentTerminalModel(key = "2", name = "Cielo", active = false)
    )

    @Test
    fun errorStateShowsMessage() {
        render(MachineListUiState(isLoading = false, hasError = true))
        composeRule.onNodeWithText("Não foi possível carregar as maquininhas.").assertIsDisplayed()
    }

    @Test
    fun loadingHidesEmptyState() {
        render(MachineListUiState(isLoading = true))
        composeRule.onNodeWithText("Nenhuma maquininha cadastrada").assertDoesNotExist()
    }

    @Test
    fun listMarksArchivedAndOnlyActiveCanBeArchived() {
        render(MachineListUiState(isLoading = false, items = items))

        composeRule.onNodeWithText("Arquivada").assertIsDisplayed()
        composeRule.onNodeWithText("Arquivar").assertIsDisplayed()
    }

    @Test
    fun archiveRequiresConfirmation() {
        render(MachineListUiState(isLoading = false, items = items))

        composeRule.onNodeWithText("Arquivar").performClick()
        composeRule.onNodeWithText("Arquivar maquininha").assertIsDisplayed()
        composeRule.onNodeWithText("Tem certeza que deseja arquivar a maquininha Stone?").assertIsDisplayed()
        composeRule.onNodeWithText("Confirmar").performClick()

        assertEquals(listOf("1"), archived)
        composeRule.onNodeWithText("Arquivar maquininha").assertDoesNotExist()
    }

    @Test
    fun cancelingArchiveDoesNothing() {
        render(MachineListUiState(isLoading = false, items = items))

        composeRule.onNodeWithText("Arquivar").performClick()
        composeRule.onNodeWithText("Cancelar").performClick()

        assertEquals(emptyList<String>(), archived)
        composeRule.onNodeWithText("Arquivar maquininha").assertDoesNotExist()
    }

    @Test
    fun clickingCardOpensEdit() {
        render(MachineListUiState(isLoading = false, items = items))

        composeRule.onNodeWithText("Stone").performClick()

        assertEquals(listOf("Stone"), edited)
    }
}
