package com.cabral.lucrovarejo.ui.screens.terminalform

import androidx.annotation.StringRes
import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType

/** Um "slot" de tarifa a preencher no formulário: tipo + bandeira (opcional) + parcela (opcional). */
data class FeeEntryUi(
    val method: TerminalPaymentType,
    val brandId: String? = null,
    val brandName: String? = null,
    val installments: Int? = null,
    val rateText: String = "",
    val effectiveFromMillis: Long? = null,
    val currentRateBps: Int? = null,
    @StringRes val error: Int? = null,
) {
    /** Chave estável usada como `key` em listas e para localizar o slot ao editar. */
    val id: String
        get() = listOf(method.name, brandId.orEmpty(), installments?.toString().orEmpty())
            .joinToString("|")
}

data class TerminalFormUiState(
    val terminalId: String? = null,
    val name: String = "",
    @StringRes val nameError: Int? = null,
    val selectedMethods: Set<TerminalPaymentType> = emptySet(),
    @StringRes val methodsError: Int? = null,
    val cashChargesFee: Boolean = false,
    val availableBrands: List<CardBrandModel> = emptyList(),
    val selectedDebitBrandIds: Set<String> = emptySet(),
    val selectedCreditBrandIds: Set<String> = emptySet(),
    val creditInstallmentsByBrand: Map<String, List<Int>> = emptyMap(),
    val feeEntries: List<FeeEntryUi> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val saveError: Int? = null,
    val saved: Boolean = false,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && selectedMethods.isNotEmpty() && !isSaving
}
