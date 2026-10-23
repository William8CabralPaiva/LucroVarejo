package com.cabral.lucrovarejo.ui.screens.terminals

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel

data class PaymentTerminalsUiState(
    val items: List<PaymentTerminalModel> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
