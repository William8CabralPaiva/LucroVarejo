package com.cabral.lucrovarejo.ui.screens.machinehistory

import androidx.annotation.StringRes
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType

data class MachineHistoryUiState(
    val terminalId: String = "",
    val method: TerminalPaymentType = TerminalPaymentType.CASH,
    val brandId: String? = null,
    val brandName: String? = null,
    val installments: Int? = null,
    val tariffs: List<PaymentMachineModel> = emptyList(),
    val currentTariffKey: String? = null,
    val isLoading: Boolean = false,
    @StringRes val loadError: Int? = null,
    val showAddDialog: Boolean = false,
    val rateText: String = "",
    val effectiveFromMillis: Long? = null,
    @StringRes val rateError: Int? = null,
    @StringRes val dateError: Int? = null,
    val isSaving: Boolean = false,
    @StringRes val saveError: Int? = null,
)

/** Chave estÃ¡vel de uma tarifa na lista (usada para marcar a tarifa "Atual"). */
fun PaymentMachineModel.historyKey(): String = "${effectiveFrom.time}|$rateBps"
