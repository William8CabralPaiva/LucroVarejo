package com.cabral.lucrovarejo.ui.screens.machinehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.parseRatePercentToBps
import com.cabral.lucrovarejo.domain.payment.usecase.AddPaymentMachineUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetPaymentMachinesUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetFilteredCurrentTariffUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MachineHistoryViewModel @Inject constructor(
    private val getPaymentMachines: GetPaymentMachinesUseCase,
    private val addPaymentMachine: AddPaymentMachineUseCase,
    private val getFilteredCurrentTariff: GetFilteredCurrentTariffUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MachineHistoryUiState(isLoading = true))
    val uiState: StateFlow<MachineHistoryUiState> = _uiState.asStateFlow()

    private var loadedSlot: PaymentMachineHistoryArgs? = null

    // Chamado a partir da tela (LaunchedEffect) com o slot vindo do back stack.
    fun loadSlot(slot: PaymentMachineHistoryArgs) {
        if (loadedSlot == slot) return
        loadedSlot = slot
        _uiState.update {
            MachineHistoryUiState(
                terminalId = slot.terminalId,
                method = slot.method,
                brandId = slot.brandId,
                installments = slot.installments,
                isLoading = true
            )
        }

        // A bandeira agora Ã© cadastrada diretamente na maquininha; o prÃ³prio nome Ã© o brandId.
        _uiState.update { it.copy(brandName = slot.brandId) }

        viewModelScope.launch {
            getPaymentMachines(slot.terminalId)
                .onStart { _uiState.update { it.copy(isLoading = true, loadError = null) } }
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false, loadError = R.string.machine_history_load_error)
                    }
                }
                .collect { allTariffs ->
                    val tariffsForSlot = allTariffs.filter {
                        it.method == slot.method &&
                            it.brandId == slot.brandId &&
                            it.installments == slot.installments
                    }.sortedByDescending { it.effectiveFrom }
                    val current = getFilteredCurrentTariff(tariffsForSlot, Date())
                    _uiState.update {
                        it.copy(
                            tariffs = tariffsForSlot,
                            currentTariffKey = current?.historyKey(),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onShowAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                rateText = "",
                effectiveFromMillis = Date().time,
                rateError = null,
                dateError = null,
                saveError = null
            )
        }
    }

    fun onDismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun onRateChange(value: String) {
        _uiState.update { it.copy(rateText = value, rateError = null) }
    }

    fun onDateChange(millis: Long) {
        _uiState.update { it.copy(effectiveFromMillis = millis, dateError = null) }
    }

    fun onConfirmAdd() {
        val slot = loadedSlot ?: return
        val state = _uiState.value
        val rateBps = parseRatePercentToBps(state.rateText)
        val effectiveFromMillis = state.effectiveFromMillis

        when {
            rateBps == null ->
                _uiState.update { it.copy(rateError = R.string.terminal_form_tariff_rate_invalid) }

            effectiveFromMillis == null ->
                _uiState.update { it.copy(dateError = R.string.terminal_form_tariff_rate_required) }

            else -> saveTariff(slot, rateBps, effectiveFromMillis)
        }
    }

    private fun saveTariff(slot: PaymentMachineHistoryArgs, rateBps: Int, effectiveFromMillis: Long) {
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            runCatching {
                addPaymentMachine(
                    PaymentMachineModel(
                        terminalId = slot.terminalId,
                        method = slot.method,
                        brandId = slot.brandId,
                        installments = slot.installments,
                        rateBps = rateBps,
                        effectiveFrom = Date(effectiveFromMillis)
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, showAddDialog = false) }
            }.onFailure { failure ->
                val errorRes = when ((failure as? PaymentFailure)?.reason) {
                    PaymentFailure.Reason.DUPLICATE_EFFECTIVE_DATE -> R.string.terminal_form_tariff_duplicate_date
                    else -> R.string.terminal_form_tariff_save_error
                }
                _uiState.update { it.copy(isSaving = false, saveError = errorRes) }
            }
        }
    }
}
