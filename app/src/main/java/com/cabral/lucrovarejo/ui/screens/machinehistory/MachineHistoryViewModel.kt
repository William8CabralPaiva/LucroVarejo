package com.cabral.lucrovarejo.ui.screens.machinehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.toRateBps
import com.cabral.lucrovarejo.domain.payment.usecase.AddTerminalFeeUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalFeesUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.ResolveFeeForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MachineHistoryViewModel @Inject constructor(
    private val getTerminalFees: GetTerminalFeesUseCase,
    private val addTerminalFee: AddTerminalFeeUseCase,
    private val resolveFeeForDate: ResolveFeeForDateUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MachineHistoryUiState(isLoading = true))
    val uiState: StateFlow<MachineHistoryUiState> = _uiState.asStateFlow()

    private var loadedSlot: FeeSlotKey? = null

    // Chamado a partir da tela (LaunchedEffect) com o slot vindo do back stack.
    fun loadSlot(slot: FeeSlotKey) {
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
            getTerminalFees(slot.terminalId)
                .onStart { _uiState.update { it.copy(isLoading = true, loadError = null) } }
                .catch { _uiState.update { state -> state.copy(isLoading = false, loadError = R.string.machine_history_load_error) } }
                .collect { allFees ->
                    val feesForSlot = allFees.filter {
                        it.method == slot.method && it.brandId == slot.brandId && it.installments == slot.installments
                    }.sortedByDescending { it.effectiveFrom }
                    val current = resolveFeeForDate(feesForSlot, Date())
                    _uiState.update {
                        it.copy(
                            fees = feesForSlot,
                            currentFeeKey = current?.historyKey(),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onShowAddDialog() {
        _uiState.update {
            it.copy(showAddDialog = true, rateText = "", effectiveFromMillis = Date().time, rateError = null, dateError = null, saveError = null)
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
        val rateBps = state.rateText.replace(',', '.').toBigDecimalOrNull()
            ?.takeIf { it >= BigDecimal.ZERO && it <= BigDecimal.valueOf(100) }
            ?.let { runCatching { it.toRateBps() }.getOrNull() }

        if (rateBps == null) {
            _uiState.update { it.copy(rateError = R.string.terminal_form_fee_rate_invalid) }
            return
        }
        if (state.effectiveFromMillis == null) {
            _uiState.update { it.copy(dateError = R.string.terminal_form_fee_rate_required) }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            runCatching {
                addTerminalFee(
                    TerminalFeeModel(
                        terminalId = slot.terminalId,
                        method = slot.method,
                        brandId = slot.brandId,
                        installments = slot.installments,
                        rateBps = rateBps,
                        effectiveFrom = Date(state.effectiveFromMillis)
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, showAddDialog = false) }
            }.onFailure { failure ->
                val errorRes = when ((failure as? PaymentFailure)?.reason) {
                    PaymentFailure.Reason.DUPLICATE_EFFECTIVE_DATE -> R.string.terminal_form_fee_duplicate_date
                    else -> R.string.terminal_form_fee_save_error
                }
                _uiState.update { it.copy(isSaving = false, saveError = errorRes) }
            }
        }
    }
}
