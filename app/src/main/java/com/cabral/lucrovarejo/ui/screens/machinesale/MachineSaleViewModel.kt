package com.cabral.lucrovarejo.ui.screens.machinesale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.toRateBps
import com.cabral.lucrovarejo.domain.payment.usecase.AddPaymentMachineUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.CreateTerminalUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetPaymentMachinesUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetFilteredCurrentTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.UpdateTerminalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MachineSaleViewModel @Inject constructor(
    private val getPaymentMachines: GetPaymentMachinesUseCase,
    private val createTerminal: CreateTerminalUseCase,
    private val updateTerminal: UpdateTerminalUseCase,
    private val addPaymentMachine: AddPaymentMachineUseCase,
    private val getFilteredCurrentTariff: GetFilteredCurrentTariffUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MachineSaleUiState(isLoading = false))
    val uiState: StateFlow<MachineSaleUiState> = _uiState.asStateFlow()

    // Emitido a cada salvamento com sucesso (a tela mostra o Snackbar).
    private val savedChannel = Channel<Unit>(Channel.BUFFERED)
    val saved: Flow<Unit> = savedChannel.receiveAsFlow()

    // Emitido apos "Salvar e voltar": a tela deve voltar para a lista.
    private val completionChannel = Channel<Unit>(Channel.BUFFERED)
    val completions: Flow<Unit> = completionChannel.receiveAsFlow()

    private var existingTariffs: List<PaymentMachineModel> = emptyList()

    // Chamado a partir da tela (LaunchedEffect) com a maquininha vinda do back stack, se houver.
    fun loadTerminal(terminal: PaymentTerminalModel?) {
        val terminalKey = terminal?.key ?: return
        if (_uiState.value.terminalId == terminalKey) return
        _uiState.update { it.copy(terminalId = terminalKey, isLoading = true) }
        viewModelScope.launch {
            existingTariffs = runCatching { getPaymentMachines(terminalKey).first() }.getOrDefault(emptyList())
            _uiState.update { state ->
                state.copy(
                    name = terminal.name,
                    selectedMethods = terminal.enabledMethods,
                    cashChargesTariff = existingTariffs.any { it.method == TerminalPaymentType.CASH },
                    availableBrands = terminal.brands,
                    selectedDebitBrandIds = existingTariffs
                        .filter { it.method == TerminalPaymentType.DEBIT }
                        .mapNotNull { it.brandId }
                        .toSet(),
                    selectedCreditBrandIds = existingTariffs
                        .filter { it.method == TerminalPaymentType.CREDIT }
                        .mapNotNull { it.brandId }
                        .toSet(),
                    creditInstallmentsByBrand = existingTariffs
                        .filter { it.method == TerminalPaymentType.CREDIT }
                        .groupBy { it.brandId.orEmpty() }
                        .mapValues { (_, tariffs) -> tariffs.mapNotNull { it.installments }.distinct().sorted() },
                    isLoading = false
                )
            }
            recomputeTariffEntries()
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
        recomputeTariffEntries()
    }

    fun onCashChargesTariffToggle(enabled: Boolean) {
        _uiState.update { it.copy(cashChargesTariff = enabled) }
        recomputeTariffEntries()
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
        recomputeTariffEntries()
    }

    fun onToggleCreditBrand(brandId: String) {
        _uiState.update { state ->
            val selected = state.selectedCreditBrandIds.toMutableSet()
            if (!selected.add(brandId)) selected.remove(brandId)
            state.copy(selectedCreditBrandIds = selected)
        }
        recomputeTariffEntries()
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
        recomputeTariffEntries()
    }

    fun onTariffRateChange(entryId: String, text: String) {
        _uiState.update { state ->
            state.copy(tariffEntries = state.tariffEntries.map { entry ->
                if (entry.id == entryId) entry.copy(rateText = text, error = null) else entry
            })
        }
    }

    fun onTariffDateChange(entryId: String, millis: Long) {
        _uiState.update { state ->
            state.copy(tariffEntries = state.tariffEntries.map { entry ->
                if (entry.id == entryId) entry.copy(effectiveFromMillis = millis, error = null) else entry
            })
        }
    }

    fun onSave(closeAfter: Boolean) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = R.string.terminal_form_name_required) }
            return
        }
        if (state.selectedMethods.isEmpty()) {
            _uiState.update { it.copy(methodsError = R.string.terminal_form_methods_required) }
            return
        }

        val invalidEntries = state.tariffEntries.filter { entry ->
            entry.rateText.isNotBlank() && entry.effectiveFromMillis != null && entry.parseRateBpsOrNull() == null
        }
        if (invalidEntries.isNotEmpty()) {
            _uiState.update { s ->
                s.copy(tariffEntries = s.tariffEntries.map { entry ->
                    if (entry.id in invalidEntries.map { it.id }) {
                        entry.copy(error = R.string.terminal_form_tariff_rate_invalid)
                    } else entry
                })
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, closeAfterSave = closeAfter, saveError = null) }
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
                // Evita criar outra maquininha se um novo "Salvar" ocorrer apos falha parcial ou sucesso.
                _uiState.update { it.copy(terminalId = terminalId) }

                state.tariffEntries
                    .filter { it.rateText.isNotBlank() && it.effectiveFromMillis != null }
                    .forEach { entry ->
                        val rateBps = entry.parseRateBpsOrNull() ?: return@forEach
                        addPaymentMachine(
                            PaymentMachineModel(
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
            }.onSuccess { terminalId ->
                savedChannel.send(Unit)
                if (closeAfter) {
                    _uiState.update { it.copy(isSaving = false) }
                    completionChannel.send(Unit)
                } else {
                    existingTariffs = runCatching { getPaymentMachines(terminalId).first() }
                        .getOrDefault(existingTariffs)
                    _uiState.update { s ->
                        s.copy(
                            isSaving = false,
                            tariffEntries = s.tariffEntries.map {
                                it.copy(rateText = "", effectiveFromMillis = null, error = null)
                            }
                        )
                    }
                    recomputeTariffEntries()
                }
            }.onFailure { failure ->
                val errorRes = when ((failure as? PaymentFailure)?.reason) {
                    PaymentFailure.Reason.DUPLICATE_EFFECTIVE_DATE -> R.string.terminal_form_tariff_duplicate_date
                    else -> R.string.terminal_form_save_error
                }
                _uiState.update { it.copy(isSaving = false, saveError = errorRes) }
            }
        }
    }

    private fun recomputeTariffEntries() {
        _uiState.update { state ->
            val entries = mutableListOf<TariffEntryUi>()
            val now = Date()

            if (state.selectedMethods.contains(TerminalPaymentType.CASH) && state.cashChargesTariff) {
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
            val previousById = state.tariffEntries.associateBy { it.id }
            val merged = entries.map { entry ->
                previousById[entry.id]?.copy(currentRateBps = entry.currentRateBps) ?: entry
            }
            state.copy(tariffEntries = merged)
        }
    }

    private fun buildEntry(
        method: TerminalPaymentType,
        brandId: String?,
        brandName: String?,
        installments: Int?,
        date: Date,
    ): TariffEntryUi {
        val tariffsForKey = existingTariffs.filter {
            it.method == method && it.brandId == brandId && it.installments == installments
        }
        val currentTariff = getFilteredCurrentTariff(tariffsForKey, date)
        return TariffEntryUi(
            method = method,
            brandId = brandId,
            brandName = brandName,
            installments = installments,
            effectiveFromMillis = date.time,
            currentRateBps = currentTariff?.rateBps
        )
    }

}

private fun TariffEntryUi.parseRateBpsOrNull(): Int? {
    val normalized = rateText.replace(',', '.')
    val value = normalized.toBigDecimalOrNull() ?: return null
    if (value < BigDecimal.ZERO || value > BigDecimal.valueOf(100)) return null
    return runCatching { value.toRateBps() }.getOrNull()
}
