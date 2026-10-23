package com.cabral.lucrovarejo.ui.screens.feehistory

import androidx.annotation.StringRes
import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType

data class FeeHistoryUiState(
    val terminalId: String = "",
    val method: TerminalPaymentType = TerminalPaymentType.CASH,
    val brandId: String? = null,
    val brandName: String? = null,
    val installments: Int? = null,
    val fees: List<TerminalFeeModel> = emptyList(),
    val currentFeeKey: String? = null,
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

/** Chave estável de uma tarifa na lista (usada para marcar a tarifa "Atual"). */
fun TerminalFeeModel.historyKey(): String = "${effectiveFrom.time}|$rateBps"
