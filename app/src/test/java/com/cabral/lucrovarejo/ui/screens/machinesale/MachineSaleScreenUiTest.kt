package com.cabral.lucrovarejo.ui.screens.machinesale

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.usecase.AddPaymentMachineUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.CreateTerminalUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetFilteredCurrentTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetPaymentMachinesUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.UpdateTerminalUseCase
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1400dp")
class MachineSaleScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private class Terminals : PaymentTerminalRepository {
        val created = mutableListOf<PaymentTerminalModel>()
        override suspend fun createTerminal(terminal: PaymentTerminalModel): PaymentTerminalModel {
            created += terminal
            return terminal.copy(key = "new")
        }

        override suspend fun updateTerminal(terminal: PaymentTerminalModel) = terminal
        override suspend fun archiveTerminal(key: String) = Unit
        override fun observeTerminals(): Flow<List<PaymentTerminalModel>> = MutableStateFlow(emptyList())
        override fun observeTerminal(key: String): Flow<PaymentTerminalModel?> = MutableStateFlow(null)
    }

    private class Tariffs : PaymentMachineRepository {
        val stored = mutableListOf<PaymentMachineModel>()
        override suspend fun addTariff(tariff: PaymentMachineModel): PaymentMachineModel {
            stored += tariff
            return tariff
        }

        override fun observeTariffs(terminalId: String): Flow<List<PaymentMachineModel>> =
            MutableStateFlow(stored.filter { it.terminalId == terminalId })

        override suspend fun getTariffsSnapshot(terminalId: String) = stored.filter { it.terminalId == terminalId }
    }

    private val terminals = Terminals()
    private val tariffs = Tariffs()

    private fun viewModel() = MachineSaleViewModel(
        GetPaymentMachinesUseCase(tariffs),
        CreateTerminalUseCase(terminals),
        UpdateTerminalUseCase(terminals),
        AddPaymentMachineUseCase(tariffs),
        GetFilteredCurrentTariffUseCase()
    )

    @Test
    fun savingNewTerminalShowsMessageAndFinishesWhenClosing() {
        val messages = mutableListOf<String>()
        var finished = 0
        composeRule.setContent {
            LucroVarejoTheme {
                MachineSaleScreen(
                    onFinish = { finished++ },
                    onShowMessage = { messages += it },
                    viewModel = viewModel()
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("Stone")
        composeRule.onAllNodesWithText("Pix").onFirst().performClick()
        composeRule.onNode(isToggleable()).performClick()
        composeRule.onNodeWithText("Salvar e voltar").performClick()
        composeRule.waitForIdle()

        assertEquals("Stone", terminals.created.single().name)
        assertTrue(TerminalPaymentType.PIX in terminals.created.single().enabledMethods)
        assertEquals(1, finished)
    }

    @Test
    fun editingTerminalPrefillsName() {
        composeRule.setContent {
            LucroVarejoTheme {
                MachineSaleScreen(
                    terminal = PaymentTerminalModel("Cielo", key = "t1"),
                    viewModel = viewModel()
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Cielo").assertExists()
    }
}
