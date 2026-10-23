package com.cabral.lucrovarejo.ui.screens.terminalform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.toRateBps
import com.cabral.lucrovarejo.domain.payment.usecase.AddTerminalFeeUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.CreateTerminalUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalFeesUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.ResolveFeeForDateUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.UpdateTerminalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TerminalFormViewModel @Inject constructor(
    private val getTerminalFees: GetTerminalFeesUseCase,
    private val createTerminal: CreateTerminalUseCase,
    private val updateTerminal: UpdateTerminalUseCase,
    private val addTerminalFee: AddTerminalFeeUseCase,
    private val resolveFeeForDate: ResolveFeeForDateUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TerminalFormUiState(isLoading = false))
    val uiState: StateFlow<TerminalFormUiState> = _uiState.asStateFlow()

    private var existingFees: List<TerminalFeeModel> = emptyList()

    // Chamado a partir da tela (LaunchedEffect) com a maquininha vinda do back stack, se houver.
    fun loadTerminal(terminal: PaymentTerminalModel?) {
        val terminalKey = terminal?.key ?: return
        if (_uiState.value.terminalId == terminalKey) return
        _uiState.update { it.copy(terminalId = terminalKey, isLoading = true) }
        viewModelScope.launch {
            existingFees = runCatching { getTerminalFees(terminalKey).first() }.getOrDefault(emptyList())
            _uiState.update { state ->
                state.copy(
                    name = terminal.name,
                    selectedMethods = terminal.enabledMethods,
                    cashChargesFee = existingFees.any { it.method == TerminalPaymentType.CASH },
                    availableBrands = terminal.brands,
                    selectedDebitBrandIds = existingFees
                        .filter { it.method == TerminalPaymentType.DEBIT }
                        .mapNotNull { it.brandId }
                        .toSet(),
                    selectedCreditBrandIds = existingFees
                        .filter { it.method == TerminalPaymentType.CREDIT }
                        .mapNotNull { it.brandId }
                        .toSet(),
                    creditInstallmentsByBrand = existingFees
                        .filter { it.method == TerminalPaymentType.CREDIT }
                        .groupBy { it.brandId.orEmpty() }
                        .mapValues { (_, fees) -> fees.mapNotNull { it.installments }.distinct().sorted() },
                    isLoading = false
                )
            }
            recomputeFeeEntries()
        }
    }


    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onToggleMethod(method: TerminalPaymentType) {
        _uiState.update { state ->
            val selected = state.selectedMethods.toMutableSet()
            if (!selected.add(method)) selected.remove(method)
            state.copy(selectedMethods = selected, methodsError = null)
        }
        recomputeFeeEntries()
    }

    fun onCashChargesFeeToggle(enabled: Boolean) {
        _uiState.update { it.copy(cashChargesFee = enabled) }
        recomputeFeeEntries()
    }

    // Bandeira é cadastrada diretamente na maquininha; o próprio nome (normalizado) é usado como id.
    fun onAddBrand(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        _uiState.update { state ->
            if (state.availableBrands.any { it.equals(trimmed, ignoreCase = true) }) return@update state
            state.copy(availableBrands = state.availableBrands + trimmed)
        }
    }

    fun onToggleDebitBrand(brandId: String) {
        _uiState.update { state ->
            val selected = state.selectedDebitBrandIds.toMutableSet()
            if (!selected.add(brandId)) selected.remove(brandId)
            state.copy(selectedDebitBrandIds = selected)
        }
        recomputeFeeEntries()
    }

    fun onToggleCreditBrand(brandId: String) {
        _uiState.update { state ->
            val selected = state.selectedCreditBrandIds.toMutableSet()
            if (!selected.add(brandId)) selected.remove(brandId)
            state.copy(selectedCreditBrandIds = selected)
        }
        recomputeFeeEntries()
    }

    fun onAddCreditInstallment(brandId: String, installment: Int) {
        _uiState.update { state ->
            val current = state.creditInstallmentsByBrand[brandId].orEmpty()
            if (installment in current) return@update state
            state.copy(
                creditInstallmentsByBrand = state.creditInstallmentsByBrand +
                    (brandId to (current + installment).sorted())
            )
        }
        recomputeFeeEntries()
    }

    fun onFeeRateChange(entryId: String, text: String) {
        _uiState.update { state ->
            state.copy(feeEntries = state.feeEntries.map { entry ->
                if (entry.id == entryId) entry.copy(rateText = text, error = null) else entry
            })
        }
    }

    fun onFeeDateChange(entryId: String, millis: Long) {
        _uiState.update { state ->
            state.copy(feeEntries = state.feeEntries.map { entry ->
                if (entry.id == entryId) entry.copy(effectiveFromMillis = millis, error = null) else entry
            })
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = R.string.terminal_form_name_required) }
            return
        }
        if (state.selectedMethods.isEmpty()) {
            _uiState.update { it.copy(methodsError = R.string.terminal_form_methods_required) }
            return
        }

        val invalidEntries = state.feeEntries.filter { entry ->
            entry.rateText.isNotBlank() && entry.effectiveFromMillis != null && entry.parseRateBpsOrNull() == null
        }
        if (invalidEntries.isNotEmpty()) {
            _uiState.update { s ->
                s.copy(feeEntries = s.feeEntries.map { entry ->
                    if (entry.id in invalidEntries.map { it.id }) {
                        entry.copy(error = R.string.terminal_form_fee_rate_invalid)
                    } else entry
                })
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            runCatching {
                val terminal = PaymentTerminalModel(
                    key = state.terminalId,
                    name = state.name.trim(),
                    active = true,
                    enabledMethods = state.selectedMethods,
                    brands = state.availableBrands
                )
                val saved = if (state.terminalId == null) createTerminal(terminal) else updateTerminal(terminal)
                val terminalId = requireNotNull(saved.key)

                state.feeEntries
                    .filter { it.rateText.isNotBlank() && it.effectiveFromMillis != null }
                    .forEach { entry ->
                        val rateBps = entry.parseRateBpsOrNull() ?: return@forEach
                        addTerminalFee(
                            TerminalFeeModel(
                                terminalId = terminalId,
                                method = entry.method,
                                brandId = entry.brandId,
                                installments = entry.installments,
                                rateBps = rateBps,
                                effectiveFrom = Date(entry.effectiveFromMillis!!)
                            )
                        )
                    }
                terminalId
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { failure ->
                val errorRes = when ((failure as? PaymentFailure)?.reason) {
                    PaymentFailure.Reason.DUPLICATE_EFFECTIVE_DATE -> R.string.terminal_form_fee_duplicate_date
                    else -> R.string.terminal_form_save_error
                }
                _uiState.update { it.copy(isSaving = false, saveError = errorRes) }
            }
        }
    }

    private fun recomputeFeeEntries() {
        _uiState.update { state ->
            val entries = mutableListOf<FeeEntryUi>()
            val now = Date()

            if (state.selectedMethods.contains(TerminalPaymentType.CASH) && state.cashChargesFee) {
                entries += buildEntry(TerminalPaymentType.CASH, null, null, null, now)
            }
            if (state.selectedMethods.contains(TerminalPaymentType.PIX)) {
                entries += buildEntry(TerminalPaymentType.PIX, null, null, null, now)
            }
            if (state.selectedMethods.contains(TerminalPaymentType.DEBIT)) {
                state.selectedDebitBrandIds.forEach { brandId ->
                    entries += buildEntry(TerminalPaymentType.DEBIT, brandId, brandId, null, now)
                }
            }
            if (state.selectedMethods.contains(TerminalPaymentType.CREDIT)) {
                state.selectedCreditBrandIds.forEach { brandId ->
                    state.creditInstallmentsByBrand[brandId].orEmpty().forEach { installment ->
                        entries += buildEntry(TerminalPaymentType.CREDIT, brandId, brandId, installment, now)
                    }
                }
            }

            // Preserva o que o usuário já digitou nos slots que continuam existindo.
            val previousById = state.feeEntries.associateBy { it.id }
            val merged = entries.map { entry ->
                previousById[entry.id]?.copy(currentRateBps = entry.currentRateBps) ?: entry
            }
            state.copy(feeEntries = merged)
        }
    }

    private fun buildEntry(
        method: TerminalPaymentType,
        brandId: String?,
        brandName: String?,
        installments: Int?,
        date: Date,
    ): FeeEntryUi {
        val feesForKey = existingFees.filter {
            it.method == method && it.brandId == brandId && it.installments == installments
        }
        val currentFee = resolveFeeForDate(feesForKey, date)
        return FeeEntryUi(
            method = method,
            brandId = brandId,
            brandName = brandName,
            installments = installments,
            effectiveFromMillis = date.time,
            currentRateBps = currentFee?.rateBps
        )
    }

}

private fun FeeEntryUi.parseRateBpsOrNull(): Int? {
    val normalized = rateText.replace(',', '.')
    val value = normalized.toBigDecimalOrNull() ?: return null
    if (value < BigDecimal.ZERO || value > BigDecimal.valueOf(100)) return null
    return runCatching { value.toRateBps() }.getOrNull()
}
