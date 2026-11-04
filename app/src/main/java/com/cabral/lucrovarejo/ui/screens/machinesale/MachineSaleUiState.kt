package com.cabral.lucrovarejo.ui.screens.machinesale

import androidx.annotation.StringRes
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType

/** Um "slot" de tarifa a preencher no formulário: tipo + bandeira (opcional) + parcela (opcional). */
data class TariffEntryUi(
    val method: TerminalPaymentType,
    val brandId: String? = null,
    val brandName: String? = null,
    val installments: Int? = null,
    val rateText: String = "",
    // Tarifa vigente pre-preenchida; so grava novo registro se o texto for diferente dela.
    val initialRateText: String = "",
    val effectiveFromMillis: Long? = null,
    val currentRateBps: Int? = null,
    @StringRes val error: Int? = null,
) {
    /** Chave estável usada como `key` em listas e para localizar o slot ao editar. */
    val id: String
        get() = listOf(method.name, brandId.orEmpty(), installments?.toString().orEmpty())
            .joinToString("|")
}

data class MachineSaleUiState(
    val terminalId: String? = null,
    val name: String = "",
    @StringRes val nameError: Int? = null,
    val selectedMethods: Set<TerminalPaymentType> = emptySet(),
    @StringRes val methodsError: Int? = null,
    val cashChargesTariff: Boolean = false,
    // Bandeiras cadastradas diretamente nesta maquininha (não é um catálogo global).
    val availableBrands: List<String> = emptyList(),
    val selectedDebitBrandIds: Set<String> = emptySet(),
    val selectedCreditBrandIds: Set<String> = emptySet(),
    val creditInstallmentsByBrand: Map<String, List<Int>> = emptyMap(),
    val tariffEntries: List<TariffEntryUi> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    // Indica qual botao disparou o salvamento em andamento ("Salvar e voltar" = true).
    val closeAfterSave: Boolean = false,
    @StringRes val saveError: Int? = null,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && selectedMethods.isNotEmpty() && !isSaving
}
