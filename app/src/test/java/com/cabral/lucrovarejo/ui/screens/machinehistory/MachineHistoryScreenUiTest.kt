package com.cabral.lucrovarejo.ui.screens.machinehistory

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.usecase.AddPaymentMachineUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetFilteredCurrentTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetPaymentMachinesUseCase
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date
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
@Config(sdk = [35])
class MachineHistoryScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()

    private fun render(state: MachineHistoryUiState) {
        composeRule.setContent {
            LucroVarejoTheme {
                MachineHistoryContent(
                    state = state,
                    onShowAddDialog = { calls += "show" },
                    onDismissAddDialog = { calls += "dismiss" },
                    onRateChange = { calls += "rate:$it" },
                    onDateChange = { calls += "date" },
                    onConfirmAdd = { calls += "confirm" }
                )
            }
        }
    }

    private fun tariff(rate: Int, from: Long) = PaymentMachineModel(
        "t1", TerminalPaymentType.PIX, null, null, rate, Date(from)
    )

    @Test
    fun loadingErrorAndEmptyStates() {
        render(MachineHistoryUiState(isLoading = true))
        composeRule.onNodeWithText("Nova tarifa").assertExists()
        composeRule.onNodeWithText("Nenhuma tarifa cadastrada.").assertDoesNotExist()
    }

    @Test
    fun showsLoadError() {
        render(MachineHistoryUiState(loadError = R.string.machine_history_load_error))
        composeRule.onNodeWithText("Não foi possível carregar o histórico de tarifas.").assertExists()
    }

    @Test
    fun showsEmptyMessageAndAddButton() {
        render(MachineHistoryUiState())

        composeRule.onNodeWithText("Nenhuma tarifa cadastrada.").assertExists()
        composeRule.onNodeWithText("Nova tarifa").performClick()
        assertEquals(listOf("show"), calls)
    }

    @Test
    fun listsTariffsAndMarksCurrentOne() {
        val current = tariff(349, 2_000)
        render(
            MachineHistoryUiState(
                tariffs = listOf(current, tariff(199, 1_000)),
                currentTariffKey = current.historyKey()
            )
        )

        composeRule.onNodeWithText("3.49%").assertExists()
        composeRule.onNodeWithText("1.99%").assertExists()
        composeRule.onNodeWithText("Atual").assertExists()
    }

    @Test
    fun dialogForwardsInputAndActionsAndShowsErrors() {
        render(
            MachineHistoryUiState(
                showAddDialog = true,
                effectiveFromMillis = 0L,
                rateError = R.string.terminal_form_tariff_rate_invalid,
                dateError = R.string.terminal_form_tariff_rate_required,
                saveError = R.string.terminal_form_tariff_duplicate_date
            )
        )

        composeRule.onNodeWithText("Informe a tarifa.").assertExists()
        composeRule.onNodeWithText(
            "Já existe uma tarifa com essa data de início para essa combinação. Escolha outra data."
        ).assertExists()
        composeRule.onAllNodesWithSetText().onFirst().performTextInput("2")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Cancelar").performClick()

        assertTrue(calls.any { it.startsWith("rate:") })
        assertTrue(calls.containsAll(listOf("confirm", "dismiss")))
    }

    @Test
    fun savingDisablesConfirm() {
        render(MachineHistoryUiState(showAddDialog = true, isSaving = true, effectiveFromMillis = 0L))
        composeRule.onNodeWithText("Salvar").assertIsNotEnabled()
    }

    @Test
    fun statefulScreenLoadsSlotAndAddsTariff() {
        val repository = object : PaymentMachineRepository {
            val tariffs = MutableStateFlow(listOf(tariff(100, 1_000)))
            val added = mutableListOf<PaymentMachineModel>()
            override suspend fun addTariff(tariff: PaymentMachineModel): PaymentMachineModel {
                added += tariff
                return tariff
            }

            override fun observeTariffs(terminalId: String): Flow<List<PaymentMachineModel>> = tariffs
            override suspend fun getTariffsSnapshot(terminalId: String) = tariffs.value
        }
        val viewModel = MachineHistoryViewModel(
            GetPaymentMachinesUseCase(repository),
            AddPaymentMachineUseCase(repository),
            GetFilteredCurrentTariffUseCase()
        )
        composeRule.setContent {
            LucroVarejoTheme {
                MachineHistoryScreen(
                    slot = PaymentMachineHistoryArgs("t1", TerminalPaymentType.PIX),
                    viewModel = viewModel
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("1.00%").assertExists()

        composeRule.onNodeWithText("Nova tarifa").performClick()
        composeRule.onAllNodesWithSetText().onFirst().performTextInput("2,5")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.waitForIdle()

        assertEquals(250, repository.added.single().rateBps)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithSetText() =
        onAllNodes(hasSetTextAction())
}
